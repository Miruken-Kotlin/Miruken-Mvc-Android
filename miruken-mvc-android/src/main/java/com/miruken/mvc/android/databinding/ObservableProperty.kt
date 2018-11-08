package com.miruken.mvc.android.databinding

import androidx.annotation.CallSuper
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <TSource> observable(initialValue: TSource) =
        ObservableProperty.Source.Standard(initialValue)

typealias AfterChange<T> =
        (property: KProperty<*>, oldValue: T?, newValue: T) -> Unit

interface ObservableProperty<TSource, TTarget> : ReadWriteProperty<Any?, TSource> {
    val sourceValue: TSource

    val source: Source<TSource>

    val afterChangeObservers: MutableSet<AfterChange<TTarget>>

    @CallSuper
    fun onProvideDelegate(thisRef: Any?, property: KProperty<*>) {}

    abstract class Source<TSource> : ObservableProperty<TSource, TSource> {
        open class Standard<TSource>(initialValue: TSource) : Source<TSource>() {
            override var sourceValue = initialValue
        }

        @Suppress("UNCHECKED_CAST")
        open class Lazy<TSource>(
                private val initialValueProvider: () -> TSource
        ) : Source<TSource>() {
            private var _sourceValue: Any? = UNINITIALISED
            override var sourceValue
                get() = when (_sourceValue) {
                    UNINITIALISED -> initialValueProvider().also { _sourceValue = it }
                    else -> _sourceValue as TSource
                }
                set(value) {
                    _sourceValue = value
                }

            private companion object {
                val UNINITIALISED = Any()
            }
        }

        abstract class WithProperty<TSource>(
                private val beforeChange: (oldValue: TSource, newValue: TSource) -> Boolean
        ) : ObservableProperty.Source<TSource>() {

            private lateinit var property: KProperty<*>

            override fun onProvideDelegate(thisRef: Any?, property: KProperty<*>) {
                super.onProvideDelegate(thisRef, property)
                provideDelegate(thisRef, property)
            }

            private operator fun provideDelegate(
                    thisRef:  Any?,
                    property: KProperty<*>
            ) = apply { this.property = property }

            override fun beforeChange(property: KProperty<*>, oldValue: TSource, newValue: TSource) =
                    beforeChange(oldValue, newValue)
        }

        abstract override var sourceValue: TSource

        private var _afterChangeObservers: MutableSet<AfterChange<TSource>>? = null
        private var _onProvideDelegateObservers: MutableSet<AfterChange<TSource>>? = null

        override val afterChangeObservers: MutableSet<AfterChange<TSource>>
            get() = synchronized(this) {
                _afterChangeObservers.let { after ->
                    after ?: mutableSetOf<AfterChange<TSource>>().also { _afterChangeObservers = it }
                }
            }

        private val onProvideDelegateObservers: MutableSet<AfterChange<TSource>>
            get() = synchronized(this) {
                _onProvideDelegateObservers.let { after ->
                    after ?: mutableSetOf<AfterChange<TSource>>().also { _onProvideDelegateObservers = it }
                }
            }

        private var assignmentInterceptor: ((TSource) -> TSource)? = null

        override val source: Source<TSource> get() = this

        protected open fun beforeChange(property: KProperty<*>, oldValue: TSource, newValue: TSource) = true

        protected open fun afterChange(property: KProperty<*>, oldValue: TSource, newValue: TSource) {}

        override fun getValue(thisRef: Any?, property: KProperty<*>) = sourceValue

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: TSource) {
            val oldValue = sourceValue
            val interceptedValue = assignmentInterceptor?.let { it(value) } ?: value
            if (!beforeChange(property, oldValue, interceptedValue)) {
                return
            }
            sourceValue = interceptedValue
            afterChange(property, oldValue, interceptedValue)
            notifyObservers(property, oldValue, interceptedValue)
        }

        override fun onProvideDelegate(thisRef: Any?, property: KProperty<*>) {
            super.onProvideDelegate(thisRef, property)
            if (_onProvideDelegateObservers != null) {
                onProvideDelegateObservers.forEach { observer ->
                    observer(property, null, sourceValue)
                }
            }
        }

        private fun notifyObservers(property: KProperty<*>, oldValue: TSource?, newValue: TSource) {
            if (_afterChangeObservers != null) {
                afterChangeObservers.apply {
                    forEach { observer ->
                        observer(property, oldValue, newValue)
                    }
                }
            }
        }
    }
}