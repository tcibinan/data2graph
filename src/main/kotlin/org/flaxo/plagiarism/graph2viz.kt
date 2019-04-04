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
import org.flaxo.plagiarism.normalization.MaximumNormalization
import org.flaxo.plagiarism.support.Click
import org.flaxo.plagiarism.support.ColorScheme
import org.flaxo.plagiarism.support.Configuration
import org.flaxo.plagiarism.support.Mouse
import org.flaxo.plagiarism.support.all
import org.flaxo.plagiarism.support.distanceTo
import org.flaxo.plagiarism.support.inCoordinatesOf
import org.flaxo.plagiarism.support.inputById
import org.flaxo.plagiarism.support.inputBySelector
import org.flaxo.plagiarism.support.spanById
import org.w3c.dom.Element
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import kotlin.browser.document
import kotlin.browser.window
import kotlin.math.sqrt
import kotlin.random.Random

private const val defaultArrowLengthX = 30.0
private const val defaultArrowLengthY = 8.0
private const val maxArrowLengthProportionX = 2
private const val maxArrowLengthProportionY = 3

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
 * Returns mouse pointer coordinates according to the receiver mouse event.
 *
 * Returning point has coordinates relative to the event target element.
 */
private fun Event.getPoint(): Point? {
    val event = this as? MouseEvent
    val target = event?.target as? Element
    val clientRect = target?.getBoundingClientRect()
    if (event != null && clientRect != null) {
        val x = event.clientX - clientRect.left
        val y = event.clientY - clientRect.top
        return Point(x, y)
    }
    return null
}

/**
 * Refreshes the visualization according to the simulation changes and the user's input.
 */
fun Viz.refreshGraph(nodes: List<GraphNode>, links: List<GraphLink>, simulation: ForceSimulation, mouse: Mouse) {
    val threshold = inputById("plagiarismMatchThreshold").value.toInt()
    spanById("plagiarismMatchThresholdMonitor").innerHTML = threshold.toString()
    val directionEnabled = inputBySelector("input[name=\"graphDirectionEnabledInput\"]").checked

    val allLines = all<Line>()
    val linkLines = allLines.subList(0, allLines.size / 3)
    val arrowLines = allLines.subList(linkLines.size, allLines.size).chunked(2).map { it -> it[0] to it[1] }

    // Changing links color according to the specified plagiarism weight threshold.
    linkLines.forEachIndexed { index, line ->
        with(line.style) {
            stroke = if (links[index].weight > threshold) ColorScheme.Link.default else ColorScheme.blank
            strokeWidth = Configuration.Link.strokeWidth
        }
    }

    // Changing arrows color according to the specified plagiarism weight threshold.
    arrowLines.forEachIndexed { index, arrowLinesPair ->
        arrowLinesPair.first.also { line ->
            with(line.style) {
                stroke = if (links[index].weight > threshold && directionEnabled)
                    ColorScheme.Link.default
                else ColorScheme.blank
            }
        }
        arrowLinesPair.second.also { line ->
            with(line.style) {
                stroke = if (links[index].weight > threshold && directionEnabled)
                    ColorScheme.Link.default
                else ColorScheme.blank
            }
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

    // Updating link coordinates according to the simulation state.
    linkLines.zip(arrowLines).forEachIndexed { index, (line, arrowLines) ->
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
        if (directionEnabled && link.directedTo != null) setArrowLinesPosition(line, arrowLines, link.directedTo)
    }

    // Resetting the cursor
    document.body?.style?.cursor = "auto"

    val closestLineLink = all<Line>()
            .mapIndexed { index, line -> line to links[index] }
            .filter { (line, link) ->
                link.weight > threshold
                        && line distanceTo mouse.point < Configuration.Mouse.triggerDistance
                        && mouse.point inCoordinatesOf line
            }
            .minBy { (line, _) -> line distanceTo mouse.point }
    if (closestLineLink != null) {
        val (line, link) = closestLineLink
        with(line.style) {
            stroke = ColorScheme.Link.selected
            strokeWidth = Configuration.Link.selectedStrokeWidth
        }
        val clickPoint = mouse.click?.point
        if (clickPoint != null
                && line distanceTo clickPoint < Configuration.Mouse.triggerDistance
                && clickPoint inCoordinatesOf line) {
            if (link.url != null) {
                window.location.href = link.url
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

/**
 * Define coordinates of the [arrowLines]
 */
fun setArrowLinesPosition(line: Line, arrowLines: Pair<Line, Line>, direction: Direction) {
    val dx = line.run { x2 - x1 }
    val dy = line.run { y2 - y1 }
    val len = sqrt(dx * dx + dy * dy)
    val sin = dy / len
    val cos = dx / len

    // Find actual length of the arrow lines, reduce it if the line is too small
    val adaptedArrowLengthX = if (defaultArrowLengthX > len / maxArrowLengthProportionX)
        len / maxArrowLengthProportionX else defaultArrowLengthX
    val adaptedArrowLengthY = if (defaultArrowLengthY > len / maxArrowLengthProportionY)
        len / maxArrowLengthProportionY else defaultArrowLengthY

    val arrowLengthX: Double
    val arrowLengthY: Double
    val arrowHeadX: Double
    val arrowHeadY: Double

    // Define the coordinates of the arrow lines
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

    arrowLines.first.x1 = calculateRelativeX(arrowLengthX, arrowLengthY, sin, cos, arrowHeadX)
    arrowLines.first.y1 = calculateRelativeY(arrowLengthX, arrowLengthY, sin, cos, arrowHeadY)
    arrowLines.first.x2 = arrowHeadX
    arrowLines.first.y2 = arrowHeadY
    arrowLines.second.x1 = calculateRelativeX(arrowLengthX, -arrowLengthY, sin, cos, arrowHeadX)
    arrowLines.second.y1 = calculateRelativeY(arrowLengthX, -arrowLengthY, sin, cos, arrowHeadY)
    arrowLines.second.x2 = arrowHeadX
    arrowLines.second.y2 = arrowHeadY
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
