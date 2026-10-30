# BioShot Product & Technical Roadmap: Biologics & GLP-1 Integration

This roadmap outlines the strategic feature set and technical architecture to evolve **BioShot** into the premier dual-purpose tracking platform for **Biologics** (autoimmune, inflammatory, and chronic conditions like RA, IBD, Psoriasis) and **GLP-1/GIP Peptides** (metabolic health and weight management).

---

## 1. Vision & Architecture Philosophy

BioShot's existing engine already contains a robust foundational core:
- **Pharmacokinetic (PK) Engine (`PkCalculator.kt`)**: 1-compartment first-order absorption and elimination modeling.
- **Injection Log & Body Map (`InjectionLogBodyMap.kt`)**: Interactive anatomical site rotation tracking.
- **Schedule & Notification Engine (`ScheduleEngine.kt`)**: Reminder and notification workers.
- **Doctor PDF Export (`PdfReportExporter.kt`)**: Clinical data export for healthcare providers.
- **Database Backup (`DatabaseBackupManager.kt`)**: Manual JSON database export and migration safety nets.

By generalizing and expanding these systems, BioShot will seamlessly support both long-half-life biologics (~10–20+ days) and short-half-life GLP-1 peptides (~5–7 days).

---

## 2. Phased Implementation Plan

### Phase 1: Dual-Category Preset Engine, Loading Doses & Inventory Tracking [COMPLETED]
- Categorized drug presets (Biologics & GLP-1s).
- Loading dose schedules & inventory/lot tracking.

### Phase 2: Advanced Health Metrics & Symptom Correlation with PK Trough Levels (`Cmin`) [COMPLETED]
- Weight, body fat, waist measurements (`WeightLogEntity`).
- Biologic health tracker dashboard card.

### Phase 3: Jetpack Compose UI/UX Modernization & Dashboard Hero Card [IN PROGRESS]
- Immersive dashboard countdown & site rotation warnings.

### Phase 4: Home Screen Widgets (Jetpack Glance) [PLANNED]
- Glance AppWidgets for active levels and quick shot logging.

### Phase 5: Enhanced Clinical Export & Reporting [COMPLETED]
- PDF clinical export & manual JSON database backup sharing.

### Phase 6: Data Portability & Privacy Security (JSON Restore & Biometric App Lock) [NEW]
- **JSON Restore / Import**: Parse exported JSON database backups to import clinical history onto new devices or after reinstalls.
- **Biometric App Lock**: Integrate Jetpack Biometric (`BiometricPrompt`) to secure health logs with fingerprint or face unlock.

### Phase 7: Advanced PK Chart Interactivity & Low-Stock Refill Alerts [NEW]
- **PK Chart Therapeutic Windows**: Visually shade peak ($C_{max}$) and minimum effective trough ($C_{min}$) levels on `PkChartScreen`.
- **Low-Stock Refill Reminders**: Automated notification triggers when pen/vial inventory drops below 2 doses.

### Phase 8: Wear OS Companion App & Tile Integration [FUTURE]
- **Wear OS Complications**: Next-dose countdown timer and quick-log complications for smartwatches.
- **Watch Sync**: Local synchronization of injection logs and reminders via the Wearable Data Layer API.

### Phase 9: On-Device AI Symptom & PK Insights (ML Kit / Gemini Nano) [FUTURE]
- **Gemini Nano Prompt API**: Local on-device AI analysis correlating symptom flare diaries with pharmacokinetic blood concentration peaks and troughs.
- **Privacy-First Summaries**: Generate automated wellness trend summaries for rheumatologist/gastroenterologist visits entirely on-device without cloud data transmission.

### Phase 10: Multi-Medication Stack Manager & Drug Interaction Advisory [FUTURE]
- **Concurrent Regimens**: Manage multiple biologic therapies, biosimilars, or supportive medications simultaneously.
- **Overlap & Timing Guidance**: Smart scheduling checks to prevent dose conflicts and advise on spacing.

---

## 3. Technical Architecture Summary

| Component | Current Implementation | Proposed Enhancement |
| :--- | :--- | :--- |
| **Data Layer (Room)** | `Entities.kt`, `Daos.kt`, `DatabaseBackupManager.kt` | Add JSON parsing/import engine for database restore. |
| **PK Engine** | `PkCalculator.kt` | Add therapeutic window shading helpers and $C_{max}/C_{min}$ markers. |
| **UI Layer** | Jetpack Compose (`PkChartScreen.kt`, `DoctorReportSection.kt`) | Add Biometric authentication wrapper and interactive chart time horizons. |
| **Security & Auth** | Standard App | Jetpack Biometric prompt integration. |
| **Device Form Factors** | Mobile (Phone/Tablet) | Wear OS companion app and Glance widgets. |
| **Device AI** | None | ML Kit GenAI Prompt API (Gemini Nano). |
