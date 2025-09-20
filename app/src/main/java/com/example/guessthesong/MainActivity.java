package com.example.guessthesong;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.spotify.sdk.android.auth.AuthorizationResponse;

public class MainActivity extends AppCompatActivity implements LoginFragment.LoginListener, CreateAccountFragment.CreateAccountListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //shows the login fragment once the app is opened
        getSupportFragmentManager().beginTransaction().replace(R.id.main, new LoginFragment()).commit();
    }

    @Override
    public void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);

        Uri uri = intent.getData();
        if (uri != null) {
            AuthorizationResponse response = AuthorizationResponse.fromUri(uri);

            WelcomeFragment welcomeFragment = (WelcomeFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.main);
            if (welcomeFragment != null) {
                welcomeFragment.handleAuthResponse(response);
            }
        }
    }

    @Override
    public void gotoWelcome() {
        Log.d("demo", "go to welcome screen");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, new WelcomeFragment()).addToBackStack(null).commit();
    }

    @Override
    public void gotoCreateAccount() {
        Log.d("demo", "go to create account screen");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, new CreateAccountFragment()).addToBackStack(null).commit();
    }
}