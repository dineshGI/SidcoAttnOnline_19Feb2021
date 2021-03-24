package com.sidcoparking.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.sidcoparking.R;
import com.sidcoparking.activity.MainActivity;

import static android.content.Context.NOTIFICATION_SERVICE;

public class NotificationReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {

        //   playNotificationSound(context);
        if (intent.getExtras() != null) {

            String body = "";

            for (String key : intent.getExtras().keySet()) {

                Object value = intent.getExtras().get(key);

                // Log.e("IFFRAN", "Key: " + key + " Value: " + value);
                if (key.equalsIgnoreCase("gcm.notification.title")) {
                    Log.e(" TITLE: ", value.toString());
                }

                if (key.equalsIgnoreCase("gcm.notification.body")) {
                    body = value.toString();
                    Log.e(" BODY: ", body);
                }

            }

            final String PushMessage = body;

            Util.Logcat.e("PushMessage: " + PushMessage);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Do Work
                    NewOrderNew(PushMessage, context);
                }
            }, 3000);
        }
    }

    private void sendNotificationNew(String remoteMessage, Context con) {
        Intent intent = new Intent(con, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(con, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        String channelId = "Default";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(con, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(con.getString(R.string.app_name))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(remoteMessage))
                .setContentText(remoteMessage);
        NotificationManager manager = (NotificationManager) con.getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Default channel", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }
        manager.notify(0, builder.build());
    }

    private void NewOrderNew(String msg, Context con) {
        Intent intent = new Intent(con, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(con, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        String channelId = "Default";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(con, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                //  .setContentTitle(con.getString(R.string.app_name))
                .setContentTitle(con.getString(R.string.app_name))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setContentText(msg);
        NotificationManager manager = (NotificationManager) con.getSystemService(NOTIFICATION_SERVICE);
        manager.cancelAll();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Default channel", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }
        manager.notify(123, builder.build());
    }
}
