package pht.eatit.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import pht.eatit.OrderList;
import pht.eatit.R;
import pht.eatit.global.Global;
import pht.eatit.model.Request;

public class OrderListener extends Service implements ChildEventListener {

    FirebaseDatabase database;
    DatabaseReference request;

    public OrderListener() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        database = FirebaseDatabase.getInstance();
        request = database.getReference("Request");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        request.addChildEventListener(this);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        Request child = dataSnapshot.getValue(Request.class);
        showNotification(dataSnapshot.getKey(), child);
    }

    private void showNotification(String key, Request child) {
        Intent intent = new Intent(getBaseContext(), OrderList.class);
        intent.putExtra("phone", child.getPhone());
        PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext());
        builder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setTicker("Hoàng Tâm")
                .setContentInfo("Your order was updated !")
                .setContentText("Order " + key + " was" + Global.convertCodeToStatus(child.getStatus()).toLowerCase())
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher);

        NotificationManager notificationManager = (NotificationManager) getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}
