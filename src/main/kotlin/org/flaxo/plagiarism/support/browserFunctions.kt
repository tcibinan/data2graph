package org.flaxo.plagiarism.support

import kotlin.browser.window

/**
 * Returns current browser page url GET parameters key value map.
 */
fun retrieveUrlParams(): Map<String, String> = window.location.search
        .removePrefix("?")
        .split("&")
        .filter { it.isNotBlank() }
        .map { it.split("=") }
        .filter { it.size == 2 }
        .map { it[0] to it[1] }
        .toMap()
