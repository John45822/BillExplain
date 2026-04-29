package com.billexplain.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.billexplain.app.database.DatabaseHelper;
import com.billexplain.app.databinding.ActivityPremiumBinding;
import com.billexplain.app.utils.SessionManager;

public class PremiumActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityPremiumBinding binding = ActivityPremiumBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        DatabaseHelper db = DatabaseHelper.getInstance(this);
        SessionManager session = new SessionManager(this);

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnUnlock.setOnClickListener(v -> {
            boolean success = db.upgradeToPremium(session.getUserId());
            if (success) {
                Toast.makeText(this, "🌟 Premium activated! Welcome!", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(this, AIHubActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
            }
        });
    }
}
