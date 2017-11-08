package com.anod.appwatcher.framework

import android.content.Intent

/**
* @author alex
* @date 7/9/14
*/
interface ActivityListener {

    interface ResultListener {
        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    }
}
