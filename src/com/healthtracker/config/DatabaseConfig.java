package com.healthtracker.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseConfig {
    private static final String DB_URL = "jdbc:sqlite:health_tracker.db";

    public static Connection getConnection() throws Exception {
        Class.forName("org.sqlite.JDBC");
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        String metricsTable = "CREATE TABLE IF NOT EXISTS metrics ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "log_date TEXT DEFAULT CURRENT_DATE, "
                + "steps INTEGER NOT NULL CHECK (steps >= 0), "
                + "water REAL NOT NULL CHECK (water >= 0.0), "
                + "sleep REAL NOT NULL CHECK (sleep >= 0.0), "
                + "weight REAL NOT NULL CHECK (weight > 0.0), "
                + "created_at TEXT DEFAULT CURRENT_TIMESTAMP)";

        String medicationsTable = "CREATE TABLE IF NOT EXISTS medications ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "med_name TEXT NOT NULL, "
                + "dosage TEXT NOT NULL, "
                + "scheduled_time TEXT NOT NULL, "
                + "status TEXT DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'TAKEN', 'MISSED')), "
                + "log_date TEXT DEFAULT CURRENT_DATE)";

        String appointmentsTable = "CREATE TABLE IF NOT EXISTS appointments ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "doctor_name TEXT NOT NULL, "
                + "specialty TEXT NOT NULL, "
                + "appt_date TEXT NOT NULL, "
                + "appt_time TEXT NOT NULL, "
                + "status TEXT DEFAULT 'CONFIRMED')";

        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            st.execute(metricsTable);
            st.execute(medicationsTable);
            st.execute(appointmentsTable);
        } catch (Exception e) {
            System.err.println("Database initialization error: " + e.getMessage());
        }
    }
}