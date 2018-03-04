package com.burlingamerobotics.scouting.common

class Listenable<T> {

    private val listeners: MutableList<Listener<T>> = mutableListOf()

    fun registerListener(listener: Listener<T>.(T) -> Unit) {
        listeners += Listener<T>(listener)
    }

    fun fire(obj: T) {
        listeners.filter {
            it.cb.invoke(it, obj)
            !it.remove
        }
    }

}

class Listener<T>(val cb: Listener<T>.(T) -> Unit) {
    var remove = false
}
