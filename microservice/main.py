import os
import yt_dlp
import requests
import re
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import Optional
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

from config import settings
YOUTUBE_API_KEY = settings.youtube_api_key

app = FastAPI(title="YouTube Data Harvester", version="1.0.0")

class VideoRequest(BaseModel):
    video_url: str

class VideoMetadata(BaseModel):
    title: str
    description: str
    duration: int
    upload_date: str
    channel: str
    channel_url: str
    view_count: int
    like_count: int
    thumbnail_url: str
    video_url: str

class ChannelRequest(BaseModel):
    channel_id: Optional[str] = None
    channel_name: Optional[str] = None
    channel_url: Optional[str] = None

class ChannelMetadata(BaseModel):
    id: str
    title: str
    description: str
    published_at: str
    thumbnail_url: str
    view_count: int
    subscriber_count: int
    video_count: int
    channel_url: str

class PlaylistItem(BaseModel):
    video_url: str

class PlaylistResponse(BaseModel):
    kind: str
    etag: str
    items: list[str]
    pageInfo: dict

def extract_playlist_id(url: str) -> str:
    """Extract playlist ID from a YouTube playlist URL"""
    if not url.startswith("https://"):
        return url
    m = re.search(r"\?list=([^?&]+)", url)
    if m:
        result = m.group(1)
        return result
    return None

def extract_channel_id_from_handle(channel_handle: str) -> str:
    """Extract channel ID from a channel handle using YouTube Data API v3"""
    if not YOUTUBE_API_KEY:
        raise HTTPException(status_code=500, detail="YouTube Data API key not configured")
    
    try:
        url = "https://www.googleapis.com/youtube/v3/channels"
        
        params = {
            'part': 'id',
            'forHandle': channel_handle.lstrip('@'),
            'key': YOUTUBE_API_KEY
        }
        
        response = requests.get(url, params=params)
        
        if response.status_code != 200:
            raise HTTPException(status_code=response.status_code, detail=f"API request failed: {response.text}")
        
        data = response.json()
        
        if not data.get('items') or len(data['items']) == 0:
            raise HTTPException(status_code=404, detail="Channel handle not found")
        
        channel_id = data['items'][0]['id']
        return channel_id
        
    except Exception as e:
        logger.error(f"Error extracting channel ID from handle {channel_handle}: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Failed to resolve channel handle to ID: {str(e)}")

def get_channel_metadata(channel_identifier: str) -> ChannelMetadata:
    """Extract channel metadata from YouTube using the Data API v3."""
    if not YOUTUBE_API_KEY:
        raise HTTPException(status_code=500, detail="YouTube Data API key not configured")
    
    try:
        channel_id = None
        if channel_identifier.startswith('@'):
            channel_id = extract_channel_id_from_handle(channel_identifier)
        elif '/channel/' in channel_identifier:
            match = re.search(r'/channel/([^?&]+)', channel_identifier)
            if match:
                channel_id = match.group(1)
            else:
                raise HTTPException(status_code=400, detail="Invalid channel URL format")
        else:
            channel_id = channel_identifier
        
        if not channel_id:
            raise HTTPException(status_code=400, detail="Could not extract channel ID")
        
        url = "https://www.googleapis.com/youtube/v3/channels"
        
        params = {
            'part': 'snippet,statistics',
            'id': channel_id,
            'key': YOUTUBE_API_KEY
        }
        
        response = requests.get(url, params=params)
        
        if response.status_code != 200:
            raise HTTPException(status_code=response.status_code, detail=f"API request failed: {response.text}")
        
        data = response.json()
        
        if not data.get('items'):
            raise HTTPException(status_code=404, detail="Channel not found")
        
        channel = data['items'][0]
        
        metadata = ChannelMetadata(
            id=channel['id'],
            title=channel['snippet']['title'],
            description=channel['snippet']['description'],
            published_at=channel['snippet']['publishedAt'],
            thumbnail_url=channel['snippet']['thumbnails']['high']['url'],
            view_count=channel['statistics'].get('viewCount', 0),
            subscriber_count=channel['statistics'].get('subscriberCount', 0),
            video_count=channel['statistics'].get('videoCount', 0),
            channel_url=f"https://www.youtube.com/channel/{channel['id']}"
        )
        
        logger.info(f"Successfully extracted metadata for channel: {metadata.title}")
        return metadata
        
    except Exception as e:
        logger.error(f"Error extracting channel metadata for {channel_identifier}: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Failed to extract channel metadata: {str(e)}")

def get_playlist_items(playlist_id: str) -> PlaylistResponse:
    """Extract video URLs from a YouTube playlist using the Data API v3"""
    if not YOUTUBE_API_KEY:
        raise HTTPException(status_code=500, detail="YouTube Data API key not configured")
    
    try:
        url = "https://www.googleapis.com/youtube/v3/playlistItems"
        
        params = {
            'part': 'snippet',
            'playlistId': playlist_id,
            'key': YOUTUBE_API_KEY,
            'maxResults': 50
        }
        
        response = requests.get(url, params=params)
        
        if response.status_code != 200:
            error_msg = f"API request failed with status {response.status_code}: {response.text}"
            logger.error(error_msg)
            raise HTTPException(status_code=response.status_code, detail=error_msg)
        
        data = response.json()
        
        if not data:
            error_msg = "Empty response from YouTube API"
            logger.error(error_msg)
            raise HTTPException(status_code=500, detail=error_msg)
        
        video_urls = []
        try:
            for item in data.get('items', []):
                video_id = item['snippet']['resourceId']['videoId']
                video_urls.append(f"https://www.youtube.com/watch?v={video_id}")
        except KeyError as e:
            error_msg = f"Error parsing playlist data: Missing key {str(e)}"
            logger.error(error_msg)
            raise HTTPException(status_code=500, detail=error_msg)
        
        playlist_response = PlaylistResponse(
            kind="youtube#playlistItemListResponse",
            etag=data.get('etag', ''),
            items=video_urls,
            pageInfo={
                'totalResults': data.get('pageInfo', {}).get('totalResults', len(video_urls)),
                'resultsPerPage': data.get('pageInfo', {}).get('resultsPerPage', 50)
            }
        )
        
        logger.info(f"Successfully extracted {len(video_urls)} videos from playlist: {playlist_id}")
        return playlist_response
        
    except Exception as e:
        logger.error(f"Error extracting playlist items for {playlist_id}: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Failed to extract playlist items: {str(e)}")

def get_playlist_details(playlist_id: str):
    """Extract detailed playlist information using the YouTube Data API v3"""
    if not YOUTUBE_API_KEY:
        raise HTTPException(status_code=500, detail="YouTube Data API key not configured")
    
    try:
        url = "https://www.googleapis.com/youtube/v3/playlists"
        
        params = {
            'part': 'snippet,status,contentDetails,player,localizations',
            'id': playlist_id,
            'key': YOUTUBE_API_KEY
        }
        
        response = requests.get(url, params=params)
        
        if response.status_code != 200:
            raise HTTPException(status_code=response.status_code, detail=f"API request failed: {response.text}")
        
        data = response.json()
        
        if not data.get('items'):
            raise HTTPException(status_code=404, detail="Playlist not found")
        
        playlist = data['items'][0]
        
        playlist_details = {
            "kind": "youtube#playlist",
            "etag": data.get('etag', ''),
            "id": playlist['id'],
            "snippet": {
                "publishedAt": playlist['snippet'].get('publishedAt', ''),
                "channelId": playlist['snippet'].get('channelId', ''),
                "title": playlist['snippet'].get('title', ''),
                "description": playlist['snippet'].get('description', ''),
                "thumbnails": playlist['snippet'].get('thumbnails', {}),
                "channelTitle": playlist['snippet'].get('channelTitle', ''),
                "defaultLanguage": playlist['snippet'].get('defaultLanguage', ''),
                "localized": playlist['snippet'].get('localized', {})
            },
            "status": playlist['status'] if 'status' in playlist else {},
            "contentDetails": playlist['contentDetails'] if 'contentDetails' in playlist else {},
            "player": playlist['player'] if 'player' in playlist else {},
            "localizations": playlist['localizations'] if 'localizations' in playlist else {}
        }
        
        logger.info(f"Successfully extracted detailed information for playlist: {playlist_id}")
        return playlist_details
        
    except Exception as e:
        logger.error(f"Error extracting playlist details for {playlist_id}: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Failed to extract playlist details: {str(e)}")

@app.get("/youtube/channel")
async def get_channel_metadata_endpoint(url: str):
    """Get YouTube channel metadata using the YouTube Data API v3."""
    if not url:
        raise HTTPException(status_code=400, detail="Channel URL or identifier is required")
    
    try:
        channel_id = None
        
        if '/channel/' in url:
            match = re.search(r'/channel/([^?&]+)', url)
            if match:
                channel_id = match.group(1)
            else:
                raise HTTPException(status_code=400, detail="Invalid channel URL format")
        elif url.startswith('@'):
            channel_id = extract_channel_id_from_handle(url)
        elif url.startswith('UC') and len(url) == 24:
            channel_id = url
        else:
            channel_id = url
        
        if not channel_id:
            raise HTTPException(status_code=400, detail="Could not extract channel ID")
        
        api_url = "https://www.googleapis.com/youtube/v3/channels"
        
        params = {
            'part': 'snippet,statistics',
            'id': channel_id,
            'key': YOUTUBE_API_KEY
        }
        
        response = requests.get(api_url, params=params)
        
        if response.status_code != 200:
            raise HTTPException(status_code=response.status_code, detail=f"API request failed: {response.text}")
        
        data = response.json()
        
        if not data.get('items') or len(data['items']) == 0:
            raise HTTPException(status_code=404, detail="Channel not found")
        
        channel_data = data['items'][0]
        
        result = {
            "kind": "youtube#channelListResponse",
            "etag": data.get('etag', ''),
            "items": [channel_data]
        }
        
        logger.info(f"Successfully extracted channel metadata: {result['items'][0]['snippet']['title']}")
        return result
            
    except Exception as e:
        logger.error(f"Error extracting channel metadata for {url}: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Failed to extract channel metadata: {str(e)}")

@app.get("/youtube/playlist/short")
async def get_playlist_items_endpoint(url: str):
    """Get YouTube playlist items (video URLs) using the YouTube Data API v3."""
    if not url:
        raise HTTPException(status_code=400, detail="Playlist URL is required")
    
    try:
        playlist_id = extract_playlist_id(url)
        if not playlist_id:
            raise HTTPException(status_code=400, detail="Invalid playlist URL")
            
        playlist_response = get_playlist_items(playlist_id)
        return playlist_response
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/youtube/playlist/long")
async def get_playlist_details_endpoint(url: str):
    """Get detailed YouTube playlist information using the YouTube Data API v3."""
    if not url:
        raise HTTPException(status_code=400, detail="Playlist URL is required")
    
    try:
        playlist_id = extract_playlist_id(url)
        if not playlist_id:
            raise HTTPException(status_code=400, detail="Invalid playlist URL")
            
        playlist_details = get_playlist_details(playlist_id)
        return playlist_details
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/youtube/video")
async def get_video_info_from_youtube_api(url: str):
    """Get YouTube video information using the YouTube Data API v3."""
    if not url:
        raise HTTPException(status_code=400, detail="Video URL or ID is required")
    
    if not YOUTUBE_API_KEY:
        raise HTTPException(status_code=500, detail="YouTube Data API key not configured")
    
    try:
        video_id = None
        if url.startswith("http"):
            match = re.search(r'(?:v=|\/)([0-9A-Za-z_-]{11}).*', url)
            if match:
                video_id = match.group(1)
            else:
                raise HTTPException(status_code=400, detail="Invalid YouTube URL")
        else:
            if len(url) == 11 and re.match(r'^[0-9A-Za-z_-]+$', url):
                video_id = url
            else:
                raise HTTPException(status_code=400, detail="Invalid video ID format")
        
        if not video_id:
            raise HTTPException(status_code=400, detail="Could not extract video ID")
        
        api_url = "https://www.googleapis.com/youtube/v3/videos"
        
        params = {
            'part': 'snippet,statistics,contentDetails',
            'id': video_id,
            'key': YOUTUBE_API_KEY
        }
        
        response = requests.get(api_url, params=params)
        
        if response.status_code != 200:
            raise HTTPException(status_code=response.status_code, detail=f"API request failed: {response.text}")
        
        data = response.json()
        
        return data
            
    except Exception as e:
        logger.error(f"Error extracting video information for {url}: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Failed to extract video information: {str(e)}")

@app.get("/")
async def root():
    return {"message": "YouTube Data Harvester API is running"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="127.0.0.1", port=8000)