package com.fanhl.aidldemo.shared

import android.view.View
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import java.util.*

class MainAdapter : BaseQuickAdapter<Msg, MainAdapter.ViewHolder>(android.R.layout.simple_list_item_1) {
    override fun convert(helper: ViewHolder?, item: Msg?) {
        helper?.bind(item)
    }

    class ViewHolder(view: View?) : BaseViewHolder(view) {
        fun bind(data: Msg?) {
            data ?: return
            (itemView.findViewById<View>(android.R.id.text1) as TextView).text = Date(data.time).toString() + ": " + data.msg
        }
    }
}