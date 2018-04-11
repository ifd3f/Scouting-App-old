package com.burlingamerobotics.scouting.common

import com.burlingamerobotics.scouting.common.csv.CSVColumn
import com.burlingamerobotics.scouting.common.csv.CSVSerializer
import org.junit.Test

data class boi(val name: String, val age: Int, val comment: String)

class BoiTest() {

    @Test
    fun test(args: Array<String>) {
        val dudes = listOf(
                boi("Meme McMeme", 30, "He's a funny, silly, man"),
                boi("asdf", 5, "sol dfjd asdlf"),
                boi("sajdflk", 413, "vdsf132,43141313,\"432\"")
        )

        val csv = CSVSerializer<boi>()
        csv.objects.addAll(dudes)
        csv.cols.add(CSVColumn("name") { it.name })
        csv.cols.add(CSVColumn("age") { it.age.toString() })
        csv.cols.add(CSVColumn("comment") { it.comment })

        println(csv.write())
    }

}
