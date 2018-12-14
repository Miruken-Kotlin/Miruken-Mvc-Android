package com.miruken.mvc.android

import com.miruken.callback.Handling
import com.miruken.callback.aspect

interface Guarding {
    fun guard(guard: Boolean): Boolean
}

fun Handling.guard(guarded: Guarding): Handling {
    var guard: Boolean? = null
    return aspect({ _, _ ->
        guard = guarded.guard(true)
        guard
    }, { _, _, _ ->
        if (guard == true) {
            guarded.guard(false)
        }
    })
}