package com.miruken.mvc.android.databinding

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter

@BindingAdapter("android:visibility")
fun setVisibility(view: View, value: Boolean?) {
    value?.apply {
        view.visibility = if (value) View.VISIBLE else View.INVISIBLE
    }
}

@BindingAdapter("android:displayed")
fun setDisplayed(view: View, value: Boolean?) {
    value?.apply {
        view.visibility = if (value) View.VISIBLE else View.GONE
    }
}

@BindingAdapter("android:src")
fun setImage(view: ImageView, value: Int?) {
    value?.apply {
        view.setImageResource(value)
    }
}

@BindingAdapter("android:text")
fun setText(view: TextView, value: Long) {
    view.text = value.toString()
}