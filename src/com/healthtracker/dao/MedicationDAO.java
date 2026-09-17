package com.healthtracker.dao;

import com.healthtracker.config.DatabaseConfig;
import com.healthtracker.model.Medication;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicationDAO {

    public void insertMedication(Medication med) throws Exception {
        String query = "INSERT INTO medications(med_name, dosage, scheduled_time, status) VALUES(?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, med.getMedName());
            ps.setString(2, med.getDosage());
            ps.setString(3, med.getScheduledTime());
            ps.setString(4, med.getStatus());
            ps.executeUpdate();
        }
    }

    public List<Medication> getAllMedications() throws Exception {
        List<Medication> list = new ArrayList<>();
        String query = "SELECT * FROM medications ORDER BY id DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                list.add(new Medication(
                        rs.getInt("id"),
                        rs.getString("med_name"),
                        rs.getString("dosage"),
                        rs.getString("scheduled_time"),
                        rs.getString("status"),
                        rs.getString("log_date")
                ));
            }
        }
        return list;
    }

    public void updateStatus(int id, String status) throws Exception {
        String query = "UPDATE medications SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }
}