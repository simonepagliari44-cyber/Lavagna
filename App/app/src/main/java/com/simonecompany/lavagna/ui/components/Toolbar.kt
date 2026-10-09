package com.simonecompany.lavagna.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simonecompany.lavagna.model.BackgroundType
import com.simonecompany.lavagna.model.Backgrounds
import com.simonecompany.lavagna.model.DefaultColors
import com.simonecompany.lavagna.model.EraserMode
import com.simonecompany.lavagna.model.PointerMode
import com.simonecompany.lavagna.model.ShapeType
import com.simonecompany.lavagna.model.Tool
import com.simonecompany.lavagna.ui.LavagnaIcons
import com.simonecompany.lavagna.ui.LocalStrings
import com.simonecompany.lavagna.ui.ShapeIcons
import com.simonecompany.lavagna.vm.BoardViewModel

/** Menu dropdown attualmente aperto nella toolbar. */
enum class MenuKind { NONE, POINTER, ERASER, SHAPES, COLOR, BACKGROUNDS }

/**
 * Toolbar superiore: replica .toolbar della web app (pallina bianca centrata in
 * alto, bottoni che vanno a capo, gruppi separati da divisori).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TopToolbar(
    vm: BoardViewModel,
    openMenu: MenuKind,
    onMenuChange: (MenuKind) -> Unit,
    onClearPage: () -> Unit,
    onAddCustomColor: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    val m = Metrics.current()
    val density = androidx.compose.ui.platform.LocalDensity.current

    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Surface(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            shape = RoundedCornerShape(m.barRadius),
            color = Web.Sheet,
            shadowElevation = 8.dp,
        ) {
            FitContentBar(
                gap = m.barGap,
                modifier = Modifier.padding(horizontal = m.barPadH, vertical = m.barPadV),
            ) {
                Box {
                    WebButton(LavagnaIcons.Pointer, label = strings["pointer"], active = vm.tool == Tool.POINTER) {
                        // come il web: primo clic seleziona lo strumento, secondo apre il menu
                        if (vm.tool != Tool.POINTER) {
                            vm.selectTool(Tool.POINTER)
                            onMenuChange(MenuKind.NONE)
                        } else {
                            onMenuChange(if (openMenu == MenuKind.POINTER) MenuKind.NONE else MenuKind.POINTER)
                        }
                    }
                    WebDropdown(openMenu == MenuKind.POINTER, { onMenuChange(MenuKind.NONE) }) {
                        WebMenuItem(
                            strings["pointerNormalFull"],
                            leading = { Icon(LavagnaIcons.Pointer, null, Modifier.size(14.dp), tint = Web.Text) },
                            selected = vm.tool == Tool.POINTER && vm.pointerMode == PointerMode.NORMAL,
                        ) { vm.pointerMode = PointerMode.NORMAL; onMenuChange(MenuKind.NONE) }
                        WebMenuItem(
                            strings["pointerLassoFull"],
                            leading = { Icon(LavagnaIcons.Lasso, null, Modifier.size(14.dp), tint = Web.Text) },
                            selected = vm.tool == Tool.POINTER && vm.pointerMode == PointerMode.LASSO,
                        ) { vm.pointerMode = PointerMode.LASSO; onMenuChange(MenuKind.NONE) }
                    }
                }

                WebButton(LavagnaIcons.Pen, label = strings["pen"], active = vm.tool == Tool.PEN) {
                    vm.selectTool(Tool.PEN)
                    onMenuChange(MenuKind.NONE)
                }

                Box {
                    WebButton(LavagnaIcons.Eraser, label = strings["eraser"], active = vm.tool == Tool.ERASER) {
                        // come il web: primo clic seleziona lo strumento, secondo apre il menu
                        if (vm.tool != Tool.ERASER) {
                            vm.selectTool(Tool.ERASER)
                            onMenuChange(MenuKind.NONE)
                        } else {
                            onMenuChange(if (openMenu == MenuKind.ERASER) MenuKind.NONE else MenuKind.ERASER)
                        }
                    }
                    WebDropdown(openMenu == MenuKind.ERASER, { onMenuChange(MenuKind.NONE) }, width = 190.dp) {
                        WebMenuItem(
                            strings["eraserNormalFull"],
                            leading = { Icon(LavagnaIcons.Eraser, null, Modifier.size(14.dp), tint = Web.Text) },
                            selected = vm.tool == Tool.ERASER && vm.eraserMode == EraserMode.NORMAL,
                        ) { vm.eraserMode = EraserMode.NORMAL; onMenuChange(MenuKind.NONE) }
                        WebMenuItem(
                            strings["eraserStrokeFull"],
                            leading = { Icon(LavagnaIcons.StrokeLines, null, Modifier.size(14.dp), tint = Web.Text) },
                            selected = vm.tool == Tool.ERASER && vm.eraserMode == EraserMode.STROKE,
                        ) { vm.eraserMode = EraserMode.STROKE; onMenuChange(MenuKind.NONE) }
                        WebMenuItem(
                            strings["eraserLassoFull"],
                            leading = { Icon(LavagnaIcons.Lasso, null, Modifier.size(14.dp), tint = Web.Text) },
                            selected = vm.tool == Tool.ERASER && vm.eraserMode == EraserMode.LASSO,
                        ) { vm.eraserMode = EraserMode.LASSO; onMenuChange(MenuKind.NONE) }
                    }
                }

                Box {
                    WebButton(LavagnaIcons.Shapes, label = strings["shapes"], active = vm.tool == Tool.SHAPE) {
                        onMenuChange(if (openMenu == MenuKind.SHAPES) MenuKind.NONE else MenuKind.SHAPES)
                    }
                    WebDropdown(openMenu == MenuKind.SHAPES, { onMenuChange(MenuKind.NONE) }, maxHeight = 340.dp) {
                        WebHeader(strings["shapes2D"], withTopBorder = false)
                        ShapeType.shapes2D.forEach { type ->
                            ShapeItem(vm, type) { onMenuChange(MenuKind.NONE) }
                        }
                        WebHeader(strings["shapes3D"])
                        ShapeType.shapes3D.forEach { type ->
                            ShapeItem(vm, type) { onMenuChange(MenuKind.NONE) }
                        }
                    }
                }

                WebButton(LavagnaIcons.Text, label = strings["toolText"], active = vm.tool == Tool.TEXT) {
                    vm.selectTool(Tool.TEXT)
                    onMenuChange(MenuKind.NONE)
                }

                WebButton(LavagnaIcons.Fill, label = strings["toolFill"], active = vm.tool == Tool.FILL) {
                    vm.selectTool(Tool.FILL)
                    onMenuChange(MenuKind.NONE)
                }

                WebButton(LavagnaIcons.Background, label = strings["toolBg"], active = vm.tool == Tool.BACKGROUND) {
                    vm.selectTool(Tool.BACKGROUND)
                    onMenuChange(MenuKind.NONE)
                }

                WebButton(LavagnaIcons.Backgrounds, label = strings["backgroundSettings"], active = openMenu == MenuKind.BACKGROUNDS) {
                    onMenuChange(if (openMenu == MenuKind.BACKGROUNDS) MenuKind.NONE else MenuKind.BACKGROUNDS)
                }

                WebDivider()

                Box {
                    WebButton(LavagnaIcons.Palette, label = strings["color"], trailing = {
                        Box(
                            Modifier
                                .size(14.dp)
                                .background(vm.inkColor, CircleShape)
                                .border(2.dp, Web.Border, CircleShape)
                        )
                    }) {
                        onMenuChange(if (openMenu == MenuKind.COLOR) MenuKind.NONE else MenuKind.COLOR)
                    }
                    WebDropdown(openMenu == MenuKind.COLOR, { onMenuChange(MenuKind.NONE) }, width = 220.dp) {
                        ColorMenuBody(vm, onAddCustomColor)
                    }
                }

                WebDivider()

                // slider spessore
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (m.btnFont >= 11) {
                        Text(
                            strings["thickness"],
                            fontSize = (m.btnFont - 1).sp,
                            lineHeight = (m.btnFont - 1).sp,
                            color = Web.Text,
                            maxLines = 1,
                        )
                        Spacer(Modifier.width(5.dp))
                    }
                    Slider(
                        value = vm.strokeWidth,
                        onValueChange = { vm.updateStrokeWidth(it) },
                        onValueChangeFinished = { vm.onStrokeWidthDragEnd() },
                        valueRange = 1f..50f,
                        steps = 48,
                        modifier = Modifier.width(m.range),
                        colors = SliderDefaults.colors(
                            thumbColor = Web.Blue,
                            activeTrackColor = Web.Blue,
                            inactiveTrackColor = Color(0xFFDDDDDD),
                        ),
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        "${vm.strokeWidth.toInt()}",
                        fontSize = (m.btnFont - 1).sp,
                        lineHeight = (m.btnFont - 1).sp,
                        color = Web.Text,
                        maxLines = 1,
                        modifier = Modifier.width(22.dp),
                    )
                }

                WebDivider()

                WebButton(LavagnaIcons.ZoomIn, iconOnly = true, active = vm.zoomMode) { vm.toggleZoomMode() }

                WebDivider()

                WebButton(LavagnaIcons.Undo, iconOnly = true, enabled = vm.canUndo()) { vm.undo() }
                WebButton(LavagnaIcons.Redo, iconOnly = true, enabled = vm.canRedo()) { vm.redo() }
                WebButton(LavagnaIcons.Delete, label = strings["clear"], danger = true, onClick = onClearPage)
            }
        }
    }
}

/** Dropdown con lo stile .dropdown-menu della web app. */
@Composable
private fun WebDropdown(
    expanded: Boolean,
    onDismiss: () -> Unit,
    width: androidx.compose.ui.unit.Dp = 200.dp,
    maxHeight: androidx.compose.ui.unit.Dp = 340.dp,
    content: @Composable () -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier
            .width(width)
            .heightIn(max = maxHeight)
            .background(Web.Sheet, RoundedCornerShape(10.dp)),
    ) {
        content()
    }
}

@Composable
private fun ShapeItem(vm: BoardViewModel, type: ShapeType, onPick: () -> Unit) {
    val selected = vm.tool == Tool.SHAPE && vm.shapeType == type
    WebMenuItem(
        LocalStrings.current[type.labelKey],
        leading = {
            Icon(
                ShapeIcons.of(type.key),
                null,
                Modifier.size(14.dp),
                tint = if (selected) Web.Blue else Web.Text,
            )
        },
        selected = selected,
    ) {
        vm.selectShape(type)
        onPick()
    }
}

@Composable
private fun ColorMenuBody(vm: BoardViewModel, onAddCustom: () -> Unit) {
    val strings = LocalStrings.current
    Column(Modifier.padding(8.dp)) {
        WebHeader(strings["colorDefault"], withTopBorder = false)
        SwatchGrid(DefaultColors) { vm.selectColor(it) }
        Text(
            strings["colorCustom"].uppercase(),
            fontSize = 10.sp,
            lineHeight = 12.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            color = Web.Blue,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(top = 6.dp, bottom = 4.dp),
        )
        SwatchGrid(vm.customColors + listOf(Color.Unspecified), addLast = onAddCustom) {
            if (it != Color.Unspecified) vm.selectColor(it)
        }
    }
}

@Composable
private fun SwatchGrid(
    colors: List<Color>,
    addLast: (() -> Unit)? = null,
    onPick: (Color) -> Unit,
) {
    // griglia 8 colonne come .color-palette
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        colors.chunked(8).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                row.forEach { c ->
                    if (c == Color.Unspecified) {
                        WebAddColorButton(onClick = { addLast?.invoke() })
                    } else {
                        WebSwatch(c, selected = false, onClick = { onPick(c) })
                    }
                }
            }
        }
    }
}