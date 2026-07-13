package com.example.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.MainActivity
import com.example.databinding.FragmentMainMenuBinding

class MainMenuFragment : Fragment() {
    private var _binding: FragmentMainMenuBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val mainActivity = activity as? MainActivity
        
        binding.btnPlay.setOnClickListener {
            mainActivity?.soundManager?.playTap()
            val unlockedLevel = mainActivity?.repository?.getUnlockedLevel() ?: 1
            // Ensure we do not go beyond 120 (max levels)
            val levelToPlay = if (unlockedLevel > 120) 120 else unlockedLevel
            val gameplayFragment = GameplayFragment.newInstance(levelToPlay)
            mainActivity?.navigateTo(gameplayFragment)
        }
        
        binding.btnLevelSelect.setOnClickListener {
            mainActivity?.soundManager?.playTap()
            mainActivity?.navigateTo(LevelSelectFragment())
        }
        
        binding.btnDaily.setOnClickListener {
            mainActivity?.soundManager?.playTap()
            val gameplayFragment = GameplayFragment.newInstance(isDaily = true)
            mainActivity?.navigateTo(gameplayFragment)
        }
        
        binding.btnStats.setOnClickListener {
            mainActivity?.soundManager?.playTap()
            mainActivity?.navigateTo(StatisticsFragment())
        }
        
        binding.btnSettings.setOnClickListener {
            mainActivity?.soundManager?.playTap()
            mainActivity?.navigateTo(SettingsFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
