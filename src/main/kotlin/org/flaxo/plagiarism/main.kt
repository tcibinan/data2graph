package org.flaxo.plagiarism

import io.data2viz.viz.bindRendererOn
import kotlinx.serialization.json.JSON
import org.flaxo.plagiarism.model.Graph
import org.w3c.fetch.Request
import kotlin.browser.window

fun main(args: Array<String>) {
    window.fetch(Request("data.json")).then { response ->
        response.text().then {
            val graph: Graph = JSON.parse(Graph.serializer(), it)
            graph.toViz().bindRendererOn("main-canvas")
        }
    }
}
