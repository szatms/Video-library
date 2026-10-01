import os
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

class videoRequest(BaseModel):
    videoUrl: str

class videoMetadata(BaseModel):
    title: str
    description: str
    duration: int
    uploadDate: str
    channel: str
    channelUrl: str
    viewCount: int
    likeCount: int
    thumbnailUrl: str
    videoUrl: str

class channelRequest(BaseModel):
    channelId: Optional[str] = None
    channelName: Optional[str] = None
    channelUrl: Optional[str] = None

class channelMetadata(BaseModel):
    id: str
    title: str
    description: str
    publishedAt: str
    thumbnailUrl: str
    viewCount: int
    subscriberCount: int
    videoCount: int
    channelUrl: str

class playlistItem(BaseModel):
    videoUrl: str

class playlistResponse(BaseModel):
    kind: str
    etag: str
    items: list[str]
    pageInfo: dict

def extractPlaylistId(url: str) -> str:
    """Extract playlist ID from a YouTube playlist URL"""
    if not url.startswith("https://"):
        return url
    m = re.search(r"\?list=([^?&]+)", url)
    if m:
        result = m.group(1)
        return result
    return None

def extractChannelIdFromHandle(channelHandle: str) -> str:
    """Extract channel ID from a channel handle using YouTube Data API v3"""
    if not YOUTUBE_API_KEY:
        raise HTTPException(status_code=500, detail="YouTube Data API key not configured")
    
    try:
        url = "https://www.googleapis.com/youtube/v3/channels"
        
        params = {
            'part': 'id',
            'forHandle': channelHandle.lstrip('@'),
            'key': YOUTUBE_API_KEY
        }
        
        response = requests.get(url, params=params)
        
        if response.status_code != 200:
            raise HTTPException(status_code=response.status_code, detail=f"API request failed: {response.text}")
        
        data = response.json()
        
        if not data.get('items') or len(data['items']) == 0:
            raise HTTPException(status_code=404, detail="Channel handle not found")
        
        channelId = data['items'][0]['id']
        return channelId
        
    except Exception as e:
        logger.error(f"Error extracting channel ID from handle {channelHandle}: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Failed to resolve channel handle to ID: {str(e)}")

def getChannelMetadata(channelIdentifier: str) -> channelMetadata:
    """Extract channel metadata from YouTube using the Data API v3."""
    if not YOUTUBE_API_KEY:
        raise HTTPException(status_code=500, detail="YouTube Data API key not configured")
    
    try:
        channelId = None
        if channelIdentifier.startswith('@'):
            channelId = extractChannelIdFromHandle(channelIdentifier)
        elif '/channel/' in channelIdentifier:
            match = re.search(r'/channel/([^?&]+)', channelIdentifier)
            if match:
                channelId = match.group(1)
            else:
                raise HTTPException(status_code=400, detail="Invalid channel URL format")
        else:
            channelId = channelIdentifier
        
        if not channelId:
            raise HTTPException(status_code=400, detail="Could not extract channel ID")
        
        url = "https://www.googleapis.com/youtube/v3/channels"
        
        params = {
            'part': 'snippet,statistics',
            'id': channelId,
            'key': YOUTUBE_API_KEY
        }
        
        response = requests.get(url, params=params)
        
        if response.status_code != 200:
            raise HTTPException(status_code=response.status_code, detail=f"API request failed: {response.text}")
        
        data = response.json()
        
        if not data.get('items'):
            raise HTTPException(status_code=404, detail="Channel not found")
        
        channel = data['items'][0]
        
        metadata = channelMetadata(
            id=channel['id'],
            title=channel['snippet']['title'],
            description=channel['snippet']['description'],
            publishedAt=channel['snippet']['publishedAt'],
            thumbnailUrl=channel['snippet']['thumbnails']['high']['url'],
            viewCount=channel['statistics'].get('viewCount', 0),
            subscriberCount=channel['statistics'].get('subscriberCount', 0),
            videoCount=channel['statistics'].get('videoCount', 0),
            channelUrl=f"https://www.youtube.com/channel/{channel['id']}"
        )
        
        logger.info(f"Successfully extracted metadata for channel: {metadata.title}")
        return metadata
        
    except Exception as e:
        logger.error(f"Error extracting channel metadata for {channelIdentifier}: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Failed to extract channel metadata: {str(e)}")

def getPlaylistItems(playlistId: str):
    """Extract video URLs from a YouTube playlist using the Data API v3"""
    if not YOUTUBE_API_KEY:
        raise HTTPException(status_code=500, detail="YouTube Data API key not configured")
    
    url = "https://www.googleapis.com/youtube/v3/playlistItems"
    
    params = {
        'part': 'snippet',
        'playlistId': playlistId,
        'key': YOUTUBE_API_KEY,
        'maxResults': 50
    }
    
    try:
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
        
        videoUrls = []
        try:
            for item in data.get('items', []):
                videoId = item['snippet']['resourceId']['videoId']
                videoUrls.append(f"https://www.youtube.com/watch?v={videoId}")
        except KeyError as e:
            error_msg = f"Error parsing playlist data: Missing key {str(e)}"
            logger.error(error_msg)
            raise HTTPException(status_code=500, detail=error_msg)
        
        # Create playlistResponse with default values to avoid undefined variable issues
        playlist_response = playlistResponse(
            kind="youtube#playlistItemListResponse",
            etag=data.get('etag', ''),
            items=videoUrls,
            pageInfo={
                'totalResults': data.get('pageInfo', {}).get('totalResults', len(videoUrls)),
                'resultsPerPage': data.get('pageInfo', {}).get('resultsPerPage', 50)
            }
        )
        
        logger.info(f"Successfully extracted {len(videoUrls)} videos from playlist: {playlistId}")
        return playlist_response
        
    except Exception as e:
        logger.error(f"Error extracting playlist items for {playlistId}: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Failed to extract playlist items: {str(e)}")

def getPlaylistDetails(playlistId: str):
    """Extract detailed playlist information using the YouTube Data API v3"""
    if not YOUTUBE_API_KEY:
        raise HTTPException(status_code=500, detail="YouTube Data API key not configured")
    
    try:
        url = "https://www.googleapis.com/youtube/v3/playlists"
        
        params = {
            'part': 'snippet,status,contentDetails,player,localizations',
            'id': playlistId,
            'key': YOUTUBE_API_KEY
        }
        
        response = requests.get(url, params=params)
        
        if response.status_code != 200:
            raise HTTPException(status_code=response.status_code, detail=f"API request failed: {response.text}")
        
        data = response.json()
        
        if not data.get('items'):
            raise HTTPException(status_code=404, detail="Playlist not found")
        
        playlist = data['items'][0]
        
        playlistDetails = {
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
        
        logger.info(f"Successfully extracted detailed information for playlist: {playlistId}")
        return playlistDetails
        
    except Exception as e:
        logger.error(f"Error extracting playlist details for {playlistId}: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Failed to extract playlist details: {str(e)}")

@app.get("/youtube/channel")
async def getChannelMetadataEndpoint(url: str):
    """Get YouTube channel metadata using the YouTube Data API v3."""
    if not url:
        raise HTTPException(status_code=400, detail="Channel URL or identifier is required")
    
    try:
        channelId = None
        
        if '/channel/' in url:
            match = re.search(r'/channel/([^?&]+)', url)
            if match:
                channelId = match.group(1)
            else:
                raise HTTPException(status_code=400, detail="Invalid channel URL format")
        elif url.startswith('@'):
            channelId = extractChannelIdFromHandle(url)
        elif url.startswith('UC') and len(url) == 24:
            channelId = url
        else:
            channelId = url
        
        if not channelId:
            raise HTTPException(status_code=400, detail="Could not extract channel ID")
        
        apiUrl = "https://www.googleapis.com/youtube/v3/channels"
        
        params = {
            'part': 'snippet,statistics',
            'id': channelId,
            'key': YOUTUBE_API_KEY
        }
        
        response = requests.get(apiUrl, params=params)
        
        if response.status_code != 200:
            raise HTTPException(status_code=response.status_code, detail=f"API request failed: {response.text}")
        
        data = response.json()
        
        if not data.get('items') or len(data['items']) == 0:
            raise HTTPException(status_code=404, detail="Channel not found")
        
        channelData = data['items'][0]
        
        result = {
            "kind": "youtube#channelListResponse",
            "etag": data.get('etag', ''),
            "items": [channelData]
        }
        
        logger.info(f"Successfully extracted channel metadata: {result['items'][0]['snippet']['title']}")
        return result
            
    except Exception as e:
        logger.error(f"Error extracting channel metadata for {url}: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Failed to extract channel metadata: {str(e)}")

@app.get("/youtube/playlist/short")
async def getPlaylistItemsEndpoint(url: str):
    """Get YouTube playlist items (video URLs) using the YouTube Data API v3."""
    if not url:
        raise HTTPException(status_code=400, detail="Playlist URL is required")
    
    try:
        playlistId = extractPlaylistId(url)
        if not playlistId:
            raise HTTPException(status_code=400, detail="Invalid playlist URL")
            
        playlist_response = getPlaylistItems(playlistId)
        return playlist_response
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/youtube/playlist/long")
async def getPlaylistDetailsEndpoint(url: str):
    """Get detailed YouTube playlist information using the YouTube Data API v3."""
    if not url:
        raise HTTPException(status_code=400, detail="Playlist URL is required")
    
    try:
        playlistId = extractPlaylistId(url)
        if not playlistId:
            raise HTTPException(status_code=400, detail="Invalid playlist URL")
            
        playlistDetails = getPlaylistDetails(playlistId)
        return playlistDetails
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/youtube/video")
async def getVideoInfoFromYoutubeApi(url: str):
    """Get YouTube video information using the YouTube Data API v3."""
    if not url:
        raise HTTPException(status_code=400, detail="Video URL or ID is required")
    
    if not YOUTUBE_API_KEY:
        raise HTTPException(status_code=500, detail="YouTube Data API key not configured")
    
    try:
        videoId = None
        if url.startswith("http"):
            match = re.search(r'(?:v=|\/)([0-9A-Za-z_-]{11}).*', url)
            if match:
                videoId = match.group(1)
            else:
                raise HTTPException(status_code=400, detail="Invalid YouTube URL")
        else:
            if len(url) == 11 and re.match(r'^[0-9A-Za-z_-]+$', url):
                videoId = url
            else:
                raise HTTPException(status_code=400, detail="Invalid video ID format")
        
        if not videoId:
            raise HTTPException(status_code=400, detail="Could not extract video ID")
        
        apiUrl = "https://www.googleapis.com/youtube/v3/videos"
        
        params = {
            'part': 'snippet,statistics,contentDetails',
            'id': videoId,
            'key': YOUTUBE_API_KEY
        }
        
        response = requests.get(apiUrl, params=params)
        
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