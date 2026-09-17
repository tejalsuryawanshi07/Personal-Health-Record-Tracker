package com.healthtracker.service;

public class ValidatorService {

    public static boolean validateMetrics(int steps, double water, double sleep, double weight) {
        return steps >= 0 && water >= 0.0 && sleep >= 0.0 && weight > 0.0;
    }

    public static boolean validateMedication(String name, String dosage, String time) {
        return name != null && !name.trim().isEmpty() 
            && dosage != null && !dosage.trim().isEmpty() 
            && time != null && !time.trim().isEmpty();
    }
}
