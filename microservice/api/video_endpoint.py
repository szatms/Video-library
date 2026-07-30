from fastapi import APIRouter, HTTPException
from ..models.video_models import VideoRequest, VideoMetadata
from ..services.ytdlp_service import get_video_metadata

router = APIRouter()

@router.post("/ytdlp/video/shorter")
async def get_video_metadata_endpoint(request: VideoRequest):
    """
    Get YouTube video metadata using yt-dlp
    
    Args:
        request (VideoRequest): Contains the YouTube video URL
        
    Returns:
        VideoMetadata: Extracted video metadata
    """
    if not request.video_url:
        raise HTTPException(status_code=400, detail="Video URL is required")
    
    try:
        metadata = get_video_metadata(request.video_url)
        return VideoMetadata(**metadata)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))