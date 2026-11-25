package com.bikemanager.domain.export

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.bikemanager.domain.common.ErrorHandler
import com.bikemanager.domain.common.Result
import com.bikemanager.domain.model.CountingMethod
import com.bikemanager.domain.model.Maintenance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Implémentation Android : génère le PDF avec android.graphics.pdf.PdfDocument
 * et l'expose via FileProvider dans le sélecteur de partage natif.
 */
class MaintenanceHistoryExporterImpl(
    private val context: Context
) : MaintenanceHistoryExporter {

    override suspend fun exportAndShare(
        bikeName: String,
        countingMethod: CountingMethod,
        maintenances: List<Maintenance>
    ): Result<Unit> = ErrorHandler.catching("export de l'historique d'entretiens en PDF") {
        val file = withContext(Dispatchers.IO) {
            generatePdf(bikeName, countingMethod, maintenances)
        }
        shareFile(file)
    }

    private fun generatePdf(
        bikeName: String,
        countingMethod: CountingMethod,
        maintenances: List<Maintenance>
    ): File {
        val pageWidth = 595
        val pageHeight = 842
        val margin = 40f
        val bottomLimit = pageHeight - margin

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            color = Color.BLACK
        }
        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 11f
            color = Color.DKGRAY
        }
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            color = Color.BLACK
        }
        val rowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 11f
            color = Color.BLACK
        }
        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)
        val generatedFormat = SimpleDateFormat("dd/MM/yyyy à HH:mm", Locale.FRANCE)
        val unit = if (countingMethod == CountingMethod.KM) "km" else "h"

        val dateColX = margin
        val nameColX = margin + 90f
        val valueColX = pageWidth - margin - 100f

        val document = PdfDocument()
        var pageNumber = 1
        var page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        var canvas = page.canvas
        var y = margin
        var isFirstPage = true

        fun drawTableHeader() {
            if (isFirstPage) {
                y += titlePaint.textSize
                canvas.drawText(bikeName, margin, y, titlePaint)
                y += 20f
                canvas.drawText(
                    "Historique des entretiens – généré le ${generatedFormat.format(Date())}",
                    margin,
                    y,
                    subtitlePaint
                )
                y += 24f
                isFirstPage = false
            }
            canvas.drawText("Date", dateColX, y, headerPaint)
            canvas.drawText("Entretien", nameColX, y, headerPaint)
            canvas.drawText("Valeur", valueColX, y, headerPaint)
            y += 6f
            canvas.drawLine(margin, y, pageWidth - margin, y, linePaint)
            y += 18f
        }

        drawTableHeader()

        if (maintenances.isEmpty()) {
            canvas.drawText("Aucun entretien enregistré.", dateColX, y, rowPaint)
        }

        for (maintenance in maintenances) {
            if (y > bottomLimit) {
                document.finishPage(page)
                pageNumber += 1
                page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
                canvas = page.canvas
                y = margin
                drawTableHeader()
            }

            canvas.drawText(dateFormat.format(Date(maintenance.date)), dateColX, y, rowPaint)
            canvas.drawText(maintenance.name, nameColX, y, rowPaint)
            canvas.drawText("${formatValue(maintenance.value)} $unit", valueColX, y, rowPaint)
            y += 20f
        }

        document.finishPage(page)

        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, "entretiens_${bikeName.sanitizeForFileName()}.pdf")
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()

        return file
    }

    private fun shareFile(file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(sendIntent, "Partager l'historique d'entretiens").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    private fun formatValue(value: Float): String =
        if (value % 1f == 0f) value.toInt().toString() else value.toString()
}

private fun String.sanitizeForFileName(): String = replace(Regex("[^A-Za-z0-9_-]"), "_")
