# BioShot: Biologics & GLP-1 Tracking App

BioShot is an advanced, privacy-first Android application designed for tracking biologic therapies (autoimmune, inflammatory, and chronic conditions) and GLP-1 peptide treatments. It combines pharmacokinetic (PK) blood concentration modeling, interactive body site rotation maps, symptom flare diaries, and weight/health metrics tracking.

---

## Key Features

1. **Pharmacokinetic (PK) Simulation**:
   - 1-compartment open pharmacokinetic modeling (`PkCalculator`) estimating serum drug concentration curves over time based on half-life and absorption rates.
2. **Interactive Injection Site Body Map**:
   - Visual anatomical body map (`InjectionLogBodyMap`) ensuring proper site rotation to prevent lipohypertrophy and ensure optimal absorption.
3. **Biologic & GLP-1 Dosing Schedules**:
   - Support for standard (weekly, bi-weekly, monthly, daily) and custom intervals, plus **Loading Doses** and cold-chain **Inventory/Lot Tracking**.
4. **Health & Weight Metrics Tracker**:
   - Track weight, BMI, body fat percentage, waist measurements, and symptom notes.
5. **Clinical Reports & Doctor Export**:
   - Export comprehensive PDF reports (`PdfReportExporter`) combining dose history, PK curves, and health logs for clinical visits.

---

## Tech Stack

- **UI**: Jetpack Compose, Material 3, Material Icons Extended.
- **Architecture**: MVVM, Kotlin Coroutines, StateFlow, Clean Architecture.
- **Local Storage**: Room Database with safe schema migrations (v1 to v4).
- **Testing**: JUnit 4, Kotlin Coroutines Test.

---

## Testing & Quality Assurance

BioShot includes comprehensive unit tests covering pharmacokinetic calculations, schedule recurrence engines, and data models.

To run tests:
```bash
./gradlew testDebugUnitTest
```
