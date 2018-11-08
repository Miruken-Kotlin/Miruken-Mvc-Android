package com.miruken.mvc.android.databinding

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter

@BindingAdapter("android:visibility")
fun setVisibility(view: View, value: Boolean?) {
    value?.apply {
        view.visibility = if (value) View.VISIBLE else View.INVISIBLE
    }
}

@BindingAdapter("app:src")
fun setImage(view: ImageView, value: Int?) {
    value?.apply {
        view.setImageResource(value)
    }
}
