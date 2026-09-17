package com.healthtracker.controller;

import com.healthtracker.config.DatabaseConfig;
import com.healthtracker.dao.HealthMetricDAO;
import com.healthtracker.dao.MedicationDAO;
import com.healthtracker.model.HealthMetric;
import com.healthtracker.model.Medication;
import com.healthtracker.service.ValidatorService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;

public class MetricApiHandler implements HttpHandler {
    private final HealthMetricDAO metricDAO = new HealthMetricDAO();
    private final MedicationDAO medDAO = new MedicationDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Map<String, String> params = parseFormData(exchange);

            try {
                if ("/add-metric".equals(path)) {
                    int steps = Integer.parseInt(params.getOrDefault("steps", "0"));
                    double water = Double.parseDouble(params.getOrDefault("water", "0"));
                    double sleep = Double.parseDouble(params.getOrDefault("sleep", "0"));
                    double weight = Double.parseDouble(params.getOrDefault("weight", "0"));

                    if (ValidatorService.validateMetrics(steps, water, sleep, weight)) {
                        metricDAO.insertMetric(new HealthMetric(steps, water, sleep, weight));
                    }
                } else if ("/add-med".equals(path)) {
                    String name = params.get("name");
                    String dosage = params.get("dosage");
                    String time = params.get("time");

                    if (ValidatorService.validateMedication(name, dosage, time)) {
                        medDAO.insertMedication(new Medication(name, dosage, time));
                    }
                } else if ("/update-med".equals(path)) {
                    int id = Integer.parseInt(params.get("id"));
                    String status = params.get("status");
                    medDAO.updateStatus(id, status);
                } else if ("/add-appointment".equals(path)) {
                    String doctor = params.get("doctor");
                    String specialty = params.get("specialty");
                    String date = params.get("date");
                    String time = params.get("time");

                    try (Connection conn = DatabaseConfig.getConnection();
                         PreparedStatement ps = conn.prepareStatement(
                                 "INSERT INTO appointments(doctor_name, specialty, appt_date, appt_time) VALUES(?, ?, ?, ?)")) {
                        ps.setString(1, doctor);
                        ps.setString(2, specialty);
                        ps.setString(3, date);
                        ps.setString(4, time);
                        ps.executeUpdate();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            exchange.getResponseHeaders().set("Location", "/");
            exchange.sendResponseHeaders(303, -1);
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }

    private Map<String, String> parseFormData(HttpExchange exchange) throws IOException {
        Map<String, String> map = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            String line = br.readLine();
            if (line != null) {
                for (String pair : line.split("&")) {
                    String[] kv = pair.split("=");
                    if (kv.length == 2) {
                        map.put(
                            URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                            URLDecoder.decode(kv[1], StandardCharsets.UTF_8)
                        );
                    }
                }
            }
        }
        return map;
    }
}