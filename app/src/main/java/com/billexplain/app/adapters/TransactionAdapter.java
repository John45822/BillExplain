package com.billexplain.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.billexplain.app.R;
import com.billexplain.app.models.Transaction;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.VH> {
    private final List<Transaction> list;

    public TransactionAdapter(List<Transaction> list) { this.list = list; }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Transaction t = list.get(pos);
        h.tvName.setText(t.name);
        h.tvDate.setText(t.date);
        h.tvAmount.setText(String.format("-₱%,.0f", t.amount));

        // Icon background color by type
        if (t.type.equals("premium")) {
            h.tvIcon.setText("★");
            h.tvIcon.setBackgroundResource(R.drawable.icon_bg_purple);
        } else {
            h.tvIcon.setText("⚡");
            h.tvIcon.setBackgroundResource(R.drawable.icon_bg_blue);
        }
    }

    @Override public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvIcon, tvName, tvDate, tvAmount;
        VH(View v) {
            super(v);
            tvIcon   = v.findViewById(R.id.tvIcon);
            tvName   = v.findViewById(R.id.tvName);
            tvDate   = v.findViewById(R.id.tvDate);
            tvAmount = v.findViewById(R.id.tvAmount);
        }
    }
}
