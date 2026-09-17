package com.healthtracker.model;

public class HealthMetric {
    private int id;
    private String logDate;
    private int steps;
    private double water;
    private double sleep;
    private double weight;

    public HealthMetric(int id, String logDate, int steps, double water, double sleep, double weight) {
        this.id = id;
        this.logDate = logDate;
        this.steps = steps;
        this.water = water;
        this.sleep = sleep;
        this.weight = weight;
    }

    public HealthMetric(int steps, double water, double sleep, double weight) {
        this(0, null, steps, water, sleep, weight);
    }

    public int getId() { return id; }
    public String getLogDate() { return logDate; }
    public int getSteps() { return steps; }
    public double getWater() { return water; }
    public double getSleep() { return sleep; }
    public double getWeight() { return weight; }
}