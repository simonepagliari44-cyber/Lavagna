package com.simonecompany.lavagna.board

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.simonecompany.lavagna.model.AccentBlue
import com.simonecompany.lavagna.model.AccentRed
import com.simonecompany.lavagna.model.BackgroundType
import com.simonecompany.lavagna.model.BoardObject
import com.simonecompany.lavagna.model.PatternKind
import com.simonecompany.lavagna.model.backgroundById
import com.simonecompany.lavagna.model.Bounds
import com.simonecompany.lavagna.model.PathObject
import com.simonecompany.lavagna.model.Point
import com.simonecompany.lavagna.model.ShapeObject
import com.simonecompany.lavagna.model.TextAlign
import com.simonecompany.lavagna.model.TextObject

/** Stato necessario per disegnare un fotogramma della lavagna. */
data class BoardRenderState(
    val background: Color,
    val backgroundId: String = "solid_white",
    val objects: List<BoardObject>,
    val livePoints: List<Point> = emptyList(),
    val liveColor: Color = Color.Unspecified,
    val liveWidth: Float = 0f,
    val tempShape: ShapeObject? = null,
    val selectedIndex: Int = -1,
    val multiSelection: Set<Int> = emptySet(),
    val showSelection: Boolean = false,
    val lasso: List<Point> = emptyList(),
    val lassoIsSelection: Boolean = false,
    val scale: Float = 1f,
    val offset: Offset = Offset.Zero,
    val zoomMode: Boolean = false,
    val viewport: Size = Size.Zero,
)

/**
 * Renderer della lavagna: porting di renderObject()/redrawPage() della web app
 * sulle API native android.graphics.
 */
class BoardRenderer {

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val patternPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        strokeWidth = 1f
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private val clip = Path()

    private fun drawBackground(canvas: Canvas, s: BoardRenderState) {
        val w = if (s.viewport.width > 0) s.viewport.width else canvas.width.toFloat()
        val h = if (s.viewport.height > 0) s.viewport.height else canvas.height.toFloat()
        if (w <= 0f || h <= 0f) return
        val bg = if (s.backgroundId.isNotEmpty() && s.backgroundId != "none") backgroundById(s.backgroundId) else null
        if (bg == null) {
            canvas.drawColor(s.background.toArgb())
            return
        }
        when (bg.type) {
            BackgroundType.SOLID -> canvas.drawColor(bg.solid.toArgb())
            BackgroundType.GRADIENT -> {
                val shaderPaint = paint
                shaderPaint.shader = null
                val rad = Math.toRadians(bg.angle.toDouble())
                val len = (Math.abs(Math.cos(rad)) * w + Math.abs(Math.sin(rad)) * h).toFloat()
                val dx = (Math.cos(rad) * len).toFloat()
                val dy = (Math.sin(rad) * len).toFloat()
                shaderPaint.shader = LinearGradient(
                    (w - dx) / 2f, (h - dy) / 2f,
                    (w + dx) / 2f, (h + dy) / 2f,
                    bg.startColor.toArgb(), bg.endColor.toArgb(),
                    Shader.TileMode.CLAMP,
                )
                canvas.drawRect(0f, 0f, w, h, shaderPaint)
                shaderPaint.shader = null
            }
            BackgroundType.PATTERN -> {
                canvas.drawColor(Color.White.toArgb())
                val p = bg.pattern ?: return
                patternPaint.shader = null
                patternPaint.color = p.color.toArgb()
                patternPaint.strokeWidth = p.width
                when (p.kind) {
                    PatternKind.LINES -> {
                        var y = p.gap
                        while (y < h) {
                            canvas.drawLine(0f, y, w, y, patternPaint)
                            y += p.gap
                        }
                    }
                    PatternKind.GRID -> {
                        var x = 0f
                        while (x < w) {
                            canvas.drawLine(x, 0f, x, h, patternPaint)
                            x += p.size
                        }
                        var y = 0f
                        while (y < h) {
                            canvas.drawLine(0f, y, w, y, patternPaint)
                            y += p.size
                        }
                    }
                    PatternKind.DOTS -> {
                        var x = p.size / 2f
                        while (x < w) {
                            var y = p.size / 2f
                            while (y < h) {
                                canvas.drawCircle(x, y, p.radius, patternPaint)
                                y += p.size
                            }
                            x += p.size
                        }
                    }
                }
            }
        }
    }

    /** Disegna l'intera scena (usato dal Canvas Compose). */
    fun render(canvas: Canvas, s: BoardRenderState) {
        drawBackground(canvas, s)
        canvas.save()
        canvas.translate(s.offset.x, s.offset.y)
        canvas.scale(s.scale, s.scale)

        for ((index, obj) in s.objects.withIndex()) {
            drawObject(canvas, obj)
            if (s.showSelection &&
                (index == s.selectedIndex || index in s.multiSelection)
            ) {
                drawSelection(canvas, obj, index in s.multiSelection, s.scale)
            }
        }
        s.livePoints.takeIf { it.size >= 2 }?.let { pts ->
            path.rewind()
            path.moveTo(pts[0].x, pts[0].y)
            for (i in 1 until pts.size) path.lineTo(pts[i].x, pts[i].y)
            strokePaint.style = Paint.Style.STROKE
            strokePaint.strokeWidth = s.liveWidth
            strokePaint.color = s.liveColor.toArgb()
            strokePaint.pathEffect = null
            canvas.drawPath(path, strokePaint)
        }
        s.tempShape?.let { drawObject(canvas, it) }

        if (s.lasso.size > 1) {
            path.rewind()
            path.moveTo(s.lasso[0].x, s.lasso[0].y)
            for (i in 1 until s.lasso.size) path.lineTo(s.lasso[i].x, s.lasso[i].y)
            path.close()
            val c = if (s.lassoIsSelection) AccentBlue else AccentRed
            strokePaint.style = Paint.Style.STROKE
            strokePaint.color = c.toArgb()
            strokePaint.strokeWidth = 2f / s.scale
            strokePaint.pathEffect = DashPathEffect(floatArrayOf(6f / s.scale, 4f / s.scale), 0f)
            canvas.drawPath(path, strokePaint)
            fillPaint.color = c.copy(alpha = if (s.lassoIsSelection) 0.10f else 0.08f).toArgb()
            fillPaint.style = Paint.Style.FILL
            canvas.drawPath(path, fillPaint)
            strokePaint.pathEffect = null
        }
        canvas.restore()

        if (s.zoomMode) {
            strokePaint.style = Paint.Style.STROKE
            strokePaint.color = AccentBlue.copy(alpha = 0.4f).toArgb()
            strokePaint.strokeWidth = 2f
            strokePaint.pathEffect = DashPathEffect(floatArrayOf(8f, 6f), 0f)
            canvas.drawRect(
                s.offset.x, s.offset.y,
                s.offset.x + s.viewport.width * s.scale,
                s.offset.y + s.viewport.height * s.scale,
                strokePaint
            )
            strokePaint.pathEffect = null
        }
    }

    /** Solo gli oggetti: usato dall'esportazione PNG/JPG/PDF. */
    fun renderObjects(canvas: Canvas, objects: List<BoardObject>) {
        for (obj in objects) drawObject(canvas, obj)
    }

    private fun drawObject(canvas: Canvas, obj: BoardObject) {
        canvas.save()

        if (obj.clips.isNotEmpty()) {
            clip.rewind()
            clip.fillType = Path.FillType.EVEN_ODD
            clip.addRect(-100000f, -100000f, 100000f, 100000f, Path.Direction.CW)
            for (lasso in obj.clips) {
                if (lasso.size < 3) continue
                clip.moveTo(lasso[0].x, lasso[0].y)
                for (i in 1 until lasso.size) clip.lineTo(lasso[i].x, lasso[i].y)
                clip.close()
            }
            canvas.clipPath(clip)
        }

        if (obj.flipX || obj.flipY) {
            val b = boundsOf(obj)
            canvas.translate(b.centerX, b.centerY)
            canvas.scale(if (obj.flipX) -1f else 1f, if (obj.flipY) -1f else 1f)
            canvas.translate(-b.centerX, -b.centerY)
        }

        when (obj) {
            is PathObject -> drawPath(canvas, obj)
            is ShapeObject -> {
                if (obj.strokeWidth > 0f) {
                    path.rewind()
                    buildShapePath(obj.shapeType, obj.x, obj.y, obj.w, obj.h, path)
                    strokePaint.style = Paint.Style.STROKE
                    strokePaint.strokeWidth = obj.strokeWidth
                    strokePaint.color = obj.color.toArgb()
                    strokePaint.pathEffect = null
                    canvas.drawPath(path, strokePaint)
                }
            }
            is TextObject -> drawText(canvas, obj)
        }
        canvas.restore()
    }

    private fun drawPath(canvas: Canvas, obj: PathObject) {
        if (obj.points.size < 2 || obj.strokeWidth <= 0f) return
        path.rewind()
        path.moveTo(obj.points[0].x, obj.points[0].y)
        for (i in 1 until obj.points.size) path.lineTo(obj.points[i].x, obj.points[i].y)
        if (obj.filled) {
            path.close()
            fillPaint.style = Paint.Style.FILL
            fillPaint.color = (obj.fillColor ?: obj.color).toArgb()
            canvas.drawPath(path, fillPaint)
        }
        strokePaint.style = Paint.Style.STROKE
        strokePaint.strokeWidth = obj.strokeWidth
        strokePaint.color = obj.color.toArgb()
        strokePaint.pathEffect = null
        canvas.drawPath(path, strokePaint)
    }

    private fun drawText(canvas: Canvas, obj: TextObject) {
        textPaint.isAntiAlias = true
        textPaint.typeface = Fonts.typeface(obj.font, obj.bold, obj.italic)
        textPaint.textSize = obj.fontSize
        textPaint.color = obj.color.toArgb()
        textPaint.textAlign = when (obj.align) {
            TextAlign.LEFT -> Paint.Align.LEFT
            TextAlign.CENTER -> Paint.Align.CENTER
            TextAlign.RIGHT -> Paint.Align.RIGHT
        }
        val maxWidth = if (obj.w > 0f) obj.w else 1000f
        val lines = wrapText(textPaint, obj.text, maxWidth)
        val lineHeight = obj.fontSize * 1.2f
        val ascent = textPaint.fontMetrics.ascent
        var ty = obj.y
        for (line in lines) {
            val tx = when (obj.align) {
                TextAlign.LEFT -> obj.x
                TextAlign.CENTER -> obj.x + obj.w / 2f
                TextAlign.RIGHT -> obj.x + obj.w
            }
            canvas.drawText(line, tx, ty - ascent, textPaint)
            ty += lineHeight
        }
    }

    private fun drawSelection(canvas: Canvas, obj: BoardObject, isMulti: Boolean, scale: Float) {
        val b: Bounds = boundsOf(obj)
        val pad = 5f
        strokePaint.style = Paint.Style.STROKE
        strokePaint.color = (if (isMulti) AccentRed else AccentBlue).toArgb()
        strokePaint.strokeWidth = 1.5f / scale
        strokePaint.pathEffect = DashPathEffect(floatArrayOf(6f / scale, 4f / scale), 0f)
        canvas.drawRect(b.x - pad, b.y - pad, b.right + pad, b.bottom + pad, strokePaint)
        strokePaint.pathEffect = null
        if (isMulti) return
        fillPaint.style = Paint.Style.FILL
        fillPaint.color = android.graphics.Color.WHITE
        strokePaint.color = AccentBlue.toArgb()
        strokePaint.strokeWidth = 2f / scale
        val half = 4f / scale
        for ((_, p) in handlePositions(b)) {
            canvas.drawRect(p.x - half, p.y - half, p.x + half, p.y + half, fillPaint)
            canvas.drawRect(p.x - half, p.y - half, p.x + half, p.y + half, strokePaint)
        }
    }
}