package com.example.guessthesong;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.guessthesong.databinding.FragmentWelcomeBinding;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

public class WelcomeFragment extends Fragment {
    FragmentWelcomeBinding binding;

    public WelcomeFragment() {
        // Required empty public constructor
    }

    private static final String REDIRECT_URI = "guess://callback";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentWelcomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("demo", "Welcome Fragment inflated ");

        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("demo", "Redirecting to Spotify Login ");
                goToSpotifyLogin();
            }
        });
    }

    void handleAuthResponse(AuthorizationResponse response) {
        if (response.getType() == AuthorizationResponse.Type.CODE) {
            Log.d("demo", "Spotify API Token: " + response.getCode());
        }
    }


    void goToSpotifyLogin() {
        AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder("60896381f87c46e580192529d73aa8e1", AuthorizationResponse.Type.CODE, REDIRECT_URI);
        builder.setScopes(new String[]{"streaming", "playlist-read-private"});

        AuthorizationRequest request = builder.build();
        AuthorizationClient.openLoginInBrowser(getActivity(), request);
    }

}