package com.simonecompany.lavagna.export

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.compose.ui.graphics.toArgb
import com.simonecompany.lavagna.board.BoardRenderer
import com.simonecompany.lavagna.vm.BoardViewModel
import java.io.OutputStream
import kotlin.math.max

/**
 * Esportazione della pagina corrente. La web app salva la canvas alla
 * risoluzione corrente; qui si genera un Bitmap identico e si scrive con le
 * API native (Storage Access Framework), senza permessi.
 */
class BoardExporter(private val renderer: BoardRenderer) {

    fun render(vm: BoardViewModel, width: Int, height: Int): Bitmap {
        val w = max(1, width)
        val h = max(1, height)
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(vm.backgroundColor.toArgb())
        canvas.save()
        canvas.translate(vm.offset.x, vm.offset.y)
        canvas.scale(vm.scale, vm.scale)
        renderer.renderObjects(canvas, vm.objects())
        canvas.restore()
        return bitmap
    }

    fun writeImage(context: Context, uri: Uri, bitmap: Bitmap, format: ImageFormat): Boolean {
        val stream: OutputStream = context.contentResolver.openOutputStream(uri) ?: return false
        val ok = stream.use {
            format.compress(bitmap, it)
        }
        bitmap.recycle()
        return ok
    }

    fun writePdf(context: Context, uri: Uri, bitmap: Bitmap): Boolean {
        val document = PdfDocument()
        val info = PdfDocument.PageInfo.Builder(max(1, bitmap.width), max(1, bitmap.height), 1).create()
        val page = document.startPage(info)
        page.canvas.drawBitmap(bitmap, 0f, 0f, null)
        document.finishPage(page)
        val stream = context.contentResolver.openOutputStream(uri)
        val ok = stream?.use { document.writeTo(it) } != null
        document.close()
        bitmap.recycle()
        return ok
    }
}

enum class ImageFormat(val mimeType: String, val extension: String) {
    PNG("image/png", "png"),
    JPG("image/jpeg", "jpg");

    fun compress(bitmap: Bitmap, stream: OutputStream): Boolean =
        bitmap.compress(
            if (this == PNG) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG,
            if (this == PNG) 100 else 95,
            stream
        )
}