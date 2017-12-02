package com.anod.appwatcher.backup

import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.v4.app.ListFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.anod.appwatcher.ListExportActivity
import com.anod.appwatcher.R
import info.anodsplace.appwatcher.framework.ApplicationContext
import java.io.File
import java.util.*

class ListExportFragment : ListFragment(), ImportTask.Listener {
    private lateinit var backupManager: DbBackupManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_restore_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        backupManager = (activity as ListExportActivity).backupManager
        listAdapter = ImportListAdapter(context!!, R.layout.list_item_restore, ArrayList(), ImportClickListener(), DeleteClickListener())
        reload()
    }

    fun reload() {
        FileListTask(listAdapter as ImportListAdapter, backupManager).execute(0)
    }

    private class FileListTask(
            val adapter: ImportListAdapter,
            val backupManager: DbBackupManager)
        : AsyncTask<Int, Void, Array<File>>() {

        override fun doInBackground(vararg params: Int?): Array<File> {
            return backupManager.fileList ?: emptyArray()
        }

        override fun onPostExecute(result: Array<File>?) {
            adapter.clear()
            if (result != null) {
                for (aResult in result) {
                    adapter.add(aResult)
                }
            }
            adapter.notifyDataSetChanged()
        }
    }

    override fun onImportFinish(code: Int) {
        //Avoid crash when fragment not attached to activity
        if (!isAdded) {
            return
        }

        ImportTask.showImportFinishToast(context!!, code)
        activity?.supportFragmentManager?.popBackStack()
    }

    private class ImportListAdapter internal constructor(
            context: Context,
            @LayoutRes private val resource: Int,
            items: ArrayList<File>,
            val restoreListener: ImportClickListener,
            val deleteListener: DeleteClickListener)
        : ArrayAdapter<File>(context, resource, items) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var v = convertView
            if (v == null) {
                val vi = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                v = vi.inflate(resource, null)
            }
            val entry = getItem(position)

            val titleView = v!!.findViewById<View>(android.R.id.title) as TextView
            var name = entry!!.name
            name = name.substring(0, name.lastIndexOf(DbBackupManager.FILE_EXT_DAT))
            titleView.tag = name
            titleView.text = name

            titleView.setOnClickListener(restoreListener)
            val applyView = v.findViewById<View>(R.id.apply_icon) as ImageView
            applyView.tag = name
            applyView.setOnClickListener(restoreListener)

            val deleteView = v.findViewById<View>(R.id.delete_action_button) as ImageView
            deleteView.tag = entry
            deleteView.setOnClickListener(deleteListener)

            v.id = position
            return v
        }
    }

    private inner class ImportClickListener : View.OnClickListener {
        override fun onClick(v: View) {
            val file = DbBackupManager.getBackupFile(v.tag as String)
            val uri = Uri.fromFile(file)
            ImportTask(ApplicationContext(context!!), this@ListExportFragment).execute(uri)
        }
    }

    private inner class DeleteClickListener : View.OnClickListener {
        override fun onClick(v: View) {
            val file = v.tag as File
            DeleteTask(ApplicationContext(context!!), listAdapter as ImportListAdapter, backupManager).execute(file)
        }
    }

    private class DeleteTask(
            private val context: ApplicationContext,
            val adapter: ImportListAdapter,
            val backupManager: DbBackupManager) : AsyncTask<File, Void, Boolean>() {

        override fun doInBackground(vararg files: File): Boolean {
            return files[0].delete()
        }

        override fun onPostExecute(result: Boolean) {
            if (result) {
                Toast.makeText(context.actual, R.string.unable_delete_file, Toast.LENGTH_SHORT).show()
            } else {
                FileListTask(adapter, backupManager).execute(0)
            }
        }
    }
}
