package org.flaxo.plagiarism.normalization

/**
 * Default normalization strategy.
 *
 * It normalizes every weight by the maximum possible weight - 100.
 */
class DefaultNormalization : DistancesNormalization {
    override fun normalize(weights: List<Double>): List<Double> = weights.map { it / 100.0 }
}
