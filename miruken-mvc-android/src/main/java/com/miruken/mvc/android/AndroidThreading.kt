@file:Suppress("MemberVisibilityCanBePrivate")

package com.miruken.mvc.android

import android.os.Handler
import android.os.Looper
import java.util.concurrent.ExecutionException
import java.util.concurrent.FutureTask

object AndroidThreading {
    val mainLooper:  Looper  = Looper.getMainLooper()
    val mainHandler: Handler = Handler(mainLooper)
    val mainThread:  Thread  = mainLooper.thread

    fun postOnMainThread(block: () -> Unit) {
        if (Thread.currentThread() != mainThread) {
            val task = FutureTask(block)
            mainHandler.post(task)
            try {
                task.get()
            } catch (t: ExecutionException) {
                throw t.cause ?: t
            }
        } else {
            block()
        }
    }

    fun <T: Any?> runOnMainThread(block: () -> T): T =
            if (Thread.currentThread() != mainThread) {
                val task = FutureTask(block)
                mainHandler.post(task)
                try {
                    task.get()
                } catch (t: ExecutionException) {
                    throw t.cause ?: t
                }
            } else {
                block()
            }
}