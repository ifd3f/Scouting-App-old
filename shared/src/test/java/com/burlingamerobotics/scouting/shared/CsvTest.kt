package com.burlingamerobotics.scouting.shared

import com.burlingamerobotics.scouting.shared.csv.CSVColumn
import com.burlingamerobotics.scouting.shared.csv.CSVSerializer
import org.junit.Test

data class boi(val name: String, val age: Int, val comment: String)

class CsvTest {

    @Test
    fun testCSV() {
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