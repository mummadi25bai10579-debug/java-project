package com.library;

import com.library.ui.MenuHandler;
import com.library.util.DBConnection;

import java.sql.SQLException;

/**
 * Main application entry point for the Smart Library Management System.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   Initializing Smart Library Management System   ");
        System.out.println("==================================================");

        try {
            DBConnection.initializeDatabase();
            System.out.println("Database connection established and schema initialized.");
            System.out.println();
        } catch (SQLException e) {
            System.err.println("FATAL: Failed to initialize SQLite database: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        MenuHandler menuHandler = new MenuHandler();
        menuHandler.start();
    }
}
