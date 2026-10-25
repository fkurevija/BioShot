# BioShot Product & Technical Roadmap: Biologics & GLP-1 Integration

This roadmap outlines the strategic feature set and technical architecture to evolve **BioShot** into the premier dual-purpose tracking platform for **Biologics** (autoimmune, inflammatory, and chronic conditions like RA, IBD, Psoriasis) and **GLP-1/GIP Peptides** (metabolic health and weight management).

---

## 1. Vision & Architecture Philosophy

BioShot's existing engine already contains a robust foundational core:
- **Pharmacokinetic (PK) Engine (`PkCalculator.kt`)**: 1-compartment first-order absorption and elimination modeling.
- **Injection Log & Body Map (`InjectionLogBodyMap.kt`)**: Interactive anatomical site rotation tracking.
- **Schedule & Notification Engine (`ScheduleEngine.kt`)**: Reminder and notification workers.
- **Doctor PDF Export (`PdfReportExporter.kt`)**: Clinical data export for healthcare providers.

By generalizing and expanding these systems, BioShot will seamlessly support both long-half-life biologics (~10–20+ days) and short-half-life GLP-1 peptides (~5–7 days).

---

## 2. Phased Implementation Plan

### Phase 1: Dual-Category Preset Engine, Loading Doses & Inventory Tracking
*Focus: Medication setup wizard expansion and cold-chain/lot management.*

- **Categorized Drug Presets**:
  - *Biologics*: Humira (Adalimumab), Stelara (Ustekinumab), Dupixent (Dupilumab), Enbrel (Etanercept), Remicade (Infliximab).
  - *GLP-1s*: Mounjaro/Zepbound (Tirzepatide), Wegovy/Ozempic (Semaglutide), Retatrutide.
- **Flexible Dosing Protocols**:
  - Support for **Loading Doses** (e.g., initial double dose on Day 0) vs. **Maintenance Doses** (every 7, 14, or 28 days).
- **Cold-Chain & Inventory Management**:
  - Track lot numbers, expiration dates, and remaining pen/vial inventory to alert users before supplies run low.

### Phase 2: Advanced Health Metrics & Symptom Correlation with PK Trough Levels (`Cmin`)
*Focus: Connecting pharmacokinetics directly to clinical outcomes.*

- **Dual-Track Metrics**:
  - *Biologic Focus*: Disease activity scores, pain/fatigue rating scales, and biomarker tracking (e.g., CRP, ESR).
  - *GLP-1 Focus*: Weight, BMI, body fat percentage, and body measurements (waist, hips).
- **Trough Concentration (`Cmin`) & Flare Correlation**:
  - Highlight peak (`Cmax`) and trough (`Cmin`) levels on the PK chart.
  - Correlate symptom flare-ups with anticipated trough level drops (identifying if symptoms return prematurely before a bi-weekly injection).

### Phase 3: Jetpack Compose UI/UX Modernization & Dashboard Hero Card
*Focus: Modern Material 3 polish and at-a-glance actionable insights.*

- **Immersive Dashboard Hero Card**:
  - Features an interactive mini-PK blood concentration curve.
  - "Active Drug Remaining" pill (e.g., `~14.2 mg remaining in system`).
  - Real-time countdown timer to the next scheduled injection.
- **Enhanced Injection Site Rotation**:
  - Color-coded recency heatmap on `InjectionLogBodyMap` warning users against overusing recent sites.

### Phase 4: Home Screen Widgets (Jetpack Glance)
*Focus: Quick logging and glanceable adherence monitoring.*

- **Jetpack Glance Implementation**:
  - **Active Level Widget**: Displays current estimated blood concentration and next injection countdown.
  - **Quick Log Action**: One-tap widget action to log an injection immediately.
- **State Synchronization**:
  - Connect Glance AppWidget directly to Room database via Coroutines/Flow to reflect real-time updates.

### Phase 5: Enhanced Clinical Export & Reporting
*Focus: Doctor-ready reports.*

- **PDF Export Enhancements**:
  - Combine dose history, PK curves, trough levels, weight trends, and symptom frequency logs into a clean, professional clinical report (`PdfReportExporter.kt`).

---

## 3. Technical Architecture Summary

| Component | Current Implementation | Proposed Enhancement |
| :--- | :--- | :--- |
| **Data Layer (Room)** | `Entities.kt`, `Daos.kt`, `BioLogDatabase.kt` | Add `WeightLogEntity`, `SymptomMetricEntity`, `InventoryEntity`, and expand `MedicationEntity` with loading dose rules. |
| **PK Engine** | `PkCalculator.kt` | Add peak (`Cmax`) and trough (`Cmin`) calculation helpers and active mass remaining (`mg`) aggregators. |
| **UI Layer** | Jetpack Compose (`DashboardScreen.kt`, `InjectionLogScreen.kt`) | Build Dashboard Hero Card, interactive mini-charts, and Glance widgets. |
| **Notifications** | `NotificationScheduler.kt`, `InjectionReminderWorker.kt` | Add inventory low-stock reminders and flexible interval scheduling. |
