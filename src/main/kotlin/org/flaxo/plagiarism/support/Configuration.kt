package org.flaxo.plagiarism.support

/**
 * Graph default parameters configuration.
 */
object Configuration {

    object Canvas {
        const val height = 500
        const val width = 800
    }

    object Node {
        const val radius = 5.0
        const val strokeWidth = 2.0

    }

    object Link {
        const val strokeWidth = 3.0
        const val tailLengthX = 20.0
        const val tailLengthY = 5.0
        const val maxTailLengthProportionX = 2
        const val maxTailLengthProportionY = 3
    }

    object Mouse {
        const val triggerDistance = 10
    }
}
