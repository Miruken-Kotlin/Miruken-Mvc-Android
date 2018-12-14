package com.miruken.mvc.android.permission

import android.content.pm.PackageManager
import androidx.annotation.StringRes
import com.miruken.concurrent.Promise
import com.miruken.protocol.Protocol
import com.miruken.protocol.ProtocolAdapter
import com.miruken.protocol.proxy
import com.miruken.typeOf

@Protocol
interface Permissions {
    enum class Result(val result: Int) {
        GRANTED(PackageManager.PERMISSION_GRANTED),
        DENIED(PackageManager.PERMISSION_DENIED)
    }

    fun grantPermission(
            permission: String,
            @StringRes rationale: Int? = null
    ): Promise<Result>

    companion object {
        val PROTOCOL = typeOf<Permissions>()
        operator fun invoke(adapter: ProtocolAdapter) =
                adapter.proxy(PROTOCOL) as Permissions
    }
}