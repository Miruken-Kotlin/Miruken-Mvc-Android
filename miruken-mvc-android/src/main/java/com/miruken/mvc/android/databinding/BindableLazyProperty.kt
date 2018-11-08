package com.miruken.mvc.android.databinding

import androidx.annotation.IdRes
import kotlin.reflect.KProperty

class BindableLazyProperty<TSource>(
        initialValueProvider: () -> TSource,
        private val observable: NotifiableObservable,
        @IdRes private val propertyId: Int,
        private val beforeChange: ((oldValue: TSource, newValue: TSource) -> Boolean)? = null
) : ObservableProperty.Source.Lazy<TSource>(initialValueProvider) {

    override fun afterChange(property: KProperty<*>, oldValue: TSource, newValue: TSource) {
        observable.notifyPropertyChanged(propertyId)
    }

    override fun beforeChange(property: KProperty<*>, oldValue: TSource, newValue: TSource) =
            beforeChange?.invoke(oldValue, newValue) ?: (newValue != oldValue)
}

fun <TSource> NotifiableObservable.bindableLazy(
        initialValueProvider: () -> TSource,
        @IdRes propertyId: Int
) = BindableLazyProperty(initialValueProvider, this, propertyId)
