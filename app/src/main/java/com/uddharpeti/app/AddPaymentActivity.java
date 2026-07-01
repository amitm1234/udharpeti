package com.uddharpeti.app;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class AddPaymentActivity extends AppCompatActivity {

    private TextInputEditText etAmount, etNote;
    private MaterialButton btnInterest, btnPrincipal, btnDatePicker, btnSavePayment;
    private TextView tvPersonName, tvTotalRemaining, tvPreviewType, tvPreviewAmount, tvPreviewRemaining;
    
    private String personId, personName, loanStartDate;
    private double currentRemainingAmount;
    private String paymentType = "interest";
    private String selectedDate = "";
    
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_payment);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        personId = getIntent().getStringExtra("personId");
        personName = getIntent().getStringExtra("personName");
        loanStartDate = getIntent().getStringExtra("startDate");
        currentRemainingAmount = getIntent().getDoubleExtra("remainingAmount", 0);

        initViews();
        setupListeners();
        setDefaultDate();
        updatePreview();
    }

    private void initViews() {
        etAmount = findViewById(R.id.etAmount);
        etNote = findViewById(R.id.etNote);
        btnInterest = findViewById(R.id.btnInterest);
        btnPrincipal = findViewById(R.id.btnPrincipal);
        btnDatePicker = findViewById(R.id.btnDatePicker);
        btnSavePayment = findViewById(R.id.btnSavePayment);
        
        tvPersonName = findViewById(R.id.tvPersonName);
        tvTotalRemaining = findViewById(R.id.tvTotalRemaining);
        tvPreviewType = findViewById(R.id.tvPreviewType);
        tvPreviewAmount = findViewById(R.id.tvPreviewAmount);
        tvPreviewRemaining = findViewById(R.id.tvPreviewRemaining);

        tvPersonName.setText(personName);
        tvTotalRemaining.setText(String.format("एकूण बाकी: ₹%.2f", currentRemainingAmount));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupListeners() {
        btnInterest.setOnClickListener(v -> {
            paymentType = "interest";
            updateToggleStyles(btnInterest, btnPrincipal);
            updatePreview();
        });

        btnPrincipal.setOnClickListener(v -> {
            paymentType = "principal";
            updateToggleStyles(btnPrincipal, btnInterest);
            updatePreview();
        });

        btnDatePicker.setOnClickListener(v -> showDatePicker());

        etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePreview();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnSavePayment.setOnClickListener(v -> savePayment());
    }

    private void updateToggleStyles(MaterialButton selected, MaterialButton unselected) {
        selected.setBackgroundTintList(getColorStateList(R.color.accent_gold));
        selected.setTextColor(getColor(R.color.bg_dark));
        
        unselected.setBackgroundTintList(getColorStateList(R.color.card_bg));
        unselected.setTextColor(getColor(R.color.text_secondary));
    }

    private void setDefaultDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        selectedDate = sdf.format(new Date());
        btnDatePicker.setText(selectedDate);
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        
        long minDate = System.currentTimeMillis();
        if (loanStartDate != null && !loanStartDate.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date date = sdf.parse(loanStartDate);
                if (date != null) {
                    minDate = date.getTime();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        DatePickerDialog picker = new DatePickerDialog(
                this,
                android.R.style.Theme_Holo_Dialog_MinWidth,
                (view, year, month, dayOfMonth) -> {
                    cal.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    selectedDate = sdf.format(cal.getTime());
                    btnDatePicker.setText(selectedDate);
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));

        // Logic for min date
        picker.getDatePicker().setMinDate(minDate);

        // Spinner style set करा
        picker.getDatePicker().setCalendarViewShown(false);
        picker.getDatePicker().setSpinnersShown(true);
        picker.show();
    }

    private void updatePreview() {
        String amountStr = etAmount.getText().toString();
        double amount = amountStr.isEmpty() ? 0 : Double.parseDouble(amountStr);
        
        tvPreviewType.setText("Payment प्रकार: " + (paymentType.equals("interest") ? "व्याज (Interest)" : "मुद्दल (Principal)"));
        tvPreviewAmount.setText(String.format("रक्कम: ₹%.2f", amount));
        tvPreviewRemaining.setText(String.format("Payment नंतर बाकी: ₹%.2f", currentRemainingAmount - amount));
    }

    private void savePayment() {
        String amountStr = etAmount.getText().toString().trim();
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "रक्कम टाका", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        String note = etNote.getText().toString().trim();
        String userId = auth.getUid();

        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("amount", amount);
        paymentData.put("paymentType", paymentType);
        paymentData.put("paymentDate", selectedDate);
        paymentData.put("note", note);
        paymentData.put("createdAt", FieldValue.serverTimestamp());

        db.collection("users").document(userId).collection("persons").document(personId)
                .collection("payments").add(paymentData)
                .addOnSuccessListener(ref -> {
                    Intent intent = new Intent(this, ConfirmationActivity.class);
                    intent.putExtra("amount", amount);
                    intent.putExtra("personName", personName);
                    intent.putExtra("paymentType", paymentType);
                    intent.putExtra("date", selectedDate);
                    intent.putExtra("personId", personId);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
