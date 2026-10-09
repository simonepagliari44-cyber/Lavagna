package com.simonecompany.lavagna.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.simonecompany.lavagna.board.Fonts
import com.simonecompany.lavagna.model.TextAlign as TextAlignEnum
import com.simonecompany.lavagna.ui.LavagnaIcons
import com.simonecompany.lavagna.ui.LocalStrings
import com.simonecompany.lavagna.vm.BoardViewModel
import kotlin.math.roundToInt

/**
 * Editor di testo sovrapposto: replica .text-input-overlay della web app
 * (sfondo bianco 95%, bordo tratteggiato #0b57d0, raggio 6px, padding 6px 10px).
 */
@Composable
fun TextEditorOverlay(vm: BoardViewModel, modifier: Modifier = Modifier) {
    val edit = vm.textEdit ?: return
    val density = LocalDensity.current
    val focusRequester = remember { FocusRequester() }
    var origin by remember { mutableStateOf(Offset.Zero) }

    val fontPx = vm.fontSize * vm.scale * density.density
    val fontUnit = (fontPx / density.fontScale).sp
    val align = when (vm.align) {
        TextAlignEnum.LEFT -> TextAlign.Left
        TextAlignEnum.CENTER -> TextAlign.Center
        TextAlignEnum.RIGHT -> TextAlign.Right
    }
    val textStyle = TextStyle(
        color = vm.inkColor,
        fontSize = fontUnit,
        lineHeight = fontUnit * 1.2f,
        fontFamily = Fonts.fontFamily(vm.fontName, vm.bold, vm.italic),
        fontWeight = if (vm.bold) FontWeight.Bold else FontWeight.Normal,
        textAlign = align,
        platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false),
    )

    LaunchedEffect(edit.index, edit.x, edit.y) {
        runCatching { focusRequester.requestFocus() }
    }

    Box(
        modifier
            .fillMaxSize()
            .blockBoardGestures()
    ) {
        val widthDp = (edit.boxWidth * vm.scale / density.density).dp.coerceAtLeast(60.dp)
        val heightDp = (edit.boxHeight * vm.scale / density.density).dp.coerceAtLeast((fontPx / density.density).dp)
        var cx by remember { mutableStateOf(0f) }
        var cy by remember { mutableStateOf(0f) }

        Box(
            Modifier
                .onGloballyPositioned { 
                    val size = it.size
                    cx = size.width / 2f
                    cy = size.height / 2f
                    origin = it.positionInWindow()
                }
                .offset { IntOffset((cx - (widthDp.value * density.density) / 2f).roundToInt(), (cy - (heightDp.value * density.density) / 2f).roundToInt()) },
        ) {
            BasicTextField(
                value = edit.text,
                onValueChange = { vm.updateEditingText(it) },
                textStyle = textStyle,
                cursorBrush = SolidColor(vm.inkColor),
                modifier = Modifier
                    .width(widthDp)
                    .heightIn(min = heightDp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.95f))
                    .dashedBorder(Web.Blue)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .focusRequester(focusRequester)
                    .onSizeChanged {
                        vm.updateEditingBox(
                            it.width / density.density / vm.scale,
                            it.height / density.density / vm.scale,
                        )
                    },
            )
        }

        TextOptionsMenu(vm, IntOffset(origin.x.roundToInt(), (origin.y + 40).roundToInt()))
    }
}

/** .fontdrop della web app: 260px, header, size, toggle, align, ricerca, lista font. */
@Composable
private fun TextOptionsMenu(vm: BoardViewModel, origin: IntOffset) {
    val strings = LocalStrings.current
    var query by remember { mutableStateOf("") }
    val fonts = remember(query) {
        Fonts.NAMES.filter { it.contains(query, ignoreCase = true) }
    }
    val placeholder = if (strings["size"] == "Dim.") "Cerca font..." else "Search font..."

    Popup(alignment = Alignment.TopStart, offset = origin) {
        Column(
            Modifier
                .width(260.dp)
                .heightIn(max = 380.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White)
                .padding(vertical = 4.dp)
        ) {
            WebHeader(strings["textOptions"], withTopBorder = false)

            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(strings["size"], fontSize = 12.sp, color = Web.Text)
                Slider(
                    value = vm.fontSize,
                    onValueChange = { vm.fontSize = it },
                    valueRange = 10f..200f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = Web.Blue,
                        activeTrackColor = Web.Blue,
                        inactiveTrackColor = Web.Border,
                    ),
                )
                Text(
                    vm.fontSize.toInt().toString(),
                    fontSize = 12.sp,
                    color = Web.Text,
                    modifier = Modifier.width(30.dp),
                )
            }

            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ToggleBtn(LavagnaIcons.Bold, vm.bold) { vm.toggleBold() }
                ToggleBtn(LavagnaIcons.Italic, vm.italic) { vm.toggleItalic() }
                Row(
                    Modifier.padding(start = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    AlignBtn(LavagnaIcons.AlignLeft, vm.align == TextAlignEnum.LEFT) { vm.applyAlign(TextAlignEnum.LEFT) }
                    AlignBtn(LavagnaIcons.AlignCenter, vm.align == TextAlignEnum.CENTER) { vm.applyAlign(TextAlignEnum.CENTER) }
                    AlignBtn(LavagnaIcons.AlignRight, vm.align == TextAlignEnum.RIGHT) { vm.applyAlign(TextAlignEnum.RIGHT) }
                }
            }

            // .font-search
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .border(1.dp, Web.Border, RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                if (query.isEmpty()) {
                    Text(placeholder, fontSize = 12.sp, color = Web.TextMuted)
                }
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 12.sp, color = Web.TextStrong),
                    cursorBrush = SolidColor(Web.Blue),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // .font-list
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Web.BorderLight)
            )
            LazyColumn(Modifier.heightIn(max = 180.dp)) {
                items(fonts) { name ->
                    val selected = name == vm.fontName
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .background(if (selected) Web.BlueSoft else Color.Transparent)
                            .clickable { vm.fontName = name }
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            name,
                            fontSize = 13.sp,
                            fontFamily = Fonts.fontFamily(name, vm.bold, vm.italic),
                            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                            color = if (selected) Web.Blue else Web.TextStrong,
                        )
                    }
                }
            }
        }
    }
}

/** .toggle-btn della web app. */
@Composable
private fun ToggleBtn(icon: androidx.compose.ui.graphics.vector.ImageVector, on: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(if (on) Web.Active else Color.White)
            .border(1.dp, if (on) Web.Blue else Web.Border, RoundedCornerShape(5.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            null,
            Modifier.size(16.dp),
            tint = if (on) Web.ActiveText else Web.Text,
        )
    }
}

/** .align-btn della web app. */
@Composable
private fun AlignBtn(icon: androidx.compose.ui.graphics.vector.ImageVector, on: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(if (on) Web.Active else Color.White)
            .border(1.dp, if (on) Web.Blue else Web.Border, RoundedCornerShape(5.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 5.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            null,
            Modifier.size(15.dp),
            tint = if (on) Web.ActiveText else Web.Text,
        )
    }
}

/** Bordo tratteggiato rettangolare (2px, come nel CSS). */
private fun Modifier.dashedBorder(color: Color): Modifier = drawBehind {
    val stroke = 2.dp.toPx()
    val inset = stroke / 2f
    val w = size.width
    val h = size.height
    drawRect(
        color = color,
        topLeft = Offset(inset, inset),
        size = androidx.compose.ui.geometry.Size(w - stroke, h - stroke),
        style = Stroke(
            width = stroke,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f)),
        )
    )
}

/** Impedisce che i tocchi sull'editor di testo arrivino alla lavagna. */
private fun Modifier.blockBoardGestures(): Modifier = pointerInput(Unit) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            event.changes.forEach { if (it.positionChanged()) it.consume() }
        }
    }
}