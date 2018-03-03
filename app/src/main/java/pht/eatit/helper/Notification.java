package pht.eatit.helper;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;

import pht.eatit.R;

public class Notification extends ContextWrapper {

    private static final String CHANNEL_ID = "pht.eatit.PHT";
    private static final String CHANNEL_NAME = "Eat It";

    private NotificationManager manager;

    public Notification(Context base) {
        super(base);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createChannel();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
        );

        channel.enableLights(false);
        channel.enableVibration(true);
        channel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(channel);
    }

    public NotificationManager getManager() {
        if(manager == null){
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }

        return manager;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public android.app.Notification.Builder getNotification(String title, String body, PendingIntent pendingIntent, Uri sound){
        return new android.app.Notification.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(body)
                .setSound(sound)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false);
    }

    @TargetApi(Build.VERSION_CODES.O)
    public android.app.Notification.Builder getNotification(String title, String body, Uri sound){
        return new android.app.Notification.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(body)
                .setSound(sound)
                .setAutoCancel(false);
    }
}