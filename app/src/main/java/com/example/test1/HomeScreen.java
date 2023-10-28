package com.example.test1;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
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

import java.util.Objects;

public class HomeScreen extends AppCompatActivity {
    TextView showNextReminder, showProgress, showDrinkTarget, showTargetReminder;
    ImageView drinkTarget, manualAdd, scanQR, settings;
    TextView text;
    FirebaseDatabase database;
    DatabaseReference reference;
    private long pressedTime;
    private static final String SHARED_PREFS = "sharedPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        // Initialize UI elements
        showNextReminder = findViewById(R.id.showNextReminder);
        showProgress = findViewById(R.id.showProgress);
        showDrinkTarget = findViewById(R.id.showDrinkTarget);
        showTargetReminder = findViewById(R.id.showTargetReminder);
        drinkTarget = findViewById(R.id.drinkTarget);
        manualAdd = findViewById(R.id.manualAdd);
        scanQR = findViewById(R.id.scanQR);
        settings = findViewById(R.id.settings);
        text = findViewById(R.id.text);

        // Initialize Firebase and retrieve the UID
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("users");
        String UID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        // Check for notification permissions (you might want to handle this differently)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(HomeScreen.this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(HomeScreen.this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        // Load data from Shared Preferences
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        int progress = sharedPreferences.getInt("PROGRESS", 0);
        int target = sharedPreferences.getInt("TARGET", 0);
        int reminder = sharedPreferences.getInt("REMINDER", 0);

        // Set text for UI elements
        String progressText = getString(R.string.progress_label, progress);
        String drinkTargetText = getString(R.string.drink_target_label, target);
        String reminderText = getString(R.string.reminder_label, reminder);

        showProgress.setText(progressText);
        showDrinkTarget.setText(drinkTargetText);
        showTargetReminder.setText(reminderText);

        // Start the NotificationService (if it's not already running)
        Intent serviceIntent = new Intent(HomeScreen.this, NotificationService.class);
        serviceIntent.putExtra("reminder", reminder);
        serviceIntent.putExtra("UID", UID);
        startService(serviceIntent);

        // Set up the AlarmReceiver to trigger notifications at specified intervals
        scheduleRepeatingAlarm(reminder);

        reference.child(UID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int progress = Integer.parseInt(String.valueOf(snapshot.child("progress").getValue()));
                int target = Integer.parseInt(String.valueOf(snapshot.child("targetDrink").getValue()));
                int reminder = Integer.parseInt(String.valueOf(snapshot.child("targetReminder").getValue()));

                // Update Shared Preferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("PROGRESS", progress);
                editor.putInt("TARGET", target);
                editor.putInt("REMINDER", reminder);
                editor.apply();

                // Start the NotificationService (if it's not already running)
                Intent serviceIntent = new Intent(HomeScreen.this, NotificationService.class);
                startService(serviceIntent);

//                LocalTime now = LocalTime.now();
//                final LocalTime[] nextReminder = {now.plusHours(reminder / 60).plusMinutes(reminder % 60)};
//                String nowStr = now.format(DateTimeFormatter.ofPattern("HH:mm"));
//                final String[] nextReminderStr = {nextReminder[0].format(DateTimeFormatter.ofPattern("HH:mm"))};
//
//                Handler handler = new Handler(Looper.getMainLooper());
//                Runnable runnableCode = new Runnable() {
//                    @Override
//                    public void run() {
//                        // Compare the current time with the next reminder time
//                        if (nowStr.equals(nextReminderStr[0])) {
//                            // Handle the case when the times are equal, e.g., show a notification
//                            nextReminder[0] = nextReminder[0].plusHours(reminder / 60).plusMinutes(reminder % 60);
//                            nextReminderStr[0] = nextReminder[0].toString();
//
//                            // Show the notification here (you may want to call a function for this)
////                            showNotification();
//                        }
//
//                        // Schedule the code to run again after a delay (e.g., every minute)
//                        handler.postDelayed(this, reminder * 60 * 1000);
//
//                        showNextReminder.setText(nextReminderStr[0]);
//                    }
//                };
//                handler.post(runnableCode);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeScreen.this, "Failed to retrieve data", Toast.LENGTH_SHORT).show();
            }

        });

        drinkTarget.setOnClickListener(view -> {

            View view1 = LayoutInflater.from(HomeScreen.this).inflate(R.layout.target_drink_layout, null);
            TextInputEditText editText = view1.findViewById(R.id.editDrinkTarget);
            AlertDialog alertDialog = new MaterialAlertDialogBuilder(HomeScreen.this)
                    .setTitle("Drink Target (ml)")
                    .setView(view1)
                    .setPositiveButton("OK", (dialogInterface, i) -> {
                        String valTargetDrink = String.valueOf(editText.getText());
                        String UID1 = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                        int value = Integer.parseInt(valTargetDrink);

                        reference.child(UID1).child("targetDrink").setValue(value);
                        Toast.makeText(HomeScreen.this, "Drink Target Saved", Toast.LENGTH_SHORT).show();
                        dialogInterface.dismiss();
                    }).setNegativeButton("Close", (dialogInterface, i) -> dialogInterface.dismiss()).create();
            alertDialog.show();
        });

        manualAdd.setOnClickListener(view -> {
            String UID12 = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
            reference.child(UID12).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    if (task.getResult().exists()){
                        DataSnapshot dataSnapshot = task.getResult();

                        View view1 = LayoutInflater.from(HomeScreen.this).inflate(R.layout.manual_add_layout, null);
                        TextInputEditText editText = view1.findViewById(R.id.editAddDrinkManual);
                        AlertDialog alertDialog = new MaterialAlertDialogBuilder(HomeScreen.this)
                                .setTitle("Drink Volume (ml)")
                                .setView(view1)
                                .setPositiveButton("OK", (dialogInterface, i) -> {
                                    String valAddDrink = String.valueOf(editText.getText());
                                    String progress1 = String.valueOf(dataSnapshot.child("progress").getValue());
                                    String UID121 = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                                    int value = Integer.parseInt(valAddDrink);
                                    int valueProgress = Integer.parseInt(progress1);
                                    int tot = value + valueProgress;

                                    reference.child(UID121).child("progress").setValue(tot);
                                    Toast.makeText(HomeScreen.this, "Drink Target Saved", Toast.LENGTH_SHORT).show();
                                    dialogInterface.dismiss();
                                }).setNegativeButton("Close", (dialogInterface, i) -> dialogInterface.dismiss()).create();
                        alertDialog.show();
                    }
                }else {
                    Toast.makeText(HomeScreen.this, "NOK", Toast.LENGTH_SHORT).show();
                }
            });
        });

        scanQR.setOnClickListener(view -> {
            IntentIntegrator intentIntegrator = new IntentIntegrator(HomeScreen.this);
            intentIntegrator.setOrientationLocked(true);
            intentIntegrator.setCaptureActivity(CaptureActivityPortrait.class);
            intentIntegrator.setPrompt("Scan QR code");
            intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
            intentIntegrator.initiateScan();
        });

        settings.setOnClickListener(view -> {
            Intent intent = new Intent(HomeScreen.this, SettingsScreen.class);
            startActivity(intent);
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
    private void scheduleRepeatingAlarm(int reminder) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Calculate the alarm trigger time in milliseconds (every 'reminder' minutes)
        long intervalMillis = reminder * 60 * 1000;

        // Set the alarm to trigger every 'reminder' minutes, starting immediately
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), intervalMillis, pendingIntent);
    }
}


















