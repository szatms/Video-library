import yt_dlp
from fastapi import HTTPException
import logging

logger = logging.getLogger(__name__)

def get_video_metadata(video_url: str):
    """Extract metadata from a YouTube video using yt-dlp"""
    try:
        ydl_opts = {
            'format': 'best',
            'noplaylist': True,
            'quiet': True,
            'no_warnings': True,
        }
        
        with yt_dlp.YoutubeDL(ydl_opts) as ydl:
            info = ydl.extract_info(video_url, download=False)
            
            # Extract relevant metadata
            metadata = {
                'title': info.get('title', ''),
                'description': info.get('description', ''),
                'duration': info.get('duration', 0),
                'upload_date': info.get('upload_date', ''),
                'channel': info.get('uploader', ''),
                'channel_url': info.get('uploader_url', ''),
                'view_count': info.get('view_count', 0),
                'like_count': info.get('like_count', 0),
                'thumbnail_url': info.get('thumbnail', ''),
                'video_url': info.get('webpage_url', '')
            }
            
            logger.info(f"Successfully extracted metadata for video: {metadata['title']}")
            return metadata
            
    except Exception as e:
        logger.error(f"Error extracting metadata for {video_url}: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Failed to extract metadata: {str(e)}")