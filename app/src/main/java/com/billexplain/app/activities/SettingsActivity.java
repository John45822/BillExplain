package com.billexplain.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.billexplain.app.database.DatabaseHelper;
import com.billexplain.app.databinding.ActivitySettingsBinding;
import com.billexplain.app.models.User;
import com.billexplain.app.utils.SessionManager;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySettingsBinding binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnBack.setOnClickListener(v -> finish());

        DatabaseHelper db = DatabaseHelper.getInstance(this);
        SessionManager session = new SessionManager(this);
        User user = db.getUserById(session.getUserId());

        binding.tvMeterSub.setText(user.meterNumber != null ? user.meterNumber + " · Verified ✓" : "Not set");

        if (user.isPremium) {
            binding.cardPremiumBadge.setVisibility(View.VISIBLE);
        } else {
            binding.cardPremiumBadge.setVisibility(View.GONE);
        }

        // Toggle states
        binding.toggleBillAlerts.setChecked(true);
        binding.toggleReminders.setChecked(false);


        binding.btnLogout.setOnClickListener(v -> {
            session.logout();
            Intent i = new Intent(this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });
    }
}
