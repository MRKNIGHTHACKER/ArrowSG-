package com.example.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.R
import com.example.databinding.ItemLevelCardBinding
import com.example.models.Level
import com.example.repository.GameRepository

class LevelSelectAdapter(
    private val levels: List<Level>,
    private val repository: GameRepository,
    private val onLevelClick: (Level) -> Unit
) : RecyclerView.Adapter<LevelSelectAdapter.LevelViewHolder>() {

    private val unlockedLevel = repository.getUnlockedLevel()

    inner class LevelViewHolder(private val binding: ItemLevelCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(level: Level) {
            binding.tvLevelNum.text = level.id.toString()
            binding.tvDifficulty.text = level.difficulty

            val isUnlocked = level.id <= unlockedLevel
            if (isUnlocked) {
                binding.lockOverlay.visibility = View.GONE
                binding.cardRoot.isClickable = true
                binding.cardRoot.isFocusable = true
                binding.cardRoot.setOnClickListener { onLevelClick(level) }
                
                val stars = repository.getLevelStars(level.id)
                binding.imgStar1.setImageResource(
                    if (stars >= 1) R.drawable.ic_star_filled else R.drawable.ic_star_outline
                )
                binding.imgStar2.setImageResource(
                    if (stars >= 2) R.drawable.ic_star_filled else R.drawable.ic_star_outline
                )
                binding.imgStar3.setImageResource(
                    if (stars >= 3) R.drawable.ic_star_filled else R.drawable.ic_star_outline
                )
                binding.starContainer.visibility = View.VISIBLE
            } else {
                binding.lockOverlay.visibility = View.VISIBLE
                binding.starContainer.visibility = View.GONE
                binding.cardRoot.isClickable = false
                binding.cardRoot.isFocusable = false
                binding.cardRoot.setOnClickListener(null)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LevelViewHolder {
        val binding = ItemLevelCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LevelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LevelViewHolder, position: Int) {
        holder.bind(levels[position])
    }

    override fun getItemCount(): Int = levels.size
}
