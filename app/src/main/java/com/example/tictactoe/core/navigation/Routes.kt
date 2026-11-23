package com.example.tictactoe.core.navigation

import com.example.tictactoe.core.game.GameMode

object Routes {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val PROFILE = "profile"
    const val GAME = "game/{gameId}/{mode}"

    fun game(gameId: String, mode: GameMode) = "game/$gameId/${mode.name}"
}