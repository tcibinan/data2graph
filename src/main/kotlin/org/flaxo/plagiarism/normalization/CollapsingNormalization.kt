package org.flaxo.plagiarism.normalization

/**
 * Collapsing normalization.
 *
 * It is a binary normalization strategy. For all weights that are higher than the given [threshold] it returns 1
 * otherwise 0.
 */
class CollapsingNormalization(private val threshold: Double) : DistancesNormalization {
    override fun normalize(weights: List<Double>): List<Double> =
            weights.map { if (it > threshold) 1.0 else 0.0 }
}
