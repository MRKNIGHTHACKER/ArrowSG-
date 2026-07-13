package com.example.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.MainActivity
import com.example.R
import com.example.databinding.FragmentStatisticsBinding

class StatisticsFragment : Fragment() {
    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val mainActivity = activity as? MainActivity
        val repository = mainActivity?.repository ?: return
        
        binding.btnBack.setOnClickListener {
            mainActivity.soundManager.playTap()
            parentFragmentManager.popBackStack()
        }
        
        val stats = repository.getStats()
        val gamesPlayed = stats["total_games_played"] ?: 0
        val levelsCompleted = stats["total_levels_completed"] ?: 0
        val totalStars = stats["total_stars_earned"] ?: 0
        val totalScore = stats["total_score"] ?: 0
        val heartsLost = stats["total_hearts_lost"] ?: 0
        val timeSpentSec = stats["total_time_spent"] ?: 0
        
        val minutes = timeSpentSec / 60
        val seconds = timeSpentSec % 60
        
        binding.tvGamesPlayed.text = getString(R.string.stats_games_played, gamesPlayed)
        binding.tvLevelsCompleted.text = getString(R.string.stats_levels_completed, levelsCompleted)
        binding.tvStarsEarned.text = getString(R.string.stats_stars_earned, totalStars)
        binding.tvTotalScore.text = getString(R.string.stats_score, totalScore)
        binding.tvHeartsLost.text = getString(R.string.stats_hearts_lost, heartsLost)
        binding.tvTimeSpent.text = getString(R.string.stats_time_spent, minutes, seconds)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
