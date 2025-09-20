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
import android.widget.Toast;

import com.example.guessthesong.databinding.FragmentLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

//this page will collect the information given from the user and save it in firebase to login to app account

public class LoginFragment extends Fragment {
    FragmentLoginBinding binding;
    LoginListener listener;

    public LoginFragment() {
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
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        //logs the user in and either connects them to the welcome page
        binding.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = binding.inputEmail.getText().toString();
                String password = binding.inputPassword.getText().toString();
                Log.d("demo", "email: " + email);
                Log.d("demo", "password: " + password);
                //create an interface that replaces the screen with the WelcomeFragment
                if (email.isEmpty()) {
                    Toast.makeText(getActivity(), "Enter email", Toast.LENGTH_SHORT).show();
                } else if (password.isEmpty()) {
                    Toast.makeText(getActivity(), "Enter password", Toast.LENGTH_SHORT).show();
                } else {

                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                listener.gotoWelcome();
                            } else {
                                Toast.makeText(getActivity(), "Incorrect Email/Password", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        binding.createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //create an interface that replaces the screen with the CreateAccountFragment
                listener.gotoCreateAccount();
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (LoginListener) context;
    }

    public interface LoginListener {
        void gotoWelcome();
        void gotoCreateAccount();
    }
}