# Video-library

Full-stack YouTube content organizer built with Spring Boot and React.

A Spring Boot + React web-app to organize and monitor YouTube playlists and videos, add notes and create spaces to study or delve into new hobbies and interests via Codices.

## Requirements

- YouTube Data API key
- Docker
- Docker-compose

No separate MongoDB, Java, Node.js, Python, or Maven installation is required.

For help with Docker, refer to the manuals at: https://docs.docker.com/engine/install/

## Setup Instructions

### 1. Clone the repository

```bash
git clone https://github.com/szatms/Video-library
```

### 2. Create .env file

```bash
cp .env.example .env
```
### 3. Configure .env

Replace <MACHINE_IP> with the IP address of the machine running the application. This is the address other devices on your network will use to reach the application.

A brief summary of the variables:

| Configuration          | User needs to provide? | What it is                                |
| ---------------------- | ---------------------- | ----------------------------------------- |
| MongoDB username       | Yes                    | MongoDB admin username                    |
| MongoDB password       | Yes                    | MongoDB password                          |
| Mongo Express username | Yes                    | Mongo Express login                       |
| Mongo Express password | Yes                    | Mongo Express login password              |
| JWT secret             | Yes                    | Secret used to sign authentication tokens |
| YouTube Data API key   | Yes                    | Google/YouTube API key                    |
| Machine IP             | Yes                    | IP address of the Docker host             |
| Ports                  | Usually no             | Change only if they conflict              |

Generate a JWT secret:

```bash
openssl rand -base64 32
```

Then copy the generated value into JWT_SECRET.

You need a YouTube Data API v3 key from Google Cloud. Enable the YouTube Data API v3 for your project, create an API key, and paste it into `YOUTUBE_DATA_API_KEY`.

You can get started on https://console.cloud.google.com/

Example .env:

```
MONGO_USERNAME=admin
MONGO_PASSWORD=your-secure-password

MONGO_EXPRESS_USERNAME=admin
MONGO_EXPRESS_PASSWORD=your-secure-password

JWT_SECRET=your-secret

FRONTEND_URL=http://192.168.1.50:5173
VITE_API_URL=http://192.168.1.50:8080/api

YOUTUBE_DATA_API_KEY=your-api-key
```

### 4. Run the application

```bash
docker compose up --build -d
```

Then confirm by running `docker compose ps`

### 5. Open the application
Once the containers are running, open:

http://<MACHINE_IP>:5173

For example:

http://192.168.1.50:5173

## Updating the app

When a new version is released, you can update your instance by navigating to your install directory and running:

```bash
docker compose down
git pull
docker compose up --build -d
```
