package org.flaxo.plagiarism.normalization

/**
 * Distances normalization strategy.
 */
interface DistancesNormalization {

    /**
     * Converts a list of a graph link [weights] to a list of a normalized weights.
     *
     * Normalized weights are in between of 0 and 1.
     */
    fun normalize(weights: List<Double>): List<Double>
}
