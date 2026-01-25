-- ============================================
-- TEMPUS DATABASE MIGRATION
-- Phase: RLS Policies for Messages Feature
-- Date: 2026-01-25
-- ============================================

-- ============================================
-- 1. DROP EXISTING POLICIES (if any)
-- ============================================
DROP POLICY IF EXISTS "Users can view their conversations" ON public.conversations;
DROP POLICY IF EXISTS "Users can create conversations" ON public.conversations;
DROP POLICY IF EXISTS "Users can update their conversations" ON public.conversations;

DROP POLICY IF EXISTS "Users can view messages in their conversations" ON public.messages;
DROP POLICY IF EXISTS "Users can send messages" ON public.messages;
DROP POLICY IF EXISTS "Users can update messages" ON public.messages;

-- ============================================
-- 2. ENABLE RLS ON TABLES
-- ============================================
ALTER TABLE public.conversations ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.messages ENABLE ROW LEVEL SECURITY;

-- ============================================
-- 3. CONVERSATIONS POLICIES
-- ============================================

-- Users can view their conversations
CREATE POLICY "Users can view their conversations"
ON public.conversations FOR SELECT
TO authenticated
USING (auth.uid() = participant1_id OR auth.uid() = participant2_id);

-- Users can create conversations
CREATE POLICY "Users can create conversations"
ON public.conversations FOR INSERT
TO authenticated
WITH CHECK (auth.uid() = participant1_id OR auth.uid() = participant2_id);

-- Users can update their conversations
CREATE POLICY "Users can update their conversations"
ON public.conversations FOR UPDATE
TO authenticated
USING (auth.uid() = participant1_id OR auth.uid() = participant2_id);

-- ============================================
-- 3. MESSAGES POLICIES
-- ============================================

-- Users can view messages in their conversations
CREATE POLICY "Users can view messages in their conversations"
ON public.messages FOR SELECT
TO authenticated
USING (
    EXISTS (
        SELECT 1 FROM public.conversations c
        WHERE c.id = conversation_id
        AND (auth.uid() = c.participant1_id OR auth.uid() = c.participant2_id)
    )
);

-- Users can send messages in their conversations
CREATE POLICY "Users can send messages"
ON public.messages FOR INSERT
TO authenticated
WITH CHECK (
    auth.uid() = sender_id
    AND EXISTS (
        SELECT 1 FROM public.conversations c
        WHERE c.id = conversation_id
        AND (auth.uid() = c.participant1_id OR auth.uid() = c.participant2_id)
    )
);

-- Users can update messages (mark as read)
CREATE POLICY "Users can update messages"
ON public.messages FOR UPDATE
TO authenticated
USING (
    EXISTS (
        SELECT 1 FROM public.conversations c
        WHERE c.id = conversation_id
        AND (auth.uid() = c.participant1_id OR auth.uid() = c.participant2_id)
    )
);
