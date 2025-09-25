package com.example.guessthesong;

import android.app.DownloadManager;
import android.util.Base64;

import androidx.annotation.NonNull;

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

public class TokenManager {
    String accessToken, refreshToken;
    String client_id = "60896381f87c46e580192529d73aa8e1";
    String client_secret = "70b0e7048d244368bfc1093c46bc7d05";

    OkHttpClient client = new OkHttpClient();

    public void refreshAccessToken(TokenCallback callback) {
        if (refreshToken == null) {
            callback.onFailure(new Exception("No refresh token available"));
            return;
        }

        String client_info = client_id + ":" + client_secret;
        String basicAuth = "Basic " + Base64.encodeToString(client_info.getBytes(), Base64.NO_WRAP);

        RequestBody formBody = new FormBody.Builder()
                .add("grant_type", "refresh_token")
                .add("refresh_token", refreshToken).build();

        Request request = new Request.Builder()
                .url("https://accounts.spotify.com/api/token")
                .addHeader("Authorization", basicAuth)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .post(formBody).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String body = response.body().string();

                    try {
                        JSONObject jsonObject = new JSONObject(body);
                        accessToken = jsonObject.getString("access_token");
                        callback.onTokenReady(accessToken);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    public void setRefreshToken(String token) {
        this.refreshToken = token;
    }

    public interface TokenCallback {
        void onTokenReady (String accessToken);
        void onFailure (Exception e);
    }
}
