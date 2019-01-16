package com.miruken.mvc.android

import android.content.Context
import android.view.View
import android.view.ViewGroup

interface ViewProvider {
    fun createView(context: Context, parent: ViewGroup): View
}