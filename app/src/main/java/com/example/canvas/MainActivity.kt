package com.example.canvas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import com.example.canvas.ui.theme.CanvasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CanvasTheme {
                MainScreen()
            }
        }
    }
}

data class Line(
    val points: List<Offset>,
    val color: Color = Color.Black,
    val strokeWidth: Float = 10f
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val lines = remember { mutableStateListOf<Line>() }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Drawing Canvas") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { lines.clear() }) {
                Icon(Icons.Default.Delete, contentDescription = "Clear")
            }
        }
    ) { innerPadding ->
        DrawingCanvas(
            lines = lines,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun DrawingCanvas(lines: MutableList<Line>, modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        lines.add(Line(points = listOf(offset)))
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val lastLine = lines.last()
                        val updatedLine = lastLine.copy(points = lastLine.points + change.position)
                        lines[lines.size - 1] = updatedLine
                    }
                )
            }
    ) {
        lines.forEach { line ->
            val path = Path().apply {
                if (line.points.isNotEmpty()) {
                    moveTo(line.points[0].x, line.points[0].y)
                    line.points.forEach { point ->
                        lineTo(point.x, point.y)
                    }
                }
            }
            drawPath(
                path = path,
                color = line.color,
                style = Stroke(
                    width = line.strokeWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    CanvasTheme {
        MainScreen()
    }
}