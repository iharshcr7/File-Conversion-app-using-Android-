package com.example.mad_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView logo = findViewById(R.id.logoImage);

        // Simple fade + scale animation for the logo
        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(900);
        fadeIn.setFillAfter(true);

        ScaleAnimation scaleUp = new ScaleAnimation(
                0.85f, 1.0f,
                0.85f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scaleUp.setDuration(900);
        scaleUp.setFillAfter(true);

        AnimationSet set = new AnimationSet(true);
        set.addAnimation(fadeIn);
        set.addAnimation(scaleUp);
        set.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) {
                navigateToNextScreen();
            }

            @Override
            public void onAnimationRepeat(Animation animation) { }
        });

        logo.startAnimation(set);
    }

    private void navigateToNextScreen() {
        // Always go to Login screen for validation
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        // Optional: clear back stack so Splash can't be returned to
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
