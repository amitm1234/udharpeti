package com.uddharpeti.app;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SimplePersonAdapter 
    extends RecyclerView.Adapter<SimplePersonAdapter.ViewHolder> {

    private List<Person> personList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Person person);
    }

    public SimplePersonAdapter(
            List<Person> personList,
            OnItemClickListener listener) {
        this.personList = personList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, 
            int viewType) {
        View view = LayoutInflater
            .from(parent.getContext())
            .inflate(R.layout.item_person, 
                parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder, 
            int position) {
        Person person = personList.get(position);
        holder.bind(person, listener);
    }

    @Override
    public int getItemCount() {
        return personList.size();
    }

    public static class ViewHolder 
            extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvName, tvPhone, 
            tvAmount, tvInterest, 
            tvStartDate, tvBadge, tvRecordCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView
                .findViewById(R.id.tvAvatar);
            tvName = itemView
                .findViewById(R.id.tvName);
            tvPhone = itemView
                .findViewById(R.id.tvPhone);
            tvAmount = itemView
                .findViewById(R.id.tvAmount);
            tvInterest = itemView
                .findViewById(R.id.tvInterest);
            tvStartDate = itemView
                .findViewById(R.id.tvStartDate);
            tvBadge = itemView
                .findViewById(R.id.tvBadge);
            tvRecordCount = itemView
                .findViewById(R.id.tvRecordCount);
        }

        public void bind(Person person, 
                OnItemClickListener listener) {
            if (tvName != null) 
                tvName.setText(person.getName());
            if (tvPhone != null) 
                tvPhone.setText(person.getPhone());
            if (tvAmount != null) 
                tvAmount.setText(
                    "₹" + person.getAmount());
            if (tvStartDate != null) 
                tvStartDate.setText(
                    person.getStartDate());
            if (tvRecordCount != null) 
                tvRecordCount.setVisibility(
                    View.GONE);

            if (tvAvatar != null 
                    && person.getName() != null 
                    && !person.getName().isEmpty()) {
                tvAvatar.setText(
                    person.getName()
                        .substring(0, 1)
                        .toUpperCase());
            }

            if (tvBadge != null) {
                if ("lent".equals(
                        person.getTransactionType())) {
                    tvBadge.setText("दिलेले");
                    tvBadge.setBackgroundColor(
                        Color.parseColor("#4CAF50"));
                } else {
                    tvBadge.setText("घेतलेले");
                    tvBadge.setBackgroundColor(
                        Color.parseColor("#F44336"));
                }
            }

            try {
                SimpleDateFormat sdf = 
                    new SimpleDateFormat(
                        "dd/MM/yyyy", 
                        Locale.getDefault());
                Date startDate = 
                    sdf.parse(person.getStartDate());
                long diff = Math.abs(
                    new Date().getTime() 
                    - startDate.getTime());
                long days = TimeUnit.DAYS.convert(
                    diff, TimeUnit.MILLISECONDS);
                double months = days / 30.0;
                double rate = 
                    person.getInterestRate();
                if ("yearly".equals(
                        person.getInterestType())) 
                    rate /= 12.0;
                double interest = 
                    (person.getAmount() 
                    * rate * months) / 100.0;
                if (tvInterest != null) 
                    tvInterest.setText(
                        "व्याज: ₹" + 
                        String.format(
                            Locale.getDefault(),
                            "%.2f", interest));
            } catch (Exception e) {
                if (tvInterest != null) 
                    tvInterest.setText(
                        "व्याज: ₹0.00");
            }

            itemView.setOnClickListener(
                v -> listener.onItemClick(person));
        }
    }
}
