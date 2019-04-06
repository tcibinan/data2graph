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
        const val radius = 8.0
        const val strokeWidth = 2.0

    }

    object Link {
        const val strokeWidth = 5.0
        const val tailLengthX = 23.0
        const val tailLengthY = 7.0
        const val maxTailLengthProportionX = 2
        const val maxTailLengthProportionY = 3
    }

    object Mouse {
        const val triggerDistance = 15
    }

    object Text {
        const val size = 16.0
        const val margin = 10
    }
}
