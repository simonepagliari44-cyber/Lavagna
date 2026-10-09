package com.simonecompany.lavagna.board

import android.annotation.SuppressLint

import android.graphics.Typeface
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * I 50 font della web app. Android non ha le stesse famiglie: ogni nome viene
 * mappato sul typeface di sistema piu' vicino, mantenendo l'elenco identico.
 */
object Fonts {

    val NAMES: List<String> = listOf(
        "Roboto", "Arial", "Helvetica", "Times New Roman", "Courier New", "Georgia", "Verdana",
        "Trebuchet MS", "Tahoma", "Comic Sans MS", "Impact", "Palatino", "Garamond", "Bookman",
        "Consolas", "Monaco", "Cambria", "Calibri", "Segoe UI", "Futura", "Gill Sans", "Optima",
        "Copperplate", "Brush Script MT", "Pacifico", "Lobster", "Dancing Script", "Indie Flower",
        "Shadows Into Light", "Amatic SC", "Playfair Display", "Lora", "Montserrat", "Open Sans",
        "Raleway", "Poppins", "Nunito", "Source Sans Pro", "Ubuntu", "PT Sans", "Merriweather",
        "Oswald", "Quicksand", "Josefin Sans", "Cabin", "Bitter", "Arvo", "Domine", "Karla",
        "Rubik", "Work Sans"
    )

    private val serifFonts = setOf(
        "Times New Roman", "Georgia", "Garamond", "Palatino", "Bookman", "Cambria",
        "Merriweather", "Lora", "Playfair Display", "Bitter", "Amatic SC",
        "Brush Script MT", "Pacifico", "Lobster", "Dancing Script", "Indie Flower",
        "Shadows Into Light"
    )

    private val monoFonts = setOf("Courier New", "Consolas", "Monaco")

    fun baseTypeface(name: String): Typeface = when {
        monoFonts.contains(name) -> Typeface.MONOSPACE
        serifFonts.contains(name) -> Typeface.SERIF
        else -> Typeface.SANS_SERIF
    }

    @SuppressLint("WrongConstant")
    fun typeface(name: String, bold: Boolean, italic: Boolean): Typeface {
        var style = Typeface.NORMAL
        if (bold) style = style or Typeface.BOLD
        if (italic) style = style or Typeface.ITALIC
        return Typeface.create(baseTypeface(name), style)
    }

    fun fontFamily(name: String, bold: Boolean, italic: Boolean): FontFamily =
        FontFamily(typeface(name, bold, italic))

    fun composeStyle(name: String, size: Float, bold: Boolean, italic: Boolean): TextStyle = TextStyle(
        fontFamily = fontFamily(name, bold, italic),
        fontSize = size.sp,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
    )
}