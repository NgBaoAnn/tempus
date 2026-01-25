-- ============================================
-- TEMPUS DATABASE MIGRATION
-- Phase 1: Priority System & Categories
-- Date: 2026-01-22
-- ============================================

-- ============================================
-- 1. PRIORITY ENUM TYPE
-- ============================================
-- Create priority enum type
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'priority_type_enum') THEN
        CREATE TYPE priority_type_enum AS ENUM ('high', 'medium', 'low');
    END IF;
END $$;

-- ============================================
-- 2. ADD PRIORITY COLUMN TO SCHEDULE
-- ============================================
-- Add priority column with default 'medium'
ALTER TABLE public.schedule 
ADD COLUMN IF NOT EXISTS priority priority_type_enum DEFAULT 'medium';

-- ============================================
-- 3. CATEGORIES TABLE (Phase 1.2 - Optional)
-- ============================================
-- Create categories table for task grouping
CREATE TABLE IF NOT EXISTS public.categories (
    id uuid NOT NULL DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL,
    name character varying(100) NOT NULL,
    color character varying(20) DEFAULT '#2196F3',
    icon character varying(50) DEFAULT 'folder',
    created_at timestamp with time zone DEFAULT now(),
    CONSTRAINT categories_pkey PRIMARY KEY (id),
    CONSTRAINT categories_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE
);

-- Add index for faster lookup
CREATE INDEX IF NOT EXISTS idx_categories_user_id ON public.categories(user_id);

-- ============================================
-- 4. ADD CATEGORY REFERENCE TO SCHEDULE
-- ============================================
-- Add category_id column to schedule (nullable - not all tasks need a category)
ALTER TABLE public.schedule 
ADD COLUMN IF NOT EXISTS category_id uuid REFERENCES public.categories(id) ON DELETE SET NULL;

-- Add index for category filtering
CREATE INDEX IF NOT EXISTS idx_schedule_category_id ON public.schedule(category_id);

-- ============================================
-- 5. ADD DESCRIPTION TO SCHEDULE (Bonus)
-- ============================================
-- Add description column for task notes
ALTER TABLE public.schedule 
ADD COLUMN IF NOT EXISTS description text;

-- ============================================
-- VERIFICATION QUERIES
-- ============================================
-- Run these to verify the migration:
-- SELECT column_name, data_type FROM information_schema.columns WHERE table_name = 'schedule';
-- SELECT * FROM pg_type WHERE typname = 'priority_type_enum';
-- \d public.categories
