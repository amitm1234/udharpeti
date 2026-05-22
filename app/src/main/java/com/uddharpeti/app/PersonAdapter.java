package com.uddharpeti.app;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class PersonAdapter extends RecyclerView.Adapter<PersonAdapter.ViewHolder> {

    private List<List<Person>> personGroups;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(List<Person> personGroup);
    }

    public PersonAdapter(List<List<Person>> personGroups, OnItemClickListener listener) {
        this.personGroups = personGroups;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_person, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        List<Person> group = personGroups.get(position);
        holder.bind(group, listener);
    }

    @Override
    public int getItemCount() {
        return personGroups.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvName, tvPhone, tvRecordCount, tvAmount, tvInterest;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvRecordCount = itemView.findViewById(R.id.tvRecordCount);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvInterest = itemView.findViewById(R.id.tvInterest);
        }

        public void bind(List<Person> group, OnItemClickListener listener) {
            Person first = group.get(0);
            tvName.setText(first.getName());
            tvPhone.setText(first.getPhone());
            
            // Avatar initials
            if (first.getName() != null && !first.getName().isEmpty()) {
                tvAvatar.setText(first.getName().substring(0, 1).toUpperCase());
            }

            // Records count
            int count = group.size();
            if (count == 1) {
                tvRecordCount.setText("1 Record");
            } else {
                tvRecordCount.setText(count + " Records");
            }

            // Total calculate
            double totalPrincipal = 0;
            double totalInterest = 0;
            for (Person p : group) {
                totalPrincipal += p.getAmount();
                
                // Interest calculate
                long days = getDaysDiff(p.getStartDate());
                double months = days / 30.0;
                double rate = p.getInterestRate();
                if ("yearly".equals(p.getInterestType())) {
                    rate = rate / 12.0;
                }
                totalInterest += (p.getAmount() * rate * months) / 100.0;
            }

            tvAmount.setText("₹" + String.format(Locale.getDefault(), "%.0f", totalPrincipal + totalInterest));
            tvInterest.setText("व्याज: ₹" + String.format(Locale.getDefault(), "%.2f", totalInterest));

            itemView.setOnClickListener(v -> listener.onItemClick(group));
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
}
