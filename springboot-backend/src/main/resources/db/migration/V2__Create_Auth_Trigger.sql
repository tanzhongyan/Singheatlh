-- =====================================================
-- Supabase Auth Integration
-- Migration: V2__Create_Auth_Trigger.sql
-- Description: Automatically creates User_Profile records when users sign up via Supabase Auth
-- =====================================================

-- =====================================================
-- Function: handle_new_user()
-- Description: Triggered after INSERT on auth.users to create corresponding User_Profile
-- =====================================================
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    -- Insert a new row into public.User_Profile
    INSERT INTO public.User_Profile (user_id, email, telephone_number, role, name)
    VALUES (
        NEW.id,                    -- UUID from auth.users
        NEW.email,                 -- Email from auth.users
        NEW.phone,                 -- Phone from auth.users (can be NULL)
        'P',                       -- Default role is 'P' for Patient
        COALESCE(NEW.raw_user_meta_data->>'name', 
                 NEW.raw_user_meta_data->>'full_name',
                 split_part(NEW.email, '@', 1))  -- Extract name from metadata or email
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- =====================================================
-- Trigger: on_auth_user_created
-- Description: Fires after a new user is inserted into auth.users
-- Note: This requires elevated privileges, wrapped in DO block to handle permission errors
-- =====================================================
DO $$
BEGIN
    -- Check if trigger already exists to avoid duplicate errors
    IF NOT EXISTS (
        SELECT 1 FROM pg_trigger 
        WHERE tgname = 'on_auth_user_created' 
        AND tgrelid = 'auth.users'::regclass
    ) THEN
        -- Attempt to create trigger (may fail if postgres user lacks permissions)
        BEGIN
            CREATE TRIGGER on_auth_user_created
                AFTER INSERT ON auth.users
                FOR EACH ROW
                EXECUTE FUNCTION public.handle_new_user();
            
            RAISE NOTICE 'Successfully created trigger on_auth_user_created';
        EXCEPTION 
            WHEN insufficient_privilege THEN
                RAISE WARNING 'Could not create trigger on auth.users: insufficient privileges. You need to run this as supabase_admin or use Supabase Dashboard to create the trigger manually.';
            WHEN OTHERS THEN
                RAISE WARNING 'Could not create trigger on auth.users: %', SQLERRM;
        END;
    ELSE
        RAISE NOTICE 'Trigger on_auth_user_created already exists, skipping creation';
    END IF;
END $$;

-- =====================================================
-- Comments for Documentation
-- =====================================================
COMMENT ON FUNCTION public.handle_new_user() IS 
    'Automatically creates a User_Profile record when a new user signs up via Supabase Auth. Maps user_id (UUID), email, phone, and extracts name from metadata. Defaults role to Patient (P).';

-- Note: COMMENT ON TRIGGER requires the same permissions as creating the trigger
-- So we wrap it in a DO block with exception handling
DO $$
BEGIN
    BEGIN
        EXECUTE 'COMMENT ON TRIGGER on_auth_user_created ON auth.users IS ''Triggers handle_new_user() function after user registration to maintain sync between auth.users and User_Profile''';
    EXCEPTION 
        WHEN insufficient_privilege THEN
            RAISE NOTICE 'Could not add comment to trigger: insufficient privileges';
        WHEN undefined_table OR undefined_object THEN
            RAISE NOTICE 'Could not add comment to trigger: trigger does not exist (permission error prevented creation)';
        WHEN OTHERS THEN
            RAISE NOTICE 'Could not add comment to trigger: %', SQLERRM;
    END;
END $$;

-- =====================================================
-- Grant necessary permissions
-- Description: Ensures the function can be executed by the authenticator role
-- =====================================================
GRANT USAGE ON SCHEMA public TO authenticator;
GRANT EXECUTE ON FUNCTION public.handle_new_user() TO authenticator;
