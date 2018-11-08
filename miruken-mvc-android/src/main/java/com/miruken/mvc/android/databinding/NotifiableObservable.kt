package com.miruken.mvc.android.databinding

import androidx.annotation.RestrictTo
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry

interface NotifiableObservable : Observable {

    fun initDelegator(delegator: NotifiableObservable)

    fun notifyPropertyChanged(propertyId: Int)

    fun notifyChange()

    object delegate :
            () -> NotifiableObservable,
            (LazyThreadSafetyMode) -> NotifiableObservable,
            (LazyThreadSafetyMode, () -> PropertyChangeRegistry) -> NotifiableObservable {

        private const val ALL_PROPERTIES = 0

        override fun invoke(): NotifiableObservable =
                NotifiableObservableImpl()

        override fun invoke(
                threadSafetyMode: LazyThreadSafetyMode
        ): NotifiableObservable =
                NotifiableObservableImpl(threadSafetyMode)

        @RestrictTo(RestrictTo.Scope.TESTS)
        override fun invoke(
                threadSafetyMode: LazyThreadSafetyMode,
                registryProvider: () -> PropertyChangeRegistry
        ): NotifiableObservable =
                NotifiableObservableImpl(threadSafetyMode, registryProvider)

        private class NotifiableObservableImpl(
                threadSafetyMode: LazyThreadSafetyMode = LazyThreadSafetyMode.NONE,
                registryProvider: () -> PropertyChangeRegistry = { PropertyChangeRegistry() }
        ) : NotifiableObservable {

            private val changeRegistryDelegate = lazy(threadSafetyMode, registryProvider)
            private val changeRegistry: PropertyChangeRegistry by changeRegistryDelegate

            private lateinit var delegator: NotifiableObservable

            override fun initDelegator(delegator: NotifiableObservable) {
                this.delegator = delegator
            }

            override fun addOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
                changeRegistry.add(callback)
            }

            override fun removeOnPropertyChangedCallback(callback: Observable.OnPropertyChangedCallback) {
                if (changeRegistryDelegate.isInitialized()) {
                    changeRegistry.remove(callback)
                }
            }

            override fun notifyChange() {
                if (changeRegistryDelegate.isInitialized()) {
                    changeRegistry.notifyChange(delegator, ALL_PROPERTIES)
                }
            }

            override fun notifyPropertyChanged(propertyId: Int) {
                if (changeRegistryDelegate.isInitialized()) {
                    changeRegistry.notifyChange(delegator, propertyId)
                }
            }
        }
    }
}