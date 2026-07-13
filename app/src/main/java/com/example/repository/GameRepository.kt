package com.example.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.models.Level
import org.json.JSONArray

class GameRepository(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("arrow_puzzle_prefs", Context.MODE_PRIVATE)
    private var levels: List<Level> = emptyList()

    init {
        loadLevelsFromAssets()
    }

    private fun loadLevelsFromAssets() {
        val loaded = mutableListOf<Level>()
        try {
            val jsonString = context.assets.open("levels.json").bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.getInt("id")
                val diff = obj.getString("difficulty")
                val size = obj.getInt("gridSize")
                val boardArray = obj.getJSONArray("board")
                
                val board = Array(size) { r ->
                    val rowArray = boardArray.getJSONArray(r)
                    Array(size) { c ->
                        rowArray.getString(c)
                    }
                }
                loaded.add(Level(id, diff, size, board))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        levels = loaded
    }

    fun getAllLevels(): List<Level> = levels

    fun getLevel(id: Int): Level? = levels.find { it.id == id }

    fun getUnlockedLevel(): Int = prefs.getInt("unlocked_level", 1)

    fun unlockNextLevel(currentLevelId: Int) {
        val currentUnlocked = getUnlockedLevel()
        if (currentLevelId >= currentUnlocked) {
            prefs.edit().putInt("unlocked_level", currentLevelId + 1).apply()
        }
    }

    fun saveLevelStats(levelId: Int, stars: Int, score: Int, completionTimeSeconds: Int) {
        val existingStars = prefs.getInt("level_stars_$levelId", 0)
        if (stars > existingStars) {
            prefs.edit().putInt("level_stars_$levelId", stars).apply()
        }
        val existingScore = prefs.getInt("level_score_$levelId", 0)
        if (score > existingScore) {
            prefs.edit().putInt("level_score_$levelId", score).apply()
        }
        val existingTime = prefs.getInt("level_time_$levelId", -1)
        if (existingTime == -1 || completionTimeSeconds < existingTime) {
            prefs.edit().putInt("level_time_$levelId", completionTimeSeconds).apply()
        }
        
        // Update cumulative stats
        incrementCompletedLevels()
        addTotalStars(stars - existingStars)
        addTotalScore(score - existingScore)
        addTimeSpent(completionTimeSeconds)
    }

    fun getLevelStars(levelId: Int): Int = prefs.getInt("level_stars_$levelId", 0)
    fun getLevelScore(levelId: Int): Int = prefs.getInt("level_score_$levelId", 0)
    fun getLevelTime(levelId: Int): Int = prefs.getInt("level_time_$levelId", -1)

    // Game stats
    fun incrementGamesPlayed() {
        prefs.edit().putInt("stats_total_games_played", prefs.getInt("stats_total_games_played", 0) + 1).apply()
    }

    private fun incrementCompletedLevels() {
        prefs.edit().putInt("stats_total_levels_completed", prefs.getInt("stats_total_levels_completed", 0) + 1).apply()
    }

    private fun addTotalStars(delta: Int) {
        if (delta > 0) {
            prefs.edit().putInt("stats_total_stars_earned", prefs.getInt("stats_total_stars_earned", 0) + delta).apply()
        }
    }

    private fun addTotalScore(delta: Int) {
        if (delta > 0) {
            prefs.edit().putInt("stats_total_score", prefs.getInt("stats_total_score", 0) + delta).apply()
        }
    }

    private fun addTimeSpent(seconds: Int) {
        prefs.edit().putInt("stats_total_time_spent", prefs.getInt("stats_total_time_spent", 0) + seconds).apply()
    }

    fun addHeartsLost(hearts: Int) {
        prefs.edit().putInt("stats_total_hearts_lost", prefs.getInt("stats_total_hearts_lost", 0) + hearts).apply()
    }

    fun getStats(): Map<String, Int> {
        return mapOf(
            "total_games_played" to prefs.getInt("stats_total_games_played", 0),
            "total_levels_completed" to prefs.getInt("stats_total_levels_completed", 0),
            "total_stars_earned" to prefs.getInt("stats_total_stars_earned", 0),
            "total_score" to prefs.getInt("stats_total_score", 0),
            "total_hearts_lost" to prefs.getInt("stats_total_hearts_lost", 0),
            "total_time_spent" to prefs.getInt("stats_total_time_spent", 0)
        )
    }

    fun isDarkModeEnabled(): Boolean = prefs.getBoolean("dark_mode", false)
    fun setDarkModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("dark_mode", enabled).apply()
    }

    fun isSoundEnabled(): Boolean = prefs.getBoolean("sound_enabled", true)
    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("sound_enabled", enabled).apply()
    }

    fun isMusicEnabled(): Boolean = prefs.getBoolean("music_enabled", true)
    fun setMusicEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("music_enabled", enabled).apply()
    }

    fun resetProgress() {
        prefs.edit().clear().apply()
    }
}
