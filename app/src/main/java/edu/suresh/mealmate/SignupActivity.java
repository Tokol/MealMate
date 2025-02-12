package edu.suresh.mealmate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignupActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextInputEditText etEmail, etChoosePassword, etConfirmPassword;
    private MaterialButton btnSignUp;
    private MaterialTextView tvSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Views
        etEmail = findViewById(R.id.etEmail);
        etChoosePassword = findViewById(R.id.etChoosePassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvSignIn = findViewById(R.id.tvSignIn);

        // Sign Up Button Click
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser(v);
            }
        });

        // Already Have an Account? Redirect to LoginActivity
        tvSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
           //     Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void registerUser(View view) {
        String email = etEmail.getText().toString().trim();
        String password = etChoosePassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validate Fields
        if (TextUtils.isEmpty(email)) {
            showSnackbar(view, "Please enter your email");
            return;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            showSnackbar(view, "Password must be at least 6 characters");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showSnackbar(view, "Passwords do not match");
            return;
        }

        // Firebase Signup
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Signup Success
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserId(user.getUid());
                            showSnackbar(view, "Signup Successful! Redirecting...");
                            // Redirect to LoginActivity after success
                            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                            finish();
                        }
                    } else {
                        // Signup Failed
                        showSnackbar(view, "Signup Failed: " + task.getException().getMessage());
                    }
                });
    }

    private void saveUserId(String userId) {
        SharedPreferences sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("USER_ID", userId);
        editor.apply();
    }

    private void showSnackbar(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
    }
}
