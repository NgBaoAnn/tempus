"""
LLM service using Google Gemini
Handles prompt building and response generation
"""
import google.generativeai as genai
from config import settings
import logging

logger = logging.getLogger(__name__)

# Configure Gemini
if settings.GEMINI_API_KEY:
    genai.configure(api_key=settings.GEMINI_API_KEY)
else:
    logger.warning("GEMINI_API_KEY not set!")

# Prompt templates for different modes
PROMPT_TEMPLATES = {
    "ask": """SYSTEM:
You are Tiramisu AI, a smart task management assistant.
Mode: ASK MODE (Q&A only)

CONTEXT (Retrieved from memory):
{context}

USER:
{query}

INSTRUCTIONS:
- Use provided context when relevant
- If context is insufficient, acknowledge it
- Give concise, actionable answers
- DO NOT perform any actions (create/edit tasks)
- Keep responses friendly and helpful""",

    "agent": """SYSTEM:
You are Tiramisu AI, a smart task management assistant.
Mode: AGENT MODE (can take actions)

CONTEXT (Retrieved from memory):
{context}

USER:
{query}

INSTRUCTIONS:
- Use provided context to understand user's schedule
- If user requests actions, respond with JSON proposal
- Confirm before making changes
- Be specific about what will be changed""",

    "planner": """SYSTEM:
You are Tiramisu AI, a life planning assistant.
Mode: PLANNER MODE (long-term goals)

CONTEXT (Retrieved from memory):
{context}

USER:
{query}

INSTRUCTIONS:
- Help user break down large goals into milestones
- Consider their existing schedule and patterns
- Suggest realistic timelines
- Be encouraging and supportive"""
}


class LLMService:
    """Service for LLM operations using Gemini"""
    
    def __init__(self):
        self.model = genai.GenerativeModel('gemini-1.5-flash')
    
    def generate(self, query: str, context_chunks: list[dict], 
                mode: str = "ask") -> str:
        """
        Generate response with context
        
        Args:
            query: User's query
            context_chunks: List of context chunks from Chroma
            mode: AI mode (ask/agent/planner)
            
        Returns:
            Generated response text
        """
        # Format context
        if context_chunks:
            context_text = "\n".join([
                f"- [{c['type']}] {c['content'][:300]}..."
                if len(c['content']) > 300 else f"- [{c['type']}] {c['content']}"
                for c in context_chunks
            ])
        else:
            context_text = "No relevant context found in memory."
        
        # Get template
        template = PROMPT_TEMPLATES.get(mode, PROMPT_TEMPLATES["ask"])
        
        # Build prompt
        prompt = template.format(
            context=context_text,
            query=query
        )
        
        logger.debug(f"Generating response for mode: {mode}")
        
        try:
            response = self.model.generate_content(prompt)
            return response.text
        except Exception as e:
            logger.error(f"Error generating response: {e}")
            return f"I'm sorry, I encountered an error: {str(e)}"
    
    def generate_summary(self, messages: list[str]) -> str:
        """
        Generate a summary of conversation messages
        Used for memory compression
        """
        prompt = f"""Summarize the following conversation in 2-3 sentences, 
focusing on key topics and any decisions made:

{chr(10).join(messages)}

Summary:"""
        
        try:
            response = self.model.generate_content(prompt)
            return response.text
        except Exception as e:
            logger.error(f"Error generating summary: {e}")
            return "Conversation summary unavailable."


# Singleton instance
llm_service = LLMService()
