-- Development/Test Environment Initial Admin Setup Only
-- WARNING: This migration should NOT run in production
-- PURPOSE: Create initial admin user for system access
-- ALL OTHER USERS SHOULD BE CREATED VIA API

-- Check if we're in a non-production environment
DO $$
BEGIN
    IF current_setting('flyway.environment', true) NOT IN ('dev', 'test', 'local-dev') THEN
        RAISE EXCEPTION 'This migration is only for development/test environments';
    END IF;
END $$;

-- NOTE: Generate proper hashes using GenerateProductionHashes.kt
-- Run: ./gradlew run --args="--main-class=com.restaurant.menu.util.GenerateProductionHashes"

-- Insert ONLY initial admin user (password: admin123)
-- This is the ONLY user created via migration for initial system access
INSERT INTO users (username, password, email, enabled) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFDYZt/I5/BFnhkSLsVBDSC', 'admin@restaurant.local', true)
ON CONFLICT (username) DO NOTHING;

-- Get admin user ID and assign roles
DO $$
DECLARE
    admin_user_id BIGINT;
BEGIN
    SELECT id INTO admin_user_id FROM users WHERE username = 'admin';
    
    IF admin_user_id IS NOT NULL THEN
        -- Insert admin roles
        INSERT INTO user_roles (user_id, role, granted_by) VALUES 
        (admin_user_id, 'ROLE_ADMIN', 'SYSTEM'),
        (admin_user_id, 'ROLE_USER', 'SYSTEM')
        ON CONFLICT (user_id, role) DO NOTHING;
        
        -- Log the initial admin creation
        INSERT INTO user_audit_log (user_id, action, performed_by, details) VALUES 
        (admin_user_id, 'INITIAL_ADMIN_CREATED', 'SYSTEM', 
         json_build_object(
             'environment', current_setting('flyway.environment', true), 
             'type', 'initial_setup',
             'note', 'All other users should be created via API endpoints'
         ));
    END IF;
END $$;

-- Add clear documentation
COMMENT ON TABLE users IS 'User accounts - Initial admin created via migration, all others via API';
COMMENT ON TABLE user_roles IS 'Role assignments - Use /api/users for management';
COMMENT ON TABLE user_audit_log IS 'Audit log - Tracks all user management actions';
