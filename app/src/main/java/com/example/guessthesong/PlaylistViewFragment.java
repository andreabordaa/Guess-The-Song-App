package com.example.guessthesong;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.example.guessthesong.databinding.FragmentPlaylistViewBinding;
import com.example.guessthesong.databinding.PlaylistCardViewBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class PlaylistViewFragment extends Fragment {
    FragmentPlaylistViewBinding binding;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    PlaylistListener listener;
    PlaylistAdapter adapter;
    ArrayList<Playlist> playlists = new ArrayList<>();
    OkHttpClient client = new OkHttpClient();
    TokenManager tokenManager;

    public PlaylistViewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentPlaylistViewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tokenManager = new TokenManager();

        //UI for the playlist grid view
        adapter = new PlaylistAdapter(getActivity(), playlists);
        binding.playlistGridView.setAdapter(adapter);

        //logs user out and directs them back to login fragment
        binding.logoutButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.logout();
            }
        });

        //retrieves and saves user spotify token as a string
        DocumentReference user = db.collection("spotifyUsers").document(FirebaseAuth.getInstance().getUid());
        user.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();

                    //TODO: implement TokenManager refreshAccessCode() function to get access code
                    tokenManager.setRefreshToken(doc.get("refreshToken").toString());
                    tokenManager.refreshAccessToken(new TokenManager.TokenCallback() {
                        @Override
                        public void onTokenReady(String accessToken) {
                            Request request = new Request.Builder()
                                    .url("https://api.spotify.com/v1/me/playlists")
                                    .addHeader("Authorization", "Bearer " + accessToken).build();

                            client.newCall(request).enqueue(new Callback() {
                                @Override
                                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                    Log.d("demo", "failed to get playlists");
                                }

                                @Override
                                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                    String body = response.body().string();
                                    try {
                                        JSONObject jsonObject = new JSONObject(body);
                                        JSONArray jsonArray = jsonObject.getJSONArray("items");

                                        for ( int i = 0; i < jsonArray.length(); i++) {
                                            JSONObject playlistItem = jsonArray.getJSONObject(i);
                                            String playlistName = playlistItem.getString("name");
                                            String playlistImage = playlistItem.getJSONArray("images").getJSONObject(0).getString("url");

                                            Log.d("demo", "playlist name: " + playlistName);
                                            Log.d("demo", "playlist image url: " + playlistImage);

                                            Playlist playlist = new Playlist(playlistName, playlistImage);
                                            playlists.add(playlist);
                                        }

                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                adapter.notifyDataSetChanged();
                                            }
                                        });

                                    } catch (JSONException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            });
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.d("demo", "failed to refresh token ");
                        }
                    });
                }
            }
        });

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (PlaylistListener) context;
    }

    public interface PlaylistListener {
        void logout();
    }

    class PlaylistAdapter extends ArrayAdapter<Playlist> {
        public PlaylistAdapter(@NonNull Context context, ArrayList<Playlist> list) {
            super(context, 0, list);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            PlaylistCardViewBinding binding;
            if (convertView == null) {
                binding = PlaylistCardViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
                convertView = binding.getRoot();
                convertView.setTag(binding);
            } else {
                binding = (PlaylistCardViewBinding) convertView.getTag();
            }

            Playlist mPlaylist = getItem(position);
            binding.playlistNameLabel.setText(mPlaylist.getPlaylistName());
            Picasso.get().load(mPlaylist.getImageUrl()).into(binding.playlistImageView);

            return convertView;
        }
    }
}