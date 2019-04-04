package org.flaxo.plagiarism

import io.data2viz.force.ForceNode
import io.data2viz.force.ForceSimulation
import io.data2viz.force.forceCenter
import io.data2viz.force.forceNBody
import io.data2viz.force.forceSimulation
import io.data2viz.geom.Point
import io.data2viz.viz.Circle
import io.data2viz.viz.Line
import io.data2viz.viz.Text
import io.data2viz.viz.TextAlignmentBaseline
import io.data2viz.viz.TextAnchor
import io.data2viz.viz.Viz
import io.data2viz.viz.viz
import org.flaxo.plagiarism.force.GraphForce
import org.flaxo.plagiarism.model.Direction
import org.flaxo.plagiarism.model.Graph
import org.flaxo.plagiarism.model.GraphLink
import org.flaxo.plagiarism.model.GraphNode
import org.flaxo.plagiarism.normalization.CollapsingNormalization
import org.flaxo.plagiarism.normalization.DefaultNormalization
import org.flaxo.plagiarism.normalization.DistancesNormalization
import org.flaxo.plagiarism.normalization.MaximumNormalization
import org.flaxo.plagiarism.support.ColorScheme
import org.flaxo.plagiarism.support.inputById
import org.flaxo.plagiarism.support.inputBySelector
import org.flaxo.plagiarism.support.spanById
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Converts the graph to a data2viz's visualization.
 */
fun Graph.toViz(canvasWidth: Int = 800, canvasHeight: Int = 500): Viz =
        toViz(canvasWidth.toDouble(), canvasHeight.toDouble())

private const val defaultArrowLengthX = 30.0
private const val defaultArrowLengthY = 8.0

private val lines = mutableListOf<Line>()
private val arrowLines = mutableListOf<List<Line>>()
private val circles = mutableListOf<Circle>()
private val texts = mutableListOf<Text>()

/**
 * Converts the graph to a data2viz's visualization.
 */
fun Graph.toViz(canvasWidth: Double = 800.0, canvasHeight: Double = 500.0): Viz = viz {

    width = canvasWidth
    height = canvasHeight

    // Creating a line for each link.
    repeat(links.size) {
        lines.add(line {
            with(style) {
                stroke = ColorScheme.link
                strokeWidth = 3.0
            }
        })
    }

    // Creating two lines for an arrow per each line.
    repeat(links.size) {
        val arrowLine = mutableListOf<Line>()
        arrowLines += arrowLine
        repeat(2) {
            arrowLine += line {
                with(style) {
                    stroke = ColorScheme.text
                    strokeWidth = 3.0
                }
            }
        }
    }

    // Creating a circle with associated text for each node.
    nodes.forEach { node ->
        circles.add(circle {
            radius = 5.0
            with(style) {
                fill = ColorScheme.node
                stroke = ColorScheme.nodeStroke
                strokeWidth = 2.0
            }
        })
        texts.add(text {
            textContent = node.name
            with(style) {
                fill = ColorScheme.text
                stroke = ColorScheme.text
                anchor = TextAnchor.START
                baseline = TextAlignmentBaseline.MIDDLE
            }
        })
    }

    // Creating force simulation that lasts forever.
    val simulation = forceSimulation {
        alphaDecay = 0.0
        nodes = this@toViz.nodes.mapIndexed { index, _ ->
            ForceNode(index, Random.nextDouble() * width, Random.nextDouble() * height)
        }
    }

    // Adding basic forces.
    simulation.addForce("forceNBody", forceNBody())
    simulation.addForce("forceCenter", forceCenter(Point(width / 2, height / 2)))

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
    val directionEnabled = inputBySelector("input[name=\"graphDirectionEnabledInput\"]").checked

    // Changing links color according to the specified plagiarism weight threshold.
    lines.forEachIndexed { index, line ->
        with(line.style) {
            stroke = if (links[index].weight > threshold) ColorScheme.link else ColorScheme.blank
        }
    }

    // Changing arrows color according to the specified plagiarism weight threshold.
    arrowLines.forEachIndexed { index, arrowLinesChunk ->
        arrowLinesChunk.forEach { line ->
            with(line.style) {
                stroke = if (links[index].weight > threshold && directionEnabled)
                    ColorScheme.link
                else ColorScheme.blank
            }
        }
    }

    // Moving all circles and texts according to the simulation state.
    circles.zip(texts)
            .forEachIndexed { index, (circle, text) ->
                val forceNode = simulation.nodes[index]
                circle.x = forceNode.x
                circle.y = forceNode.y
                text.x = circle.x + 8
                text.y = circle.y
            }

    // Changing link positions according to the simulation state.
    lines.zip(arrowLines).forEachIndexed { index, (line, arrowLines) ->
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

        // Set position of the arrow lines for the line
        if (directionEnabled) setArrowLinesPosition(line, arrowLines, link.directedTo)
    }

    // Retrieving set scale, shift and normalization.
    val scale = inputById("plagiarismGraphScale").value.toDouble()
    spanById("plagiarismGraphScaleMonitor").innerHTML = scale.toString()
    val shift = inputById("nodeDistancesShift").value.toDouble()
    spanById("nodeDistancesShiftMonitor").innerHTML = shift.toString()
    val normalization = inputBySelector("input[name=\"distanceNormalizationInput\"]:checked").value
            .toNormalization(threshold.toDouble())

    // Replacing graph force with the new one that is created according to the user's input.
    val graphForce = GraphForce(links, nodes, normalization, scale, shift)
    simulation.removeForce("Graph force")
    simulation.addForce("Graph force", graphForce)
}

/**
 * Define coordinates of the [arrowLines]
 */
fun setArrowLinesPosition(line: Line, arrowLines: List<Line>, direction: Direction) {
    val dx = line.run { x2 - x1 }
    val dy = line.run { y2 - y1 }
    val len = sqrt(dx * dx + dy * dy)
    val sin = dy / len
    val cos = dx / len

    val arrowLengthX: Double
    val arrowLengthY: Double
    val arrowHeadX: Double
    val arrowHeadY: Double

    when (direction) {
        Direction.FIRST -> {
            arrowLengthX = defaultArrowLengthX
            arrowLengthY = defaultArrowLengthY
            arrowHeadX = line.x1
            arrowHeadY = line.y1
        }
        Direction.SECOND -> {
            arrowLengthX = -defaultArrowLengthX
            arrowLengthY = defaultArrowLengthY
            arrowHeadX = line.x2
            arrowHeadY = line.y2
        }
    }

    arrowLines[0].x1 = calculateRelativeX(arrowLengthX, arrowLengthY, sin, cos, arrowHeadX)
    arrowLines[0].y1 = calculateRelativeY(arrowLengthX, arrowLengthY, sin, cos, arrowHeadY)
    arrowLines[0].x2 = arrowHeadX
    arrowLines[0].y2 = arrowHeadY
    arrowLines[1].x1 = calculateRelativeX(arrowLengthX, -arrowLengthY, sin, cos, arrowHeadX)
    arrowLines[1].y1 = calculateRelativeY(arrowLengthX, -arrowLengthY, sin, cos, arrowHeadY)
    arrowLines[1].x2 = arrowHeadX
    arrowLines[1].y2 = arrowHeadY
}

/**
 * Calculate relative x coordinate by the position of [x0]
 */
fun calculateRelativeX(x: Double, y: Double, sin: Double, cos: Double, x0: Double) =
        x * cos - y * sin + x0

/**
 * Calculate relative y coordinate by the position of [y0]
 */
fun calculateRelativeY(x: Double, y: Double, sin: Double, cos: Double, y0: Double) =
        x * sin + y * cos + y0