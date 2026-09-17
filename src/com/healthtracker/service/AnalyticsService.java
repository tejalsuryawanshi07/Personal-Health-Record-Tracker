package com.healthtracker.service;

import com.healthtracker.model.HealthMetric;
import com.healthtracker.model.Medication;
import java.util.List;

public class AnalyticsService {

    public static double calculateAdherenceRate(List<Medication> meds) {
        if (meds == null || meds.isEmpty()) return 0.0;
        int takenCount = 0;
        for (Medication m : meds) {
            if ("TAKEN".equalsIgnoreCase(m.getStatus())) {
                takenCount++;
            }
        }
        return ((double) takenCount / meds.size()) * 100.0;
    }

    public static String getHealthTip(HealthMetric metric) {
        if (metric == null) return "Start logging your daily vitals to receive personalized health tips!";
        if (metric.getSteps() < 5000) return "Activity Alert: Daily steps are below 5,000. Try taking a 20-minute walk.";
        if (metric.getWater() < 2.0) return "Hydration Alert: Water intake is under 2 Liters. Keep a water bottle nearby.";
        if (metric.getSleep() < 6.0) return "Rest Alert: Sleep duration is under 6 hours. Prioritize a consistent sleep cycle.";
        return "Great job! All your tracked vitals are within target ranges today.";
    }
}
