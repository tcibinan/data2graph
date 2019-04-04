package org.flaxo.plagiarism.support

import io.data2viz.geom.Point

/**
 * Mouse with mutable point.
 */
class Mouse(
        /**
         * Point where user cursor is.
         */
        var point: Point = Point()
) {

    /**
     * Mouse latest click.
     *
     * It resets click every time it is requested.
     */
    var click: Click? = null
        get() {
            val storedClick = field
            if (storedClick != null) field = null
            return storedClick
        }
}
