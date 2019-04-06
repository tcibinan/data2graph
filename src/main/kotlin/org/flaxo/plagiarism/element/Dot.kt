package org.flaxo.plagiarism.element

import io.data2viz.force.ForceNode
import io.data2viz.viz.CircleNode
import io.data2viz.viz.FontWeight
import io.data2viz.viz.TextNode
import org.flaxo.plagiarism.model.GraphNode
import org.flaxo.plagiarism.support.ColorScheme
import org.flaxo.plagiarism.support.Configuration

class Dot(val circle: CircleNode, val text: TextNode, val node: GraphNode) {

    /**
     * Shows all components of the dot.
     */
    fun show() {
        circle.fill = ColorScheme.Node.default
        text.fontWeight = FontWeight.NORMAL
    }

    /**
     * Hides all components of the dot.
     */
    fun hide() {
        circle.fill = ColorScheme.blank
        text.textColor = ColorScheme.blank
    }

    /**
     * Highlights all components of the dot.
     */
    fun select() {
        circle.fill = ColorScheme.Node.selected
        text.fontWeight = FontWeight.BOLD
    }

    /**
     * Updates coordinates of all required components of the dot.
     */
    fun update(forceNode: ForceNode) {
        circle.x = forceNode.x
        circle.y = forceNode.y
        text.x = circle.x
        text.y = circle.y - Configuration.Text.margin
    }
}
