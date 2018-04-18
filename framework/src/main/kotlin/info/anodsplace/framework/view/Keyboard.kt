package info.anodsplace.framework.view

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

/**
 * @author Alex Gavrishev
 * *
 * @date 27/08/2016.
 */

object Keyboard {

    fun hide(view: View, context: Context) {
        // hide virtual keyboard
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)

    }
}
