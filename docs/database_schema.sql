-- WARNING: This schema is for context only and is not meant to be run.
-- Table order and constraints may not be valid for execution.

CREATE TABLE public.ai_history (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  user_id uuid NOT NULL,
  prompt text,
  response text,
  meta jsonb,
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT ai_history_pkey PRIMARY KEY (id),
  CONSTRAINT ai_history_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id)
);
CREATE TABLE public.blocked_users (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  blocker_id uuid NOT NULL,
  blocked_id uuid NOT NULL,
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT blocked_users_pkey PRIMARY KEY (id),
  CONSTRAINT blocked_users_blocker_fkey FOREIGN KEY (blocker_id) REFERENCES public.users(id),
  CONSTRAINT blocked_users_blocked_fkey FOREIGN KEY (blocked_id) REFERENCES public.users(id)
);
CREATE TABLE public.categories (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  user_id uuid NOT NULL,
  name character varying NOT NULL,
  color character varying DEFAULT '#2196F3'::character varying,
  icon character varying DEFAULT 'folder'::character varying,
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT categories_pkey PRIMARY KEY (id),
  CONSTRAINT categories_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id)
);
CREATE TABLE public.conversations (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  participant1_id uuid NOT NULL,
  participant2_id uuid NOT NULL,
  last_message_at timestamp with time zone,
  last_message_preview text,
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT conversations_pkey PRIMARY KEY (id),
  CONSTRAINT conversations_p1_fkey FOREIGN KEY (participant1_id) REFERENCES public.users(id),
  CONSTRAINT conversations_p2_fkey FOREIGN KEY (participant2_id) REFERENCES public.users(id)
);
CREATE TABLE public.edited_version (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  name_schedule character varying,
  icon_id integer,
  start_time_date timestamp with time zone,
  implementation_time interval,
  color character varying,
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT edited_version_pkey PRIMARY KEY (id)
);
CREATE TABLE public.friend_requests (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  sender_id uuid NOT NULL,
  receiver_id uuid NOT NULL,
  status character varying NOT NULL DEFAULT 'pending'::character varying CHECK (status::text = ANY (ARRAY['pending'::character varying, 'accepted'::character varying, 'rejected'::character varying]::text[])),
  created_at timestamp with time zone DEFAULT now(),
  updated_at timestamp with time zone DEFAULT now(),
  CONSTRAINT friend_requests_pkey PRIMARY KEY (id),
  CONSTRAINT friend_requests_sender_fkey FOREIGN KEY (sender_id) REFERENCES public.users(id),
  CONSTRAINT friend_requests_receiver_fkey FOREIGN KEY (receiver_id) REFERENCES public.users(id)
);
CREATE TABLE public.friendships (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  user1_id uuid NOT NULL,
  user2_id uuid NOT NULL,
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT friendships_pkey PRIMARY KEY (id),
  CONSTRAINT friendships_user1_fkey FOREIGN KEY (user1_id) REFERENCES public.users(id),
  CONSTRAINT friendships_user2_fkey FOREIGN KEY (user2_id) REFERENCES public.users(id)
);
CREATE TABLE public.messages (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  conversation_id uuid NOT NULL,
  sender_id uuid NOT NULL,
  content text NOT NULL,
  message_type character varying DEFAULT 'text'::character varying CHECK (message_type::text = ANY (ARRAY['text'::character varying, 'image'::character varying, 'file'::character varying]::text[])),
  is_read boolean DEFAULT false,
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT messages_pkey PRIMARY KEY (id),
  CONSTRAINT messages_conversation_fkey FOREIGN KEY (conversation_id) REFERENCES public.conversations(id),
  CONSTRAINT messages_sender_fkey FOREIGN KEY (sender_id) REFERENCES public.users(id)
);
CREATE TABLE public.personalization (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  user_id uuid NOT NULL,
  name character varying,
  wake_up_time time without time zone,
  sleep_time time without time zone,
  start_working_time time without time zone,
  end_working_time time without time zone,
  CONSTRAINT personalization_pkey PRIMARY KEY (id),
  CONSTRAINT personalization_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id)
);
CREATE TABLE public.point_history (
  id bigint NOT NULL DEFAULT nextval('point_history_id_seq'::regclass),
  user_id uuid NOT NULL,
  points integer NOT NULL,
  reason text NOT NULL,
  timestamp bigint DEFAULT ((EXTRACT(epoch FROM now()) * (1000)::numeric))::bigint,
  CONSTRAINT point_history_pkey PRIMARY KEY (id)
);
CREATE TABLE public.schedule (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  user_id uuid NOT NULL,
  name_schedule character varying NOT NULL,
  label USER-DEFINED,
  start_time_date timestamp with time zone NOT NULL,
  implementation_time interval NOT NULL,
  repeat USER-DEFINED NOT NULL DEFAULT 'once'::repeat_type_enum,
  color character varying DEFAULT '#2196F3'::character varying,
  source USER-DEFINED NOT NULL DEFAULT 'manual'::source_enum,
  created_at timestamp with time zone DEFAULT now(),
  priority USER-DEFINED DEFAULT 'medium'::priority_type_enum,
  category_id uuid,
  description text,
  end_date timestamp with time zone,
  repeat_days text,
  CONSTRAINT schedule_pkey PRIMARY KEY (id),
  CONSTRAINT schedule_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id),
  CONSTRAINT schedule_category_id_fkey FOREIGN KEY (category_id) REFERENCES public.categories(id)
);
CREATE TABLE public.schedule_items (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  task_id uuid NOT NULL,
  date date NOT NULL,
  status USER-DEFINED NOT NULL DEFAULT 'planned'::status_type_enum,
  edited_version uuid,
  created_at timestamp with time zone DEFAULT now(),
  updated_at timestamp with time zone DEFAULT now(),
  CONSTRAINT schedule_items_pkey PRIMARY KEY (id),
  CONSTRAINT schedule_items_task_id_fkey FOREIGN KEY (task_id) REFERENCES public.schedule(id),
  CONSTRAINT schedule_items_edited_version_fkey FOREIGN KEY (edited_version) REFERENCES public.edited_version(id)
);
CREATE TABLE public.sub_task (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  schedule_id uuid NOT NULL,
  title text NOT NULL,
  is_done boolean NOT NULL DEFAULT false,
  order_no integer DEFAULT 0,
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT sub_task_pkey PRIMARY KEY (id),
  CONSTRAINT sub_task_schedule_id_fkey FOREIGN KEY (schedule_id) REFERENCES public.schedule(id)
);
CREATE TABLE public.timer_sessions (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  user_id uuid NOT NULL,
  session_type USER-DEFINED NOT NULL DEFAULT 'focus'::session_type_enum,
  started_at timestamp with time zone NOT NULL DEFAULT now(),
  ended_at timestamp with time zone,
  duration_sec integer,
  note text,
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT timer_sessions_pkey PRIMARY KEY (id),
  CONSTRAINT timer_sessions_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id)
);
CREATE TABLE public.trees (
  id bigint NOT NULL DEFAULT nextval('trees_id_seq'::regclass),
  user_id uuid NOT NULL,
  name text DEFAULT 'My Tree'::text,
  tree_type text DEFAULT 'oak'::text,
  invested_points integer DEFAULT 0,
  state text DEFAULT 'SEED'::text,
  created_at bigint DEFAULT ((EXTRACT(epoch FROM now()) * (1000)::numeric))::bigint,
  last_watered_at bigint DEFAULT ((EXTRACT(epoch FROM now()) * (1000)::numeric))::bigint,
  is_alive boolean DEFAULT true,
  CONSTRAINT trees_pkey PRIMARY KEY (id)
);
CREATE TABLE public.user_constraints (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  user_id uuid NOT NULL UNIQUE,
  persona_type text DEFAULT 'custom'::text,
  wake_time text DEFAULT '07:00'::text,
  sleep_time text DEFAULT '23:00'::text,
  fixed_slots jsonb DEFAULT '[]'::jsonb,
  activity_prefs jsonb DEFAULT '[]'::jsonb,
  created_at timestamp with time zone DEFAULT now(),
  updated_at timestamp with time zone DEFAULT now(),
  CONSTRAINT user_constraints_pkey PRIMARY KEY (id),
  CONSTRAINT user_constraints_user_id_fkey FOREIGN KEY (user_id) REFERENCES auth.users(id)
);
CREATE TABLE public.user_points (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  user_id uuid NOT NULL UNIQUE,
  total_points integer DEFAULT 0,
  current_streak integer DEFAULT 0,
  best_streak integer DEFAULT 0,
  last_active_date text,
  level integer DEFAULT 1,
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT user_points_pkey PRIMARY KEY (id)
);
CREATE TABLE public.users (
  id uuid NOT NULL DEFAULT gen_random_uuid(),
  username character varying,
  email character varying UNIQUE,
  created_at timestamp with time zone DEFAULT now(),
  avatar text,
  theme_color character varying,
  app_color character varying,
  CONSTRAINT users_pkey PRIMARY KEY (id),
  CONSTRAINT users_id_fkey FOREIGN KEY (id) REFERENCES auth.users(id)
);