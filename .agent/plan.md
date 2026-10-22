# Project Plan

BioLog Android App - Stage (b) Data Model/ERD and Schedule Engine Design

## Project Brief

# Project Brief: BioLog Android App (MVP)

## Features
1. **Biometric Data Modeling & Entry**: Define custom biometric data attributes, schema entities, and capture daily readings through a clean, structured form interface.
2. **Schedule Engine & Reminders**: Configure custom biometric logging schedules, recurring intervals, and intelligent notifications for scheduled data inputs.
3. **Dashboard & Trends Overview**: View captured biometric records and schedules in an adaptive multi-pane layout with summary statistics and visualizations.

## High-Level Technical Stack
- **Language**: Kotlin
- **UI Toolkit**: Jetpack Compose with Material 3
- **Architecture & Navigation**: State-driven **Jetpack Navigation 3**
- **Adaptive Layouts**: **Compose Material Adaptive** library
- **Asynchrony**: Kotlin Coroutines & Flow

*(Note: The UI Design Image section has been omitted due to a `RESOURCE_EXHAUSTED` quota limit on image generation.)*

## Implementation Steps
**Total Duration:** 5h 9m 53s

### Task_1_CoreDataModelAndRoom: Implement core Room entities, DAOs, and database setup for medications, injections, symptoms, and site rotation tracking.
- **Status:** COMPLETED
- **Updates:** Completed implementation of core Room entities, DAOs, TypeConverters, and database setup (`BioLogDatabase`) for medications, injections, and symptoms. Verified successful compilation and project build.
- **Acceptance Criteria:**
  - Room database created successfully
  - Entities and DAOs compiled without errors
  - Project builds successfully
- **Duration:** 2m 35s

### Task_2_ScheduleEngineAndPKCurve: Implement the schedule calculation engine and pharmacokinetic (PK) estimated level curve logic.
- **Status:** COMPLETED
- **Updates:** Completed ScheduleEngine and PkCalculator implementation with support for loading + maintenance schedules, pharmacokinetic one-compartment modeling, and unit test validation.
- **Acceptance Criteria:**
  - Schedule engine calculates upcoming doses correctly
  - PK curve estimation logic implemented
  - Project builds successfully
- **Duration:** 2m 9s

### Task_3_UIAndScreens: Implement Jetpack Compose UI screens including dashboard, medication tracker, schedule view, site rotation body map, and symptom diary.
- **Status:** COMPLETED
- **Updates:** Implemented complete Jetpack Compose UI screens (Dashboard, Medication Setup Wizard, Injection Log with Interactive Body Map, Symptom Diary, Charts with PK Level Curve and disclaimers, Doctor Export & Settings), navigation graph, and updated MainActivity. Verified successful build.
- **Acceptance Criteria:**
  - All core screens implemented with Jetpack Compose
  - Navigation working smoothly
  - Project builds successfully
- **Duration:** 5h 2m 4s

### Task_4_RunAndVerify: Run and Verify application stability, build pass, ensure no crashes, and confirm alignment with user requirements.
- **Status:** COMPLETED
- **Updates:** Successfully completed Task_4_RunAndVerify. All unit tests passed, lint checks and locale formatting errors resolved, and build verified successfully.
- **Acceptance Criteria:**
  - app does not crash
  - build pass
  - make sure all existing tests pass
- **Duration:** 3m 5s

