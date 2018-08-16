package com.example.android.instaline;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class NotificationService extends Service {
    private static final int NOTIFICATION_ID = 1234;

    public NotificationService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action.equals("lineApproaching")) {
            Thread thread = new Thread(() -> {
                System.out.println("SHOULD POP UP!!!!!!");

                String restName = intent.getStringExtra("restName");
                int qNum = intent.getIntExtra("qNum", -1);
//                String waitTime = intent.getStringExtra("waitTime");
                String notificationText = "";

                if (qNum == 5) {
                    notificationText = "You are at fifth position.";
                } else if (qNum == 1) {
                    notificationText = "You are the next!";
                }

                if (!notificationText.equals("")) {
                    Notification.Builder builder = new Notification.Builder(NotificationService.this)
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setPriority(Notification.PRIORITY_MAX)
                            .setVibrate(new long[0])
                            .setContentTitle(String.format("%s@Instaline", restName))
                            .setContentText(notificationText)
                            .setSmallIcon(R.drawable.notification_icon);
                    Notification n = builder.build();
                    NotificationManager manager = (NotificationManager)
                            getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.notify(NOTIFICATION_ID, n);
                }
            });
            thread.start();
        }

        return START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
