package info.anodsplace.framework.widget.recyclerview

import android.support.v7.widget.RecyclerView


import java.util.Collections
import java.util.Comparator

abstract class ArrayAdapter<T : Any, VH : RecyclerView.ViewHolder>(private val objects: MutableList<T>) : RecyclerView.Adapter<VH>() {

    /**
     * Adds the specified object at the end of the array.
     *
     * @param object The object to add at the end of the array.
     */
    open fun add(`object`: T) {
        objects.add(`object`)
        notifyItemInserted(objects.size - 1)
    }

    /**
     *
     * @param objects List of objects to be added
     */
    open fun addAll(objects: List<T>) {
        val count = this.objects.size
        this.objects.addAll(objects)
        notifyItemRangeInserted(count, objects.size)
    }

    /**
     * Remove all elements from the list.
     */
    open fun clear() {
        objects.clear()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return objects.size
    }

    open fun getItem(position: Int): T {
        return objects[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    /**
     * Returns the position of the specified item in the array.
     *
     * @param item The item to retrieve the position of.
     * @return The position of the specified item.
     */
    open fun getPosition(item: T): Int {
        return objects.indexOf(item)
    }

    /**
     * Inserts the specified object at the specified index in the array.
     *
     * @param object The object to insert into the array.
     * @param index  The index at which the object must be inserted.
     */
    open fun insert(`object`: T, index: Int) {
        objects.add(index, `object`)
        notifyItemInserted(index)
    }

    /**
     * Removes the specified object from the array.
     *
     * @param object The object to remove.
     */
    open fun remove(`object`: T) {
        val position = getPosition(`object`)
        objects.remove(`object`)
        notifyItemRemoved(position)
    }

    /**
     * Sorts the content of this adapter using the specified comparator.
     *
     * @param comparator The comparator used to sort the objects contained in this adapter.
     */
    open fun sort(comparator: Comparator<in T>) {
        Collections.sort(objects, comparator)
        notifyItemRangeChanged(0, objects.size)
    }

}