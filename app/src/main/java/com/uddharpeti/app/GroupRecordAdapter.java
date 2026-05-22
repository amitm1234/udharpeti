package com.uddharpeti.app;

import android.content.Intent;
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

public class GroupRecordAdapter extends RecyclerView.Adapter<GroupRecordAdapter.ViewHolder> {

    private List<Person> records;

    public GroupRecordAdapter(List<Person> records) {
        this.records = records;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Person p = records.get(position);
        
        holder.tvAmount.setText(String.format("₹%.0f", p.getAmount()));
        holder.tvDate.setText(p.getStartDate());
        holder.tvRate.setText("दर: " + p.getInterestRate() + "%");
        
        // Calculate interest
        long days = getDaysDiff(p.getStartDate());
        double months = days / 30.0;
        double rate = p.getInterestRate();
        if ("yearly".equals(p.getInterestType())) {
            rate = rate / 12.0;
        }
        double interest = (p.getAmount() * rate * months) / 100.0;
        holder.tvInterest.setText(String.format("व्याज: ₹%.2f", interest));

        if ("lent".equals(p.getTransactionType())) {
            holder.tvBadge.setText("दिलेले");
            holder.tvBadge.setBackgroundResource(R.drawable.badge_bg_green);
        } else {
            holder.tvBadge.setText("घेतलेले");
            holder.tvBadge.setBackgroundResource(R.drawable.badge_bg_red);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), PersonDetailActivity.class);
            intent.putExtra("personId", p.getId());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return records.size();
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBadge, tvAmount, tvDate, tvRate, tvInterest;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBadge = itemView.findViewById(R.id.tvBadge);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvRate = itemView.findViewById(R.id.tvRate);
            tvInterest = itemView.findViewById(R.id.tvInterest);
        }
    }
}
