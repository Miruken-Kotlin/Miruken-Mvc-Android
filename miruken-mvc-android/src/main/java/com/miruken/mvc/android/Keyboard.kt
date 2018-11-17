package com.miruken.mvc.android

import android.view.View
import com.miruken.protocol.Protocol
import com.miruken.protocol.ProtocolAdapter
import com.miruken.protocol.proxy
import com.miruken.typeOf

@Protocol
interface Keyboard {
    fun present(focus: View? = null): View?
    fun dismiss()

    companion object {
        val PROTOCOL = typeOf<Keyboard>()
        operator fun invoke(adapter: ProtocolAdapter) =
                adapter.proxy(PROTOCOL) as Keyboard
    }
}