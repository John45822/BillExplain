package com.billexplain.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.billexplain.app.R;
import com.billexplain.app.models.ChatMessage;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.VH> {
    private final List<ChatMessage> messages;

    public ChatAdapter(List<ChatMessage> messages) { this.messages = messages; }

    @Override public int getItemViewType(int pos) { return messages.get(pos).type; }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = viewType == ChatMessage.TYPE_AI
                ? R.layout.item_chat_ai : R.layout.item_chat_user;
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        h.tvMsg.setText(messages.get(pos).text);
    }

    @Override public int getItemCount() { return messages.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvMsg;
        VH(View v) { super(v); tvMsg = v.findViewById(R.id.tvMessage); }
    }
}
