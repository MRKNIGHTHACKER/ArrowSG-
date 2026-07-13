package com.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.databinding.ActivityMainBinding
import com.example.fragments.SplashFragment
import com.example.repository.GameRepository
import com.example.utilities.SoundManager

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    
    val repository by lazy { GameRepository(applicationContext) }
    val soundManager by lazy { SoundManager(applicationContext, repository) }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Enforce dark theme preference before layout inflation
        val isDark = repository.isDarkModeEnabled()
        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Navigate to initial Splash screen
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SplashFragment())
                .commit()
        }
    }

    fun navigateTo(fragment: Fragment, addToBackstack: Boolean = true) {
        val transaction = supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            .replace(R.id.fragment_container, fragment)
        if (addToBackstack) {
            transaction.addToBackStack(null)
        }
        transaction.commit()
    }

    override fun onResume() {
        super.onResume()
        soundManager.startMusic()
    }

    override fun onPause() {
        super.onPause()
        soundManager.stopMusic()
    }

    override fun onDestroy() {
        soundManager.release()
        super.onDestroy()
    }
}
