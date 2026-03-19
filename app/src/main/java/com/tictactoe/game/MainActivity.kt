package com.tictactoe.game

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var game: TicTacToeGame
    private val cellViews = Array(3) { arrayOfNulls<View>(3) }
    private val cellSymbols = Array(3) { arrayOfNulls<ImageView>(3) }

    private lateinit var tvStatus: TextView
    private lateinit var tvPlayerScore: TextView
    private lateinit var tvComputerScore: TextView
    private lateinit var tvDrawScore: TextView
    private lateinit var btnRestart: Button
    private lateinit var gameGrid: GridLayout

    private var playerScore = 0
    private var computerScore = 0
    private var drawScore = 0

    private val handler = Handler(Looper.getMainLooper())
    private var isAnimating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        game = TicTacToeGame()
        initViews()
        setupBoard()
        updateStatus()
    }

    private fun initViews() {
        tvStatus = findViewById(R.id.tvStatus)
        tvPlayerScore = findViewById(R.id.tvPlayerScore)
        tvComputerScore = findViewById(R.id.tvComputerScore)
        tvDrawScore = findViewById(R.id.tvDrawScore)
        btnRestart = findViewById(R.id.btnRestart)
        gameGrid = findViewById(R.id.gameGrid)

        btnRestart.setOnClickListener { resetGame() }
    }

    private fun setupBoard() {
        val cellIds = arrayOf(
            arrayOf(R.id.cell00, R.id.cell01, R.id.cell02),
            arrayOf(R.id.cell10, R.id.cell11, R.id.cell12),
            arrayOf(R.id.cell20, R.id.cell21, R.id.cell22)
        )
        val symbolIds = arrayOf(
            arrayOf(R.id.symbol00, R.id.symbol01, R.id.symbol02),
            arrayOf(R.id.symbol10, R.id.symbol11, R.id.symbol12),
            arrayOf(R.id.symbol20, R.id.symbol21, R.id.symbol22)
        )

        for (row in 0..2) {
            for (col in 0..2) {
                val cell = findViewById<View>(cellIds[row][col])
                val symbol = findViewById<ImageView>(symbolIds[row][col])
                cellViews[row][col] = cell
                cellSymbols[row][col] = symbol

                val r = row
                val c = col
                cell.setOnClickListener { onCellClicked(r, c) }
            }
        }
    }

    private fun onCellClicked(row: Int, col: Int) {
        if (isAnimating) return
        if (game.state != TicTacToeGame.GameState.IN_PROGRESS) return
        if (game.currentPlayer != TicTacToeGame.Player.X) return

        if (game.makeMove(row, col)) {
            animateCellPlacement(row, col, TicTacToeGame.Player.X)
            updateStatus()
            checkGameEnd()

            if (game.state == TicTacToeGame.GameState.IN_PROGRESS) {
                isAnimating = true
                handler.postDelayed({ computerMove() }, 600)
            }
        }
    }

    private fun computerMove() {
        val move = game.getBestMove() ?: return
        game.makeMove(move.first, move.second)
        animateCellPlacement(move.first, move.second, TicTacToeGame.Player.O)
        isAnimating = false
        updateStatus()
        checkGameEnd()
    }

    private fun animateCellPlacement(row: Int, col: Int, player: TicTacToeGame.Player) {
        val symbol = cellSymbols[row][col] ?: return
        val cell = cellViews[row][col] ?: return

        val drawableRes = if (player == TicTacToeGame.Player.X) R.drawable.ic_x else R.drawable.ic_o
        symbol.setImageResource(drawableRes)
        symbol.visibility = View.VISIBLE

        // Pop-in animation
        symbol.scaleX = 0f
        symbol.scaleY = 0f
        symbol.alpha = 0f

        val scaleX = ObjectAnimator.ofFloat(symbol, View.SCALE_X, 0f, 1.2f, 1f)
        val scaleY = ObjectAnimator.ofFloat(symbol, View.SCALE_Y, 0f, 1.2f, 1f)
        val alpha = ObjectAnimator.ofFloat(symbol, View.ALPHA, 0f, 1f)

        scaleX.duration = 300
        scaleY.duration = 300
        alpha.duration = 200

        scaleX.interpolator = BounceInterpolator()
        scaleY.interpolator = BounceInterpolator()

        val set = AnimatorSet()
        set.playTogether(scaleX, scaleY, alpha)
        set.start()

        // Highlight cell briefly
        cell.isPressed = true
        handler.postDelayed({ cell.isPressed = false }, 150)
    }

    private fun checkGameEnd() {
        when (game.state) {
            TicTacToeGame.GameState.X_WINS -> {
                playerScore++
                updateScores()
                highlightWinningCells()
                animateWin()
            }
            TicTacToeGame.GameState.O_WINS -> {
                computerScore++
                updateScores()
                highlightWinningCells()
                animateLoss()
            }
            TicTacToeGame.GameState.DRAW -> {
                drawScore++
                updateScores()
                animateDraw()
            }
            TicTacToeGame.GameState.IN_PROGRESS -> {}
        }
    }

    private fun highlightWinningCells() {
        val winCells = game.winningCells
        val isPlayerWin = game.state == TicTacToeGame.GameState.X_WINS
        val highlightColor = ContextCompat.getColor(
            this,
            if (isPlayerWin) R.color.player_x_color else R.color.player_o_color
        )

        for ((row, col) in winCells) {
            val cell = cellViews[row][col] ?: continue
            val symbol = cellSymbols[row][col] ?: continue

            // Pulse animation on winning cells
            val pulse = ObjectAnimator.ofFloat(cell, View.ALPHA, 1f, 0.4f, 1f)
            pulse.duration = 600
            pulse.repeatCount = 2
            pulse.start()

            val scaleX = ObjectAnimator.ofFloat(symbol, View.SCALE_X, 1f, 1.3f, 1f)
            val scaleY = ObjectAnimator.ofFloat(symbol, View.SCALE_Y, 1f, 1.3f, 1f)
            scaleX.duration = 400
            scaleY.duration = 400
            scaleX.repeatCount = 2
            scaleY.repeatCount = 2
            AnimatorSet().apply {
                playTogether(scaleX, scaleY)
                start()
            }
        }
    }

    private fun animateWin() {
        val shake = ObjectAnimator.ofFloat(gameGrid, View.ROTATION, 0f, -3f, 3f, -2f, 2f, 0f)
        shake.duration = 500
        shake.start()
    }

    private fun animateLoss() {
        val pulse = ValueAnimator.ofFloat(1f, 0.95f, 1f)
        pulse.duration = 400
        pulse.addUpdateListener { anim ->
            val scale = anim.animatedValue as Float
            gameGrid.scaleX = scale
            gameGrid.scaleY = scale
        }
        pulse.start()
    }

    private fun animateDraw() {
        val rotate = ObjectAnimator.ofFloat(gameGrid, View.ROTATION, 0f, 5f, -5f, 0f)
        rotate.duration = 400
        rotate.start()
    }

    private fun updateStatus() {
        tvStatus.text = when (game.state) {
            TicTacToeGame.GameState.IN_PROGRESS -> {
                if (game.currentPlayer == TicTacToeGame.Player.X) {
                    getString(R.string.your_turn)
                } else {
                    getString(R.string.computer_thinking)
                }
            }
            TicTacToeGame.GameState.X_WINS -> getString(R.string.you_win)
            TicTacToeGame.GameState.O_WINS -> getString(R.string.computer_wins)
            TicTacToeGame.GameState.DRAW -> getString(R.string.draw)
        }

        // Animate status text change
        tvStatus.animate()
            .scaleX(1.1f)
            .scaleY(1.1f)
            .setDuration(100)
            .withEndAction {
                tvStatus.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start()
            }
            .start()
    }

    private fun updateScores() {
        tvPlayerScore.text = playerScore.toString()
        tvComputerScore.text = computerScore.toString()
        tvDrawScore.text = drawScore.toString()

        // Animate the updated score
        val scoreView = when (game.state) {
            TicTacToeGame.GameState.X_WINS -> tvPlayerScore
            TicTacToeGame.GameState.O_WINS -> tvComputerScore
            TicTacToeGame.GameState.DRAW -> tvDrawScore
            else -> null
        }
        scoreView?.let { view ->
            view.animate()
                .scaleX(1.4f)
                .scaleY(1.4f)
                .setDuration(200)
                .withEndAction {
                    view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(200)
                        .setInterpolator(BounceInterpolator())
                        .start()
                }
                .start()
        }
    }

    private fun resetGame() {
        game.reset()
        isAnimating = false
        handler.removeCallbacksAndMessages(null)

        for (row in 0..2) {
            for (col in 0..2) {
                cellSymbols[row][col]?.let { symbol ->
                    symbol.animate()
                        .alpha(0f)
                        .scaleX(0f)
                        .scaleY(0f)
                        .setDuration(200)
                        .withEndAction {
                            symbol.visibility = View.INVISIBLE
                            symbol.alpha = 1f
                            symbol.scaleX = 1f
                            symbol.scaleY = 1f
                        }
                        .start()
                }
            }
        }

        handler.postDelayed({
            gameGrid.alpha = 0f
            gameGrid.animate().alpha(1f).setDuration(300).start()
            updateStatus()
        }, 250)

        // Reset button animation
        btnRestart.animate()
            .rotationBy(360f)
            .setDuration(400)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
