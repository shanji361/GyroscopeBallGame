package com.example.gyroscopeballgame

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.gyroscopeballgame.ui.theme.GyroscopeBallGameTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GyroscopeBallGameTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF023972)
                ) {
                    GyroscopeBallGame()
                }
            }
        }
    }
}

data class Ball(var x: Float, var y: Float, val radius: Float = 40f)
data class Wall(val rect: Rect)

@Composable
fun GyroscopeBallGame() {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val gyroscope = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }

    var tiltX by remember { mutableFloatStateOf(0f) }
    var tiltY by remember { mutableFloatStateOf(0f) }
    var ball by remember { mutableStateOf(Ball(100f, 100f)) }
    var hasWon by remember { mutableStateOf(false) }
    var gameStarted by remember { mutableStateOf(false) }

    val walls = remember {
        listOf(

            Wall(Rect(0f, 0f, 1080f, 40f)),
            Wall(Rect(0f, 0f, 40f, 2000f)),
            Wall(Rect(1040f, 0f, 1080f, 2000f)),
            Wall(Rect(0f, 1960f, 1080f, 2000f)),


            Wall(Rect(60f, 200f, 110f, 600f)),
            Wall(Rect(400f, 390f, 800f, 430f)),
            Wall(Rect(500f, 690f, 700f, 730f)),
            Wall(Rect(590f, 40f, 630f, 700f)),
            Wall(Rect(70f, 990f, 200f, 1030f)),
            Wall(Rect(400f, 800f, 1200f, 850f)),
            Wall(Rect(390f, 1200f, 430f, 1600f)),
            Wall(Rect(790f, 1300f, 830f, 1700f)),
            Wall(Rect(300f, 300f, 550f, 340f)),
            Wall(Rect(200f, 500f, 600f, 540f)),
            Wall(Rect(500f, 1100f, 900f, 1140f)),
            Wall(Rect(250f, 1500f, 750f, 1540f))

        )
    }

    val goalRect = remember { Rect(60f, 1800f, 1000f, 1960f) }

    val sensorListener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(e: SensorEvent?) {
                e?.let {
                    tiltX = it.values[0]
                    tiltY = it.values[1]
                }
            }
            override fun onAccuracyChanged(s: Sensor?, a: Int) {}
        }
    }


    // Listen for gyroscope events
    DisposableEffect(Unit) {
        gyroscope?.let {
            sensorManager.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_GAME)
        }

        onDispose {
            sensorManager.unregisterListener(sensorListener)
        }
    }

    LaunchedEffect(tiltX, tiltY, gameStarted) {
        if (!gameStarted) return@LaunchedEffect
        while (!hasWon) {
            delay(16)

            val speed = 3f
            var newX = ball.x - tiltX * speed
            var newY = ball.y + tiltY * speed

            val ballRect = Rect(
                newX - ball.radius,
                newY - ball.radius,
                newX + ball.radius,
                newY + ball.radius
            )

            var collision = false
            for (wall in walls) {
                if (ballRect.overlaps(wall.rect)) {
                    collision = true
                    break
                }
            }

            if (!collision) {
                ball = ball.copy(x = newX, y = newY)
            } else {
                val ballRectX = Rect(
                    newX - ball.radius,
                    ball.y - ball.radius,
                    newX + ball.radius,
                    ball.y + ball.radius
                )
                var xCollision = false
                for (wall in walls) {
                    if (ballRectX.overlaps(wall.rect)) {
                        xCollision = true
                        break
                    }
                }
                if (!xCollision) {
                    ball = ball.copy(x = newX)
                }

                val ballRectY = Rect(
                    ball.x - ball.radius,
                    newY - ball.radius,
                    ball.x + ball.radius,
                    newY + ball.radius
                )
                var yCollision = false
                for (wall in walls) {
                    if (ballRectY.overlaps(wall.rect)) {
                        yCollision = true
                        break
                    }
                }
                if (!yCollision) {
                    ball = ball.copy(y = newY)
                }
            }

            val ballCenter = Rect(
                ball.x - ball.radius,
                ball.y - ball.radius,
                ball.x + ball.radius,
                ball.y + ball.radius
            )
            if (ballCenter.overlaps(goalRect)) {
                hasWon = true
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val scaleX = size.width / 1080f
            val scaleY = size.height / 2000f

            walls.forEach { wall ->
                drawRect(
                    color = Color(0xFFD2692D),
                    topLeft = Offset(wall.rect.left * scaleX, wall.rect.top * scaleY),
                    size = Size(wall.rect.width * scaleX, wall.rect.height * scaleY)
                )
            }

            drawRect(
                color = Color(0xFF2FEF36),
                topLeft = Offset(goalRect.left * scaleX, goalRect.top * scaleY),
                size = Size(goalRect.width * scaleX, goalRect.height * scaleY)
            )

            drawCircle(
                color = Color(0xFF48D7EA),
                radius = ball.radius * scaleX,
                center = Offset(ball.x * scaleX, ball.y * scaleY)
            )
        }

        if (!gameStarted) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Move the ball to the green block!",
                    color = Color(0xFFF6F8F7)
                )

                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = { gameStarted = true }) {
                    Text("Start Game")
                }
            }
        }

        if (hasWon) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "You Win!",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color(0xFF4CAF50)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    ball = Ball(100f, 100f)
                    hasWon = false
                    gameStarted = false
                }) {
                    Text("Play Again")
                }
            }
        }
    }
}