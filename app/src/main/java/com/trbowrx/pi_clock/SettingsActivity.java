package com.trbowrx.pi_clock;


import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.view.MenuItem;
import com.google.android.things.contrib.driver.ht16k33.Ht16k33;


import java.io.IOException;
import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {
    private static final String TAG = AppCompatPreferenceActivity.class.getSimpleName();
    private static final String I2C_BUS = BoardDefaults.getI2CPort();

    private Ht16k33 mSegmentDisplay;
    //Used to set Army Time: True = Army Time
    public Boolean hour_setting;
    public Integer displayPeriod = 1;

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            Log.d("Pi-Clock", stringValue);
            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        hour_setting = sharedPref.getBoolean("24_hour_switch", true);
        if (hour_setting) {
            displayPeriod = 0;
        }
        String timezone_setting = sharedPref.getString("timezone_list", "");
        Log.d("Pi-Clock", "Hour Setting " + hour_setting);
        Log.d("Pi-Clock", "TimeZone Setting: " + timezone_setting);

        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        am.setTimeZone("America/Los_Angeles");
        if (mSegmentDisplay == null) {
            try {
                mSegmentDisplay = new Ht16k33(I2C_BUS);
                mSegmentDisplay.setBrightness(.1f);
                mSegmentDisplay.setEnabled(true);
                clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Clock c = new Clock(this);

        c.AddClockTickListner(new OnClockTickListner() {
            @Override
            public void OnMinuteTick(Time currentTime) {
                Log.d("Tick Test per Minute", DateFormat.format("h:mm aa", currentTime.toMillis(true)).toString());

                String currTime = DateFormat.format("hhmm", currentTime.toMillis(true)).toString();
                String ampm = DateFormat.format("aa", currentTime.toMillis(true)).toString();
                Log.d(TAG, "APAM: " + ampm);
                if (hour_setting) {
                    currTime = DateFormat.format("HHmm", currentTime.toMillis(true)).toString();
                }

                int i = 0;
                for (char c : currTime.toCharArray()) {
                    short dispVal = (short) Font.myMap.get(String.valueOf(c)).intValue();
                    if (i == 4  && ampm.equals("PM")) {
                        Log.d(TAG, "equals PM");
                        dispVal =  (short) (dispVal | displayPeriod.intValue() << 7);
                    }
                    try {
                        mSegmentDisplay.writeColumn(i,dispVal);
                    } catch (IOException e) {
                        Log.d(TAG, "error: " + e);
                    }
                    i++;
                    //This is the colon Column so skip it
                    if (i == 2) {
                        i++;
                    }

                }
            }
        });
        //Log.d(TAG, "Testing");
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            //bindPreferenceSummaryToValue(findPreference("timezone_text"));
            bindPreferenceSummaryToValue(findPreference("timezone_list"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            Log.d("Pi-Clock", "Clicked on: " + id);
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSegmentDisplay != null) {

            Log.i(TAG, "Closing display");
            try {
                clear();
                mSegmentDisplay.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing display", e);
            } finally {
                mSegmentDisplay = null;
            }
        }
    }

    public void clear() throws IOException {
        for (int i = 0; i <= 4; i++) {
            mSegmentDisplay.writeColumn(i, (short) 0);
        }
    }
}
