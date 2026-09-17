package com.healthtracker;

import com.healthtracker.config.DatabaseConfig;
import com.healthtracker.controller.DashboardHandler;
import com.healthtracker.controller.MetricApiHandler;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class Main {
    private static final int PORT = 8080;

    public static void main(String[] args) {
        try {
            DatabaseConfig.initializeDatabase();

            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

            server.createContext("/", new DashboardHandler());
            server.createContext("/add-metric", new MetricApiHandler());
            server.createContext("/add-med", new MetricApiHandler());
            server.createContext("/update-med", new MetricApiHandler());
            server.createContext("/add-appointment", new MetricApiHandler());

            server.setExecutor(null);
            System.out.println("=========================================");
            System.out.println(" Health Tracker Server Started!");
            System.out.println(" Modern Hub Live: http://localhost:" + PORT);
            System.out.println(" Press Ctrl+C in terminal to stop.");
            System.out.println("=========================================");
            server.start();
        } catch (Exception e) {
            System.err.println("Fatal: Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}