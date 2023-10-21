package com.example.test1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class SettingsScreen extends AppCompatActivity {
    Button btnOK;
    Button btnLogOut;
    NumberPicker numberPickerHour, numberPickerMint;
    FirebaseDatabase database;
    DatabaseReference reference;
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String HOUR = "HH";
    public static final String MIN = "mm";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_screen);

        btnOK = findViewById(R.id.btnOK);
        btnLogOut = findViewById(R.id.btnLogOut);

        numberPickerHour = findViewById(R.id.numPickHour);
        numberPickerMint = findViewById(R.id.numPickMint);

        numberPickerHour.setMinValue(0);
        numberPickerHour.setMaxValue(12);
        numberPickerMint.setMinValue(0);
        numberPickerMint.setMaxValue(60);

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int hour, mint, targetReminder;
                database = FirebaseDatabase.getInstance();
                reference = database.getReference("users");

                hour = numberPickerHour.getValue() * 60;
                mint = numberPickerMint.getValue();
                targetReminder = hour + mint;
                String UID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                reference.child(UID).child("targetReminder").setValue(targetReminder);

                Date dateAndTime = Calendar.getInstance().getTime();
                SimpleDateFormat currH = new SimpleDateFormat("HH", Locale.getDefault());
                SimpleDateFormat currM = new SimpleDateFormat("mm", Locale.getDefault());
                String nowH = currH.format(dateAndTime);
                String nowM = currM.format(dateAndTime);

                Intent intent = new Intent(getApplicationContext(), HomeScreen.class);
                intent.putExtra(HOUR, nowH);
                intent.putExtra(MIN, nowM);
                startActivity(intent);
                finish();
            }
        });

        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("name", "");
                editor.apply();

                Toast.makeText(SettingsScreen.this, "Log Out Successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), LoginScreen.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (data.getBooleanExtra("EXIT", false)) {
                finish();
            }
        }
    }
}