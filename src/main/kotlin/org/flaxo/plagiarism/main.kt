package org.flaxo.plagiarism

import io.data2viz.viz.bindRendererOn
import kotlinx.serialization.json.JSON
import org.flaxo.plagiarism.model.Graph
import org.flaxo.plagiarism.support.elementById
import org.w3c.dom.HTMLCanvasElement
import org.w3c.fetch.Request
import kotlin.browser.window

fun main(args: Array<String>) {
    window.fetch(Request("data.json")).then { response ->
        response.text().then {
            val graph: Graph = JSON.parse(Graph.serializer(), it)
            elementById<HTMLCanvasElement>("main-canvas").apply {
                graph.toViz(scrollWidth, scrollHeight).bindRendererOn(this)
            }
        }
    }
}
