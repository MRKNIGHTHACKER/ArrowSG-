package com.example.utilities

import com.example.models.Level
import java.util.Random

object LevelGenerator {
    
    fun generateDailyLevel(levelId: Int, seed: Long): Level {
        val random = Random(seed)
        val gridSize = 6 // Standard 6x6 for daily challenges
        val density = 0.50
        val maxAttempts = 1000
        
        for (attempt in 0 until maxAttempts) {
            val board = Array(gridSize) { Array(gridSize) { "" } }
            val arrowCount = (gridSize * gridSize * density).toInt()
            
            val allCells = mutableListOf<Pair<Int, Int>>()
            for (r in 0 until gridSize) {
                for (c in 0 until gridSize) {
                    allCells.add(Pair(r, c))
                }
            }
            
            allCells.shuffle(random)
            val chosenCells = allCells.take(arrowCount)
            val directions = listOf("U", "D", "L", "R")
            
            for (cell in chosenCells) {
                board[cell.first][cell.second] = directions[random.nextInt(directions.size)]
            }
            
            if (isSolvable(gridSize, board)) {
                return Level(
                    id = levelId,
                    difficulty = "Daily",
                    gridSize = gridSize,
                    initialBoard = board
                )
            }
        }
        
        // Fallback safety board
        val fallbackBoard = Array(5) { Array(5) { "" } }
        fallbackBoard[2][2] = "R"
        return Level(levelId, "Daily", 5, fallbackBoard)
    }
    
    private fun isSolvable(gridSize: Int, board: Array<Array<String>>): Boolean {
        val temp = Array(gridSize) { r ->
            Array(gridSize) { c -> board[r][c] }
        }
        
        fun pathClear(r: Int, c: Int, dir: String): Boolean {
            when (dir) {
                "U" -> {
                    for (i in 0 until r) {
                        if (temp[i][c].isNotEmpty()) return false
                    }
                }
                "D" -> {
                    for (i in (r + 1) until gridSize) {
                        if (temp[i][c].isNotEmpty()) return false
                    }
                }
                "L" -> {
                    for (j in 0 until c) {
                        if (temp[r][j].isNotEmpty()) return false
                    }
                }
                "R" -> {
                    for (j in (c + 1) until gridSize) {
                        if (temp[r][j].isNotEmpty()) return false
                    }
                }
            }
            return true
        }
        
        while (true) {
            var moved = false
            outer@ for (r in 0 until gridSize) {
                for (c in 0 until gridSize) {
                    val valDir = temp[r][c]
                    if (valDir.isNotEmpty() && pathClear(r, c, valDir)) {
                        temp[r][c] = ""
                        moved = true
                        break@outer
                    }
                }
            }
            if (!moved) break
        }
        
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                if (temp[r][c].isNotEmpty()) return false
            }
        }
        return true
    }
}

fun <T> MutableList<T>.shuffle(random: Random) {
    for (i in size - 1 downTo 1) {
        val j = random.nextInt(i + 1)
        val temp = this[i]
        this[i] = this[j]
        this[j] = temp
    }
}
