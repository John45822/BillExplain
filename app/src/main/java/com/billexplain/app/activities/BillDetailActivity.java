package com.billexplain.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.billexplain.app.database.DatabaseHelper;
import com.billexplain.app.databinding.ActivityBillDetailBinding;
import com.billexplain.app.models.Bill;
import com.billexplain.app.utils.SessionManager;
import java.util.List;

public class BillDetailActivity extends AppCompatActivity {
    private ActivityBillDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBillDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        DatabaseHelper db = DatabaseHelper.getInstance(this);
        SessionManager session = new SessionManager(this);
        List<Bill> bills = db.getBillsForUser(session.getUserId());

        // Get bill by id or use latest
        int billId = getIntent().getIntExtra("bill_id", -1);
        Bill bill = null;
        for (Bill b : bills) { if (b.id == billId) { bill = b; break; } }
        if (bill == null && !bills.isEmpty()) bill = bills.get(0);
        if (bill == null) { finish(); return; }

        final Bill finalBill = bill;

        binding.tvTitle.setText(bill.getMonthYear() + " Bill");
        binding.tvTotalAmount.setText(String.format("₱%,.0f", bill.totalAmount));
        binding.tvGenAmount.setText(String.format("₱%,.0f", bill.generationCharge));
        binding.tvTransAmount.setText(String.format("₱%,.0f", bill.transmissionCharge));
        binding.tvDistAmount.setText(String.format("₱%,.0f", bill.distributionCharge));
        binding.tvOtherAmount.setText(String.format("₱%,.0f", bill.otherCharges));
        binding.btnPay.setText(String.format("Pay ₱%,.0f", bill.totalAmount));

        binding.btnBack.setOnClickListener(v -> finish());
        if (bill.status.equals("Paid")) {
            binding.btnPay.setText("Already Paid");
            binding.btnPay.setAlpha(0.5f);
            binding.btnPay.setOnClickListener(v ->
                    Toast.makeText(this, "✅ Already paid for this month!", Toast.LENGTH_SHORT).show());
        } else {
            binding.btnPay.setOnClickListener(v -> {
                Intent i = new Intent(this, PaymentActivity.class);
                i.putExtra("bill_id", finalBill.id);
                startActivity(i);
            });
        }
    }
}
