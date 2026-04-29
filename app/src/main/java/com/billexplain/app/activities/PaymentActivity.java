package com.billexplain.app.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.billexplain.app.R;
import com.billexplain.app.database.DatabaseHelper;
import com.billexplain.app.databinding.ActivityPaymentBinding;
import com.billexplain.app.models.Bill;
import com.billexplain.app.models.User;
import com.billexplain.app.utils.SessionManager;
import java.util.List;

public class PaymentActivity extends AppCompatActivity {
    private ActivityPaymentBinding binding;
    private String selectedMethod = "GCash";
    private Bill bill;
    private DatabaseHelper db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPaymentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = DatabaseHelper.getInstance(this);
        session = new SessionManager(this);

        User user = db.getUserById(session.getUserId());
        List<Bill> bills = db.getBillsForUser(session.getUserId());

        int billId = getIntent().getIntExtra("bill_id", -1);
        for (Bill b : bills) { if (b.id == billId) { bill = b; break; } }
        if (bill == null && !bills.isEmpty()) bill = bills.get(0);
        if (bill == null) { finish(); return; }

        // Provider name above ref
        binding.tvProvider.setText(user.provider != null ? user.provider : "CEPALCO");
        binding.tvRef.setText("Ref: BE-0" + bill.year + bill.id + "-001");

        // Payment method chips
        int[] ids = {R.id.chipBpi, R.id.chipLandbank, R.id.chipUnionbank,
                R.id.chipMaya, R.id.chipGcash, R.id.chipPaypal};
        String[] methods = {"BPI", "Landbank", "UnionBank", "Maya", "GCash", "PayPal"};

        for (int i = 0; i < ids.length; i++) {
            final String method = methods[i];
            final int id = ids[i];
            findViewById(id).setOnClickListener(v -> {
                selectedMethod = method;
                for (int j : ids)
                    ((android.widget.TextView) findViewById(j))
                            .setBackgroundResource(R.drawable.chip_default);
                ((android.widget.TextView) v).setBackgroundResource(R.drawable.chip_selected);
            });
        }

        // Already paid state
        if (bill.status.equals("Paid")) {
            binding.tvPayAmount.setText("₱0");
            binding.btnConfirmPay.setText("Already Paid");
            binding.btnConfirmPay.setAlpha(0.5f);
            binding.btnConfirmPay.setOnClickListener(v ->
                    Toast.makeText(this, "✅ Already paid for this month!", Toast.LENGTH_LONG).show());
        } else {
            binding.tvPayAmount.setText(String.format("₱%,.0f", bill.totalAmount));
            ((android.widget.TextView) findViewById(R.id.chipGcash))
                    .setBackgroundResource(R.drawable.chip_selected);
            binding.btnConfirmPay.setOnClickListener(v -> {
                db.addTransaction(session.getUserId(),
                        bill.getMonthYear() + " Bill",
                        bill.dueDate + " · Via " + selectedMethod,
                        bill.totalAmount, selectedMethod, "bill");
                db.markBillAsPaid(bill.id);
                Toast.makeText(this, "✅ Payment of ₱" +
                                String.format("%,.0f", bill.totalAmount) + " confirmed!",
                        Toast.LENGTH_LONG).show();
                finish();
            });
        }

        binding.btnBack.setOnClickListener(v -> finish());
    }
}