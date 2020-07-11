package com.fanhl.aidldemo.client;

import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.fanhl.aidldemo.Msg;

import java.util.Date;

public class MainAdapter extends BaseQuickAdapter<Msg, MainAdapter.ViewHolder> {
    public MainAdapter() {
        super(android.R.layout.simple_list_item_1);
    }

    @Override
    protected void convert(ViewHolder helper, Msg item) {
        helper.bind(item);
    }

    public static class ViewHolder extends BaseViewHolder {
        public ViewHolder(View view) {
            super(view);
        }

        public void bind(Msg data) {
            ((TextView) itemView.findViewById(android.R.id.text1)).setText(new Date(data.getTime()) + ": " + data.getMsg());
        }
    }
}
