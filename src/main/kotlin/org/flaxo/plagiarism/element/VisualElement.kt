package org.flaxo.plagiarism.element

/**
 * Visual element.
 */
interface VisualElement<C> {
    /**
     * Shows all required components of the element.
     */
    fun show(directed: Boolean = false)

    /**
     * Hides all components of the element.
     */
    fun hide(directed: Boolean = false)

    /**
     * Highlights all required components of the element.
     */
    fun select(directed: Boolean = false)

    /**
     * Updates coordinates of all required components of the element.
     */
    fun update(coordinatesSource: C, directed: Boolean = false)
}
