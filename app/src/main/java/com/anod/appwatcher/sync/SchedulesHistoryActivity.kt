// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.sync

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anod.appwatcher.R
import com.anod.appwatcher.compose.AppTheme
import com.anod.appwatcher.compose.BaseComposeActivity
import com.anod.appwatcher.compose.CommonActivityAction
import com.anod.appwatcher.compose.onCommonActivityAction
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.entities.Failed
import com.anod.appwatcher.database.entities.New
import com.anod.appwatcher.database.entities.Schedule
import com.anod.appwatcher.database.entities.Skipped
import com.anod.appwatcher.database.entities.Success
import com.anod.appwatcher.utils.isLightColor
import com.google.accompanist.flowlayout.FlowRow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * @author Alex Gavrishev
 * @date 04/01/2018
 */
class SchedulesHistoryActivity : BaseComposeActivity(), KoinComponent {
    private val database: AppsDatabase by inject()
    private val dateFormat = SimpleDateFormat("MMM d, HH:mm:ss", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val schedules by database.schedules().load().collectAsState(initial = emptyList())
            SchedulesHistoryScreen(
                schedules = schedules,
                dateFormat = dateFormat,
                onActivityAction = { onCommonActivityAction(it) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulesHistoryScreen(schedules: List<Schedule>, dateFormat: DateFormat, onActivityAction: (CommonActivityAction) -> Unit) {
    AppTheme {
        Surface {
            Column(modifier = Modifier.fillMaxWidth()) {
                TopAppBar(
                    title = { Text(text = stringResource(id = R.string.refresh_history)) },
                    navigationIcon = {
                        IconButton(onClick = { onActivityAction(CommonActivityAction.Finish) }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = stringResource(id = R.string.back)
                            )
                        }
                    },
                )
                LazyColumn {
                    items(schedules.size) { index ->
                        ScheduleRow(
                            schedule = schedules[index],
                            dateFormat = dateFormat
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleRow(schedule: Schedule, dateFormat: DateFormat) {
    val range = remember(schedule) {
        if (schedule.finish > 0) {
            val duration = ((schedule.finish - schedule.start) / 1000.0).toInt()
            "${dateFormat.format(Date(schedule.start))} - ${dateFormat.format(Date(schedule.finish))} (${duration}s)"
        } else {
            dateFormat.format(Date(schedule.start))
        }
    }

    val colorBlue = Color(0xFF64B5F6)
    val colorGreen = Color(0xFF26A69A)
    val colorOrange = Color(0xFFF57C00)
    val colorGrey = Color(0xFF757F94)
    val colorBlueDk = Color(0xFF1976D2)
    val colorYellow = Color(0xFFFFD54F)
    val colorPurple = Color(0xFFff5bff)

    val result = schedule.result()

    Column(Modifier.padding(16.dp)) {
        Text(text = range)
        FlowRow(
            mainAxisSpacing = 8.dp
        ) {
            ScheduleChip(
                text = when (result) {
                    is New -> stringResource(R.string.schedule_status_new)
                    is Success -> stringResource(R.string.schedule_status_success)
                    is Failed -> stringResource(R.string.schedule_status_failed)
                    is Skipped -> stringResource(R.string.schedule_status_skipped)
                },
                color = when (result) {
                    is New -> colorBlue
                    is Success -> colorGreen
                    is Failed -> colorOrange
                    is Skipped -> colorGrey
                }
            )

            ScheduleChip(
                text = when (schedule.reason) {
                    Schedule.reasonSchedule -> stringResource(R.string.schedule_reason_schedule)
                    Schedule.reasonManual -> stringResource(R.string.schedule_status_manual)
                    else -> "Unknown"
                },
                color = when (schedule.reason) {
                    Schedule.reasonSchedule -> colorGrey
                    Schedule.reasonManual -> colorYellow
                    else -> colorBlue
                }
            )

            if (result is Success) {
                ScheduleChip(
                    text = stringResource(R.string.schedule_chip_checked, schedule.checked),
                    color = colorOrange
                )
                ScheduleChip(
                    text = stringResource(R.string.schedule_chip_found, schedule.found),
                    color = colorBlueDk
                )
                if (schedule.unavailable > 0) {
                    ScheduleChip(
                        text = stringResource(R.string.schedule_chip_unavailable, schedule.unavailable),
                        color = colorGrey
                    )
                }
                ScheduleChip(
                    text = stringResource(R.string.schedule_chip_notified, schedule.notified),
                    color = colorPurple
                )
            }
        }
        if (result is Failed || result is Skipped) {
            Text(text = when (result) {
                is Failed -> when (result.reason) {
                    Schedule.statusFailed -> "Unknown error"
                    Schedule.statusFailedNoToken -> "Cannot receive access token"
                    else -> ""
                }
                is Skipped -> when (result.reason) {
                    Schedule.statusSkippedMinTime -> "Last update less than second"
                    Schedule.statusSkippedNoWifi -> "Wifi not enabled"
                    else -> ""
                }
                else -> ""
            })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleChip(text: String, color: Color) {
    SuggestionChip(
        label = { Text(text = text) },
        onClick = {  },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = color,
            labelColor = if (color.isLightColor) Color.Black else Color.White
        )
    )
}