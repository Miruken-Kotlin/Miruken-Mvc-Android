package com.miruken.mvc.android

import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import com.miruken.callback.Handling
import com.miruken.callback.aspectBefore
import com.miruken.mvc.option.overlay
import com.miruken.mvc.view.ViewingRegion
import com.miruken.protocol.proxy

fun Handling.overlayR(
        @LayoutRes layoutId: Int,
        viewModel: Any? = null,
        init:      (View.() -> Unit)? = null
) = aspectBefore({ _, composer ->
    composer.overlay.proxy<ViewingRegion>()
            .show(ViewLayout(layoutId, viewModel, init))
})

fun Handling.overlayR(
        @LayoutRes layoutId:    Int,
        @IdRes     viewModelId: Int,
        viewModel: Any? = null,
        init:      (View.(binding: ViewDataBinding) -> Unit)? = null
) = aspectBefore({ _, composer ->
    composer.overlay.proxy<ViewingRegion>()
            .show(ViewBindingLayout(layoutId, viewModelId, viewModel, init))
})

fun <B: ViewDataBinding> Handling.overlay(
        @LayoutRes layoutId:    Int,
        @IdRes     viewModelId: Int,
        viewModel: Any? = null,
        init:      (View.(binding: B) -> Unit)? = null
) = aspectBefore({ _, composer ->
    composer.overlay.proxy<ViewingRegion>()
            .show(ViewBindingLayout(layoutId, viewModelId, viewModel, init))
})
