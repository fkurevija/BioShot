package com.combustiblelemonlabs.bioshot.feature.settings

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.combustiblelemonlabs.bioshot.core.database.InjectionLogEntity
import com.combustiblelemonlabs.bioshot.core.database.MedicationEntity
import com.combustiblelemonlabs.bioshot.core.database.SymptomEntryEntity
import com.combustiblelemonlabs.bioshot.core.pk.PkCalculator
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object PdfReportExporter {

    fun generatePdfReport(
        context: Context,
        medications: List<MedicationEntity>,
        injectionLogs: List<InjectionLogEntity>,
        symptomEntries: List<SymptomEntryEntity>
    ): Uri? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 page dimensions
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
        }

        val titlePaint = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(20, 80, 160)
            textSize = 20f
            isFakeBoldText = true
        }

        val headerPaint = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(40, 40, 40)
            textSize = 14f
            isFakeBoldText = true
        }

        val bodyPaint = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(60, 60, 60)
            textSize = 11f
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }

        var y = 40f

        // Title
        canvas.drawText("BIOSHOT THERAPY & SYMPTOM CLINICAL REPORT", 40f, y, titlePaint)
        y += 24f

        canvas.drawText("Generated: ${dateFormat.format(Date())}", 40f, y, bodyPaint)
        y += 20f

        paint.color = Color.LTGRAY
        paint.strokeWidth = 1f
        canvas.drawLine(40f, y, 555f, y, paint)
        y += 25f

        // Section 1: Active Biologic Medications
        canvas.drawText("1. Active Biologic Prescriptions", 40f, y, headerPaint)
        y += 18f

        if (medications.isEmpty()) {
            canvas.drawText("No active medications recorded.", 50f, y, bodyPaint)
            y += 20f
        } else {
            medications.forEach { med ->
                canvas.drawText("• ${med.name} | Dosage: ${med.dosage} | Freq: ${med.frequency.name} | Device: ${med.deviceType.name}", 50f, y, bodyPaint)
                y += 18f
            }
        }
        y += 10f

        // Section 2: PK Concentration Curve Visual Chart
        canvas.drawText("2. Estimated Serum Pharmacokinetic (PK) Curve", 40f, y, headerPaint)
        y += 18f

        val doseEvents = injectionLogs.map { log ->
            val amountNum = log.dosageGiven.filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 40.0
            PkCalculator.DoseEvent(timestamp = log.timestamp, doseAmount = amountNum, bioavailability = 0.85)
        }

        val now = System.currentTimeMillis()
        val startTime = now - (30L * 24 * 60 * 60 * 1000)
        val endTime = now + (30L * 24 * 60 * 60 * 1000)
        val curvePoints = PkCalculator.generateCurve(
            doses = doseEvents,
            startTimeMs = startTime,
            endTimeMs = endTime,
            stepCount = 100,
            halfLifeHours = 336.0,
            absorptionHalfLifeHours = 24.0
        )

        if (curvePoints.isNotEmpty()) {
            val chartLeft = 50f
            val chartRight = 545f
            val chartTop = y
            val chartBottom = y + 120f
            val chartWidth = chartRight - chartLeft
            val chartHeight = chartBottom - chartTop

            val bgPaint = Paint().apply {
                color = Color.rgb(245, 247, 250)
                style = Paint.Style.FILL
            }
            canvas.drawRect(chartLeft, chartTop, chartRight, chartBottom, bgPaint)

            val maxConc = (curvePoints.maxOfOrNull { it.concentration } ?: 1.0).coerceAtLeast(10.0)
            val curvePath = Path()
            val stepX = chartWidth / (curvePoints.size - 1).coerceAtLeast(1)

            curvePoints.forEachIndexed { index, point ->
                val px = chartLeft + (index * stepX)
                val py = chartBottom - ((point.concentration / maxConc).toFloat() * chartHeight)
                if (index == 0) curvePath.moveTo(px, py) else curvePath.lineTo(px, py)
            }

            val linePaint = Paint().apply {
                color = Color.rgb(20, 120, 220)
                strokeWidth = 2.5f
                style = Paint.Style.STROKE
                isAntiAlias = true
            }
            canvas.drawPath(curvePath, linePaint)

            y = chartBottom + 15f
            canvas.drawText("Chart: 60-day serum concentration estimate (-30d to +30d)", 50f, y, bodyPaint)
            y += 20f
        } else {
            canvas.drawText("No injection history available for PK curve calculation.", 50f, y, bodyPaint)
            y += 20f
        }

        // Section 3: Recent Injection History
        canvas.drawText("3. Recent Injection History & Site Rotation", 40f, y, headerPaint)
        y += 18f

        if (injectionLogs.isEmpty()) {
            canvas.drawText("No injection records logged.", 50f, y, bodyPaint)
            y += 20f
        } else {
            injectionLogs.take(8).forEach { log ->
                val dateStr = dateFormat.format(Date(log.timestamp))
                canvas.drawText("• $dateStr | Site: ${log.site.name} | Dose: ${log.dosageGiven} | Pain: ${log.painLevel ?: 0}/10 | Lot: ${log.lotNumber ?: "N/A"}", 50f, y, bodyPaint)
                y += 16f
            }
        }
        y += 10f

        // Section 4: Symptom Diary
        canvas.drawText("4. Symptom Diary & Flare-up Correlation Log", 40f, y, headerPaint)
        y += 18f

        if (symptomEntries.isEmpty()) {
            canvas.drawText("No symptoms or flare-ups recorded.", 50f, y, bodyPaint)
            y += 20f
        } else {
            symptomEntries.take(6).forEach { entry ->
                val dateStr = dateFormat.format(Date(entry.timestamp))
                canvas.drawText("• $dateStr | ${entry.symptomName} (Severity: ${entry.severity}/10) | Notes: ${entry.notes ?: "None"}", 50f, y, bodyPaint)
                y += 16f
            }
        }

        pdfDocument.finishPage(page)

        val file = File(context.cacheDir, "BioShot_Doctor_Visit_Report.pdf")
        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
}
