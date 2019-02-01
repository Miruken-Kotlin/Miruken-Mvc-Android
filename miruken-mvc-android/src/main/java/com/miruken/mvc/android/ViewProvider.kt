package com.miruken.mvc.android

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.miruken.mvc.Navigation

interface ViewProvider {
    fun createView(
            context:    Context,
            parent:     ViewGroup,
            navigation: Navigation<*>?
    ): View
}