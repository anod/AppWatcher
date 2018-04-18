package info.anodsplace.framework.widget.recyclerview

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

/**
 * @author alex
 * @date 2015-07-05
 */
class TopOffsetItemDecorator(private val topOffsetPixel: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State?) {
        val pos = parent.getChildAdapterPosition(view)
        if (pos == 0) {
            outRect.set(0, topOffsetPixel, 0, 0)
        }
    }
}