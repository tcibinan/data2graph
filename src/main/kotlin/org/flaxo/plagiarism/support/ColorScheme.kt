package org.flaxo.plagiarism.support

import io.data2viz.color.Color
import io.data2viz.color.colors

/**
 * Graph default color scheme.
 */
object ColorScheme {

    val blank = Color(alpha = 0.0F)

    val text = colors.black

    object Node {
        val default = Color().withRed(31).withGreen(119).withBlue(180)
        val stroke = colors.white
    }

    object Link {
        val default = colors.lightgray.withAlpha(0.5F)
        val selected = Color().withRed(204).withGreen(255).withBlue(102).withAlpha(0.5F)
    }
}
