# 💉 BioShot

**BioShot** is a private, offline-first Android injection tracker built for people managing biologic and biosimilar medications. It combines a clean Jetpack Compose UI with a pharmacokinetic engine, site-rotation guidance, symptom diary, and doctor-ready export — all stored locally on your device with no cloud dependency.

> **Medical Disclaimer:** BioShot is an organisational tool only. Pharmacokinetic curves are mathematical estimates and do not constitute medical advice, diagnosis, or treatment. Always follow the dosing schedule prescribed by your healthcare provider.

---

## ✨ Features

### Dashboard
- **Next dose countdown** — days/hours until your next scheduled injection
- **Last injection site reminder** — never repeat the same site twice in a row
- **Quick-log shortcut** — one tap to open the injection logger

### Medication Setup Wizard
- **Quick-select biologic presets** (tap a chip to auto-fill name & default dose):

  | Brand | Generic | Default Dose |
  |---|---|---|
  | Humira | Adalimumab | 40 mg |
  | Enbrel | Etanercept | 50 mg |
  | Stelara | Ustekinumab | 45 mg |
  | Cosentyx | Secukinumab | 300 mg |
  | Dupixent | Dupilumab | 300 mg |
  | Simponi | **Golimumab** | 50 mg |

- Custom drug, dose, and device entry for any biologic or biosimilar not in the preset list
- Supported schedules: Daily · Weekly · Biweekly · Monthly · Custom · As-Needed
- Supported devices: Auto-Injector · Pen · Syringe · Pump

### Injection Log & Body Map
- Visual site picker: abdomen (L/R), thigh (L/R), upper arm (L/R), buttock (L/R)
- Lot number and expiry tracking per injection
- Pain level (0–10) and free-text notes
- Side-effect tagging

### Symptom Diary
- Log symptom name, severity (1–10), and notes
- Entries linked to the injection timeline for correlation

### PK Curve Chart
- Visualises estimated serum concentration over the past 30 days and next 30 days
- Uses a **one-compartment, first-order absorption/elimination** model (Bateman equation):

  ```
  C(t) = [(F · Dose · ka) / (Vd · (ka − kel))] · [e^(−kel·Δt) − e^(−ka·Δt)]
  ```

  where `kel = ln(2) / t½` and `ka = ln(2) / t½_absorption`
- Superimposes multiple dose events for a cumulative curve
- Prominent disclaimer on every chart view

### Doctor Visit Export
- Generates a structured plain-text report of medications, full injection history, and symptom diary
- Share directly via email or any installed app with Android's standard Share sheet

---

## 🏗️ Architecture

```
app/
└── src/main/java/com/fkurevija/bioshot/
    ├── core/
    │   ├── database/          # Room DB, DAOs, entities, type converters
    │   ├── model/             # Domain models & enums (Medication, InjectionLog, SymptomEntry …)
    │   ├── pk/                # PkCalculator — pharmacokinetic engine
    │   └── schedule/          # ScheduleEngine — next-dose & loading-dose logic
    ├── feature/
    │   ├── chart/             # PK Curve screen
    │   ├── dashboard/         # Dashboard screen
    │   ├── log/               # Injection Log & Body Map screen
    │   ├── medication/        # Medication List + Setup Wizard screens
    │   ├── navigation/        # BioShotNavGraph + Screen sealed class
    │   ├── settings/          # Doctor Export & Settings screen
    │   ├── symptom/           # Symptom Diary screen
    │   └── viewmodel/         # BioShotViewModel (single shared VM)
    ├── ui/theme/              # Material 3 colour, typography, theme
    └── MainActivity.kt
```

**Key patterns:**
- **Offline-first** — Room database, no network calls required
- **Append-only event log** — injection records are never mutated, only appended or deleted
- **Single ViewModel** — `BioShotViewModel` exposes `StateFlow`s consumed by all screens
- **Jetpack Compose** — fully declarative UI with Material 3

---

## 🧪 Schedule Engine

`ScheduleEngine` supports:
- **Standard intervals** — Daily, Weekly, Biweekly, Monthly, Custom (N days)
- **Loading-dose induction phases** — e.g., weeks 0 → 1 → 2 → then maintenance (common for ustekinumab, secukinumab)
- `generateUpcomingSchedules()` produces a list of N future injection timestamps

---

## 🔬 Pharmacokinetic Engine

`PkCalculator` provides:
- `calculateEliminationRateConstant(halfLifeHours)` → `kel`
- `calculateConcentrationAtTime(...)` → concentration at a single time point (handles `ka ≈ kel` edge case)
- `generateCurve(doses, startTime, endTime, stepCount, halfLifeHours)` → full `List<PkDataPoint>` for charting

All functions are pure (no Android dependencies) and fully unit-tested.

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + StateFlow |
| Database | Room (encrypted offline storage) |
| Async | Kotlin Coroutines |
| DI | None (manual construction via `AndroidViewModel`) |
| Serialization | kotlinx.serialization |
| Build | Gradle (KTS) + KSP |
| Min SDK | API 26 (Android 8.0) |
| Target SDK | API 37 |

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Meerkat or later
- JDK 11
- Android SDK API 26+

### Build & Run

```bash
# Clone the repo
git clone https://github.com/your-username/BioShot.git
cd BioShot

# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew testDebugUnitTest

# Install on connected device / emulator
./gradlew installDebug
```

The APK will be output to `app/build/outputs/apk/debug/`.

---

## 📁 Project Structure at a Glance

```
BioShot/
├── app/
│   ├── src/
│   │   ├── main/          # Application source
│   │   ├── test/          # Unit tests (PkCalculator, ScheduleEngine)
│   │   └── androidTest/   # Instrumented tests
│   └── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## ⚠️ Privacy & Data

- **All data is stored locally** on-device in the Room database (`bioshot_database`)
- No telemetry, analytics, or cloud sync of any kind
- Exporting a report is a manual, user-initiated action via the system Share sheet

---

## 📄 License

This project is for personal use. Add your preferred license here (e.g. MIT, Apache 2.0).
