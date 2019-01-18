package hashcode

import java.lang.Exception
import java.lang.Math.ceil
import java.lang.Math.min


data class Ingredient(val tCnt: Int, val mCnt: Int, val sCnt: Int)
data class Point(val x: Int, val y: Int)
data class RectangleSelected(val rectangle: Rectangle, val tomatoCnt: Int, val mushroomCnt: Int, val sliceSize: Int)
data class Rectangle(private val str: String) {
    val down: Int
    val right: Int

    init {
        val parts = str.split(",")
        down = parts[0].toInt()
        right = parts[1].toInt()
    }
}

class Slicer {
    companion object {

        fun slice(pizza: Pizza, src: Point): ArrayList<PizzaSliccerSearchNode> {
            val (config, plate) = pizza
            val (rows, column, minIngredient, maxCellsPerSlice) = config
            val (tCnt, mCnt, sCnt) = countIngredient(plate)
            val minCellsToSolve = ceil(column * rows / maxCellsPerSlice.toDouble())
            val minTaping = min(tCnt, mCnt)
            if (minTaping == 0) return ArrayList<PizzaSliccerSearchNode>()
            minCellsToSolve + minTaping + sCnt
            val possibleRectangles = generatePossibleRectangle(minIngredient, maxCellsPerSlice, rows, column)
            println("${src.x},${src.y}")
            return solve(pizza, src.x, src.y, possibleRectangles, config)
        }


        private fun solve(
            pizza: Pizza,
            i: Int,
            j: Int,
            possibleRectangles: List<Rectangle>,
            config: PizzaConfig
        ): ArrayList<PizzaSliccerSearchNode> {
            val res = getAllValidSlicesFromCurrentPoint(pizza, i, j, possibleRectangles, config)
            val results = ArrayList<PizzaSliccerSearchNode>()
            res.forEach {
                val newPizza = pizza.copy()
                newPizza.markSlice(it.rectangle, i, j)
                val newChild = PizzaSliccerSearchNode(newPizza, getNextPoint(pizza, i, j, it.rectangle))
                newChild.setG(1/(it.sliceSize.toDouble()))
                results.add(newChild)
            }
            return results
        }

        private fun getNextPoint(pizza: Pizza, i: Int, j: Int, rectangle: Rectangle): Point {
            if (j + rectangle.right + 1 < pizza.config.column) return Point(i, j + rectangle.right + 1)
            pizza.plate.forEachIndexed { ind, row ->
                if (ind > i) {
                    row.forEachIndexed { rowInd, value ->
                        if (value != PARTS.SELECTED) return Point(ind, rowInd)
                    }
                }

            }
            return Point(pizza.config.rows - 1, pizza.config.column - 1)

        }



        private fun getAllValidSlicesContainingMinTaping(
            pizza: Pizza,
            i: Int,
            j: Int,
            minIngredient: Int,
            validSlicesNotContainingSelected: ArrayList<Rectangle>
        ): ArrayList<RectangleSelected> {
            val result = ArrayList<RectangleSelected>()
            validSlicesNotContainingSelected.forEach { rec ->
                var tCnt = 0
                var mCnt = 0
                val coordinatesInsideRectangle = getCoordinatesInsideRectangle(i, j, rec.down, rec.right)

                coordinatesInsideRectangle.forEach {

                    when (pizza.plate[it.x][it.y]) {
                        PARTS.TOMATO -> tCnt++
                        PARTS.MUSHROOM -> mCnt++
                        PARTS.SELECTED -> throw Exception("all selected should be deleted before this step")
                    }


                }
                if (tCnt >= minIngredient && mCnt >= minIngredient) {
                    result.add(RectangleSelected(rec, tCnt, mCnt, tCnt + mCnt))
                }
            }
            return result
        }

        internal fun getCoordinatesInsideRectangle(i: Int, j: Int, down: Int, right: Int): ArrayList<Point> {
            val result = ArrayList<Point>()
            for (indexDown in 0..down)
                for (indexRight in 0..right)
                    result.add(Point(indexDown + i, indexRight + j))
            return result
        }


        private fun getAllValidSlicesNotContainingSelected(
            pizza: Pizza,
            i: Int,
            j: Int,
            validSlicesBySize: ArrayList<Rectangle>
        ): ArrayList<Rectangle> {
            val result = ArrayList<Rectangle>()
            validSlicesBySize.forEach { rec ->
                var insert = true
                for (iInd in 0..rec.down)
                    for (jInd in 0..rec.right)
                        if (pizza.plate[i + iInd][j + jInd] == PARTS.SELECTED) {
                            insert = false
                            break
                        }

                if (insert) result.add(rec)

            }
            return result

        }

        private fun generatePossibleRectangle(
            minIngredient: Int,
            maxCellsPerSlice: Int,
            rows: Int,
            column: Int
        ): List<Rectangle> {
            val result = HashSet<String>()
            for (i in minIngredient..maxCellsPerSlice)
                for (j in 1..i) {
                    if (i * j > maxCellsPerSlice) break
                    val rectangle = "${i - 1},${j - 1}"
                    result.add(rectangle)
                    result.add(rectangle.reversed())
                }
            return result.map { it -> Rectangle(it) }
                .filter { it.down <= rows && it.right <= column }
        }


        private fun getAllValidSlicesByPizzaPlateSize(
            i: Int,
            j: Int,
            possibleRectangles: List<Rectangle>,
            config: PizzaConfig
        ): ArrayList<Rectangle> {
            val result = ArrayList<Rectangle>()

            possibleRectangles.forEach {
                if (i + it.down < config.rows && j + it.right < config.column)
                    result.add(it)

            }

            return result
        }


        private fun getAllValidSlicesFromCurrentPoint(
            pizza: Pizza,
            i: Int,
            j: Int,
            possibleRectangles: List<Rectangle>,
            config: PizzaConfig
        ): ArrayList<RectangleSelected> {
            val validSlicesBySize = getAllValidSlicesByPizzaPlateSize(i, j, possibleRectangles, config)
            val validSlicesNotContainingSelected =
                getAllValidSlicesNotContainingSelected(pizza, i, j, validSlicesBySize)
            return getAllValidSlicesContainingMinTaping(
                pizza,
                i,
                j,
                config.minIngredient,
                validSlicesNotContainingSelected
            )
        }


        private fun countIngredient(pizza: ArrayList<Array<PARTS>>): Ingredient {
            var tCnt = 0
            var mCnt = 0
            var sCnt = 0
            pizza.forEach { tops ->
                tops.forEach { top ->
                    when (top) {
                        PARTS.TOMATO -> tCnt++
                        PARTS.MUSHROOM -> mCnt++
                        PARTS.SELECTED -> sCnt++
                    }
                }
            }
            return Ingredient(tCnt, mCnt, sCnt)

        }
    }
}