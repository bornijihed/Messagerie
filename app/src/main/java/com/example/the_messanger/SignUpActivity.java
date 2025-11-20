package com.example.the_messanger;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {
    EditText edtName, edtEmail, edtPassword;
    Button btnSignUp;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        edtName = findViewById(R.id.edt_name);
        edtEmail = findViewById(R.id.edt_email);
        edtPassword = findViewById(R.id.edt_password);
        btnSignUp = findViewById(R.id.btn_signup);

        btnSignUp.setOnClickListener(view -> {
            String name = edtName.getText().toString();
            String email = edtEmail.getText().toString();
            String password = edtPassword.getText().toString();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (mAuth == null) {
                Toast.makeText(SignUpActivity.this, "Firebase not configured", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                String uid = mAuth.getCurrentUser().getUid();
                                User user = new User(uid, name, email);
                                FirebaseDatabase.getInstance().getReference().child("users").child(uid).setValue(user)
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                                                finish();
                                            } else {
                                            String error = task1.getException() != null ? task1.getException().getMessage() : "Failed to save user";
                                                Toast.makeText(SignUpActivity.this, error, Toast.LENGTH_SHORT).show();
                                             }
                                        });
                            } else {
                            String error = task.getException() != null ? task.getException().getMessage() : "Sign Up Failed";
                                Toast.makeText(SignUpActivity.this, error, Toast.LENGTH_SHORT).show();
                            }
                        });
            } catch (Exception e) {
                Toast.makeText(SignUpActivity.this, "Firebase not configured", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
