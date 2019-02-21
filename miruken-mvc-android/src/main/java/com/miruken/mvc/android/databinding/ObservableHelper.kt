package com.miruken.mvc.android.databinding

import androidx.databinding.Observable

@Suppress("UNCHECKED_CAST")
fun <T: Observable> T.addOnPropertyChanged(callback: (T, Int) -> Unit) =
        object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(observable: Observable, propertyId: Int) =
                    callback(observable as T, propertyId)
        }.also { addOnPropertyChangedCallback(it) }.let {
            { removeOnPropertyChangedCallback(it) }
        }