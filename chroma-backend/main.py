"""
Chroma Vector Memory Backend - FastAPI Application
Main entry point for the API server
"""
from fastapi import FastAPI, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware
import uuid
import logging
from models.schemas import (
    ChatRequest, ChatResponse, TaskSyncRequest, SyncResponse,
    MemoryStats, StatusResponse
)
from services.chroma_service import chroma_service
from services.llm_service import llm_service
from config import settings

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# Create FastAPI app
app = FastAPI(
    title="Tempus AI Memory API",
    description="Chroma Vector Database backend for long-term AI context",
    version="1.0.0"
)

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"]
)


# ============ HEALTH CHECK ============

@app.get("/health", response_model=StatusResponse)
async def health_check():
    """Health check endpoint"""
    return StatusResponse(status="ok")


# ============ CHAT ENDPOINTS ============

@app.post("/ai/chat", response_model=ChatResponse)
async def chat(request: ChatRequest):
    """
    Main chat endpoint with vector context retrieval
    
    1. Retrieves relevant context from Chroma
    2. Builds prompt with context
    3. Generates response using LLM
    4. Stores interaction in history
    """
    logger.info(f"Chat request from user {request.user_id}: {request.query[:50]}...")
    
    try:
        # Generate session ID if not provided
        session_id = request.session_id or f"session_{uuid.uuid4().hex[:8]}"
        
        # 1. Retrieve relevant context from vector store
        context_chunks = chroma_service.retrieve_context(
            user_id=request.user_id,
            query=request.query,
            top_k=settings.TOP_K_RESULTS
        )
        logger.info(f"Retrieved {len(context_chunks)} context chunks")
        
        # 2. Generate response using LLM with context
        response = llm_service.generate(
            query=request.query,
            context_chunks=context_chunks,
            mode=request.mode
        )
        
        # 3. Store this interaction in history
        chroma_service.add_interaction(
            user_id=request.user_id,
            user_msg=request.query,
            ai_msg=response,
            mode=request.mode,
            session_id=session_id
        )
        
        return ChatResponse(
            response=response,
            context_used=[c["content"][:100] for c in context_chunks],
            session_id=session_id
        )
        
    except Exception as e:
        logger.error(f"Error in chat: {e}")
        raise HTTPException(status_code=500, detail=str(e))


# ============ MEMORY SYNC ENDPOINTS ============

@app.post("/memory/sync/tasks", response_model=SyncResponse)
async def sync_tasks(request: TaskSyncRequest):
    """
    Sync tasks to vector memory for semantic search
    Called periodically by Android app
    """
    logger.info(f"Syncing {len(request.tasks)} tasks for user {request.user_id}")
    
    try:
        tasks_dicts = [task.model_dump() for task in request.tasks]
        synced = chroma_service.upsert_tasks_batch(request.user_id, tasks_dicts)
        return SyncResponse(synced=synced)
        
    except Exception as e:
        logger.error(f"Error syncing tasks: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/memory/add", response_model=StatusResponse)
async def add_memory(
    user_id: str = Query(..., description="User ID"),
    text: str = Query(..., description="Memory text"),
    category: str = Query("general", description="Memory category")
):
    """Add a user preference or memory"""
    logger.info(f"Adding memory for user {user_id}: {text[:50]}...")
    
    try:
        chroma_service.add_memory(user_id, text, category)
        return StatusResponse(status="ok")
        
    except Exception as e:
        logger.error(f"Error adding memory: {e}")
        raise HTTPException(status_code=500, detail=str(e))


# ============ MEMORY MANAGEMENT ============

@app.delete("/memory/clear/{user_id}", response_model=StatusResponse)
async def clear_memory(user_id: str):
    """Clear all vector memory for a user"""
    logger.info(f"Clearing memory for user {user_id}")
    
    try:
        chroma_service.clear_user_data(user_id)
        return StatusResponse(status="cleared")
        
    except Exception as e:
        logger.error(f"Error clearing memory: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/memory/stats/{user_id}", response_model=MemoryStats)
async def get_stats(user_id: str):
    """Get memory statistics for a user"""
    try:
        stats = chroma_service.get_stats(user_id)
        return MemoryStats(**stats)
        
    except Exception as e:
        logger.error(f"Error getting stats: {e}")
        raise HTTPException(status_code=500, detail=str(e))


# ============ ADMIN ENDPOINTS ============

@app.post("/admin/cleanup", response_model=StatusResponse)
async def cleanup_old_data(days: int = Query(30, description="Days to keep")):
    """Cleanup old interaction history (admin only)"""
    logger.info(f"Cleaning up interactions older than {days} days")
    
    try:
        chroma_service.cleanup_old_history(days)
        return StatusResponse(status="cleaned")
        
    except Exception as e:
        logger.error(f"Error during cleanup: {e}")
        raise HTTPException(status_code=500, detail=str(e))


# ============ RUN SERVER ============

if __name__ == "__main__":
    import uvicorn
    logger.info(f"Starting server on {settings.HOST}:{settings.PORT}")
    uvicorn.run(
        app, 
        host=settings.HOST, 
        port=settings.PORT
    )
