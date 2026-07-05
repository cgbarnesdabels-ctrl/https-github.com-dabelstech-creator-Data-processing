package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.SyncViewModel

@Composable
fun DailyHealthMetricsForm(
    viewModel: SyncViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Fields state holders
    var stepsText by remember { mutableStateOf(TextFieldValue("7450")) }
    var activeMinutesSlider by remember { mutableFloatStateOf(45f) }
    var sleepHoursSlider by remember { mutableFloatStateOf(7.5f) }
    var heartRateText by remember { mutableStateOf(TextFieldValue("72")) }
    var hydrationText by remember { mutableStateOf(TextFieldValue("1250")) }
    var personalNotesText by remember { mutableStateOf(TextFieldValue("Felt energized during morning cardio. Hydrated well.")) }

    // Toggle: Also map to Calendar system contract
    var syncToCalendarCheckbox by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .border(1.dp, Slate700, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Form Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.DateRange, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
            Column {
                Text(
                    text = "Log biometric metrics",
                    style = MaterialTheme.typography.titleLarge.copy(color = Color.White)
                )
                Text(
                    text = "Append dynamic wellness journals to SQLite db",
                    style = MaterialTheme.typography.labelSmall.copy(color = Slate400)
                )
            }
        }

        Divider(color = Slate700)

        // Steps Input Field
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Steps
            Column(modifier = Modifier.weight(1f)) {
                Text("Steps Walked", color = Slate300, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                TextField(
                    value = stepsText,
                    onValueChange = { stepsText = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Slate900,
                        unfocusedContainerColor = Slate900,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("steps_input_field")
                )
            }

            // Mean Heart Rate
            Column(modifier = Modifier.weight(1f)) {
                Text("Heart Rate (bpm)", color = Slate300, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                TextField(
                    value = heartRateText,
                    onValueChange = { heartRateText = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Slate900,
                        unfocusedContainerColor = Slate900,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("heart_rate_input_field")
                )
            }
        }

        // Active minutes slider
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Active Session Time", color = Slate300, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Text("${activeMinutesSlider.toInt()} minutes", color = EmeraldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = activeMinutesSlider,
                onValueChange = { activeMinutesSlider = it },
                valueRange = 0f..180f,
                colors = SliderDefaults.colors(
                    thumbColor = EmeraldPrimary,
                    activeTrackColor = EmeraldPrimary,
                    inactiveTrackColor = Slate700
                ),
                modifier = Modifier.testTag("active_minutes_slider")
            )
        }

        // Sleep hours slider
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Sleep Duration", color = Slate300, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Text(String.format("%.1f hrs", sleepHoursSlider), color = EmeraldPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = sleepHoursSlider,
                onValueChange = { sleepHoursSlider = it },
                valueRange = 0f..15f,
                colors = SliderDefaults.colors(
                    thumbColor = EmeraldPrimary,
                    activeTrackColor = EmeraldPrimary,
                    inactiveTrackColor = Slate700
                ),
                modifier = Modifier.testTag("sleep_hours_slider")
            )
        }

        // Hydration Field
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Hydration Volume (ml)", color = Slate300, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            TextField(
                value = hydrationText,
                onValueChange = { hydrationText = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Slate900,
                    unfocusedContainerColor = Slate900,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("hydration_input_field")
            )
        }

        // Personal Notes Field
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Welfare Summary / Notes", color = Slate300, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            TextField(
                value = personalNotesText,
                onValueChange = { personalNotesText = it },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Slate900,
                    unfocusedContainerColor = Slate900,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(64.dp).testTag("notes_input_field")
            )
        }

        Divider(color = Slate700)

        // Dynamic System Calendar Mapping checkbox (Google Calendar sync integration)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Slate900)
                .clickable { syncToCalendarCheckbox = !syncToCalendarCheckbox }
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                Column {
                    Text(text = "Map directly to system Calendar", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Insert entries under CalendarContract protocol", color = Slate400, fontSize = 10.sp)
                }
            }
            Checkbox(
                checked = syncToCalendarCheckbox,
                onCheckedChange = { syncToCalendarCheckbox = it },
                colors = CheckboxDefaults.colors(checkedColor = EmeraldPrimary, uncheckedColor = Slate600),
                modifier = Modifier.testTag("calendar_sync_checkbox")
            )
        }

        // Save Logging Button
        Button(
            onClick = {
                val steps = stepsText.text.toIntOrNull() ?: 0
                val hr = heartRateText.text.toIntOrNull() ?: 0
                val hyd = hydrationText.text.toIntOrNull() ?: 0
                val notes = personalNotesText.text
                
                isSaving = true
                
                viewModel.logMetricSnapshot(
                    steps = steps,
                    activeMinutes = activeMinutesSlider.toInt(),
                    sleepHours = sleepHoursSlider,
                    heartRate = hr,
                    hydrationMl = hyd,
                    notes = notes
                )

                // Calendar synchronization
                if (syncToCalendarCheckbox) {
                    val dummyMetric = com.example.data.model.HealthMetric(
                        timestamp = System.currentTimeMillis(),
                        steps = steps,
                        activeMinutes = activeMinutesSlider.toInt(),
                        sleepHours = sleepHoursSlider,
                        heartRate = hr,
                        hydrationMl = hyd,
                        notes = notes
                    )
                    viewModel.syncMetricToAndroidCalendar(context, dummyMetric) { error ->
                        isSaving = false
                        if (error != null) {
                            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Logged to DB and scheduled to Android Calendar!", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    isSaving = false
                    Toast.makeText(context, "Log recorded inside local DB.", Toast.LENGTH_SHORT).show()
                }

                // Reset inputs to standard
                stepsText = TextFieldValue("")
                activeMinutesSlider = 30f
                sleepHoursSlider = 7f
                heartRateText = TextFieldValue("")
                hydrationText = TextFieldValue("")
                personalNotesText = TextFieldValue("")
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = EmeraldPrimary,
                contentColor = Slate950
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("save_metric_button"),
            enabled = !isSaving
        ) {
            if (isSaving) {
                CircularProgressIndicator(color = Slate950, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Save entry and reconcile", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
