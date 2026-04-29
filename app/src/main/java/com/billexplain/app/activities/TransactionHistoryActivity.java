package com.billexplain.app.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.billexplain.app.adapters.TransactionAdapter;
import com.billexplain.app.database.DatabaseHelper;
import com.billexplain.app.databinding.ActivityTransactionHistoryBinding;
import com.billexplain.app.models.Transaction;
import com.billexplain.app.utils.SessionManager;
import java.util.List;

public class TransactionHistoryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityTransactionHistoryBinding binding =
                ActivityTransactionHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        DatabaseHelper db = DatabaseHelper.getInstance(this);
        SessionManager session = new SessionManager(this);
        List<Transaction> txns = db.getTransactionsForUser(session.getUserId());

        binding.tvCount.setText(txns.size() + " records");
        binding.btnBack.setOnClickListener(v -> finish());

        // Total
        float total = 0;
        for (Transaction t : txns) total += t.amount;
        binding.tvTotal.setText(String.format("₱%,.0f", total));

        TransactionAdapter adapter = new TransactionAdapter(txns);
        binding.rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        binding.rvTransactions.setAdapter(adapter);
    }
}
