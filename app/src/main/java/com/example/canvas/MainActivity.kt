package com.example.canvas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
    val palette = remember {
        listOf(
            Color.Black,
            Color(0xFF0D47A1),
            Color(0xFF2E7D32),
            Color(0xFFEF6C00),
            Color(0xFFB71C1C),
            Color(0xFF6A1B9A)
        )
    }
    var selectedColor by remember { mutableStateOf(Color.Black) }
    var strokeWidth by remember { mutableFloatStateOf(10f) }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            BrushControls(
                palette = palette,
                selectedColor = selectedColor,
                onColorSelected = { selectedColor = it },
                strokeWidth = strokeWidth,
                onStrokeWidthChange = { strokeWidth = it }
            )
            Spacer(modifier = Modifier.height(8.dp))
            DrawingCanvas(
                lines = lines,
                activeColor = selectedColor,
                activeStrokeWidth = strokeWidth,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun BrushControls(
    palette: List<Color>,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    strokeWidth: Float,
    onStrokeWidthChange: (Float) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "Color")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                palette.forEach { color ->
                    val isSelected = color == selectedColor
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 34.dp else 28.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable { onColorSelected(color) }
                    )
                }
            }
            Text(
                text = "Brush size: ${strokeWidth.toInt()} px",
                modifier = Modifier.padding(top = 12.dp)
            )
            Slider(
                value = strokeWidth,
                onValueChange = onStrokeWidthChange,
                valueRange = 2f..32f
            )
        }
    }
}

@Composable
fun DrawingCanvas(
    lines: MutableList<Line>,
    activeColor: Color,
    activeStrokeWidth: Float,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        lines.add(
                            Line(
                                points = listOf(offset),
                                color = activeColor,
                                strokeWidth = activeStrokeWidth
                            )
                        )
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