package com.example.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.MainActivity
import com.example.R
import com.example.databinding.FragmentSettingsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
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
        
        binding.switchDarkMode.isChecked = repository.isDarkModeEnabled()
        binding.switchSound.isChecked = repository.isSoundEnabled()
        binding.switchMusic.isChecked = repository.isMusicEnabled()
        
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            mainActivity.soundManager.playTap()
            repository.setDarkModeEnabled(isChecked)
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
        
        binding.switchSound.setOnCheckedChangeListener { _, isChecked ->
            repository.setSoundEnabled(isChecked)
            mainActivity.soundManager.playTap()
        }
        
        binding.switchMusic.setOnCheckedChangeListener { _, isChecked ->
            mainActivity.soundManager.playTap()
            repository.setMusicEnabled(isChecked)
            if (isChecked) {
                mainActivity.soundManager.startMusic()
            } else {
                mainActivity.soundManager.stopMusic()
            }
        }
        
        binding.btnResetProgress.setOnClickListener {
            mainActivity.soundManager.playTap()
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.settings_reset_confirm_title)
                .setMessage(R.string.settings_reset_confirm)
                .setPositiveButton(R.string.yes) { _, _ ->
                    repository.resetProgress()
                    repository.setDarkModeEnabled(false)
                    repository.setSoundEnabled(true)
                    repository.setMusicEnabled(true)
                    
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    mainActivity.soundManager.startMusic()
                    
                    binding.switchDarkMode.isChecked = false
                    binding.switchSound.isChecked = true
                    binding.switchMusic.isChecked = true
                    
                    Toast.makeText(requireContext(), "Progress Reset Successful!", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton(R.string.no) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
