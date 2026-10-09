package com.simonecompany.lavagna.model

import androidx.compose.ui.graphics.Color

enum class BackgroundType { SOLID, GRADIENT, PATTERN }

/** Categoria della finestra "Impostazioni sfondo" (tab Colori / Sfondi). */
enum class BackgroundGroup { COLORS, PATTERNS }

/** Tipo di texture per gli sfondi a motivo. */
enum class PatternKind { LINES, GRID, DOTS }

/** Parametri di una texture, identici a pattern:{...} della web app. */
data class BackgroundPattern(
    val kind: PatternKind,
    val size: Float = 10f,
    val gap: Float = 24f,
    val color: Color = Color(0xFFB4B4B4),
    val width: Float = 1f,
    val radius: Float = 1f,
)

/** Voce del catalogo sfondi, identica a BACKGROUNDS della web app. */
data class BackgroundItem(
    val id: String,
    val name: String,
    val type: BackgroundType,
    val group: BackgroundGroup,
    val solid: Color = Color.White,
    val startColor: Color = Color.Transparent,
    val endColor: Color = Color.Transparent,
    val angle: Float = 135f,
    val pattern: BackgroundPattern? = null,
)

private fun solid(id: String, name: String, hex: Long) =
    BackgroundItem(id, name, BackgroundType.SOLID, BackgroundGroup.COLORS, Color(0xFF000000L or hex))

private fun gradient(id: String, name: String, g1: Long, g2: Long, angle: Float = 135f) =
    BackgroundItem(
        id, name, BackgroundType.GRADIENT, BackgroundGroup.COLORS,
        startColor = Color(0xFF000000L or g1),
        endColor = Color(0xFF000000L or g2),
        angle = angle,
    )

private fun pattern(id: String, name: String, p: BackgroundPattern) =
    BackgroundItem(id, name, BackgroundType.PATTERN, BackgroundGroup.PATTERNS, pattern = p)

private const val LINE = 0x4D969696L // rgba(150,150,150,0.30)
private const val LINE2 = 0x59B4B4B4L // rgba(180,180,180,0.35)
private const val LINE3 = 0x4DAAAAAAL // rgba(170,170,170,0.30)
private const val DOT = 0x4DA0A0A0L // rgba(160,160,160,0.30)
private const val DOTP = 0x2EC8C8C8L // rgba(200,200,200,0.18)

val Backgrounds: List<BackgroundItem> = listOf(
    solid("none", "Nessuno", 0xFFFFFF),
    solid("solid_white", "Bianco", 0xFFFFFF),
    solid("solid_cream", "Crema", 0xFAF7F2),
    solid("solid_beige", "Beige", 0xF5F1EC),
    solid("solid_lightgray", "Grigio chiaro", 0xF5F5F5),
    solid("solid_gray", "Grigio", 0xD0D0D0),
    solid("solid_darkgray", "Grigio scuro", 0x6B6B6B),
    solid("solid_black", "Nero", 0x111111),
    solid("solid_blue", "Azzurro", 0xE7F3FF),
    solid("solid_sky", "Cielo", 0xD6ECFF),
    solid("solid_teal", "Verde acqua", 0xE0F2F1),
    solid("solid_green", "Verde", 0xF1F8E9),
    solid("solid_lime", "Lime", 0xF4FCE3),
    solid("solid_yellow", "Giallo", 0xFFFDE7),
    solid("solid_orange", "Arancio", 0xFFF3E0),
    solid("solid_red", "Rosso", 0xFFEBEE),
    solid("solid_pink", "Rosa", 0xFCE4EC),
    solid("solid_purple", "Lavanda", 0xF3E5F5),
    solid("solid_brown", "Marrone", 0xEFEBE9),
    gradient("grad_sky", "Gradiente cielo", 0xF0F9FF, 0xE0F2FE, 180f),
    gradient("grad_sunrise", "Gradiente alba", 0xFFF7ED, 0xFCE7F3),
    gradient("grad_ocean", "Gradiente oceano", 0xE0F7FA, 0xE8EAF6),
    gradient("grad_forest", "Gradiente foresta", 0xF1F8E9, 0xE8F5E9),
    gradient("grad_purple", "Gradiente viola", 0xF3E5F5, 0xE8EAF6),
    pattern("lined", "Righe", BackgroundPattern(PatternKind.LINES, gap = 24f, color = Color(LINE))),
    pattern("lined_wide", "Righe larghe", BackgroundPattern(PatternKind.LINES, gap = 40f, color = Color(LINE))),
    pattern("grid_sm", "Quadretti piccoli", BackgroundPattern(PatternKind.GRID, size = 10f, color = Color(LINE2))),
    pattern("grid", "Quadretti medi", BackgroundPattern(PatternKind.GRID, size = 20f, color = Color(LINE2))),
    pattern("grid_lg", "Quadretti grandi", BackgroundPattern(PatternKind.GRID, size = 40f, color = Color(LINE2))),
    pattern("graph", "Quadretti fitti", BackgroundPattern(PatternKind.GRID, size = 8f, color = Color(LINE3))),
    pattern("dots_sm", "Punti piccoli", BackgroundPattern(PatternKind.DOTS, size = 12f, radius = 1f, color = Color(DOT))),
    pattern("dots", "Punti medi", BackgroundPattern(PatternKind.DOTS, size = 20f, radius = 1.5f, color = Color(DOT))),
    pattern("dots_lg", "Punti grandi", BackgroundPattern(PatternKind.DOTS, size = 30f, radius = 2.5f, color = Color(DOT))),
    pattern("paper", "Carta", BackgroundPattern(PatternKind.DOTS, size = 20f, radius = 1f, color = Color(DOTP))),
)

fun backgroundById(id: String): BackgroundItem = Backgrounds.firstOrNull { it.id == id } ?: Backgrounds.first()
