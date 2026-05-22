package com.uddharpeti.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class ConfirmationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        String personName = getIntent().getStringExtra("personName");
        String paymentType = getIntent().getStringExtra("paymentType");
        double amount = getIntent().getDoubleExtra("amount", 0);
        String date = getIntent().getStringExtra("date");
        String personId = getIntent().getStringExtra("personId");

        TextView tvPersonName = findViewById(R.id.tvPersonName);
        TextView tvType = findViewById(R.id.tvType);
        TextView tvAmount = findViewById(R.id.tvAmount);
        TextView tvDate = findViewById(R.id.tvDate);
        
        tvPersonName.setText(personName);
        tvType.setText(paymentType);
        tvAmount.setText(String.format("₹%.2f", amount));
        tvDate.setText(date);

        findViewById(R.id.btnViewDetails).setOnClickListener(v -> {
            Intent intent = new Intent(this, PersonDetailActivity.class);
            intent.putExtra("personId", personId);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.btnHome).setOnClickListener(v -> {
            // Assuming MainActivity exists
            try {
                Intent intent = new Intent(this, Class.forName("com.uddharpeti.app.MainActivity"));
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            } catch (ClassNotFoundException e) {
                finish();
            }
        });
    }
}
