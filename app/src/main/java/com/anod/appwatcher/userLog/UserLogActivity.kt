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
import com.anod.appwatcher.databinding.ActivityUserLogBinding
import com.anod.appwatcher.utils.Theme
import com.anod.appwatcher.utils.prefs
import info.anodsplace.framework.app.CustomThemeColors
import info.anodsplace.framework.app.ToolbarActivity
import org.koin.core.component.KoinComponent

/**
 * @author Alex Gavrishev
 * @date 04/01/2018
 */
class UserLogActivity : ToolbarActivity(), KoinComponent {

    class UserLogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val lineNumber: TextView = itemView.findViewById(R.id.lineNumber)
        private val messageView: TextView = itemView.findViewById(R.id.messageView)

        private var textColor: Int? = null
        fun apply(position: Int, message: Message) {
            lineNumber.text = "$position"
            messageView.text = "${message.timestamp} ${message.message}"

            if (textColor == null) {
                textColor = messageView.textColors.defaultColor
            }

            if (message.level > Log.WARN) {
                messageView.setTextColor(ContextCompat.getColor(messageView.context, android.R.color.holo_red_dark))
            } else {
                messageView.setTextColor(textColor!!)
            }
        }
    }

    class UserLogAdapter(private val userLogger: UserLogger, val context: Context) : RecyclerView.Adapter<UserLogViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserLogViewHolder {
            val view = LayoutInflater.from(context).inflate(R.layout.list_item_log, parent, false)
            return UserLogViewHolder(view)
        }

        override fun getItemCount() = userLogger.messages.size

        override fun onBindViewHolder(holder: UserLogViewHolder, position: Int) {
            holder.apply(itemCount - position, userLogger.messages[position])
        }
    }

    private lateinit var binding: ActivityUserLogBinding
    override val themeRes: Int
        get() = Theme(this, prefs).theme
    override val themeColors: CustomThemeColors
        get() = Theme(this, prefs).colors


    override val layoutView: View
        get() {
            binding = ActivityUserLogBinding.inflate(layoutInflater)
            return binding.root
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.list.layoutManager = LinearLayoutManager(this)
        binding.list.adapter = UserLogAdapter(UserLogger(), this)
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