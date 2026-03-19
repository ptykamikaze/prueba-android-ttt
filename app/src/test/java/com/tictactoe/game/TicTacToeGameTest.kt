package com.tictactoe.game

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

class TicTacToeGameTest {

    private lateinit var game: TicTacToeGame

    @Before
    fun setUp() {
        game = TicTacToeGame()
    }

    @Test
    fun initialState_boardIsEmpty() {
        for (row in 0..2) {
            for (col in 0..2) {
                assertEquals(TicTacToeGame.Player.NONE, game.board[row][col])
            }
        }
    }

    @Test
    fun initialState_isInProgress() {
        assertEquals(TicTacToeGame.GameState.IN_PROGRESS, game.state)
    }

    @Test
    fun initialState_playerXGoesFirst() {
        assertEquals(TicTacToeGame.Player.X, game.currentPlayer)
    }

    @Test
    fun makeMove_validMove_returnsTrue() {
        assertTrue(game.makeMove(0, 0))
    }

    @Test
    fun makeMove_alreadyOccupied_returnsFalse() {
        game.makeMove(0, 0)
        assertFalse(game.makeMove(0, 0))
    }

    @Test
    fun makeMove_alternatesPlayers() {
        game.makeMove(0, 0) // X
        assertEquals(TicTacToeGame.Player.O, game.currentPlayer)
        game.makeMove(1, 1) // O
        assertEquals(TicTacToeGame.Player.X, game.currentPlayer)
    }

    @Test
    fun makeMove_xWinsOnRow() {
        game.makeMove(0, 0) // X
        game.makeMove(1, 0) // O
        game.makeMove(0, 1) // X
        game.makeMove(1, 1) // O
        game.makeMove(0, 2) // X wins row 0
        assertEquals(TicTacToeGame.GameState.X_WINS, game.state)
    }

    @Test
    fun makeMove_oWinsOnColumn() {
        game.makeMove(0, 0) // X
        game.makeMove(0, 2) // O
        game.makeMove(1, 0) // X
        game.makeMove(1, 2) // O
        game.makeMove(2, 1) // X (not in column 2)
        game.makeMove(2, 2) // O wins column 2
        assertEquals(TicTacToeGame.GameState.O_WINS, game.state)
    }

    @Test
    fun makeMove_xWinsOnDiagonal() {
        game.makeMove(0, 0) // X
        game.makeMove(0, 1) // O
        game.makeMove(1, 1) // X
        game.makeMove(0, 2) // O
        game.makeMove(2, 2) // X wins diagonal
        assertEquals(TicTacToeGame.GameState.X_WINS, game.state)
    }

    @Test
    fun makeMove_draw() {
        // X O X
        // X X O
        // O X O
        game.makeMove(0, 0) // X
        game.makeMove(0, 1) // O
        game.makeMove(0, 2) // X
        game.makeMove(1, 2) // O
        game.makeMove(1, 0) // X
        game.makeMove(2, 0) // O
        game.makeMove(1, 1) // X
        game.makeMove(2, 2) // O
        game.makeMove(2, 1) // X
        assertEquals(TicTacToeGame.GameState.DRAW, game.state)
    }

    @Test
    fun winningCells_correctForRowWin() {
        game.makeMove(0, 0) // X
        game.makeMove(1, 0) // O
        game.makeMove(0, 1) // X
        game.makeMove(1, 1) // O
        game.makeMove(0, 2) // X wins row 0
        val cells = game.winningCells
        assertEquals(3, cells.size)
        assertTrue(cells.contains(Pair(0, 0)))
        assertTrue(cells.contains(Pair(0, 1)))
        assertTrue(cells.contains(Pair(0, 2)))
    }

    @Test
    fun reset_clearsBoard() {
        game.makeMove(0, 0)
        game.makeMove(1, 1)
        game.reset()
        for (row in 0..2) {
            for (col in 0..2) {
                assertEquals(TicTacToeGame.Player.NONE, game.board[row][col])
            }
        }
        assertEquals(TicTacToeGame.GameState.IN_PROGRESS, game.state)
        assertEquals(TicTacToeGame.Player.X, game.currentPlayer)
    }

    @Test
    fun getBestMove_blockPlayerWin() {
        // X is about to win in top row (0,0 and 0,1 occupied)
        // O should block by playing 0,2
        game.makeMove(0, 0) // X
        game.makeMove(2, 2) // O
        game.makeMove(0, 1) // X
        // Now it's O's turn - O should block row 0
        val bestMove = game.getBestMove()
        assertNotNull(bestMove)
        // AI must prevent X from winning at (0,2)
        assertEquals(Pair(0, 2), bestMove)
    }

    @Test
    fun getBestMove_takesWinningMove() {
        // O can win by playing at (2,2)
        game.makeMove(0, 0) // X
        game.makeMove(0, 2) // O
        game.makeMove(1, 0) // X
        game.makeMove(1, 2) // O
        game.makeMove(2, 1) // X (not column 2)
        // O's turn - should take winning move at (2,2)
        val bestMove = game.getBestMove()
        assertNotNull(bestMove)
        assertEquals(Pair(2, 2), bestMove)
    }

    @Test
    fun getBestMove_returnsNullWhenGameOver() {
        // Fill the board to draw
        game.makeMove(0, 0); game.makeMove(0, 1); game.makeMove(0, 2)
        game.makeMove(1, 0); game.makeMove(1, 1); game.makeMove(1, 2)
        game.makeMove(2, 0); game.makeMove(2, 1); game.makeMove(2, 2)
        assertNull(game.getBestMove())
    }
}
