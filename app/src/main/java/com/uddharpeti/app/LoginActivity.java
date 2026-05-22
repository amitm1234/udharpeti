package com.uddharpeti.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText etEmail, etPassword;
    MaterialButton btnLogin, btnGoogle;
    TextView tvForgot, tvRegister;
    ProgressBar progressBar;

    FirebaseAuth auth;

    GoogleSignInClient googleSignInClient;

    private final int RC_SIGN_IN = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        btnLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.btnGoogle);

        tvForgot = findViewById(R.id.tvForgotPassword);
        tvRegister = findViewById(R.id.tvRegister);

        progressBar = findViewById(R.id.progressBar);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        btnLogin.setOnClickListener(v -> loginUser());

        tvForgot.setOnClickListener(v -> forgotPassword());

        btnGoogle.setOnClickListener(v -> signInWithGoogle());

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this,
                    RegisterActivity.class));
        });
    }

    private void loginUser() {

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Email टाका");
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password टाका");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {

                        Toast.makeText(this,
                                "Login Successful",
                                Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(
                                LoginActivity.this,
                                MainActivity.class));

                        finish();

                    } else {

                        Toast.makeText(this,
                                task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void forgotPassword() {

        EditText resetMail = new EditText(this);
        resetMail.setHint("Email");
        resetMail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        new AlertDialog.Builder(this)
                .setTitle("Password Reset")
                .setMessage("तुमचा Email टाका")
                .setView(resetMail)

                .setPositiveButton("Send", (dialog, which) -> {

                    String mail = resetMail.getText().toString().trim();

                    auth.sendPasswordResetEmail(mail)
                            .addOnSuccessListener(unused -> Toast.makeText(
                                    this,
                                    "Reset email पाठवले",
                                    Toast.LENGTH_SHORT).show())

                            .addOnFailureListener(e -> Toast.makeText(
                                    this,
                                    e.getMessage(),
                                    Toast.LENGTH_SHORT).show());

                })

                .setNegativeButton("Cancel", null)
                .show();
    }

    private void signInWithGoogle() {

        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode,
                                 int resultCode,
                                 Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {

            Task<GoogleSignInAccount> task =
                    GoogleSignIn.getSignedInAccountFromIntent(data);

            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {

        try {

            GoogleSignInAccount account =
                    completedTask.getResult(ApiException.class);

            firebaseAuthWithGoogle(account.getIdToken());

        } catch (ApiException e) {

            Toast.makeText(this,
                    e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {

        AuthCredential credential =
                GoogleAuthProvider.getCredential(idToken, null);

        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {

                    if (task.isSuccessful()) {

                        startActivity(new Intent(
                                LoginActivity.this,
                                MainActivity.class));

                        finish();

                    } else {

                        Toast.makeText(this,
                                "Google Login Failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}