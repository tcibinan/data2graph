package org.flaxo.plagiarism.force

import io.data2viz.force.Force
import io.data2viz.force.ForceNode
import io.data2viz.force.Link
import io.data2viz.force.forceLink
import org.flaxo.plagiarism.model.GraphLink
import org.flaxo.plagiarism.model.GraphNode
import org.flaxo.plagiarism.normalization.DistancesNormalization

/**
 * Graph force.
 */
class GraphForce(graphLinks: List<GraphLink>,
                 graphNodes: List<GraphNode>,
                 normalization: DistancesNormalization,
                 distancesScale: Double,
                 distancesShift: Double
) : Force {

    private val innerForce = forceLink {
        linksAccessor = { nodes ->
            graphLinks.map { link ->
                val source = link.first
                val target = link.second
                val sourceNodeIndex = graphNodes.indexOfFirst { it.name == source }
                val targetNodeIndex = graphNodes.indexOfFirst { it.name == target }
                val sourceNode = nodes[sourceNodeIndex]
                val targetNode = nodes[targetNodeIndex]
                Link(sourceNode, targetNode, _index = (sourceNode.index + targetNode.index * 37))
            }
        }
        distancesAccessor = { links ->
            links.asSequence()
                    .mapIndexed { index, _ -> graphLinks[index].weight }
                    .map { it.toDouble() }
                    .toList()
                    .let { normalization.normalize(it) }
                    .map { normalizedDistance -> (1 - normalizedDistance) * distancesScale + distancesShift }
        }
    }

    override fun applyForceToNodes(alpha: Double) {
        innerForce.applyForceToNodes(alpha)
    }

    override fun assignNodes(nodes: List<ForceNode>) {
        innerForce.assignNodes(nodes)
    }
}
