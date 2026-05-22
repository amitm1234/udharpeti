package com.uddharpeti.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private TextView tvTotalLent, tvTotalBorrowed, tvTotalInterest, tvEmpty;
    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private BottomNavigationView bottomNavigation;
    private ImageButton btnLogout;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String userId;

    private List<List<Person>> groupedList;
    private PersonAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();
        userId = auth.getCurrentUser().getUid();

        tvTotalLent = findViewById(R.id.tvTotalLent);
        tvTotalBorrowed = findViewById(R.id.tvTotalBorrowed);
        tvTotalInterest = findViewById(R.id.tvTotalInterest);
        tvEmpty = findViewById(R.id.tvEmpty);
        recyclerView = findViewById(R.id.recyclerView);
        fabAdd = findViewById(R.id.fabAdd);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        btnLogout = findViewById(R.id.btnLogout);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        groupedList = new ArrayList<>();
        adapter = new PersonAdapter(groupedList, group -> {
            if (group.size() == 1) {
                Intent intent = new Intent(MainActivity.this, PersonDetailActivity.class);
                intent.putExtra("personId", group.get(0).getId());
                startActivity(intent);
            } else {
                Intent intent = new Intent(MainActivity.this, PersonGroupActivity.class);
                intent.putExtra("personGroupId", group.get(0).getPersonGroupId());
                intent.putExtra("personName", group.get(0).getName());
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);

        btnLogout.setOnClickListener(v -> logout());
        fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AddPersonActivity.class));
        });

        setupBottomNavigation();
        loadPersons();
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.navigation_completed) {
                startActivity(new Intent(this, CompletedActivity.class));
                return true;
            } else if (itemId == R.id.nav_history) {
                startActivity(new Intent(this, HistoryActivity.class));
                return true;
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }

            return false;
        });
    }

    private void loadPersons() {
        db.collection("users")
                .document(userId)
                .collection("persons")
                .whereEqualTo("status", "active")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots != null) {
                        Map<String, List<Person>> groupedMap = new HashMap<>();
                        for (DocumentSnapshot doc : snapshots) {
                            Person p = doc.toObject(Person.class);
                            if (p != null) {
                                p.setId(doc.getId());
                                String gId = p.getPersonGroupId();
                                if (gId == null) gId = p.getId();
                                
                                if (!groupedMap.containsKey(gId)) {
                                    groupedMap.put(gId, new ArrayList<>());
                                }
                                groupedMap.get(gId).add(p);
                            }
                        }
                        
                        groupedList.clear();
                        for (Map.Entry<String, List<Person>> entry : groupedMap.entrySet()) {
                            groupedList.add(entry.getValue());
                        }
                        
                        adapter.notifyDataSetChanged();
                        calculateSummary();
                        showEmptyState();
                    }
                });
    }

    private void calculateSummary() {
        double totalLent = 0, totalBorrowed = 0, totalInterest = 0;

        for (List<Person> group : groupedList) {
            for (Person p : group) {
                if ("lent".equals(p.getTransactionType())) {
                    totalLent += p.getAmount();
                } else {
                    totalBorrowed += p.getAmount();
                }

                long days = getDaysDiff(p.getStartDate());
                double months = days / 30.0;
                double principal = p.getAmount();
                double rate = p.getInterestRate();
                if ("yearly".equals(p.getInterestType())) {
                    rate = rate / 12.0;
                }
                double interest = (principal * rate * months) / 100;
                totalInterest += interest;
            }
        }

        tvTotalLent.setText("₹" + String.format(Locale.getDefault(), "%.2f", totalLent));
        tvTotalBorrowed.setText("₹" + String.format(Locale.getDefault(), "%.2f", totalBorrowed));
        tvTotalInterest.setText("₹" + String.format(Locale.getDefault(), "%.2f", totalInterest));
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

    private void showEmptyState() {
        if (groupedList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
    }

    private void logout() {
        auth.signOut();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finishAffinity();
    }
}
