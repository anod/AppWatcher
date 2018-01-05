package com.anod.appwatcher.userLog

import android.content.Context
import android.os.Bundle
import android.support.v4.util.LruCache
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.anod.appwatcher.App
import com.anod.appwatcher.R
import info.anodsplace.appwatcher.framework.ToolbarActivity
import kotlinx.android.synthetic.main.activity_user_log.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author algavris
 * @date 04/01/2018
 */
class UserLogActivity: ToolbarActivity() {

    class UserLogViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
        private val levels = mapOf(
                Log.INFO to "INFO",
                Log.ERROR to "ERROR",
                Log.VERBOSE to "VERBOSE",
                Log.DEBUG to "DEBUG",
                Log.ASSERT to "ASSERT",
                Log.WARN to "WARN"
        )

        fun apply(message: Message) {
            val level = levels[message.level] ?: "UNKNOWN"
            val tv = itemView as TextView
            tv.text = "${format.format(message.date)}: [$level] ${message.message}"
            if (message.level > Log.WARN) {
                tv.setTextColor(itemView.context.resources.getColor(android.R.color.holo_red_dark))
            } else {
                tv.setTextColor(itemView.context.resources.getColor(android.R.color.white))
            }
        }
    }

    class UserLogAdapter(private val userLogger: UserLogger, val context: Context): RecyclerView.Adapter<UserLogViewHolder>() {
        val cacheSize = 1 * 1024 * 1024; // 1MiB
        private val messagesCache = object: LruCache<Int, Message?>(cacheSize) {
            override fun sizeOf(key: Int?, value: Message?): Int {
                return value?.asBytes?.size ?: 0
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): UserLogViewHolder {
            val view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false)
            return UserLogViewHolder(view)
        }

        override fun getItemCount(): Int {
            return userLogger.count
        }

        override fun onBindViewHolder(holder: UserLogViewHolder, position: Int) {
            val cached = messagesCache[position]
            if (cached == null) {
                if (position < userLogger.count) {
                    val userMessage = userLogger.iterator().asSequence().elementAt(position)
                    messagesCache.put(position, userMessage)
                    holder.apply(userMessage)
                }
            } else {
                holder.apply(cached)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_log)
        setupToolbar()

        list.adapter = UserLogAdapter(App.log(this), this)
    }
}