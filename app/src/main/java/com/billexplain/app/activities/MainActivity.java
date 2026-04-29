package com.billexplain.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.billexplain.app.R;
import com.billexplain.app.database.DatabaseHelper;
import com.billexplain.app.databinding.ActivityMainBinding;
import com.billexplain.app.models.Bill;
import com.billexplain.app.models.User;
import com.billexplain.app.utils.SessionManager;

import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private DatabaseHelper db;
    private SessionManager session;
    private User currentUser;
    private List<Bill> bills;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = DatabaseHelper.getInstance(this);
        session = new SessionManager(this);

        if (!session.isLoggedIn()) {
            goToLogin();
            return;
        }

        loadUser();
        setupUI();
        setupNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUser(); // refresh premium state
        setupUI();
    }

    private void loadUser() {
        currentUser = db.getUserById(session.getUserId());
        bills = db.getBillsForUser(currentUser.id);
    }

    private void setupUI() {
        // Greeting
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting = hour < 12 ? "Good morning" : hour < 17 ? "Good afternoon" : "Good evening";
        binding.tvGreeting.setText(greeting);
        binding.tvUserName.setText(currentUser.name);

        // Avatar initial
        binding.tvAvatar.setText(String.valueOf(currentUser.name.charAt(0)).toUpperCase());

        // Hero bill card
        if (!bills.isEmpty()) {
            Bill latest = bills.get(0);
            binding.tvHeroAmount.setText(String.format("₱%,.0f", latest.totalAmount));

            if (latest.status.equals("Paid")) {
                binding.tvHeroAmount.setText("₱0");
                binding.tvHeroBillVs.setText("You're all caught up this month ✓");
                binding.tvDueTag.setText("✅ Already paid for this month");
            } else {
                binding.tvHeroAmount.setText(String.format("₱%,.0f", latest.totalAmount));
                float prev = bills.size() > 1 ? bills.get(1).totalAmount : 0;
                if (prev > 0) {
                    float diff = ((latest.totalAmount - prev) / prev) * 100;
                    String sign = diff > 0 ? "↑" : "↓";
                    binding.tvHeroBillVs.setText(String.format("vs ₱%,.0f last month %s%.1f%%",
                            prev, sign, Math.abs(diff)));
                }
                binding.tvDueTag.setText("⚡ Due in 2 days");
            }
        }

        // Recent bills (top 3)
        if (bills.size() > 0) setupBillRow(binding.getRoot().findViewById(R.id.layoutBill1), bills.get(0));
        if (bills.size() > 1) setupBillRow(binding.getRoot().findViewById(R.id.layoutBill2), bills.get(1));
        if (bills.size() > 2) setupBillRow(binding.getRoot().findViewById(R.id.layoutBill3), bills.get(2));

        // Premium lock on AI nav
        if (currentUser.isPremium) {
            binding.ivAiLock.setVisibility(View.GONE);
        } else {
            binding.ivAiLock.setVisibility(View.VISIBLE);
        }
    }

    private void setupBillRow(View row, Bill bill) {
        TextView tvMonth  = row.findViewById(R.id.tvBillMonth);
        TextView tvDate   = row.findViewById(R.id.tvBillDate);
        TextView tvAmount = row.findViewById(R.id.tvBillAmount);
        TextView tvStatus = row.findViewById(R.id.tvBillStatus);

        tvMonth.setText(bill.getMonthYear());
        tvDate.setText("Paid on " + bill.dueDate.split(",")[0]);
        tvAmount.setText(String.format("₱%,.0f", bill.totalAmount));
        tvStatus.setText(bill.status);

        if (bill.status.equals("Paid")) {
            tvStatus.setBackgroundResource(R.drawable.tag_green);
            tvStatus.setTextColor(getColor(R.color.green));
        } else {
            tvStatus.setBackgroundResource(R.drawable.tag_red);
            tvStatus.setTextColor(getColor(R.color.red));
        }

        row.setOnClickListener(v -> {
            Intent i = new Intent(this, BillDetailActivity.class);
            i.putExtra("bill_id", bill.id);
            startActivity(i);
        });
    }

    private void setupNavigation() {
        // Hero card → bill detail
        binding.cardHeroBill.setOnClickListener(v -> {
            if (!bills.isEmpty()) {
                Intent i = new Intent(this, BillDetailActivity.class);
                i.putExtra("bill_id", bills.get(0).id);
                startActivity(i);
            }
        });

        // Premium banner
        binding.cardPremiumBanner.setOnClickListener(v ->
                startActivity(new Intent(this, PremiumActivity.class)));

        // Bottom nav
        binding.navHome.setOnClickListener(v -> { /* already here */ });
        binding.navHistory.setOnClickListener(v ->
                startActivity(new Intent(this, TransactionHistoryActivity.class)));
        binding.navPay.setOnClickListener(v -> {
            if (!bills.isEmpty()) {
                Intent i = new Intent(this, PaymentActivity.class);
                i.putExtra("bill_id", bills.get(0).id);
                startActivity(i);
            }
        });
        binding.navAi.setOnClickListener(v -> {
            if (currentUser.isPremium) {
                startActivity(new Intent(this, AIHubActivity.class));
            } else {
                startActivity(new Intent(this, PremiumActivity.class));
            }
        });
        binding.navSettings.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));
    }

    private void goToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        finishAffinity(); // exit app from home
    }
}
