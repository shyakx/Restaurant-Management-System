-- Production Environment Setup
-- This migration creates the initial admin user for production

-- Check if we're in production environment
DO $$
BEGIN
    IF current_setting('flyway.environment', true) != 'prod' THEN
        RAISE EXCEPTION 'This migration is only for production environment';
    END IF;
END $$;

-- Create initial admin user only if no users exist
-- Password should be changed immediately after first login
DO $$
DECLARE
    user_count INTEGER;
    admin_user_id BIGINT;
BEGIN
    SELECT COUNT(*) INTO user_count FROM users;
    
    IF user_count = 0 THEN
        -- Create initial admin user with temporary password
        -- This password MUST be changed on first login
        INSERT INTO users (username, password, email, enabled) VALUES 
        ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFDYZt/I5/BFnhkSLsVBDSC', 'admin@restaurant.com', true)
        RETURNING id INTO admin_user_id;
        
        -- Grant admin role
        INSERT INTO user_roles (user_id, role, granted_by) VALUES 
        (admin_user_id, 'ROLE_ADMIN', 'SYSTEM'),
        (admin_user_id, 'ROLE_USER', 'SYSTEM');
        
        -- Log the creation
        INSERT INTO user_audit_log (user_id, action, performed_by, details) VALUES 
        (admin_user_id, 'INITIAL_ADMIN_CREATED', 'SYSTEM', 
         json_build_object('environment', 'production', 'requires_password_change', true));
         
        RAISE NOTICE 'Initial admin user created. Password must be changed immediately.';
    ELSE
        RAISE NOTICE 'Users already exist. Skipping initial admin creation.';
    END IF;
END $$;

-- Create stored procedure for secure user creation
CREATE OR REPLACE FUNCTION create_user(
    p_username VARCHAR(255),
    p_password VARCHAR(255),
    p_email VARCHAR(255),
    p_roles VARCHAR(255)[],
    p_created_by VARCHAR(255)
) RETURNS BIGINT AS $$
DECLARE
    new_user_id BIGINT;
    role_text VARCHAR(255);
BEGIN
    -- Insert user
    INSERT INTO users (username, password, email, enabled)
    VALUES (p_username, p_password, p_email, true)
    RETURNING id INTO new_user_id;
    
    -- Insert roles
    FOREACH role_text IN ARRAY p_roles
    LOOP
        INSERT INTO user_roles (user_id, role, granted_by)
        VALUES (new_user_id, role_text, p_created_by);
    END LOOP;
    
    -- Log creation
    INSERT INTO user_audit_log (user_id, action, performed_by, details)
    VALUES (new_user_id, 'USER_CREATED', p_created_by,
            json_build_object('username', p_username, 'email', p_email, 'roles', p_roles));
    
    RETURN new_user_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Create function for password reset
CREATE OR REPLACE FUNCTION reset_user_password(
    p_user_id BIGINT,
    p_new_password VARCHAR(255),
    p_reset_by VARCHAR(255)
) RETURNS BOOLEAN AS $$
DECLARE
    user_exists BOOLEAN;
BEGIN
    SELECT EXISTS(SELECT 1 FROM users WHERE id = p_user_id) INTO user_exists;
    
    IF user_exists THEN
        UPDATE users 
        SET password = p_new_password, 
            updated_at = CURRENT_TIMESTAMP,
            credentials_non_expired = true
        WHERE id = p_user_id;
        
        -- Log password reset
        INSERT INTO user_audit_log (user_id, action, performed_by, details)
        VALUES (p_user_id, 'PASSWORD_RESET', p_reset_by,
                json_build_object('timestamp', CURRENT_TIMESTAMP));
        
        RETURN TRUE;
    ELSE
        RETURN FALSE;
    END IF;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Grant necessary permissions
GRANT EXECUTE ON FUNCTION create_user(VARCHAR(255), VARCHAR(255), VARCHAR(255), VARCHAR(255)[], VARCHAR(255)) TO menu_user;
GRANT EXECUTE ON FUNCTION reset_user_password(BIGINT, VARCHAR(255), VARCHAR(255)) TO menu_user;

-- Add production-specific comments
COMMENT ON FUNCTION create_user IS 'Secure function for creating users with audit trail';
COMMENT ON FUNCTION reset_user_password IS 'Secure function for resetting user passwords with audit trail';
