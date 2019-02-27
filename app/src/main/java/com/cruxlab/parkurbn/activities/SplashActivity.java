package com.cruxlab.parkurbn.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.cruxlab.parkurbn.SharedPrefsManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SharedPrefsManager.get().isUserLoggedIn()) {
            startActivity(new Intent(SplashActivity.this, MapActivity.class));
        } else {
            //startActivity(new Intent(SplashActivity.this, MapActivity.class));
            startActivity(new Intent(SplashActivity.this, StartActivity.class));
            //startActivity(new Intent(this, TutorialActivity.class));
        }
        finish();
    }

}
