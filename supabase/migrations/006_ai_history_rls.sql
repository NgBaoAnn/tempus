-- Migration: Enable RLS for ai_history table
-- This allows users to save and read their own AI chat history

-- Enable RLS on ai_history table
ALTER TABLE public.ai_history ENABLE ROW LEVEL SECURITY;

-- Policy: Users can view their own AI history
CREATE POLICY "Users can view own ai_history"
ON public.ai_history
FOR SELECT
USING (auth.uid() = user_id);

-- Policy: Users can insert their own AI history
CREATE POLICY "Users can insert own ai_history"
ON public.ai_history
FOR INSERT
WITH CHECK (auth.uid() = user_id);

-- Policy: Users can delete their own AI history
CREATE POLICY "Users can delete own ai_history"
ON public.ai_history
FOR DELETE
USING (auth.uid() = user_id);

-- Policy: Users can update their own AI history (optional, for future use)
CREATE POLICY "Users can update own ai_history"
ON public.ai_history
FOR UPDATE
USING (auth.uid() = user_id)
WITH CHECK (auth.uid() = user_id);
