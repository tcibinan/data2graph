package org.flaxo.plagiarism.model

/**
 * Direction of link
 */
enum class Direction {
    FIRST, SECOND;

    override fun toString() = name.toLowerCase()
}