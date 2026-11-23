package com.example.tictactoe.core

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class AppViewModel : ViewModel() {
    // null = system setting follow karo
    var darkOverride by mutableStateOf<Boolean?>(null)
        private set

    fun toggleTheme(currentDark: Boolean) {
        darkOverride = !currentDark
    }
}