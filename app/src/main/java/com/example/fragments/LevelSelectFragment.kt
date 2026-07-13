package com.example.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.MainActivity
import com.example.adapters.LevelSelectAdapter
import com.example.databinding.FragmentLevelSelectBinding

class LevelSelectFragment : Fragment() {
    private var _binding: FragmentLevelSelectBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLevelSelectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val mainActivity = activity as? MainActivity
        
        binding.btnBack.setOnClickListener {
            mainActivity?.soundManager?.playTap()
            parentFragmentManager.popBackStack()
        }
        
        val levels = mainActivity?.repository?.getAllLevels() ?: emptyList()
        val repository = mainActivity?.repository
        
        if (repository != null) {
            val adapter = LevelSelectAdapter(levels, repository) { level ->
                mainActivity.soundManager.playTap()
                val gameplayFragment = GameplayFragment.newInstance(level.id)
                mainActivity.navigateTo(gameplayFragment)
            }
            binding.rvLevels.layoutManager = GridLayoutManager(requireContext(), 3)
            binding.rvLevels.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
