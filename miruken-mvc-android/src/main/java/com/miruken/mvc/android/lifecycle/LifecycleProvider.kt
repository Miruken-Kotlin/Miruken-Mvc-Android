package com.miruken.mvc.android.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.miruken.TypeReference
import com.miruken.callback.*
import com.miruken.callback.policy.bindings.MemberBinding
import com.miruken.context.Contextual
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.jvmErasure

class LifecycleProvider(val lifecycle: Lifecycle) : FilteringProvider {
    constructor(owner: LifecycleOwner) : this(owner.lifecycle)

    override val required = true

    override fun appliesTo(
            callback: Any,
            callbackType: TypeReference?
    ) = callback is Inquiry

    override fun getFilters(
            binding:      MemberBinding,
            filterType:   KType,
            callback:     Any,
            callbackType: TypeReference?,
            composer:     Handling
    ): List<Filtering<*, *>>? {
        return binding.returnType.jvmErasure.let {
            if (it.isSubclassOf(LifecycleAware::class)) {
                listOf(LifecycleAwareFilter)
            } else if (it.isSubclassOf(LifecycleObserver::class) &&
                       it.isSubclassOf(Contextual::class)) {
                listOf(LifecycleObserverFilter)
            } else {
                emptyList()
            }
        }
    }

    private object LifecycleAwareFilter : Filtering<Inquiry, LifecycleAware> {
        override var order: Int? = Int.MAX_VALUE - 1000

        override fun next(
                callback:    Inquiry,
                rawCallback: Any,
                binding:     MemberBinding,
                composer:    Handling,
                next:        Next<LifecycleAware>,
                provider:    FilteringProvider?
        ) = next().also {
            it then { observer ->
                val lifecycle = (provider as LifecycleProvider).lifecycle
                observer.lifecycle = lifecycle
            }
        }
    }

    private object LifecycleObserverFilter : Filtering<Inquiry, LifecycleObserver> {
        override var order: Int? = Int.MAX_VALUE - 1000

        override fun next(
                callback:    Inquiry,
                rawCallback: Any,
                binding:     MemberBinding,
                composer:    Handling,
                next:        Next<LifecycleObserver>,
                provider:    FilteringProvider?
        ) = next().also {
            it then { observer ->
                (observer as Contextual).context?.also { ctx ->
                    val lifecycle = (provider as LifecycleProvider).lifecycle
                    lifecycle.addObserver(observer)
                    ctx.contextEnded.register {
                        lifecycle.removeObserver(observer)
                    }
                }
            }
        }
    }
}



