import os
import yt_dlp
import requests
import re
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import Optional
import logging

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# YouTube Data API v3
# https://console.cloud.google.com/
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

def get_video_metadata_raw(video_url: str):
    """Extract metadata from a YouTube video using yt-dlp and return raw data"""
    try:
        # First check if we got a full URL or just a video ID
        # If it's a URL, we'll use it directly
        # If it's an ID, we need to construct a proper YouTube URL
        if not video_url.startswith("http"):
            # It's likely a video ID, construct the full URL
            video_url = f"https://www.youtube.com/watch?v={video_url}"
        
        # Use the most basic yt-dlp configuration that should work with any version
        # Avoid any options that might trigger the compatibility error
        ydl_opts = {
            'quiet': True,
            'no_warnings': True,
            'noplaylist': True,
            'format': 'best',
            'user_agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36',
            'referer': 'https://www.youtube.com/',
        }
        
        # Use a different approach - get info in a way that avoids the problematic path
        try:
            # Try the standard method first
            with yt_dlp.YoutubeDL(ydl_opts) as ydl:
                info = ydl.extract_info(video_url, download=False)
                logger.info(f"Successfully extracted metadata for video: {info.get('title', 'Unknown Title')}")
                return info
        except Exception as e:
            # If we get the specific error, try a completely different approach
            if "'UrllibResponseAdapter' object has no attribute '_http_error'" in str(e):
                logger.warning("yt-dlp compatibility issue detected, using alternative approach")
                # Try with minimal options and different configuration
                ydl_opts = {
                    'quiet': True,
                    'no_warnings': True,
                    'noplaylist': True,
                    'format': 'best',
                    'extract_flat': True,
                    'user_agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36',
                    'referer': 'https://www.youtube.com/',
                }
                try:
                    with yt_dlp.YoutubeDL(ydl_opts) as ydl:
                        info = ydl.extract_info(video_url, download=False)
                        logger.info(f"Successfully extracted metadata (fallback): {info.get('title', 'Unknown Title')}")
                        return info
                except Exception as e2:
                    # If still failing, try the most basic approach
                    logger.warning("Even fallback failed, trying absolute minimal approach")
                    ydl_opts = {
                        'quiet': True,
                        'no_warnings': True,
                        'noplaylist': True,
                        'user_agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36',
                        'referer': 'https://www.youtube.com/',
                    }
                    with yt_dlp.YoutubeDL(ydl_opts) as ydl:
                        info = ydl.extract_info(video_url, download=False)
                        logger.info(f"Successfully extracted metadata (minimal): {info.get('title', 'Unknown Title')}")
                        return info
            else:
                raise e
                
    except Exception as e:
        logger.error(f"Error extracting metadata for {video_url}: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Failed to extract metadata after all attempts: {str(e)}")

def get_video_metadata(video_url: str) -> VideoMetadata:
    """Extract metadata from a YouTube video using yt-dlp with maximum compatibility"""
    try:
        # First check if we got a full URL or just a video ID
        # If it's a URL, we'll use it directly
        # If it's an ID, we need to construct a proper YouTube URL
        if not video_url.startswith("http"):
            # It's likely a video ID, construct the full URL
            video_url = f"https://www.youtube.com/watch?v={video_url}"
        
        # Use the most basic yt-dlp configuration that should work with any version
        # Avoid any options that might trigger the compatibility error
        ydl_opts = {
            'quiet': True,
            'no_warnings': True,
            'noplaylist': True,
            'format': 'best',
            'user_agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36',
            'referer': 'https://www.youtube.com/',
        }
        
        # Use a different approach - get info in a way that avoids the problematic path
        try:
            # Try the standard method first
            with yt_dlp.YoutubeDL(ydl_opts) as ydl:
                info = ydl.extract_info(video_url, download=False)
                
                # Extract relevant metadata with fallbacks
                metadata = VideoMetadata(
                    title=info.get('title', 'Unknown Title'),
                    description=info.get('description', 'No description'),
                    duration=info.get('duration', 0),
                    upload_date=info.get('upload_date', 'Unknown'),
                    channel=info.get('uploader', 'Unknown Channel'),
                    channel_url=info.get('uploader_url', ''),
                    view_count=info.get('view_count', 0),
                    like_count=info.get('like_count', 0),
                    thumbnail_url=info.get('thumbnail', ''),
                    video_url=info.get('webpage_url', video_url)
                )
                
                logger.info(f"Successfully extracted metadata for video: {metadata.title}")
                return metadata
        except Exception as e:
            # If we get the specific error, try a completely different approach
            if "'UrllibResponseAdapter' object has no attribute '_http_error'" in str(e):
                logger.warning("yt-dlp compatibility issue detected, using alternative approach")
                # Try with minimal options and different configuration
                ydl_opts = {
                    'quiet': True,
                    'no_warnings': True,
                    'noplaylist': True,
                    'format': 'best',
                    'extract_flat': True,
                    'user_agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36',
                    'referer': 'https://www.youtube.com/',
                }
                try:
                    with yt_dlp.YoutubeDL(ydl_opts) as ydl:
                        info = ydl.extract_info(video_url, download=False)
                        
                        metadata = VideoMetadata(
                            title=info.get('title', 'Unknown Title'),
                            description=info.get('description', 'No description'),
                            duration=info.get('duration', 0),
                            upload_date=info.get('upload_date', 'Unknown'),
                            channel=info.get('uploader', 'Unknown Channel'),
                            channel_url=info.get('uploader_url', ''),
                            view_count=info.get('view_count', 0),
                            like_count=info.get('like_count', 0),
                            thumbnail_url=info.get('thumbnail', ''),
                            video_url=info.get('webpage_url', video_url)
                        )
                        
                        logger.info(f"Successfully extracted metadata (fallback): {metadata.title}")
                        return metadata
                except Exception as e2:
                    # If still failing, try the most basic approach
                    logger.warning("Even fallback failed, trying absolute minimal approach")
                    ydl_opts = {
                        'quiet': True,
                        'no_warnings': True,
                        'noplaylist': True,
                        'user_agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36',
                        'referer': 'https://www.youtube.com/',
                    }
                    with yt_dlp.YoutubeDL(ydl_opts) as ydl:
                        info = ydl.extract_info(video_url, download=False)
                        
                        metadata = VideoMetadata(
                            title=info.get('title', 'Unknown Title'),
                            description=info.get('description', 'No description'),
                            duration=info.get('duration', 0),
                            upload_date=info.get('upload_date', 'Unknown'),
                            channel=info.get('uploader', 'Unknown Channel'),
                            channel_url=info.get('uploader_url', ''),
                            view_count=info.get('view_count', 0),
                            like_count=info.get('like_count', 0),
                            thumbnail_url=info.get('thumbnail', ''),
                            video_url=info.get('webpage_url', video_url)
                        )
                        
                        logger.info(f"Successfully extracted metadata (minimal): {metadata.title}")
                        return metadata
            else:
                raise e
                
    except Exception as e:
        logger.error(f"Error extracting metadata for {video_url}: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Failed to extract metadata after all attempts: {str(e)}")

def extract_playlist_id(url: str) -> str:
    """Extract playlist ID from a YouTube playlist URL"""
    if not url.startswith("https://"):
        return url
    # else
    m = re.search(r"\?list=([^?&]+)", url)
    if m:
        result = m.group(1)
        return result
    # else
    return None

def extract_channel_id_from_handle(channel_handle: str) -> str:
    """Extract channel ID from a channel handle using YouTube Data API v3"""
    if not YOUTUBE_API_KEY:
        raise HTTPException(status_code=500, detail="YouTube Data API key not configured")
    
    try:
        # YouTube Data API v3 endpoint for searching channels by handle
        url = "https://www.googleapis.com/youtube/v3/channels"
        
        params = {
            'part': 'id',
            'forHandle': channel_handle.lstrip('@'),  # Remove @ if present
            'key': YOUTUBE_API_KEY
        }
        
        response = requests.get(url, params=params)
        
        if response.status_code != 200:
            raise HTTPException(status_code=response.status_code, detail=f"API request failed: {response.text}")
        
        data = response.json()
        
        if not data.get('items') or len(data['items']) == 0:
            raise HTTPException(status_code=404, detail="Channel handle not found")
        
        # Return the channel ID from the first result
        channel_id = data['items'][0]['id']
        return channel_id
        
    except Exception as e:
        logger.error(f"Error extracting channel ID from handle {channel_handle}: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Failed to resolve channel handle to ID: {str(e)}")

def get_channel_metadata(channel_identifier: str) -> ChannelMetadata:
    """Extract channel metadata from YouTube using the Data API v3.
    Accepts either channel ID or channel handle (with @ prefix)"""
    if not YOUTUBE_API_KEY:
        raise HTTPException(status_code=500, detail="YouTube Data API key not configured")
    
    try:
        # Check if we have a handle (starts with @) or ID
        channel_id = None
        if channel_identifier.startswith('@'):
            # Resolve handle to channel ID
            channel_id = extract_channel_id_from_handle(channel_identifier)
        elif '/channel/' in channel_identifier:
            # Extract channel ID from URL
            match = re.search(r'/channel/([^?&]+)', channel_identifier)
            if match:
                channel_id = match.group(1)
            else:
                raise HTTPException(status_code=400, detail="Invalid channel URL format")
        else:
            # Assume it's already a channel ID
            channel_id = channel_identifier
        
        if not channel_id:
            raise HTTPException(status_code=400, detail="Could not extract channel ID")
        
        # YouTube Data API v3 endpoint for getting channel information
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
        
        # Extract relevant metadata
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
        # YouTube Data API v3 endpoint for getting playlist items
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
        
        # Check if we got valid data
        if not data:
            error_msg = "Empty response from YouTube API"
            logger.error(error_msg)
            raise HTTPException(status_code=500, detail=error_msg)
        
        # Extract video URLs from playlist items
        video_urls = []
        try:
            for item in data.get('items', []):
                video_id = item['snippet']['resourceId']['videoId']
                video_urls.append(f"https://www.youtube.com/watch?v={video_id}")
        except KeyError as e:
            error_msg = f"Error parsing playlist data: Missing key {str(e)}"
            logger.error(error_msg)
            raise HTTPException(status_code=500, detail=error_msg)
        
        # Create response in the specified format
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
        # YouTube Data API v3 endpoint for getting playlist details
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
        
        # Extract detailed playlist information
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

@app.get("/")
async def root():
    return {"message": "YouTube Data Harvester API"}

@app.get("/ytdlp/video/shorter")
async def get_video_metadata_endpoint(url: str):
    """
    Get YouTube video metadata using yt-dlp
    
    Args:
        url (str): The YouTube video URL
        
    Returns:
        dict: Extracted video metadata in raw format
    """
    if not url:
        raise HTTPException(status_code=400, detail="Video URL is required")
    
    try:
        # Call the function that returns raw data from yt-dlp
        raw_data = get_video_metadata_raw(url)
        return raw_data
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/youtube/channel")
async def get_channel_metadata_endpoint(url: str):
    """
    Get YouTube channel metadata using the YouTube Data API v3 to match OneRing format
    
    Args:
        url (str): The YouTube channel URL or channel identifier
        
    Returns:
        dict: Channel info in YouTube Data API v3 format matching OneRing
    """
    if not url:
        raise HTTPException(status_code=400, detail="Channel URL or identifier is required")
    
    try:
        # First check if we got a full URL or just a channel identifier
        channel_id = None
        
        # If it's a URL with /channel/, extract the ID
        if '/channel/' in url:
            import re
            match = re.search(r'/channel/([^?&]+)', url)
            if match:
                channel_id = match.group(1)
            else:
                raise HTTPException(status_code=400, detail="Invalid channel URL format")
        elif url.startswith('@'):
            # Handle channel handle (like @channelname)
            channel_id = extract_channel_id_from_handle(url)
        elif url.startswith('UC') and len(url) == 24:
            # It's already a channel ID
            channel_id = url
        else:
            # Assume it's a channel ID
            channel_id = url
        
        if not channel_id:
            raise HTTPException(status_code=400, detail="Could not extract channel ID")
        
        # Now get the actual channel metadata from YouTube Data API v3
        # YouTube Data API v3 endpoint for getting channel information
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
        
        # Reformat to match the expected structure exactly
        # The backend expects items to be a list, not a single item
        if not data.get('items') or len(data['items']) == 0:
            raise HTTPException(status_code=404, detail="Channel not found")
        
        # Build the response exactly as OneRing would return
        channel_data = data['items'][0]
        
        # Return exactly what OneRing's endpoint returns
        result = {
            "kind": "youtube#channelListResponse",
            "etag": data.get('etag', ''),
            "items": [channel_data]  # This should match OneRing's exact format
        }
        
        logger.info(f"Successfully extracted channel metadata: {result['items'][0]['snippet']['title']}")
        return result
            
    except Exception as e:
        logger.error(f"Error extracting channel metadata for {url}: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Failed to extract channel metadata: {str(e)}")

@app.get("/youtube/playlist/short")
async def get_playlist_items_endpoint(url: str):
    """
    Get YouTube playlist items (video URLs) using the YouTube Data API v3
    
    Args:
        url (str): The YouTube playlist URL
        
    Returns:
        PlaylistResponse: Playlist items in the specified format
    """
    if not url:
        raise HTTPException(status_code=400, detail="Playlist URL is required")
    
    try:
        # Extract playlist ID from URL
        playlist_id = extract_playlist_id(url)
        if not playlist_id:
            raise HTTPException(status_code=400, detail="Invalid playlist URL")
            
        playlist_response = get_playlist_items(playlist_id)
        return playlist_response
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/youtube/playlist/long")
async def get_playlist_details_endpoint(url: str):
    """
    Get detailed YouTube playlist information using the YouTube Data API v3
    
    Args:
        url (str): The YouTube playlist URL
        
    Returns:
        dict: Detailed playlist information in youtube#playlist format
    """
    if not url:
        raise HTTPException(status_code=400, detail="Playlist URL is required")
    
    try:
        # Extract playlist ID from URL
        playlist_id = extract_playlist_id(url)
        if not playlist_id:
            raise HTTPException(status_code=400, detail="Invalid playlist URL")
            
        playlist_details = get_playlist_details(playlist_id)
        return playlist_details
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/youtube/video/combined")
async def get_combined_video_info(url: str):
    """
    Get combined YouTube video information from both YouTube API and yt-dlp
    
    Args:
        url (str): The YouTube video URL
        
    Returns:
        dict: Combined video information
    """
    if not url:
        raise HTTPException(status_code=400, detail="Video URL is required")
    
    try:
        # For now, just return the yt-dlp data
        # In a real implementation, this would combine both YouTube API and yt-dlp data
        metadata = get_video_metadata(url)
        return metadata
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    # Restrict to localhost only
    uvicorn.run(app, host="127.0.0.1", port=8000)