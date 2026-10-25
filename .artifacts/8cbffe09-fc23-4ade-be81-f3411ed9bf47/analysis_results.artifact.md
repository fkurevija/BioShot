# Comprehensive Comparison: BioShot vs. Shotsy & Improvement Plan

This document provides a detailed feature-by-feature comparison between **BioShot** and **Shotsy** ([Shotsy on Google Play](https://play.google.com/store/apps/details?id=com.shotsy.app)), followed by an actionable roadmap for functional and design enhancements to elevate BioShot to industry-leading standards.

---

## 1. Feature Comparison Matrix

| Feature Category | BioShot (Current State) | Shotsy (Industry Benchmark) | Gap / Opportunity |
| :--- | :--- | :--- | :--- |
| **Pharmacokinetic (PK) Modeling** | Robust 1-compartment model (`PkCalculator`), half-life, absorption/elimination curves. | PK simulation curves, active drug amount remaining (`mg`), peak/trough markers. | **Medium**: BioShot has the core math engine (`PkCalculator`), but can add active level countdowns ("X mg remaining") and peak day highlights. |
| **Medication & Peptide Presets** | Generic drug model (`Drug.kt`), medication setup wizard. | Pre-loaded presets for popular GLP-1/GIP peptides (Semaglutide, Tirzepatide, Retatrutide, Liraglutide) with default half-lives (e.g., 7 days, 5 days). | **High**: Add dedicated GLP-1/peptide preset library with reconstitution calculator (mg to syringe units). |
| **Injection Tracking & Body Map** | Interactive body map (`InjectionLogBodyMap.kt`), site rotation tracking. | Visual injection site logging with rotation history warnings ("Site used recently"). | **Low/Medium**: Enhance rotation intelligence with smart site recommendations and usage heatmaps. |
| **Weight & Body Metrics** | Basic logs or limited tracking. | Comprehensive weight tracking, BMI calculation, body fat %, measurements (waist/hips), progress photo timeline. | **Critical Gap**: BioShot currently lacks dedicated weight & body composition tracking and correlation with PK curves. |
| **Symptom & Side Effect Diary** | Symptom diary (`SymptomDiaryScreen.kt`), symptom form. | Symptom logging with severity sliders, side-effect tags (nausea, fatigue, suppression), and correlation against drug concentration peaks. | **Medium**: Link symptom severity directly to PK level charts to show side-effect correlation. |
| **Reminders & Schedule Engine** | `ScheduleEngine`, notification worker, reminders. | Flexible dosing intervals (every 5, 7, 10 days), refill alerts, pen inventory tracking. | **Medium**: Expand scheduling flexibility and add pen/vial inventory tracking. |
| **Widgets & Home Screen** | App UI screens. | Home screen widgets (active level, countdown to next shot, quick log). | **High**: Implement Jetpack Glance widgets for quick at-a-glance monitoring. |
| **Reports & Export** | PDF report exporter (`PdfReportExporter.kt`, doctor export). | Doctor-ready PDF summaries combining dose history, PK curves, and weight loss progress. | **Low**: BioShot already has strong doctor export; enhance it with weight trend overlays. |

---

## 2. Functional Improvements Plan (Tailored for Biologics & Future GLP-1 Support)

### Phase 1: Symptom Flare & Health Metric Tracking (Biologic & Chronic Focus)
- **Beyond Weight**: While Shotsy focuses on weight loss, Biologics (used for autoimmune, inflammatory, or chronic conditions like RA, Psoriasis, IBD, Eczema) require tracking **Disease Activity Scores, Symptom Severity, Pain/Fatigue Scores, and Biomarkers** (e.g., CRP, ESR).
- **Dual-Purpose Metric Log**: Allow users to log weight/BMI (relevant for both biologics and GLP-1s) alongside custom symptom severity and flare markers.
- **PK Curve Correlation**: Correlate symptom flare-ups and trough concentration (`Cmin`) drops to help patients and doctors identify if symptoms return prematurely before the next bi-weekly or monthly injection.

### Phase 2: Biologic & Peptide Preset Library with Loading & Maintenance Doses
- **Comprehensive Preset Library**: Pre-load profiles for both major biologics (e.g., Humira/Adalimumab, Stelara/Ustekinumab, Dupixent/Dupilumab, Enbrel) and GLP-1s (Semaglutide, Tirzepatide) with correct half-lives (~10–20 days for antibodies vs. ~5–7 days for peptides).
- **Loading Dose Schedules**: Support complex biologic dosing protocols (e.g., Initial Double Dose / Loading Dose on Day 0, followed by maintenance doses every 14 or 28 days).
- **Lot & Expiration Tracking**: Essential for biologics (cold-chain items stored in fridges)—track lot numbers, expiration dates, and pen/syringe inventory.

### Phase 3: Trough Concentration (`Cmin`) Analytics & Compliance
- **Therapeutic Window & Trough Warnings**: For biologics, maintaining a minimum effective concentration (MEC) above the trough threshold is critical. Add visual markers for peak (`Cmax`) and trough (`Cmin`) on the PK chart.
- **Dose Delay Alerts**: Alert users if a bi-weekly injection is delayed, highlighting potential antibody clearance and loss of efficacy.
- **Export Integration**: Include trough level estimates and symptom-to-level correlation in the doctor PDF export (`PdfReportExporter.kt`).

### Phase 4: Home Screen Widgets (Jetpack Glance)
- **Active Level Widget**: Glance widget showing current estimated drug concentration and days until next dose.
- **Next Dose Countdown & Quick Log**: Quick access to log injections directly from the home screen.

---

## 3. UI/UX & Design Enhancements

- **Material 3 Polish**: Embrace dynamic color, sleek dark mode optimized for OLED screens, and fluid animations for card expansions.
- **Immersive Dashboard Hero Card**: Redesign the dashboard top card to feature an interactive mini-PK curve, active drug remaining pill, and countdown timer.
- **Enhanced Body Map**: Add pulsing indicators or color-coded recency heatmap on `InjectionLogBodyMap` to clearly guide users to the optimal injection site rotation.

---

## 4. Verification & Next Steps
1. Review this proposed roadmap with the user.
2. Select priority features (e.g., Weight Tracking + Peptide Presets + Glance Widgets) to implement in upcoming development cycles.
