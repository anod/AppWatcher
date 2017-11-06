package com.google.android.finsky.api.model

import com.android.volley.Response
import com.anod.appwatcher.BuildConfig
import com.google.android.finsky.api.DfeApi
import com.google.android.finsky.protos.nano.Messages
import com.google.android.finsky.protos.nano.Messages.Details

import java.util.ArrayList

import info.anodsplace.android.log.AppLog

class DfeBulkDetails(private val api: DfeApi,private val filter: FilterPredicate?) : DfeBaseModel() {
    private var bulkDetailsResponse: Details.BulkDetailsResponse? = null
    var docIds: List<String>? = null


    override fun execute(responseListener: Response.Listener<Messages.Response.ResponseWrapper>, errorListener: Response.ErrorListener) {
        api.details(docIds, true, responseListener, errorListener)
    }

    val documents: List<Document>?
        get() {
            val list: ArrayList<Document>?
            if (this.bulkDetailsResponse == null) {
                list = null
            } else {
                list = ArrayList()
                for (i in this.bulkDetailsResponse!!.entry.indices) {
                    val doc = this.bulkDetailsResponse!!.entry[i].doc
                    if (doc == null) {
                        if (BuildConfig.DEBUG) {
                            AppLog.d("Null document for requested docId: %s ", this.docIds!![i])
                        }
                    } else {
                        list.add(Document(doc))
                    }
                }
            }
            return if (filter == null || list == null) {
                list
            } else {
                list.filter(filter)
            }
        }

    override val isReady: Boolean
        get() = this.bulkDetailsResponse != null

    override fun onResponse(responseWrapper: Messages.Response.ResponseWrapper) {
        this.bulkDetailsResponse = responseWrapper.payload.bulkDetailsResponse
        this.notifyDataSetChanged()
    }

}