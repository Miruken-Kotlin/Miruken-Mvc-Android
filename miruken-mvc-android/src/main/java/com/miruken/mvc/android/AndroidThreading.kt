package com.miruken.mvc.android

import android.os.Handler
import android.os.Looper

object AndroidThreading {
    val mainLooper: Looper = Looper.getMainLooper()
    val mainHandler: Handler = Handler(mainLooper)
    val mainThread: Thread = mainLooper.thread
}