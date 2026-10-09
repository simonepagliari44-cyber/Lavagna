package com.simonecompany.lavagna.vm

import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Color
import com.simonecompany.lavagna.model.BoardObject
import com.simonecompany.lavagna.model.EraserMode
import com.simonecompany.lavagna.model.PathObject
import com.simonecompany.lavagna.model.Point
import com.simonecompany.lavagna.model.PointerMode
import com.simonecompany.lavagna.model.ShapeObject
import com.simonecompany.lavagna.model.ShapeType
import com.simonecompany.lavagna.model.TextAlign
import com.simonecompany.lavagna.model.TextObject
import com.simonecompany.lavagna.model.Tool
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Salvataggio locale della lavagna (file privato dell'app): la lavagna
 * sopravvive alla chiusura del processo esattamente come nel web, dove lo stato
 * vive nella pagina, e qui viene ripristinato al riavvio successivo.
 */
object BoardSerializer {

    private const val PATH = "lavagna/board.json"

    fun save(vm: BoardViewModel, filesDir: File) {
        val root = JSONObject()
        val pages = JSONArray()
        vm.pages.forEach { objs -> pages.put(objectsToJson(objs)) }
        root.put("pages", pages)
        root.put("pageIndex", vm.pageIndex)
        root.put("background", vm.backgroundColor.value.toLong())
        root.put("backgroundId", vm.backgroundId)
        root.put("ink", vm.inkColor.value.toLong())
        root.put("width", vm.strokeWidth.toDouble())
        root.put("shape", vm.shapeType.key)
        root.put("tool", vm.tool.name)
        root.put("eraser", vm.eraserMode.name)
        root.put("pointer", vm.pointerMode.name)
        root.put("font", vm.fontName)
        root.put("fontSize", vm.fontSize.toDouble())
        root.put("bold", vm.bold)
        root.put("italic", vm.italic)
        root.put("align", vm.align.name)
        val customs = JSONArray()
        vm.customColors.forEach { customs.put(it.value.toLong()) }
        root.put("customs", customs)

        val file = File(filesDir, PATH)
        file.parentFile?.mkdirs()
        file.writeText(root.toString())
    }

    fun load(vm: BoardViewModel, filesDir: File) {
        val file = File(filesDir, PATH)
        if (!file.exists()) return
        val root = JSONObject(file.readText())

        val pagesArray = root.optJSONArray("pages") ?: return
        val pages = ArrayList<List<BoardObject>>(pagesArray.length())
        for (i in 0 until pagesArray.length()) {
            val array = pagesArray.optJSONArray(i) ?: JSONArray()
            val list = ArrayList<BoardObject>(array.length())
            for (j in 0 until array.length()) {
                array.optJSONObject(j)?.let { jsonToObject(it)?.let(list::add) }
            }
            pages.add(list)
        }
        if (pages.isEmpty()) return
        vm.pages = pages
        vm.pageIndex = root.optInt("pageIndex", 0).coerceIn(0, pages.size - 1)
        if (root.has("background")) vm.backgroundColor = Color(root.optLong("background").toULong())
        if (root.has("backgroundId")) vm.backgroundId = root.optString("backgroundId", "solid_white")
        if (root.has("ink")) vm.inkColor = Color(root.optLong("ink").toULong())
        if (root.has("width")) vm.strokeWidth = root.optDouble("width", 3.0).toFloat()
        if (root.has("shape")) vm.shapeType = ShapeType.fromKey(root.optString("shape"))
        if (root.has("tool")) vm.tool = runCatching { Tool.valueOf(root.optString("tool")) }.getOrDefault(Tool.PEN)
        if (root.has("eraser")) vm.eraserMode = runCatching { EraserMode.valueOf(root.optString("eraser")) }.getOrDefault(EraserMode.NORMAL)
        if (root.has("pointer")) vm.pointerMode = runCatching { PointerMode.valueOf(root.optString("pointer")) }.getOrDefault(PointerMode.NORMAL)
        if (root.has("font")) vm.fontName = root.optString("font")
        if (root.has("fontSize")) vm.fontSize = root.optDouble("fontSize", 24.0).toFloat()
        vm.bold = root.optBoolean("bold", false)
        vm.italic = root.optBoolean("italic", false)
        if (root.has("align")) vm.align = runCatching { TextAlign.valueOf(root.optString("align")) }.getOrDefault(TextAlign.LEFT)
        val customs = root.optJSONArray("customs")
        if (customs != null) {
            vm.customColors = (0 until customs.length()).map { Color(customs.optLong(it).toULong()) }
        }
    }

    // ------------------------------------------------------------------ json
    private fun objectsToJson(objects: List<BoardObject>): JSONArray {
        val array = JSONArray()
        objects.forEach { array.put(objectToJson(it)) }
        return array
    }

    private fun objectToJson(obj: BoardObject): JSONObject = JSONObject().apply {
        when (obj) {
            is PathObject -> {
                put("t", "path")
                put("c", obj.color.toArgbLong())
                put("w", obj.strokeWidth.toDouble())
                put("f", obj.filled)
                obj.fillColor?.let { put("fc", it.toArgbLong()) }
                val pts = JSONArray()
                obj.points.forEach { p -> pts.put(JSONArray().put(p.x.toDouble()).put(p.y.toDouble())) }
                put("p", pts)
                putFlips(obj)
                putClips(obj)
            }
            is ShapeObject -> {
                put("t", "shape")
                put("s", obj.shapeType.key)
                put("x", obj.x.toDouble())
                put("y", obj.y.toDouble())
                put("w", obj.w.toDouble())
                put("h", obj.h.toDouble())
                put("c", obj.color.toArgbLong())
                put("sw", obj.strokeWidth.toDouble())
                putFlips(obj)
                putClips(obj)
            }
            is TextObject -> {
                put("t", "text")
                put("tx", obj.text)
                put("x", obj.x.toDouble())
                put("y", obj.y.toDouble())
                put("w", obj.w.toDouble())
                put("h", obj.h.toDouble())
                put("c", obj.color.toArgbLong())
                put("fn", obj.font)
                put("fs", obj.fontSize.toDouble())
                put("b", obj.bold)
                put("i", obj.italic)
                put("al", obj.align.name)
                putFlips(obj)
                putClips(obj)
            }
        }
    }

    private fun JSONObject.putFlips(obj: BoardObject) {
        put("fx", obj.flipX)
        put("fy", obj.flipY)
    }

    private fun JSONObject.putClips(obj: BoardObject) {
        if (obj.clips.isEmpty()) return
        val outer = JSONArray()
        obj.clips.forEach { lasso ->
            val inner = JSONArray()
            lasso.forEach { p -> inner.put(JSONArray().put(p.x.toDouble()).put(p.y.toDouble())) }
            outer.put(inner)
        }
        put("cl", outer)
    }

    private fun jsonToObject(json: JSONObject): BoardObject? {
        val clips = parseClips(json.optJSONArray("cl"))
        val flipX = json.optBoolean("fx")
        val flipY = json.optBoolean("fy")
        val color = Color(json.optLong("c", AndroidColor.BLACK.toLong()).toULong())
        return when (json.optString("t")) {
            "path" -> {
                val pointsArray = json.optJSONArray("p") ?: return null
                val points = ArrayList<Point>(pointsArray.length())
                for (i in 0 until pointsArray.length()) {
                    val pair = pointsArray.optJSONArray(i) ?: continue
                    points.add(Point(pair.optDouble(0).toFloat(), pair.optDouble(1).toFloat()))
                }
                PathObject(
                    points = points,
                    color = color,
                    strokeWidth = json.optDouble("w", 3.0).toFloat(),
                    filled = json.optBoolean("f"),
                    fillColor = if (json.has("fc")) Color(json.optLong("fc").toULong()) else null,
                    flipX = flipX,
                    flipY = flipY,
                    clips = clips
                )
            }
            "shape" -> ShapeObject(
                shapeType = ShapeType.fromKey(json.optString("s")),
                x = json.optDouble("x").toFloat(),
                y = json.optDouble("y").toFloat(),
                w = json.optDouble("w").toFloat(),
                h = json.optDouble("h").toFloat(),
                color = color,
                strokeWidth = json.optDouble("sw", 3.0).toFloat(),
                flipX = flipX,
                flipY = flipY,
                clips = clips
            )
            "text" -> TextObject(
                text = json.optString("tx"),
                x = json.optDouble("x").toFloat(),
                y = json.optDouble("y").toFloat(),
                w = json.optDouble("w", 100.0).toFloat(),
                h = json.optDouble("h", 30.0).toFloat(),
                color = color,
                font = json.optString("fn", "Roboto"),
                fontSize = json.optDouble("fs", 24.0).toFloat(),
                bold = json.optBoolean("b"),
                italic = json.optBoolean("i"),
                align = runCatching { TextAlign.valueOf(json.optString("al", "LEFT")) }.getOrDefault(TextAlign.LEFT),
                flipX = flipX,
                flipY = flipY,
                clips = clips
            )
            else -> null
        }
    }

    private fun parseClips(array: JSONArray?): List<List<Point>> {
        if (array == null || array.length() == 0) return emptyList()
        val result = ArrayList<List<Point>>(array.length())
        for (i in 0 until array.length()) {
            val inner = array.optJSONArray(i) ?: continue
            val lasso = ArrayList<Point>(inner.length())
            for (j in 0 until inner.length()) {
                val pair = inner.optJSONArray(j) ?: continue
                lasso.add(Point(pair.optDouble(0).toFloat(), pair.optDouble(1).toFloat()))
            }
            if (lasso.size >= 3) result.add(lasso)
        }
        return result
    }
}

/** ARGB a 32 bit senza segno, per JSON. */
private fun Color.toArgbLong(): Long {
    val argb = android.graphics.Color.argb(
        (alpha * 255f).toInt().coerceIn(0, 255),
        (red * 255f).toInt().coerceIn(0, 255),
        (green * 255f).toInt().coerceIn(0, 255),
        (blue * 255f).toInt().coerceIn(0, 255),
    )
    return argb.toLong() and 0xFFFFFFFFL
}