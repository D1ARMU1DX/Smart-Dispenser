package com.example.test1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class RegisterScreen extends AppCompatActivity {

    TextInputEditText editTextEmail;
    TextInputEditText editTextPassword;
    Button btnRegister;
    TextView login;
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_screen);

        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        btnRegister = findViewById(R.id.btnRegister);
        login = findViewById(R.id.login);

        mAuth = FirebaseAuth.getInstance();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterScreen.this, LoginScreen.class);
                startActivity(intent);
                finish();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                database = FirebaseDatabase.getInstance();
                reference = database.getReference("users");

                String email = editTextEmail.getText().toString();
                String password = editTextPassword.getText().toString();
                try {
                    String encPass = EncryptDecrypt.encrypt(password);
                    if (!validateEmail() | !validatePassword()) {
                    } else {
                        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            String UID;
                            int progress = 0;
                            int targetDrink = 2000;
                            int targetReminder = 60;
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()){
                                    UID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                                    HelperClass helperClass = new HelperClass(UID, encPass);
                                    reference.child(UID).setValue(helperClass);
                                    reference.child(UID).child("progress").setValue(progress);
                                    reference.child(UID).child("targetDrink").setValue(targetDrink);
                                    reference.child(UID).child("targetReminder").setValue(targetReminder);

                                    Toast.makeText(RegisterScreen.this, "Registration complete", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(RegisterScreen.this, LoginScreen.class);
                                    startActivity(intent);
                                    finish();
                                }else {
                                    Toast.makeText(RegisterScreen.this, "Email is already registered", Toast.LENGTH_SHORT).show();
                                    editTextPassword.setText("");
                                }
                            }
                        });
                    }
                } catch (InvalidAlgorithmParameterException | NoSuchPaddingException |
                         IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException |
                         InvalidKeyException e) {
                    e.printStackTrace();
                }
                btnRegister.onEditorAction(EditorInfo.IME_ACTION_DONE);
            }
        });
    }
    public Boolean validateEmail() {
        String val = editTextEmail.getText().toString();
        if (val.isEmpty()) {
            editTextEmail.setError("Email cannot be empty");
            return false;
        } else if (val.length() < 10) {
            editTextEmail.setError("Enter Valid Email");
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
        } else if (val.length() < 7) {
            editTextPassword.setError("Password should be greater than 7 character");
            return false;
        } else {
            editTextPassword.setError(null);
            return true;
        }
    }
}