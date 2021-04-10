package org.flaxo.plagiarism

import io.data2viz.viz.bindRendererOn
import kotlinx.html.InputType
import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.dom.create
import kotlinx.html.h1
import kotlinx.html.id
import kotlinx.html.img
import kotlinx.html.input
import kotlinx.html.js.canvas
import kotlinx.html.label
import kotlinx.html.small
import kotlinx.html.span
import kotlinx.html.style
import kotlinx.serialization.json.JSON
import org.flaxo.plagiarism.model.Graph
import org.flaxo.plagiarism.support.decodeURIComponent
import org.flaxo.plagiarism.support.elementById
import org.flaxo.plagiarism.support.retrieveUrlParams
import org.w3c.dom.HTMLCanvasElement
import org.w3c.fetch.Request
import kotlin.browser.document
import kotlin.browser.window

private const val GRAPH_URL_PARAMETER = "graph_url"

fun main() {
    val parameters: Map<String, String> = retrieveUrlParams()
    val graphUrl = parameters[GRAPH_URL_PARAMETER]
    if (graphUrl != null) {
        val decodedGraphUrl = decodeURIComponent(graphUrl)
        window.fetch(Request(decodedGraphUrl)).then { response ->
            if (response.status < 400) {
                response.text().then {
                    try {
                        val graph: Graph = JSON.parse(Graph.serializer(), it)
                        showGraphVisualizationTool()
                        elementById<HTMLCanvasElement>("main-canvas").apply {
                            graph.toViz(scrollWidth, scrollHeight).bindRendererOn(this)
                        }
                    } catch (error: Throwable) {
                        showBadlyFormattedGraphJsonPlaceholder(error)
                    }
                }
            } else {
                showBadResponseGraphUrlPlaceholder(decodedGraphUrl, response.status)
            }
        }.catch { error ->
            showUnreachableGraphUrlPlaceholder(decodedGraphUrl, error)
        }
    } else {
        showEmptyGraphUrlPlaceholder()
    }
}

private fun showGraphVisualizationTool() {
    val controls = document.create.div(classes = "controls") {
        style = "text-transform: uppercase; " +
                "font-family: monospace; "
        div(classes = "input-group") {
            div {
                +"Threshold "
                span {
                    id = "plagiarismMatchThresholdMonitor"
                    +"75"
                }
                span { +"%" }
            }
            label {
                input(classes = "threshold-range") {
                    id = "plagiarismMatchThreshold"
                    type = InputType.range
                    min = "0"
                    max = "100"
                    value = "75"
                }
            }
        }
        div(classes = "input-group") {
            div {
                +"Shift "
                span {
                    id = "nodeDistancesShiftMonitor"
                    +"0"
                }
                span { +"%" }
            }
            label {
                input(classes = "threshold-range") {
                    id = "nodeDistancesShift"
                    type = InputType.range
                    min = "0"
                    max = "400"
                    value = "200"
                }
            }
        }
        div(classes = "input-group") {
            div {
                +"Scale "
                span {
                    id = "plagiarismGraphScaleMonitor"
                    +"300"
                }
                span { +"%" }
            }
            label {
                input(classes = "threshold-range") {
                    id = "plagiarismGraphScale"
                    type = InputType.range
                    min = "0"
                    max = "500"
                    value = "200"
                }
            }
        }
        div(classes = "input-group") {
            div {
                +"Normalization"
            }
            label {
                input(classes = "threshold-range") {
                    name = "distanceNormalizationInput"
                    type = InputType.radio
                    value = "none"
                    checked = true
                }
                +"disabled"
            }
            label {
                input(classes = "threshold-range") {
                    name = "distanceNormalizationInput"
                    type = InputType.radio
                    value = "max"
                }
                +"max"
            }
            label {
                input(classes = "threshold-range") {
                    name = "distanceNormalizationInput"
                    type = InputType.radio
                    value = "collapsing"
                }
                +"collapsing"
            }
        }
        div(classes = "input-group") {
            div {
                +"Direction"
            }
            label {
                input(classes = "threshold-range") {
                    id = "graphDirectionEnabledInput"
                    type = InputType.checkBox
                    checked = false
                }
                +"enabled"
            }
        }
        div(classes = "input-group") {
            div {
                +"Show active nodes"
            }
            label {
                input(classes = "threshold-range") {
                    id = "showActiveNodesEnabledInput"
                    type = InputType.checkBox
                    checked = true
                }
                +"enabled"
            }
        }
        div(classes = "links") {
            a(href = "https://github.com/tcibinan/flaxo") {
                img(src = "https://img.shields.io/badge/from_flaxo-with_♥-blue.svg", alt = "rom_flaxo with_♥")
            }
            a(href = "https://github.com/tcibinan/data2graph", classes = "github-button") {
                attributes.put("data-icon", "octicon-star")
                attributes.put("aria-label", "Star tcibinan/graph2viz on GitHub")
                +"Star"
            }
            a(href = "https://github.com/tcibinan/data2graph/issues", classes = "github-button") {
                attributes.put("data-icon", "octicon-star")
                attributes.put("aria-label", "Issue tcibinan/graph2viz on GitHub")
                +"Issue"
            }
        }
    }
    val canvas = document.create.canvas(classes = "graph") {
        id = "main-canvas"
    }
    document.body?.apply {
        appendChild(controls)
        appendChild(canvas)
    }
}

private fun showEmptyGraphUrlPlaceholder() {
    val demoUrl = window.location.href + "?$GRAPH_URL_PARAMETER=data.json"
    val placeholder = document.create.div(classes = "placeholder") {
        h1 {
            +"Use $GRAPH_URL_PARAMETER parameter to specify graph source"
        }
        small {
            +"Like here "
            a(href = demoUrl) {
                +demoUrl
            }
        }
    }
    document.body?.appendChild(placeholder)
}

private fun showUnreachableGraphUrlPlaceholder(graphUrl: String, error: Throwable) {
    val placeholder = document.create.div(classes = "placeholder") {
        h1 {
            +"The requested "
            a(href = graphUrl) {
                +"graph url"
            }
            +" is unreachable"
        }
        small {
            +"Error: $error"
        }
    }
    document.body?.appendChild(placeholder)
}

private fun showBadResponseGraphUrlPlaceholder(graphUrl: String, status: Short) {
    val placeholder = document.create.div(classes = "placeholder") {
        h1 {
            +"The requested "
            a(href = graphUrl) {
                +"graph url"
            }
            +" returns bad HTTP status"
        }
        small {
            +"HTTP status: $status"
        }
    }
    document.body?.appendChild(placeholder)
}

fun showBadlyFormattedGraphJsonPlaceholder(error: Throwable) {
    val placeholder = document.create.div(classes = "placeholder") {
        h1 {
            +"Downloaded graph json parsing has failed"
        }
        small {
            +"Error: $error"
        }
    }
    document.body?.appendChild(placeholder)
}
