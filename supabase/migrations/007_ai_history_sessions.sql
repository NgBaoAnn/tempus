-- Migration: Add session support to ai_history table
-- This allows grouping messages into chat sessions with AI-generated titles

-- Add session_id column to group messages in the same conversation
ALTER TABLE public.ai_history 
ADD COLUMN IF NOT EXISTS session_id uuid DEFAULT gen_random_uuid();

-- Add title column for AI-generated conversation titles
ALTER TABLE public.ai_history 
ADD COLUMN IF NOT EXISTS title text;

-- Create index for faster session queries
CREATE INDEX IF NOT EXISTS idx_ai_history_session_id ON public.ai_history(session_id);
CREATE INDEX IF NOT EXISTS idx_ai_history_user_session ON public.ai_history(user_id, session_id);

-- Comment for documentation
COMMENT ON COLUMN public.ai_history.session_id IS 'Groups messages belonging to the same chat conversation';
COMMENT ON COLUMN public.ai_history.title IS 'AI-generated title for the chat session';
