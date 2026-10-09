package com.simonecompany.lavagna.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FormatAlignCenter
import androidx.compose.material.icons.outlined.FormatAlignLeft
import androidx.compose.material.icons.outlined.FormatAlignRight
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatColorFill
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material.icons.outlined.ZoomIn
import androidx.compose.material.icons.outlined.ZoomOut
import androidx.compose.material.icons.outlined.ZoomOutMap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * Icone dell'app: i tracciati delle icone principali sono presi identici
 * dall'SVG inline di index.html, le altre sono le Material Symbols equivalenti.
 */
object LavagnaIcons {

    private fun svg(name: String, d: String) = buildVector(name, d, filled = true)

    private fun stroked(name: String, d: String, width: Float = 2f) =
        buildVector(name, d, filled = false, strokeWidth = width)

    private fun buildVector(
        name: String,
        d: String,
        filled: Boolean,
        strokeWidth: Float = 0f,
    ): ImageVector = ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString(d).toNodes(),
            fill = if (filled) SolidColor(Color.Black) else null,
            stroke = if (filled) null else SolidColor(Color.Black),
            strokeLineWidth = if (filled) 0f else strokeWidth,
            strokeLineCap = if (filled) StrokeCap.Butt else StrokeCap.Round,
            strokeLineJoin = if (filled) StrokeJoin.Miter else StrokeJoin.Round,
        )
    }.build()

    // --- icone riprese dall'SVG della web app
    val Pointer = svg("Pointer", "M7 2l12 11.2-5.8.5 3.3 7.3-2.2 1-3.2-7.4L7 19V2z")
    val Pen = svg(
        "Pen",
        "M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"
    )
    val Eraser = svg(
        "Eraser",
        "M15.14 3c-.51 0-1.02.2-1.41.59L2.59 14.73c-.78.78-.78 2.05 0 2.83L7.03 22h12.59c.55 0 1-.45 1-1s-.45-1-1-1h-6.17l8.28-8.28c.78-.78.78-2.05 0-2.83l-5.18-5.18c-.39-.39-.9-.59-1.41-.59z"
    )
    val Shapes = svg(
        "Shapes",
        "M12 2l-5.5 9h11zM4 14.5h7v7H4zM17.5 14.5c-1.93 0-3.5 1.57-3.5 3.5s1.57 3.5 3.5 3.5 3.5-1.57 3.5-3.5-1.57-3.5-3.5-3.5z"
    )
    val Undo = svg(
        "Undo",
        "M12.5 8c-2.65 0-5.05.99-6.9 2.6L2 7v9h9l-3.62-3.62c1.39-1.16 3.16-1.88 5.12-1.88 3.54 0 6.55 2.31 7.6 5.5l2.37-.78C21.08 11.03 17.15 8 12.5 8z"
    )
    val Redo = svg(
        "Redo",
        "M18.4 10.6C16.55 8.99 14.15 8 11.5 8c-4.65 0-8.58 3.03-9.96 7.22L3.9 16c1.05-3.19 4.05-5.5 7.6-5.5 1.95 0 3.73.72 5.12 1.88L13 16h9V7l-3.6 3.6z"
    )
    val Delete = svg(
        "Delete",
        "M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"
    )
    val ChevronLeft = svg("ChevronLeft", "M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12z")
    val ChevronRight = svg("ChevronRight", "M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z")
    val Add = svg("Add", "M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z")
    val Pages = svg("Pages", "M3 6h18v2H3zm0 5h18v2H3zm0 5h18v2H3z")
    val Close = svg(
        "Close",
        "M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"
    )
    val Check = svg("Check", "M9 16.2L4.8 12l-1.4 1.4L9 19 21 7l-1.4-1.4z")
    val Document = svg("Document", "M6 2h9l5 5v13a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2zm8 1.5V8h4.5L14 3.5z")
    val Lasso = stroked("Lasso", "M4 4l4 4M20 4l-4 4M4 20l4-4M20 20l-4-4")
    val CircleOutline = stroked("CircleOutline", "M4 12a8 8 0 1 0 16 0a8 8 0 1 0-16 0")
    val StrokeLines = stroked("StrokeLines", "M3 12h18M3 6h18M3 18h18")

    // --- Material Symbols (stessi glifi usati dalla web app)
    val Text: ImageVector = Icons.Outlined.TextFields
    val Palette: ImageVector = Icons.Outlined.Palette
    val Fill: ImageVector = Icons.Outlined.FormatColorFill
    val Background: ImageVector = Icons.Outlined.Wallpaper
    val Backgrounds = svg(
        "Backgrounds",
        "M19 3H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V5a2 2 0 0 0-2-2zm0 16H5V5h14v14zM6 6h6v6H6zM12 12h6v6h-6z",
    )
    val Save: ImageVector = Icons.Outlined.Save
    val ZoomIn: ImageVector = Icons.Outlined.ZoomIn
    val ZoomOut: ImageVector = Icons.Outlined.ZoomOut
    val ZoomReset: ImageVector = Icons.Outlined.ZoomOutMap
    val Bold: ImageVector = Icons.Outlined.FormatBold
    val Italic: ImageVector = Icons.Outlined.FormatItalic
    val AlignLeft: ImageVector = Icons.Outlined.FormatAlignLeft
    val AlignCenter: ImageVector = Icons.Outlined.FormatAlignCenter
    val AlignRight: ImageVector = Icons.Outlined.FormatAlignRight
}

/** Mini-icona di una forma, per il menu forme (stessi tracciati del menu web). */
object ShapeIcons {
    private fun draw(name: String, d: String): ImageVector = ImageVector.Builder(
        name = name,
        defaultWidth = 18.dp,
        defaultHeight = 18.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString(d).toNodes(),
            stroke = SolidColor(Color(0xFF444746)),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        )
    }.build()

    private val paths = mapOf(
        "rect" to "M4 5h16v14H4z",
        "square" to "M4 4h16v16H4z",
        "circle" to "M12 3a9 9 0 1 0 0 18a9 9 0 1 0 0-18",
        "ellipse" to "M12 6a9 6 0 1 0 0 12a9 6 0 1 0 0-12",
        "line" to "M3 21L21 3",
        "arrow" to "M3 12h16M13 6l6 6-6 6",
        "triangle" to "M12 3L21 21H3z",
        "rightTriangle" to "M4 3v18h16z",
        "star" to "M12 2l3 7h7l-5.5 4.5L18 21l-6-4-6 4 1.5-7.5L2 9h7z",
        "star4" to "M12 2l2 8 8 2-8 2-2 8-2-8-8-2 8-2z",
        "star6" to "M12 2l3 6 6 0-4.5 4.5L18 20l-6-3-6 3 1.5-7.5L3 8h6z",
        "heart" to "M12 21.4l-1.4-1.3C5.4 15.4 2 12.3 2 8.5 2 5.4 4.4 3 7.5 3c1.7 0 3.4 0.8 4.5 2.1C13.1 3.8 14.8 3 16.5 3 19.6 3 22 5.4 22 8.5c0 3.8-3.4 6.9-8.6 11.6z",
        "diamond" to "M12 2l10 10-10 10L2 12z",
        "parallelogram" to "M7 4h15l-5 16H2z",
        "trapezoid" to "M6 4h12l4 16H2z",
        "pentagon" to "M12 2l10 7-4 12H6L2 9z",
        "hexagon" to "M12 2l9 5v10l-9 5-9-5V7z",
        "heptagon" to "M12 2l7 3 3 7-5 8H7L2 12l3-7z",
        "octagon" to "M8 2h8l6 6v8l-6 6H8l-6-6V8z",
        "nonagon" to "M12 2l6 2 4 5-1 7-5 5-8 1-5-4-1-7 4-6z",
        "decagon" to "M12 2l5 1 4 4 1 6-3 5-6 2-5-2-4-4-1-6 3-5z",
        "cross" to "M9 2h6v7h7v6h-7v7H9v-7H2V9h7z",
        "cube" to "M4 8h10v10H4zM9 3h10v10H9zM4 8l5-5M14 8l5-5M14 18l5-5M4 18l5-5",
        "cuboid" to "M2 10h14v10H2zM8 4h14v10H8zM2 10l6-6M16 10l6-6M16 20l6-6",
        "cylinder" to "M4 5a8 3 0 0 0 16 0a8 3 0 0 0-16 0M4 5v14a8 3 0 0 0 16 0V5",
        "cone" to "M12 3L4 19a8 3 0 0 0 16 0z",
        "pyramid" to "M12 3L2 19h20zM12 3v16",
        "sphere" to "M12 3a9 9 0 1 0 0 18a9 9 0 1 0 0-18M3 12h18",
    )

    fun of(key: String): ImageVector = draw(key, paths[key] ?: "M4 4h16v16H4z")
}