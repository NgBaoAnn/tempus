"""
Pydantic models for API requests and responses
"""
from pydantic import BaseModel
from typing import List, Optional
from datetime import datetime


class ChatRequest(BaseModel):
    """Request for AI chat with vector context"""
    user_id: str
    query: str
    mode: str = "ask"  # ask | agent | planner
    session_id: Optional[str] = None


class ChatResponse(BaseModel):
    """Response from AI chat"""
    response: str
    context_used: List[str]
    session_id: str


class TaskDto(BaseModel):
    """Task data for vector storage"""
    id: str
    title: str
    description: Optional[str] = None
    status: str = "active"
    deadline: Optional[str] = None
    priority: str = "medium"
    labels: List[str] = []


class TaskSyncRequest(BaseModel):
    """Request to sync tasks to vector memory"""
    user_id: str
    tasks: List[TaskDto]


class SyncResponse(BaseModel):
    """Response from sync operation"""
    synced: int


class MemoryStats(BaseModel):
    """Statistics about vector memory"""
    tasks: int
    memories: int
    interactions: int
    user_tasks: int
    user_memories: int
    user_interactions: int


class StatusResponse(BaseModel):
    """Generic status response"""
    status: str


class ContextChunk(BaseModel):
    """A chunk of context retrieved from vector store"""
    content: str
    type: str
    score: float
    metadata: dict
