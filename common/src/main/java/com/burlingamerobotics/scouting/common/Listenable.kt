package com.burlingamerobotics.scouting.common

class Listenable<T> {

    private val listeners: MutableList<(T) -> Unit> = mutableListOf()

    fun registerListener(listener: (T) -> Unit) {
        listeners += listener
    }

    fun fire(obj: T) {
        listeners.forEach { it(obj) }
    }

}