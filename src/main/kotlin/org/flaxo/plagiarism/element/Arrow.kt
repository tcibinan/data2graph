package org.flaxo.plagiarism.element

import io.data2viz.force.ForceNode
import io.data2viz.viz.Line
import org.flaxo.plagiarism.model.Direction
import org.flaxo.plagiarism.model.GraphLink
import org.flaxo.plagiarism.support.ColorScheme
import org.flaxo.plagiarism.support.Configuration
import kotlin.math.sqrt

/**
 * Arrow abstraction.
 *
 * It consists of [line] and two [tails] and has an associated graph [link].
 *
 * If no direction is provided or requested than it is just a line.
 */
class Arrow(val line: Line, val tails: Pair<Line, Line>, val link: GraphLink) {

    /**
     * Shows all required components of the arrow.
     */
    fun show(directed: Boolean) {
        components(directed).forEach { it.style.stroke = ColorScheme.Link.default }
        if (!directed) tails.toList().forEach { it.style.stroke = ColorScheme.blank }
    }

    /**
     * Hides all components of the arrow.
     */
    fun hide() = components(withTails = true).forEach { it.style.stroke = ColorScheme.blank }

    /**
     * Highlights all required components of the arrow.
     */
    fun select(directed: Boolean) = components(directed).forEach { it.style.stroke = ColorScheme.Link.selected }

    /**
     * Updates coordinates of all required components of the arrow.
     */
    fun update(sourceNode: ForceNode, targetNode: ForceNode, directed: Boolean) {
        with(line) {
            x1 = sourceNode.x
            y1 = sourceNode.y
            x2 = targetNode.x
            y2 = targetNode.y
        }

        if (directed && link.directedTo != null) updateTails(link.directedTo)
    }

    private fun updateTails(direction: Direction) {
        val dx = line.run { x2 - x1 }
        val dy = line.run { y2 - y1 }
        val len = sqrt(dx * dx + dy * dy)
        val sin = dy / len
        val cos = dx / len

        // Find actual length of the tails, reduce it if the line is too small
        val adaptedArrowLengthX = if (Configuration.Link.tailLengthX > len / Configuration.Link.maxTailLengthProportionX)
            len / Configuration.Link.maxTailLengthProportionX else Configuration.Link.tailLengthX
        val adaptedArrowLengthY = if (Configuration.Link.tailLengthY > len / Configuration.Link.maxTailLengthProportionY)
            len / Configuration.Link.maxTailLengthProportionY else Configuration.Link.tailLengthY

        val arrowLengthX: Double
        val arrowLengthY: Double
        val arrowHeadX: Double
        val arrowHeadY: Double

        // Define the coordinates of the tails
        when (direction) {
            Direction.FIRST -> {
                arrowLengthX = adaptedArrowLengthX
                arrowLengthY = adaptedArrowLengthY
                arrowHeadX = line.x1
                arrowHeadY = line.y1
            }
            Direction.SECOND -> {
                arrowLengthX = -adaptedArrowLengthX
                arrowLengthY = adaptedArrowLengthY
                arrowHeadX = line.x2
                arrowHeadY = line.y2
            }
        }

        with(tails.first) {
            x1 = calculateRelativeX(arrowLengthX, arrowLengthY, sin, cos, arrowHeadX)
            y1 = calculateRelativeY(arrowLengthX, arrowLengthY, sin, cos, arrowHeadY)
            x2 = arrowHeadX
            y2 = arrowHeadY
        }

        with(tails.second) {
            x1 = calculateRelativeX(arrowLengthX, -arrowLengthY, sin, cos, arrowHeadX)
            y1 = calculateRelativeY(arrowLengthX, -arrowLengthY, sin, cos, arrowHeadY)
            x2 = arrowHeadX
            y2 = arrowHeadY
        }
    }


    /**
     * Calculate relative x coordinate by the position of [x0]
     */
    private fun calculateRelativeX(x: Double, y: Double, sin: Double, cos: Double, x0: Double) =
            x * cos - y * sin + x0

    /**
     * Calculate relative y coordinate by the position of [y0]
     */
    private fun calculateRelativeY(x: Double, y: Double, sin: Double, cos: Double, y0: Double) =
            x * sin + y * cos + y0


    private fun components(withTails: Boolean) = if (withTails) listOf(line) + tails.toList() else listOf(line)
}
