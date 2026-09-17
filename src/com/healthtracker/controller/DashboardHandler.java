package com.healthtracker.controller;

import com.healthtracker.config.DatabaseConfig;
import com.healthtracker.dao.HealthMetricDAO;
import com.healthtracker.dao.MedicationDAO;
import com.healthtracker.model.HealthMetric;
import com.healthtracker.model.Medication;
import com.healthtracker.service.AnalyticsService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DashboardHandler implements HttpHandler {
    private final HealthMetricDAO metricDAO = new HealthMetricDAO();
    private final MedicationDAO medDAO = new MedicationDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            List<HealthMetric> metrics = metricDAO.getAllMetrics();
            List<Medication> meds = medDAO.getAllMedications();

            HealthMetric latest = metrics.isEmpty() ? null : metrics.get(0);
            String tip = AnalyticsService.getHealthTip(latest);
            double adherence = AnalyticsService.calculateAdherenceRate(meds);

            int currentSteps = latest != null ? latest.getSteps() : 0;
            double currentWater = latest != null ? latest.getWater() : 0.0;
            double currentSleep = latest != null ? latest.getSleep() : 0.0;
            double currentWeight = latest != null ? latest.getWeight() : 0.0;

            // Chart data preparation
            List<HealthMetric> chartMetrics = new ArrayList<>(metrics);
            Collections.reverse(chartMetrics);
            StringBuilder labelsJson = new StringBuilder("[");
            StringBuilder stepsJson = new StringBuilder("[");
            StringBuilder waterJson = new StringBuilder("[");
            StringBuilder sleepJson = new StringBuilder("[");

            for (int i = 0; i < chartMetrics.size(); i++) {
                HealthMetric m = chartMetrics.get(i);
                labelsJson.append("'").append(m.getLogDate()).append("'");
                stepsJson.append(m.getSteps());
                waterJson.append(m.getWater());
                sleepJson.append(m.getSleep());
                if (i < chartMetrics.size() - 1) {
                    labelsJson.append(",");
                    stepsJson.append(",");
                    waterJson.append(",");
                    sleepJson.append(",");
                }
            }
            labelsJson.append("]");
            stepsJson.append("]");
            waterJson.append("]");
            sleepJson.append("]");

            // Medication rows
            StringBuilder medRows = new StringBuilder();
            for (Medication med : meds) {
                String badgeStyle = "PENDING".equalsIgnoreCase(med.getStatus())
                        ? "background: rgba(245, 158, 11, 0.15); color: #f59e0b; border: 1px solid #f59e0b;"
                        : "TAKEN".equalsIgnoreCase(med.getStatus())
                        ? "background: rgba(16, 185, 129, 0.15); color: #10b981; border: 1px solid #10b981;"
                        : "background: rgba(239, 68, 68, 0.15); color: #ef4444; border: 1px solid #ef4444;";

                medRows.append("<tr>")
                    .append("<td style='font-weight:600;'>").append(med.getMedName()).append("</td>")
                    .append("<td>").append(med.getDosage()).append("</td>")
                    .append("<td>").append(med.getScheduledTime()).append("</td>")
                    .append("<td><span style='padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight:700; ").append(badgeStyle).append("'>")
                    .append(med.getStatus()).append("</span></td>")
                    .append("<td>")
                    .append("<form method='POST' action='/update-med' style='display:inline; margin:0;'>")
                    .append("<input type='hidden' name='id' value='").append(med.getId()).append("'>")
                    .append("<button type='submit' name='status' value='TAKEN' class='btn btn-success' style='margin-right:5px;'>✔ Taken</button>")
                    .append("<button type='submit' name='status' value='MISSED' class='btn btn-danger'>✖ Missed</button>")
                    .append("</form>")
                    .append("</td>")
                    .append("</tr>");
            }

            // Appointments rows
            StringBuilder apptRows = new StringBuilder();
            try (Connection conn = DatabaseConfig.getConnection();
                 Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT * FROM appointments ORDER BY id DESC")) {
                while (rs.next()) {
                    apptRows.append("<tr>")
                        .append("<td><strong>").append(rs.getString("doctor_name")).append("</strong></td>")
                        .append("<td>").append(rs.getString("specialty")).append("</td>")
                        .append("<td>").append(rs.getString("appt_date")).append("</td>")
                        .append("<td>").append(rs.getString("appt_time")).append("</td>")
                        .append("<td><span style='padding:4px 8px; border-radius:12px; background:rgba(56,189,248,0.15); color:#38bdf8; font-size:12px; font-weight:600;'>")
                        .append(rs.getString("status")).append("</span></td>")
                        .append("</tr>");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            String html = "<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "<title>PulseCare | Personal Health & Vitals Hub</title>"
                + "<script src='https://cdn.jsdelivr.net/npm/chart.js'></script>"
                + "<link href='https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap' rel='stylesheet'>"
                + "<style>"
                + "* { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Plus Jakarta Sans', sans-serif; }"
                + "body { background: #090d16; color: #e2e8f0; display: flex; min-height: 100vh; }"
                + ".sidebar { width: 260px; background: #0f172a; border-right: 1px solid #1e293b; padding: 25px 20px; display: flex; flex-direction: column; position: fixed; height: 100vh; }"
                + ".logo { font-size: 20px; font-weight: 800; color: #38bdf8; margin-bottom: 35px; display: flex; align-items: center; gap: 10px; }"
                + ".nav-links { list-style: none; display: flex; flex-direction: column; gap: 8px; flex: 1; }"
                + ".nav-links a { text-decoration: none; color: #94a3b8; padding: 12px 16px; border-radius: 10px; font-weight: 600; font-size: 14px; display: flex; align-items: center; gap: 12px; transition: all 0.2s ease; }"
                + ".nav-links a:hover, .nav-links a.active { background: #1e293b; color: #38bdf8; transform: translateX(4px); }"
                + ".main-content { margin-left: 260px; flex: 1; padding: 35px 40px; overflow-y: auto; }"
                + ".hero-banner { background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%); border: 1px solid #334155; border-radius: 18px; padding: 25px 30px; margin-bottom: 30px; display: flex; justify-content: space-between; align-items: center; }"
                + ".quote-box { max-width: 650px; }"
                + ".quote-box h2 { font-size: 24px; font-weight: 700; color: #f8fafc; margin-bottom: 6px; }"
                + ".quote-box p { color: #94a3b8; font-size: 14px; line-height: 1.5; }"
                + ".tip-pill { background: rgba(56, 189, 248, 0.1); border: 1px solid rgba(56, 189, 248, 0.25); color: #38bdf8; padding: 6px 14px; border-radius: 20px; font-size: 12px; font-weight: 700; display: inline-block; margin-top: 10px; }"
                + ".kpi-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(210px, 1fr)); gap: 18px; margin-bottom: 30px; }"
                + ".kpi-card { background: #131d31; border: 1px solid #1e293b; border-radius: 16px; padding: 20px; position: relative; overflow: hidden; transition: transform 0.2s ease, border-color 0.2s ease; }"
                + ".kpi-card:hover { transform: translateY(-4px); border-color: #38bdf8; }"
                + ".kpi-title { font-size: 13px; color: #94a3b8; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; }"
                + ".kpi-val { font-size: 28px; font-weight: 800; color: #f8fafc; margin: 10px 0 4px 0; }"
                + ".kpi-sub { font-size: 12px; color: #10b981; font-weight: 600; }"
                + ".grid-2 { display: grid; grid-template-columns: 2fr 1.2fr; gap: 24px; margin-bottom: 30px; }"
                + ".panel { background: #131d31; border: 1px solid #1e293b; border-radius: 16px; padding: 24px; }"
                + ".panel-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 18px; }"
                + ".panel-header h3 { font-size: 17px; font-weight: 700; color: #f1f5f9; }"
                + "form { display: grid; gap: 12px; }"
                + "input, select { background: #090d16; border: 1px solid #26334d; border-radius: 10px; padding: 11px 14px; color: #f8fafc; font-size: 14px; outline: none; transition: border-color 0.2s; width: 100%; }"
                + "input:focus { border-color: #38bdf8; }"
                + ".btn { border: none; border-radius: 10px; padding: 10px 16px; font-weight: 700; font-size: 13px; cursor: pointer; transition: all 0.2s ease; display: inline-flex; align-items: center; justify-content: center; text-decoration: none; }"
                + ".btn-primary { background: #38bdf8; color: #090d16; }"
                + ".btn-primary:hover { background: #0284c7; }"
                + ".btn-success { background: rgba(16, 185, 129, 0.2); color: #10b981; border: 1px solid #10b981; }"
                + ".btn-success:hover { background: #10b981; color: #fff; }"
                + ".btn-danger { background: rgba(239, 68, 68, 0.2); color: #ef4444; border: 1px solid #ef4444; }"
                + ".btn-danger:hover { background: #ef4444; color: #fff; }"
                + "table { width: 100%; border-collapse: collapse; }"
                + "th, td { padding: 12px 14px; text-align: left; font-size: 13px; border-bottom: 1px solid #1e293b; }"
                + "th { color: #64748b; font-weight: 700; text-transform: uppercase; font-size: 11px; }"
                + "</style></head><body>"

                // Sidebar
                + "<div class='sidebar'>"
                + "<div class='logo'>❤️ PulseCare</div>"
                + "<ul class='nav-links'>"
                + "<li><a href='#dashboard' class='active'>📊 Overview</a></li>"
                + "<li><a href='#analytics'>📈 Health Charts</a></li>"
                + "<li><a href='#medications'>💊 Medications</a></li>"
                + "<li><a href='#appointments'>📅 Appointments</a></li>"
                + "<li><a href='#vitals-form'>➕ Log Vitals</a></li>"
                + "</ul>"
                + "<div style='padding: 15px; background: #131d31; border-radius: 12px; border:1px solid #1e293b;'>"
                + "<div style='font-size:11px; color:#64748b;'>MEDICATION ADHERENCE</div>"
                + "<div style='font-size:22px; font-weight:800; color:#10b981; margin: 4px 0;'>" + String.format("%.1f", adherence) + "%</div>"
                + "<div style='font-size:12px; color:#94a3b8;'>Daily compliance score</div>"
                + "</div>"
                + "</div>"

                // Main Container
                + "<div class='main-content' id='dashboard'>"
                + "<div class='hero-banner'>"
                + "<div class='quote-box'>"
                + "<h2>Welcome Back to Your Health Dashboard</h2>"
                + "<p>“Take care of your body. It's the only place you have to live.” — Jim Rohn</p>"
                + "<div class='tip-pill'>💡 Insight: " + tip + "</div>"
                + "</div>"
                + "<a href='#vitals-form' class='btn btn-primary'>+ Quick Log</a>"
                + "</div>"

                // KPI Grid
                + "<div class='kpi-grid'>"
                + "<div class='kpi-card'><div class='kpi-title'>Daily Steps</div><div class='kpi-val'>" + currentSteps + "</div><div class='kpi-sub'>Target: 8,000 / day</div></div>"
                + "<div class='kpi-card'><div class='kpi-title'>Hydration</div><div class='kpi-val'>" + currentWater + " <span style='font-size:16px;'>L</span></div><div class='kpi-sub'>Target: 2.5 - 3.0 L</div></div>"
                + "<div class='kpi-card'><div class='kpi-title'>Sleep Quality</div><div class='kpi-val'>" + currentSleep + " <span style='font-size:16px;'>hrs</span></div><div class='kpi-sub'>Recommended: 7-8 hrs</div></div>"
                + "<div class='kpi-card'><div class='kpi-title'>Weight</div><div class='kpi-val'>" + currentWeight + " <span style='font-size:16px;'>kg</span></div><div class='kpi-sub'>Persistent Record</div></div>"
                + "</div>"

                // Charts Section
                + "<div class='grid-2' id='analytics'>"
                + "<div class='panel'>"
                + "<div class='panel-header'><h3>Activity & Vitals History</h3></div>"
                + "<canvas id='vitalsChart' height='140'></canvas>"
                + "</div>"
                + "<div class='panel' id='vitals-form'>"
                + "<div class='panel-header'><h3>Log Today's Vitals</h3></div>"
                + "<form action='/add-metric' method='POST'>"
                + "<input type='number' name='steps' placeholder='Steps Count' required>"
                + "<input type='number' step='0.1' name='water' placeholder='Water Intake (Liters)' required>"
                + "<input type='number' step='0.1' name='sleep' placeholder='Sleep (Hours)' required>"
                + "<input type='number' step='0.1' name='weight' placeholder='Weight (kg)' required>"
                + "<button type='submit' class='btn btn-primary'>Save Vitals Log</button>"
                + "</form>"
                + "</div>"
                + "</div>"

                // Medications Section
                + "<div class='panel' id='medications' style='margin-bottom: 24px;'>"
                + "<div class='panel-header'>"
                + "<h3>Medication Adherence & Schedule</h3>"
                + "<span style='font-size:13px; color:#94a3b8;'>Adherence Rate: <strong style='color:#38bdf8;'>" + String.format("%.1f", adherence) + "%</strong></span>"
                + "</div>"
                + "<div style='display:grid; grid-template-columns: 2fr 1fr; gap:24px;'>"
                + "<div>"
                + "<table><thead><tr><th>Medicine</th><th>Dosage</th><th>Time</th><th>Status</th><th>Actions</th></tr></thead>"
                + "<tbody>" + (medRows.length() > 0 ? medRows.toString() : "<tr><td colspan='5' style='text-align:center; color:#64748b;'>No medications scheduled yet.</td></tr>") + "</tbody></table>"
                + "</div>"
                + "<form action='/add-med' method='POST'>"
                + "<h4 style='font-size:14px; margin-bottom:4px;'>Schedule New Medicine</h4>"
                + "<input type='text' name='name' placeholder='Medicine Name (e.g. Paracetamol)' required>"
                + "<input type='text' name='dosage' placeholder='Dosage (e.g. 500mg, 1 tab)' required>"
                + "<input type='text' name='time' placeholder='Time (e.g. 08:30 AM)' required>"
                + "<button type='submit' class='btn btn-primary'>Add Prescription</button>"
                + "</form>"
                + "</div>"
                + "</div>"

                // Appointments Section
                + "<div class='panel' id='appointments'>"
                + "<div class='panel-header'><h3>Upcoming Doctor Appointments</h3></div>"
                + "<div style='display:grid; grid-template-columns: 2fr 1fr; gap:24px;'>"
                + "<div>"
                + "<table><thead><tr><th>Doctor</th><th>Specialty</th><th>Date</th><th>Time</th><th>Status</th></tr></thead>"
                + "<tbody>" + (apptRows.length() > 0 ? apptRows.toString() : "<tr><td colspan='5' style='text-align:center; color:#64748b;'>No scheduled consultations.</td></tr>") + "</tbody></table>"
                + "</div>"
                + "<form action='/add-appointment' method='POST'>"
                + "<h4 style='font-size:14px; margin-bottom:4px;'>Book / Schedule Doctor</h4>"
                + "<input type='text' name='doctor' placeholder='Dr. Name' required>"
                + "<input type='text' name='specialty' placeholder='Specialty (Cardiology, General)' required>"
                + "<input type='date' name='date' required>"
                + "<input type='text' name='time' placeholder='Time (e.g. 10:30 AM)' required>"
                + "<button type='submit' class='btn btn-primary'>Confirm Appointment</button>"
                + "</form>"
                + "</div>"
                + "</div>"

                // Chart.js Script
                + "<script>"
                + "const ctx = document.getElementById('vitalsChart').getContext('2d');"
                + "new Chart(ctx, {"
                + "  type: 'line',"
                + "  data: {"
                + "    labels: " + labelsJson.toString() + ","
                + "    datasets: [{"
                + "      label: 'Steps',"
                + "      data: " + stepsJson.toString() + ","
                + "      borderColor: '#38bdf8',"
                + "      backgroundColor: 'rgba(56, 189, 248, 0.1)',"
                + "      tension: 0.3,"
                + "      fill: true"
                + "    }, {"
                + "      label: 'Water (L x1000)',"
                + "      data: " + waterJson.toString() + ".map(v => v * 1000),"
                + "      borderColor: '#10b981',"
                + "      tension: 0.3"
                + "    }]"
                + "  },"
                + "  options: {"
                + "    responsive: true,"
                + "    plugins: { legend: { labels: { color: '#94a3b8' } } },"
                + "    scales: {"
                + "      x: { ticks: { color: '#64748b' }, grid: { color: '#1e293b' } },"
                + "      y: { ticks: { color: '#64748b' }, grid: { color: '#1e293b' } }"
                + "    }"
                + "  }"
                + "});"
                + "</script>"

                + "</div></body></html>";

            byte[] response = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(500, -1);
        }
    }
}