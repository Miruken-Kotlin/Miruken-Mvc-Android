package com.miruken.mvc.android

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.miruken.callback.*
import com.miruken.concurrent.ChildCancelMode
import com.miruken.concurrent.Promise
import com.miruken.event.Event
import com.miruken.mvc.Navigation
import com.miruken.mvc.option.NavigationOptions
import com.miruken.mvc.view.Viewing
import com.miruken.mvc.view.ViewingLayer
import com.miruken.mvc.view.ViewingStackView
import java.util.concurrent.atomic.AtomicBoolean

class ViewRegion : ViewContainer, ViewingStackView {
    private val _layers       = mutableListOf<ViewLayer>()
    private var _unwinding    = false
    private var _isChild      = false

    constructor(
            context: Context
    ) : super(context)

    constructor(
            context: Context,
            attrs:   AttributeSet
    ) : super(context, attrs) {
        init(context, attrs, 0)
    }

    constructor(
            context:      Context,
            attrs:        AttributeSet,
            defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init(context, attrs, defStyleAttr)
    }

    val activeView get() = activeLayer?.view

    @Provides
    fun provideContext(): Context = context

    private fun init(
            context:      Context,
            attrs:        AttributeSet,
            defStyleAttr: Int
    ) {
        context.theme.obtainStyledAttributes(
                attrs, R.styleable.ViewRegion, defStyleAttr, 0).apply {
            tag = getString(R.styleable.ViewRegion_tag)
            recycle()
        }
    }

    override fun createViewStack() =
            ViewRegion(context).apply { _isChild = true }

    override fun show(view: Viewing, composer: Handling): ViewingLayer {
        val options       = composer.getOptions(NavigationOptions())
        val regionOptions = options?.region
        val region        = regionOptions?.region
        if (region != null && region != tag) {
            notHandled()
        }

        var push       = false
        var overlay    = false
        val navigation = composer.resolve<Navigation<*>>()

        if (region == null) {
            val impliedRegion = navigation?.viewRegion
            if (impliedRegion != null && impliedRegion != tag) {
                notHandled()
            }
        } else {
            navigation?.viewRegion = region
        }

        var layer: ViewLayer? = null

        regionOptions?.also {
            when {
                it.push == true -> push = true
                it.overlay == true -> {
                    push = true
                    overlay = true
                }
                it.unload == true -> {
                    unwindLayers()
                    push = true
                }
                else -> layer = it.choose(_layers) as ViewLayer
            }
        }

        if (push) {
            layer = if (overlay) {
                ViewLayer(push = true, overlay = true)
                        .apply { _layers.add(this) }
            } else {
                pushLayer()
            }
        } else if (layer == null) {
            layer = (navigation?.viewLayer as? ViewLayer)?.takeIf {
                _layers.contains(it)
            } ?: _layers.firstOrNull { !it.push }
              ?: ViewLayer(bottom = true)
                    .apply { _layers.add(0, this) }
        }

        return (layer ?: activeLayer)?.apply {
            if (navigation?.viewLayer == null) {
                navigation?.viewLayer = this
            }
            val bv = bindView(view, this, navigation)
            transitionTo(view to bv, options, composer)
        } ?: error("Unable to determine the view layer")
    }

    // Layers

    private val activeLayer get() =
        if (_layers.isNotEmpty()) _layers.last() else null

    override fun pushLayer() =
            ViewLayer(push = true).apply { _layers.add(this) }

    override fun unwindLayers() {
        _unwinding = true
        while (_layers.isNotEmpty()) {
            _layers.last().close()
        }
        _unwinding = false
    }

    private fun removeLayer(layer: ViewLayer) =
            layer.takeIf { _layers.remove(it) }
                    ?.transitionFrom()

    private fun dropLayer(layer: ViewLayer) =
            _layers.indexOf(layer).takeIf { it > 0 }?.let {
                _layers.removeAt(it)
                _layers[it - 1]
            }

    private fun getLayerBelow(layer: ViewLayer) =
        getLayerIndex(layer).takeIf { it > 0 }?.let {
            _layers[it - 1]
        }

    private fun getLayerIndex(layer: ViewLayer) =
            _layers.indexOf(layer)

    @Suppress("UNUSED_PARAMETER")
    private fun addView(
            fromView:       View?,
            view:           View,
            viewIndex:      Int?,
            options:        NavigationOptions?,
            removeFromView: Boolean,
            composer:       Handling
    ): Promise<*> {
        if (_unwinding && indexOfChild(view) >= 0) {
            return Promise.EMPTY
        }

        applyConstraints(view)

        val fromIndex = fromView?.let { indexOfChild(it) } ?: -1
        if (fromIndex >= 0) {
            addView(view, fromIndex + 1)
        } else {
            addView(view)
        }

        fromView?.takeIf { removeFromView }?.apply {
            removeView(fromView, null, composer)
        }

        return Promise.EMPTY
    }

    @Suppress("UNUSED_PARAMETER")
    private fun removeView(
            fromView: View,
            toView:   View?,
            composer: Handling?
    ): Promise<*> {
        removeView(fromView)
        return Promise.EMPTY
    }

    private fun bindView(
            view:       Viewing,
            layer:      ViewingLayer,
            navigation: Navigation<*>?
    ): View {
        if (view.viewModel == null) {
            navigation?.controller?.also { controller ->
                view.viewModel = controller
                controller.context?.also {
                    lateinit var dispose: () -> Unit
                    dispose = it.contextEnded.register { (_, reason) ->
                        // allows ending animation
                        if ((_layers.size > 1 || !_isChild) &&
                                reason !is Navigation<*>) {
                            layer.close()
                        }
                        dispose()
                    }
                }
            }
        }
        return when (view) {
            is View -> view
            is ViewProvider -> view.createView(context, this, navigation)
            else -> notHandled()
        }
    }

    private fun applyConstraints(view: View) = view.apply {
        layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT)
    }

    inner class ViewLayer(
            val push:    Boolean = false,
            val overlay: Boolean = false,
            val bottom:  Boolean = false
    ) : ViewingLayer {
        private var _composer: Handling? = null
        private var _disposed = false

        var view: Pair<Viewing, View>? = null
            private set

        override val index get()  = getLayerIndex(this)
        override val transitioned = Event<ViewingLayer>()
        override val disposed     = Event<ViewingLayer>()

        fun transitionTo(
                newView:  Pair<Viewing, View>,
                options:  NavigationOptions?,
                composer: Handling
        ): ViewingLayer {
            _composer = composer

            if (view === newView) {
                return this
            }

            var oldView = view

            oldView?.takeIf { overlay }?.also {
                val layer = dropLayer(this)
                if (layer != null) {
                    val actual = layer.transitionTo(
                            newView, options, composer)
                    removeView(oldView!!.second, null, composer)
                    return actual
                }
            }

            val removeFromView = oldView != null
            if (!removeFromView) {
                getLayerBelow(this)?.also {
                    oldView = it.view
                }
            }

            val index = if (bottom && oldView == null) 0 else null

            view = newView
            addView(oldView?.second, newView.second, index,
                    options, removeFromView, composer)

            transitioned(this)
            return this
        }

        fun transitionFrom() = AndroidThreading.runOnMainThread {
            val activeView = activeView
            view?.takeUnless {
                it.second === activeView?.second
            }?.also {
                removeView(it.second, activeView?.second, _composer)
            }
            view = null
        }

        override fun duration(durationMillis: Long): Promise<Boolean> {
            return Promise(ChildCancelMode.ANY) { resolve, _, onCancel ->
                val guard = AtomicBoolean(false)

                var cancelTransition: (() -> Unit)? = null
                var cancelDisposed:   (() -> Unit)? = null

                lateinit var expired: Runnable

                val stopTimer = { cancelled: Boolean, complete: Boolean ->
                    if (guard.compareAndSet(false, true)) {
                        cancelTransition?.invoke()
                        cancelDisposed?.invoke()
                        AndroidThreading.mainHandler.removeCallbacks(expired)
                        if (complete) resolve(cancelled)
                    }
                }

                onCancel { stopTimer(true, true) }

                expired = Runnable { stopTimer(false, true) }

                if (!AndroidThreading.mainHandler.postDelayed(
                                expired, durationMillis)) {
                    stopTimer(false, true)
                }

                cancelTransition = transitioned.register {
                    stopTimer(true, false)
                }

                cancelDisposed = transitioned.register {
                    stopTimer(false, false)
                }
            }
        }

        override fun close() {
            if (_disposed) return
            try {
                removeLayer(this)
            } finally {
                _disposed   = true
                _composer = null
                disposed(this)
            }
        }
    }
}