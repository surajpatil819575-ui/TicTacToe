package com.example.tictactoe   // apna package name rakho

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

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
class GameViewModel : ViewModel() {
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
    var darkOverride by mutableStateOf<Boolean?>(null)
        private set
    var singlePlayer by mutableStateOf(false)
        private set
    var hard by mutableStateOf(true)
        private set

    val oDisplay: String get() = if (singlePlayer) "Computer" else oName
    val isComputerTurn: Boolean get() = singlePlayer && player == "O" && !gameOver

    fun setMode(single: Boolean) { singlePlayer = single; resetScores() }
    fun changeDifficulty(h: Boolean) { hard = h; resetScores() }
    fun toggleTheme(currentDark: Boolean) {
        darkOverride = !currentDark
    }

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

class MainActivity : ComponentActivity() {
    private val vm: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Dark theme: phone ki system setting follow karta hai
            val dark = vm.darkOverride ?: isSystemInDarkTheme()
            MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    TicTacToeScreen(vm, dark)
                }
            }
        }
    }
}

// ---------- X / O animation ----------
@Composable
fun AnimatedMark(mark: String, xColor: Color, oColor: Color, modifier: Modifier = Modifier) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(mark) {
        progress.snapTo(0f)
        if (mark.isNotEmpty()) progress.animateTo(1f, tween(400))
    }

    Canvas(modifier) {
        val p = progress.value
        val pad = size.minDimension * 0.25f
        val strokeW = 10.dp.toPx()

        when (mark) {
            "X" -> {
                val p1 = (p * 2f).coerceIn(0f, 1f)
                val p2 = ((p - 0.5f) * 2f).coerceIn(0f, 1f)
                val a = Offset(pad, pad)
                val b = Offset(size.width - pad, size.height - pad)
                drawLine(xColor, a, lerp(a, b, p1), strokeW, StrokeCap.Round)
                if (p2 > 0f) {
                    val c = Offset(size.width - pad, pad)
                    val d = Offset(pad, size.height - pad)
                    drawLine(xColor, c, lerp(c, d, p2), strokeW, StrokeCap.Round)
                }
            }
            "O" -> drawArc(
                color = oColor,
                startAngle = -90f,
                sweepAngle = 360f * p,
                useCenter = false,
                topLeft = Offset(pad, pad),
                size = Size(size.width - 2 * pad, size.height - 2 * pad),
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )
        }
    }
}

// ---------- Win line ----------
@Composable
fun WinLine(line: List<Int>?, modifier: Modifier = Modifier) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(line) {
        progress.snapTo(0f)
        if (line != null) progress.animateTo(1f, tween(500, delayMillis = 400))
    }

    Canvas(modifier) {
        if (line != null) {
            val cw = size.width / 3f
            val ch = size.height / 3f
            fun center(i: Int) = Offset((i % 3 + 0.5f) * cw, (i / 3 + 0.5f) * ch)

            val start = center(line[0])
            val end = center(line[2])
            drawLine(
                color = Color(0xFFEF5350),
                start = start,
                end = lerp(start, end, progress.value),
                strokeWidth = 12.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

// ---------- Confetti ----------
class Particle(
    val x: Float,
    val speed: Float,
    val size: Float,
    val color: Color,
    val drift: Float,
    val phase: Float,
    val spin: Float
)

val confettiColors = listOf(
    Color(0xFFE53935), Color(0xFF1E88E5), Color(0xFF43A047),
    Color(0xFFFDD835), Color(0xFF8E24AA), Color(0xFFFB8C00)
)

@Composable
fun Confetti(modifier: Modifier = Modifier) {
    val particles = remember {
        List(90) {
            Particle(
                x = Random.nextFloat(),
                speed = 1f + Random.nextFloat() * 0.6f,
                size = 6f + Random.nextFloat() * 6f,
                color = confettiColors.random(),
                drift = 10f + Random.nextFloat() * 30f,
                phase = Random.nextFloat() * 6.28f,
                spin = 0.5f + Random.nextFloat()
            )
        }
    }
    val t = remember { Animatable(0f) }
    LaunchedEffect(Unit) { t.animateTo(1f, tween(3500, easing = LinearEasing)) }

    Canvas(modifier) {
        for (p in particles) {
            val prog = t.value * p.speed
            val y = -20f + prog * (size.height + 40f)
            if (y > size.height) continue
            val x = p.x * size.width + sin(prog * 10f + p.phase) * p.drift.dp.toPx()
            val s = p.size.dp.toPx()
            rotate(degrees = prog * 720f * p.spin, pivot = Offset(x, y)) {
                drawRect(p.color, Offset(x - s / 2, y - s / 2), Size(s, s * 0.6f))
            }
        }
    }
}

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
                if (showO) {
                    OutlinedTextField(
                        value = x,
                        onValueChange = { if (it.length <= 10) x = it },
                        label = { Text("Player X") },
                        singleLine = true
                    )
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

// ---------- Main screen ----------
@Composable
fun TicTacToeScreen(vm: GameViewModel, dark: Boolean) {
    // Sirf UI-level state yahan: dialog dikhana hai ya nahi, aur announcement timing
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
    // Computer ki turn: thoda ruk ke move kare, taaki natural lage
    LaunchedEffect(vm.isComputerTurn, vm.board) {
        if (vm.isComputerTurn) {
            delay(600)
            vm.computerMove()
        }
    }

    // Theme ke hisaab se colors
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
            // Mode selector (board Box se BAHAR)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !vm.singlePlayer,
                    onClick = { vm.setMode(false) },
                    label = { Text("2 Players") }
                )
                FilterChip(
                    selected = vm.singlePlayer,
                    onClick = { vm.setMode(true) },
                    label = { Text("vs Computer") }
                )
            }
            if (vm.singlePlayer) {
                Spacer(Modifier.height(8.dp))
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
            }
            Spacer(Modifier.height(16.dp))

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

            // Sirf board: Box ke andar sirf 3x3 cells + WinLine
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
        // Theme toggle: top-right corner
        IconButton(
            onClick = { vm.toggleTheme(dark) },
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