package com.simonecompany.lavagna.board

import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.compose.ui.graphics.Color
import com.simonecompany.lavagna.model.BoardObject
import com.simonecompany.lavagna.model.Bounds
import com.simonecompany.lavagna.model.PathObject
import com.simonecompany.lavagna.model.Point
import com.simonecompany.lavagna.model.ShapeObject
import com.simonecompany.lavagna.model.ShapeType
import com.simonecompany.lavagna.model.TextObject
import com.simonecompany.lavagna.model.regularSides
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt

/** Handle di ridimensionamento della cornice di selezione. */
enum class Handle { TL, TR, BR, BL }

/**
 * Traduzione 1:1 della funzione drawShapeGeometry() della web app:
 * costruisce il tracciato nativo Android per ogni forma.
 */
fun buildShapePath(type: ShapeType, x1: Float, y1: Float, w: Float, h: Float, out: Path) {
    val x2 = x1 + w
    val y2 = y1 + h
    val r = sqrt(w * w + h * h) / 2f
    val cx = x1 + w / 2f
    val cy = y1 + h / 2f
    when (type) {
        ShapeType.RECT -> out.addRect(x1, y1, x2, y2, Path.Direction.CW)
        ShapeType.SQUARE -> {
            val s = abs(w)
            out.addRect(x1, y1, x1 + s, y1 + s, Path.Direction.CW)
        }
        ShapeType.CIRCLE -> out.addCircle(cx, cy, abs(w / 2f), Path.Direction.CW)
        ShapeType.ELLIPSE -> out.addOval(
            RectF(cx - abs(w / 2f), cy - abs(h / 2f), cx + abs(w / 2f), cy + abs(h / 2f)),
            Path.Direction.CW
        )
        ShapeType.LINE -> {
            out.moveTo(x1, y1)
            out.lineTo(x2, y2)
        }
        ShapeType.ARROW -> {
            val a = atan2(h, w)
            val hl = 15f
            out.moveTo(x1, y1)
            out.lineTo(x2, y2)
            out.lineTo(x2 - hl * cos(a - Math.PI / 6.0).toFloat(), y2 - hl * sin(a - Math.PI / 6.0).toFloat())
            out.moveTo(x2, y2)
            out.lineTo(x2 - hl * cos(a + Math.PI / 6.0).toFloat(), y2 - hl * sin(a + Math.PI / 6.0).toFloat())
        }
        ShapeType.TRIANGLE -> {
            out.moveTo(cx, y1)
            out.lineTo(x2, y2)
            out.lineTo(x1, y2)
            out.close()
        }
        ShapeType.RIGHT_TRIANGLE -> {
            out.moveTo(x1, y1)
            out.lineTo(x1, y2)
            out.lineTo(x2, y2)
            out.close()
        }
        ShapeType.STAR -> starPath(cx, cy, 5, r / 2f, r, out)
        ShapeType.STAR4 -> starPath(cx, cy, 4, r / 2f, r, out)
        ShapeType.STAR6 -> starPath(cx, cy, 6, r / 2f, r, out)
        ShapeType.HEART -> {
            out.moveTo(cx, y1 + h * .3f)
            out.cubicTo(cx, y1, x1, y1, x1, y1 + h * .4f)
            out.cubicTo(x1, y1 + h * .75f, cx, y2, cx, y2)
            out.cubicTo(cx, y2, x2, y1 + h * .75f, x2, y1 + h * .4f)
            out.cubicTo(x2, y1, cx, y1, cx, y1 + h * .3f)
        }
        ShapeType.DIAMOND -> {
            out.moveTo(cx, y1)
            out.lineTo(x2, cy)
            out.lineTo(cx, y2)
            out.lineTo(x1, cy)
            out.close()
        }
        ShapeType.PARALLELOGRAM -> {
            out.moveTo(x1 + w * .2f, y1)
            out.lineTo(x2, y1)
            out.lineTo(x2 - w * .2f, y2)
            out.lineTo(x1, y2)
            out.close()
        }
        ShapeType.TRAPEZOID -> {
            out.moveTo(x1 + w * .2f, y1)
            out.lineTo(x2 - w * .2f, y1)
            out.lineTo(x2, y2)
            out.lineTo(x1, y2)
            out.close()
        }
        ShapeType.CROSS -> {
            val th = abs(w) * .3f
            out.addRect(cx - th / 2f, y1, cx + th / 2f, y2, Path.Direction.CW)
            out.addRect(x1, cy - th / 2f, x2, cy + th / 2f, Path.Direction.CW)
        }
        ShapeType.CUBE, ShapeType.CUBOID -> {
            val d = minOf(abs(w), abs(h)) * .3f
            val fw = w - d
            val fh = h - d
            val fx = x1
            val fy = y1 + d
            val bx = x1 + d
            val by = y1
            out.addRect(fx, fy, fx + fw, fy + fh, Path.Direction.CW)
            out.addRect(bx, by, bx + fw, by + fh, Path.Direction.CW)
            out.moveTo(fx, fy); out.lineTo(bx, by)
            out.moveTo(fx + fw, fy); out.lineTo(bx + fw, by)
            out.moveTo(fx + fw, fy + fh); out.lineTo(bx + fw, by + fh)
            out.moveTo(fx, fy + fh); out.lineTo(bx, by + fh)
        }
        ShapeType.CYLINDER -> {
            val ry = abs(h) * .15f
            val rx = abs(w / 2f)
            out.addOval(RectF(cx - rx, y1 + ry - ry, cx + rx, y1 + ry + ry), Path.Direction.CW)
            out.arcTo(RectF(cx - rx, y2 - ry - ry, cx + rx, y2 - ry + ry), 0f, 180f)
            out.moveTo(x1, y1 + ry); out.lineTo(x1, y2 - ry)
            out.moveTo(x2, y1 + ry); out.lineTo(x2, y2 - ry)
        }
        ShapeType.CONE -> {
            val ry = abs(h) * .15f
            val rx = abs(w / 2f)
            out.addOval(RectF(cx - rx, y2 - ry - ry, cx + rx, y2 - ry + ry), Path.Direction.CW)
            out.moveTo(cx, y1); out.lineTo(x1, y2 - ry)
            out.moveTo(cx, y1); out.lineTo(x2, y2 - ry)
        }
        ShapeType.PYRAMID -> {
            out.moveTo(cx, y1)
            out.lineTo(x1, y2)
            out.lineTo(x2, y2)
            out.close()
            out.moveTo(cx, y1); out.lineTo(cx + w * .1f, y2)
        }
        ShapeType.SPHERE -> {
            val rr = minOf(abs(w), abs(h)) / 2f
            out.addCircle(cx, cy, rr, Path.Direction.CW)
            out.addOval(RectF(cx - rr, cy - rr / 3f, cx + rr, cy + rr / 3f), Path.Direction.CW)
        }
        else -> {
            val sides = type.regularSides()
            if (sides != null) regularPolyPath(cx, cy, sides, r, out)
        }
    }
}

private fun starPath(cx: Float, cy: Float, points: Int, inner: Float, outer: Float, out: Path) {
    var rot = Math.PI / 2.0 * 3.0
    val step = Math.PI / points
    out.moveTo(cx, cy - outer)
    repeat(points) {
        out.lineTo(cx + (cos(rot) * outer).toFloat(), cy + (sin(rot) * outer).toFloat())
        rot += step
        out.lineTo(cx + (cos(rot) * inner).toFloat(), cy + (sin(rot) * inner).toFloat())
        rot += step
    }
    out.close()
}

private fun regularPolyPath(cx: Float, cy: Float, sides: Int, r: Float, out: Path) {
    out.moveTo(cx + r * cos(0.0).toFloat(), cy + r * sin(0.0).toFloat())
    for (i in 1..sides) {
        val a = i * 2.0 * Math.PI / sides
        out.lineTo(cx + (r * cos(a)).toFloat(), cy + (r * sin(a)).toFloat())
    }
    out.close()
}

/** Tracciato completo di un oggetto (usato dal renderer e dall'esportazione). */
fun objectPath(obj: BoardObject, out: Path) {
    when (obj) {
        is PathObject -> {
            if (obj.points.size < 2) return
            out.moveTo(obj.points[0].x, obj.points[0].y)
            for (i in 1 until obj.points.size) out.lineTo(obj.points[i].x, obj.points[i].y)
            if (obj.filled) out.close()
        }
        is ShapeObject -> buildShapePath(obj.shapeType, obj.x, obj.y, obj.w, obj.h, out)
        is TextObject -> Unit
    }
}

/** Bounding box dell'oggetto (getObjectBounds della web app). */
fun boundsOf(obj: BoardObject): Bounds = when (obj) {
    is TextObject -> Bounds(obj.x, obj.y, obj.w, obj.h)
    is ShapeObject -> Bounds(
        minOf(obj.x, obj.x + obj.w),
        minOf(obj.y, obj.y + obj.h),
        abs(obj.w),
        abs(obj.h)
    )
    is PathObject -> {
        if (obj.points.isEmpty()) Bounds(0f, 0f, 0f, 0f)
        else {
            var minX = Float.MAX_VALUE; var minY = Float.MAX_VALUE
            var maxX = -Float.MAX_VALUE; var maxY = -Float.MAX_VALUE
            for (p in obj.points) {
                if (p.x < minX) minX = p.x
                if (p.y < minY) minY = p.y
                if (p.x > maxX) maxX = p.x
                if (p.y > maxY) maxY = p.y
            }
            Bounds(minX, minY, maxX - minX, maxY - minY)
        }
    }
}

/** Posizione dei 4 handle (+5px come nella web app). */
fun handlePositions(b: Bounds): List<Pair<Handle, Point>> = listOf(
    Handle.TL to Point(b.x - 5f, b.y - 5f),
    Handle.TR to Point(b.x + b.w + 5f, b.y - 5f),
    Handle.BR to Point(b.x + b.w + 5f, b.y + b.h + 5f),
    Handle.BL to Point(b.x - 5f, b.y + b.h + 5f),
)

fun handleAt(b: Bounds, p: Point, threshold: Float): Handle? =
    handlePositions(b).firstOrNull { hypot(it.second.x - p.x, it.second.y - p.y) <= threshold }?.first

fun distToSegment(px: Float, py: Float, x1: Float, y1: Float, x2: Float, y2: Float): Float {
    val dx = x2 - x1
    val dy = y2 - y1
    val l2 = dx * dx + dy * dy
    var t = if (l2 == 0f) 0f else ((px - x1) * dx + (py - y1) * dy) / l2
    t = t.coerceIn(0f, 1f)
    return hypot(px - (x1 + t * dx), py - (y1 + t * dy))
}

fun hitTestPath(obj: PathObject, px: Float, py: Float, thickness: Float): Boolean {
    for (i in 0 until obj.points.size - 1) {
        val a = obj.points[i]
        val b = obj.points[i + 1]
        if (distToSegment(px, py, a.x, a.y, b.x, b.y) <= thickness) return true
    }
    return false
}

fun hitTestBounds(obj: BoardObject, px: Float, py: Float, thickness: Float): Boolean =
    boundsOf(obj).containsPoint(px, py, thickness)

fun pointInPolygon(x: Float, y: Float, poly: List<Point>): Boolean {
    var inside = false
    var j = poly.size - 1
    for (i in poly.indices) {
        val xi = poly[i].x; val yi = poly[i].y
        val xj = poly[j].x; val yj = poly[j].y
        if ((yi > y) != (yj > y) && x < (xj - xi) * (y - yi) / (yj - yi) + xi) inside = !inside
        j = i
    }
    return inside
}

/** objectTouchesLasso(): l'oggetto e' "preso" dal lazo. */
fun objectTouchesLasso(obj: BoardObject, lasso: List<Point>): Boolean {
    if (lasso.size < 3) return false
    val b = boundsOf(obj)
    for (p in lasso) if (p.x >= b.x - 1 && p.x <= b.right + 1 && p.y >= b.y - 1 && p.y <= b.bottom + 1) return true
    val samples = 10
    for (i in 0..samples) {
        val t = i / samples.toFloat()
        if (pointInPolygon(b.x + b.w * t, b.y, lasso)) return true
        if (pointInPolygon(b.x + b.w * t, b.bottom, lasso)) return true
        if (pointInPolygon(b.x, b.y + b.h * t, lasso)) return true
        if (pointInPolygon(b.right, b.y + b.h * t, lasso)) return true
    }
    if (obj is PathObject) for (pt in obj.points) if (pointInPolygon(pt.x, pt.y, lasso)) return true
    return false
}

/** Test di riempimento: il punto e' dentro un tracciato chiuso? */
fun pathContainsPoint(obj: PathObject, px: Float, py: Float): Boolean {
    if (obj.points.size < 3) return false
    return pointInPolygon(px, py, obj.points)
}

/** wrapTextCtx(): va a capo sulle parole. */
fun wrapText(paint: Paint, text: String, maxWidth: Float): List<String> {
    val words = text.split(Regex("\\s+")).filter { it.isNotEmpty() }
    if (words.isEmpty()) return emptyList()
    val lines = ArrayList<String>()
    var current = ""
    for (w in words) {
        val test = if (current.isEmpty()) w else "$current $w"
        if (paint.measureText(test) > maxWidth && current.isNotEmpty()) {
            lines.add(current)
            current = w
        } else {
            current = test
        }
    }
    if (current.isNotEmpty()) lines.add(current)
    return lines
}

/** Colore di default del pennello. */
val DefaultInkColor: Color = Color(0xFF0B57D0)