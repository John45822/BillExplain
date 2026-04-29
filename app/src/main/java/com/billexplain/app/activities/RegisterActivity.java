package com.billexplain.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AppCompatActivity;
import com.billexplain.app.database.DatabaseHelper;
import com.billexplain.app.databinding.ActivityRegisterBinding;
import com.billexplain.app.utils.SessionManager;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private DatabaseHelper db;
    private SessionManager session;
    private String selectedProvider = "MERALCO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = DatabaseHelper.getInstance(this);
        session = new SessionManager(this);

        // Setup provider dropdown
        String[] providers = {"CEPALCO", "Meralco", "VECO"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, providers);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerProvider.setAdapter(adapter);
        binding.spinnerProvider.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(android.widget.AdapterView<?> parent,
                                               android.view.View view, int position, long id) {
                        selectedProvider = providers[position];
                    }
                    @Override
                    public void onNothingSelected(android.widget.AdapterView<?> parent) {}
                });

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnCreate.setOnClickListener(v -> attemptRegister());
        binding.tvSignIn.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        String name   = binding.etName.getText().toString().trim();
        String email  = binding.etEmail.getText().toString().trim();
        String meter  = binding.etMeter.getText().toString().trim();
        String pass   = binding.etPassword.getText().toString().trim();
        String pass2  = binding.etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name))  { binding.etName.setError("Enter your name"); return; }
        if (TextUtils.isEmpty(email)) { binding.etEmail.setError("Enter email"); return; }
        if (TextUtils.isEmpty(pass))  { binding.etPassword.setError("Enter password"); return; }
        if (pass.length() < 8)        { binding.etPassword.setError("Minimum 8 characters"); return; }
        if (!pass.equals(pass2))      { binding.etConfirmPassword.setError("Passwords do not match"); return; }
        if (db.emailExists(email))    { binding.etEmail.setError("Email already registered"); return; }

        long userId = db.registerUser(name, email, pass, meter, selectedProvider);
        if (userId > 0) {
            session.saveSession((int) userId);
            Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        } else {
            Toast.makeText(this, "Registration failed. Try again.", Toast.LENGTH_SHORT).show();
        }
    }
}
