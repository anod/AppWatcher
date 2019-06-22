package com.anod.appwatcher.userLog

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anod.appwatcher.R
import com.anod.appwatcher.utils.Theme
import info.anodsplace.framework.app.CustomThemeColors
import info.anodsplace.framework.app.ToolbarActivity
import kotlinx.android.synthetic.main.activity_user_log.*


/**
 * @author Alex Gavrishev
 * @date 04/01/2018
 */
class UserLogActivity: ToolbarActivity() {

    class UserLogViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private var textColor: Int? = null
        fun apply(message: Message) {
            val tv = itemView as TextView
            tv.text = "${message.timestamp} ${message.message}"

            if (textColor == null) {
                textColor = tv.textColors.defaultColor
            }

            if (message.level > Log.WARN) {
                tv.setTextColor(ContextCompat.getColor(tv.context, android.R.color.holo_red_dark))
            } else {
                tv.setTextColor(textColor!!)
            }
        }
    }

    class UserLogAdapter(private val userLogger: UserLogger, val context: Context): RecyclerView.Adapter<UserLogViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserLogViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.list_item_log, parent, false)
            return UserLogViewHolder(view)
        }

        override fun getItemCount(): Int {
            return userLogger.messages.size
        }

        override fun onBindViewHolder(holder: UserLogViewHolder, position: Int) {
            holder.apply(userLogger.messages[position])
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
        list.adapter = UserLogAdapter(UserLogger(), this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.app_log, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_share) {
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_TITLE, "AppWatcher Log")
            sendIntent.putExtra(Intent.EXTRA_TEXT, UserLogger().content)
            sendIntent.type = "text/plain"
            startActivity(sendIntent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}