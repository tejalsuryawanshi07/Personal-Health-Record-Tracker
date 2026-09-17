package com.healthtracker.model;

public class Medication {
    private int id;
    private String medName;
    private String dosage;
    private String scheduledTime;
    private String status;
    private String logDate;

    public Medication(int id, String medName, String dosage, String scheduledTime, String status, String logDate) {
        this.id = id;
        this.medName = medName;
        this.dosage = dosage;
        this.scheduledTime = scheduledTime;
        this.status = status;
        this.logDate = logDate;
    }

    public Medication(String medName, String dosage, String scheduledTime) {
        this(0, medName, dosage, scheduledTime, "PENDING", null);
    }

    public int getId() { return id; }
    public String getMedName() { return medName; }
    public String getDosage() { return dosage; }
    public String getScheduledTime() { return scheduledTime; }
    public String getStatus() { return status; }
    public String getLogDate() { return logDate; }
}