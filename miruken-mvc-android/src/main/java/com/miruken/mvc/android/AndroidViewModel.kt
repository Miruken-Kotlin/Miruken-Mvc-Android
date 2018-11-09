package com.miruken.mvc.android

import com.miruken.mvc.android.databinding.NotifiableObservable

@Suppress("LeakingThis")
abstract class AndroidViewModel :
        NotifiableObservable by NotifiableObservable.delegate() {
    init {
        initDelegator(this)
    }
}