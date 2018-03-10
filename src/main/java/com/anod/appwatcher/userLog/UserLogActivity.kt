package com.anod.appwatcher.userLog

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.util.LruCache
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.widget.TextView
import com.anod.appwatcher.App
import com.anod.appwatcher.R
import info.anodsplace.framework.app.ToolbarActivity
import kotlinx.android.synthetic.main.activity_user_log.*
import java.text.SimpleDateFormat


/**
 * @author algavris
 * @date 04/01/2018
 */
class UserLogActivity: ToolbarActivity() {

    class UserLogViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val format = SimpleDateFormat.getDateTimeInstance()
        private val levels = mapOf(
                Log.INFO to "INFO",
                Log.ERROR to "ERROR",
                Log.VERBOSE to "VERBOSE",
                Log.DEBUG to "DEBUG",
                Log.ASSERT to "ASSERT",
                Log.WARN to "WARN"
        )

        fun apply(message: Message) {
            val tv = itemView as TextView
            tv.text = "${format.format(message.date)} - ${message.message}"
            if (message.level > Log.WARN) {
                tv.setTextColor(itemView.context.resources.getColor(android.R.color.holo_red_dark))
            } else {
                tv.setTextColor(itemView.context.resources.getColor(android.R.color.white))
            }
        }
    }

    class UserLogAdapter(private val userLogger: UserLogger, val context: Context): RecyclerView.Adapter<UserLogViewHolder>() {
        val cacheSize = 1 * 1024 * 1024 // 1MiB
        private val messagesCache = object: LruCache<Int, Message?>(cacheSize) {
            override fun sizeOf(key: Int?, value: Message?): Int {
                return value?.asBytes?.size ?: 0
            }
        }

        init {
            userLogger.iterator.asSequence().take(2000).forEachIndexed {
                index, message -> messagesCache.put(index, message)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): UserLogViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.list_item_log, parent, false)
            return UserLogViewHolder(view)
        }

        override fun getItemCount(): Int {
            return userLogger.count
        }

        override fun onBindViewHolder(holder: UserLogViewHolder, position: Int) {
            val cached = messagesCache[position]
            if (cached == null) {
                if (position < userLogger.count) {
                    val userMessage = userLogger.iterator.asSequence().elementAt(position)
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

        list.layoutManager = LinearLayoutManager(this)
        list.adapter = UserLogAdapter(App.log(this), this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.app_log, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_share) {
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_TITLE, "AppWatcher Log")
            sendIntent.putExtra(Intent.EXTRA_TEXT, App.log(this).content)
            sendIntent.type = "text/plain"
            startActivity(sendIntent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}