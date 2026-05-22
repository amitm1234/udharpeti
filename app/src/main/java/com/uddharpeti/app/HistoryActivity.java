package com.uddharpeti.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView rvHistory;
    private HistoryAdapter adapter;
    private List<HistoryItem> allItems;
    private List<HistoryItem> filteredItems;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ChipGroup chipGroupFilters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        rvHistory = findViewById(R.id.rvHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        allItems = new ArrayList<>();
        filteredItems = new ArrayList<>();
        adapter = new HistoryAdapter(filteredItems);
        rvHistory.setAdapter(adapter);

        chipGroupFilters = (ChipGroup) findViewById(R.id.chipGroupFilters);
        chipGroupFilters.setOnCheckedChangeListener((group, checkedId) -> {
            applyFilter(checkedId);
        });

        loadAllHistory();
    }

    private void loadAllHistory() {
        String userId = auth.getUid();
        if (userId == null) return;

        db.collection("users").document(userId).collection("persons")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allItems.clear();
                    for (QueryDocumentSnapshot personDoc : queryDocumentSnapshots) {
                        String personName = personDoc.getString("name");
                        String transType = personDoc.getString("transactionType");
                        
                        // Add the initial transaction as a history item
                        allItems.add(new HistoryItem(
                                personName,
                                "Initial " + transType,
                                personDoc.getString("startDate"),
                                personDoc.getDouble("amount"),
                                transType
                        ));

                        // Load payments for this person
                        personDoc.getReference().collection("payments")
                                .get()
                                .addOnSuccessListener(paymentDocs -> {
                                    for (QueryDocumentSnapshot payDoc : paymentDocs) {
                                        allItems.add(new HistoryItem(
                                                personName,
                                                payDoc.getString("paymentType"),
                                                payDoc.getString("paymentDate"),
                                                payDoc.getDouble("amount"),
                                                transType
                                        ));
                                    }
                                    sortAndDisplay();
                                });
                    }
                    sortAndDisplay();
                });
    }

    private void sortAndDisplay() {
        if (chipGroupFilters != null) {
            applyFilter(chipGroupFilters.getCheckedChipId());
        }
    }

    private void applyFilter(int checkedId) {
        if (checkedId == R.id.chipAll || checkedId == -1) {
            showAll();
        } else if (checkedId == R.id.chipLent) {
            filterByType("lent");
        } else if (checkedId == R.id.chipBorrowed) {
            filterByType("borrowed");
        } else if (checkedId == R.id.chipInterest) {
            filterByPaymentType("interest");
        } else if (checkedId == R.id.chipPrincipal) {
            filterByPaymentType("principal");
        }
    }

    private void showAll() {
        filteredItems.clear();
        filteredItems.addAll(allItems);
        adapter.notifyDataSetChanged();
    }

    private void filterByType(String type) {
        filteredItems.clear();
        for (HistoryItem item : allItems) {
            if (type.equals(item.getTransactionType())) {
                filteredItems.add(item);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void filterByPaymentType(String type) {
        filteredItems.clear();
        for (HistoryItem item : allItems) {
            if (type.equals(item.getPaymentType())) {
                filteredItems.add(item);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
        private List<HistoryItem> items;

        HistoryAdapter(List<HistoryItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            HistoryItem item = items.get(position);
            holder.tvName.setText(item.getPersonName());
            holder.tvType.setText(item.getPaymentType());
            holder.tvDate.setText(item.getDate());
            holder.tvAmount.setText(String.format("₹%.2f", item.getAmount()));
            
            if ("lent".equals(item.getTransactionType())) {
                holder.tvAmount.setTextColor(getColor(R.color.colorLent));
            } else {
                holder.tvAmount.setTextColor(getColor(R.color.colorBorrowed));
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvType, tvDate, tvAmount;
            ViewHolder(View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvPersonName);
                tvType = itemView.findViewById(R.id.tvPaymentType);
                tvDate = itemView.findViewById(R.id.tvDate);
                tvAmount = itemView.findViewById(R.id.tvAmount);
            }
        }
    }
}
