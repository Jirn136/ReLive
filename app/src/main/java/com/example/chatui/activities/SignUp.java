package com.example.chatui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatui.MainActivity;
import com.example.chatui.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignUp extends AppCompatActivity {
    private EditText edtUsername, edtEmail, edtPassword, edtCPassword;
    private Button btnSignup;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        edtUsername = findViewById(R.id.edtUsername);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtCPassword = findViewById(R.id.edtCPassword);

        btnSignup = findViewById(R.id.btnSignup);

        mAuth = FirebaseAuth.getInstance();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("My Users");


        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = String.valueOf(edtUsername.getText());
                final String password = String.valueOf(edtEmail.getText());
                final String emailId = String.valueOf(edtEmail.getText());

                mAuth.createUserWithEmailAndPassword(emailId, password)
                        .addOnCompleteListener(SignUp.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    HashMap map = new HashMap();
                                    map.put("Username", name);
                                    map.put("Email Id", emailId);
                                    map.put("Password", password);

                                    myRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(map);
                                    Toast.makeText(SignUp.this, "Added", Toast.LENGTH_SHORT).show();

                                    startActivity(new Intent(SignUp.this, MainActivity.class));

                                } else {
                                    // If sign in fails, display a message to the user.

                                    Toast.makeText(SignUp.this, "worst coder", Toast.LENGTH_SHORT).show();

                                }

                                // ...
                            }
                        });
            }
        });
    }

}
