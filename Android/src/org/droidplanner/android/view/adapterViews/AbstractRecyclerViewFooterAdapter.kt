package org.droidplanner.android.view.adapterViews

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup

/**
 * @author ne0fhyk (Fredia Huya-Kouadio)
 */
abstract class AbstractRecyclerViewFooterAdapter<T>(recyclerView: RecyclerView, onLoadMoreListener: OnLoadMoreListener?) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private companion object {
        private const val VISIBLE_THRESHOLD = 5

        private const val ITEM_VIEW_TYPE_BASIC = 0
        private const val ITEM_VIEW_TYPE_FOOTER = 1
    }

    private val dataSet = mutableListOf<T?>()

    private var firstVisibleItem = 0
    private var visibleItemCount = 0
    private var totalItemCount = 0
    private var previousTotal = 0
    private var loading = true

    private var hasMoreData = true

    init {
        if(recyclerView.layoutManager is LinearLayoutManager) {
            val layoutMgr = recyclerView.layoutManager as LinearLayoutManager
            recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener(){
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    totalItemCount = layoutMgr.getItemCount()
                    visibleItemCount = layoutMgr.getChildCount()
                    firstVisibleItem = layoutMgr.findFirstVisibleItemPosition()

                    if (loading) {
                        if (totalItemCount > previousTotal) {
                            loading = false
                            previousTotal = totalItemCount
                        }
                    }
                    if (hasMoreData && !loading && totalItemCount - visibleItemCount <= firstVisibleItem + VISIBLE_THRESHOLD) {
                        // End has been reached

                        addItem(null)
                        onLoadMoreListener?.onLoadMore()
                        loading = true
                    }
                }
            })
        }
    }

    fun setHasMoreData(hasMore: Boolean){
        hasMoreData = hasMore
        if(!hasMore){
            removeItem(null)
        }
    }

    fun getFirstVisibleItem(): Int {
        return firstVisibleItem
    }

    fun resetItems(newDataSet: List<T>?) {
        loading = true
        firstVisibleItem = 0
        visibleItemCount = 0
        totalItemCount = 0
        previousTotal = 0

        dataSet.clear()

        if(newDataSet != null) {
            dataSet.addAll(newDataSet)
        }
        notifyDataSetChanged()
    }

    fun getItems() = dataSet

    fun addItems(newDataSetItems: List<T>) {
        removeItem(null)

        val lastPos = dataSet.size
        dataSet.addAll(newDataSetItems)
        notifyItemRangeInserted(lastPos, newDataSetItems.size)
    }

    private fun addItem(item: T?) {
        if (!dataSet.contains(item)) {
            dataSet.add(item)
            notifyItemInserted(dataSet.size - 1)
        }
    }

    private fun removeItem(item: T?) {
        val indexOfItem = dataSet.indexOf(item)
        if (indexOfItem != -1) {
            this.dataSet.removeAt(indexOfItem)
            notifyItemRemoved(indexOfItem)
        }
    }

    fun getItem(index: Int): T {
        return dataSet[index]?: throw IllegalArgumentException("Item with index $index doesn't exist, dataSet is $dataSet")
    }

    override fun getItemViewType(position: Int): Int {
        return if (dataSet.get(position) != null) ITEM_VIEW_TYPE_BASIC else ITEM_VIEW_TYPE_FOOTER
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == ITEM_VIEW_TYPE_BASIC) {
            onBindBasicItemView(holder, position);
        } else {
            onBindFooterView(holder, position);
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == ITEM_VIEW_TYPE_BASIC) {
            return onCreateBasicItemViewHolder(parent, viewType);
        } else if (viewType == ITEM_VIEW_TYPE_FOOTER) {
            return onCreateFooterViewHolder(parent, viewType);
        } else {
            throw IllegalStateException("Invalid type, this type ot items " + viewType + " can't be handled");
        }
    }

    override fun getItemCount() = dataSet.size

    abstract fun onCreateBasicItemViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder

    abstract fun onBindBasicItemView(genericHolder: RecyclerView.ViewHolder, position: Int)

    abstract fun onCreateFooterViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder

    abstract fun onBindFooterView(genericHolder: RecyclerView.ViewHolder, position: Int)

}

interface OnLoadMoreListener {
    fun onLoadMore()

}
