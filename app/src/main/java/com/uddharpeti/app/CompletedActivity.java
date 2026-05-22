package com.uddharpeti.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.uddharpeti.app.SimplePersonAdapter;

import java.util.ArrayList;
import java.util.List;

public class CompletedActivity extends AppCompatActivity {

    private TextView tvEmpty;
    private RecyclerView recyclerView;
    private BottomNavigationView bottomNavigation;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String userId;

    private List<Person> personList;
    private SimplePersonAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_completed);

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();
        userId = auth.getCurrentUser().getUid();

        tvEmpty = findViewById(R.id.tvEmpty);
        recyclerView = findViewById(R.id.recyclerView);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        personList = new ArrayList<>();
        adapter = new SimplePersonAdapter(personList, person -> {
            Intent intent = new Intent(CompletedActivity.this, PersonDetailActivity.class);
            intent.putExtra("personId", person.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        setupBottomNavigation();
        loadCompletedPersons();
    }

    private void setupBottomNavigation() {
        bottomNavigation = findViewById(R.id.bottomNavigation);
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.navigation_completed);
            bottomNavigation.setOnNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.navigation_completed) {
                    return true;
                } else if (itemId == R.id.nav_history) {
                    startActivity(new Intent(this, HistoryActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_settings) {
                    startActivity(new Intent(this, SettingsActivity.class));
                    finish();
                    return true;
                }
                return false;
            });
        }
    }

    private void loadCompletedPersons() {
        db.collection("users")
                .document(userId)
                .collection("persons")
                .whereEqualTo("status", "completed")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots != null) {
                        personList.clear();
                        for (DocumentSnapshot doc : snapshots) {
                            Person person = doc.toObject(Person.class);
                            if (person != null) {
                                person.setId(doc.getId());
                                personList.add(person);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        showEmptyState();
                    }
                });
    }

    private void showEmptyState() {
        if (personList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
    }
}
