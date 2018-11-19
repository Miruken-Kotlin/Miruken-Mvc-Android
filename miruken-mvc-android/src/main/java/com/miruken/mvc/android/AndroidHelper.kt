package com.miruken.mvc.android

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.IdRes

// Binding

fun <T : View> Activity.bind(@IdRes idRes: Int) =
    unsafeLazy { findViewById<T>(idRes) }

fun <T : View> View.bind(@IdRes idRes: Int) =
        unsafeLazy { findViewById<T>(idRes) }

private fun <T> unsafeLazy(initializer: () -> T) =
        lazy(LazyThreadSafetyMode.NONE, initializer)

// Keypad

fun View.showKeyboard(): View? {
    if (requestFocusFromTouch()) {
        val focused = findFocus() ?: return null
        val input   = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        input.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
        focused.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) focused.hideKeyboard()
        }
        return focused
    }
    return null
}

fun View.hideKeyboard() {
    val input = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    input.hideSoftInputFromWindow(windowToken, 0)
    val focused = findFocus()
    focused?.clearFocus()
}

