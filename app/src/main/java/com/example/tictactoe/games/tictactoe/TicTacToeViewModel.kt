package com.example.tictactoe.games.tictactoe

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

// yahan winLines, findWin, GameViewModel paste karo
// ---------- Game logic ----------
val winLines = listOf(
    listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
    listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
    listOf(0, 4, 8), listOf(2, 4, 6)
)

fun findWin(board: List<String>): Pair<String, List<Int>>? {
    for (line in winLines) {
        val (a, b, c) = line
        if (board[a].isNotEmpty() && board[a] == board[b] && board[b] == board[c])
            return board[a] to line
    }
    return null
}

// ---------- ViewModel: saara game state yahan rehta hai ----------
class TicTacToeViewModel : ViewModel() {
    var board by mutableStateOf(List(9) { "" })
        private set
    var player by mutableStateOf("X")
        private set
    var xWins by mutableIntStateOf(0)
        private set
    var oWins by mutableIntStateOf(0)
        private set
    var draws by mutableIntStateOf(0)
        private set
    var xName by mutableStateOf("X")
        private set
    var oName by mutableStateOf("O")
        private set
    // null = phone ki system setting follow karo, true/false = user ne manually chuna
    var singlePlayer by mutableStateOf(false)
        private set
    var hard by mutableStateOf(true)
        private set

    val oDisplay: String get() = if (singlePlayer) "Computer" else oName
    val isComputerTurn: Boolean get() = singlePlayer && player == "O" && !gameOver

    private var started = false

    // Dashboard se aaya mode sirf pehli baar apply hota hai (rotation pe dobara nahi)
    fun startWith(single: Boolean) {
        if (started) return
        started = true
        singlePlayer = single
    }
    fun changeDifficulty(h: Boolean) { hard = h; resetScores() }

    // board se nikalne wali values (derived state)
    private val result get() = findWin(board)
    val winner: String? get() = result?.first
    val winLine: List<Int>? get() = result?.second
    val isDraw: Boolean get() = winner == null && board.all { it.isNotEmpty() }
    val gameOver: Boolean get() = winner != null || isDraw

    fun nameOf(p: String) = if (p == "X") xName else oDisplay

    // Insaan ka click: computer ki turn mein ignore
    fun onCellClick(index: Int) {
        if (isComputerTurn) return
        place(index)
    }

    // Asli move rakhne ka kaam (insaan aur computer dono use karte hain)
    private fun place(index: Int) {
        if (board[index].isNotEmpty() || gameOver) return
        val newBoard = board.toMutableList().also { it[index] = player }
        board = newBoard

        val r = findWin(newBoard)
        when {
            r != null -> if (r.first == "X") xWins++ else oWins++
            newBoard.all { it.isNotEmpty() } -> draws++
            else -> player = if (player == "X") "O" else "X"
        }
    }

    fun computerMove() {
        if (!isComputerTurn) return
        val empty = board.indices.filter { board[it].isEmpty() }
        val move = if (hard) bestMove(board) else empty.random()
        place(move)
    }

    // O (computer) ke liye sabse achhi cell
    private fun bestMove(b: List<String>): Int {
        var best = -100
        var bestIdx = -1
        for (i in b.indices) {
            if (b[i].isEmpty()) {
                val copy = b.toMutableList().also { it[i] = "O" }
                val score = minimax(copy, false, 1)
                if (score > best) { best = score; bestIdx = i }
            }
        }
        return bestIdx
    }

    // O maximize karta hai, X minimize. Jaldi jeetna better (10 - depth)
    private fun minimax(b: MutableList<String>, oTurn: Boolean, depth: Int): Int {
        findWin(b)?.let { return if (it.first == "O") 10 - depth else depth - 10 }
        if (b.all { it.isNotEmpty() }) return 0

        var best = if (oTurn) -100 else 100
        for (i in b.indices) {
            if (b[i].isEmpty()) {
                b[i] = if (oTurn) "O" else "X"
                val score = minimax(b, !oTurn, depth + 1)
                b[i] = ""                                   // undo (backtracking)
                best = if (oTurn) maxOf(best, score) else minOf(best, score)
            }
        }
        return best
    }

    fun newRound() {
        board = List(9) { "" }
        player = "X"
    }

    fun resetScores() {
        newRound()
        xWins = 0; oWins = 0; draws = 0
    }

    fun setNames(x: String, o: String) {
        xName = x.trim().ifEmpty { "X" }
        oName = o.trim().ifEmpty { "O" }
    }
}
