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