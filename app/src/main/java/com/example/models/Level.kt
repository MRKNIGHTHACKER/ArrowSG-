package com.example.models

data class Level(
    val id: Int,
    val difficulty: String,
    val gridSize: Int,
    val initialBoard: Array<Array<String>> // "U", "D", "L", "R", or ""
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Level) return false
        if (id != other.id) return false
        if (difficulty != other.difficulty) return false
        if (gridSize != other.gridSize) return false
        if (!initialBoard.contentDeepEquals(other.initialBoard)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + difficulty.hashCode()
        result = 31 * result + gridSize
        result = 31 * result + initialBoard.contentDeepHashCode()
        return result
    }
}
