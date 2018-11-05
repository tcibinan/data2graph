package org.flaxo.plagiarism

import io.data2viz.force.ForceNode
import io.data2viz.force.ForceSimulation
import io.data2viz.force.SimulationEvent
import io.data2viz.force.forceCenter
import io.data2viz.force.forceNBody
import io.data2viz.force.forceSimulation
import io.data2viz.geom.Point
import io.data2viz.math.random
import io.data2viz.viz.Circle
import io.data2viz.viz.Line
import io.data2viz.viz.Text
import io.data2viz.viz.TextAlignmentBaseline
import io.data2viz.viz.TextAnchor
import io.data2viz.viz.Viz
import io.data2viz.viz.viz
import org.flaxo.plagiarism.force.GraphForce
import org.flaxo.plagiarism.model.Graph
import org.flaxo.plagiarism.model.GraphLink
import org.flaxo.plagiarism.model.GraphNode
import org.flaxo.plagiarism.normalization.CollapsingNormalization
import org.flaxo.plagiarism.normalization.DefaultNormalization
import org.flaxo.plagiarism.normalization.DistancesNormalization
import org.flaxo.plagiarism.normalization.MaximumNormalization
import org.flaxo.plagiarism.support.ColorScheme
import org.flaxo.plagiarism.support.all
import org.flaxo.plagiarism.support.inputById
import org.flaxo.plagiarism.support.inputBySelector
import org.flaxo.plagiarism.support.spanById

/**
 * Converts the graph to a data2viz's visualization.
 */
fun Graph.toViz(canvasWidth: Double = 800.0, canvasHeight: Double = 500.0): Viz = viz {

    width = canvasWidth
    height = canvasHeight

    // Creating a line for each link.
    repeat(links.size) {
        line {
            with(style) {
                stroke = ColorScheme.link
                strokeWidth = 3.0
            }
        }
    }

    // Creating a circle with associated text for each node.
    nodes.forEach { node ->
        circle {
            radius = 5.0
            with(style) {
                fill = ColorScheme.node
                stroke = ColorScheme.nodeStroke
                strokeWidth = 2.0
            }
        }
        text {
            textContent = node.name
            with(style) {
                fill = ColorScheme.text
                stroke = ColorScheme.text
                anchor = TextAnchor.START
                baseline = TextAlignmentBaseline.MIDDLE
            }
        }
    }

    // Creating force simulation that lasts forever.
    val simulation = forceSimulation {
        alphaDecay = 0.0
        nodes = this@toViz.nodes.mapIndexed { index, _ -> ForceNode(index, random() * width, random() * height) }
        on(SimulationEvent.TICK, "tickEvent") { refreshGraph(this@toViz.nodes, links, it) }
    }

    // Basic forces
    simulation.addForce("forceNBody", forceNBody())
    simulation.addForce("forceCenter", forceCenter(Point(canvasWidth / 2, canvasHeight / 2)))

    onFrame { refreshGraph(nodes, links, simulation) }
}

/**
 * Converts the string to a normalization strategy.
 *
 * @param threshold Plagiarism weight threshold.
 */
private fun String.toNormalization(threshold: Double): DistancesNormalization =
        when (this) {
            "max" -> MaximumNormalization()
            "collapsing" -> CollapsingNormalization(threshold)
            else -> DefaultNormalization()
        }

/**
 * Refreshes the visualization according to the simulation changes and the user's input.
 */
fun Viz.refreshGraph(nodes: List<GraphNode>, links: List<GraphLink>, simulation: ForceSimulation) {
    val threshold = inputById("plagiarismMatchThreshold").value.toInt()
    spanById("plagiarismMatchThresholdMonitor").innerHTML = threshold.toString()

    // Changing links color according to the specified plagiarism weight threshold.
    all<Line>().forEachIndexed { index, line ->
        with(line.style) {
            stroke = if (links[index].weight > threshold) ColorScheme.link else ColorScheme.blank
        }
    }

    // Moving all circles and texts according to the simulation state.
    all<Circle>().zip(all<Text>())
            .forEachIndexed { index, (circle, text) ->
                val forceNode = simulation.nodes[index]
                circle.x = forceNode.x
                circle.y = forceNode.y
                text.x = circle.x + 8
                text.y = circle.y
            }

    // Changing link positions according to the simulation state.
    all<Line>().forEachIndexed { index, line ->
        val link = links[index]
        val source = link.first
        val target = link.second
        val sourceNodeIndex = nodes.indexOfFirst { it.name == source }
        val targetNodeIndex = nodes.indexOfFirst { it.name == target }
        val sourceNode = simulation.nodes[sourceNodeIndex]
        val targetNode = simulation.nodes[targetNodeIndex]
        line.x1 = sourceNode.x
        line.y1 = sourceNode.y
        line.x2 = targetNode.x
        line.y2 = targetNode.y
    }

    // Retrieving set scale, shift and normalization.
    val scale = inputById("plagiarismGraphScale").value.toDouble()
    val shift = inputById("nodeDistancesShift").value.toDouble()
    val normalization = inputBySelector("input[name=\"distanceNormalizationInput\"]:checked").value
            .toNormalization(threshold.toDouble())

    // Replacing graph force with the new one that is created according to the user's input.
    val graphForce = GraphForce(links, nodes, normalization, scale, shift)
    simulation.removeForce("Graph force")
    simulation.addForce("Graph force", graphForce)
}
