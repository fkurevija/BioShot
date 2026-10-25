# BioShot Architecture & Technical Documentation

This document outlines the core architecture, data flow, pharmacokinetic modeling, and database design of **BioShot**.

---

## 1. Architectural Overview

BioShot follows **Clean Architecture** principles combined with **MVVM (Model-View-ViewModel)** and **Jetpack Compose** for the UI layer.

```
[UI Layer: Jetpack Compose Screens & Components]
                     │
                     ▼
       [BioShotViewModel (StateFlow)]
                     │
                     ▼
     [Repositories (Medication, Injection, Symptom, Weight)]
                     │
                     ▼
      [Room Database (SQLite with Migrations)]
```

---

## 2. Core Modules & Packages

- **`com.fkurevija.bioshot.core.model`**: Domain data classes (`Drug`, `Medication`, `InjectionLog`, `SymptomEntry`, enums).
- **`com.fkurevija.bioshot.core.database`**: Room entities (`MedicationEntity`, `InjectionLogEntity`, `SymptomEntryEntity`, `WeightLogEntity`), DAOs, TypeConverters, and Database Migrations (1 to 4).
- **`com.fkurevija.bioshot.core.pk`**: Pharmacokinetic calculation engine (`PkCalculator`) implementing 1-compartment first-order absorption and elimination modeling.
- **`com.fkurevija.bioshot.core.schedule`**: Schedule and recurrence engine (`ScheduleEngine`) for weekly, bi-weekly, monthly, daily, and custom intervals.
- **`com.fkurevija.bioshot.core.notifications`**: Notification scheduling and background worker (`InjectionReminderWorker`).
- **`com.fkurevija.bioshot.feature`**: Feature-specific screens and UI components (`dashboard`, `medication`, `log`, `symptom`, `chart`, `settings`).

---

## 3. Pharmacokinetic Modeling (`PkCalculator`)

BioShot calculates theoretical serum drug concentrations over time using the one-compartment open pharmacokinetic model with first-order absorption and elimination:

$$C(t) = \frac{F \cdot \text{Dose} \cdot k_a}{V_d \cdot (k_a - k_{el})} \cdot \left( e^{-k_{el} \cdot (t - t_{\text{dose}})} - e^{-k_a \cdot (t - t_{\text{dose}})} \right)$$

- **Elimination Rate Constant ($k_{el}$)**: $\frac{\ln(2)}{t_{1/2}}$
- **Bioavailability ($F$)**: Fractional absorption (default 1.0).
- **Volume of Distribution ($V_d$)**: Normalized to 1.0 for relative concentration tracking.

---

## 4. Database & Migrations

BioShot uses **Room** with schema export enabled and robust migration handling (`DatabaseBackupManager` performs pre-migration backups to prevent data loss).
- **Version 1**: Initial schema (`medications`, `injection_logs`, `symptom_entries`).
- **Version 2**: Added typed JSON list columns (`sideEffects`, `tags`).
- **Version 3**: Added `customIntervalDays` for flexible dosing schedules.
- **Version 4**: Added loading dose rules (`isInitialLoadingDose`, `loadingDoseAmount`), inventory tracking (`inventoryCount`, `expirationDate`), and `weight_logs` table.

---

## 5. Testing Strategy

Unit tests cover critical algorithmic and domain components:
- **`PkCalculatorTest`**: Verifies elimination rate constants, pre/post-dose concentration calculations, and curve generation step counts.
- **`ScheduleEngineTest`**: Verifies accurate date calculation for weekly, bi-weekly, monthly, and custom intervals.
- **`MedicationEntityTest`**: Validates frequency formatting extension functions.
