package org.flaxo.plagiarism.support

import io.data2viz.geom.Point
import io.data2viz.viz.LineNode
import io.data2viz.viz.Node
import io.data2viz.viz.Viz
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Returns all elements of type [T] that exist in the current visualization.
 */
inline fun <reified T : Node> Viz.all(): List<T> =
        activeLayer.children.asSequence()
                .filter { it is T }
                .map { it as T }
                .toList()

/**
 * Calculates a distance from the line to the given point [m].
 *
 * Ignores the line length as if was an infinite line.
 */
infix fun LineNode.distanceTo(m: Point): Double {
    val l1 = Point(x1, y1)
    val l2 = Point(x2, y2)
    val a = l1.y - l2.y
    val b = l2.x - l1.x
    val c = l1.x * l2.y - l1.y * l2.x
    return abs(a * m.x + b * m.y + c) / (sqrt(a.pow(2) + b.pow(2)))
}

/**
 * Checks if the point lies within the given [line] coordinates.
 */
infix fun Point.inCoordinatesOf(line: LineNode): Boolean {
    val l1 = Point(line.x1, line.y1)
    val l2 = Point(line.x2, line.y2)
    val xMin = min(l1.x, l2.x)
    val xMax = max(l1.x, l2.x)
    val yMin = min(l1.y, l2.y)
    val yMax = max(l1.y, l2.y)
    return x > xMin && x < xMax && y > yMin && y < yMax
}
