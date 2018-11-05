package org.flaxo.plagiarism.normalization

/**
 * Maximum normalization strategy.
 *
 * It normalizes every weight by the maximum of all the given weights.
 */
class MaximumNormalization : DistancesNormalization {
    override fun normalize(weights: List<Double>): List<Double> {
        val max = weights.max() ?: 100.0
        return weights.map { it / max }
    }
}