package com.billexplain.app.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.billexplain.app.database.DatabaseHelper;
import com.billexplain.app.databinding.ActivityAiHubBinding;
import com.billexplain.app.models.Bill;
import com.billexplain.app.models.User;
import com.billexplain.app.utils.SessionManager;
import java.util.List;

public class AIHubActivity extends AppCompatActivity {
    private ActivityAiHubBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAiHubBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        DatabaseHelper db = DatabaseHelper.getInstance(this);
        SessionManager session = new SessionManager(this);
        User user = db.getUserById(session.getUserId());
        List<Bill> bills = db.getBillsForUser(user.id);

        // Quick stats
        if (!bills.isEmpty()) {
            binding.tvStatThisMonth.setText(String.format("₱%,.0f", bills.get(0).totalAmount));
            if (bills.size() > 1)
                binding.tvStatLastMonth.setText(String.format("₱%,.0f", bills.get(1).totalAmount));
        }

        // 6-month average
        float sum = 0; int count = Math.min(bills.size(), 6);
        for (int i = 0; i < count; i++) sum += bills.get(i).totalAmount;
        if (count > 0) binding.tvStatAvg.setText(String.format("₱%,.0f", sum / count));

        binding.btnBack.setOnClickListener(v -> finish());
        binding.cardChat.setOnClickListener(v -> {
            Intent i = new Intent(this, ChatActivity.class);
            i.putExtra("user_name", user.name);
            startActivity(i);
        });
        binding.cardAnalytics.setOnClickListener(v ->
                startActivity(new Intent(this, AnalyticsActivity.class)));

        // Bottom nav
        binding.navHome.setOnClickListener(v -> finish());
        binding.navHistory.setOnClickListener(v ->
                startActivity(new Intent(this, TransactionHistoryActivity.class)));
        binding.navPay.setOnClickListener(v ->
                startActivity(new Intent(this, PaymentActivity.class)));
        binding.navAi.setOnClickListener(v -> { /* already here */ });
        binding.navSettings.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));
    }
}
