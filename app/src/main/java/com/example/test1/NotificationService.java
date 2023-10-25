package com.example.test1;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class NotificationService extends Service {

    private Handler handler;
    private int reminder; // Store the reminder value
    private String nextReminderStr;
    private String UID; // Your user ID
    private DatabaseReference reference; // Your Firebase reference
    private static final String CHANNEL_ID = "CHANNEL_ID_NOTIFICATION"; // Notification channel ID

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        reference = FirebaseDatabase.getInstance().getReference(); // Initialize your Firebase reference
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            reminder = intent.getIntExtra("reminder", 0);
            UID = intent.getStringExtra("UID"); // Get user ID
            startReminderTimer();
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startReminderTimer() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                // Get the current time
                LocalTime now = LocalTime.now();
                String nowStr = now.format(DateTimeFormatter.ofPattern("HH:mm"));

                // Calculate the next reminder time based on your preference
                LocalTime nextReminder;
                nextReminder = now.plusHours(reminder / 60).plusMinutes(reminder % 60);
                nextReminderStr = nextReminder.format(DateTimeFormatter.ofPattern("HH:mm"));

                // Get midnight time
                LocalTime midnight = LocalTime.of(0, 0);
                String midnightStr = midnight.toString();

                // Check if the current time matches midnight
                if (nowStr.equals(midnightStr)) {
                    // Handle the case when the current time is midnight
                    // For example, reset progress and set the next reminder time to 05:00
                    reference.child(UID).child("progress").setValue(0);
                    nextReminder = LocalTime.of(5, 0);
                    nextReminderStr = nextReminder.toString();
                }

                // Check if the current time matches the next reminder time
                if (nowStr.equals(nextReminderStr)) {
                    // Handle the reminder/notification shown
                    showNotification(); // Call the function to show the notification
                    nextReminder = nextReminder.plusHours(reminder / 60).plusMinutes(reminder % 60);
                    nextReminderStr = nextReminder.format(DateTimeFormatter.ofPattern("HH:mm"));
                }

                // Schedule the code to run again after a delay (e.g., every minute)
                handler.postDelayed(this, 60 * 1000);
            }
        });
    }

    private void showNotification() {
        // Create and display the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Drink Bro")
                .setContentText("Udah jam segini")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        Intent notificationIntent = new Intent(this, HomeScreen.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "Notification Channel", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.GREEN);
            notificationChannel.enableVibration(true);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        // Notify the user
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }
}


