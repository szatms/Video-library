# Video-library

Full-stack YouTube content organizer built with Spring Boot and React.

A Spring Boot + React web-app to organize and monitor YouTube playlists and videos, add notes and create spaces to study or delve into new hobbies and interests via Codices.

## Requirements

- YouTube Data API key
- Docker
- Docker-compose

## Setup Instructions

### 1. Clone the repository

```bash
git clone https://github.com/szatms/Video-library
```

### 2. Create .env file

Create a `.env` file in the root directory with the following content, then populate placeholders with your own values:

```env# ==========================================
# MongoDB
# ==========================================

MONGO_USERNAME=<MONGODB_USERNAME>
MONGO_PASSWORD=<MONGODB_PASSWORD>
MONGO_DATABASE=video-library
MONGO_PORT=27017


# ==========================================
# MongoExpress
# ==========================================

MONGO_EXPRESS_USERNAME=<MONGOEXPRESS_USERNAME>
MONGO_EXPRESS_PASSWORD=<MONGOEXPRESS_PASSWORD>


# ==========================================
# Backend
# ==========================================

BACKEND_PORT=8080

JWT_SECRET=<JWT_SECRET>
JWT_EXPIRATION=3600000


# ==========================================
# Frontend
# ==========================================

FRONTEND_PORT=5173
VITE_API_URL=http://localhost:8080/api


# ==========================================
# YouTube Harvester Microservice
# ==========================================

MICROSERVICE_PORT=8000
YOUTUBE_DATA_API_KEY=<YOUTUBE_DATA_API_KEY> 
```
### 3. Run the application

```bash
docker compose up --build -d
```
