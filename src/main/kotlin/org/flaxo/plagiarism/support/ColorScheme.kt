package org.flaxo.plagiarism.support

import io.data2viz.color.Color
import io.data2viz.color.colors

/**
 * Graph default color scheme.
 */
object ColorScheme {
    val blank = Color(alpha = 0.0F)
    val node = Color().withRed(31).withGreen(119).withBlue(180)
    val nodeStroke = colors.white
    val text = colors.black
    val link = colors.lightgray.withAlpha(0.5F)
}