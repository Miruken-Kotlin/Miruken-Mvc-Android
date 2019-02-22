package com.miruken.mvc.android

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.miruken.mvc.Navigation
import com.miruken.mvc.android.databinding.DataBindingConventions
import com.miruken.mvc.view.Viewing
import com.miruken.mvc.view.ViewingRegion

class ViewBindingLayout<B: ViewDataBinding>(
        @LayoutRes     val layoutId:    Int,
        @IdRes private val viewModelId: Int,
        override       var viewModel: Any? = null,
        private        val initView: ((B) -> Unit)? = null
) : Viewing, ViewProvider {
    override fun display(region: ViewingRegion) = region.show(this)

    override fun createView(
            context:    Context,
            parent:     ViewGroup,
            navigation: Navigation<*>?
    ): View {
        val inflater = LayoutInflater.from(context)
        val binding  = DataBindingUtil.inflate<ViewDataBinding>(
                inflater, layoutId, parent, false,
                DataBindingConventions)
        bind(binding)
        return binding.root
    }

    private fun bind(binding: ViewDataBinding) {
        check(viewModel != null) {
            "A view model is required to bind the layout"
        }
        check(binding.setVariable(viewModelId, viewModel)) {
            "Unable to bind the view model to layout $layoutId.  Did you forget to add a data variable for the view model?"
        }
        @Suppress("UNCHECKED_CAST")
        initView?.invoke(binding as B)
    }
}