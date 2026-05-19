package com.uddharpeti.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();

        ImageView ivLogo = findViewById(R.id.ivLogo);
        TextView tvAppNameHindi = findViewById(R.id.tvAppNameHindi);

        Animation fadeDown = AnimationUtils.loadAnimation(this, R.anim.fade_in_down);
        ivLogo.startAnimation(fadeDown);

        Animation fadeUp = AnimationUtils.loadAnimation(this, R.anim.fade_in_up);
        tvAppNameHindi.startAnimation(fadeUp);

        new Handler().postDelayed(() -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            Intent intent;
            if (currentUser != null) {
                intent = new Intent(SplashActivity.this, HomeActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            finish();
        }, 2500);
    }
}
