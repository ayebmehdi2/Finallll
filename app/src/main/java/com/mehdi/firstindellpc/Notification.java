package com.mehdi.firstindellpc;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

public class Notification {

    private Context ctx;
    private Class classAct;

    public Notification(Context mCtx, Class classAct){
        this.ctx = mCtx;
        this.classAct  = classAct;
    }

    private NotificationManager notificationManager;

    public void notify(String bannerTxt, String titulo, String txt, int icon, int id){

        notificationManager =
                (NotificationManager) this.ctx
                        .getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent pendingIntent =
                PendingIntent.getActivity
                        (
                                ctx.getApplicationContext(),
                                0,
                                new Intent
                                        (
                                                ctx.getApplicationContext(),
                                                this.classAct
                                        ),
                                0
                        );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this.ctx.getApplicationContext());

        builder.setSmallIcon(icon);
        builder.setTicker(bannerTxt);
        builder.setContentTitle(titulo);
        builder.setVibrate(new long[]{100, 100, 100, 100});
        builder.setContentIntent(pendingIntent);
        builder.setContentText(txt);


        assert notificationManager != null;
        notificationManager.notify(id, builder.build());


    }

    public void cancelAll(){
        if (notificationManager != null){
            notificationManager.cancelAll();
        }

    }

}