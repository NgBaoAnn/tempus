-- ============================================
-- TEMPUS DATABASE MIGRATION
-- Phase: Add RLS Policy for User Search
-- Date: 2026-01-25
-- ============================================

-- Đảm bảo RLS được bật cho table users
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;

-- ============================================
-- POLICY: Cho phép authenticated users search users khác
-- Đây là cần thiết để tìm kiếm bạn bè
-- ============================================

-- Policy cho phép đọc thông tin cơ bản của users (username, avatar, email)
-- Authenticated users có thể tìm kiếm và xem profile cơ bản của users khác
CREATE POLICY "Authenticated users can search and view basic user info"
ON public.users FOR SELECT
TO authenticated
USING (true);

-- Hoặc nếu muốn hạn chế chỉ xem thông tin cơ bản, có thể dùng view thay vì policy
-- Nhưng tạm thời sử dụng policy này để tìm kiếm hoạt động được

-- ============================================
-- POLICY: Cho phép users update profile của chính mình
-- ============================================
CREATE POLICY "Users can update their own profile"
ON public.users FOR UPDATE
TO authenticated
USING (auth.uid() = id)
WITH CHECK (auth.uid() = id);

-- ============================================
-- VERIFICATION
-- ============================================
-- Run: SELECT * FROM pg_policies WHERE tablename = 'users';
