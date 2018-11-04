package org.flaxo.plagiarism

import io.data2viz.color.Color
import io.data2viz.color.colors
import io.data2viz.force.ForceLink
import io.data2viz.force.ForceNode
import io.data2viz.force.ForceSimulation
import io.data2viz.force.Link
import io.data2viz.force.SimulationEvent
import io.data2viz.force.forceCenter
import io.data2viz.force.forceLink
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
import org.flaxo.plagiarism.model.Graph
import org.flaxo.plagiarism.model.GraphLink
import org.flaxo.plagiarism.model.GraphNode
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSpanElement
import kotlin.browser.document

val blankColor = Color(alpha = 0.0F)
val nodesColor = Color().withRed(31).withGreen(119).withBlue(180)
val linksColor = colors.lightgray.withAlpha(0.5F)

fun Graph.toViz(canvasWidth: Double = 800.0, canvasHeight: Double = 500.0): Viz {
    val forces = listOf(
            nodeLinksForce(links, nodes),
            forceNBody(),
            forceCenter(Point(canvasWidth / 2, canvasHeight / 2))
    )

    return viz {

        width = canvasWidth
        height = canvasHeight

        fun updateGraph() {
            val thresholdInput = document.getElementById("plagiarismMatchThreshold") as HTMLInputElement
            val threshold = thresholdInput.value.toInt()
            val thresholdMonitor = document.getElementById("plagiarismMatchThresholdMonitor") as HTMLSpanElement
            thresholdMonitor.innerHTML = threshold.toString()

            activeLayer.children
                    .asSequence()
                    .filter { it is Line }
                    .map { it as Line }
                    .forEachIndexed { index, line ->
                        val linkJson = links[index]
                        val value = linkJson.weight
                        with(line.style) {
                            stroke = if (value > threshold) colors.lightgray else blankColor
                        }
                    }

        }

        fun refresh(simulation: ForceSimulation) {
            val texts = activeLayer.children.asSequence()
                    .filter { it is Text }
                    .map { it as Text }
                    .toList()
            val circles = activeLayer.children.asSequence()
                    .filter { it is Circle }
                    .map { it as Circle }
                    .toList()
            circles.zip(texts)
                    .forEachIndexed { index, (circle, text) ->
                        val forceNode = simulation.nodes[index]
                        circle.x = forceNode.x
                        circle.y = forceNode.y
                        text.x = forceNode.x + 8
                        text.y = forceNode.y
                    }

            val lines = activeLayer.children.asSequence()
                    .filter { it is Line }
                    .map { it as Line }
            lines.forEachIndexed { index, line ->
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

            val scaleInput = document.getElementById("plagiarismGraphScale") as HTMLInputElement
            val scale = scaleInput.value.toInt()
            val shiftInput = document.getElementById("nodeDistancesShift") as HTMLInputElement
            val shift = shiftInput.value.toInt()
            val distanceNormalizationInput = document.querySelector("input[name=\"distanceNormalizationInput\"]:checked")
                    as HTMLInputElement
            val distanceNormalization = distanceNormalizationInput.value
            simulation.removeForce("force 0")
            simulation.addForce("force 0", nodeLinksForce(links, nodes, scale, shift, distanceNormalization))
        }

        val simulation = forceSimulation {
            // simulation lasts forever
            alphaDecay = 0.0
            nodes = this@toViz.nodes.mapIndexed { index, _ ->
                ForceNode(index, random() * width, random() * height)
            }
            on(SimulationEvent.TICK, "tickEvent") { refresh(it) }
            on(SimulationEvent.END, "endEvent") { println("SIMULATION ENDS") }
        }
        links.forEach {
            line {
                with(style) {
                    stroke = linksColor
                    strokeWidth = 3.0
                }
            }
        }

        simulation.nodes.forEach { node ->
            circle {
                x = node.x
                y = node.y
                radius = 5.0
                with(style) {
                    fill = nodesColor
                    stroke = colors.white
                    strokeWidth = 2.0
                }
            }
            text {
                x = node.x + 8
                y = node.y
                textContent = nodes[node.index].name
                with(style) {
                    stroke = colors.black
                    fill = colors.black
                    anchor = TextAnchor.START
                    baseline = TextAlignmentBaseline.MIDDLE
                }
            }
        }

        forces.forEachIndexed { index, force ->
            simulation.addForce("force $index", force)
        }

        onFrame {
            refresh(simulation)
            updateGraph()
        }
    }
}

fun nodeLinksForce(nodeLinks: List<GraphLink>, studentNodes: List<GraphNode>, scale: Int = 300, shift: Int = 0,
                   distanceNormalization: String = "none"): ForceLink {
    val normalization: (List<Int>) -> Double = {
        when (distanceNormalization) {
            "max" -> it.max()?.toDouble() ?: Double.NaN
            "mean" -> it.average()
            else -> 100.0
        }
    }

    return forceLink {
        linksAccessor = { nodes ->
            nodeLinks.map { link ->
                val source = link.first
                val target = link.second
                val sourceNodeIndex = studentNodes.indexOfFirst { it.name == source }
                val targetNodeIndex = studentNodes.indexOfFirst { it.name == target }
                val sourceNode = nodes[sourceNodeIndex]
                val targetNode = nodes[targetNodeIndex]
                Link(sourceNode, targetNode, _index = (sourceNode.index + targetNode.index * 37))
            }
        }
        distancesAccessor = { links ->
            val values = links.mapIndexed { index, _ -> nodeLinks[index].weight }
            val norm = normalization(values)
            values.asSequence()
                    .map { it.toDouble() }
                    .map { ((1 - it / norm) * scale) + shift }
                    .toList()
        }
    }
}
