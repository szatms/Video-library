# YouTube Data Harvester API

## Summary

This is a lightweight microservice designed to quickly extract metadata from YouTube videos, channels, and playlists. The service combines yt-dlp for video metadata extraction with the YouTube Data API v3 for channel and playlist information, providing a fast and efficient way to gather YouTube data programmatically.

## API Endpoints

### 1. Video Metadata Extraction
- **Endpoint**: `GET /ytdlp/video/shorter`
- **Purpose**: Extracts metadata from YouTube videos using yt-dlp
- **Parameters**: 
  - `url` (query parameter): The YouTube video URL
- **Response**: Raw video metadata including title, description, duration, etc.
- **Test Command**:
```bash
curl -X 'GET' \
  'http://127.0.0.1:8000/ytdlp/video/shorter?url=https://www.youtube.com/watch?v=dQw4w9WgXcQ' \
  -H 'accept: application/json'
```

### 2. Channel Metadata Extraction
- **Endpoint**: `GET /youtube/channel`
- **Purpose**: Extracts metadata from YouTube channels using YouTube Data API v3
- **Parameters**: 
  - `url` (query parameter): YouTube channel URL or identifier
- **Response**: Channel metadata including title, description, subscriber count, etc.
- **Test Command**:
```bash
curl -X 'GET' \
  'http://127.0.0.1:8000/youtube/channel?url=https://www.youtube.com/channel/UC-lHJZR3Gqxm24_Vd_AJ5Yw' \
  -H 'accept: application/json'
```

### 3. Playlist Items Extraction (Short Format)
- **Endpoint**: `GET /youtube/playlist/short`
- **Purpose**: Extracts video URLs from YouTube playlists using YouTube Data API v3
- **Parameters**: 
  - `url` (query parameter): YouTube playlist URL
- **Response**: Video URLs from the playlist in a compact format
- **Test Command**:
```bash
curl -X 'GET' \
  'http://127.0.0.1:8000/youtube/playlist/short?url=https://www.youtube.com/playlist?list=PL9rnuGMB9kcJYtZ_TBCGMURdvEaz8V0ae' \
  -H 'accept: application/json'
```

### 4. Playlist Details Extraction
- **Endpoint**: `GET /youtube/playlist/long`
- **Purpose**: Extracts detailed information about a YouTube playlist
- **Parameters**: 
  - `url` (query parameter): YouTube playlist URL
- **Response**: Detailed playlist information in youtube#playlist format
- **Test Command**:
```bash
curl -X 'GET' \
  'http://127.0.0.1:8000/youtube/playlist/long?url=https://www.youtube.com/playlist?list=PL9rnuGMB9kcJYtZ_TBCGMURdvEaz8V0ae' \
  -H 'accept: application/json'
```

### 5. Combined Video Information
- **Endpoint**: `GET /youtube/video/combined`
- **Purpose**: Combines video information from both YouTube API and yt-dlp
- **Parameters**: 
  - `url` (query parameter): YouTube video URL
- **Response**: Combined video information
- **Test Command**:
```bash
curl -X 'GET' \
  'http://127.0.0.1:8000/youtube/video/combined?url=https://www.youtube.com/watch?v=dQw4w9WgXcQ' \
  -H 'accept: application/json'
```

### 6. Health Check
- **Endpoint**: `GET /`
- **Purpose**: Basic health check endpoint
- **Response**: Simple welcome message
- **Test Command**:
```bash
curl -X 'GET' \
  'http://127.0.0.1:8000/' \
  -H 'accept: application/json'
```

## Setup

1. Ensure you have a YouTube Data API v3 key
2. Set the environment variable: `YOUTUBE_DATA_API_KEY=your_api_key_here`
3. Run the application: `./start.sh`
4. The service will only accept connections from localhost

## Configuration

The service reads the YouTube Data API v3 key from the environment variable `YOUTUBE_DATA_API_KEY` as defined in `config.py`.

## Security

The service is configured to only accept connections from localhost (127.0.0.1) for security reasons.