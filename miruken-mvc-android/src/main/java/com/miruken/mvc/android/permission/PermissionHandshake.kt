package com.miruken.mvc.android.permission

import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Color
import android.view.View
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
import com.miruken.concurrent.ChildCancelMode
import com.miruken.concurrent.Promise
import com.miruken.concurrent.mapError
import com.miruken.concurrent.timeout
import com.miruken.mvc.android.R
import java.lang.IllegalStateException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeoutException

object PermissionHandshake {
    private var nextId  = 0
    private val pending = ConcurrentHashMap<Int, Pending>()

    fun checkPermission(
            permission: String,
            rationale:  Int?,
            activity:   Activity
    ): Promise<Permissions.Result> {
        val rc = ActivityCompat.checkSelfPermission(
                activity, permission)
        if (rc == PackageManager.PERMISSION_GRANTED) {
            return Promise.resolve(Permissions.Result.GRANTED)
        }

        if (rationale != null &&
                ActivityCompat.shouldShowRequestPermissionRationale(
                        activity, permission)) {
            return Promise { resolve, reject ->
                Snackbar.make(activity.findViewById<View>(android.R.id.content),
                        rationale, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.ok) {
                            requestPermission(permission, activity).then(resolve, reject)
                        }
                        .setActionTextColor(Color.WHITE)
                        .show()
            }
        }

        return requestPermission(permission, activity)
    }

    fun grantPermission(
            requestId:    Int,
            permissions:  Array<String>,
            grantResults: IntArray
    ) {
        pending[requestId]?.also {
            val permission = it.permission
            if (!permissions.contains(permission)) {
                it.reject(IllegalStateException(
                        "Unexpected permission request: $permission"))
            }
            if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                it.resolve(Permissions.Result.GRANTED)
            } else {
                it.reject(PermissionException(
                        Permissions.Result.DENIED,
                        "Permission $permission denied"))
            }
        }
    }

    private fun requestPermission(
            permission: String,
            activity:   Activity
    ): Promise<Permissions.Result> {
        val requestId = getNextId()
        val request   = Pending(permission).apply { pending[requestId] = this }
        ActivityCompat.requestPermissions(
                activity, arrayOf(permission), requestId)
        return request.promise.timeout(PERMISSION_TIMEOUT) mapError  {
            when (it) {
                is TimeoutException ->
                    throw PermissionException(
                            Permissions.Result.DENIED,
                            "Permission $permission denied")
                else -> throw it
            }
        } finally {
            pending.remove(requestId)
        }
    }

    @Synchronized
    private fun getNextId(): Int {
        val id = nextId++
        if (id == Int.MAX_VALUE)
            nextId = 0
        return id
    }

    private class Pending(val permission: String) {
        lateinit var resolve: (Permissions.Result) -> Unit
            private set
        lateinit var reject:  (Throwable) -> Unit
            private set

        val promise = Promise<Permissions.Result>(ChildCancelMode.ANY) { resolve, reject ->
            this.resolve = resolve
            this.reject  = reject
        }
    }
}

private const val PERMISSION_TIMEOUT = 15000L // 15 sec