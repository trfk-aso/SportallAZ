package com.sportall.az.export

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.DrawableResource
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSLocale
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.currentLocale
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.writeToURL
import platform.UIKit.NSFontAttributeName
import platform.UIKit.NSForegroundColorAttributeName
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIColor
import platform.UIKit.UIFont
import platform.UIKit.UIGraphicsPDFRenderer
import platform.UIKit.UIGraphicsPDFRendererFormat
import platform.UIKit.UIImage
import platform.UIKit.UINavigationController
import platform.UIKit.UITabBarController
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.drawAtPoint
import platform.UIKit.popoverPresentationController

actual class PdfExporter actual constructor() {

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun export(payload: ExportPayload, fileName: String): ExportResult =
        withContext(Dispatchers.Default) {

            try {
                val fm = NSFileManager.defaultManager

                val urls = fm.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask)
                val docsUrl = (urls?.get(0) as? NSURL)
                    ?: return@withContext ExportResult.Error("No Documents folder")

                val fileUrl = docsUrl.URLByAppendingPathComponent("$fileName.pdf")
                    ?: return@withContext ExportResult.Error("Failed to build file URL")

                val bounds = CGRectMake(0.0, 0.0, 595.0, 842.0)
                val renderer = UIGraphicsPDFRenderer(bounds, UIGraphicsPDFRendererFormat())

                val data = renderer.PDFDataWithActions { ctx ->

                    ctx?.beginPage() ?: return@PDFDataWithActions

                    fun draw(text: String, x: Double, y: Double, size: Double = 14.0) {
                        val attrs = mapOf<Any?, Any?>(
                            NSFontAttributeName to UIFont.systemFontOfSize(size),
                            NSForegroundColorAttributeName to UIColor.blackColor
                        )
                        (text as NSString).drawAtPoint(CGPointMake(x, y), attrs)
                    }

                    var y = 40.0
                    val x = 30.0

                    draw("Exported: ${payload.generatedAt}", x, y, 18.0)
                    y += 30

                    draw("Favorites:", x, y, 16.0)
                    y += 20

                    payload.favorites.forEach {
                        draw("- ID: $it", x, y, 14.0)
                        y += 16
                    }

                    y += 20
                    draw("History:", x, y, 16.0)
                    y += 20

                    payload.history.forEach { record ->
                        val dateStr = formatDate(record.date)
                        val starsStr = record.stars?.toString() ?: "—"

                        draw(
                            "- $dateStr • Drill ID: ${record.drillId} • Stars: $starsStr",
                            x,
                            y,
                            12.0
                        )
                        y += 20
                    }
                }

                val ok = data.writeToURL(fileUrl, true)
                if (!ok) return@withContext ExportResult.Error("Failed to write PDF")

                ExportResult.Ok(fileUrl.path!!)
            } catch (e: Throwable) {
                ExportResult.Error("Export failed: ${e.message}")
            }
        }
}


actual class ExportViewer actual constructor() {

    @OptIn(ExperimentalForeignApi::class)
    actual fun view(location: String) {
        val url = NSURL.fileURLWithPath(location)
        val controller = UIActivityViewController(
            activityItems = listOf(url),
            applicationActivities = null
        )

        val root = topViewController() ?: return

        val pop = controller.popoverPresentationController
        pop?.sourceView = root.view
        pop?.sourceRect = CGRectMake(0.0, 0.0, 1.0, 1.0)

        root.presentViewController(controller, true, null)
    }
}

fun topViewController(): UIViewController? {
    val keyWindow = UIApplication.sharedApplication.keyWindow
        ?: UIApplication.sharedApplication.windows.firstOrNull() as? UIWindow
        ?: return null

    var top = keyWindow.rootViewController ?: return null

    while (top.presentedViewController != null) {
        top = top.presentedViewController!!
    }

    if (top is UINavigationController) {
        return top.visibleViewController
    }

    if (top is UITabBarController) {
        return top.selectedViewController
    }

    return top
}


fun formatDate(timestamp: Long): String {
    val date = NSDate.dateWithTimeIntervalSince1970(timestamp.toDouble() / 1000.0)

    val formatter = NSDateFormatter().apply {
        dateFormat = "yyyy-MM-dd"
        locale = NSLocale.currentLocale
    }

    return formatter.stringFromDate(date)
}