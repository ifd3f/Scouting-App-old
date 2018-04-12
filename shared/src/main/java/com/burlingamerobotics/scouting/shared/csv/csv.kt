package com.burlingamerobotics.scouting.shared.csv

import java.lang.StringBuilder
import kotlin.reflect.full.declaredMembers

class CSVSerializer<T>(val sep: String = ",") {

    val objects = mutableListOf<T>()
    val cols = mutableListOf<CSVColumn<T>>()

    fun write(): String {
        val sb = StringBuilder()
        cols.joinTo(sb, sep) { sanitized(it.name) }

        objects.forEach { o ->
            sb.append('\n')
            cols.joinTo(sb, sep) { sanitized(it.ser(o)) }
        }
        return sb.toString()
    }

    fun sanitized(str: String): String {
        val escQuotes = str.replace("\"", "\"\"")
        if (str.contains(sep)) {
            return "\"$escQuotes\""
        }
        return escQuotes
    }

}

data class CSVColumn<T>(val name: String, val ser: (T) -> String)

/*
inline fun <reified T> getColumnSerializers(): List<CSVColumn<T>> {
    return T::class.declaredMembers.map { prop ->
        prop.
        //CSVColumn(prop.name) { obj ->  }
    }
}*/