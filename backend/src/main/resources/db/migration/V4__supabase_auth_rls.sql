-- V4 Supabase Auth, Profiles, and PostgreSQL Row Level Security (RLS)

-- 1. Profiles Table linked to Supabase auth.users
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    full_name TEXT,
    email TEXT,
    avatar_url TEXT,
    role TEXT DEFAULT 'ROLE_USER',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 2. Trigger Function to automatically create profile & sync public.users on signup
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
DECLARE
    v_first_name TEXT;
    v_last_name TEXT;
    v_full_name TEXT;
    v_username TEXT;
BEGIN
    v_full_name := COALESCE(NEW.raw_user_meta_data->>'full_name', NEW.raw_user_meta_data->>'name', '');
    v_first_name := COALESCE(NEW.raw_user_meta_data->>'first_name', split_part(v_full_name, ' ', 1));
    v_last_name := COALESCE(NEW.raw_user_meta_data->>'last_name', substring(v_full_name from length(v_first_name) + 2));
    v_username := COALESCE(NEW.raw_user_meta_data->>'username', split_part(NEW.email, '@', 1) || '_' || substr(NEW.id::text, 1, 6));

    -- Upsert Profile
    INSERT INTO public.profiles (id, full_name, email, role, created_at, updated_at)
    VALUES (NEW.id, v_full_name, NEW.email, 'ROLE_USER', NOW(), NOW())
    ON CONFLICT (id) DO UPDATE SET 
        full_name = EXCLUDED.full_name,
        email = EXCLUDED.email,
        updated_at = NOW();

    -- Upsert public.users for backend JPA integration
    INSERT INTO public.users (
        id, username, email, password_hash, first_name, last_name, role, status, email_verified, storage_used, storage_limit, created_at, updated_at
    )
    VALUES (
        NEW.id,
        v_username,
        NEW.email,
        'SUPABASE_AUTH',
        v_first_name,
        v_last_name,
        'ROLE_USER',
        'ACTIVE',
        true,
        0,
        16106127360,
        NOW(),
        NOW()
    )
    ON CONFLICT (id) DO UPDATE SET
        email = EXCLUDED.email,
        updated_at = NOW();

    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Trigger on auth.users
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- Trigger to auto-confirm users if desired so they can immediately log in
CREATE OR REPLACE FUNCTION public.auto_confirm_user()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.email_confirmed_at IS NULL THEN
        NEW.email_confirmed_at := NOW();
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS on_auth_user_auto_confirm ON auth.users;
CREATE TRIGGER on_auth_user_auto_confirm
    BEFORE INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.auto_confirm_user();

-- Backfill any existing auth.users into profiles and users
INSERT INTO public.profiles (id, full_name, email, role)
SELECT id, COALESCE(raw_user_meta_data->>'full_name', email), email, 'ROLE_USER'
FROM auth.users
ON CONFLICT (id) DO NOTHING;

-- 3. Performance Indexes on User Ownership Columns
CREATE INDEX IF NOT EXISTS idx_files_owner_id ON public.files(owner_id);
CREATE INDEX IF NOT EXISTS idx_folders_owner_id ON public.folders(owner_id);
CREATE INDEX IF NOT EXISTS idx_favorites_user_id ON public.favorites(user_id);
CREATE INDEX IF NOT EXISTS idx_trash_items_deleted_by_id ON public.trash_items(deleted_by_id);
CREATE INDEX IF NOT EXISTS idx_workspaces_owner_id ON public.workspaces(owner_id);
CREATE INDEX IF NOT EXISTS idx_profiles_id ON public.profiles(id);

-- 4. Enable Row Level Security (RLS) on User-Owned Tables
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.files ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.folders ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.workspaces ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.favorites ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.trash_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.tags ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.comments ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.file_versions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.document_insights ENABLE ROW LEVEL SECURITY;

-- 5. RLS Policies for Profiles
DROP POLICY IF EXISTS "Users can view own profile" ON public.profiles;
CREATE POLICY "Users can view own profile" ON public.profiles
    FOR SELECT TO authenticated
    USING (auth.uid() = id);

DROP POLICY IF EXISTS "Users can update own profile" ON public.profiles;
CREATE POLICY "Users can update own profile" ON public.profiles
    FOR UPDATE TO authenticated
    USING (auth.uid() = id)
    WITH CHECK (auth.uid() = id);

-- 6. RLS Policies for Files (Owner Isolation)
DROP POLICY IF EXISTS "Users can view own files" ON public.files;
CREATE POLICY "Users can view own files" ON public.files
    FOR SELECT TO authenticated
    USING (auth.uid() = owner_id);

DROP POLICY IF EXISTS "Users can insert own files" ON public.files;
CREATE POLICY "Users can insert own files" ON public.files
    FOR INSERT TO authenticated
    WITH CHECK (auth.uid() = owner_id);

DROP POLICY IF EXISTS "Users can update own files" ON public.files;
CREATE POLICY "Users can update own files" ON public.files
    FOR UPDATE TO authenticated
    USING (auth.uid() = owner_id)
    WITH CHECK (auth.uid() = owner_id);

DROP POLICY IF EXISTS "Users can delete own files" ON public.files;
CREATE POLICY "Users can delete own files" ON public.files
    FOR DELETE TO authenticated
    USING (auth.uid() = owner_id);

-- 7. RLS Policies for Folders
DROP POLICY IF EXISTS "Users can view own folders" ON public.folders;
CREATE POLICY "Users can view own folders" ON public.folders
    FOR SELECT TO authenticated
    USING (auth.uid() = owner_id);

DROP POLICY IF EXISTS "Users can insert own folders" ON public.folders;
CREATE POLICY "Users can insert own folders" ON public.folders
    FOR INSERT TO authenticated
    WITH CHECK (auth.uid() = owner_id);

DROP POLICY IF EXISTS "Users can update own folders" ON public.folders;
CREATE POLICY "Users can update own folders" ON public.folders
    FOR UPDATE TO authenticated
    USING (auth.uid() = owner_id)
    WITH CHECK (auth.uid() = owner_id);

DROP POLICY IF EXISTS "Users can delete own folders" ON public.folders;
CREATE POLICY "Users can delete own folders" ON public.folders
    FOR DELETE TO authenticated
    USING (auth.uid() = owner_id);

-- 8. RLS Policies for Favorites
DROP POLICY IF EXISTS "Users can view own favorites" ON public.favorites;
CREATE POLICY "Users can view own favorites" ON public.favorites
    FOR SELECT TO authenticated
    USING (auth.uid() = user_id);

DROP POLICY IF EXISTS "Users can insert own favorites" ON public.favorites;
CREATE POLICY "Users can insert own favorites" ON public.favorites
    FOR INSERT TO authenticated
    WITH CHECK (auth.uid() = user_id);

DROP POLICY IF EXISTS "Users can delete own favorites" ON public.favorites;
CREATE POLICY "Users can delete own favorites" ON public.favorites
    FOR DELETE TO authenticated
    USING (auth.uid() = user_id);

-- 9. RLS Policies for Trash Items
DROP POLICY IF EXISTS "Users can view own trash items" ON public.trash_items;
CREATE POLICY "Users can view own trash items" ON public.trash_items
    FOR SELECT TO authenticated
    USING (auth.uid() = deleted_by_id);

DROP POLICY IF EXISTS "Users can insert own trash items" ON public.trash_items;
CREATE POLICY "Users can insert own trash items" ON public.trash_items
    FOR INSERT TO authenticated
    WITH CHECK (auth.uid() = deleted_by_id);

DROP POLICY IF EXISTS "Users can delete own trash items" ON public.trash_items;
CREATE POLICY "Users can delete own trash items" ON public.trash_items
    FOR DELETE TO authenticated
    USING (auth.uid() = deleted_by_id);

-- 10. RLS Policies for Storage Objects (Bucket: cloudstorage)
-- Enforce user data isolation in Supabase Storage bucket so users can only access objects under their own user UUID folder!
DROP POLICY IF EXISTS "Users can view own bucket objects" ON storage.objects;
CREATE POLICY "Users can view own bucket objects" ON storage.objects
    FOR SELECT TO authenticated
    USING (bucket_id = 'cloudstorage' AND (storage.foldername(name))[1] = auth.uid()::text);

DROP POLICY IF EXISTS "Users can upload own bucket objects" ON storage.objects;
CREATE POLICY "Users can upload own bucket objects" ON storage.objects
    FOR INSERT TO authenticated
    WITH CHECK (bucket_id = 'cloudstorage' AND (storage.foldername(name))[1] = auth.uid()::text);

DROP POLICY IF EXISTS "Users can update own bucket objects" ON storage.objects;
CREATE POLICY "Users can update own bucket objects" ON storage.objects
    FOR UPDATE TO authenticated
    USING (bucket_id = 'cloudstorage' AND (storage.foldername(name))[1] = auth.uid()::text);

DROP POLICY IF EXISTS "Users can delete own bucket objects" ON storage.objects;
CREATE POLICY "Users can delete own bucket objects" ON storage.objects
    FOR DELETE TO authenticated
    USING (bucket_id = 'cloudstorage' AND (storage.foldername(name))[1] = auth.uid()::text);
