package com.sportall.az.export

import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.sportall.az.androidContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

actual class PdfExporter actual constructor() {

    private val context get() = androidContext

    @RequiresApi(Build.VERSION_CODES.Q)
    actual suspend fun export(payload: ExportPayload, fileName: String): ExportResult =
        withContext(Dispatchers.IO) {
            try {
                val doc = android.graphics.pdf.PdfDocument()
                val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
                val page = doc.startPage(pageInfo)
                val c = page.canvas

                c.drawColor(Color.WHITE)

                val title = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.BLACK
                    textSize = 20f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                }
                val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.BLACK
                    textSize = 12f
                }

                var y = 60f
                val x = 48f

                c.drawText("Exported: ${payload.generatedAt}", x, y, title)
                y += 40f

                c.drawText("Favorites:", x, y, title)
                y += 24f
                payload.favorites.forEach {
                    c.drawText("- ID: $it", x, y, text)
                    y += 20f
                }

                y += 24f
                c.drawText("History:", x, y, title)
                y += 24f
                payload.history.forEach { record ->
                    val dateStr = formatDate(record.date)
                    val starsStr = record.stars?.toString() ?: "—"

                    c.drawText("- $dateStr • Drill ID: ${record.drillId} • Stars: $starsStr", x, y, text)
                    y += 20f
                }

                doc.finishPage(page)

                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/SportallAZ")
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    ?: return@withContext ExportResult.Error("Cannot create file in Downloads")

                resolver.openOutputStream(uri)?.use { out ->
                    doc.writeTo(out)
                } ?: return@withContext ExportResult.Error("Cannot open output stream")

                doc.close()

                ExportResult.Ok(uri.toString())
            } catch (t: Throwable) {
                ExportResult.Error("Export failed: ${t.message}")
            }
        }
}

actual class ExportViewer actual constructor() {

    private val context get() = androidContext

    actual fun view(location: String) {
        val uri = Uri.parse(location)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Share PDF")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        context.startActivity(chooser)
    }
}

private fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(date)
}