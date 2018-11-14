package com.miruken.mvc.android

import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.databinding.ObservableBoolean
import androidx.databinding.ViewDataBinding
import com.miruken.callback.Handling
import com.miruken.mvc.Controller
import com.miruken.mvc.android.databinding.NotifiableObservable
import java.util.concurrent.atomic.AtomicBoolean

open class AndroidController : Controller(),
        NotifiableObservable by NotifiableObservable.delegate(),
        Guarding {
    private val _guarded = AtomicBoolean(false)

    init {
        @Suppress("LeakingThis")
        initDelegator(this)
    }

    abstract inner class ViewModel : AndroidViewModel()

    val guarded   = ObservableBoolean(false)
    val unguarded = ObservableBoolean(true)

    override fun guard(guard: Boolean): Boolean {
        if (_guarded.compareAndSet(!guard, guard)) {
            guarded.set(guard)
            unguarded.set(!guard)
            return true
        }
        return false
    }

    protected fun showR(
            @LayoutRes layoutId: Int,
            viewModel: Any? = null,
            init:      (View.() -> Unit)? = null
    ) = show(ViewLayout(layoutId, viewModel, init))

    protected fun showR(
            handler:   Handling,
            @LayoutRes layoutId: Int,
            viewModel: Any? = null,
            init:      (View.() -> Unit)? = null
    ) = show(handler, ViewLayout(layoutId, viewModel, init))

    protected fun showR(
            @LayoutRes layoutId:    Int,
            @IdRes     viewModelId: Int,
            viewModel: Any? = null,
            init:      (View.(binding: ViewDataBinding) -> Unit)? = null
    ) = show(ViewBindingLayout(layoutId, viewModelId, viewModel, init))

    protected fun showR(
            handler:   Handling,
            @LayoutRes layoutId:    Int,
            @IdRes     viewModelId: Int,
            viewModel: Any? = null,
            init:      (View.(binding: ViewDataBinding) -> Unit)? = null
    ) = show(handler, ViewBindingLayout(layoutId, viewModelId, viewModel, init))

    protected fun <B: ViewDataBinding> show(
            @LayoutRes layoutId:    Int,
            @IdRes     viewModelId: Int,
            viewModel: Any? = null,
            init:      (View.(binding: B) -> Unit)? = null
    ) = show(ViewBindingLayout(layoutId, viewModelId, viewModel, init))

    protected fun <B: ViewDataBinding> show(
            handler:   Handling,
            @LayoutRes layoutId:    Int,
            @IdRes     viewModelId: Int,
            viewModel: Any? = null,
            init:      (View.(binding: B) -> Unit)? = null
    ) = show(handler, ViewBindingLayout(layoutId, viewModelId, viewModel, init))
}