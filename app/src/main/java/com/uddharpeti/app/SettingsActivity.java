package com.uddharpeti.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsActivity extends AppCompatActivity {

    private TextView tvUserInitials, tvUserEmail;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        tvUserInitials = findViewById(R.id.tvUserInitials);
        tvUserEmail = findViewById(R.id.tvUserEmail);

        if (user != null) {
            String email = user.getEmail();
            tvUserEmail.setText(email);
            if (email != null && !email.isEmpty()) {
                tvUserInitials.setText(email.substring(0, 1).toUpperCase());
            }
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnShare).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, "UdharPeti app वापरा! तुमचा हिशोब सोपा करा.");
            startActivity(Intent.createChooser(intent, "Share via"));
        });

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("तुम्हाला नक्की Logout करायचे आहे का?")
                    .setPositiveButton("हो", (dialog, which) -> {
                        auth.signOut();
                        // Assuming LoginActivity exists. If not, user will need to create it.
                        // For now, I'll just redirect to a placeholder or finish.
                        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                        finishAffinity();
                    })
                    .setNegativeButton("नाही", null)
                    .show();
        });
    }
}
