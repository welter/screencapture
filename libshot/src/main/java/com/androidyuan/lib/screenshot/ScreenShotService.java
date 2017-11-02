package com.androidyuan.lib.screenshot;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by welter on 17年11月1日.
 */

public class ScreenShotService extends Service {
    private static Intent ResultIntent;
    private static Shotter shotter;
    private Handler handler;
    private Runnable runnable;

    public static class AlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
    /*        Intent i = new Intent(context, ScreenShotService.class);
            i.putExtra("ResultIntent",ResultIntent);
            context.startService(i);*/
            shotter.startScreenShot(new Shotter.OnShotListener() {
                @Override
                public void onFinish() {
//                toast("shot finish!");

                }
            },Shotter.ResultType.RTNet);
        }
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(Context.ACTIVITY_SERVICE,"Service start.");
        if (ResultIntent==null & intent!=null ) ResultIntent= (Intent) intent.getParcelableExtra("ResultIntent");
        if (shotter==null) shotter=new Shotter(this,ResultIntent);
        //commented by welter 11-1
        /*shotter.startScreenShot(new Shotter.OnShotListener() {
            @Override
            public void onFinish() {
//                toast("shot finish!");

            }
        },Shotter.ResultType.RTNet);*/
        //commented by welter 11-2
        /*AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int five = 5000; // 这是5s
        long triggerAtTime = SystemClock.elapsedRealtime() + five;
        Intent i = new Intent(this, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        manager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime,1000, pi);*/


        handler.postDelayed(runnable, 200);//每半秒执行一次runnable.
        return Service.START_REDELIVER_INTENT; //super.onStartCommand(intent, (int) Service.START_FLAG_REDELIVERY, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        handler=new Handler();


        runnable=new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                shotter.startScreenShot(new Shotter.OnShotListener() {
                    @Override
                    public void onFinish() {
//                toast("shot finish!");

                    }
                },Shotter.ResultType.RTNet);
                handler.postDelayed(this, 300);
            }
        };
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
