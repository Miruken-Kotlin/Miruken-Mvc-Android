package com.miruken.mvc.android

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.miruken.TypeReference
import com.miruken.callback.Handling
import com.miruken.callback.notHandled
import com.miruken.callback.requireComposer
import com.miruken.mvc.view.Viewing
import com.miruken.mvc.view.ViewingLayer
import com.miruken.mvc.view.ViewingRegion
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf

abstract class ViewContainer :
        ConstraintLayout, ViewingRegion, Viewing {

    constructor(
            context: Context?
    ) : super(context)

    constructor(
            context: Context?,
            attrs:   AttributeSet?
    ) : super(context, attrs)

    constructor(
            context:      Context?,
            attrs:        AttributeSet?,
            defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr)

    override var viewModel: Any? = null

    override fun display(region: ViewingRegion) =
            region.show(this)

    override fun view(
            viewKey: Any,
            init:    (Viewing.() -> Unit)?
    )= (when (viewKey) {
        is KClass<*> -> createView(viewKey)
        is KType -> (viewKey.classifier as? KClass<*>)
                ?.let(::createView)
        is TypeReference -> (viewKey.type as? Class<*>)?.kotlin
                ?.let(::createView)
        else -> null
    } ?: notHandled()).also { init?.invoke(it) }

    override fun show(view: Viewing): ViewingLayer {
        val composer = requireComposer()
        return AndroidThreading.runOnMainThread {
            show(view, composer)
        }
    }

    abstract fun show(
            view:     Viewing,
            composer: Handling
    ): ViewingLayer

    protected fun inflateLayout(layout: ViewLayout): View =
        View.inflate(context, layout.layoutId, null).apply {
            layout.init(this)
        }

    protected fun inflateBinding(layout: ViewBindingLayout<*>): View {
        val inflater = LayoutInflater.from(context)
        val binding  = DataBindingUtil.inflate<ViewDataBinding>(
                inflater, layout.layoutId, this, false)
        layout.bind(binding.root, binding)
        return binding.root
    }

    private fun createView(viewClass: KClass<*>): Viewing? {
        if (!viewClass.isSubclassOf(Viewing::class) ||
                viewClass.java.isInterface ||
                viewClass.isAbstract) {
            return null
        }
        return viewClass.constructors.firstOrNull {
            it.parameters.size == 1 &&
            it.parameters[0].type.classifier == Context::class
        }?.let {
            AndroidThreading.runOnMainThread {
                it.call(context) as Viewing
            }
        }
    }
}