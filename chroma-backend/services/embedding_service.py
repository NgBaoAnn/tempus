"""
Embedding service using sentence-transformers
Supports Vietnamese + English with multilingual model
"""
from sentence_transformers import SentenceTransformer
from config import settings
import logging

logger = logging.getLogger(__name__)


class EmbeddingService:
    """Service for generating text embeddings"""
    
    def __init__(self):
        logger.info(f"Loading embedding model: {settings.EMBEDDING_MODEL}")
        self.model = SentenceTransformer(settings.EMBEDDING_MODEL)
        logger.info("Embedding model loaded successfully")
    
    def encode(self, text: str) -> list[float]:
        """
        Generate embedding for a single text
        
        Args:
            text: Text to embed
            
        Returns:
            List of floats representing the embedding vector
        """
        return self.model.encode(text).tolist()
    
    def encode_batch(self, texts: list[str]) -> list[list[float]]:
        """
        Generate embeddings for multiple texts
        
        Args:
            texts: List of texts to embed
            
        Returns:
            List of embedding vectors
        """
        return self.model.encode(texts).tolist()
    
    def get_embedding_dimension(self) -> int:
        """Get the dimension of embedding vectors"""
        return self.model.get_sentence_embedding_dimension()


# Singleton instance
embedding_service = EmbeddingService()
