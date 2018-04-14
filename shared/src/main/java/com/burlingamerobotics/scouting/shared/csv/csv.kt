package com.burlingamerobotics.scouting.shared.csv

import java.lang.StringBuilder
import kotlin.reflect.full.declaredMemberProperties

class CSVSerializer<T>(val cols: List<CSVColumn<T>>, val sep: String = ",") {

    fun makeCSV(objects: List<T>): String {
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

data class CSVColumn<in T>(val name: String, val ser: (T) -> String)

inline fun <reified T : Any> createSerializers(): List<CSVColumn<T>> {
    return T::class.declaredMemberProperties.map { prop ->
        CSVColumn<T>(prop.name) { prop.call(it).toString() }
    }
}
