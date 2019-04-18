package com.miruken.mvc.android

import androidx.appcompat.app.AppCompatActivity
import com.miruken.TypeReference
import com.miruken.addSorted
import com.miruken.callback.policy.*
import com.miruken.callback.policy.bindings.PolicyMemberBinding
import com.miruken.callback.policy.bindings.PolicyMemberBindingInfo
import com.miruken.runtime.getMetaAnnotations
import com.miruken.runtime.isInstanceCallable
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.KType

class FixedHandlerDescriptorFactory(
        descriptors: Collection<HandlerDescriptor>
) : HandlerDescriptorFactory {

    private val _descriptors =
            descriptors.associateBy { it.handlerClass }

    private val _handlerCache = ConcurrentHashMap<HandlerCacheKey, List<KType>>()

    override fun getDescriptor(handlerClass: KClass<*>) = _descriptors[handlerClass]

    override fun getInstanceHandlers(
            policy:       CallbackPolicy,
            callback:     Any,
            callbackType: TypeReference?
    ) = getCachedHandlerTypes(policy, callback, callbackType, true, false)

    override fun getTypeHandlers(
            policy:       CallbackPolicy,
            callback:     Any,
            callbackType: TypeReference?
    ) = getCachedHandlerTypes(policy, callback, callbackType, false, true)

    override fun getCallbackHandlers(
            policy:       CallbackPolicy,
            callback:     Any,
            callbackType: TypeReference?
    ) = getCachedHandlerTypes(policy, callback, callbackType, true, true)

    private fun getCachedHandlerTypes(
            policy:       CallbackPolicy,
            callback:     Any,
            callbackType: TypeReference? = null,
            instances:    Boolean        = false,
            types:        Boolean        = false
    ): List<KType> = _handlerCache.getOrPut(HandlerCacheKey(
            callback, callbackType, policy, instances, types)) {
        getHandlerTypes(policy, callback, callbackType, instances, types)
    }

    private fun getHandlerTypes(
            policy:       CallbackPolicy,
            callback:     Any,
            callbackType: TypeReference? = null,
            instances:    Boolean        = false,
            types:        Boolean        = false
    ): List<KType> {
        if (_descriptors.isEmpty()) return emptyList()
        val invariants   = mutableListOf<PolicyMemberBinding>()
        val compatible   = mutableListOf<PolicyMemberBinding>()
        val orderMembers = policy.orderMembers

        _descriptors.values.forEach { descriptor ->
            val instanceCallbacks = descriptor.instancePolicies
                    .takeIf { instances }?.get(policy)
            val typeCallbacks     = descriptor.typePolicies
                    .takeIf { types }?.get(policy)

            (instanceCallbacks
                    ?.getInvariantMembers(callback, callbackType)
                    ?.firstOrNull() ?:
            typeCallbacks?.getInvariantMembers(callback, callbackType)
                    ?.firstOrNull())?.also { invariants.add(it) } ?:
            (instanceCallbacks
                    ?.getCompatibleMembers(callback, callbackType)
                    ?.firstOrNull() ?:
            typeCallbacks?.getCompatibleMembers(callback, callbackType)
                    ?.firstOrNull())?.also { compatible.addSorted(it, orderMembers) }
        }

        return (invariants + compatible).map { it.dispatcher.owningType }
    }

    override fun getPolicyMembers(policy: CallbackPolicy, key: Any) =
            _descriptors.values.flatMap { descriptor ->
                (descriptor.instancePolicies[policy]?.let {
                    it.getInvariantMembers(key, null) +
                            it.getCompatibleMembers(key, null)
                } ?: emptyList()) +
                        (descriptor.typePolicies[policy]?.let {
                            it.getInvariantMembers(key, null) +
                                    it.getCompatibleMembers(key, null)
                        } ?: emptyList())
            }

    override fun getPolicyMembers(policy: CallbackPolicy) =
            _descriptors.values.flatMap { descriptor ->
                (descriptor.instancePolicies[policy]?.invariantMembers
                        ?: emptyList()) +
                        (descriptor.typePolicies[policy]?.invariantMembers
                                ?: emptyList())
            }

    companion object {
        fun registerHandlers(
                activity:     AppCompatActivity,
                resourceName: String,
                visitor:      HandlerDescriptorVisitor? = null,
                done:         suspend CoroutineScope.() -> Unit
        ) {
            registerHandlers(activity, listOf(resourceName), visitor, done)
        }

        fun registerHandlers(
                activity:      AppCompatActivity,
                resourceNames: Collection<String>,
                visitor:       HandlerDescriptorVisitor? = null,
                done:          suspend CoroutineScope.() -> Unit
        ) = GlobalScope.launch {
            val resources = activity.resources
            val begin = System.currentTimeMillis()

            resourceNames.flatMap { resourceName ->
                val id = resources.getIdentifier(resourceName, "raw", activity.packageName)
                BufferedReader(InputStreamReader(resources.openRawResource(id)))
                        .use { it.readLines() }.map { line -> async {
                            val start           = System.currentTimeMillis()
                            val members         = line.split(";").filter { it.isNotBlank() }
                            val handler         = members[0]
                            val methods         = members.drop(1)
                            val hasMethods      = methods.any { !it.startsWith("<init>") }
                            val hasConstructors = methods.any { it.startsWith("<init>") }
                            val handlerClass    = Class.forName(handler).kotlin
                            createDescriptor(handlerClass, hasMethods, hasConstructors, visitor).also {
                                val time = System.currentTimeMillis() - start
                                Timber.d("Register handler '$handlerClass' took ($time ms) ${Thread.currentThread().name}")
                            }
                        }
                        }.map { it.await() }.also {
                            HandlerDescriptorFactory.useFactory(FixedHandlerDescriptorFactory(it))
                        }.also {
                            val total = System.currentTimeMillis() - begin
                            Timber.d("Registered ${it.size} handlers in ($total ms) ${Thread.currentThread().name}")
                            withContext(Dispatchers.Main, done)
                        }
            }
        }

        private fun createDescriptor(
                handlerClass:    KClass<*>,
                hasMethods:      Boolean,
                hasConstructors: Boolean,
                visitor:         HandlerDescriptorVisitor?
        ): HandlerDescriptor {
            var instancePolicies: MutableMap<CallbackPolicy, MutableList<PolicyMemberBinding>>? = null
            var typePolicies:     MutableMap<CallbackPolicy, MutableList<PolicyMemberBinding>>? = null
            if (hasMethods) {
                handlerClass.members.filter { it.isInstanceCallable }.forEach { member ->
                    val method = when (member) {
                        is KProperty<*> -> member.getter
                        is KFunction<*> -> member
                        else -> null
                    } ?: return@forEach
                    val dispatch by lazy(LazyThreadSafetyMode.NONE) {
                        CallableDispatch(method)
                    }
                    for ((annotation, usePolicies) in method
                            .getMetaAnnotations<UsePolicy>(false)) {
                        usePolicies.single().policy?.also {
                            val rule = it.match(dispatch)
                                    ?: throw PolicyRejectedException(it, method,
                                            "The policy for @${annotation.annotationClass.simpleName} rejected '$member'")
                            val binding = rule.bind(it, dispatch, annotation)
                            val policies = when {
                                handlerClass.objectInstance != null -> {
                                    if (typePolicies == null) {
                                        typePolicies = mutableMapOf()
                                    }
                                    typePolicies!!
                                }
                                else -> {
                                    if (instancePolicies == null) {
                                        instancePolicies = mutableMapOf()
                                    }
                                    instancePolicies!!
                                }
                            }
                            policies.getOrPut(it) { mutableListOf() }.add(binding)
                        }
                    }
                }
            }

            if (hasConstructors) {
                handlerClass.constructors.forEach { constructor ->
                    val dispatch by lazy(LazyThreadSafetyMode.NONE) {
                        CallableDispatch(constructor)
                    }
                    for ((annotation, usePolicies) in constructor
                            .getMetaAnnotations<UsePolicy>(false)) {
                        usePolicies.single().policy?.also {
                            val bindingInfo = PolicyMemberBindingInfo(
                                    null, dispatch, annotation, false).apply {
                                outKey = constructor.returnType
                            }
                            val binding = it.bindMethod(bindingInfo)
                            if (typePolicies == null) {
                                typePolicies = mutableMapOf()
                            }
                            typePolicies!!.getOrPut(it) { mutableListOf() }.add(binding)
                        }
                    }
                }
            }

            val descriptor = HandlerDescriptor(
                    handlerClass,
                    instancePolicies?.mapValues { entry  ->
                        CallbackPolicyDescriptor(entry.key, entry.value)
                    } ?: emptyMap(),
                    typePolicies?.mapValues { entry  ->
                        CallbackPolicyDescriptor(entry.key, entry.value)
                    } ?: emptyMap()
            )

            if (visitor != null) {
                instancePolicies?.values?.flatten()?.forEach {
                    visitor(descriptor, it)

                }
                typePolicies?.values?.flatten()?.forEach {
                    visitor(descriptor, it)
                }
            }

            return descriptor
        }
    }

    private class HandlerCacheKey(
            callback:             Any,
            callbackType:         TypeReference?,
            private val policy:   CallbackPolicy,
            private val instance: Boolean,
            private val types:    Boolean
    ) {
        private val _key = policy.getKey(callback, callbackType)
        private val _hash = (31 * (31 * (31 * policy.hashCode() +
                _key.hashCode())) + instance.hashCode()) +
                types.hashCode()

        override fun equals(other: Any?): Boolean {
            return other is HandlerCacheKey &&
                    policy === other.policy &&
                    _key == other._key &&
                    instance == other.instance &&
                    types == other.types
        }

        override fun hashCode() = _hash
    }
}