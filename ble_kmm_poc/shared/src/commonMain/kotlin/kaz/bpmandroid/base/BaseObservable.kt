package kaz.bpmandroid.base

import kotlin.jvm.Synchronized

open class BaseObservable<Listener> : IBaseObservable<Listener> {
    private val _listeners by lazy { HashSet<Listener>() }

    val listeners: Set<Listener>
        get() = HashSet(_listeners)

    @Synchronized
    override fun registerListener(listener: Listener) {
        _listeners.remove(listener)
        _listeners.add(listener)
    }

    @Synchronized
    override fun unregisterListener(listener: Listener) {
        _listeners.remove(listener)
    }

    override fun invokeListeners(invoker: (Listener) -> Unit) {
        listeners.forEach(invoker::invoke)
    }
}
