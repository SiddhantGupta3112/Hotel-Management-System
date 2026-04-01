-- =============================================================
-- Hotel Management System — New Tables (Phase 1)
-- Run this ONCE in Oracle SQL Plus after the existing schema
-- =============================================================

-- -------------------------------------------------------------
-- CUSTOMERS
-- -------------------------------------------------------------
CREATE TABLE CUSTOMERS (
    customer_id     NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id         NUMBER NOT NULL,
    address         VARCHAR2(255),
    id_proof        VARCHAR2(100),
    nationality     VARCHAR2(50),
    loyalty_points  NUMBER DEFAULT 0,
    CONSTRAINT fk_cust_user FOREIGN KEY (user_id) REFERENCES USERS(user_id)
);

-- -------------------------------------------------------------
-- DEPARTMENTS
-- -------------------------------------------------------------
CREATE TABLE DEPARTMENTS (
    department_id   NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    department_name VARCHAR2(100) NOT NULL,
    manager_id      NUMBER
);

-- -------------------------------------------------------------
-- EMPLOYEES
-- -------------------------------------------------------------
CREATE TABLE EMPLOYEES (
    employee_id     NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id         NUMBER NOT NULL,
    department_id   NUMBER,
    role            VARCHAR2(50),
    salary          NUMBER(10,2),
    hire_date       DATE DEFAULT SYSDATE,
    CONSTRAINT fk_emp_user FOREIGN KEY (user_id) REFERENCES USERS(user_id),
    CONSTRAINT fk_emp_dept FOREIGN KEY (department_id) REFERENCES DEPARTMENTS(department_id)
);

-- Add FK from DEPARTMENTS to EMPLOYEES (manager) after EMPLOYEES exists
ALTER TABLE DEPARTMENTS
    ADD CONSTRAINT fk_dept_manager FOREIGN KEY (manager_id) REFERENCES EMPLOYEES(employee_id);

-- -------------------------------------------------------------
-- ROOM_TYPES
-- -------------------------------------------------------------
CREATE TABLE ROOM_TYPES (
    room_type_id    NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    type_name       VARCHAR2(50)  NOT NULL,
    capacity        NUMBER        DEFAULT 1,
    price_per_night NUMBER(10,2)  NOT NULL,
    description     VARCHAR2(255)
);

-- -------------------------------------------------------------
-- ROOMS
-- -------------------------------------------------------------
CREATE TABLE ROOMS (
    room_id         NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    room_number     NUMBER        NOT NULL UNIQUE,
    room_type_id    NUMBER        NOT NULL,
    floor           NUMBER        DEFAULT 1,
    status          VARCHAR2(20)  DEFAULT 'AVAILABLE'
        CHECK (status IN ('AVAILABLE','OCCUPIED','MAINTENANCE','RESERVED')),
    CONSTRAINT fk_room_type FOREIGN KEY (room_type_id) REFERENCES ROOM_TYPES(room_type_id)
);

-- -------------------------------------------------------------
-- BOOKINGS
-- -------------------------------------------------------------
CREATE TABLE BOOKINGS (
    booking_id      NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id     NUMBER        NOT NULL,
    room_id         NUMBER        NOT NULL,
    booking_date    DATE          DEFAULT SYSDATE,
    check_in_date   DATE          NOT NULL,
    check_out_date  DATE          NOT NULL,
    booking_status  VARCHAR2(20)  DEFAULT 'CONFIRMED'
        CHECK (booking_status IN ('CONFIRMED','CHECKED_IN','CHECKED_OUT','CANCELLED')),
    CONSTRAINT fk_book_cust FOREIGN KEY (customer_id) REFERENCES CUSTOMERS(customer_id),
    CONSTRAINT fk_book_room FOREIGN KEY (room_id)     REFERENCES ROOMS(room_id)
);

-- -------------------------------------------------------------
-- PAYMENT_METHODS
-- -------------------------------------------------------------
CREATE TABLE PAYMENT_METHODS (
    method_id       NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    method_name     VARCHAR2(50)  NOT NULL UNIQUE
);

-- -------------------------------------------------------------
-- PAYMENTS
-- -------------------------------------------------------------
CREATE TABLE PAYMENTS (
    payment_id      NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    booking_id      NUMBER        NOT NULL,
    method_id       NUMBER        NOT NULL,
    amount          NUMBER(10,2)  NOT NULL,
    payment_date    DATE          DEFAULT SYSDATE,
    status          VARCHAR2(20)  DEFAULT 'COMPLETED'
        CHECK (status IN ('COMPLETED','PENDING','FAILED','REFUNDED')),
    CONSTRAINT fk_pay_booking FOREIGN KEY (booking_id) REFERENCES BOOKINGS(booking_id),
    CONSTRAINT fk_pay_method  FOREIGN KEY (method_id)  REFERENCES PAYMENT_METHODS(method_id)
);

-- -------------------------------------------------------------
-- INVOICES
-- -------------------------------------------------------------
CREATE TABLE INVOICES (
    invoice_id      NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    booking_id      NUMBER        NOT NULL UNIQUE,
    total_amount    NUMBER(10,2)  NOT NULL,
    tax             NUMBER(10,2)  DEFAULT 0,
    generated_date  DATE          DEFAULT SYSDATE,
    CONSTRAINT fk_inv_booking FOREIGN KEY (booking_id) REFERENCES BOOKINGS(booking_id)
);

-- -------------------------------------------------------------
-- SERVICES
-- -------------------------------------------------------------
CREATE TABLE SERVICES (
    service_id      NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    service_name    VARCHAR2(100) NOT NULL,
    price           NUMBER(10,2)  NOT NULL
);

-- -------------------------------------------------------------
-- SERVICE_USAGE
-- -------------------------------------------------------------
CREATE TABLE SERVICE_USAGE (
    usage_id        NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    booking_id      NUMBER        NOT NULL,
    service_id      NUMBER        NOT NULL,
    quantity        NUMBER        DEFAULT 1,
    total_price     NUMBER(10,2)  NOT NULL,
    CONSTRAINT fk_su_booking FOREIGN KEY (booking_id) REFERENCES BOOKINGS(booking_id),
    CONSTRAINT fk_su_service FOREIGN KEY (service_id) REFERENCES SERVICES(service_id)
);

-- -------------------------------------------------------------
-- ROOM_MAINTENANCE
-- -------------------------------------------------------------
CREATE TABLE ROOM_MAINTENANCE (
    maintenance_id  NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    room_id         NUMBER        NOT NULL,
    employee_id     NUMBER,
    description     VARCHAR2(255),
    maintenance_date DATE         DEFAULT SYSDATE,
    status          VARCHAR2(20)  DEFAULT 'PENDING'
        CHECK (status IN ('PENDING','IN_PROGRESS','COMPLETED')),
    CONSTRAINT fk_maint_room FOREIGN KEY (room_id)      REFERENCES ROOMS(room_id),
    CONSTRAINT fk_maint_emp  FOREIGN KEY (employee_id)  REFERENCES EMPLOYEES(employee_id)
);

-- =============================================================
-- SEED DATA
-- =============================================================

-- Payment methods
INSERT INTO PAYMENT_METHODS (method_name) VALUES ('Cash');
INSERT INTO PAYMENT_METHODS (method_name) VALUES ('Credit Card');
INSERT INTO PAYMENT_METHODS (method_name) VALUES ('Debit Card');
INSERT INTO PAYMENT_METHODS (method_name) VALUES ('UPI');
INSERT INTO PAYMENT_METHODS (method_name) VALUES ('Net Banking');

-- Room types
INSERT INTO ROOM_TYPES (type_name, capacity, price_per_night, description)
    VALUES ('Standard Single', 1, 1500.00, 'Cozy single room with essential amenities');
INSERT INTO ROOM_TYPES (type_name, capacity, price_per_night, description)
    VALUES ('Standard Double', 2, 2500.00, 'Comfortable double room with twin beds');
INSERT INTO ROOM_TYPES (type_name, capacity, price_per_night, description)
    VALUES ('Deluxe Double',   2, 4000.00, 'Spacious deluxe room with king-size bed');
INSERT INTO ROOM_TYPES (type_name, capacity, price_per_night, description)
    VALUES ('Suite',           4, 8000.00, 'Luxury suite with living area and premium view');
INSERT INTO ROOM_TYPES (type_name, capacity, price_per_night, description)
    VALUES ('Presidential',    6, 15000.00,'Top-floor presidential suite with all luxuries');

-- Rooms (10 sample rooms across 3 floors)
INSERT INTO ROOMS (room_number, room_type_id, floor, status)
    VALUES (101, 1, 1, 'AVAILABLE');
INSERT INTO ROOMS (room_number, room_type_id, floor, status)
    VALUES (102, 1, 1, 'AVAILABLE');
INSERT INTO ROOMS (room_number, room_type_id, floor, status)
    VALUES (103, 2, 1, 'AVAILABLE');
INSERT INTO ROOMS (room_number, room_type_id, floor, status)
    VALUES (104, 2, 1, 'OCCUPIED');
INSERT INTO ROOMS (room_number, room_type_id, floor, status)
    VALUES (201, 3, 2, 'AVAILABLE');
INSERT INTO ROOMS (room_number, room_type_id, floor, status)
    VALUES (202, 3, 2, 'MAINTENANCE');
INSERT INTO ROOMS (room_number, room_type_id, floor, status)
    VALUES (203, 2, 2, 'AVAILABLE');
INSERT INTO ROOMS (room_number, room_type_id, floor, status)
    VALUES (301, 4, 3, 'AVAILABLE');
INSERT INTO ROOMS (room_number, room_type_id, floor, status)
    VALUES (302, 4, 3, 'OCCUPIED');
INSERT INTO ROOMS (room_number, room_type_id, floor, status)
    VALUES (401, 5, 4, 'AVAILABLE');

-- Services
INSERT INTO SERVICES (service_name, price) VALUES ('Room Service',      200.00);
INSERT INTO SERVICES (service_name, price) VALUES ('Laundry',           150.00);
INSERT INTO SERVICES (service_name, price) VALUES ('Airport Pickup',    800.00);
INSERT INTO SERVICES (service_name, price) VALUES ('Spa & Wellness',   1200.00);
INSERT INTO SERVICES (service_name, price) VALUES ('Extra Bed',         500.00);
INSERT INTO SERVICES (service_name, price) VALUES ('Breakfast',         350.00);
INSERT INTO SERVICES (service_name, price) VALUES ('Late Checkout',     400.00);

-- Department
INSERT INTO DEPARTMENTS (department_name) VALUES ('Front Desk');
INSERT INTO DEPARTMENTS (department_name) VALUES ('Housekeeping');
INSERT INTO DEPARTMENTS (department_name) VALUES ('Food & Beverage');

COMMIT;
