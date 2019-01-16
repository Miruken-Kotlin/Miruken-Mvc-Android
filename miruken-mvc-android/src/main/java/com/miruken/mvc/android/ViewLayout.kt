package com.miruken.mvc.android

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.miruken.mvc.view.Viewing
import com.miruken.mvc.view.ViewingRegion

class ViewLayout(
        @LayoutRes val layoutId: Int,
        override   var viewModel: Any? = null,
        private    val initView: (View.() -> Unit)? = null
) : Viewing, ViewProvider {
    override fun display(region: ViewingRegion) = region.show(this)

    override fun createView(context: Context, parent: ViewGroup): View =
        View.inflate(context, layoutId, null).apply {
            initView?.invoke(this)
    }
}