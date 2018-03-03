package pht.eatit.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.Map;
import java.util.Random;
import pht.eatit.RequestList;
import pht.eatit.R;
import pht.eatit.Welcome;
import pht.eatit.global.Global;
import pht.eatit.helper.Notification;

public class FirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if(remoteMessage.getData() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                sendNotification26(remoteMessage);
            } else {
                sendNotification(remoteMessage);
            }
        }
    }

    private void sendNotification26(RemoteMessage remoteMessage) {
        Map<String, String> content = remoteMessage.getData();
        String title = content.get("title");
        String message = content.get("message");

        if(Global.activeUser != null) {
            Intent orderList = new Intent(this, RequestList.class);
            orderList.putExtra("phone", Global.activeUser.getPhone());
            orderList.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, orderList, PendingIntent.FLAG_ONE_SHOT);
            Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            Notification helper = new Notification(this);

            android.app.Notification.Builder builder = helper.getNotification(
                    title,
                    message,
                    pendingIntent,
                    defaultSound
            );

            // Random to show all notifications
            helper.getManager().notify(new Random().nextInt(), builder.build());
        } else {
            Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            Notification helper = new Notification(this);

            android.app.Notification.Builder builder = helper.getNotification(
                    title,
                    message,
                    defaultSound
            );

            // Random to show all notifications
            helper.getManager().notify(new Random().nextInt(), builder.build());
        }
    }

    private void sendNotification(RemoteMessage remoteMessage) {
        Map<String, String> content = remoteMessage.getData();
        String title = content.get("title");
        String message = content.get("message");

        Intent welcome = new Intent(this, Welcome.class);
        welcome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, welcome, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSound)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }
}