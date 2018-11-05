package org.flaxo.plagiarism.support

import io.data2viz.viz.Node
import io.data2viz.viz.Viz

/**
 * Returns all elements of type [T] that exist in the current visualization.
 */
inline fun <reified T: Node> Viz.all(): List<T> =
        activeLayer.children.asSequence()
                .filter { it is T }
                .map { it as T }
                .toList()
