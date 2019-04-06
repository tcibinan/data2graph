package org.flaxo.plagiarism.support

import io.data2viz.color.Colors
import io.data2viz.math.pct

/**
 * Graph default color scheme.
 */
object ColorScheme {

    val blank = Colors.Web.black.withAlpha(0.pct)
    val text = Colors.Web.black

    object Node {
        val default = Colors.rgb(31, 119, 180)
        val stroke = Colors.Web.white
        val selected = Colors.Web.black
    }

    object Link {
        val default = Colors.Web.lightgray.withAlpha(70.pct)
        val selected = Colors.Web.black
    }
}
