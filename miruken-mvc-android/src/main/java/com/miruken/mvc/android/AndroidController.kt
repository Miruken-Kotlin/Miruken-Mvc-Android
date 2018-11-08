package com.miruken.mvc.android

import androidx.databinding.ViewDataBinding
import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import com.miruken.callback.Handling
import com.miruken.mvc.Controller
import com.miruken.mvc.android.databinding.NotifiableObservable

@Suppress("LeakingThis")
open class AndroidController : Controller(),
        NotifiableObservable by NotifiableObservable.delegate() {

    init {
        initDelegator(this)
    }

    protected fun showR(
            @LayoutRes layoutId: Int,
            init: (View.() -> Unit)? = null
    ) = show(ViewLayout(layoutId, init))

    protected fun showR(
            handler:   Handling,
            @LayoutRes layoutId: Int,
            init:      (View.() -> Unit)? = null
    ) = show(handler, ViewLayout(layoutId, init))

    protected fun showR(
            @LayoutRes layoutId:    Int,
            @IdRes     viewModelId: Int,
            init:      (View.(binding: ViewDataBinding) -> Unit)? = null
    ) = show(ViewBindingLayout(layoutId, viewModelId, init))

    protected fun showR(
            handler:   Handling,
            @LayoutRes layoutId:    Int,
            @IdRes     viewModelId: Int,
            init:      (View.(binding: ViewDataBinding) -> Unit)? = null
    ) = show(handler, ViewBindingLayout(layoutId, viewModelId, init))

    protected fun <B: ViewDataBinding> show(
            @LayoutRes layoutId:    Int,
            @IdRes     viewModelId: Int,
            init:      (View.(binding: B) -> Unit)? = null
    ) = show(ViewBindingLayout(layoutId, viewModelId, init))

    protected fun <B: ViewDataBinding> show(
            handler:   Handling,
            @LayoutRes layoutId:    Int,
            @IdRes     viewModelId: Int,
            init:      (View.(binding: B) -> Unit)? = null
    ) = show(handler, ViewBindingLayout(layoutId, viewModelId, init))
}