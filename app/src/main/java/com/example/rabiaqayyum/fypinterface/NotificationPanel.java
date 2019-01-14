package com.example.rabiaqayyum.fypinterface;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

public class NotificationPanel {

    private Context parent;
    private String songName;
    private NotificationManager nManager;
    private NotificationCompat.Builder nBuilder;
    private RemoteViews remoteView;

    public NotificationPanel(Context parent, String SongName) {
        // TODO Auto-generated constructor stub
        this.parent = parent;
        this.songName=SongName;
        nBuilder = new NotificationCompat.Builder(parent)
                .setContentTitle(songName)
                .setSmallIcon(R.drawable.default_artwork)
                .setOngoing(true);

        remoteView = new RemoteViews(parent.getPackageName(), R.layout.notification_bar);
        //set the button listeners
        setListeners(remoteView);
        nBuilder.setContent(remoteView);

        nManager = (NotificationManager) parent.getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.notify(2, nBuilder.build());
    }
    public void setListeners(RemoteViews view){
        //listener 1
        Intent volume = new Intent(parent,NotificationReturnSlot.class);
        volume.putExtra("DO", "volume");
        PendingIntent btn1 = PendingIntent.getActivity(parent, 0, volume, 0);
        view.setOnClickPendingIntent(R.id.ib_fast_forward, btn1);

        //listener 2
        Intent stop = new Intent(parent, NotificationReturnSlot.class);
        stop.putExtra("DO", "stop");
        PendingIntent btn2 = PendingIntent.getActivity(parent, 1, stop, 0);
        view.setOnClickPendingIntent(R.id.ib_play_pause, btn2);
    }
    public void notificationCancel() {
        nManager.cancel(2);
    }
}