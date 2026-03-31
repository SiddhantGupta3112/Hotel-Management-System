INSERT INTO ROLES (role_name) VALUES ('ROLE_ADMIN');
INSERT INTO ROLES (role_name) VALUES ('ROLE_MANAGER');
INSERT INTO ROLES (role_name) VALUES ('ROLE_STAFF');
INSERT INTO ROLES (role_name) VALUES ('ROLE_CUSTOMER');


INSERT INTO USERS (email, password_hash, name, phone_country_code, phone_number)
VALUES ('admin@hotel.com', '$2a$12$SE.HqXOe6gpFtIbf5v9gRerX8wO1mgc3h4fguk8y87FKSrURRQ9iC', 'SYSTEM ADMIN', '+91', '1234567890');

INSERT INTO USER_ROLES (user_id, role_id)
VALUES (
    (SELECT user_id FROM USERS WHERE email = 'admin@hotel.com'),
    (SELECT role_id FROM ROLES WHERE role_name = 'ROLE_ADMIN')
);

COMMIT;