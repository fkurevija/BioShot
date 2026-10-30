# BioShot 💉📊

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white" alt="Platform" />
  <img src="https://img.shields.io/badge/MinSdk-26%2B-blue?style=flat-square" alt="MinSdk" />
  <img src="https://img.shields.io/badge/Kotlin-2.0+-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white" alt="Compose" />
  <img src="https://img.shields.io/badge/Tests-28%20Passed-success?style=flat-square" alt="Tests" />
  <img src="https://img.shields.io/badge/License-MIT-yellow.svg?style=flat-square" alt="License" />
</p>

**BioShot** is an advanced, privacy-first Android application designed for tracking biologic therapies (autoimmune, inflammatory, and chronic conditions) and GLP-1 peptide treatments. It combines pharmacokinetic (PK) blood concentration modeling, interactive body site rotation maps, symptom flare diaries, and weight/health metrics tracking into a sleek Material 3 experience.

---

## 📑 Table of Contents
- [Key Features](#-key-features)
- [Architecture & Tech Stack](#-architecture--tech-stack)
- [Pharmacokinetic (PK) Modeling](#-pharmacokinetic-pk-modeling)
- [Project Structure](#-project-structure)
- [Getting Started & Building](#-getting-started--building)
- [Testing & Code Coverage](#-testing--code-coverage)
- [License](#-license)

---

## 🚀 Key Features

- **Pharmacokinetic (PK) Simulation**: 1-compartment open pharmacokinetic model (`PkCalculator`) estimating active drug concentration curves (`mg` remaining) over time based on drug half-life and absorption rates.
- **Interactive Body Site Map**: Visual anatomical body site rotation tracker (`InjectionLogBodyMap`) ensuring proper site rotation to prevent lipohypertrophy and ensure optimal absorption.
- **Dual-Therapy Preset Engine**: Built-in presets for major Biologics (Humira, Stelara, Dupixent, Enbrel) and GLP-1 Peptides (Wegovy, Mounjaro, Ozempic) with support for **Loading Doses** and cold-chain **Inventory/Lot Tracking**.
- **Health & Symptom Metrics Tracker**: Track weight, BMI, body fat percentage, waist measurements, pain scales, and symptom flare diaries.
- **Clinical Doctor Export**: Generate and share professional PDF reports combining dose history, PK concentration curves, and symptom logs for clinical visits.

---

## 🛠️ Architecture & Tech Stack

BioShot follows **Clean Architecture** principles and **MVVM** design patterns:
- **UI**: Jetpack Compose, Material 3, Material Icons Extended, Dynamic Color.
- **State Management**: Kotlin Coroutines & `StateFlow`.
- **Local Database**: Room Database with robust pre-migration backups and safe schema migrations (`v1` to `v4`).
- **Testing**: JUnit 4, Kotlin Coroutines Test, JaCoCo Code Coverage.

---

## 📈 Pharmacokinetic (PK) Modeling

BioShot models serum concentration over time using first-order absorption and elimination equations:

$$C(t) = \frac{F \cdot \text{Dose} \cdot k_a}{V_d \cdot (k_a - k_{el})} \cdot \left( e^{-k_{el} \cdot (t - t_{\text{dose}})} - e^{-k_a \cdot (t - t_{\text{dose}})} \right)$$

---

## 📁 Project Structure

```
com.fkurevija.bioshot/
├── core/
│   ├── database/       # Room Entities, DAOs, Repositories, Migrations
│   ├── model/          # Domain data classes & enums
│   ├── notifications/  # Notification scheduler & workers
│   ├── pk/             # Pharmacokinetic calculation engine
│   └── schedule/       # Recurrence & dosing schedule engine
└── feature/
    ├── chart/          # PK concentration curve charts
    ├── dashboard/      # Hero card, countdown timer, health tracker
    ├── log/            # Interactive body map & injection logger
    ├── medication/     # Medication setup wizard & biologic presets
    ├── navigation/     # NavGraph & screens
    ├── settings/       # Doctor PDF export & app preferences
    └── symptom/        # Symptom diary & flare tracking
```

---

## ⚙️ Getting Started & Building

1. Clone the repository:
   ```bash
   git clone https://github.com/fkurevija/BioShot.git
   ```
2. Open the project in **Android Studio** (Koala / Ladybug or newer).
3. Build and run on your emulator or physical device:
   ```bash
   ./gradlew assembleDebug
   ```

---

## 🧪 Testing & Code Coverage

BioShot features a robust unit test suite covering PK math, scheduling engines, and data formatters.

- Run unit tests:
  ```bash
  ./gradlew testDebugUnitTest
  ```
- Generate JaCoCo code coverage report:
  ```bash
  ./gradlew app:jacocoTestReport
  ```
  *(HTML report output: `app/build/reports/jacoco/jacocoTestReport/html/index.html`)*

---

## 📄 License

Distributed under the **MIT License**. See [LICENSE](LICENSE) for more information.
