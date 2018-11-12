package com.miruken.mvc.android

import com.miruken.mvc.android.databinding.NotifiableObservable

abstract class AndroidViewModel :
        NotifiableObservable by NotifiableObservable.delegate(),
        AutoCloseable {

    init {
        @Suppress("LeakingThis")
        initDelegator(this)
    }

    final override fun close() {
        cleanUp()
    }

    open fun cleanUp() {}
}