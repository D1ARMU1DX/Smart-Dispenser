package com.example.test1;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class HomeScreen extends AppCompatActivity {
    TextView showNextReminder, showProgress, showDrinkTarget, showTargetReminder;
    ImageView drinkTarget, manualAdd, scanQR, settings;
    TextView text;
    FirebaseDatabase database;
    DatabaseReference reference;
    SharedPreferences sharedPreferences;
    private long pressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

//        showNextReminder = findViewById(R.id.showNextReminder);
        showProgress = findViewById(R.id.showProgress);
        showDrinkTarget = findViewById(R.id.showDrinkTarget);
        showTargetReminder = findViewById(R.id.showTargetReminder);
        drinkTarget = findViewById(R.id.drinkTarget);
        manualAdd = findViewById(R.id.manualAdd);
        scanQR = findViewById(R.id.scanQR);
        settings = findViewById(R.id.settings);
        text = findViewById(R.id.text);

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("users");
        String UID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        sharedPreferences = getSharedPreferences("Data", MODE_PRIVATE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if (ContextCompat.checkSelfPermission(HomeScreen.this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(HomeScreen.this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        reference.child(UID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int progress = Integer.parseInt(String.valueOf(snapshot.child("progress").getValue()));
                int target = Integer.parseInt(String.valueOf(snapshot.child("targetDrink").getValue()));
                int reminder = Integer.parseInt(String.valueOf(snapshot.child("targetReminder").getValue()));

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("PROGRESS", progress);
                editor.putInt("TARGET", target);
                editor.putInt("REMINDER", reminder);
                editor.apply();

                String progressText = getString(R.string.progress_label, progress);
                String drinkTargetText = getString(R.string.drink_target_label, target);
                String reminderText = getString(R.string.reminder_label, reminder);

                scheduleRepeatingAlarm(reminder);
                checkNextReminderTime(UID, reminder, reminderText);//

                showProgress.setText(progressText);
                showDrinkTarget.setText(drinkTargetText);
                showTargetReminder.setText(reminderText);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeScreen.this, "Failed to retrieve data", Toast.LENGTH_SHORT).show();
            }
        });

        drinkTarget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                View view1 = LayoutInflater.from(HomeScreen.this).inflate(R.layout.target_drink_layout, null);
                TextInputEditText editText = view1.findViewById(R.id.editDrinkTarget);
                AlertDialog alertDialog = new MaterialAlertDialogBuilder(HomeScreen.this)
                        .setTitle("Drink Target (ml)")
                        .setView(view1)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String valTargetDrink = editText.getText().toString();
                                String UID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                                int value = Integer.parseInt(valTargetDrink);

                                reference.child(UID).child("targetDrink").setValue(value);
                                Toast.makeText(HomeScreen.this, "Drink Target Saved", Toast.LENGTH_SHORT).show();
                                dialogInterface.dismiss();
                            }
                        }).setNegativeButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).create();
                alertDialog.show();
            }
        });

        manualAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String UID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                reference.child(UID).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (task.isSuccessful()){
                            if (task.getResult().exists()){
                                DataSnapshot dataSnapshot = task.getResult();

                                View view1 = LayoutInflater.from(HomeScreen.this).inflate(R.layout.manual_add_layout, null);
                                TextInputEditText editText = view1.findViewById(R.id.editAddDrinkManual);
                                AlertDialog alertDialog = new MaterialAlertDialogBuilder(HomeScreen.this)
                                        .setTitle("Drink Volume (ml)")
                                        .setView(view1)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                String valAddDrink = editText.getText().toString();
                                                String progress = String.valueOf(dataSnapshot.child("progress").getValue());
                                                String UID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                                                int value = Integer.parseInt(valAddDrink);
                                                int valueProgress = Integer.parseInt(progress);
                                                int tot = value + valueProgress;

                                                reference.child(UID).child("progress").setValue(tot);
                                                Toast.makeText(HomeScreen.this, "Drink Target Saved", Toast.LENGTH_SHORT).show();
                                                dialogInterface.dismiss();
                                            }
                                        }).setNegativeButton("Close", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();
                                            }
                                        }).create();
                                alertDialog.show();
                            }
                        }else {
                            Toast.makeText(HomeScreen.this, "NOK", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        scanQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator intentIntegrator = new IntentIntegrator(HomeScreen.this);
                intentIntegrator.setOrientationLocked(true);
                intentIntegrator.setCaptureActivity(CaptureActivityPortrait.class);
                intentIntegrator.setPrompt("Scan QR code");
                intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
                intentIntegrator.initiateScan();
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeScreen.this, SettingsScreen.class);
                startActivity(intent);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null){
            String contents = intentResult.getContents();
            if (contents != null){
                text.setText(intentResult.getContents());
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    @Override
    public void onBackPressed() {
        if (pressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
            finish();
        } else {
            Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT).show();
        }
        pressedTime = System.currentTimeMillis();
    }
    private void scheduleRepeatingAlarm(int reminderInt) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // Create an intent to trigger your BroadcastReceiver
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Set the alarm to trigger every X minutes
        long intervalMillis = reminderInt * 60 * 1000;
        long triggerTime = System.currentTimeMillis();

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerTime, intervalMillis, alarmIntent);
    }
    private String checkNextReminderTime(String UID, int reminderInt, String nextReminderStr) {
        final Handler handler = new Handler(Looper.getMainLooper());
        Runnable runnableCode = new Runnable() {
            @Override
            public void run() {
                // Get the current time
                LocalTime now = LocalTime.now();
                LocalTime midnight = LocalTime.of(0, 0); // 00:00
                LocalTime nextReminder = LocalTime.now().plusHours(reminderInt / 60).plusMinutes(reminderInt % 60);
                String nowStr = now.format(DateTimeFormatter.ofPattern("HH:mm"));
                String midnightStr = midnight.toString();
                String nextReminderStr = nextReminder.toString();

                // Midnight
                if (nowStr.equals(midnightStr)) {
                    reference.child(UID).child("progress").setValue(0);
                    nextReminder = LocalTime.of(5, 0); // 05:00
                    nextReminderStr = nextReminder.toString();
                }

                // Compare the current time with the next reminder time
                if (nowStr.equals(nextReminder.toString())) {
                    // Handle the case when the times are equal, e.g., show a notification
                    nextReminder = nextReminder.plusHours(reminderInt / 60).plusMinutes(reminderInt % 60);
                    nextReminderStr = nextReminder.toString();
                }

                // Schedule the code to run again after a delay (e.g., every minute)
                handler.postDelayed(this, 60 * 1000); // 1 minute
            }
        };
        handler.post(runnableCode);
        // Return the calculated nextReminderStr
        return nextReminderStr;
    }


}


















