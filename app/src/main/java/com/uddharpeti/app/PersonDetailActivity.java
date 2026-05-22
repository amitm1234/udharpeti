package com.uddharpeti.app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PersonDetailActivity extends AppCompatActivity {

    private TextView tvInitials, tvName, tvPhone, tvBadge, tvStatPrincipal, tvStatRate, tvStatInterest, tvStartDate, tvFormulaMonthly, tvFormulaYearly, tvFormulaCalc, tvRemainingAmount;
    private RecyclerView rvPayments;
    private PaymentAdapter adapter;
    private List<Payment> paymentList;
    private MaterialButton btnAddPayment;
    
    private String personId;
    private Person currentPerson;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private double totalInterest = 0;
    private double totalDue = 0;
    private double remaining = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_detail);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        personId = getIntent().getStringExtra("personId");

        initViews();
        loadPersonData();
        loadPayments();
    }

    private void initViews() {
        tvInitials = findViewById(R.id.tvInitials);
        tvName = findViewById(R.id.tvName);
        tvPhone = findViewById(R.id.tvPhone);
        tvBadge = findViewById(R.id.tvBadge);
        tvStatPrincipal = findViewById(R.id.tvStatPrincipal);
        tvStatRate = findViewById(R.id.tvStatRate);
        tvStatInterest = findViewById(R.id.tvStatInterest);
        tvStartDate = findViewById(R.id.tvStartDate);
        tvFormulaMonthly = findViewById(R.id.tvFormulaMonthly);
        tvFormulaYearly = findViewById(R.id.tvFormulaYearly);
        tvFormulaCalc = findViewById(R.id.tvFormulaCalc);
        tvRemainingAmount = findViewById(R.id.tvRemainingAmount);
        btnAddPayment = findViewById(R.id.btnAddPayment);

        rvPayments = findViewById(R.id.rvPayments);
        rvPayments.setLayoutManager(new LinearLayoutManager(this));
        paymentList = new ArrayList<>();
        adapter = new PaymentAdapter(paymentList);
        rvPayments.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        btnAddPayment.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddPaymentActivity.class);
            intent.putExtra("personId", personId);
            if (currentPerson != null) {
                intent.putExtra("personName", currentPerson.getName());
                intent.putExtra("startDate", currentPerson.getStartDate());
            }
            intent.putExtra("remainingAmount", remaining);
            startActivity(intent);
        });

        findViewById(R.id.btnCloseAccount).setOnClickListener(v -> showCloseAccountDialog());
    }

    private void loadPersonData() {
        String userId = auth.getUid();
        if (userId == null || personId == null) return;

        db.collection("users").document(userId).collection("persons").document(personId)
                .addSnapshotListener((doc, e) -> {
                    if (doc != null && doc.exists()) {
                        currentPerson = doc.toObject(Person.class);
                        if (currentPerson != null) {
                            currentPerson.setId(doc.getId());
                            updateUI(currentPerson);
                            calculateInterest(currentPerson);
                            
                            // Check status
                            if ("completed".equals(currentPerson.getStatus())) {
                                btnAddPayment.setVisibility(View.GONE);
                            } else {
                                btnAddPayment.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
    }

    private void updateUI(Person person) {
        tvName.setText(person.getName());
        tvPhone.setText(person.getPhone());
        tvInitials.setText(getInitials(person.getName()));
        tvStatPrincipal.setText(String.format("₹%.0f", person.getAmount()));
        tvStatRate.setText(String.format("%.1f%%", person.getInterestRate()));
        tvStartDate.setText(person.getStartDate());
        
        if ("lent".equals(person.getTransactionType())) {
            tvBadge.setText("दिलेले");
            tvBadge.setBackgroundResource(R.drawable.badge_bg_green);
        } else {
            tvBadge.setText("घेतलेले");
            tvBadge.setBackgroundResource(R.drawable.badge_bg_red);
        }
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "??";
        String[] parts = name.split(" ");
        if (parts.length > 1) {
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        }
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }

    private void calculateInterest(Person person) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date start = sdf.parse(person.getStartDate());
            Date now = new Date();
            
            long diffMs = now.getTime() - start.getTime();
            long days = diffMs / (1000 * 60 * 60 * 24);
            double months = days / 30.0;
            
            double principal = person.getAmount();
            double rate = person.getInterestRate();
            
            if ("yearly".equals(person.getInterestType())) {
                rate = rate / 12.0;
            }
            
            totalInterest = (principal * rate * months) / 100.0;
            totalDue = principal + totalInterest;
            remaining = totalDue;
            
            tvStatInterest.setText(String.format("₹%.2f", totalInterest));
            tvRemainingAmount.setText(String.format("₹%.2f", totalDue));
            
            double monthlyInterest = (principal * rate) / 100.0;
            tvFormulaMonthly.setText(String.format("₹%.2f/महिना", monthlyInterest));
            tvFormulaYearly.setText(String.format("₹%.2f/वर्ष", monthlyInterest * 12));
            tvFormulaCalc.setText(String.format(Locale.getDefault(), "%.0f × %.1f × %.1f ÷ 100 = %.2f", principal, rate, months, totalInterest));
            
            if (!paymentList.isEmpty()) {
                calculateRemaining(person, paymentList);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadPayments() {
        String userId = auth.getUid();
        db.collection("users").document(userId).collection("persons").document(personId)
                .collection("payments").orderBy("paymentDate", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        paymentList.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Payment payment = doc.toObject(Payment.class);
                            payment.setId(doc.getId());
                            paymentList.add(payment);
                        }
                        adapter.notifyDataSetChanged();
                        
                        if (currentPerson != null) {
                            calculateRemaining(currentPerson, paymentList);
                        }
                    }
                });
    }

    private void calculateRemaining(Person person, List<Payment> payments) {
        // Days calculate
        long days = getDaysDiff(person.getStartDate());
        double months = days / 30.0;
        double principal = person.getAmount();
        double rate = person.getInterestRate();

        // Yearly असेल तर monthly मध्ये convert करा
        if (person.getInterestType().equals("yearly")) {
            rate = rate / 12;
        }

        // Simple Interest
        totalInterest = (principal * rate * months) / 100.0;
        totalDue = principal + totalInterest;

        // सर्व payments subtract करा
        double totalPaid = 0;
        for (Payment payment : payments) {
            totalPaid += payment.getAmount();
        }

        remaining = totalDue - totalPaid;
        if (remaining < 0) remaining = 0;

        // UI update करा
        tvStatInterest.setText("₹" + String.format("%.2f", totalInterest));
        tvRemainingAmount.setText("₹" + String.format("%.2f", remaining));

        // Remaining 0 असेल तर green दाखवा
        if (remaining == 0) {
            tvRemainingAmount.setTextColor(Color.parseColor("#4CAF50"));
            tvRemainingAmount.setText("पूर्ण भरले ✓");
        } else {
            tvRemainingAmount.setTextColor(Color.parseColor("#F44336"));
        }
    }

    private long getDaysDiff(String startDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date start = sdf.parse(startDate);
            Date now = new Date();
            long diffMs = now.getTime() - start.getTime();
            return diffMs / (1000 * 60 * 60 * 24);
        } catch (Exception e) {
            return 0;
        }
    }

    private void showCloseAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle("खाते बंद करा")
                .setMessage("तुम्हानाला नक्की हे खाते बंद करायचे आहे का?")
                .setPositiveButton("हो", (dialog, which) -> {
                    db.collection("users").document(auth.getUid())
                            .collection("persons").document(personId)
                            .update("status", "completed")
                            .addOnSuccessListener(aVoid -> {
                                // Payment जोडा button लपवा
                                btnAddPayment.setVisibility(View.GONE);
                                Toast.makeText(PersonDetailActivity.this, "हिशोब पूर्ण झाला! ✓", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                })
                .setNegativeButton("नाही", null)
                .show();
    }

    private class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.ViewHolder> {
        private List<Payment> payments;

        PaymentAdapter(List<Payment> payments) {
            this.payments = payments;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_payment, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Payment p = payments.get(position);
            
            String type = p.getPaymentType();
            if (type.equals("interest")) {
                holder.tvType.setText("व्याज");
                holder.tvType.setTextColor(Color.parseColor("#FFA500"));
            } else if (type.equals("principal")) {
                holder.tvType.setText("मुद्दल");
                holder.tvType.setTextColor(Color.parseColor("#4CAF50"));
            } else {
                holder.tvType.setText(type);
            }
            
            holder.tvDate.setText(p.getPaymentDate());
            holder.tvAmount.setText(String.format("₹%.2f", p.getAmount()));
        }

        @Override
        public int getItemCount() {
            return payments.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvType, tvDate, tvAmount;
            ViewHolder(View itemView) {
                super(itemView);
                tvType = itemView.findViewById(R.id.tvPaymentType);
                tvDate = itemView.findViewById(R.id.tvPaymentDate);
                tvAmount = itemView.findViewById(R.id.tvPaymentAmount);
            }
        }
    }
}
