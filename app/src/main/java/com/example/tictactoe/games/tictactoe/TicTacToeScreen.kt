package com.example.tictactoe.games.tictactoe

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tictactoe.core.game.GameMode
import com.example.tictactoe.core.ui.Confetti
import kotlinx.coroutines.delay

// yahan ScoreItem, Announcement, NamesDialog paste karo (jaise the waise)
// ---------- Scoreboard ----------
@Composable
fun ScoreItem(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 14.sp, color = Color.Gray)
        Text(value.toString(), fontSize = 28.sp, fontWeight = FontWeight.Bold)
    }
}

// ---------- Announcement ----------
@Composable
fun Announcement(visible: Boolean, text: String, color: Color) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(initialScale = 0.3f) + fadeIn()
    ) {
        Text(
            text = text,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
    }
}

// ---------- Player names dialog ----------
@Composable
fun NamesDialog(
    xName: String,
    oName: String,
    showO: Boolean,
    onSave: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var x by remember { mutableStateOf(xName) }
    var o by remember { mutableStateOf(oName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Player names") },
        text = {
            Column {
                OutlinedTextField(
                    value = x,
                    onValueChange = { if (it.length <= 10) x = it },
                    label = { Text("Player X") },
                    singleLine = true
                )
                if (showO) {
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = o,
                        onValueChange = { if (it.length <= 10) o = it },
                        label = { Text("Player O") },
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = { onSave(x, o) }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
@Composable
fun TicTacToeScreen(
    mode: GameMode,
    dark: Boolean,
    onToggleTheme: () -> Unit,
    onExit: () -> Unit,
    vm: TicTacToeViewModel = viewModel()   // is screen (nav entry) ke scope ka ViewModel
) {
    // Dashboard se aaya mode set karo (VM mein sirf pehli baar apply hota hai)
    LaunchedEffect(Unit) { vm.startWith(mode == GameMode.VS_COMPUTER) }

    var showNames by rememberSaveable { mutableStateOf(true) }
    var showResult by remember { mutableStateOf(false) }

    val winner = vm.winner
    val gameOver = vm.gameOver

    LaunchedEffect(gameOver) {
        showResult = false
        if (gameOver) {
            delay(900)
            showResult = true
        }
    }

    LaunchedEffect(vm.isComputerTurn, vm.board) {
        if (vm.isComputerTurn) {
            delay(600)
            vm.computerMove()
        }
    }

    val lineColor = MaterialTheme.colorScheme.onBackground
    val xColor = lineColor
    val oColor = if (dark) Color(0xFF64B5F6) else Color(0xFF1565C0)

    val status = if (gameOver) "Game over" else "${vm.nameOf(vm.player)}'s turn (${vm.player})"
    val announceText =
        if (winner != null) "\uD83C\uDF89 ${vm.nameOf(winner)} wins!" else "It's a draw!"
    val announceColor = when (winner) {
        "X" -> xColor
        "O" -> oColor
        else -> Color.Gray
    }
    val cellSize = 96.dp

    if (showNames) {
        NamesDialog(
            xName = vm.xName,
            oName = vm.oName,
            showO = !vm.singlePlayer,
            onSave = { x, o -> vm.setNames(x, o); showNames = false },
            onDismiss = { showNames = false }
        )
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Difficulty sirf vs Computer mein
            if (vm.singlePlayer) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !vm.hard,
                        onClick = { vm.changeDifficulty(false) },
                        label = { Text("Easy") }
                    )
                    FilterChip(
                        selected = vm.hard,
                        onClick = { vm.changeDifficulty(true) },
                        label = { Text("Hard") }
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ScoreItem(vm.xName + " (X)", vm.xWins)
                ScoreItem("Draws", vm.draws)
                ScoreItem(vm.oDisplay + " (O)", vm.oWins)
            }

            Spacer(Modifier.height(24.dp))
            Text(status, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            // Sirf board: Box ke andar 3x3 cells + WinLine
            Box {
                Column {
                    for (row in 0..2) {
                        Row {
                            for (col in 0..2) {
                                val index = row * 3 + col
                                Box(
                                    modifier = Modifier
                                        .size(cellSize)
                                        .border(1.dp, lineColor)
                                        .clickable { vm.onCellClick(index) }
                                ) {
                                    AnimatedMark(
                                        mark = vm.board[index],
                                        xColor = xColor,
                                        oColor = oColor,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }
                WinLine(vm.winLine, Modifier.matchParentSize())
            }

            Box(
                modifier = Modifier.height(64.dp).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Announcement(visible = showResult, text = announceText, color = announceColor)
            }

            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { vm.newRound() }) { Text("New Round") }
                OutlinedButton(onClick = { vm.resetScores() }) { Text("Reset Scores") }
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { showNames = true }) { Text("Change player names") }
        }

        // Back button: top-left
        TextButton(
            onClick = onExit,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(8.dp)
        ) { Text("← Back") }

        // Theme toggle: top-right
        IconButton(
            onClick = onToggleTheme,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(8.dp)
        ) {
            Text(if (dark) "☀️" else "🌙", fontSize = 26.sp)
        }

        if (showResult && winner != null) {
            Confetti(Modifier.fillMaxSize())
        }
    }
}