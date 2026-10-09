package com.simonecompany.lavagna.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import com.simonecompany.lavagna.model.BackgroundGroup
import com.simonecompany.lavagna.model.BackgroundItem
import com.simonecompany.lavagna.model.BackgroundType
import com.simonecompany.lavagna.model.Backgrounds
import com.simonecompany.lavagna.model.PatternKind
import com.simonecompany.lavagna.ui.LocalStrings
import com.simonecompany.lavagna.vm.BoardViewModel

/** Overlay comune dei modali della web app (.modal-overlay + .modal). */
@Composable
private fun WebModal(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = true, dismissOnBackPress = true),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .widthIn(max = 400.dp),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Web.Sheet)
                    .padding(20.dp)
            ) { content() }
        }
    }
}

/** .modal-btn della web app. */
@Composable
private fun ModalButton(
    label: String,
    confirm: Boolean = false,
    onClick: () -> Unit,
) {
    Box(
        Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (confirm) Web.Blue else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(
            label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (confirm) Color.White else Web.Blue,
        )
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val strings = LocalStrings.current
    WebModal(onDismiss) {
        WebCardTitle(title)
        Spacer(Modifier.height(10.dp))
        WebCardBody(message)
        Spacer(Modifier.height(18.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        ) {
            ModalButton(strings["cancel"]) { onDismiss() }
            ModalButton(strings["confirm"], confirm = true) { onConfirm() }
        }
    }
}

/** Finestra di salvataggio: nome file + scelta formato (PNG / JPG / PDF). */
@Composable
fun SaveDialog(
    initialName: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val strings = LocalStrings.current
    var name by remember { mutableStateOf(initialName) }
    var focused by remember { mutableStateOf(false) }
    val placeholder = if (strings["saveTitle"] == "Salva Lavagna") "Nome del file" else "Filename"

    WebModal(onDismiss) {
        WebCardTitle(strings["saveTitle"])
        Spacer(Modifier.height(14.dp))
        WebCardBody(strings["saveMsg"])
        Spacer(Modifier.height(10.dp))

        // .filename-input
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White)
                .border(2.dp, if (focused) Web.Blue else Web.Border, RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            if (name.isEmpty()) {
                Text(placeholder, fontSize = 13.sp, color = Web.TextMuted)
            }
            BasicTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                textStyle = TextStyle(fontSize = 13.sp, color = Web.TextStrong),
                cursorBrush = SolidColor(Web.Blue),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(14.dp))

        // .format-buttons
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FormatButton("PNG", "immagine", Modifier.weight(1f)) { onSave("$name.png") }
            FormatButton("JPG", "immagine", Modifier.weight(1f)) { onSave("$name.jpg") }
            FormatButton("PDF", "documento", Modifier.weight(1f)) { onSave("$name.pdf") }
        }

        Spacer(Modifier.height(8.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            ModalButton(strings["cancel"]) { onDismiss() }
        }
    }
}

@Composable
private fun FormatButton(label: String, ext: String, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .border(2.dp, Web.Border, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Web.TextStrong)
        Text(ext, fontSize = 10.sp, fontWeight = FontWeight.Normal, color = Web.TextMuted)
    }
}

/** Selettore colore HSV (sostituisce l'input color del browser). */
@Composable
fun ColorPickerDialog(
    initial: Color,
    onConfirm: (Color) -> Unit,
    onDismiss: () -> Unit,
) {
    val strings = LocalStrings.current
    val hsv = remember { FloatArray(3) }
    android.graphics.Color.colorToHSV(initial.toArgb(), hsv)
    var hue by remember { mutableStateOf(hsv[0]) }
    var sat by remember { mutableStateOf(hsv[1]) }
    var bri by remember { mutableStateOf(hsv[2]) }
    val current = remember(hue, sat, bri) {
        Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, sat, bri)))
    }
    var hex by remember(current) {
        mutableStateOf("#%06X".format(0xFFFFFF and current.toArgb()))
    }
    var hexError by remember { mutableStateOf(false) }

    WebModal(onDismiss) {
        WebCardTitle(strings["colorCustom"])
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(44.dp)
                    .background(current, CircleShape)
                    .border(2.dp, Web.Border, CircleShape)
            )
            Spacer(Modifier.width(12.dp))
            Box(
                Modifier
                    .width(140.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .border(1.dp, if (hexError) Web.Danger else Web.Border, RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            ) {
                BasicTextField(
                    value = hex,
                    onValueChange = { value ->
                        hex = value
                        val parsed = parseHex(value)
                        hexError = parsed == null
                        if (parsed != null) {
                            android.graphics.Color.colorToHSV(parsed.toArgb(), hsv)
                            hue = hsv[0]
                            sat = hsv[1]
                            bri = hsv[2]
                        }
                    },
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Web.TextStrong,
                    ),
                    cursorBrush = SolidColor(Web.Blue),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        HsvSlider("H", hue, 0f, 360f) { hue = it }
        HsvSlider("S", sat, 0f, 1f) { sat = it }
        HsvSlider("V", bri, 0f, 1f) { bri = it }
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
            ModalButton(strings["cancel"]) { onDismiss() }
            ModalButton(strings["confirm"], confirm = true) { onConfirm(current) }
        }
    }
}

@Composable
private fun HsvSlider(label: String, value: Float, min: Float, max: Float, onChange: (Float) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            fontSize = 11.sp,
            color = Web.Text,
            modifier = Modifier.width(18.dp),
        )
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = min..max,
            colors = SliderDefaults.colors(
                thumbColor = Web.Blue,
                activeTrackColor = Web.Blue,
                inactiveTrackColor = Web.Border,
            ),
            modifier = Modifier.weight(1f),
        )
    }
}

private fun parseHex(value: String): Color? {
    val hex = value.trim().removePrefix("#")
    if (hex.length != 6) return null
    val v = hex.toLongOrNull(16) ?: return null
    return Color(0xFF000000L or v)
}


/** Finestra Material "Impostazioni sfondo" con tab Colori / Sfondi (3 per riga). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BackgroundsDialog(
    vm: BoardViewModel,
    onDismiss: () -> Unit,
) {
    val strings = LocalStrings.current
    var tab by remember { mutableStateOf(BackgroundGroup.COLORS) }
    WebModal(onDismiss) {
        WebCardTitle(strings["backgrounds"])
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BgTab(strings["colors2"], tab == BackgroundGroup.COLORS, Modifier.weight(1f)) {
                tab = BackgroundGroup.COLORS
            }
            BgTab(strings["patterns2"], tab == BackgroundGroup.PATTERNS, Modifier.weight(1f)) {
                tab = BackgroundGroup.PATTERNS
            }
        }
        Spacer(Modifier.height(12.dp))
        FlowRow(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Backgrounds.filter { it.group == tab }.forEach { bg ->
                BackgroundCell(
                    name = if (bg.id == "none") strings["none"] else bg.name,
                    item = bg,
                    selected = vm.backgroundId == bg.id,
                ) { vm.applyBackground(bg.id); onDismiss() }
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        ) {
            ModalButton(strings["cancel"], onClick = onDismiss)
        }
    }
}

@Composable
private fun BgTab(label: String, active: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (active) Web.Blue else Color(0xFFF8F9FA))
            .border(1.dp, if (active) Web.Blue else Color(0xFFDADCE0), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontSize = 13.sp, color = if (active) Color.White else Color(0xFF3C4043))
    }
}

@Composable
private fun BackgroundCell(
    name: String,
    item: BackgroundItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        Modifier
            .width(88.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Color(0xFFE8F0FE) else Color(0xFFF8F9FA))
            .border(
                if (selected) 2.dp else 1.dp,
                if (selected) Color(0xFF0B57D0) else Color(0xFFDADCE0),
                RoundedCornerShape(10.dp),
            )
            .clickable(onClick = onClick)
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White),
        ) {
            BackgroundPreview(item)
        }
        Spacer(Modifier.height(5.dp))
        Text(
            name,
            fontSize = 11.sp,
            color = Color(0xFF3C4043),
            maxLines = 1,
        )
    }
}

@Composable
private fun BackgroundPreview(item: BackgroundItem) {
    Canvas(Modifier.fillMaxSize()) {
        when (item.type) {
            BackgroundType.SOLID -> drawRect(item.solid)
            BackgroundType.GRADIENT -> drawRect(
                androidx.compose.ui.graphics.Brush.linearGradient(listOf(item.startColor, item.endColor)),
            )
            BackgroundType.PATTERN -> {
                drawRect(Color.White)
                val p = item.pattern ?: return@Canvas
                when (p.kind) {
                    PatternKind.LINES -> {
                        var y = p.gap
                        while (y < size.height) {
                            drawLine(p.color, Offset(0f, y), Offset(size.width, y), p.width)
                            y += p.gap
                        }
                    }
                    PatternKind.GRID -> {
                        var x = 0f
                        while (x < size.width) {
                            drawLine(p.color, Offset(x, 0f), Offset(x, size.height), p.width)
                            x += p.size
                        }
                        var y = 0f
                        while (y < size.height) {
                            drawLine(p.color, Offset(0f, y), Offset(size.width, y), p.width)
                            y += p.size
                        }
                    }
                    PatternKind.DOTS -> {
                        var x = p.size / 2f
                        while (x < size.width) {
                            var y = p.size / 2f
                            while (y < size.height) {
                                drawCircle(p.color, p.radius, Offset(x, y))
                                y += p.size
                            }
                            x += p.size
                        }
                    }
                }
            }
        }
    }
}
