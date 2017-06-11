package com.trbowrx.pi_clock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.text.format.Time;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by vlara on 6/5/17.
 * based from https://stackoverflow.com/questions/6527474/how-to-create-a-digital-clock-on-android
 */

public class Clock {

    private Time time;
    private TimeZone timeZone;
    private Handler handler;
    private List<OnClockTickListner> OnClockTickListenerList = new ArrayList<OnClockTickListner>();

    private Runnable Ticker;

    private BroadcastReceiver intentReceiver;
    private IntentFilter intentFilter;

    private int tickMethod = 0;
    Context context;

    public Clock(Context context) {
        this.context = context;
        this.time = new Time();
        this.time.setToNow();

        this.StartTickPerMinute();
    }

    private void Tick(long tickInMillis) {
        Clock.this.time.set(Clock.this.time.toMillis(true)+tickInMillis);
        this.NotifyOnTickListners();
    }

    private void NotifyOnTickListners() {
        for (OnClockTickListner listner:OnClockTickListenerList) {
            listner.OnMinuteTick(time);
        }
    }

    private void StartTickPerMinute() {
        this.intentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Tick(60000);
            }
        };
        this.intentFilter = new IntentFilter();
        this.intentFilter.addAction(Intent.ACTION_TIME_TICK);
        this.context.registerReceiver(this.intentReceiver, this.intentFilter,null, this.handler);

    }

    public void StopTick() {
        if (this.intentReceiver != null) {
            this.context.unregisterReceiver(this.intentReceiver);
        }
        if (this.handler != null) {
            this.handler.removeCallbacks(this.Ticker);
        }
    }

    public Time GetCurrentTime() {
        return this.time;
    }

    public void AddClockTickListner(OnClockTickListner listner) {
        this.OnClockTickListenerList.add(listner);
    }

}
