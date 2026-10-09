package com.simonecompany.lavagna.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Colori presi alla lettera dal CSS della web app. */
object Web {
    val Blue = Color(0xFF0B57D0)
    val BlueHover = Color(0xFF0842A0)
    val BlueSoft = Color(0xFFE8F0FE)
    val Active = Color(0xFFC2E7FF)
    val ActiveHover = Color(0xFFD3E4FD)
    val ActiveText = Color(0xFF001D35)
    val Text = Color(0xFF444746)
    val TextStrong = Color(0xFF1F1F1F)
    val TextMuted = Color(0xFF808080)
    val Border = Color(0xFFE0E0E0)
    val BorderLight = Color(0xFFF0F0F0)
    val Hover = Color(0xFFF2F2F2)
    val Danger = Color(0xFFB3261E)
    val DangerSoft = Color(0xFFFDEDED)
    val Sheet = Color(0xFFFFFFFF)
}

/**
 * Medie del CSS: la web app ha quattro breakpoint (900 / 600 / 420 px) che
 * cambiano padding, dimensione icone e slider. Qui la stessa cosa in dp.
 */
@Immutable
class Metrics(
    val btnPadH: Dp,
    val btnPadV: Dp,
    val btnFont: Int,
    val icon: Dp,
    val barRadius: Dp,
    val barGap: Dp,
    val barPadH: Dp,
    val barPadV: Dp,
    val dividerH: Dp,
    val range: Dp,
    val indicatorFont: Int,
    val showLabels: Boolean,
) {
    companion object {
        @Composable
        fun current(): Metrics {
            val w = LocalConfiguration.current.screenWidthDp
            return when {
                w <= 420 -> Metrics(8.dp, 6.dp, 13, 20.dp, 16.dp, 5.dp, 12.dp, 8.dp, 20.dp, 48.dp, 11, true)
                w <= 600 -> Metrics(9.dp, 6.dp, 13, 20.dp, 16.dp, 5.dp, 12.dp, 8.dp, 20.dp, 52.dp, 11, true)
                w <= 900 -> Metrics(10.dp, 7.dp, 14, 22.dp, 18.dp, 6.dp, 14.dp, 8.dp, 24.dp, 64.dp, 12, true)
                else -> Metrics(12.dp, 8.dp, 15, 24.dp, 20.dp, 8.dp, 16.dp, 10.dp, 28.dp, 80.dp, 13, true)
            }
        }
    }
}

/** .btn della web app. */
@Composable
fun WebButton(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    label: String? = null,
    active: Boolean = false,
    danger: Boolean = false,
    iconOnly: Boolean = false,
    enabled: Boolean = true,
    trailing: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
) {
    val m = Metrics.current()
    val background = when {
        active -> Web.Active
        else -> Color.Transparent
    }
    val fg = when {
        active -> Web.ActiveText
        danger -> Web.Danger
        else -> Web.Text
    }
    Row(
        modifier
            .clip(if (iconOnly) CircleShape else RoundedCornerShape(8.dp))
            .background(background)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(
                horizontal = if (iconOnly) m.btnPadV else m.btnPadH,
                vertical = m.btnPadV,
            )
            .then(if (!enabled) Modifier.alpha(0.3f) else Modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (label.isNullOrEmpty()) 0.dp else 3.dp),
    ) {
        Icon(icon, null, Modifier.size(m.icon), tint = fg)
        if (!label.isNullOrEmpty() && !iconOnly) {
            Text(
                label,
                fontSize = m.btnFont.sp,
                lineHeight = m.btnFont.sp,
                fontWeight = FontWeight.Medium,
                color = fg,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        trailing?.invoke()
    }
}

/** .divider della web app. */
@Composable
fun WebDivider(modifier: Modifier = Modifier) {
    val m = Metrics.current()
    Box(
        modifier
            .padding(horizontal = 1.dp)
            .width(1.dp)
            .height(m.dividerH)
            .background(Web.Border)
    )
}

/** .dropdown-header della web app. */
@Composable
fun WebHeader(text: String, modifier: Modifier = Modifier, withTopBorder: Boolean = true) {
    Column(
        modifier
            .fillMaxWidth()
            .then(if (withTopBorder) Modifier.border(1.dp, Web.BorderLight) else Modifier)
            .padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 3.dp)
    ) {
        Text(
            text.uppercase(),
            fontSize = 10.sp,
            lineHeight = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Web.Blue,
            letterSpacing = 0.5.sp,
        )
    }
}

/** .dropdown-item della web app. */
@Composable
fun WebMenuItem(
    text: String,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
    selected: Boolean = false,
    danger: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        modifier
            .fillMaxWidth()
            .background(if (selected) Web.BlueSoft else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        leading?.invoke()
        Text(
            text,
            fontSize = 12.sp,
            lineHeight = 14.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            color = when {
                danger -> Web.Danger
                selected -> Web.Blue
                else -> Web.TextStrong
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** .color-swatch della web app (20px, bordo 2px, anello bianco se selezionato). */
@Composable
fun WebSwatch(color: Color, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(color)
            .border(2.dp, if (selected) Web.Blue else Color.Transparent, CircleShape)
            .padding(if (selected) 2.dp else 0.dp)
            .clip(CircleShape)
            .background(if (selected) Color.White else Color.Transparent)
            .padding(2.dp)
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick)
    )
}

/** Pulsante "+" di aggiunta colore (bordo tratteggiato come nella web app). */
@Composable
fun WebAddColorButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(Web.BlueSoft)
            .dashedBorder(Web.Blue)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "+",
            fontSize = 13.sp,
            lineHeight = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Web.Blue,
        )
    }
}

/** Titolo del pannello/dialog (17px, peso 500). */
@Composable
fun WebCardTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.Medium,
        color = Web.TextStrong,
        modifier = modifier,
    )
}

@Composable
fun WebCardBody(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        fontSize = 13.sp,
        lineHeight = 20.sp,
        color = Web.Text,
        modifier = modifier,
    )
}

/** Bordo tratteggiato approssimato: cerchio con bordo Web.Blue tratteggiato via Canvas. */
private fun Modifier.dashedBorder(color: Color): Modifier =
    this.then(
        Modifier.drawBehind {
            val stroke = 2.dp.toPx()
            val w = size.width
            val h = size.height
            val path = androidx.compose.ui.graphics.Path().apply {
                addOval(
                    androidx.compose.ui.geometry.Rect(
                        stroke / 2f, stroke / 2f,
                        w - stroke / 2f, h - stroke / 2f,
                    )
                )
            }
            drawPath(
                path,
                color,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = stroke,
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(4f, 3f)),
                )
            )
        }
    )
/**
 * Barra come nella web app: `width: fit-content; max-width: 100%; flex-wrap: wrap`.
 * Il contenuto riceve `wrap`: se la riga singola entra nello schermo resta in fila,
 * altrimenti va a capo (come il flex-wrap del CSS).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FitContentBar(
    gap: Dp,
    modifier: Modifier = Modifier,
    content: @Composable (wrap: Boolean) -> Unit,
) {
    SubcomposeLayout(modifier) { constraints ->
        val gapPx = gap.roundToPx()
        // 1) larghezza naturale: riga singola senza limiti
        val single = subcompose("single") {
            Row(
                horizontalArrangement = Arrangement.spacedBy(gap),
                verticalAlignment = Alignment.CenterVertically,
            ) { content(false) }
        }.map { it.measure(constraints.copy(minWidth = 0, maxWidth = Constraints.Infinity)) }

        val natural = single.sumOf { it.width } + gapPx * (single.size - 1).coerceAtLeast(0)
        val wraps = natural > constraints.maxWidth

        // 2) misura finale: riga singola, oppure FlowRow che va a capo
        val placeables = subcompose("final") {
            if (wraps) {
                FlowRow(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(gap),
                ) { content(true) }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(gap),
                    verticalAlignment = Alignment.CenterVertically,
                ) { content(false) }
            }
        }.map { it.measure(constraints) }

        val width = placeables.maxOfOrNull { it.width } ?: 0
        val height = placeables.maxOfOrNull { it.height } ?: 0
        layout(width, height) {
            placeables.forEach { it.place(0, 0) }
        }
    }
}
