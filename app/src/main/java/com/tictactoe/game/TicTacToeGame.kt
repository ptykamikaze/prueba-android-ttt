package com.tictactoe.game

/**
 * Core Tic Tac Toe game logic and AI.
 */
class TicTacToeGame {

    enum class Player { X, O, NONE }

    enum class GameState {
        IN_PROGRESS, X_WINS, O_WINS, DRAW
    }

    val board = Array(3) { Array(3) { Player.NONE } }
    var currentPlayer = Player.X
        private set

    private var _state = GameState.IN_PROGRESS
    val state: GameState get() = _state

    var winningCells: List<Pair<Int, Int>> = emptyList()
        private set

    /** Makes a move at the given position. Returns true if the move was valid. */
    fun makeMove(row: Int, col: Int): Boolean {
        if (_state != GameState.IN_PROGRESS) return false
        if (board[row][col] != Player.NONE) return false

        board[row][col] = currentPlayer
        checkGameState()

        if (_state == GameState.IN_PROGRESS) {
            currentPlayer = if (currentPlayer == Player.X) Player.O else Player.X
        }
        return true
    }

    private fun checkGameState() {
        val winner = checkWinner()
        _state = when {
            winner != null -> {
                if (winner == Player.X) GameState.X_WINS else GameState.O_WINS
            }
            isBoardFull() -> GameState.DRAW
            else -> GameState.IN_PROGRESS
        }
    }

    private fun checkWinner(): Player? {
        // Check rows
        for (row in 0..2) {
            if (board[row][0] != Player.NONE &&
                board[row][0] == board[row][1] &&
                board[row][1] == board[row][2]
            ) {
                winningCells = listOf(Pair(row, 0), Pair(row, 1), Pair(row, 2))
                return board[row][0]
            }
        }
        // Check columns
        for (col in 0..2) {
            if (board[0][col] != Player.NONE &&
                board[0][col] == board[1][col] &&
                board[1][col] == board[2][col]
            ) {
                winningCells = listOf(Pair(0, col), Pair(1, col), Pair(2, col))
                return board[0][col]
            }
        }
        // Check diagonals
        if (board[0][0] != Player.NONE &&
            board[0][0] == board[1][1] &&
            board[1][1] == board[2][2]
        ) {
            winningCells = listOf(Pair(0, 0), Pair(1, 1), Pair(2, 2))
            return board[0][0]
        }
        if (board[0][2] != Player.NONE &&
            board[0][2] == board[1][1] &&
            board[1][1] == board[2][0]
        ) {
            winningCells = listOf(Pair(0, 2), Pair(1, 1), Pair(2, 0))
            return board[0][2]
        }
        return null
    }

    private fun isBoardFull(): Boolean {
        return board.all { row -> row.all { it != Player.NONE } }
    }

    fun reset() {
        for (row in 0..2) for (col in 0..2) board[row][col] = Player.NONE
        currentPlayer = Player.X
        _state = GameState.IN_PROGRESS
        winningCells = emptyList()
    }

    /** Returns the best move for the computer (O player) using minimax with depth scoring. */
    fun getBestMove(): Pair<Int, Int>? {
        if (_state != GameState.IN_PROGRESS) return null

        var bestScore = Int.MIN_VALUE
        var bestMove: Pair<Int, Int>? = null

        for (row in 0..2) {
            for (col in 0..2) {
                if (board[row][col] == Player.NONE) {
                    board[row][col] = Player.O
                    val score = minimax(board, 0, false)
                    board[row][col] = Player.NONE
                    if (score > bestScore) {
                        bestScore = score
                        bestMove = Pair(row, col)
                    }
                }
            }
        }
        return bestMove
    }

    private fun minimax(
        board: Array<Array<Player>>,
        depth: Int,
        isMaximizing: Boolean
    ): Int {
        val result = evaluateBoard(board, depth)
        if (result != null) return result
        if (board.all { row -> row.all { it != Player.NONE } }) return 0

        return if (isMaximizing) {
            var best = Int.MIN_VALUE
            for (row in 0..2) {
                for (col in 0..2) {
                    if (board[row][col] == Player.NONE) {
                        board[row][col] = Player.O
                        best = maxOf(best, minimax(board, depth + 1, false))
                        board[row][col] = Player.NONE
                    }
                }
            }
            best
        } else {
            var best = Int.MAX_VALUE
            for (row in 0..2) {
                for (col in 0..2) {
                    if (board[row][col] == Player.NONE) {
                        board[row][col] = Player.X
                        best = minOf(best, minimax(board, depth + 1, true))
                        board[row][col] = Player.NONE
                    }
                }
            }
            best
        }
    }

    private fun evaluateBoard(board: Array<Array<Player>>, depth: Int): Int? {
        val score = 10 - depth
        // Check rows
        for (row in 0..2) {
            if (board[row][0] != Player.NONE &&
                board[row][0] == board[row][1] &&
                board[row][1] == board[row][2]
            ) {
                return if (board[row][0] == Player.O) score else -score
            }
        }
        // Check columns
        for (col in 0..2) {
            if (board[0][col] != Player.NONE &&
                board[0][col] == board[1][col] &&
                board[1][col] == board[2][col]
            ) {
                return if (board[0][col] == Player.O) score else -score
            }
        }
        // Check diagonals
        if (board[0][0] != Player.NONE &&
            board[0][0] == board[1][1] &&
            board[1][1] == board[2][2]
        ) {
            return if (board[0][0] == Player.O) score else -score
        }
        if (board[0][2] != Player.NONE &&
            board[0][2] == board[1][1] &&
            board[1][1] == board[2][0]
        ) {
            return if (board[0][2] == Player.O) score else -score
        }
        return null
    }
}
