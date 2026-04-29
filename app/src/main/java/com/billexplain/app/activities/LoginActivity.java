package com.billexplain.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.billexplain.app.database.DatabaseHelper;
import com.billexplain.app.databinding.ActivityLoginBinding;
import com.billexplain.app.models.User;
import com.billexplain.app.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private DatabaseHelper db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = DatabaseHelper.getInstance(this);
        session = new SessionManager(this);

        binding.btnSignIn.setOnClickListener(v -> attemptLogin());
        binding.tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void attemptLogin() {
        String email = binding.etEmail.getText().toString().trim();
        String pass = binding.etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) { binding.etEmail.setError("Enter email"); return; }
        if (TextUtils.isEmpty(pass))  { binding.etPassword.setError("Enter password"); return; }

        User user = db.loginUser(email, pass);
        if (user != null) {
            session.saveSession(user.id);
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        // Block back — must log in
        finishAffinity();
    }
}
