CREATE OR REPLACE PROCEDURE save_user(
    p_email              IN  VARCHAR2,
    p_password_hash      IN  VARCHAR2,
    p_name               IN  VARCHAR2,
    p_phone_country_code IN  VARCHAR2,
    p_phone_number       IN  VARCHAR2,
    p_is_active          IN  NUMBER,
    p_user_id            OUT NUMBER
) AS
BEGIN
    INSERT INTO USERS (
        email,
        password_hash,
        name,
        phone_country_code,
        phone_number,
        is_active
    ) VALUES (
        p_email,
        p_password_hash,
        p_name,
        p_phone_country_code,
        p_phone_number,
        p_is_active
    )
    RETURNING user_id INTO p_user_id;

    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END save_user;
/

CREATE OR REPLACE PROCEDURE delete_user(
    p_user_id IN NUMBER
) AS
BEGIN
    -- 1. Remove security roles immediately to prevent any access
    DELETE FROM USER_ROLES WHERE user_id = p_user_id;

    -- 2. Clear role-specific sensitive data but keep the records
    -- (Keeping the record allows the Department to still show who 'used' to be there)
    UPDATE CUSTOMERS
    SET address = 'ANONYMIZED', id_proof = NULL, loyalty_points = 0
    WHERE user_id = p_user_id;

    -- 3. Check if they are Staff or Manager and 'deactivate' their profile
    -- We keep the job_description and salary for financial history/reports
    UPDATE STAFF SET manager_id = NULL WHERE user_id = p_user_id;

    -- If they were a Head of Department, we must remove them as head
    UPDATE DEPARTMENTS SET head_manager_id = NULL WHERE head_manager_id = (
        SELECT manager_id FROM MANAGERS WHERE user_id = p_user_id
    );

    -- 4. Soft Delete and "Anonymize" the User
    -- We append the ID to the email to keep it unique but free up the original email
    UPDATE USERS
    SET is_active = 0,
        email = 'deleted_' || p_user_id || '_' || email,
        phone_number = NULL,
        password_hash = 'DEACTIVATED'
    WHERE user_id = p_user_id;

    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END;
/

