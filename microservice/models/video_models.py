from pydantic import BaseModel
from typing import Optional

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