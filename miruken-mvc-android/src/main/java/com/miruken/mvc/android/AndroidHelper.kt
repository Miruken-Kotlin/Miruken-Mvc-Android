package com.miruken.mvc.android

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.IdRes
import java.util.concurrent.FutureTask

// Binding

fun <T : View> Activity.bind(@IdRes idRes: Int) =
    unsafeLazy { findViewById<T>(idRes) }

fun <T : View> View.bind(@IdRes idRes: Int) =
        unsafeLazy { findViewById<T>(idRes) }

private fun <T> unsafeLazy(initializer: () -> T) =
        lazy(LazyThreadSafetyMode.NONE, initializer)

// Synchronization

fun <T: Any?> runOnMainThread(block: () -> T): T =
    if (Thread.currentThread() != AndroidThreading.mainThread) {
        val task = FutureTask<T>(block)
        AndroidThreading.mainHandler.post(task)
        task.get()
    } else {
        block()
    }

// Keypad

fun View.showKeyboard() {
    if (requestFocusFromTouch()) {
        val input = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        input.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }
}

fun View.hideKeyboard() {
    val input = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    input.hideSoftInputFromWindow(windowToken, 0)
}

