package com.example.fragments

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.MainActivity
import com.example.R
import com.example.databinding.DialogGameOverBinding
import com.example.databinding.DialogVictoryBinding
import com.example.databinding.FragmentGameplayBinding
import com.example.databinding.ItemGridCellBinding
import com.example.viewmodel.GameState
import com.example.viewmodel.GameViewModel
import com.google.android.material.card.MaterialCardView

class GameplayFragment : Fragment() {
    private var _binding: FragmentGameplayBinding? = null
    private val binding get() = _binding!!

    private val viewModel by lazy {
        ViewModelProvider(this)[GameViewModel::class.java]
    }

    private var cellBindings: Array<Array<ItemGridCellBinding>>? = null
    private var activeDialog: android.app.Dialog? = null
    private var defaultOutlineColor: Int = Color.GRAY

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameplayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainActivity = activity as? MainActivity
        
        // Resolve theme outline color
        val typedValue = TypedValue()
        context?.theme?.resolveAttribute(com.google.android.material.R.attr.colorOutline, typedValue, true)
        defaultOutlineColor = typedValue.data

        // Handle navigation arguments
        val levelId = arguments?.getInt(ARG_LEVEL_ID, 1) ?: 1
        val isDaily = arguments?.getBoolean(ARG_IS_DAILY, false) ?: false

        if (isDaily) {
            viewModel.startDailyChallenge()
        } else {
            viewModel.startLevel(levelId)
        }

        // Top bar buttons
        binding.btnHome.setOnClickListener {
            mainActivity?.soundManager?.playTap()
            parentFragmentManager.popBackStack()
        }

        binding.btnPause.setOnClickListener {
            mainActivity?.soundManager?.playTap()
            viewModel.pauseGame()
        }

        binding.btnResume.setOnClickListener {
            mainActivity?.soundManager?.playTap()
            viewModel.resumeGame()
        }

        binding.btnPauseHome.setOnClickListener {
            mainActivity?.soundManager?.playTap()
            parentFragmentManager.popBackStack()
        }

        // Bottom actions
        binding.btnHint.setOnClickListener {
            mainActivity?.soundManager?.playTap()
            viewModel.requestHint()
        }

        binding.btnRestart.setOnClickListener {
            mainActivity?.soundManager?.playTap()
            viewModel.restartLevel()
        }

        // Observe level structure
        viewModel.currentLevel.observe(viewLifecycleOwner) { level ->
            if (level != null) {
                binding.tvLevelTitle.text = if (viewModel.isDailyChallenge) {
                    getString(R.string.gameplay_daily)
                } else {
                    getString(R.string.gameplay_level, level.id)
                }
                initBoardGrid(level.gridSize)
            }
        }

        // Observe moves
        viewModel.moves.observe(viewLifecycleOwner) { moves ->
            binding.tvMoves.text = moves.toString()
        }

        // Observe timer
        viewModel.timeElapsed.observe(viewLifecycleOwner) { elapsedSec ->
            val min = elapsedSec / 60
            val sec = elapsedSec % 60
            binding.tvTimer.text = String.format("%02d:%02d", min, sec)
        }

        // Observe game state changes
        viewModel.gameState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is GameState.Playing -> {
                    binding.pauseOverlay.visibility = View.GONE
                }
                is GameState.Paused -> {
                    binding.pauseOverlay.visibility = View.VISIBLE
                }
                is GameState.Won -> {
                    mainActivity?.soundManager?.playWin()
                    val heartsLeft = viewModel.hearts.value ?: 5
                    val timeSec = viewModel.timeElapsed.value ?: 0
                    val score = (heartsLeft * 200) + maxOf(0, 1000 - timeSec * 2)
                    showVictoryDialog(score, heartsLeft, timeSec)
                }
                is GameState.GameOver -> {
                    mainActivity?.soundManager?.playGameOver()
                    showGameOverDialog()
                }
            }
        }

        // Observe remaining hearts
        viewModel.hearts.observe(viewLifecycleOwner) { hearts ->
            updateHearts(hearts)
        }

        // Observe hint suggestions
        viewModel.hintCell.observe(viewLifecycleOwner) { cell ->
            highlightHintCell(cell)
        }

        // Observe game board updates
        viewModel.engine.observe(viewLifecycleOwner) {
            updateBoardGrid()
        }
    }

    private fun initBoardGrid(gridSize: Int) {
        binding.boardGrid.removeAllViews()
        binding.boardGrid.columnCount = gridSize
        binding.boardGrid.rowCount = gridSize

        cellBindings = Array(gridSize) { r ->
            Array(gridSize) { c ->
                val cellBinding = ItemGridCellBinding.inflate(layoutInflater, binding.boardGrid, false)
                val params = GridLayout.LayoutParams().apply {
                    width = 0
                    height = 0
                    columnSpec = GridLayout.spec(c, 1f)
                    rowSpec = GridLayout.spec(r, 1f)
                }
                cellBinding.root.layoutParams = params
                binding.boardGrid.addView(cellBinding.root)
                cellBinding
            }
        }
        updateBoardGrid()
    }

    private fun updateBoardGrid() {
        val bindings = cellBindings ?: return
        val engine = viewModel.engine.value ?: return
        val mainActivity = activity as? MainActivity

        for (r in 0 until engine.gridSize) {
            for (c in 0 until engine.gridSize) {
                val cellBinding = bindings[r][c]
                val direction = engine.board[r][c]
                val cellCard = cellBinding.cellCard
                val arrowImg = cellBinding.arrowImage

                cellCard.strokeColor = defaultOutlineColor
                cellCard.strokeWidth = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 1.5f, resources.displayMetrics
                ).toInt()

                if (direction.isNotEmpty()) {
                    arrowImg.visibility = View.VISIBLE
                    val resId = when (direction) {
                        "U" -> R.drawable.ic_arrow_up
                        "D" -> R.drawable.ic_arrow_down
                        "L" -> R.drawable.ic_arrow_left
                        "R" -> R.drawable.ic_arrow_right
                        else -> 0
                    }
                    arrowImg.setImageResource(resId)

                    cellCard.setOnClickListener {
                        viewModel.tapCell(r, c,
                            onSuccess = { row, col, dir ->
                                mainActivity?.soundManager?.playTap()
                                updateBoardGrid()
                            },
                            onBlocked = { row, col, dir ->
                                mainActivity?.soundManager?.playInvalid()
                                flashCellRed(cellCard)
                            }
                        )
                    }
                } else {
                    arrowImg.visibility = View.INVISIBLE
                    cellCard.setOnClickListener(null)
                }
            }
        }
    }

    private fun highlightHintCell(cell: Pair<Int, Int>?) {
        val bindings = cellBindings ?: return
        for (r in 0 until bindings.size) {
            for (c in 0 until bindings[r].size) {
                val cellCard = bindings[r][c].cellCard
                if (cell != null && cell.first == r && cell.second == c) {
                    cellCard.strokeColor = Color.parseColor("#FFC107") // Gold
                    cellCard.strokeWidth = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 3f, resources.displayMetrics
                    ).toInt()
                } else {
                    cellCard.strokeColor = defaultOutlineColor
                    cellCard.strokeWidth = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 1.5f, resources.displayMetrics
                    ).toInt()
                }
            }
        }
    }

    private fun flashCellRed(card: MaterialCardView) {
        val originalColor = card.strokeColor
        val redColor = Color.parseColor("#FF2B55")
        
        val animator = ObjectAnimator.ofArgb(
            card,
            "strokeColor",
            originalColor,
            redColor,
            originalColor
        )
        animator.duration = 400
        animator.setEvaluator(ArgbEvaluator())
        animator.start()
    }

    private fun updateHearts(hearts: Int) {
        binding.heart1.setImageResource(if (hearts >= 1) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline)
        binding.heart2.setImageResource(if (hearts >= 2) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline)
        binding.heart3.setImageResource(if (hearts >= 3) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline)
        binding.heart4.setImageResource(if (hearts >= 4) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline)
        binding.heart5.setImageResource(if (hearts >= 5) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline)
    }

    private fun showVictoryDialog(score: Int, remainingHearts: Int, timeSec: Int) {
        activeDialog?.dismiss()
        val dialogBinding = DialogVictoryBinding.inflate(layoutInflater)

        dialogBinding.tvVictoryScore.text = getString(R.string.victory_score, score)
        dialogBinding.tvVictoryHearts.text = getString(R.string.victory_hearts, remainingHearts)
        dialogBinding.tvVictoryTime.text = getString(R.string.victory_time, timeSec)

        val stars = when {
            remainingHearts >= 5 -> 3
            remainingHearts >= 3 -> 2
            else -> 1
        }
        dialogBinding.dialogStar1.setImageResource(if (stars >= 1) R.drawable.ic_star_filled else R.drawable.ic_star_outline)
        dialogBinding.dialogStar2.setImageResource(if (stars >= 2) R.drawable.ic_star_filled else R.drawable.ic_star_outline)
        dialogBinding.dialogStar3.setImageResource(if (stars >= 3) R.drawable.ic_star_filled else R.drawable.ic_star_outline)

        val dialog = android.app.Dialog(requireContext())
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)

        if (viewModel.isDailyChallenge) {
            dialogBinding.btnNextLevel.visibility = View.GONE
        } else {
            dialogBinding.btnNextLevel.visibility = View.VISIBLE
            dialogBinding.btnNextLevel.setOnClickListener {
                dialog.dismiss()
                val currentLvlId = viewModel.currentLevel.value?.id ?: 1
                if (currentLvlId < 120) {
                    viewModel.startLevel(currentLvlId + 1)
                } else {
                    (activity as? MainActivity)?.navigateTo(MainMenuFragment(), addToBackstack = false)
                }
            }
        }

        dialogBinding.btnVictoryHome.setOnClickListener {
            dialog.dismiss()
            (activity as? MainActivity)?.navigateTo(MainMenuFragment(), addToBackstack = false)
        }

        dialog.show()
        activeDialog = dialog
    }

    private fun showGameOverDialog() {
        activeDialog?.dismiss()
        val dialogBinding = DialogGameOverBinding.inflate(layoutInflater)

        val dialog = android.app.Dialog(requireContext())
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)

        dialogBinding.btnGameoverRestart.setOnClickListener {
            dialog.dismiss()
            viewModel.restartLevel()
        }

        dialogBinding.btnGameoverHome.setOnClickListener {
            dialog.dismiss()
            (activity as? MainActivity)?.navigateTo(MainMenuFragment(), addToBackstack = false)
        }

        dialog.show()
        activeDialog = dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activeDialog?.dismiss()
        activeDialog = null
        _binding = null
    }

    companion object {
        private const val ARG_LEVEL_ID = "level_id"
        private const val ARG_IS_DAILY = "is_daily"

        fun newInstance(levelId: Int = 1, isDaily: Boolean = false): GameplayFragment {
            val fragment = GameplayFragment()
            val args = Bundle().apply {
                putInt(ARG_LEVEL_ID, levelId)
                putBoolean(ARG_IS_DAILY, isDaily)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
