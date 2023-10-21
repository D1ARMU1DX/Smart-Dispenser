package com.example.test1;

import static android.app.ProgressDialog.show;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class LoginScreen extends AppCompatActivity {

    TextInputEditText editTextEmail;
    TextInputEditText editTextPassword;
    Button btnLogin;
    TextView register;
    FirebaseAuth mAuth;

    public static final String SHARED_PREFS = "sharedPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        rememberMe();

        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        btnLogin = findViewById(R.id.btnLogin);
        register = findViewById(R.id.register);

        mAuth = FirebaseAuth.getInstance();

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginScreen.this, RegisterScreen.class);
                startActivity(intent);
                finish();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = editTextEmail.getText().toString();
                String password = editTextPassword.getText().toString();

                if (!validateEmail() | !validatePassword()) {

                } else {
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString("name", "true");
                                        editor.apply();

                                        Toast.makeText(getApplicationContext(), "Login Success", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getApplicationContext(), HomeScreen.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(LoginScreen.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
                btnLogin.onEditorAction(EditorInfo.IME_ACTION_DONE);
            }
        });
    }

    public Boolean validateEmail() {
        String val = editTextEmail.getText().toString();
        if (val.isEmpty()) {
            editTextEmail.setError("Email cannot be empty");
            return false;
        } else {
            editTextEmail.setError(null);
            return true;
        }
    }

    public Boolean validatePassword() {
        String val = editTextPassword.getText().toString();
        if (val.isEmpty()) {
            editTextPassword.setError("Password cannot be empty");
            return false;
        } else {
            editTextPassword.setError(null);
            return true;
        }
    }

    private void rememberMe() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        String check = sharedPreferences.getString("name", "true");

        if (check.equals("true")) {
            Toast.makeText(LoginScreen.this, "Login Success", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginScreen.this, HomeScreen.class);
            startActivity(intent);
            finish();
        }
    }
}