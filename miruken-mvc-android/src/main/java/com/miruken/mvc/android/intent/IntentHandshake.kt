package com.miruken.mvc.android.intent

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import com.miruken.concurrent.Promise
import java.lang.IllegalStateException
import java.util.concurrent.ConcurrentHashMap

object IntentHandshake {
    private var nextId  = 0
    private val pending = ConcurrentHashMap<Int, Pending>()

    fun initiateIntent(intent: Intent, activity: Activity): Promise<Intent?> {
        return intent.resolveActivity(activity.packageManager)?.let {
            val requestId = getNextId()
            val request   = Pending().apply { pending[requestId] = this }
            activity.startActivityForResult(intent, requestId)
            request.promise finally { pending.remove(requestId) }
        } ?: Promise.reject(IllegalStateException(
                "Unable to resolve activity for Intent"
        ))
    }

    fun completeIntent(requestId: Int, resultCode: Int, data: Intent?) {
        pending[requestId]?.also {
            if (resultCode == Activity.RESULT_OK) {
                it.resolve(data)
            } else {
                it.reject(IntentException(resultCode,
                        "Intent failed with result code: $resultCode"))
            }
        }
    }

    fun fulfillIntent(intent: Intent, activity: Activity) {
        val activities = activity.packageManager.queryIntentActivities(
                intent,
                PackageManager.MATCH_DEFAULT_ONLY
        )
        if (activities.isNotEmpty()) {
            activity.startActivity(intent)
        }
    }

    @Synchronized
    private fun getNextId(): Int {
        val id = nextId++
        if (id == Int.MAX_VALUE)
            nextId = 0
        return id
    }

    private class Pending {
        lateinit var resolve: (Intent?) -> Unit
            private set
        lateinit var reject:  (Throwable) -> Unit
            private set

        val promise = Promise<Intent?> { resolve, reject ->
            this.resolve = resolve
            this.reject  = reject
        }
    }
}