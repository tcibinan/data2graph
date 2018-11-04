package org.flaxo.plagiarism.model

import kotlinx.serialization.Serializable

/**
 * Graph node.
 *
 * Represents a single node in a graph.
 */
@Serializable
class GraphNode(

        /**
         * Node unique name.
         */
        val name: String
)
