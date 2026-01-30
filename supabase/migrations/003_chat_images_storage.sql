-- ============================================
-- TEMPUS DATABASE MIGRATION
-- Phase: Chat Images Storage Bucket
-- Date: 2026-01-30
-- ============================================

-- Create storage bucket for chat images
INSERT INTO storage.buckets (id, name, public) 
VALUES ('chat-images', 'chat-images', true)
ON CONFLICT (id) DO NOTHING;

-- RLS Policy: Allow authenticated users to upload chat images
CREATE POLICY "Authenticated users can upload chat images"
ON storage.objects FOR INSERT
TO authenticated
WITH CHECK (bucket_id = 'chat-images');

-- RLS Policy: Anyone can view chat images (public bucket)
CREATE POLICY "Anyone can view chat images"
ON storage.objects FOR SELECT
TO public
USING (bucket_id = 'chat-images');

-- RLS Policy: Users can delete their own uploaded images
CREATE POLICY "Users can delete own chat images"
ON storage.objects FOR DELETE
TO authenticated
USING (bucket_id = 'chat-images' AND auth.uid()::text = (storage.foldername(name))[1]);
