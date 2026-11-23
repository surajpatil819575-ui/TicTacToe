package com.example.tictactoe.feature.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tictactoe.core.game.GameInfo
import com.example.tictactoe.core.game.GameMode
import com.example.tictactoe.core.game.GameRegistry

@Composable
fun DashboardScreen(
    dark: Boolean,
    onToggleTheme: () -> Unit,
    onOpenProfile: () -> Unit,
    onPlay: (String, GameMode) -> Unit
) {
    Column(Modifier.fillMaxSize().statusBarsPadding().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Game Hub",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onToggleTheme) {
                Text(if (dark) "☀️" else "🌙", fontSize = 24.sp)
            }
            IconButton(onClick = onOpenProfile) {
                Text("👤", fontSize = 24.sp)
            }
        }

        Spacer(Modifier.height(16.dp))

        // LazyColumn: sirf screen pe dikhne wale cards compose hote hain (games badhne par bhi fast)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(GameRegistry.games, key = { it.id }) { game ->
                GameCard(game, onPlay)
            }
        }
    }
}

@Composable
private fun GameCard(game: GameInfo, onPlay: (String, GameMode) -> Unit) {
    Card(Modifier.fillMaxWidth().alpha(if (game.available) 1f else 0.5f)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(game.emoji, fontSize = 32.sp)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(game.title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(game.description, fontSize = 13.sp, color = Color.Gray)
                }
            }

            if (game.available) {
                Spacer(Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    game.modes.forEach { mode ->
                        Button(
                            onClick = { onPlay(game.id, mode) },
                            enabled = mode.available,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (mode.available) mode.title else "${mode.title} (coming soon)")
                        }
                    }
                }
            }
        }
    }
}