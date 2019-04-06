package org.flaxo.plagiarism.element

import io.data2viz.force.ForceNode
import io.data2viz.viz.CircleNode
import io.data2viz.viz.FontWeight
import io.data2viz.viz.TextNode
import org.flaxo.plagiarism.model.GraphNode
import org.flaxo.plagiarism.support.ColorScheme
import org.flaxo.plagiarism.support.Configuration

/**
 * Dot visual element.
 *
 * It consists of [circle] and [text] and has an associated graph [node].
 */
class Dot(val circle: CircleNode, val text: TextNode, val node: GraphNode)
    : VisualElement<ForceNode> {

    override fun show(directed: Boolean) {
        circle.fill = ColorScheme.Node.default
        text.fontWeight = FontWeight.NORMAL
    }

    override fun hide(directed: Boolean) {
        circle.fill = ColorScheme.blank
        text.textColor = ColorScheme.blank
    }

    override fun select(directed: Boolean) {
        circle.fill = ColorScheme.Node.selected
        text.fontWeight = FontWeight.BOLD
    }

    /**
     * Updates coordinates of all required components of the dot.
     */
    override fun update(coordinatesSource: ForceNode, directed: Boolean) {
        circle.x = coordinatesSource.x
        circle.y = coordinatesSource.y
        text.x = circle.x
        text.y = circle.y - Configuration.Text.margin
    }
}
