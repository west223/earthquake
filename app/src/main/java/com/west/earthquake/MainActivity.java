package com.west.earthquake;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

/**
 * Created by usr1 on 7/29/14.
 */
public class MainActivity extends Activity {

    static final private int MENU_PREFERENCES = Menu.FIRST+1;
    static final private int MENU_UPDATE = Menu.FIRST+2;

    private static final int SHOW_PREFERENCES = 1;

    public int minimumMagnitude = 0;
    public boolean autoUpdateChecked = false;
    public int updateFreq = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        updateFromPreferences();

        // CAP 8 / 3
        SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);

        SearchableInfo searchableInfo = searchManager.getSearchableInfo(getComponentName());

        SearchView searchView = (SearchView)findViewById(R.id.searchView);
        searchView.setSearchableInfo(searchableInfo);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, MENU_PREFERENCES, Menu.NONE, R.string.menu_preferences);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        super.onOptionsItemSelected(item);
        switch (item.getItemId()){
            case (MENU_PREFERENCES): {
                Intent i = new Intent(this, PreferencesActivity.class);
                startActivityForResult(i, SHOW_PREFERENCES);
                return true;
            }
        }
        return false;
    }

    private void updateFromPreferences(){
        Context context = getApplicationContext();
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(context);

        int minMagIndex = prefs.getInt(PreferencesActivity.PREF_MIN_MAG_INDEX, 0);
        if (minMagIndex<0)
            minMagIndex = 0;

        int freqIndex = prefs.getInt(PreferencesActivity.PREF_UPDATE_FREQ_INDEX, 0);
        if (freqIndex<0)
            freqIndex = 0;

        autoUpdateChecked = prefs.getBoolean(PreferencesActivity.PREF_AUTO_UPDATE, false);

        Resources r = getResources();
        String[] minMagValues = r.getStringArray(R.array.magnitude);
        String[] freqValues = r.getStringArray(R.array.update_freq_values);

        minimumMagnitude = Integer.valueOf(minMagValues[minMagIndex]);
        updateFreq = Integer.valueOf(freqValues[freqIndex]);
    }

//    //  V2.2 (Preferences)
//    private void updateFromPreferences(){
//        Context context = getApplicationContext();
//        SharedPreferences prefs =
//                PreferenceManager.getDefaultSharedPreferences(context);
//        minimumMagnitude =
//                Integer.parseInt(prefs.getString(PreferencesActivity.PREF_MIN_MAG, "3"));
//        updateFreq =
//                Integer.parseInt(prefs.getString(PreferencesActivity.PREF_UPDATE_FREQ, "60"));
//
//        autoUpdateChecked = prefs.getBoolean(PreferencesActivity.PREF_AUTO_UPDATE, false);
//    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SHOW_PREFERENCES)
            if (resultCode == Activity.RESULT_OK){
                updateFromPreferences();
                FragmentManager fm = getFragmentManager();
                final EarthQuake earthquakeList =
                        (EarthQuake)fm.findFragmentById(R.id.EarthquakeListFragment);

                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        earthquakeList.refreshEarthquakes();
                    }
                });
                t.start();
            }
    }

//       //  V2.2 (Preferences) onActivityResult
//    public void onActivityResult(int requestCode, int resultCode, Intent data){
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode==SHOW_PREFERENCES)
//            updateFromPreferences();
//
//        FragmentManager fm = getFragmentManager();
//        final EarthQuake earthquakeList = (EarthQuake)fm.findFragmentById(R.id.EarthQuake);
//
//        Thread t = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                earthquakeList.refreshEarthquakes();
//            }
//        });
//        t.start();
//
//    }

}
