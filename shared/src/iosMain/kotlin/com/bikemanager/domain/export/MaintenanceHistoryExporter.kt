package com.bikemanager.domain.export

import com.bikemanager.domain.common.ErrorHandler
import com.bikemanager.domain.common.Result
import com.bikemanager.domain.model.CountingMethod
import com.bikemanager.domain.model.Maintenance
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSData
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.NSString
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.UIKit.NSFontAttributeName
import platform.UIKit.NSForegroundColorAttributeName
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIColor
import platform.UIKit.UIDevice
import platform.UIKit.UIFont
import platform.UIKit.UIGraphicsPDFRenderer
import platform.UIKit.UIGraphicsPDFRendererContext
import platform.UIKit.UIUserInterfaceIdiomPad
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene

/**
 * Implémentation iOS : génère le PDF avec UIGraphicsPDFRenderer
 * et le partage via UIActivityViewController (share sheet natif).
 */
class MaintenanceHistoryExporterImpl : MaintenanceHistoryExporter {

    override suspend fun exportAndShare(
        bikeName: String,
        countingMethod: CountingMethod,
        maintenances: List<Maintenance>
    ): Result<Unit> = ErrorHandler.catching("export de l'historique d'entretiens en PDF") {
        val fileURL = generatePdf(bikeName, countingMethod, maintenances)
        shareFile(fileURL)
    }

    private fun generatePdf(
        bikeName: String,
        countingMethod: CountingMethod,
        maintenances: List<Maintenance>
    ): NSURL {
        val pageWidth = 595.0
        val pageHeight = 842.0
        val margin = 40.0
        val bottomLimit = pageHeight - margin

        val frLocale = NSLocale(localeIdentifier = "fr_FR")
        val dateFormat = NSDateFormatter().apply {
            dateFormat = "dd/MM/yyyy"
            locale = frLocale
        }
        val generatedFormat = NSDateFormatter().apply {
            dateFormat = "dd/MM/yyyy à HH:mm"
            locale = frLocale
        }
        val unit = if (countingMethod == CountingMethod.KM) "km" else "h"

        val titleAttrs = mapOf<Any?, Any?>(
            NSFontAttributeName to UIFont.boldSystemFontOfSize(20.0),
            NSForegroundColorAttributeName to UIColor.blackColor
        )
        val subtitleAttrs = mapOf<Any?, Any?>(
            NSFontAttributeName to UIFont.systemFontOfSize(11.0),
            NSForegroundColorAttributeName to UIColor.darkGrayColor
        )
        val headerAttrs = mapOf<Any?, Any?>(
            NSFontAttributeName to UIFont.boldSystemFontOfSize(11.0),
            NSForegroundColorAttributeName to UIColor.blackColor
        )
        val rowAttrs = mapOf<Any?, Any?>(
            NSFontAttributeName to UIFont.systemFontOfSize(11.0),
            NSForegroundColorAttributeName to UIColor.blackColor
        )

        val dateColX = margin
        val nameColX = margin + 90.0
        val valueColX = pageWidth - margin - 100.0

        val renderer = UIGraphicsPDFRenderer(bounds = CGRectMake(0.0, 0.0, pageWidth, pageHeight))

        val data: NSData = renderer.PDFDataWithActions { rendererContext ->
            val context: UIGraphicsPDFRendererContext = rendererContext!!
            var y = margin
            var isFirstPage = true

            fun drawTableHeader() {
                if (isFirstPage) {
                    (bikeName as NSString).drawAtPoint(CGPointMake(margin, y), withAttributes = titleAttrs)
                    y += 26.0
                    ("Historique des entretiens – généré le ${generatedFormat.stringFromDate(NSDate())}" as NSString)
                        .drawAtPoint(CGPointMake(margin, y), withAttributes = subtitleAttrs)
                    y += 24.0
                    isFirstPage = false
                }
                ("Date" as NSString).drawAtPoint(CGPointMake(dateColX, y), withAttributes = headerAttrs)
                ("Entretien" as NSString).drawAtPoint(CGPointMake(nameColX, y), withAttributes = headerAttrs)
                ("Valeur" as NSString).drawAtPoint(CGPointMake(valueColX, y), withAttributes = headerAttrs)
                y += 20.0
            }

            context.beginPage()
            drawTableHeader()

            if (maintenances.isEmpty()) {
                ("Aucun entretien enregistré." as NSString).drawAtPoint(
                    CGPointMake(dateColX, y),
                    withAttributes = rowAttrs
                )
            }

            for (maintenance in maintenances) {
                if (y > bottomLimit) {
                    context.beginPage()
                    y = margin
                    drawTableHeader()
                }

                val dateText = dateFormat.stringFromDate(
                    NSDate.dateWithTimeIntervalSince1970(maintenance.date / 1000.0)
                )
                val valueText = "${formatValue(maintenance.value)} $unit"

                (dateText as NSString).drawAtPoint(CGPointMake(dateColX, y), withAttributes = rowAttrs)
                (maintenance.name as NSString).drawAtPoint(CGPointMake(nameColX, y), withAttributes = rowAttrs)
                (valueText as NSString).drawAtPoint(CGPointMake(valueColX, y), withAttributes = rowAttrs)
                y += 20.0
            }
        }

        val filePath = NSTemporaryDirectory() + "entretiens_${bikeName.sanitizeForFileName()}.pdf"
        data.writeToFile(filePath, atomically = true)
        return NSURL.fileURLWithPath(filePath)
    }

    private fun shareFile(fileURL: NSURL) {
        val activityController = UIActivityViewController(
            activityItems = listOf(fileURL),
            applicationActivities = null
        )

        val windowScene = UIApplication.sharedApplication.connectedScenes.firstOrNull() as? UIWindowScene
        val rootViewController = windowScene?.windows?.firstOrNull()?.let { window ->
            (window as? UIWindow)?.rootViewController
        }

        if (rootViewController != null) {
            if (UIDevice.currentDevice.userInterfaceIdiom == UIUserInterfaceIdiomPad) {
                activityController.popoverPresentationController?.sourceView = rootViewController.view
            }
            rootViewController.presentViewController(activityController, animated = true, completion = null)
        }
    }

    private fun formatValue(value: Float): String =
        if (value % 1f == 0f) value.toInt().toString() else value.toString()
}

private fun String.sanitizeForFileName(): String = replace(Regex("[^A-Za-z0-9_-]"), "_")
