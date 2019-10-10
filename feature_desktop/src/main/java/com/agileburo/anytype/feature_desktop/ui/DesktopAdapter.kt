package com.agileburo.anytype.feature_desktop.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.feature_desktop.R
import com.agileburo.anytype.feature_desktop.mvvm.DesktopView
import com.agileburo.anytype.feature_desktop.utils.DesktopDiffUtil

class DesktopAdapter(
    private val data : MutableList<DesktopView>,
    private val onDocumentClicked : (DesktopView.Document) -> Unit
) : RecyclerView.Adapter<DesktopAdapter.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_DOCUMENT = 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when(viewType) {
            VIEW_TYPE_DOCUMENT -> {
                inflater.inflate(R.layout.item_desktop_page, parent, false).let {
                    ViewHolder.DocumentHolder(it)
                }
            }
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (data[position]) {
            is DesktopView.Document -> VIEW_TYPE_DOCUMENT
            else -> throw IllegalStateException("Unexpected type")
        }
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when(holder) {
            is ViewHolder.DocumentHolder -> {
                holder.bind(
                    doc = data[position] as DesktopView.Document,
                    onClick = onDocumentClicked
                )
            }
        }
    }

    sealed class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        class DocumentHolder(itemView: View) : ViewHolder(itemView) {

            fun bind(doc : DesktopView.Document, onClick: (DesktopView.Document) -> Unit) {
                itemView.setOnClickListener { onClick(doc) }
            }
        }
    }

    fun update(views : List<DesktopView>) {

        val callback = DesktopDiffUtil(
            old = data,
            new = views
        )
        val result = DiffUtil.calculateDiff(callback)

        data.apply {
            clear()
            addAll(views)
        }

        result.dispatchUpdatesTo(this)
    }
}