package org.flaxo.plagiarism.model

import kotlinx.serialization.Serializable

/**
 * Graph - a set of nodes and links that connects some of the given nodes.
 */
@Serializable
class Graph(

        /**
         * Graph nodes.
         */
        val nodes: List<GraphNode>,

        /**
         * Graph links.
         */
        val links: List<GraphLink>
)
