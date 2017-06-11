package com.trbowrx.pi_clock;

/**
 * Created by vlara on 6/6/17.
 */

import android.text.format.Time;


public interface OnClockTickListner {
    public void OnMinuteTick(Time currentTime);
}
