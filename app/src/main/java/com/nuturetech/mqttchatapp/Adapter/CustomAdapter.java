package com.nuturetech.mqttchatapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.github.library.bubbleview.BubbleTextView;
import com.nuturetech.mqttchatapp.R;
import com.nuturetech.mqttchatapp.model.ChatModel;

import java.util.List;

public class CustomAdapter extends BaseAdapter {

    private List<ChatModel> list_chat_models;
    private Context context;
    private LayoutInflater layoutInflater;

    public CustomAdapter(List<ChatModel> list_chat_models, Context context) {
        this.list_chat_models = list_chat_models;
        this.context = context;
        layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return list_chat_models.size();
    }

    @Override
    public Object getItem(int position) {
        return list_chat_models.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        View view = convertView;
        if (view == null){
            if (list_chat_models.get(position).isSend)
                view = layoutInflater.inflate(R.layout.list_item_message_send, null);
            else
                view = layoutInflater.inflate(R.layout.list_item_message_recv, null);

            BubbleTextView bubbleTextView = view.findViewById(R.id.text_message);
            bubbleTextView.setText(list_chat_models.get(position).message);
        }
        return view;
    }
}
