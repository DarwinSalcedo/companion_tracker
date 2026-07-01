package com.test.track.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import com.test.track.data.AnalyticsEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun FloatingBubbleUI(eventsFlow: Flow<List<AnalyticsEvent>>, expandState: MutableStateFlow<Boolean>, onDrag: (Int, Int) -> Unit) {
    val events by eventsFlow.collectAsState(initial = emptyList())
    val isExpanded by expandState.collectAsState()

    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFF6200EE))
                .testTag("analytics_bubble_icon")
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount.x.toInt(), dragAmount.y.toInt())
                    }
                }
                .clickable { expandState.value = !isExpanded },
            contentAlignment = Alignment.Center
        ) {

            if (events.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxSize()
                        .background(Color.Red, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (events.size > 99) "99+" else events.size.toString(),
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // The expanded list
        AnimatedVisibility(visible = isExpanded) {
            Card(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .width(280.dp)
                    .heightIn(max = 350.dp)
                    .testTag("analytics_bubble_list"),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {

                    if (events.isEmpty()) {
                        Text(text = "No hay eventos capturados", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        // Show the latest 5 events
                        events.take(5).forEachIndexed { index, event ->
                            Column(
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .testTag("event_item_$index")
                            ) {
                                Text(
                                    text = event.eventName,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.testTag("event_name_$index")
                                )
                                Text(
                                    text = event.eventData,
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    maxLines = 2,
                                    modifier = Modifier.testTag("event_data_$index")
                                )
                                HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
