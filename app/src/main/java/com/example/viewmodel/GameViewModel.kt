package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.engine.GameEngine
import com.example.engine.MoveResult
import com.example.models.Level
import com.example.repository.GameRepository
import com.example.utilities.LevelGenerator
import kotlinx.coroutines.*
import java.util.Calendar

class GameViewModel(application: Application) : AndroidViewModel(application) {
    val repository = GameRepository(application)
    
    private val _currentLevel = MutableLiveData<Level?>()
    val currentLevel: LiveData<Level?> = _currentLevel
    
    private val _engine = MutableLiveData<GameEngine?>()
    val engine: LiveData<GameEngine?> = _engine
    
    private val _hearts = MutableLiveData<Int>()
    val hearts: LiveData<Int> = _hearts
    
    private val _moves = MutableLiveData<Int>()
    val moves: LiveData<Int> = _moves
    
    private val _timeElapsed = MutableLiveData<Int>()
    val timeElapsed: LiveData<Int> = _timeElapsed
    
    private val _gameState = MutableLiveData<GameState>()
    val gameState: LiveData<GameState> = _gameState
    
    private val _hintCell = MutableLiveData<Pair<Int, Int>?>()
    val hintCell: LiveData<Pair<Int, Int>?> = _hintCell
    
    private var timerJob: Job? = null
    var isDailyChallenge: Boolean = false
    var dailyLevelId: Int = 9999
    
    fun startLevel(levelId: Int) {
        val level = repository.getLevel(levelId)
        if (level != null) {
            isDailyChallenge = false
            initGame(level)
        }
    }
    
    fun startDailyChallenge() {
        isDailyChallenge = true
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val seed = (year * 10000 + month * 100 + day).toLong()
        
        val dailyLevel = LevelGenerator.generateDailyLevel(dailyLevelId, seed)
        initGame(dailyLevel)
    }
    
    private fun initGame(level: Level) {
        stopTimer()
        val gameEngine = GameEngine(level)
        _currentLevel.value = level
        _engine.value = gameEngine
        _hearts.value = gameEngine.hearts
        _moves.value = gameEngine.totalMoves
        _timeElapsed.value = 0
        _hintCell.value = null
        _gameState.value = GameState.Playing
        
        repository.incrementGamesPlayed()
        startTimer()
    }
    
    fun tapCell(r: Int, c: Int, onSuccess: (r: Int, c: Int, dir: String) -> Unit, onBlocked: (r: Int, c: Int, dir: String) -> Unit) {
        val gameEngine = _engine.value ?: return
        if (_gameState.value != GameState.Playing) return
        
        _hintCell.value = null
        
        val result = gameEngine.tapCell(r, c)
        _hearts.value = gameEngine.hearts
        _moves.value = gameEngine.totalMoves
        
        when (result) {
            is MoveResult.Success -> {
                onSuccess(result.row, result.col, result.direction)
                if (gameEngine.isWon) {
                    handleWin()
                }
            }
            is MoveResult.Blocked -> {
                onBlocked(result.row, result.col, result.direction)
                repository.addHeartsLost(1)
                if (gameEngine.isGameOver) {
                    handleGameOver()
                }
            }
            else -> {}
        }
    }
    
    fun requestHint() {
        val gameEngine = _engine.value ?: return
        if (_gameState.value != GameState.Playing) return
        val hint = gameEngine.getHint()
        if (hint != null) {
            _hintCell.value = hint
        }
    }
    
    fun clearHint() {
        _hintCell.value = null
    }
    
    fun pauseGame() {
        if (_gameState.value == GameState.Playing) {
            _gameState.value = GameState.Paused
            stopTimer()
        }
    }
    
    fun resumeGame() {
        if (_gameState.value == GameState.Paused) {
            _gameState.value = GameState.Playing
            startTimer()
        }
    }
    
    fun restartLevel() {
        val lvl = _currentLevel.value ?: return
        if (isDailyChallenge) {
            startDailyChallenge()
        } else {
            startLevel(lvl.id)
        }
    }
    
    private fun handleWin() {
        _gameState.value = GameState.Won
        stopTimer()
        
        val lvl = _currentLevel.value ?: return
        val h = _hearts.value ?: 5
        val timeSec = _timeElapsed.value ?: 0
        
        val stars = when {
            h >= 5 -> 3
            h >= 3 -> 2
            else -> 1
        }
        
        val score = (h * 200) + maxOf(0, 1000 - timeSec * 2)
        
        if (!isDailyChallenge) {
            repository.saveLevelStats(lvl.id, stars, score, timeSec)
            repository.unlockNextLevel(lvl.id)
        }
    }
    
    private fun handleGameOver() {
        _gameState.value = GameState.GameOver
        stopTimer()
    }
    
    private fun startTimer() {
        timerJob = viewModelScope.launch(Dispatchers.Default) {
            while (isActive) {
                delay(1000)
                withContext(Dispatchers.Main) {
                    _timeElapsed.value = (_timeElapsed.value ?: 0) + 1
                }
            }
        }
    }
    
    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }
    
    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}

sealed class GameState {
    object Playing : GameState()
    object Paused : GameState()
    object Won : GameState()
    object GameOver : GameState()
}
