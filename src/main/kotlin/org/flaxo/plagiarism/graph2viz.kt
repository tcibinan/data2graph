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
import org.flaxo.plagiarism.model.Graph
import org.flaxo.plagiarism.model.GraphLink
import org.flaxo.plagiarism.model.GraphNode
import org.flaxo.plagiarism.element.Arrow
import org.flaxo.plagiarism.normalization.CollapsingNormalization
import org.flaxo.plagiarism.normalization.DefaultNormalization
import org.flaxo.plagiarism.normalization.MaximumNormalization
import org.flaxo.plagiarism.support.Click
import org.flaxo.plagiarism.support.ColorScheme
import org.flaxo.plagiarism.support.Configuration
import org.flaxo.plagiarism.support.Mouse
import org.flaxo.plagiarism.support.all
import org.flaxo.plagiarism.support.distanceTo
import org.flaxo.plagiarism.support.getPoint
import org.flaxo.plagiarism.support.inCoordinatesOf
import org.flaxo.plagiarism.support.inputById
import org.flaxo.plagiarism.support.inputBySelector
import org.flaxo.plagiarism.support.spanById
import org.w3c.dom.HTMLCanvasElement
import kotlin.browser.document
import kotlin.browser.window
import kotlin.random.Random

/**
 * Converts the graph to a data2viz's visualization.
 */
fun Graph.toViz(canvasWidth: Int = Configuration.Canvas.width,
                canvasHeight: Int = Configuration.Canvas.height
): Viz = toViz(canvasWidth.toDouble(), canvasHeight.toDouble())

/**
 * Converts the graph to a data2viz's visualization.
 */
fun Graph.toViz(canvasWidth: Double = Configuration.Canvas.width.toDouble(),
                canvasHeight: Double = Configuration.Canvas.height.toDouble()
): Viz = viz {

    width = canvasWidth
    height = canvasHeight

    // Creating a line for each link.
    repeat(links.size) {
        line {
            with(style) {
                stroke = ColorScheme.Link.default
                strokeWidth = Configuration.Link.strokeWidth
            }
        }
    }
    // Creating two lines for each arrow.
    repeat(links.size * 2) {
        line {
            with(style) {
                stroke = ColorScheme.Link.default
                strokeWidth = 3.0
            }
        }
    }

    // Creating a circle with associated text for each node.
    nodes.forEach { node ->
        circle {
            radius = Configuration.Node.radius
            with(style) {
                fill = ColorScheme.Node.default
                stroke = ColorScheme.Node.stroke
                strokeWidth = Configuration.Node.strokeWidth
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

    // Creating a force simulation that lasts forever.
    val simulation = forceSimulation {
        alphaDecay = 0.0
        nodes = this@toViz.nodes.mapIndexed { index, _ ->
            ForceNode(index, Random.nextDouble() * width, Random.nextDouble() * height)
        }
    }

    // Creating basic forces.
    simulation.addForce("forceNBody", forceNBody())
    simulation.addForce("forceCenter", forceCenter(Point(width / 2, height / 2)))

    val mouse = Mouse()
    val canvas = document.getElementById("main-canvas") as? HTMLCanvasElement
    canvas?.onmousemove = { e -> e.getPoint()?.also { mouse.point = it } }
    canvas?.onclick = { e -> e.getPoint()?.also { mouse.click = Click(it) } }
    onFrame {
        refreshGraph(nodes, links, simulation, mouse)
    }
}

/**
 * Refreshes the visualization according to the simulation changes and the user's input.
 */
fun Viz.refreshGraph(nodes: List<GraphNode>, links: List<GraphLink>, simulation: ForceSimulation, mouse: Mouse) {
    val threshold = inputById("plagiarismMatchThreshold").value.toInt()
    spanById("plagiarismMatchThresholdMonitor").innerHTML = threshold.toString()
    val directionEnabled = inputById("graphDirectionEnabledInput").checked

    val arrows = getArrows(links)

    arrows.forEach { arrow ->
        if (arrow.link.weight > threshold) arrow.show(directionEnabled) else arrow.hide()
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

    // Updating link coordinates according to the simulation state.
    arrows.forEach { arrow ->
        val source = arrow.link.first
        val target = arrow.link.second
        val sourceNodeIndex = nodes.indexOfFirst { it.name == source }
        val targetNodeIndex = nodes.indexOfFirst { it.name == target }
        val sourceNode = simulation.nodes[sourceNodeIndex]
        val targetNode = simulation.nodes[targetNodeIndex]

        arrow.update(sourceNode, targetNode, directionEnabled)
    }

    // Resetting the cursor.
    document.body?.style?.cursor = "auto"

    // Finding the closest arrow to mouse.
    val closestArrow = arrows
            .filter { arrow ->
                arrow.link.weight > threshold
                        && arrow.line distanceTo mouse.point < Configuration.Mouse.triggerDistance
                        && mouse.point inCoordinatesOf arrow.line
            }
            .minBy { arrow -> arrow.line distanceTo mouse.point }

    // Highlighting the closest arrow and handling the clicks.
    if (closestArrow != null) {
        closestArrow.select(directionEnabled)
        val clickPoint = mouse.click?.point
        if (clickPoint != null
                && closestArrow.line distanceTo clickPoint < Configuration.Mouse.triggerDistance
                && clickPoint inCoordinatesOf closestArrow.line) {
            if (closestArrow.link.url != null) {
                window.location.href = closestArrow.link.url
            }
        }
        document.body?.style?.cursor = "pointer"
    }

    // Retrieving set scale, shift and normalization.
    val scale = inputById("plagiarismGraphScale").value.toDouble()
    spanById("plagiarismGraphScaleMonitor").innerHTML = scale.toString()
    val shift = inputById("nodeDistancesShift").value.toDouble()
    spanById("nodeDistancesShiftMonitor").innerHTML = shift.toString()
    val normalization = when (inputBySelector("input[name=\"distanceNormalizationInput\"]:checked").value) {
        "max" -> MaximumNormalization()
        "collapsing" -> CollapsingNormalization(threshold.toDouble())
        else -> DefaultNormalization()
    }

    // Replacing graph force with the new one that is created according to the user's input.
    val graphForce = GraphForce(links, nodes, normalization, scale, shift)
    simulation.removeForce("Graph force")
    simulation.addForce("Graph force", graphForce)
}

private fun Viz.getArrows(links: List<GraphLink>): List<Arrow> {
    val allLines = all<Line>()
    val linkLines = allLines.subList(0, allLines.size / 3)
    val arrowLines = allLines.subList(linkLines.size, allLines.size).chunked(2).map { it[0] to it[1] }
    return linkLines.zip(arrowLines).mapIndexed { index, (line, arrows) -> Arrow(line, arrows, links[index]) }
}
