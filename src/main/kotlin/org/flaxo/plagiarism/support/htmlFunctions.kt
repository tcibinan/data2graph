package org.flaxo.plagiarism.support

import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLSpanElement
import kotlin.browser.document

fun inputById(id: String): HTMLInputElement = elementById(id)

fun spanById(id: String): HTMLSpanElement = elementById(id)

/**
 * Returns html element of type [T] from the [document] by the given [id].
 *
 * Fails if there is no such an element or the element has a wrong type.
 */
inline fun <reified T: HTMLElement> elementById(id: String): T = document.getElementById(id) as T

fun inputBySelector(selector: String): HTMLInputElement = elementBySelector(selector)

/**
 * Returns html element of type [T] from the [document] by the given css [selector].
 *
 * Fails if there is no such an element or the element has a wrong type.
 */
inline fun <reified T: HTMLElement> elementBySelector(selector: String): T = document.querySelector(selector) as T
