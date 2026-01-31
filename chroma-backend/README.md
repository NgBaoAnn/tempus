# Chroma Backend

Vector memory backend for Tempus AI using Chroma + FastAPI.

## Quick Start

### 1. Setup Environment

```bash
cd chroma-backend
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate
pip install -r requirements.txt
```

### 2. Configure

```bash
cp .env.example .env
# Edit .env and add your GEMINI_API_KEY
```

### 3. Run Server

```bash
python main.py
```

Server will start at `http://localhost:8000`

## Docker

```bash
# Build and run
docker-compose up -d

# View logs
docker-compose logs -f
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/health` | Health check |
| POST | `/ai/chat` | Chat with vector context |
| POST | `/memory/sync/tasks` | Sync tasks |
| POST | `/memory/add` | Add user memory |
| DELETE | `/memory/clear/{user_id}` | Clear user data |
| GET | `/memory/stats/{user_id}` | Get statistics |

## API Docs

Interactive docs at: `http://localhost:8000/docs`
