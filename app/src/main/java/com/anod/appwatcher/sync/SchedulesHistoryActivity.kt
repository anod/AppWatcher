// Copyright (c) 2020. Alex Gavrishev
package com.anod.appwatcher.sync

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anod.appwatcher.Application
import com.anod.appwatcher.R
import com.anod.appwatcher.database.entities.*
import com.anod.appwatcher.utils.Theme
import com.anod.appwatcher.utils.colorStateListOf
import info.anodsplace.framework.app.CustomThemeColors
import info.anodsplace.framework.app.ToolbarActivity
import kotlinx.android.synthetic.main.activity_user_log.*
import kotlinx.android.synthetic.main.list_item_schedule.view.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author Alex Gavrishev
 * @date 04/01/2018
 */
class SchedulesHistoryActivity : ToolbarActivity() {

    class ScheduleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val sdf = SimpleDateFormat("MMM d, hh:mm:ss", Locale.getDefault())
        private val context: Context
            get() = itemView.context
        private var textColor: Int? = null
        fun apply(position: Int, schedule: Schedule) {
            val range = if (schedule.finish > 0) {
                val duration = ((schedule.finish - schedule.start) / 1000.0).toInt()
                "${sdf.format(Date(schedule.start))} - ${sdf.format(Date(schedule.finish))} (${duration}s)"
            } else {
                "${sdf.format(Date(schedule.start))}"
            }
            itemView.time.text = range
            val result = schedule.result()
            itemView.status.text = when (result) {
                is New -> "New"
                is Success -> "Success"
                is Failed -> "Failed"
                is Skipped -> "Skipped"
            }
            itemView.status.chipBackgroundColor = when (result) {
                is New -> colorStateListOf(context, R.color.chip_blue)
                is Success -> colorStateListOf(context, R.color.chip_green)
                is Failed -> colorStateListOf(context, R.color.chip_orange)
                is Skipped -> colorStateListOf(context, R.color.chip_gray)
            }
            itemView.reason.text = when (schedule.reason) {
                Schedule.reasonSchedule -> "Schedule"
                Schedule.reasonManual -> "Manual"
                else -> "Unknown"
            }
            itemView.reason.chipBackgroundColor = when (schedule.reason) {
                Schedule.reasonSchedule -> colorStateListOf(context, R.color.chip_gray)
                Schedule.reasonManual -> colorStateListOf(context, R.color.chip_yellow)
                else -> colorStateListOf(context, R.color.chip_blue)
            }
            itemView.checked.text = "${schedule.checked} synced"
            itemView.checked.isVisible = result is Success
            itemView.found.text = "${schedule.found} updates"
            itemView.found.isVisible = result is Success
            itemView.unavailable.text = "${schedule.unavailable} not available"
            itemView.unavailable.isVisible = result is Success && schedule.unavailable > 0

            itemView.description.isVisible = when (result) {
                is Failed -> true
                is Skipped -> true
                else -> false
            }
            itemView.description.text = when (result) {
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

        private fun getString(@StringRes id: Int, vararg args: Any) = itemView.resources.getString(id, args)

    }

    class SchedulesAdapter(val context: Context) : RecyclerView.Adapter<ScheduleViewHolder>() {
        var schedules: List<Schedule> = listOf()
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.list_item_schedule, parent, false)
            return ScheduleViewHolder(view)
        }

        override fun getItemCount() = schedules.size

        override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
            holder.apply(itemCount - position, schedules[position])
        }
    }

    override val layoutResource: Int
        get() = R.layout.activity_user_log
    override val themeRes: Int
        get() = Theme(this).theme
    override val themeColors: CustomThemeColors
        get() = Theme(this).colors

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        list.layoutManager = LinearLayoutManager(this)
        val adapter = SchedulesAdapter(this)
        val schedules = Application.provide(this).database.schedules()
        schedules.load().observe(this, Observer {
            adapter.schedules = it
        })
        list.adapter = adapter
    }
}