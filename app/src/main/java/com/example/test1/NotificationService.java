package com.example.test1;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class NotificationService extends Service {
    private static final int ALARM_INTERVAL_MINUTES = 1; // Set your desired interval in minutes

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        scheduleRepeatingAlarm();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void scheduleRepeatingAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        long intervalMillis = ALARM_INTERVAL_MINUTES * 60 * 1000;
        long triggerTime = System.currentTimeMillis();

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerTime, intervalMillis, alarmIntent);
    }
}

