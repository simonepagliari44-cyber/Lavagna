package com.simonecompany.lavagna.vm

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import com.simonecompany.lavagna.board.BoardRenderer
import com.simonecompany.lavagna.board.Handle
import com.simonecompany.lavagna.board.boundsOf
import com.simonecompany.lavagna.board.handleAt
import com.simonecompany.lavagna.board.hitTestPath
import com.simonecompany.lavagna.board.objectTouchesLasso
import com.simonecompany.lavagna.board.pathContainsPoint
import com.simonecompany.lavagna.board.pointInPolygon
import com.simonecompany.lavagna.export.BoardExporter
import com.simonecompany.lavagna.model.AccentBlue
import com.simonecompany.lavagna.model.BoardObject
import com.simonecompany.lavagna.model.Bounds
import com.simonecompany.lavagna.model.EraserMode
import com.simonecompany.lavagna.model.PathObject
import com.simonecompany.lavagna.model.PointerMode
import com.simonecompany.lavagna.model.Point
import com.simonecompany.lavagna.model.ShapeObject
import com.simonecompany.lavagna.model.ShapeType
import com.simonecompany.lavagna.model.TextAlign
import com.simonecompany.lavagna.model.TextObject
import com.simonecompany.lavagna.model.Backgrounds
import com.simonecompany.lavagna.model.Tool
import com.simonecompany.lavagna.model.backgroundById
import com.simonecompany.lavagna.model.isAspectLocked
import kotlin.math.abs
import kotlin.math.max

/** Stato dell'editor di testo sovrapposto alla lavagna. */
data class TextEditState(
    val index: Int,
    val x: Float,
    val y: Float,
    val text: String,
    val boxWidth: Float,
    val boxHeight: Float,
)

/** Menu contestuale aperto su un oggetto (ancoraggio in coordinate lavagna). */
data class ContextMenuState(val index: Int, val at: Point)

/** Cosa e' iniziato dal tocco del puntatore. */
enum class PointerGesture { NONE, DRAG, RESIZE }

/**
 * Stato e logica della lavagna (porting di index.html).
 */
class BoardViewModel(app: Application) : AndroidViewModel(app) {

    // ---------------------------------------------------------------- stato
    var pages: List<List<BoardObject>> by mutableStateOf(listOf(emptyList()))
    var pageIndex: Int by mutableStateOf(0)

    private val undoStack = mutableStateListOf<List<BoardObject>>()
    private val redoStack = mutableStateListOf<List<BoardObject>>()

    var tool: Tool by mutableStateOf(Tool.POINTER)
    var eraserMode: EraserMode by mutableStateOf(EraserMode.NORMAL)
    var pointerMode: PointerMode by mutableStateOf(PointerMode.NORMAL)

    var inkColor: Color by mutableStateOf(AccentBlue)
    var strokeWidth: Float by mutableStateOf(3f)
    var shapeType: ShapeType by mutableStateOf(ShapeType.RECT)
    var backgroundColor: Color by mutableStateOf(Color.White)
    var backgroundId: String by mutableStateOf("solid_white")
    var customColors: List<Color> by mutableStateOf(emptyList())

    var selectedIndex: Int by mutableStateOf(-1)
    var multiSelection: Set<Int> by mutableStateOf(emptySet())
    var zoomMode: Boolean by mutableStateOf(false)
    var scale: Float by mutableStateOf(1f)
    var offset: Offset by mutableStateOf(Offset.Zero)

    var fontName: String by mutableStateOf("Roboto")
    var fontSize: Float by mutableStateOf(24f)
    var bold: Boolean by mutableStateOf(false)
    var italic: Boolean by mutableStateOf(false)
    var align: TextAlign by mutableStateOf(TextAlign.LEFT)

    // ---------------------------------------------------------- transitori
    private val livePoints = mutableStateListOf<Point>()
    private var liveColor: Color by mutableStateOf(AccentBlue)
    private var liveWidth: Float by mutableStateOf(3f)
    var tempShape: ShapeObject? by mutableStateOf(null)
    private val lassoPoints = mutableStateListOf<Point>()
    var lassoIsSelection: Boolean by mutableStateOf(false)
    var textEdit: TextEditState? by mutableStateOf(null)
    var contextMenu: ContextMenuState? by mutableStateOf(null)

    val livePathPoints: List<Point> get() = livePoints
    val livePathColor: Color get() = liveColor
    val livePathWidth: Float get() = liveWidth
    val lasso: List<Point> get() = lassoPoints

    private var dragIndex = -1
    private var dragOffsetX = 0f
    private var dragOffsetY = 0f
    private var dragStart = Point(0f, 0f)
    private var dragStartCenterX = 0f
    private var dragStartCenterY = 0f
    private var flipXBase = false
    private var flipYBase = false
    private var resizeHandle: Handle? = null
    private var resizeStartBounds: Bounds? = null
    private var lassoActive = false
    private var strokeEraserDirty = false

    private val renderer = BoardRenderer()
    private val exporter = BoardExporter(renderer)

    // ------------------------------------------------------------- utilita'
    fun objects(): List<BoardObject> = pages.getOrElse(pageIndex) { emptyList() }

    private fun setObjects(list: List<BoardObject>) {
        val copy = pages.toMutableList()
        copy[pageIndex] = list
        pages = copy
    }

    fun screenToBoard(p: Offset): Offset = Offset((p.x - offset.x) / scale, (p.y - offset.y) / scale)

    fun boardToScreen(p: Offset): Offset = Offset(p.x * scale + offset.x, p.y * scale + offset.y)

    fun canUndo(): Boolean = undoStack.isNotEmpty()

    fun canRedo(): Boolean = redoStack.isNotEmpty()

    fun clearSelection() {
        selectedIndex = -1
        multiSelection = emptySet()
    }

    // -------------------------------------------------------------- history
    fun saveHistory() {
        undoStack.add(objects())
        redoStack.clear()
    }

    private fun clearHistory() {
        undoStack.clear()
        redoStack.clear()
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        redoStack.add(objects())
        setObjects(undoStack.removeAt(undoStack.lastIndex))
        clearSelection()
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        undoStack.add(objects())
        setObjects(redoStack.removeAt(redoStack.lastIndex))
        clearSelection()
    }

    // --------------------------------------------------------------- tools
    fun selectTool(next: Tool) {
        if (next != Tool.TEXT) commitText()
        contextMenu = null
        tool = next
        if (next != Tool.POINTER) clearSelection()
        if (zoomMode) {
            zoomMode = false
            resetZoom()
        }
    }

    fun selectShape(type: ShapeType) {
        shapeType = type
        selectTool(Tool.SHAPE)
    }

    fun selectColor(c: Color) {
        inkColor = c
        if (selectedIndex >= 0 && tool == Tool.POINTER) {
            val list = objects().toMutableList()
            if (selectedIndex < list.size) {
                saveHistory()
                val o = list[selectedIndex]
                list[selectedIndex] = when (o) {
                    is PathObject -> o.copy(color = c)
                    is ShapeObject -> o.copy(color = c)
                    is TextObject -> o.copy(color = c)
                }
                setObjects(list)
            }
        }
    }

    private var widthDragActive = false

    fun updateStrokeWidth(w: Float) {
        strokeWidth = w
        if (selectedIndex >= 0 && tool == Tool.POINTER) {
            val list = objects().toMutableList()
            if (selectedIndex < list.size) {
                if (!widthDragActive) {
                    widthDragActive = true
                    saveHistory()
                }
                val o = list[selectedIndex]
                list[selectedIndex] = when (o) {
                    is PathObject -> o.copy(strokeWidth = w)
                    is ShapeObject -> o.copy(strokeWidth = w)
                    is TextObject -> o
                }
                setObjects(list)
            }
        }
    }

    /** Chiusura del gesto dello slider: la prossima modifica apre un nuovo passo di history. */
    fun onStrokeWidthDragEnd() {
        widthDragActive = false
    }

    fun addCustomColor(c: Color) {
        val next = (customColors + c).takeLast(7)
        customColors = next
        selectColor(c)
    }

    fun removeCustomColor(c: Color) {
        customColors = customColors - c
    }

    // ---------------------------------------------------------------- zoom
    fun toggleZoomMode() {
        zoomMode = !zoomMode
        if (!zoomMode) resetZoom()
    }

    fun zoomBy(factor: Float) {
        scale = scale * factor
    }

    fun resetZoom() {
        scale = 1f
        offset = Offset.Zero
    }

    fun onPinch(from: Offset, to: Offset, distFrom: Float, distTo: Float) {
        val anchor = screenToBoard(from)
        val next = if (distFrom > 1f) (scale * (distTo / distFrom)) else scale
        scale = next
        offset = Offset(to.x - anchor.x * next, to.y - anchor.y * next)
    }

    // -------------------------------------------------------------- pennello
    fun penDown(p: Point) {
        if (tool == Tool.PEN || (tool == Tool.ERASER && eraserMode == EraserMode.NORMAL)) {
            val erasing = tool == Tool.ERASER
            liveColor = if (erasing) backgroundColor else inkColor
            liveWidth = if (erasing) max(strokeWidth, 12f) else strokeWidth
            saveHistory()
            livePoints.clear()
            livePoints.add(p)
        }
    }

    fun penMove(p: Point) {
        if (livePoints.isNotEmpty()) livePoints.add(p)
    }

    fun penUp() {
        if (livePoints.size >= 2) {
            setObjects(objects() + PathObject(livePoints.toList(), liveColor, liveWidth))
        }
        livePoints.clear()
    }

    // --------------------------------------------------------------- forme
    fun shapeDown(p: Point) {
        saveHistory()
        dragStart = p
        tempShape = ShapeObject(shapeType, p.x, p.y, 0f, 0f, inkColor, strokeWidth)
    }

    fun shapeMove(p: Point) {
        val t = tempShape ?: return
        var w = p.x - dragStart.x
        var h = p.y - dragStart.y
        if (t.shapeType.isAspectLocked()) {
            val s = max(abs(w), abs(h))
            w = s * if (w < 0) -1f else 1f
            h = s * if (h < 0) -1f else 1f
        }
        tempShape = t.copy(w = w, h = h)
    }

    fun shapeUp() {
        val t = tempShape ?: return
        tempShape = null
        val b = boundsOf(t)
        if (b.w > 1f && b.h > 1f) setObjects(objects() + t)
    }

    // ---------------------------------------------------------------- lazo
    fun lassoDown(p: Point, forSelection: Boolean) {
        lassoIsSelection = forSelection
        lassoActive = true
        lassoPoints.clear()
        lassoPoints.add(p)
    }

    fun lassoMove(p: Point) {
        if (lassoActive) lassoPoints.add(p)
    }

    fun lassoUp() {
        lassoActive = false
        val lasso = lassoPoints.toList()
        lassoPoints.clear()
        if (lasso.size <= 2) return
        if (lassoIsSelection) {
            val objs = objects()
            multiSelection = objs.indices.filter { objectTouchesLasso(objs[it], lasso) }.toSet()
            selectedIndex = -1
        } else {
            saveHistory()
            val result = ArrayList<BoardObject>(objects().size)
            for (obj in objects()) {
                if (!objectTouchesLasso(obj, lasso)) {
                    result.add(obj)
                    continue
                }
                when (obj) {
                    is ShapeObject -> result.add(obj.withClips(lasso))
                    is PathObject -> {
                        val inside = obj.points.count { pointInPolygon(it.x, it.y, lasso) }
                        if (inside >= obj.points.size) continue
                        result.add(obj.withClips(lasso))
                    }
                    is TextObject -> result.add(obj)
                }
            }
            setObjects(result)
        }
    }

    // ---------------------------------------------------------- riempimento
    fun fillAt(p: Point) {
        val objs = objects()
        for (i in objs.indices.reversed()) {
            val o = objs[i]
            val hit = when (o) {
                is PathObject -> pathContainsPoint(o, p.x, p.y)
                is ShapeObject -> boundsOf(o).containsPoint(p.x, p.y)
                is TextObject -> false
            }
            if (hit) {
                val list = objs.toMutableList()
                when (val cur = list[i]) {
                    is PathObject -> list[i] = cur.copy(filled = true, fillColor = inkColor)
                    is ShapeObject -> Unit
                    is TextObject -> Unit
                }
                saveHistory()
                setObjects(list)
                return
            }
        }
    }

    fun paintBackground() {
        backgroundColor = inkColor
        backgroundId = ""
    }

    fun applyBackground(id: String) {
        backgroundId = id
        if (id == "none" || id.isEmpty()) {
            backgroundColor = Color.White
            return
        }
        val bg = backgroundById(id)
        backgroundColor = if (bg.type.name == "SOLID") bg.solid else Color.White
    }

    // ----------------------------------------------------------------- lazo
    fun pointerDown(p: Point): PointerGesture {
        contextMenu = null
        val objs = objects()
        val hit = topIndexAt(objs, p.x, p.y)

        if (selectedIndex >= 0 && selectedIndex < objs.size && hit == selectedIndex) {
            val handle = handleAt(boundsOf(objs[selectedIndex]), p, 10f / scale)
            if (handle != null) {
                saveHistory()
                resizeHandle = handle
                resizeStartBounds = boundsOf(objs[selectedIndex])
                dragStart = p
                return PointerGesture.RESIZE
            }
        }

        if (multiSelection.isNotEmpty()) {
            // come nella web app: toccare un elemento della selezione multi apre il menu contestuale
            if (hit >= 0 && hit in multiSelection) {
                contextMenu = ContextMenuState(hit, p)
                return PointerGesture.NONE
            }
            multiSelection = emptySet()
        }

        if (selectedIndex >= 0 && hit == selectedIndex) {
            contextMenu = ContextMenuState(selectedIndex, p)
            return PointerGesture.NONE
        }

        if (hit >= 0) {
            selectedIndex = hit
            beginDrag(hit, p)
            return PointerGesture.DRAG
        }
        selectedIndex = -1
        return PointerGesture.NONE
    }

    private fun topIndexAt(objs: List<BoardObject>, x: Float, y: Float): Int {
        for (i in objs.indices.reversed()) {
            if (boundsOf(objs[i]).containsPoint(x, y, 10f)) return i
        }
        return -1
    }

    private fun beginDrag(index: Int, p: Point) {
        val objs = objects()
        val o = objs[index]
        val b = boundsOf(o)
        dragIndex = index
        dragOffsetX = p.x - when (o) {
            is ShapeObject -> o.x
            is TextObject -> o.x
            is PathObject -> b.x
        }
        dragOffsetY = p.y - when (o) {
            is ShapeObject -> o.y
            is TextObject -> o.y
            is PathObject -> b.y
        }
        dragStart = p
        dragStartCenterX = b.centerX
        dragStartCenterY = b.centerY
        flipXBase = o.flipX
        flipYBase = o.flipY
        saveHistory()
    }

    fun pointerMove(p: Point) {
        if (resizeHandle != null) {
            resizeTo(p)
            return
        }
        if (dragIndex < 0) return
        val objs = objects().toMutableList()
        if (multiSelection.isNotEmpty()) {
            val dx = p.x - dragStart.x
            val dy = p.y - dragStart.y
            for (i in multiSelection) {
                if (i in objs.indices) objs[i] = objs[i].translated(dx, dy)
            }
            dragStart = p
            setObjects(objs)
            return
        }
        val i = dragIndex
        if (i !in objs.indices) return
        val o = objs[i]
        when (o) {
            is ShapeObject -> objs[i] = o.copy(x = p.x - dragOffsetX, y = p.y - dragOffsetY)
            is TextObject -> objs[i] = o.copy(x = p.x - dragOffsetX, y = p.y - dragOffsetY)
            is PathObject -> {
                val dx = p.x - dragStart.x
                val dy = p.y - dragStart.y
                objs[i] = o.copy(points = o.points.map { Point(it.x + dx, it.y + dy) })
                dragStart = p
            }
        }
        val flipX = (dragStart.x - dragStartCenterX) * (p.x - dragStartCenterX) < 0
        val flipY = (dragStart.y - dragStartCenterY) * (p.y - dragStartCenterY) < 0
        objs[i] = when (val cur = objs[i]) {
            is ShapeObject -> cur.copy(flipX = if (flipX) !flipXBase else flipXBase, flipY = if (flipY) !flipYBase else flipYBase)
            is PathObject -> cur.copy(flipX = if (flipX) !flipXBase else flipXBase, flipY = if (flipY) !flipYBase else flipYBase)
            is TextObject -> cur.copy(flipX = if (flipX) !flipXBase else flipXBase, flipY = if (flipY) !flipYBase else flipYBase)
        }
        setObjects(objs)
    }

    private fun resizeTo(p: Point) {
        val i = selectedIndex
        val objs = objects().toMutableList()
        if (i !in objs.indices) return
        val b = resizeStartBounds ?: return
        var l = b.x; var t = b.y; var r = b.right; var bt = b.bottom
        when (resizeHandle) {
            Handle.TL -> { l = p.x; t = p.y }
            Handle.TR -> { r = p.x; t = p.y }
            Handle.BR -> { r = p.x; bt = p.y }
            Handle.BL -> { l = p.x; bt = p.y }
            null -> return
        }
        var nl = minOf(l, r)
        var nt = minOf(t, bt)
        var nw = abs(r - l)
        var nh = abs(bt - t)
        val o = objs[i]
        if (o is ShapeObject && o.shapeType.isAspectLocked()) {
            val s = max(nw, nh)
            when (resizeHandle) {
                Handle.TL -> { nl = b.right - s; nt = b.bottom - s }
                Handle.TR -> { nl = b.x; nt = b.bottom - s }
                Handle.BR -> { nl = b.x; nt = b.y }
                Handle.BL -> { nl = b.right - s; nt = b.y }
                null -> {}
            }
            nw = s; nh = s
        }
        objs[i] = when (val cur = objs[i]) {
            is ShapeObject -> cur.copy(x = nl, y = nt, w = nw, h = nh)
            is TextObject -> cur.copy(x = nl, y = nt, w = nw, h = nh)
            is PathObject -> cur
        }
        setObjects(objs)
    }

    fun pointerUp() {
        dragIndex = -1
        resizeHandle = null
        resizeStartBounds = null
        strokeEraserDirty = false
    }

    /** Doppio tocco: modifica il testo se l'oggetto e' un blocco di testo. */
    fun onDoubleTap(p: Point) {
        val objs = objects()
        val hit = topIndexAt(objs, p.x, p.y)
        if (hit >= 0 && objs[hit] is TextObject) openTextEditor(p, hit)
    }

    /** Pressione prolungata: apre il menu contestuale. */
    fun onLongPress(p: Point) {
        val objs = objects()
        val hit = topIndexAt(objs, p.x, p.y)
        if (hit >= 0) {
            contextMenu = ContextMenuState(hit, p)
        }
    }

    // -------------------------------------------------------- menu contestuale
    fun closeContextMenu() {
        contextMenu = null
    }

    fun contextDuplicate() {
        val index = contextMenu?.index ?: return
        val objs = objects()
        if (index !in objs.indices) { contextMenu = null; return }
        saveHistory()
        val copy = objs[index].translated(20f, 20f)
        setObjects(objs + copy)
        selectedIndex = objs.size
        contextMenu = null
    }

    fun contextBringToFront() {
        val index = contextMenu?.index ?: return
        val objs = objects().toMutableList()
        if (index !in objs.indices) { contextMenu = null; return }
        saveHistory()
        val o = objs.removeAt(index)
        objs.add(o)
        setObjects(objs)
        selectedIndex = objs.size - 1
        contextMenu = null
    }

    fun contextSendToBack() {
        val index = contextMenu?.index ?: return
        val objs = objects().toMutableList()
        if (index !in objs.indices) { contextMenu = null; return }
        saveHistory()
        val o = objs.removeAt(index)
        objs.add(0, o)
        setObjects(objs)
        selectedIndex = 0
        contextMenu = null
    }

    fun contextDelete() {
        val index = contextMenu?.index ?: return
        val objs = objects().toMutableList()
        if (index !in objs.indices) { contextMenu = null; return }
        saveHistory()
        objs.removeAt(index)
        setObjects(objs)
        clearSelection()
        contextMenu = null
    }

    fun deleteSelection() {
        val objs = objects().toMutableList()
        val targets = if (multiSelection.isNotEmpty()) multiSelection.toList()
        else if (selectedIndex >= 0) listOf(selectedIndex) else return
        if (targets.isEmpty()) return
        saveHistory()
        for (i in targets.sortedDescending()) if (i in objs.indices) objs.removeAt(i)
        setObjects(objs)
        clearSelection()
    }

    fun selectAll() {
        multiSelection = objects().indices.toSet()
        selectedIndex = -1
    }

    // ---------------------------------------------------------- gomma a tratto
    fun eraseStrokeAt(p: Point) {
        val objs = objects().toMutableList()
        val thickness = max(10f, strokeWidth) / scale
        var target = -1
        for (i in objs.indices.reversed()) {
            val o = objs[i]
            val hit = when (o) {
                is PathObject -> hitTestPath(o, p.x, p.y, thickness)
                is ShapeObject -> boundsOf(o).containsPoint(p.x, p.y, thickness)
                is TextObject -> boundsOf(o).containsPoint(p.x, p.y, thickness)
            }
            if (hit) {
                target = i
                break
            }
        }
        if (target >= 0) {
            if (!strokeEraserDirty) {
                saveHistory()
                strokeEraserDirty = true
            }
            objs.removeAt(target)
            setObjects(objs)
            clearSelection()
        }
    }

    // ---------------------------------------------------------------- testo
    fun openTextEditor(p: Point, index: Int = -1) {
        commitText()
        val objs = objects()
        if (index in objs.indices) {
            val o = objs[index]
            if (o is TextObject) {
                fontSize = o.fontSize
                fontName = o.font
                bold = o.bold
                italic = o.italic
                align = o.align
                inkColor = o.color
                textEdit = TextEditState(index, o.x, o.y, o.text, o.w, o.h)
                return
            }
        }
        textEdit = TextEditState(-1, p.x, p.y, "", 0f, 0f)
    }

    fun updateEditingText(value: String) {
        val e = textEdit ?: return
        textEdit = e.copy(text = value)
    }

    fun updateEditingBox(w: Float, h: Float) {
        val e = textEdit ?: return
        if (e.boxWidth == 0f && e.boxHeight == 0f) textEdit = e.copy(boxWidth = w, boxHeight = h)
    }

    fun commitText() {
        val e = textEdit ?: return
        textEdit = null
        if (e.text.isBlank()) return
        saveHistory()
        val obj = TextObject(
            text = e.text,
            x = e.x,
            y = e.y,
            w = max(e.boxWidth, 60f),
            h = max(e.boxHeight, fontSize * 1.2f),
            color = inkColor,
            font = fontName,
            fontSize = fontSize,
            bold = bold,
            italic = italic,
            align = align
        )
        val list = objects().toMutableList()
        if (e.index in list.indices) list[e.index] = obj else list.add(obj)
        setObjects(list)
    }

    fun cancelText() {
        textEdit = null
    }

    fun toggleBold() { bold = !bold }

    fun toggleItalic() { italic = !italic }

    fun applyAlign(a: TextAlign) { align = a }

    // --------------------------------------------------------------- pagine
    fun addPage() {
        commitText()
        pages = pages + listOf(emptyList())
        pageIndex = pages.size - 1
        clearSelection()
        clearHistory()
    }

    fun switchPage(index: Int) {
        commitText()
        if (index !in pages.indices) return
        pageIndex = index
        clearSelection()
        clearHistory()
    }

    fun prevPage() = switchPage(pageIndex - 1)

    fun nextPage() = switchPage(pageIndex + 1)

    fun deletePage(index: Int) {
        commitText()
        if (index !in pages.indices) return
        if (pages.size == 1) {
            setObjects(emptyList())
            clearSelection()
            return
        }
        pages = pages.filterIndexed { i, _ -> i != index }
        if (pageIndex >= pages.size) pageIndex = pages.size - 1
        clearSelection()
        clearHistory()
    }

    fun clearPage() {
        commitText()
        saveHistory()
        setObjects(emptyList())
        clearSelection()
    }

    fun clearAllPages() {
        commitText()
        pages = listOf(emptyList())
        pageIndex = 0
        clearSelection()
        clearHistory()
    }

    fun pageCount(): Int = pages.size

    // -------------------------------------------------------------- export
    var canvasWidth: Int by mutableStateOf(0)
    var canvasHeight: Int by mutableStateOf(0)

    fun renderBitmap(width: Int, height: Int): Bitmap {
        val metrics = getApplication<Application>().resources.displayMetrics
        val w = if (width > 0) width else metrics.widthPixels
        val h = if (height > 0) height else metrics.heightPixels
        return exporter.render(this, w, h)
    }

    fun writeImage(uri: android.net.Uri, format: com.simonecompany.lavagna.export.ImageFormat): Boolean =
        exporter.writeImage(getApplication(), uri, renderBitmap(canvasWidth, canvasHeight), format)

    fun writePdf(uri: android.net.Uri): Boolean =
        exporter.writePdf(getApplication(), uri, renderBitmap(canvasWidth, canvasHeight))

    // --------------------------------------------------------- persistenza
    fun save() {
        runCatching { BoardSerializer.save(this, getApplication<Application>().filesDir) }
    }

    fun load() {
        runCatching { BoardSerializer.load(this, getApplication<Application>().filesDir) }
    }
}