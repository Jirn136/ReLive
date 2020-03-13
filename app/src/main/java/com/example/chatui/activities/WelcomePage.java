package com.example.chatui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatui.R;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class WelcomePage extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth mAuth;
    private CallbackManager manager;
    AccessToken accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_page);

        mAuth = FirebaseAuth.getInstance();

        manager = CallbackManager.Factory.create();
        Button btnCreate = findViewById(R.id.btnCreate);


        LoginButton btnFb = findViewById(R.id.btnFb);

        btnFb.setReadPermissions("email", "public_profile");


        btnFb.registerCallback(manager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Toast.makeText(WelcomePage.this, "bulb uh!! ", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Log.i("TAG", error.getMessage());
            }
        });

        btnCreate.setOnClickListener(this);

    }

    private void handleAccessToken(AccessToken accessToken) {
        final AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener(WelcomePage.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    updateUser(user);
                } else {
                    Toast.makeText(WelcomePage.this, "error" + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateUser(FirebaseUser user) {

        Log.i("TAG", String.valueOf(user));
        if (user != null) {
            String name = Profile.getCurrentProfile().getName();
            Log.i("TAG", name);

            accessToken = AccessToken.getCurrentAccessToken();

            boolean isLoggedIn = accessToken != null && !accessToken.isExpired();

            if (isLoggedIn) {
                startActivity(new Intent(this, UploadData.class));
                finish();
            } else {
                Toast.makeText(this, "Log out", Toast.LENGTH_SHORT).show();
            }

        }


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCreate:
                startActivity(new Intent(WelcomePage.this, SignUp.class));
                break;
            case R.id.txtLogin:
                Toast.makeText(this, "in progress", Toast.LENGTH_SHORT).show();
                break;
//            case R.id.btnFb:
//                handleAccessToken(accessToken);
//                break;
            default:
                Toast.makeText(this, "No such method.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        manager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        updateUser(user);
    }
}
