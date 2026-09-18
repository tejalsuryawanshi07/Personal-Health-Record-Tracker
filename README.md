# Personal Health Record & Medication Tracker

## Overview
A comprehensive command-line Java application designed to log daily health metrics, track prescriptions and medical history, analyze health trends, and provide rule-based personalized health insights with persistent database storage via JDBC[cite: 1, 2].

## Key Features
- **Health Data Tracking:** Input and track steps, water intake, sleep, and weight with daily dashboard summaries.
- **Progress & Analytics:** Calculate weekly and monthly health trends with progress summaries.
- **Personalized Health Insights:** Rule-based engine providing actionable recommendations based on daily metrics.
- **Medication & Prescription Management:** CRUD operations for prescriptions and dosage validation.
- **Automated Reminders:** Background multithreaded alert service for scheduled doses.

## Technologies Used
- **Language:** Java (JDK 8+)
- **Database:** SQLite / MySQL via JDBC[cite: 2]
- **Concurrency:** Java Multithreading (`Runnable`)[cite: 2]
- **Testing:** JUnit & CLI verification[cite: 2]

## Installation & Setup
1. Clone the repository:
   ```bash
   git clone [https://github.com/tejalsuryawanshi07/Personal-Health-Record-Tracker.git](https://github.com/tejalsuryawanshi07/Personal-Health-Record-Tracker.git)
