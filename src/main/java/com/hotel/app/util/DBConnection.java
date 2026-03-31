package com.hotel.app.util;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final Dotenv dotenv = Dotenv.configure().load();

    private static final String DB_URL = dotenv.get("DATABASE_URL");
    private static final String DB_USER = dotenv.get("DATABASE_USER");
    private static final String DB_PASS = dotenv.get("DATABASE_PASSWORD");

    static {
        if (DB_URL == null || DB_URL.trim().isEmpty()) {
            throw new IllegalStateException("Database URL is not configured in .env");
        }
        if (DB_USER == null || DB_USER.trim().isEmpty()) {
            throw new IllegalStateException("Database Username is not configured in .env");
        }
        if (DB_PASS == null || DB_PASS.trim().isEmpty()) {
            throw new IllegalStateException("Database Password is not configured in .env");
        }
    }

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }
}