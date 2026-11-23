package com.example.tictactoe.core.game

enum class GameMode(val title: String, val available: Boolean) {
    LOCAL_2P("Two Players (same phone)", true),
    VS_COMPUTER("vs Computer", true),
    ONLINE("Online Multiplayer", false)
}

data class GameInfo(
    val id: String,
    val title: String,
    val emoji: String,
    val description: String,
    val modes: List<GameMode>,
    val available: Boolean = true
)

// Naya game add karna ho toh bas yahan ek entry + games/<naam>/ package
object GameRegistry {
    val games = listOf(
        GameInfo(
            id = "tictactoe",
            title = "Tic-Tac-Toe",
            emoji = "❌⭕",
            description = "Classic 3x3 with animations",
            modes = listOf(GameMode.VS_COMPUTER, GameMode.LOCAL_2P, GameMode.ONLINE)
        ),
        GameInfo("chess", "Chess", "♟️", "Coming soon", emptyList(), available = false),
        GameInfo("sudoku", "Sudoku", "🔢", "Coming soon", emptyList(), available = false)
    )

    fun byId(id: String?) = games.firstOrNull { it.id == id }
}