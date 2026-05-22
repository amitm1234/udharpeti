package com.uddharpeti.app;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class PersonGroupActivity extends AppCompatActivity {

    private String personGroupId;
    private String personName;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String userId;

    private TextView tvHeaderName, tvAvatar, tvName, tvRecordCount;
    private TextView tvTotalPrincipal, tvTotalInterest, tvTotalDue;
    private RecyclerView rvRecords;
    private List<Person> recordList;
    private GroupRecordAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_group);

        personGroupId = getIntent().getStringExtra("personGroupId");
        personName = getIntent().getStringExtra("personName");

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        userId = auth.getUid();

        initViews();
        loadGroupRecords();
    }

    private void initViews() {
        tvHeaderName = findViewById(R.id.tvHeaderName);
        tvAvatar = findViewById(R.id.tvAvatar);
        tvName = findViewById(R.id.tvName);
        tvRecordCount = findViewById(R.id.tvRecordCount);
        
        tvTotalPrincipal = findViewById(R.id.tvTotalPrincipal);
        tvTotalInterest = findViewById(R.id.tvTotalInterest);
        tvTotalDue = findViewById(R.id.tvTotalDue);
        
        rvRecords = findViewById(R.id.rvRecords);
        rvRecords.setLayoutManager(new LinearLayoutManager(this));
        
        tvHeaderName.setText(personName);
        tvName.setText(personName);
        if (personName != null && !personName.isEmpty()) {
            tvAvatar.setText(personName.substring(0, 1).toUpperCase());
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        recordList = new ArrayList<>();
        adapter = new GroupRecordAdapter(recordList);
        rvRecords.setAdapter(adapter);
    }

    private void loadGroupRecords() {
        if (userId == null || personGroupId == null) return;

        db.collection("users")
            .document(userId)
            .collection("persons")
            .whereEqualTo("personGroupId", personGroupId)
            .whereEqualTo("status", "active")
            .addSnapshotListener((snapshots, error) -> {
                if (error != null) {
                    Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (snapshots != null) {
                    recordList.clear();
                    double totalPrincipal = 0;
                    double totalInterest = 0;

                    for (DocumentSnapshot doc : snapshots) {
                        Person p = doc.toObject(Person.class);
                        if (p != null) {
                            p.setId(doc.getId());
                            recordList.add(p);

                            totalPrincipal += p.getAmount();
                            
                            // Calculate interest
                            long days = getDaysDiff(p.getStartDate());
                            double months = days / 30.0;
                            double rate = p.getInterestRate();
                            if ("yearly".equals(p.getInterestType())) {
                                rate = rate / 12.0;
                            }
                            totalInterest += (p.getAmount() * rate * months) / 100.0;
                        }
                    }

                    tvRecordCount.setText(recordList.size() + " Records");
                    tvTotalPrincipal.setText("₹" + String.format(Locale.getDefault(), "%.2f", totalPrincipal));
                    tvTotalInterest.setText("₹" + String.format(Locale.getDefault(), "%.2f", totalInterest));
                    tvTotalDue.setText("₹" + String.format(Locale.getDefault(), "%.2f", totalPrincipal + totalInterest));
                    
                    adapter.notifyDataSetChanged();
                }
            });
    }

    private long getDaysDiff(String startDateStr) {
        if (startDateStr == null || startDateStr.isEmpty()) return 0;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date startDate = sdf.parse(startDateStr);
            Date currentDate = new Date();
            long diffInMillis = Math.abs(currentDate.getTime() - startDate.getTime());
            return TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
        } catch (ParseException e) {
            return 0;
        }
    }
}
