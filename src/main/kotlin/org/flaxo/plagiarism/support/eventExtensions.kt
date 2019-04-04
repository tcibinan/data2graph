package org.flaxo.plagiarism.support

import io.data2viz.geom.Point
import org.w3c.dom.Element
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent


/**
 * Returns mouse pointer coordinates according to the receiver mouse event.
 *
 * Returning point has coordinates relative to the event target element.
 */
fun Event.getPoint(): Point? {
    val event = this as? MouseEvent
    val target = event?.target as? Element
    val clientRect = target?.getBoundingClientRect()
    if (event != null && clientRect != null) {
        val x = event.clientX - clientRect.left
        val y = event.clientY - clientRect.top
        return Point(x, y)
    }
    return null
}
