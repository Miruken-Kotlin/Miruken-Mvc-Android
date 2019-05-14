package com.miruken.mvc.android.intent

import android.content.Intent
import com.miruken.concurrent.Promise
import com.miruken.protocol.ProtocolAdapter
import com.miruken.protocol.proxy
import com.miruken.typeOf

interface Intents {
    fun fulfillIntentWithResult(intent: Intent): Promise<Intent?>

    fun fulfillIntent(intent: Intent, title: String?)

    companion object {
        val PROTOCOL = typeOf<Intents>()
        operator fun invoke(adapter: ProtocolAdapter) =
                adapter.proxy(PROTOCOL) as Intents
    }
}
