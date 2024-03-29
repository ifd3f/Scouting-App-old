package com.burlingamerobotics.scouting.shared

import com.burlingamerobotics.scouting.shared.csv.CSVColumn
import com.burlingamerobotics.scouting.shared.csv.CSVSerializer
import com.burlingamerobotics.scouting.shared.csv.createSerializers
import org.junit.Assert
import org.junit.Test

data class Boi(val name: String, val age: Int, val comment: String) {
}

class UtilsTest {

    @Test
    fun testCSV() {
        val dudes = listOf(
                Boi("Meme McMeme", 30, "He's a funny, silly, man"),
                Boi("asdf", 5, "sol dfjd asdlf"),
                Boi("sajdflk", 413, "vdsf132,43141313,\"432\"")
        )

        val csv = CSVSerializer<Boi>(listOf(
                CSVColumn("name") { it.name },
                CSVColumn("age") { it.age.toString() },
                CSVColumn("comment") { it.comment }
        ))

        Assert.assertEquals(csv.makeCSV(dudes), """name,age,comment
Meme McMeme,30,"He's a funny, silly, man"
asdf,5,sol dfjd asdlf
sajdflk,413,"vdsf132,43141313,""432"""""")

        println(CSVSerializer(createSerializers<Boi>()).makeCSV(dudes))
    }


}
