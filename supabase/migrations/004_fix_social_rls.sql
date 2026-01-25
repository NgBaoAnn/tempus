-- ============================================
-- TEMPUS DATABASE MIGRATION
-- Phase: Fix RLS Policies for Social Features
-- Date: 2026-01-25
-- ============================================

-- ============================================
-- 1. DROP EXISTING POLICIES (nếu có lỗi)
-- ============================================
DROP POLICY IF EXISTS "Users can view their friend requests" ON public.friend_requests;
DROP POLICY IF EXISTS "Users can send friend requests" ON public.friend_requests;
DROP POLICY IF EXISTS "Users can update friend requests they received" ON public.friend_requests;
DROP POLICY IF EXISTS "Users can delete their sent pending requests" ON public.friend_requests;

DROP POLICY IF EXISTS "Users can view their friendships" ON public.friendships;
DROP POLICY IF EXISTS "Users can create friendships" ON public.friendships;
DROP POLICY IF EXISTS "Users can delete their friendships" ON public.friendships;

DROP POLICY IF EXISTS "Users can view their blocked users" ON public.blocked_users;
DROP POLICY IF EXISTS "Users can block others" ON public.blocked_users;
DROP POLICY IF EXISTS "Users can unblock" ON public.blocked_users;

-- ============================================
-- 2. ENABLE RLS ON TABLES
-- ============================================
ALTER TABLE public.friend_requests ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.friendships ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.blocked_users ENABLE ROW LEVEL SECURITY;

-- ============================================
-- 3. FRIEND_REQUESTS POLICIES
-- ============================================

-- Users can view friend requests they sent or received
CREATE POLICY "Users can view their friend requests"
ON public.friend_requests FOR SELECT
TO authenticated
USING (auth.uid() = sender_id OR auth.uid() = receiver_id);

-- Users can create friend requests (as sender)
CREATE POLICY "Users can send friend requests"
ON public.friend_requests FOR INSERT
TO authenticated
WITH CHECK (auth.uid() = sender_id);

-- Receiver can update status (accept/reject)
CREATE POLICY "Users can update friend requests they received"
ON public.friend_requests FOR UPDATE
TO authenticated
USING (auth.uid() = receiver_id);

-- Sender can cancel pending requests
CREATE POLICY "Users can delete their sent pending requests"
ON public.friend_requests FOR DELETE
TO authenticated
USING (auth.uid() = sender_id);

-- ============================================
-- 4. FRIENDSHIPS POLICIES
-- ============================================

-- Users can view friendships they are part of
CREATE POLICY "Users can view their friendships"
ON public.friendships FOR SELECT
TO authenticated
USING (auth.uid() = user1_id OR auth.uid() = user2_id);

-- Users can create friendships
CREATE POLICY "Users can create friendships"
ON public.friendships FOR INSERT
TO authenticated
WITH CHECK (auth.uid() = user1_id OR auth.uid() = user2_id);

-- Users can delete friendships (unfriend)
CREATE POLICY "Users can delete their friendships"
ON public.friendships FOR DELETE
TO authenticated
USING (auth.uid() = user1_id OR auth.uid() = user2_id);

-- ============================================
-- 5. BLOCKED_USERS POLICIES
-- ============================================

-- Users can view their blocked list
CREATE POLICY "Users can view their blocked users"
ON public.blocked_users FOR SELECT
TO authenticated
USING (auth.uid() = blocker_id);

-- Users can block others
CREATE POLICY "Users can block others"
ON public.blocked_users FOR INSERT
TO authenticated
WITH CHECK (auth.uid() = blocker_id);

-- Users can unblock
CREATE POLICY "Users can unblock"
ON public.blocked_users FOR DELETE
TO authenticated
USING (auth.uid() = blocker_id);

-- ============================================
-- VERIFICATION: Run these after migration
-- ============================================
-- SELECT * FROM pg_policies WHERE tablename IN ('friend_requests', 'friendships', 'blocked_users');
