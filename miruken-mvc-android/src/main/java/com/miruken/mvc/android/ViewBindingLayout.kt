package com.miruken.mvc.android

import androidx.databinding.ViewDataBinding
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import android.view.View
import com.miruken.mvc.view.Viewing
import com.miruken.mvc.view.ViewingRegion

class ViewBindingLayout<B: ViewDataBinding>(
        @LayoutRes     val layoutId:    Int,
        @IdRes private val viewModelId: Int,
        private val initView: (View.(binding: B) -> Unit)? = null
) : Viewing {
    override var viewModel: Any? = null

    override fun display(region: ViewingRegion) =
            region.show(this)

    fun bind(view: View, binding: ViewDataBinding) {
        check(viewModel != null) {
            "A view model is required to bind the layout"
        }
        check(binding.setVariable(viewModelId, viewModel)) {
            "Unable to bind the view model to layout $layoutId.  Did you forget to add a data variable for the view model"
        }
        @Suppress("UNCHECKED_CAST")
        initView?.invoke(view, binding as B)
    }
}