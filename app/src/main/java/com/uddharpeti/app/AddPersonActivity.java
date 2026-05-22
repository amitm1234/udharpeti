package com.uddharpeti.app;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

public class AddPersonActivity extends AppCompatActivity {

    private AutoCompleteTextView etName;
    private TextInputEditText etPhone, etAmount, etRate, etNotes;
    private MaterialButton btnLent, btnBorrowed, btnMonthly, btnYearly, btnDatePicker, btnSave;
    private TextView tvPreviewMonthly, tvPreviewYearly, tvPreviewTotal;
    private ProgressBar progressBar;
    
    private String transactionType = "lent";
    private String interestType = "monthly";
    private String selectedDate = "";
    
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String userId;

    private List<Person> existingPersons = new ArrayList<>();
    private String selectedGroupId = null;
    private boolean isSelectedFromSuggestion = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_person);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        userId = auth.getUid();

        initViews();
        setupListeners();
        setDefaultDate();
        loadExistingPersons();
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etName.setDropDownBackgroundResource(R.color.colorCard);
        etName.setDropDownWidth(ViewGroup.LayoutParams.MATCH_PARENT);

        etPhone = findViewById(R.id.etPhone);
        etAmount = findViewById(R.id.etAmount);
        etRate = findViewById(R.id.etRate);
        etNotes = findViewById(R.id.etNotes);
        
        btnLent = findViewById(R.id.btnLent);
        btnBorrowed = findViewById(R.id.btnBorrowed);
        btnMonthly = findViewById(R.id.btnMonthly);
        btnYearly = findViewById(R.id.btnYearly);
        btnDatePicker = findViewById(R.id.btnDatePicker);
        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);
        
        tvPreviewMonthly = findViewById(R.id.tvPreviewMonthly);
        tvPreviewYearly = findViewById(R.id.tvPreviewYearly);
        tvPreviewTotal = findViewById(R.id.tvPreviewTotal);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void setupListeners() {
        btnLent.setOnClickListener(v -> {
            transactionType = "lent";
            updateToggleStyles(btnLent, btnBorrowed);
        });

        btnBorrowed.setOnClickListener(v -> {
            transactionType = "borrowed";
            updateToggleStyles(btnBorrowed, btnLent);
        });

        btnMonthly.setOnClickListener(v -> {
            interestType = "monthly";
            updateToggleStyles(btnMonthly, btnYearly);
            updatePreview();
        });

        btnYearly.setOnClickListener(v -> {
            interestType = "yearly";
            updateToggleStyles(btnYearly, btnMonthly);
            updatePreview();
        });

        btnDatePicker.setOnClickListener(v -> showDatePicker());

        etName.setOnItemClickListener((parent, view, position, id) -> {
            isSelectedFromSuggestion = true;
            String selectedName = etName.getText().toString().trim();
            for (Person p : existingPersons) {
                if (p.getName().equals(selectedName)) {
                    selectedGroupId = p.getPersonGroupId();
                    if (selectedGroupId == null) selectedGroupId = p.getId();
                    break;
                }
            }
        });

        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isSelectedFromSuggestion = false;
                selectedGroupId = null;
                updatePreview();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePreview();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        etAmount.addTextChangedListener(watcher);
        etRate.addTextChangedListener(watcher);

        btnSave.setOnClickListener(v -> saveToFirestore());
    }

    private void loadExistingPersons() {
        if (userId == null) return;
        db.collection("users")
            .document(userId)
            .collection("persons")
            .get()
            .addOnSuccessListener(snapshots -> {
                existingPersons.clear();
                List<String> names = new ArrayList<>();
                for (DocumentSnapshot doc : snapshots) {
                    Person p = doc.toObject(Person.class);
                    if (p != null) {
                        p.setId(doc.getId());
                        existingPersons.add(p);
                        if (!names.contains(p.getName())) {
                            names.add(p.getName());
                        }
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_dropdown_item_1line, names);
                etName.setAdapter(adapter);
            });
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
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("तारीख निवडा")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(selection);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            selectedDate = sdf.format(calendar.getTime());
            btnDatePicker.setText(selectedDate);
        });

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    private void updatePreview() {
        String amountStr = etAmount.getText().toString();
        String rateStr = etRate.getText().toString();

        if (amountStr.isEmpty() || rateStr.isEmpty()) {
            tvPreviewMonthly.setText("Monthly व्याज: ₹0");
            tvPreviewYearly.setText("Yearly व्याज: ₹0");
            tvPreviewTotal.setText("एकूण येणे: ₹0");
            return;
        }

        double principal = Double.parseDouble(amountStr);
        double rate = Double.parseDouble(rateStr);
        
        double monthly, yearly;
        if (interestType.equals("monthly")) {
            monthly = (principal * rate) / 100;
            yearly = monthly * 12;
        } else {
            yearly = (principal * rate) / 100;
            monthly = yearly / 12;
        }

        tvPreviewMonthly.setText(String.format("Monthly व्याज: ₹%.2f", monthly));
        tvPreviewYearly.setText(String.format("Yearly व्याज: ₹%.2f", yearly));
        tvPreviewTotal.setText(String.format("एकूण येणे: ₹%.2f", principal + monthly));
    }

    private void saveToFirestore() {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "नाव टाका", Toast.LENGTH_SHORT).show();
            return;
        }
        if (etAmount.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "रक्कम टाका", Toast.LENGTH_SHORT).show();
            return;
        }
        if (etRate.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "व्याज दर टाका", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "तारीख निवडा", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isSelectedFromSuggestion) {
            proceedToSave(selectedGroupId);
        } else {
            checkExistingName(name);
        }
    }

    private void checkExistingName(String name) {
        boolean nameExists = false;
        String existingGroupId = null;
        
        for (Person p : existingPersons) {
            if (p.getName().equalsIgnoreCase(name)) {
                nameExists = true;
                existingGroupId = p.getPersonGroupId();
                if (existingGroupId == null) existingGroupId = p.getId();
                break;
            }
        }
        
        if (nameExists) {
            String finalExistingGroupId = existingGroupId;
            new AlertDialog.Builder(this)
                .setTitle("नाव आधीपासून आहे")
                .setMessage("\"" + name + "\" आधीपासून आहे.\n\nहाच " + name + " आहे का?")
                .setPositiveButton("हो, हाच आहे", (dialog, which) -> proceedToSave(finalExistingGroupId))
                .setNegativeButton("नाही, वेगळा आहे", (dialog, which) -> proceedToSave(UUID.randomUUID().toString()))
                .setCancelable(false)
                .show();
        } else {
            proceedToSave(UUID.randomUUID().toString());
        }
    }

    private void proceedToSave(String groupId) {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        double amount = Double.parseDouble(etAmount.getText().toString().trim());
        double rate = Double.parseDouble(etRate.getText().toString().trim());
        String notes = etNotes.getText().toString().trim();

        btnSave.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        Map<String, Object> personData = new HashMap<>();
        personData.put("name", name);
        personData.put("phone", phone);
        personData.put("amount", amount);
        personData.put("interestRate", rate);
        personData.put("interestType", interestType);
        personData.put("transactionType", transactionType);
        personData.put("startDate", selectedDate);
        personData.put("notes", notes);
        personData.put("status", "active");
        personData.put("personGroupId", groupId);
        personData.put("createdAt", FieldValue.serverTimestamp());

        db.collection("users").document(userId).collection("persons")
            .add(personData)
            .addOnSuccessListener(ref -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "यशस्वी जोडले! 🎉", Toast.LENGTH_SHORT).show();
                finish();
            })
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
    }
}
