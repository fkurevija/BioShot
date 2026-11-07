# BioShot Core Testing Guide

Welcome to the **BioShot** Testing Guide. All steps outlined below represent the complete validation procedure and should always be tested during QA and verification cycles.

---

## 1. Dashboard & Adherence
1. Open the app to the **Dashboard** tab.
2. Verify that the **Next Dose Countdown** card displays accurate remaining days/hours until your next scheduled injection.
3. Verify that the **Last Injection Site** card displays your most recent administration site and rotation tips.

---

## 2. Therapy Setup (Biologics & GLP-1 Peptides)
1. Navigate to the **Medications** tab and tap the `+` floating action button.
2. Tap one of the **Quick Select Biologic** or **Quick Select GLP-1 Peptide** chips (e.g., Humira or Mounjaro/Zepbound) and verify name, dosage, and schedule auto-populate.
3. Expand the **GLP-1 / Peptide Syringe Calculator** card, enter vial milligrams and bacteriostatic water volume, and tap **"Apply to Dosage Field"**.
4. Configure loading/induction doses if applicable and save the medication.

---

## 3. Injection & Body Map Site Rotation
1. Navigate to the **Body Map** tab.
2. Tap an anatomical site on the interactive body map (e.g., Abdomen Left).
3. If the site was used within your last 3 injections, verify that the **Site Rotation Warning** banner appears with the **"Switch to Recommended"** button. Tap it to automatically jump to the recommended site.
4. Record the injection and verify it appears in the **Injection History** below.
5. Tap **Edit** on a past log entry, change its location/site or timestamp, and save.

---

## 4. Weight & Health Metrics Tracker
1. On the **Dashboard**, tap **"Log Weight / Metric"** or the `+` icon on the **Weight & Health Tracker** card.
2. Enter weight (lbs/kg), optional body fat percentage, waist measurements, and symptom notes. Save and verify it updates the card immediately.

---

## 5. Pharmacokinetic (PK) Curve
1. Navigate to the **PK Curve** tab.
2. Verify that the serum concentration graph renders smoothly and displays active drug mass remaining (`mg`).

---

## 6. Data Backup & Restore (Data Portability)
1. Navigate to the **Settings** tab.
2. Tap **"Backup Your Data"**, verify that a success Toast appears, and test both **"Share"** (sending via email/Drive) and **"Save to Device"** (saving to your local filesystem or Google Drive).
3. Tap **"Import My Data"**, select a previously exported `.json` backup file via the file picker.
4. Test all three **Conflict Resolution Strategies** in the modal dialog:
   - **Merge**: Combines imported records with existing ones (fresh auto-generated IDs ensure zero data loss).
   - **Overwrite**: Replaces existing database records with the backup file contents.
   - **Skip duplicates**: Skips records with conflicting IDs.
5. Verify that data populates correctly across dashboard, charts, and logs.

---

## 7. Notifications & Reminders
1. In **Settings**, toggle **"Injection Reminders"** ON.
2. Tap **"Set Custom Reminder Time of Day"** and select a preferred time.
3. Verify that reminders schedule correctly with the Android WorkManager.

---

## 8. Database Integrity & Error Checks
1. In **Settings**, tap **"Check for Database Errors"**.
2. Verify that the app confirms database health or flags migration issues.

---

## 9. Running Automated Tests

Developers and QA can run the unit test suite and coverage reports via Gradle:
- **Run Unit Tests**:
  ```bash
  ./gradlew testDebugUnitTest
  ```
- **Generate JaCoCo Coverage Report**:
  ```bash
  ./gradlew app:jacocoTestReport
  ```
  *(HTML report output: `app/build/reports/jacoco/jacocoTestReport/html/index.html`)*
