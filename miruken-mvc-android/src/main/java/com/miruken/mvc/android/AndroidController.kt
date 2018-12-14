package com.miruken.mvc.android

import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.databinding.ObservableBoolean
import androidx.databinding.ViewDataBinding
import com.miruken.callback.Handling
import com.miruken.context.requireContext
import com.miruken.mvc.Controller
import com.miruken.mvc.android.databinding.NotifiableObservable
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

open class AndroidController : Controller(),
        NotifiableObservable by NotifiableObservable.delegate(),
        Guarding, CoroutineScope {

    init {
        @Suppress("LeakingThis")
        initDelegator(this)
    }

    private val job = SupervisorJob()

    val guarded = ObservableBoolean(false)

    val guard get() = requireContext().guard(this)

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun guard(guard: Boolean): Boolean {
        if (guarded.get() != guard) {
            guarded.set(guard)
            return true
        }
        return false
    }

    abstract inner class ViewModel : AndroidViewModel()

    // Render

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

    // Keyboard

    @CallSuper
    open fun presentKeyboard(focus: View? = null) {
        context?.also { Keyboard(it).presentKeyboard(focus) }
    }

    @CallSuper
    open fun dismissKeyboard() {
        context?.also { Keyboard(it).dismissKeyboard() }
    }

    @CallSuper
    override fun cleanUp() {
        coroutineContext.cancelChildren()
    }
}