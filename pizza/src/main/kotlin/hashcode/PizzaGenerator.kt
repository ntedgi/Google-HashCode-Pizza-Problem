package hashcode

import java.io.File


enum class PARTS { TOMATO, MUSHROOM, SELECTED }


data class PizzaConfig (val rows:Int,val colums:Int,val minIngredient:Int,val maxCellsPerSlice:Int)

private data class PizzaFile(val config: String, val lines: ArrayList<String>)

data class Pizza(val config: PizzaConfig ,val plate: ArrayList<Array<PARTS>>  )

class PizzaGenerator {

    companion object {
        fun create(input: String): Pizza {
            val pizzaFile = readPizzaFile(input)
            return Pizza(createPizzaConfig(pizzaFile.config) , buildPizzaFromFile(pizzaFile))
        }

        private fun createPizzaConfig(config: String): PizzaConfig {
            val parts  = config.split(" ").map { it.toInt() }
            if (parts.size!=4) throw IllegalArgumentException("config most be of size 4")
            return PizzaConfig(parts[0],parts[1],parts[2],parts[3])
        }


        private fun buildPizzaFromFile(pizzaFile: PizzaFile): ArrayList<Array<PARTS>> {
            val pizzaPlate = ArrayList<Array<PARTS>>()
            pizzaFile.lines.forEach { line ->
                pizzaPlate.add(convertLineToToppins(line))
            }
            return pizzaPlate
        }

        private fun convertLineToToppins(line: String): Array<PARTS> =
            line.map { it -> convertCharToPizzaPart(it) }.toTypedArray()

        private fun convertCharToPizzaPart(c: Char): PARTS {
            return when (c) {
                'T' -> PARTS.TOMATO
                'M' -> PARTS.MUSHROOM
                else -> {
                    throw IllegalArgumentException("FILE SHOULD CONATAINS M OR T")
                }
            }
        }


        private fun readPizzaFile(input: String): PizzaFile {
            File(input).bufferedReader().use { reader ->
                val config = reader.readLine() ?: ""
                val pizza = arrayListOf<String>()
                while (true) {
                    val line = reader.readLine() ?: break
                    pizza.add(line)
                }
                return PizzaFile(config = config, lines = pizza)
            }

        }

    }
}