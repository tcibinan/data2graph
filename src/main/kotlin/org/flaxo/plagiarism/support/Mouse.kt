package org.flaxo.plagiarism.support

import io.data2viz.geom.Point

/**
 * Mouse with mutable point.
 */
class Mouse(
        /**
         * Point where user cursor is.
         */
        var point: Point = Point(),

        /**
         * Mouse clicked state.
         *
         * It should be set in the reverse state while handling the click.
         */
        var clicked: Boolean = false
)
