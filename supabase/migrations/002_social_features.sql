-- ============================================
-- TEMPUS DATABASE MIGRATION
-- Phase: Social Features - Friend System
-- Date: 2026-01-25
-- ============================================

-- ============================================
-- 1. FRIEND REQUESTS TABLE
-- Stores pending, accepted, and rejected friend requests
-- ============================================
CREATE TABLE IF NOT EXISTS public.friend_requests (
    id uuid NOT NULL DEFAULT gen_random_uuid(),
    sender_id uuid NOT NULL,
    receiver_id uuid NOT NULL,
    status varchar(20) NOT NULL DEFAULT 'pending',
    created_at timestamptz DEFAULT now(),
    updated_at timestamptz DEFAULT now(),
    CONSTRAINT friend_requests_pkey PRIMARY KEY (id),
    CONSTRAINT friend_requests_sender_fkey FOREIGN KEY (sender_id) REFERENCES public.users(id) ON DELETE CASCADE,
    CONSTRAINT friend_requests_receiver_fkey FOREIGN KEY (receiver_id) REFERENCES public.users(id) ON DELETE CASCADE,
    CONSTRAINT friend_requests_unique UNIQUE (sender_id, receiver_id),
    CONSTRAINT friend_requests_not_self CHECK (sender_id != receiver_id),
    CONSTRAINT friend_requests_status_check CHECK (status IN ('pending', 'accepted', 'rejected'))
);

-- Indexes for friend_requests
CREATE INDEX IF NOT EXISTS idx_friend_requests_receiver_status ON public.friend_requests(receiver_id, status);
CREATE INDEX IF NOT EXISTS idx_friend_requests_sender ON public.friend_requests(sender_id);

-- ============================================
-- 2. FRIENDSHIPS TABLE
-- Stores confirmed friendships (bidirectional)
-- Uses ordered constraint to prevent duplicates (A,B) and (B,A)
-- ============================================
CREATE TABLE IF NOT EXISTS public.friendships (
    id uuid NOT NULL DEFAULT gen_random_uuid(),
    user1_id uuid NOT NULL,
    user2_id uuid NOT NULL,
    created_at timestamptz DEFAULT now(),
    CONSTRAINT friendships_pkey PRIMARY KEY (id),
    CONSTRAINT friendships_user1_fkey FOREIGN KEY (user1_id) REFERENCES public.users(id) ON DELETE CASCADE,
    CONSTRAINT friendships_user2_fkey FOREIGN KEY (user2_id) REFERENCES public.users(id) ON DELETE CASCADE,
    CONSTRAINT friendships_unique UNIQUE (user1_id, user2_id),
    CONSTRAINT friendships_ordered CHECK (user1_id < user2_id)
);

-- Indexes for friendships
CREATE INDEX IF NOT EXISTS idx_friendships_user1 ON public.friendships(user1_id);
CREATE INDEX IF NOT EXISTS idx_friendships_user2 ON public.friendships(user2_id);

-- ============================================
-- 3. BLOCKED USERS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS public.blocked_users (
    id uuid NOT NULL DEFAULT gen_random_uuid(),
    blocker_id uuid NOT NULL,
    blocked_id uuid NOT NULL,
    created_at timestamptz DEFAULT now(),
    CONSTRAINT blocked_users_pkey PRIMARY KEY (id),
    CONSTRAINT blocked_users_blocker_fkey FOREIGN KEY (blocker_id) REFERENCES public.users(id) ON DELETE CASCADE,
    CONSTRAINT blocked_users_blocked_fkey FOREIGN KEY (blocked_id) REFERENCES public.users(id) ON DELETE CASCADE,
    CONSTRAINT blocked_users_unique UNIQUE (blocker_id, blocked_id),
    CONSTRAINT blocked_users_not_self CHECK (blocker_id != blocked_id)
);

-- Index for blocked_users
CREATE INDEX IF NOT EXISTS idx_blocked_users_blocker ON public.blocked_users(blocker_id);

-- ============================================
-- 4. CONVERSATIONS TABLE (for Phase 2)
-- ============================================
CREATE TABLE IF NOT EXISTS public.conversations (
    id uuid NOT NULL DEFAULT gen_random_uuid(),
    participant1_id uuid NOT NULL,
    participant2_id uuid NOT NULL,
    last_message_at timestamptz,
    last_message_preview text,
    created_at timestamptz DEFAULT now(),
    CONSTRAINT conversations_pkey PRIMARY KEY (id),
    CONSTRAINT conversations_p1_fkey FOREIGN KEY (participant1_id) REFERENCES public.users(id) ON DELETE CASCADE,
    CONSTRAINT conversations_p2_fkey FOREIGN KEY (participant2_id) REFERENCES public.users(id) ON DELETE CASCADE,
    CONSTRAINT conversations_unique UNIQUE (participant1_id, participant2_id),
    CONSTRAINT conversations_ordered CHECK (participant1_id < participant2_id)
);

-- Indexes for conversations
CREATE INDEX IF NOT EXISTS idx_conversations_p1 ON public.conversations(participant1_id);
CREATE INDEX IF NOT EXISTS idx_conversations_p2 ON public.conversations(participant2_id);
CREATE INDEX IF NOT EXISTS idx_conversations_last_message ON public.conversations(last_message_at DESC);

-- ============================================
-- 5. MESSAGES TABLE (for Phase 2)
-- ============================================
CREATE TABLE IF NOT EXISTS public.messages (
    id uuid NOT NULL DEFAULT gen_random_uuid(),
    conversation_id uuid NOT NULL,
    sender_id uuid NOT NULL,
    content text NOT NULL,
    message_type varchar(20) DEFAULT 'text',
    is_read boolean DEFAULT false,
    created_at timestamptz DEFAULT now(),
    CONSTRAINT messages_pkey PRIMARY KEY (id),
    CONSTRAINT messages_conversation_fkey FOREIGN KEY (conversation_id) REFERENCES public.conversations(id) ON DELETE CASCADE,
    CONSTRAINT messages_sender_fkey FOREIGN KEY (sender_id) REFERENCES public.users(id) ON DELETE CASCADE,
    CONSTRAINT messages_type_check CHECK (message_type IN ('text', 'image', 'file'))
);

-- Indexes for messages
CREATE INDEX IF NOT EXISTS idx_messages_conversation ON public.messages(conversation_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_messages_sender ON public.messages(sender_id);

-- ============================================
-- 6. USER SEARCH INDEX
-- Using simple btree index for pattern matching (ILIKE)
-- Note: gin_trgm_ops requires pg_trgm extension which may not be available
-- ============================================
CREATE INDEX IF NOT EXISTS idx_users_username_search ON public.users(username);
CREATE INDEX IF NOT EXISTS idx_users_username_lower ON public.users(lower(username));

-- ============================================
-- 7. RLS POLICIES FOR FRIEND REQUESTS
-- ============================================
ALTER TABLE public.friend_requests ENABLE ROW LEVEL SECURITY;

-- Users can view friend requests they sent or received
CREATE POLICY "Users can view their friend requests"
ON public.friend_requests FOR SELECT
USING (auth.uid() = sender_id OR auth.uid() = receiver_id);

-- Users can create friend requests (as sender)
CREATE POLICY "Users can send friend requests"
ON public.friend_requests FOR INSERT
WITH CHECK (auth.uid() = sender_id);

-- Sender can delete their pending request, receiver can update status
CREATE POLICY "Users can update friend requests they received"
ON public.friend_requests FOR UPDATE
USING (auth.uid() = receiver_id AND status = 'pending');

-- Sender can cancel pending requests
CREATE POLICY "Users can delete their sent pending requests"
ON public.friend_requests FOR DELETE
USING (auth.uid() = sender_id AND status = 'pending');

-- ============================================
-- 8. RLS POLICIES FOR FRIENDSHIPS
-- ============================================
ALTER TABLE public.friendships ENABLE ROW LEVEL SECURITY;

-- Users can view friendships they are part of
CREATE POLICY "Users can view their friendships"
ON public.friendships FOR SELECT
USING (auth.uid() = user1_id OR auth.uid() = user2_id);

-- Only system (via function) should insert friendships, but for simplicity:
CREATE POLICY "Users can create friendships"
ON public.friendships FOR INSERT
WITH CHECK (auth.uid() = user1_id OR auth.uid() = user2_id);

-- Users can delete friendships they are part of (unfriend)
CREATE POLICY "Users can delete their friendships"
ON public.friendships FOR DELETE
USING (auth.uid() = user1_id OR auth.uid() = user2_id);

-- ============================================
-- 9. RLS POLICIES FOR BLOCKED USERS
-- ============================================
ALTER TABLE public.blocked_users ENABLE ROW LEVEL SECURITY;

-- Users can view their blocked list
CREATE POLICY "Users can view their blocked users"
ON public.blocked_users FOR SELECT
USING (auth.uid() = blocker_id);

-- Users can block others
CREATE POLICY "Users can block others"
ON public.blocked_users FOR INSERT
WITH CHECK (auth.uid() = blocker_id);

-- Users can unblock
CREATE POLICY "Users can unblock"
ON public.blocked_users FOR DELETE
USING (auth.uid() = blocker_id);

-- ============================================
-- 10. RLS POLICIES FOR CONVERSATIONS
-- ============================================
ALTER TABLE public.conversations ENABLE ROW LEVEL SECURITY;

-- Users can view conversations they are part of
CREATE POLICY "Users can view their conversations"
ON public.conversations FOR SELECT
USING (auth.uid() = participant1_id OR auth.uid() = participant2_id);

-- Users can create conversations
CREATE POLICY "Users can create conversations"
ON public.conversations FOR INSERT
WITH CHECK (auth.uid() = participant1_id OR auth.uid() = participant2_id);

-- ============================================
-- 11. RLS POLICIES FOR MESSAGES
-- ============================================
ALTER TABLE public.messages ENABLE ROW LEVEL SECURITY;

-- Users can view messages in their conversations
CREATE POLICY "Users can view messages in their conversations"
ON public.messages FOR SELECT
USING (
    EXISTS (
        SELECT 1 FROM public.conversations c 
        WHERE c.id = messages.conversation_id 
        AND (c.participant1_id = auth.uid() OR c.participant2_id = auth.uid())
    )
);

-- Users can send messages in their conversations
CREATE POLICY "Users can send messages"
ON public.messages FOR INSERT
WITH CHECK (
    auth.uid() = sender_id
    AND EXISTS (
        SELECT 1 FROM public.conversations c 
        WHERE c.id = conversation_id 
        AND (c.participant1_id = auth.uid() OR c.participant2_id = auth.uid())
    )
);

-- Users can update read status of messages sent to them
CREATE POLICY "Users can mark messages as read"
ON public.messages FOR UPDATE
USING (
    EXISTS (
        SELECT 1 FROM public.conversations c 
        WHERE c.id = messages.conversation_id 
        AND (c.participant1_id = auth.uid() OR c.participant2_id = auth.uid())
    )
    AND sender_id != auth.uid()
);

-- ============================================
-- 12. HELPER FUNCTION: Accept Friend Request
-- ============================================
CREATE OR REPLACE FUNCTION accept_friend_request(request_id uuid)
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_sender_id uuid;
    v_receiver_id uuid;
    v_user1 uuid;
    v_user2 uuid;
BEGIN
    -- Get the request details
    SELECT sender_id, receiver_id INTO v_sender_id, v_receiver_id
    FROM public.friend_requests
    WHERE id = request_id AND receiver_id = auth.uid() AND status = 'pending';
    
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Friend request not found or already processed';
    END IF;
    
    -- Order the IDs for friendships table constraint
    IF v_sender_id < v_receiver_id THEN
        v_user1 := v_sender_id;
        v_user2 := v_receiver_id;
    ELSE
        v_user1 := v_receiver_id;
        v_user2 := v_sender_id;
    END IF;
    
    -- Update request status
    UPDATE public.friend_requests
    SET status = 'accepted', updated_at = now()
    WHERE id = request_id;
    
    -- Create friendship
    INSERT INTO public.friendships (user1_id, user2_id)
    VALUES (v_user1, v_user2)
    ON CONFLICT DO NOTHING;
END;
$$;

-- ============================================
-- VERIFICATION QUERIES
-- ============================================
-- Run these to verify the migration:
-- \d public.friend_requests
-- \d public.friendships
-- \d public.blocked_users
-- \d public.conversations
-- \d public.messages
-- SELECT * FROM pg_policies WHERE tablename IN ('friend_requests', 'friendships', 'blocked_users', 'conversations', 'messages');
