package com.miruken.mvc.android

import android.view.View
import androidx.annotation.LayoutRes
import com.miruken.mvc.view.Viewing
import com.miruken.mvc.view.ViewingRegion

class ViewLayout(
        @LayoutRes val layoutId: Int,
        override   var viewModel: Any? = null,
        private    val initView: (View.() -> Unit)? = null
) : Viewing {
    override fun display(region: ViewingRegion) = region.show(this)

    fun init(view: View) = initView?.invoke(view)
}