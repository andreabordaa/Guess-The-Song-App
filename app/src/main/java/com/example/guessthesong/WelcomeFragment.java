package com.example.guessthesong;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;

import com.example.guessthesong.databinding.FragmentWelcomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class WelcomeFragment extends Fragment {
    FragmentWelcomeBinding binding;
    WelcomeListener listener;
    FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    OkHttpClient client = new OkHttpClient();

    String CLIENT_ID = "60896381f87c46e580192529d73aa8e1";
    String CLIENT_SECRET = "70b0e7048d244368bfc1093c46bc7d05";

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
        // inflate the layout for this fragment
        binding = FragmentWelcomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //opens browser for Spotify Login
        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("demo", "Redirecting to Spotify Login ");
                goToSpotifyLogin();
            }
        });

        //redirects user back to login fragment
        binding.logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("demo", "Logging user out");
                listener.logout();

            }
        });
    }

    //saves the spotify token in firestore db
    void handleAuthResponse(AuthorizationResponse response) {
        if (response.getType() == AuthorizationResponse.Type.CODE) {
            Log.d("demo", "Spotify API Auth Code: " + response.getCode());

            mAuth = FirebaseAuth.getInstance();
            DocumentReference spotifyUser = db.collection("spotifyUsers").document(mAuth.getUid());
            //saves refresh token from spotify to firestore db
            RequestBody formBody = new FormBody.Builder()
                    .add("grant_type", "authorization_code")
                    .add("code", response.getCode())
                    .add("redirect_uri", REDIRECT_URI).build();

            String clientInfo = CLIENT_ID + ":" + CLIENT_SECRET;
            String basicAuth = "Basic " + Base64.encodeToString(clientInfo.getBytes(), Base64.NO_WRAP);

            Request request = new Request.Builder()
                    .url("https://accounts.spotify.com/api/token")
                    .post(formBody)
                    .addHeader("Authorization", basicAuth)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {}

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String body = response.body().string();
                        try {
                            JSONObject jsonObject = new JSONObject(body);
                            String refreshToken = jsonObject.getString("refresh_token");
                            Log.d("demo", "refresh token from spotify: ");

                            spotifyUser.update("refreshToken", refreshToken);
                            Log.d("demo", "saved refresh token: " + spotifyUser.get());

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
            listener.gotoPlaylists();
        }
    }

    //opens a browser to complete spotify login
    void goToSpotifyLogin() {
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookies(null);
        cookieManager.flush();

        AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder("60896381f87c46e580192529d73aa8e1", AuthorizationResponse.Type.CODE, REDIRECT_URI);
        builder.setScopes(new String[]{"streaming", "playlist-read-private"});

        AuthorizationRequest request = builder.setShowDialog(true).build();
        AuthorizationClient.openLoginInBrowser(getActivity(), request);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (WelcomeListener) context;
    }

    public interface WelcomeListener {
        void logout();
        void gotoPlaylists();
    }

}