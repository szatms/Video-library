import os
from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    youtube_api_key: str = os.environ.get("YOUTUBE_DATA_API_KEY", "")
    port: int = 8000

settings = Settings()