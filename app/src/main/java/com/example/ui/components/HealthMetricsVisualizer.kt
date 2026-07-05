package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.HealthMetric
import com.example.ui.theme.*
import com.example.ui.viewmodel.SyncViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HealthMetricsVisualizer(
    viewModel: SyncViewModel,
    modifier: Modifier = Modifier
) {
    val metrics by viewModel.allMetrics.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Calculated fields based on state
    val totalSteps = remember(metrics) { metrics.sumOf { it.steps } }
    val meanSteps = remember(metrics) { if (metrics.isNotEmpty()) totalSteps / metrics.size else 0 }
    val meanSleep = remember(metrics) { if (metrics.isNotEmpty()) metrics.map { it.sleepHours }.average().toFloat() else 0f }
    val meanActive = remember(metrics) { if (metrics.isNotEmpty()) metrics.map { it.activeMinutes }.average().toInt() else 0 }
    val meanHeartRate = remember(metrics) { if (metrics.isNotEmpty()) metrics.map { it.heartRate }.average().toInt() else 0 }
    val meanHydration = remember(metrics) { if (metrics.isNotEmpty()) metrics.sumOf { it.hydrationMl } / metrics.size else 0 }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .border(1.dp, Slate700, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Module Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                Column {
                    Text(
                        text = "Welfare stats & analytics",
                        style = MaterialTheme.typography.titleLarge.copy(color = Color.White)
                    )
                    Text(
                        text = "Dynamic SQLite analytics and calendar agenda streams",
                        style = MaterialTheme.typography.labelSmall.copy(color = Slate400)
                    )
                }
            }

            IconButton(
                onClick = {
                    viewModel.clearAllLoggedMetrics()
                    Toast.makeText(context, "Metrics database cleared", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .size(36.dp)
                    .background(Slate800, CircleShape)
                    .testTag("clear_metrics_button")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Clear Metrics", tint = RosePrimary, modifier = Modifier.size(16.dp))
            }
        }

        Divider(color = Slate700)

        // Average Stats Grid Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatsCard(
                title = "Mean Steps",
                value = if (metrics.isEmpty()) "0" else "$meanSteps",
                unit = "steps/day",
                icon = Icons.Default.PlayArrow,
                color = EmeraldPrimary,
                modifier = Modifier.weight(1f)
            )

            StatsCard(
                title = "Mean Sleep",
                value = if (metrics.isEmpty()) "0" else String.format("%.1fh", meanSleep),
                unit = "hours/night",
                icon = Icons.Default.Star,
                color = EmeraldPrimary,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatsCard(
                title = "Mean Cardio",
                value = if (metrics.isEmpty()) "0" else "$meanHeartRate",
                unit = "beats/min",
                icon = Icons.Default.Favorite,
                color = RosePrimary,
                modifier = Modifier.weight(1f)
            )

            StatsCard(
                title = "Mean Hydration",
                value = if (metrics.isEmpty()) "0" else "$meanHydration",
                unit = "ml/day",
                icon = Icons.Default.Favorite,
                color = EmeraldPrimary,
                modifier = Modifier.weight(1f)
            )
        }

        // Custom Canvas drawing for dynamic trend curves
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Active calorie & timeline curves",
                style = MaterialTheme.typography.labelSmall.copy(color = Slate400, fontWeight = FontWeight.SemiBold),
                modifier = Modifier.weight(1f)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Steps indicator
                Box(modifier = Modifier.size(6.dp).background(EmeraldPrimary, CircleShape))
                Text("Steps", color = Slate400, fontSize = 9.sp, fontWeight = FontWeight.Bold)

                // Active Minutes indicator
                Box(modifier = Modifier.size(6.dp).background(Color(0xFFA78BFA), CircleShape))
                Text("Active Exercise", color = Slate400, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Slate900)
                .border(1.dp, Slate800, RoundedCornerShape(10.dp))
                .padding(8.dp)
        ) {
            if (metrics.size < 2) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Slate600, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Awaiting biometric datasets (Min 2 entries).",
                            style = MaterialTheme.typography.labelSmall.copy(color = Slate500)
                        )
                    }
                }
            } else {
                // Drawing dynamic curves using Canvas!
                val sortedMetrics = remember(metrics) { metrics.sortedBy { it.timestamp } }
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                ) {
                    val width = size.width
                    val height = size.height

                    val maxSteps = sortedMetrics.maxOf { it.steps }.toFloat().coerceAtLeast(1000f)
                    val minSteps = sortedMetrics.minOf { it.steps }.toFloat().coerceAtMost(maxSteps - 100f)
                    val stepsRange = (maxSteps - minSteps).coerceAtLeast(1f)

                    val maxActive = sortedMetrics.maxOf { it.activeMinutes }.toFloat().coerceAtLeast(60f)
                    val minActive = sortedMetrics.minOf { it.activeMinutes }.toFloat().coerceAtMost(maxActive - 5f)
                    val activeRange = (maxActive - minActive).coerceAtLeast(1f)

                    val points = sortedMetrics.mapIndexed { idx, item ->
                        val x = if (sortedMetrics.size > 1) {
                            idx.toFloat() / (sortedMetrics.size - 1) * width
                        } else {
                            width / 2
                        }
                        // Invert coordinates for Canvas 0,0 top-left
                        val normalizedSteps = (item.steps.toFloat() - minSteps) / stepsRange
                        val y = height - (normalizedSteps * height)
                        Offset(x, y)
                    }

                    val activePoints = sortedMetrics.mapIndexed { idx, item ->
                        val x = if (sortedMetrics.size > 1) {
                            idx.toFloat() / (sortedMetrics.size - 1) * width
                        } else {
                            width / 2
                        }
                        val normalizedActive = (item.activeMinutes.toFloat() - minActive) / activeRange
                        val y = height - (normalizedActive * height)
                        Offset(x, y)
                    }

                    // Draw connections path
                    val path = Path().apply {
                        if (points.isNotEmpty()) {
                            moveTo(points[0].x, points[0].y)
                            for (i in 1 until points.size) {
                                // Simple spline interpolation
                                val prev = points[i - 1]
                                val curr = points[i]
                                val cx1 = (prev.x + curr.x) / 2
                                val cy1 = prev.y
                                val cx2 = (prev.x + curr.x) / 2
                                val cy2 = curr.y
                                cubicTo(cx1, cy1, cx2, cy2, curr.x, curr.y)
                            }
                        }
                    }

                    val activePath = Path().apply {
                        if (activePoints.isNotEmpty()) {
                            moveTo(activePoints[0].x, activePoints[0].y)
                            for (i in 1 until activePoints.size) {
                                val prev = activePoints[i - 1]
                                val curr = activePoints[i]
                                val cx1 = (prev.x + curr.x) / 2
                                val cy1 = prev.y
                                val cx2 = (prev.x + curr.x) / 2
                                val cy2 = curr.y
                                cubicTo(cx1, cy1, cx2, cy2, curr.x, curr.y)
                            }
                        }
                    }

                    // Stroke steps
                    drawPath(
                        path = path,
                        brush = Brush.linearGradient(
                            colors = listOf(EmeraldPrimary, EmeraldSecondary)
                        ),
                        style = Stroke(width = 3.dp.toPx())
                    )

                    // Stroke active minutes (dashed trend curve)
                    drawPath(
                        path = activePath,
                        color = Color(0xFFA78BFA),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
                        )
                    )

                    // Circles inside endpoints (Steps)
                    points.forEach { point ->
                        drawCircle(
                            color = CosmicBackground,
                            radius = 5.dp.toPx(),
                            center = point
                        )
                        drawCircle(
                            color = EmeraldPrimary,
                            radius = 3.dp.toPx(),
                            center = point
                        )
                    }

                    // Circles inside endpoints (Active minutes)
                    activePoints.forEach { point ->
                        drawCircle(
                            color = CosmicBackground,
                            radius = 5.dp.toPx(),
                            center = point
                        )
                        drawCircle(
                            color = Color(0xFFA78BFA),
                            radius = 3.dp.toPx(),
                            center = point
                        )
                    }
                }
            }
        }

        // Calendar Scheduling Snapshots Timeline UI (NEW: calendar integration)
        Text(
            text = "Active calendar schedule logs",
            style = MaterialTheme.typography.labelSmall.copy(color = Slate400, fontWeight = FontWeight.SemiBold)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 140.dp)
        ) {
            if (metrics.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Calendar schedules currently vacant.",
                        style = MaterialTheme.typography.labelSmall.copy(color = Slate500)
                    )
                }
            } else {
                val last3Metrics = remember(metrics) { metrics.take(3) }
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    last3Metrics.forEach { metric ->
                        CalendarScheduleLogCard(metric)
                    }
                }
            }
        }
    }
}

@Composable
fun StatsCard(
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Slate900)
            .border(1.dp, Slate800, RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(color.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        }

        Column {
            Text(text = title, color = Slate400, style = MaterialTheme.typography.labelSmall)
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = value,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    fontSize = 15.sp
                )
                Text(
                    text = unit,
                    color = Slate500,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp
                )
            }
        }
    }
}

@Composable
fun CalendarScheduleLogCard(metric: HealthMetric) {
    val dateText = remember(metric.timestamp) {
        SimpleDateFormat("EEE, MMM dd 'at' HH:mm", Locale.getDefault()).format(Date(metric.timestamp))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Slate900)
            .border(1.dp, Slate800, RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(EmeraldPrimary.copy(alpha = 0.12f), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.DateRange, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(14.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Biometric Sync: Daily Welfare Active Event",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color.White),
                fontSize = 11.sp
            )
            Text(
                text = "Mapped on calendar: $dateText. Logs walked ${metric.steps} steps with sleep ${metric.sleepHours}h mean.",
                style = MaterialTheme.typography.labelSmall.copy(color = Slate300),
                fontSize = 10.sp
            )
        }
    }
}
