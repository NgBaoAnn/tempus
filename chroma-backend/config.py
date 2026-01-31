"""
Configuration for Chroma Backend
"""
import os
from dotenv import load_dotenv

load_dotenv()


class Settings:
    # Chroma settings
    CHROMA_PERSIST_DIR = os.getenv("CHROMA_PERSIST_DIR", "./chroma_data")
    
    # Embedding model - multilingual support for Vietnamese + English
    EMBEDDING_MODEL = "sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2"
    
    # Gemini API
    GEMINI_API_KEY = os.getenv("GEMINI_API_KEY", "")
    
    # Retrieval settings
    TOP_K_RESULTS = 5
    MAX_CONTEXT_CHUNKS = 10
    
    # Server settings
    HOST = os.getenv("HOST", "0.0.0.0")
    PORT = int(os.getenv("PORT", "8000"))


settings = Settings()
