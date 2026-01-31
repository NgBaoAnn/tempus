"""
Chroma vector database service
Handles all CRUD operations for vector storage
"""
import chromadb
from chromadb.config import Settings as ChromaSettings
from datetime import datetime, timedelta
import uuid
import logging
from config import settings
from services.embedding_service import embedding_service

logger = logging.getLogger(__name__)


class ChromaService:
    """Service for Chroma vector database operations"""
    
    def __init__(self):
        logger.info(f"Initializing Chroma with persist dir: {settings.CHROMA_PERSIST_DIR}")
        self.client = chromadb.PersistentClient(
            path=settings.CHROMA_PERSIST_DIR,
            settings=ChromaSettings(anonymized_telemetry=False)
        )
        self._init_collections()
        logger.info("Chroma initialized successfully")
    
    def _init_collections(self):
        """Initialize all collections"""
        self.task_collection = self.client.get_or_create_collection(
            name="task_context",
            metadata={"description": "Task information for semantic search"}
        )
        self.memory_collection = self.client.get_or_create_collection(
            name="user_memory",
            metadata={"description": "User preferences and patterns"}
        )
        self.history_collection = self.client.get_or_create_collection(
            name="interaction_history",
            metadata={"description": "AI conversation history"}
        )
        logger.info("Collections initialized: task_context, user_memory, interaction_history")
    
    # ============ INSERT OPERATIONS ============
    
    def upsert_task(self, user_id: str, task: dict):
        """Insert or update a task"""
        task_id = task.get("id")
        document = self._format_task_document(task)
        embedding = embedding_service.encode(document)
        
        self.task_collection.upsert(
            ids=[f"task_{task_id}"],
            documents=[document],
            embeddings=[embedding],
            metadatas=[{
                "user_id": user_id,
                "type": "task",
                "task_id": task_id,
                "status": task.get("status", "pending"),
                "priority": task.get("priority", "medium"),
                "updated_at": datetime.utcnow().isoformat()
            }]
        )
        logger.debug(f"Upserted task: {task_id}")
    
    def upsert_tasks_batch(self, user_id: str, tasks: list[dict]) -> int:
        """Batch upsert multiple tasks"""
        if not tasks:
            return 0
            
        ids = []
        documents = []
        embeddings = []
        metadatas = []
        
        for task in tasks:
            task_id = task.get("id")
            document = self._format_task_document(task)
            
            ids.append(f"task_{task_id}")
            documents.append(document)
            metadatas.append({
                "user_id": user_id,
                "type": "task",
                "task_id": task_id,
                "status": task.get("status", "pending"),
                "priority": task.get("priority", "medium"),
                "updated_at": datetime.utcnow().isoformat()
            })
        
        # Batch encode all documents
        embeddings = embedding_service.encode_batch(documents)
        
        self.task_collection.upsert(
            ids=ids,
            documents=documents,
            embeddings=embeddings,
            metadatas=metadatas
        )
        
        logger.info(f"Batch upserted {len(tasks)} tasks for user {user_id}")
        return len(tasks)
    
    def add_memory(self, user_id: str, memory_text: str, category: str = "general"):
        """Add user memory/preference"""
        memory_id = str(uuid.uuid4())
        embedding = embedding_service.encode(memory_text)
        
        self.memory_collection.add(
            ids=[f"memory_{memory_id}"],
            documents=[memory_text],
            embeddings=[embedding],
            metadatas=[{
                "user_id": user_id,
                "type": "preference",
                "category": category,
                "timestamp": datetime.utcnow().isoformat()
            }]
        )
        logger.debug(f"Added memory for user {user_id}: {memory_text[:50]}...")
    
    def add_interaction(self, user_id: str, user_msg: str, ai_msg: str, 
                       mode: str, session_id: str):
        """Store conversation turn"""
        interaction_id = str(uuid.uuid4())
        document = f"User: {user_msg}\nAI: {ai_msg}"
        embedding = embedding_service.encode(document)
        
        self.history_collection.add(
            ids=[f"interaction_{interaction_id}"],
            documents=[document],
            embeddings=[embedding],
            metadatas=[{
                "user_id": user_id,
                "type": "interaction",
                "mode": mode,
                "session_id": session_id,
                "timestamp": datetime.utcnow().isoformat()
            }]
        )
        logger.debug(f"Added interaction for session {session_id}")
    
    # ============ QUERY OPERATIONS ============
    
    def retrieve_context(self, user_id: str, query: str, 
                        top_k: int = 5) -> list[dict]:
        """Retrieve relevant context for a query"""
        query_embedding = embedding_service.encode(query)
        results = []
        
        # Query tasks
        try:
            task_results = self.task_collection.query(
                query_embeddings=[query_embedding],
                n_results=top_k,
                where={"user_id": user_id}
            )
            results.extend(self._format_results(task_results, "task"))
        except Exception as e:
            logger.warning(f"Error querying tasks: {e}")
        
        # Query memories
        try:
            memory_results = self.memory_collection.query(
                query_embeddings=[query_embedding],
                n_results=3,
                where={"user_id": user_id}
            )
            results.extend(self._format_results(memory_results, "memory"))
        except Exception as e:
            logger.warning(f"Error querying memories: {e}")
        
        # Query history
        try:
            history_results = self.history_collection.query(
                query_embeddings=[query_embedding],
                n_results=3,
                where={"user_id": user_id}
            )
            results.extend(self._format_results(history_results, "history"))
        except Exception as e:
            logger.warning(f"Error querying history: {e}")
        
        # Sort by relevance (distance) and take top N
        results.sort(key=lambda x: x["score"])
        return results[:settings.MAX_CONTEXT_CHUNKS]
    
    # ============ DELETE OPERATIONS ============
    
    def delete_task(self, user_id: str, task_id: str):
        """Delete a task from collection"""
        try:
            self.task_collection.delete(
                ids=[f"task_{task_id}"],
                where={"user_id": user_id}
            )
            logger.debug(f"Deleted task: {task_id}")
        except Exception as e:
            logger.warning(f"Error deleting task {task_id}: {e}")
    
    def clear_user_data(self, user_id: str):
        """Clear all data for a user"""
        for collection in [self.task_collection, self.memory_collection, 
                          self.history_collection]:
            try:
                results = collection.get(where={"user_id": user_id})
                if results["ids"]:
                    collection.delete(ids=results["ids"])
            except Exception as e:
                logger.warning(f"Error clearing collection: {e}")
        logger.info(f"Cleared all data for user {user_id}")
    
    def cleanup_old_history(self, days: int = 30):
        """Remove interactions older than N days"""
        cutoff = (datetime.utcnow() - timedelta(days=days)).isoformat()
        
        try:
            results = self.history_collection.get(
                where={"timestamp": {"$lt": cutoff}}
            )
            if results["ids"]:
                self.history_collection.delete(ids=results["ids"])
                logger.info(f"Cleaned up {len(results['ids'])} old interactions")
        except Exception as e:
            logger.warning(f"Error during cleanup: {e}")
    
    # ============ HELPER METHODS ============
    
    def _format_task_document(self, task: dict) -> str:
        """Format task as searchable document"""
        labels = ', '.join(task.get('labels', [])) if task.get('labels') else 'None'
        return f"""Task: {task.get('title', 'Untitled')}
Description: {task.get('description', 'No description')}
Status: {task.get('status', 'pending')}
Deadline: {task.get('deadline', 'No deadline')}
Priority: {task.get('priority', 'medium')}
Labels: {labels}"""
    
    def _format_results(self, results: dict, source: str) -> list[dict]:
        """Format Chroma results to unified format"""
        formatted = []
        if results.get("documents") and results["documents"][0]:
            for i, doc in enumerate(results["documents"][0]):
                distance = results["distances"][0][i] if results.get("distances") else 0
                metadata = results["metadatas"][0][i] if results.get("metadatas") else {}
                formatted.append({
                    "content": doc,
                    "type": source,
                    "score": distance,
                    "metadata": metadata
                })
        return formatted
    
    def get_stats(self, user_id: str) -> dict:
        """Get memory statistics for user"""
        try:
            user_tasks = self.task_collection.get(where={"user_id": user_id})
            user_memories = self.memory_collection.get(where={"user_id": user_id})
            user_interactions = self.history_collection.get(where={"user_id": user_id})
            
            return {
                "tasks": self.task_collection.count(),
                "memories": self.memory_collection.count(),
                "interactions": self.history_collection.count(),
                "user_tasks": len(user_tasks["ids"]) if user_tasks.get("ids") else 0,
                "user_memories": len(user_memories["ids"]) if user_memories.get("ids") else 0,
                "user_interactions": len(user_interactions["ids"]) if user_interactions.get("ids") else 0
            }
        except Exception as e:
            logger.error(f"Error getting stats: {e}")
            return {
                "tasks": 0,
                "memories": 0,
                "interactions": 0,
                "user_tasks": 0,
                "user_memories": 0,
                "user_interactions": 0
            }


# Singleton instance
chroma_service = ChromaService()
