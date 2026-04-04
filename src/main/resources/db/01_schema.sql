CREATE TABLE USERS (
    user_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email VARCHAR2(100) UNIQUE NOT NULL,
    password_hash VARCHAR2(255) NOT NULL,
    name VARCHAR2(100) NOT NULL,
    phone_country_code VARCHAR2(5),
    phone_number VARCHAR2(15),
    is_active NUMBER(1) DEFAULT 1 NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE USERS MODIFY (email NULL);

CREATE TABLE ROLES(
    role_id   NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    role_name VARCHAR2(50) NOT NULL UNIQUE
);

CREATE TABLE USER_ROLES (
    user_id NUMBER NOT NULL,
    role_id NUMBER NOT NULL,
    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES USERS(user_id),
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES ROLES(role_id)
);

CREATE TABLE CUSTOMERS (
    customer_id    NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id        NUMBER UNIQUE NOT NULL,
    address        VARCHAR2(255) DEFAULT '',
    id_proof       VARCHAR2(100) DEFAULT '',
    nationality    VARCHAR2(50)  DEFAULT '',
    loyalty_points NUMBER        DEFAULT 0,
    CONSTRAINT fk_cust_user FOREIGN KEY (user_id) REFERENCES USERS(user_id)
);

-- 1. DEPARTMENTS
CREATE TABLE DEPARTMENTS (
    department_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    department_name VARCHAR2(100) NOT NULL UNIQUE,
    head_manager_id NUMBER -- This will be set after creating Managers
);

-- 2. MANAGERS
CREATE TABLE MANAGERS (
    manager_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id NUMBER NOT NULL UNIQUE,
    department_id NUMBER NOT NULL,
    reports_to_manager_id NUMBER,
    job_description VARCHAR2(100),
    salary NUMBER(10, 2),
    CONSTRAINT fk_mgr_user FOREIGN KEY (user_id) REFERENCES USERS(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_mgr_dept FOREIGN KEY (department_id) REFERENCES DEPARTMENTS(department_id),
    CONSTRAINT fk_mgr_mgr FOREIGN KEY (reports_to_manager_id) REFERENCES MANAGERS(manager_id)
);

-- 3. STAFF
CREATE TABLE STAFF (
    staff_id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id NUMBER NOT NULL UNIQUE,
    department_id NUMBER NOT NULL,
    manager_id NUMBER NOT NULL,
    job_description VARCHAR2(100),
    salary NUMBER(10, 2),
    CONSTRAINT fk_staff_user FOREIGN KEY (user_id) REFERENCES USERS(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_staff_dept FOREIGN KEY (department_id) REFERENCES DEPARTMENTS(department_id),
    CONSTRAINT fk_staff_mgr FOREIGN KEY (manager_id) REFERENCES MANAGERS(manager_id)
);

-- After tables are created, update Department to link the Head Manager
ALTER TABLE DEPARTMENTS ADD CONSTRAINT fk_dept_head
FOREIGN KEY (head_manager_id) REFERENCES MANAGERS(manager_id);
