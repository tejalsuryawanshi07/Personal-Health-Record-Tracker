package com.healthtracker.dao;

import com.healthtracker.config.DatabaseConfig;
import com.healthtracker.model.HealthMetric;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HealthMetricDAO {

    public void insertMetric(HealthMetric m) throws Exception {
        String query = "INSERT INTO metrics(steps, water, sleep, weight) VALUES(?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, m.getSteps());
            ps.setDouble(2, m.getWater());
            ps.setDouble(3, m.getSleep());
            ps.setDouble(4, m.getWeight());
            ps.executeUpdate();
        }
    }

    public List<HealthMetric> getAllMetrics() throws Exception {
        List<HealthMetric> list = new ArrayList<>();
        String query = "SELECT * FROM metrics ORDER BY id DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                list.add(new HealthMetric(
                        rs.getInt("id"),
                        rs.getString("log_date"),
                        rs.getInt("steps"),
                        rs.getDouble("water"),
                        rs.getDouble("sleep"),
                        rs.getDouble("weight")
                ));
            }
        }
        return list;
    }
}