package com.simonecompany.lavagna.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalViewConfiguration
import com.simonecompany.lavagna.board.BoardRenderState
import com.simonecompany.lavagna.board.BoardRenderer
import com.simonecompany.lavagna.model.EraserMode
import com.simonecompany.lavagna.model.PointerMode
import com.simonecompany.lavagna.model.Point
import com.simonecompany.lavagna.model.Tool
import kotlinx.coroutines.withTimeoutOrNull
import com.simonecompany.lavagna.vm.BoardViewModel
import com.simonecompany.lavagna.vm.PointerGesture

private enum class GestureMode { NONE, TAP, DRAG, RESIZE, PAN, DRAW, LASSO, PINCH }

/**
 * La lavagna: canvas Compose che disegna tramite android.graphics (identico
 * alla web app) e gestisce il multitouch al posto degli eventi mouse/touch JS.
 */
@Composable
fun BoardSurface(vm: BoardViewModel, modifier: Modifier = Modifier) {
    val renderer = remember { BoardRenderer() }
    var viewport by remember { mutableStateOf(Size.Zero) }

    Box(modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged {
                    viewport = Size(it.width.toFloat(), it.height.toFloat())
                    vm.canvasWidth = it.width
                    vm.canvasHeight = it.height
                }
                .boardGestures(vm)
        ) {
            drawIntoCanvas { canvas ->
                renderer.render(
                    canvas.nativeCanvas,
                    BoardRenderState(
                        background = vm.backgroundColor,
                        backgroundId = vm.backgroundId,
                        objects = vm.objects(),
                        livePoints = vm.livePathPoints,
                        liveColor = vm.livePathColor,
                        liveWidth = vm.livePathWidth,
                        tempShape = vm.tempShape,
                        selectedIndex = vm.selectedIndex,
                        multiSelection = vm.multiSelection,
                        showSelection = vm.tool == Tool.POINTER && !vm.zoomMode && vm.textEdit == null,
                        lasso = vm.lasso,
                        lassoIsSelection = vm.lassoIsSelection,
                        scale = vm.scale,
                        offset = vm.offset,
                        zoomMode = vm.zoomMode,
                        viewport = viewport,
                    )
                )
            }
        }
    }
}

@Composable
private fun Modifier.boardGestures(vm: BoardViewModel): Modifier {
    val viewConfiguration = LocalViewConfiguration.current
    val haptics = LocalHapticFeedback.current
    val slop = viewConfiguration.touchSlop
    return this.pointerInput(vm) {
        var lastTapTime = 0L
        var lastTapPosition = Offset.Zero

        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            val boardDown = vm.screenToBoard(down.position).toPoint()
            val tool = vm.tool
            var mode = GestureMode.NONE
            var moved = false
            var finished = false
            var panAnchor = Offset.Zero
            var panOrigin = Offset.Zero
            var pinchCenter = Offset.Zero
            var pinchDistance = 0f

            fun Offset.toBoard() = vm.screenToBoard(this).toPoint()

            fun board(p: Offset) = vm.screenToBoard(p).toPoint()

            fun finish() {
                if (finished) return
                finished = true
                when (mode) {
                    GestureMode.DRAW -> when {
                        vm.zoomMode -> Unit
                        tool == Tool.SHAPE -> vm.shapeUp()
                        tool == Tool.PEN -> vm.penUp()
                        tool == Tool.ERASER -> vm.penUp()
                        else -> Unit
                    }
                    GestureMode.LASSO -> vm.lassoUp()
                    GestureMode.DRAG, GestureMode.RESIZE, GestureMode.PAN -> vm.pointerUp()
                    else -> Unit
                }
            }

            when {
                vm.zoomMode -> {
                    mode = GestureMode.PAN
                    panAnchor = down.position
                    panOrigin = vm.offset
                    down.consume()
                }

                tool == Tool.POINTER && vm.pointerMode == PointerMode.NORMAL -> {
                    val now = System.currentTimeMillis()
                    val isDoubleTap = now - lastTapTime < 320 &&
                        (down.position - lastTapPosition).getDistance() < slop * 2.5f
                    // timeout scaduto => il dito e' rimasto premuto a lungo (long press);
                    // ritorno anticipato => il dito e' stato sollevato prima (tap normale)
                    val released = if (isDoubleTap) {
                        null
                    } else {
                        withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
                            waitForUpOrCancellation(PointerEventPass.Main)
                        }
                    }
                    when {
                        isDoubleTap -> {
                            lastTapTime = 0L
                            vm.onDoubleTap(boardDown)
                            down.consume()
                            mode = GestureMode.TAP
                        }
                        released == null -> {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            vm.onLongPress(down.position.toBoard())
                            down.consume()
                            return@awaitEachGesture
                        }
                        else -> {
                            lastTapTime = now
                            lastTapPosition = down.position
                            mode = when (vm.pointerDown(boardDown)) {
                                PointerGesture.DRAG -> GestureMode.DRAG
                                PointerGesture.RESIZE -> GestureMode.RESIZE
                                PointerGesture.NONE -> GestureMode.TAP
                            }
                            if (mode != GestureMode.TAP) down.consume()
                        }
                    }
                }

                tool == Tool.POINTER -> {
                    vm.lassoDown(boardDown, forSelection = true)
                    mode = GestureMode.LASSO
                    down.consume()
                }

                tool == Tool.PEN -> {
                    vm.penDown(boardDown)
                    mode = GestureMode.DRAW
                    down.consume()
                }

                tool == Tool.ERASER -> when (vm.eraserMode) {
                    EraserMode.NORMAL -> {
                        vm.penDown(boardDown)
                        mode = GestureMode.DRAW
                        down.consume()
                    }
                    EraserMode.STROKE -> {
                        vm.eraseStrokeAt(boardDown)
                        mode = GestureMode.DRAW
                        down.consume()
                    }
                    EraserMode.LASSO -> {
                        vm.lassoDown(boardDown, forSelection = false)
                        mode = GestureMode.LASSO
                        down.consume()
                    }
                }

                tool == Tool.SHAPE -> {
                    vm.shapeDown(boardDown)
                    mode = GestureMode.DRAW
                    down.consume()
                }

                tool == Tool.FILL -> {
                    vm.fillAt(boardDown)
                    mode = GestureMode.TAP
                }

                tool == Tool.BACKGROUND -> {
                    vm.paintBackground()
                    mode = GestureMode.TAP
                }

                tool == Tool.TEXT -> {
                    vm.openTextEditor(boardDown)
                    mode = GestureMode.TAP
                    down.consume()
                }
            }

            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Main)
                val pressed = event.changes.filter { it.pressed }
                if (pressed.isEmpty()) break

                if (pressed.size >= 2) {
                    if (mode != GestureMode.PINCH) {
                        // la prima delle due dita chiude il gesto corrente
                        finish()
                        mode = GestureMode.PINCH
                        pinchCenter = event.centroidOf(pressed)
                        pinchDistance = event.distanceOf(pressed)
                    } else {
                        val center = event.centroidOf(pressed)
                        val distance = event.distanceOf(pressed)
                        if (pinchDistance > 1f) {
                            vm.onPinch(pinchCenter, center, pinchDistance, distance)
                        }
                        pinchCenter = center
                        pinchDistance = distance
                    }
                    pressed.forEach { it.consume() }
                    continue
                }

                val change = pressed.first()
                val position = change.position
                if (mode == GestureMode.TAP || mode == GestureMode.DRAG || mode == GestureMode.RESIZE) {
                    if (!moved && (position - down.position).getDistance() > slop) moved = true
                }

                when (mode) {
                    GestureMode.PAN -> vm.offset = panOrigin + (position - panAnchor)
                    GestureMode.PINCH -> Unit
                    GestureMode.DRAG -> if (moved) vm.pointerMove(board(position))
                    GestureMode.RESIZE -> if (moved) vm.pointerMove(board(position))
                    GestureMode.DRAW -> when {
                        vm.zoomMode -> Unit
                        tool == Tool.SHAPE -> vm.shapeMove(board(position))
                        tool == Tool.ERASER && vm.eraserMode == EraserMode.STROKE ->
                            vm.eraseStrokeAt(board(position))
                        else -> vm.penMove(board(position))
                    }
                    GestureMode.LASSO -> vm.lassoMove(board(position))
                    else -> Unit
                }

                if (mode != GestureMode.TAP && mode != GestureMode.PINCH) change.consume()
            }

            finish()
        }
    }
}

private fun Offset.toPoint() = Point(x, y)

private fun PointerEvent.centroidOf(pressed: List<PointerInputChange>): Offset {
    var sum = Offset.Zero
    for (c in pressed) sum += c.position
    return sum / pressed.size.toFloat()
}

private fun PointerEvent.distanceOf(pressed: List<PointerInputChange>): Float {
    if (pressed.size < 2) return 0f
    var maxDistance = 0f
    for (i in pressed.indices) {
        for (j in i + 1 until pressed.size) {
            val d = (pressed[i].position - pressed[j].position).getDistance()
            if (d > maxDistance) maxDistance = d
        }
    }
    return maxDistance
}
