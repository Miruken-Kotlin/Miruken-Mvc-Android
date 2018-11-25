package com.miruken.mvc.android.databinding

import android.view.View
import androidx.databinding.BindingAdapter

interface ClickBindings {
    @BindingAdapter("android:onClick", requireAll = false)
    fun bindClick(view: View, listener: View.OnClickListener)
}