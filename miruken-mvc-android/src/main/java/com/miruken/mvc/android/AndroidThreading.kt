package com.miruken.mvc.android

import android.os.Handler
import android.os.Looper
import java.util.concurrent.FutureTask

object AndroidThreading {
    val mainLooper:  Looper  = Looper.getMainLooper()
    val mainHandler: Handler = Handler(mainLooper)
    val mainThread:  Thread  = mainLooper.thread

    fun postOnMainThread(block: () -> Unit) {
        if (Thread.currentThread() != mainThread) {
            mainHandler.post(block)
        } else {
            block()
        }
    }

    fun <T: Any?> runOnMainThread(block: () -> T): T =
            if (Thread.currentThread() != mainThread) {
                val task = FutureTask<T>(block)
                mainHandler.post(task)
                task.get()
            } else {
                block()
            }
}