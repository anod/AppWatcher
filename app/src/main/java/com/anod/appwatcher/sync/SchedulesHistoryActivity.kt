// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.sync

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anod.appwatcher.R
import com.anod.appwatcher.database.AppsDatabase
import com.anod.appwatcher.database.entities.*
import com.anod.appwatcher.databinding.ActivityUserLogBinding
import com.anod.appwatcher.databinding.ListItemScheduleBinding
import com.anod.appwatcher.utils.Theme
import com.anod.appwatcher.utils.colorStateListOf
import com.anod.appwatcher.utils.prefs
import info.anodsplace.framework.app.CustomThemeColors
import info.anodsplace.framework.app.ToolbarActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author Alex Gavrishev
 * @date 04/01/2018
 */
class SchedulesHistoryActivity : ToolbarActivity(), KoinComponent {
    private val database: AppsDatabase by inject()

    class ScheduleViewHolder(private val itemBinding: ListItemScheduleBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        private val sdf = SimpleDateFormat("MMM d, HH:mm:ss", Locale.getDefault())
        private val context: Context
            get() = itemView.context

        fun apply(position: Int, schedule: Schedule) {
            val range = if (schedule.finish > 0) {
                val duration = ((schedule.finish - schedule.start) / 1000.0).toInt()
                "${sdf.format(Date(schedule.start))} - ${sdf.format(Date(schedule.finish))} (${duration}s)"
            } else {
                sdf.format(Date(schedule.start))
            }
            itemBinding.time.text = range
            val result = schedule.result()
            itemBinding.status.text = when (result) {
                is New -> context.getString(R.string.schedule_status_new)
                is Success -> context.getString(R.string.schedule_status_success)
                is Failed -> context.getString(R.string.schedule_status_failed)
                is Skipped -> context.getString(R.string.schedule_status_skipped)
            }
            itemBinding.status.chipBackgroundColor = when (result) {
                is New -> colorStateListOf(context, R.color.chip_blue)
                is Success -> colorStateListOf(context, R.color.chip_green)
                is Failed -> colorStateListOf(context, R.color.chip_orange)
                is Skipped -> colorStateListOf(context, R.color.chip_gray)
            }
            itemBinding.reason.text = when (schedule.reason) {
                Schedule.reasonSchedule -> context.getString(R.string.schedule_reason_schedule)
                Schedule.reasonManual -> context.getString(R.string.schedule_status_manual)
                else -> "Unknown"
            }
            itemBinding.reason.chipBackgroundColor = when (schedule.reason) {
                Schedule.reasonSchedule -> colorStateListOf(context, R.color.chip_gray)
                Schedule.reasonManual -> colorStateListOf(context, R.color.chip_yellow)
                else -> colorStateListOf(context, R.color.chip_blue)
            }

            itemBinding.checked.text = context.getString(R.string.schedule_chip_checked, schedule.checked)
            itemBinding.checked.isVisible = result is Success
            itemBinding.found.text = context.getString(R.string.schedule_chip_found, schedule.found)
            itemBinding.found.isVisible = result is Success
            itemBinding.unavailable.text = context.getString(R.string.schedule_chip_unavailable, schedule.unavailable)
            itemBinding.unavailable.isVisible = result is Success && schedule.unavailable > 0
            itemBinding.notified.text = context.getString(R.string.schedule_chip_notified, schedule.notified)
            itemBinding.notified.isVisible = result is Success

            itemBinding.description.isVisible = when (result) {
                is Failed -> true
                is Skipped -> true
                else -> false
            }
            itemBinding.description.text = when (result) {
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
            }
        }
    }

    class SchedulesAdapter(val context: Context) : RecyclerView.Adapter<ScheduleViewHolder>() {
        var schedules: List<Schedule> = listOf()
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
            val binding = ListItemScheduleBinding.inflate(LayoutInflater.from(context), parent, false)
            return ScheduleViewHolder(binding)
        }

        override fun getItemCount() = schedules.size

        override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
            holder.apply(itemCount - position, schedules[position])
        }
    }

    private lateinit var binding: ActivityUserLogBinding
    override val layoutView: View
        get() {
            binding = ActivityUserLogBinding.inflate(layoutInflater)
            return binding.root
        }

    override val themeRes: Int
        get() = Theme(this, prefs).theme
    override val themeColors: CustomThemeColors
        get() = Theme(this, prefs).colors

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.list.layoutManager = LinearLayoutManager(this)
        val adapter = SchedulesAdapter(this)
        val schedules = database.schedules()
        lifecycleScope.launch {
            schedules.load().collectLatest {
                adapter.schedules = it
            }
        }
        binding.list.adapter = adapter
    }
}