package com.simonecompany.lavagna.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/** Strumenti della lavagna (corrispondono a currentTool della web app). */
enum class Tool { POINTER, PEN, ERASER, SHAPE, TEXT, FILL, BACKGROUND }

/** Modalita' della gomma. */
enum class EraserMode { NORMAL, STROKE, LASSO }

/** Modalita' del puntatore. */
enum class PointerMode { NORMAL, LASSO }

/** Allineamento del testo. */
enum class TextAlign { LEFT, CENTER, RIGHT }

/**
 * Tutte le forme della web app, nello stesso ordine dei menu'.
 * [key] e' la chiave usata dall'SVG/JS originale, [threeD] raggruppa il menu' "Figure 3D".
 */
enum class ShapeType(val key: String, val threeD: Boolean = false) {
    RECT("rect"),
    SQUARE("square"),
    CIRCLE("circle"),
    ELLIPSE("ellipse"),
    LINE("line"),
    ARROW("arrow"),
    TRIANGLE("triangle"),
    RIGHT_TRIANGLE("rightTriangle"),
    STAR("star"),
    STAR4("star4"),
    STAR6("star6"),
    HEART("heart"),
    DIAMOND("diamond"),
    PARALLELOGRAM("parallelogram"),
    TRAPEZOID("trapezoid"),
    PENTAGON("pentagon"),
    HEXAGON("hexagon"),
    HEPTAGON("heptagon"),
    OCTAGON("octagon"),
    NONAGON("nonagon"),
    DECAGON("decagon"),
    CROSS("cross"),
    CUBE("cube", true),
    CUBOID("cuboid", true),
    CYLINDER("cylinder", true),
    CONE("cone", true),
    PYRAMID("pyramid", true),
    SPHERE("sphere", true);

    /** Chiave i18n dell'etichetta ("shape_rect", ...). */
    val labelKey: String get() = "shape_$key"

    companion object {
        val shapes2D: List<ShapeType> = entries.filter { !it.threeD }
        val shapes3D: List<ShapeType> = entries.filter { it.threeD }
        fun fromKey(key: String): ShapeType = entries.firstOrNull { it.key == key } ?: RECT
    }
}

/** Numero di lati per i poligoni regolari. */
fun ShapeType.regularSides(): Int? = when (this) {
    ShapeType.PENTAGON -> 5
    ShapeType.HEXAGON -> 6
    ShapeType.HEPTAGON -> 7
    ShapeType.OCTAGON -> 8
    ShapeType.NONAGON -> 9
    ShapeType.DECAGON -> 10
    else -> null
}

/** Quadrato e cerchio mantengono le proporzioni durante il ridimensionamento. */
fun ShapeType.isAspectLocked(): Boolean = this == ShapeType.SQUARE || this == ShapeType.CIRCLE

@Immutable
data class Point(val x: Float, val y: Float)

@Immutable
data class Bounds(val x: Float, val y: Float, val w: Float, val h: Float) {
    val right: Float get() = x + w
    val bottom: Float get() = y + h
    val centerX: Float get() = x + w / 2f
    val centerY: Float get() = y + h / 2f

    fun containsPoint(px: Float, py: Float, margin: Float = 0f): Boolean =
        px >= x - margin && px <= right + margin && py >= y - margin && py <= bottom + margin
}

/** Un oggetto disegnato sulla lavagna. */
@Immutable
sealed class BoardObject {
    abstract val color: Color
    abstract val strokeWidth: Float
    abstract val flipX: Boolean
    abstract val flipY: Boolean

    /** Lassi usati per "tagliare" l'oggetto (gomma a lazo). */
    abstract val clips: List<List<Point>>

    fun translated(dx: Float, dy: Float): BoardObject = when (this) {
        is PathObject -> copy(points = points.map { Point(it.x + dx, it.y + dy) })
        is ShapeObject -> copy(x = x + dx, y = y + dy)
        is TextObject -> copy(x = x + dx, y = y + dy)
    }

    fun withClips(extra: List<Point>): BoardObject = when (this) {
        is PathObject -> copy(clips = clips + listOf(extra))
        is ShapeObject -> copy(clips = clips + listOf(extra))
        is TextObject -> copy(clips = clips + listOf(extra))
    }
}

/** Tratto di penna (o di gomma normale). */
@Immutable
data class PathObject(
    val points: List<Point>,
    override val color: Color,
    override val strokeWidth: Float,
    val filled: Boolean = false,
    val fillColor: Color? = null,
    override val flipX: Boolean = false,
    override val flipY: Boolean = false,
    override val clips: List<List<Point>> = emptyList(),
) : BoardObject()

/** Forma geometrica. */
@Immutable
data class ShapeObject(
    val shapeType: ShapeType,
    val x: Float,
    val y: Float,
    val w: Float,
    val h: Float,
    override val color: Color,
    override val strokeWidth: Float,
    override val flipX: Boolean = false,
    override val flipY: Boolean = false,
    override val clips: List<List<Point>> = emptyList(),
) : BoardObject()

/** Blocco di testo. */
@Immutable
data class TextObject(
    val text: String,
    val x: Float,
    val y: Float,
    val w: Float,
    val h: Float,
    override val color: Color,
    val font: String,
    val fontSize: Float,
    val bold: Boolean,
    val italic: Boolean,
    val align: TextAlign,
    override val strokeWidth: Float = 1f,
    override val flipX: Boolean = false,
    override val flipY: Boolean = false,
    override val clips: List<List<Point>> = emptyList(),
) : BoardObject()

/** Palette predefinita della web app. */
val DefaultColors: List<Color> = listOf(
    Color(0xFF000000), Color(0xFF444746), Color(0xFF808080), Color(0xFFBDBDBD),
    Color(0xFFE0E0E0), Color(0xFFF5F5F5), Color(0xFFFFFFFF), Color(0xFF7F1D1D),
    Color(0xFFB3261E), Color(0xFFDC2626), Color(0xFFF97316), Color(0xFFF59E0B),
    Color(0xFFFACC15), Color(0xFFA3E635), Color(0xFF4ADE80), Color(0xFF22D3EE),
    Color(0xFF3B82F6), Color(0xFF1D4ED8), Color(0xFF7C3AED), Color(0xFFC026D3),
    Color(0xFFDB2777),
)

/** Colore attivo della lavagna, come nella web app. */
val AccentBlue: Color = Color(0xFF0B57D0)

/** Rosso usato per la selezione multipla e il lazo della gomma. */
val AccentRed: Color = Color(0xFFD32F2F)