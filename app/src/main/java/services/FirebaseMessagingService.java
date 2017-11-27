package services;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.google.firebase.messaging.RemoteMessage;

import musictag.hytham1.com.mychat.ProfileActivity;
import musictag.hytham1.com.mychat.R;


/**
 * Created by Hytham on 11/17/2017.
 */

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    String click_action ;
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        super.onMessageReceived(remoteMessage);

        String notification_title = remoteMessage.getNotification().getTitle();
        String notification_body = remoteMessage.getNotification().getBody();

         click_action = remoteMessage.getNotification().getClickAction();
        String from_sender_id = remoteMessage.getData().get("from_sender_id").toString();





        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this , "")
                        .setSmallIcon(R.mipmap.mychat)
                        .setContentTitle(notification_title)
                        .setContentText(notification_body)
                        .setOngoing(true)
                         .setWhen(System.currentTimeMillis())

                         ;

        Intent resultIntent = new Intent(click_action);
       resultIntent.putExtra("visit_user_id" , from_sender_id);



        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);



        // Sets an ID for the notification
        int mNotificationId = (int) System.currentTimeMillis();
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.

        mNotifyMgr.notify(mNotificationId, mBuilder.build());




    }


}
