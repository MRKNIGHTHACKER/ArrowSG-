package com.example.engine

import com.example.models.Level

class GameEngine(val level: Level) {
    val gridSize: Int = level.gridSize
    val board: Array<Array<String>> = Array(gridSize) { r ->
        Array(gridSize) { c -> level.initialBoard[r][c] }
    }
    
    var hearts: Int = 5
    var totalMoves: Int = 0
    var isGameOver: Boolean = false
    var isWon: Boolean = false
    
    fun tapCell(r: Int, c: Int): MoveResult {
        if (isGameOver || isWon) return MoveResult.InvalidGameEnded
        if (r < 0 || r >= gridSize || c < 0 || c >= gridSize) return MoveResult.EmptyCell
        val cell = board[r][c]
        if (cell.isEmpty()) return MoveResult.EmptyCell
        
        val clear = isPathClear(r, c, cell)
        if (clear) {
            board[r][c] = ""
            totalMoves++
            checkWinState()
            return MoveResult.Success(r, c, cell)
        } else {
            hearts--
            totalMoves++
            checkLoseState()
            return MoveResult.Blocked(r, c, cell)
        }
    }
    
    fun getHint(): Pair<Int, Int>? {
        if (isGameOver || isWon) return null
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                val dir = board[r][c]
                if (dir.isNotEmpty() && isPathClear(r, c, dir)) {
                    return Pair(r, c)
                }
            }
        }
        return null
    }
    
    fun isPathClear(r: Int, c: Int, dir: String): Boolean {
        when (dir) {
            "U" -> {
                for (i in 0 until r) {
                    if (board[i][c].isNotEmpty()) return false
                }
            }
            "D" -> {
                for (i in (r + 1) until gridSize) {
                    if (board[i][c].isNotEmpty()) return false
                }
            }
            "L" -> {
                for (j in 0 until c) {
                    if (board[r][j].isNotEmpty()) return false
                }
            }
            "R" -> {
                for (j in (c + 1) until gridSize) {
                    if (board[r][j].isNotEmpty()) return false
                }
            }
        }
        return true
    }
    
    private fun checkWinState() {
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                if (board[r][c].isNotEmpty()) return
            }
        }
        isWon = true
    }
    
    private fun checkLoseState() {
        if (hearts <= 0) {
            isGameOver = true
        }
    }
}

sealed class MoveResult {
    object EmptyCell : MoveResult()
    object InvalidGameEnded : MoveResult()
    data class Success(val row: Int, val col: Int, val direction: String) : MoveResult()
    data class Blocked(val row: Int, val col: Int, val direction: String) : MoveResult()
}
