package com.west.earthquake;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

/**
 * Created by usr1 on 7/30/14.
 */
public class PreferencesActivity extends Activity {

      //--------------- V 2.2 Preferences ------------------------

//    public static final String PREF_MIN_MAG = "PREF_MIN_MAG";
//    public static final String PREF_UPDATE_FREQ = "PREF_UPDATE_FREQ";
//    public static final String PREF_AUTO_UPDATE = "PREF_AUTO_UPDATE";
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        addPreferencesFromResource(R.xml.userpreferences);
//    }
//   -------------------------------------------------------------

    CheckBox autoUpdate;
    Spinner updateFreqSpinner;
    Spinner magnitudeSpinner;

    public static final String USER_PREFERENCES = "USER_PREFERENCES";
    public static final String PREF_AUTO_UPDATE = "PREF_AUTO_UPDATE";
    public static final String PREF_MIN_MAG_INDEX = "PREF_MIN_MAG_INDEX";
    public static final String PREF_UPDATE_FREQ_INDEX = "PREF_UPDATE_FREQ_INDEX";

    SharedPreferences prefs;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preferences);

        updateFreqSpinner = (Spinner)findViewById(R.id.spinner_update_freq);
        magnitudeSpinner = (Spinner)findViewById(R.id.spinner_quake_mag);
        autoUpdate = (CheckBox)findViewById(R.id.checkbox_auto_update);

        populateSpinner();

        Context context = getApplicationContext();
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        updateUIFromPreferences();

        Button okButton = (Button)findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePreferences();
                PreferencesActivity.this.setResult(RESULT_OK);
                finish();
            }
        });
    }

    private void savePreferences(){

        int updateIndex = updateFreqSpinner.getSelectedItemPosition();
        int minMagIndex = magnitudeSpinner.getSelectedItemPosition();
        boolean autoUpdateChecked = autoUpdate.isChecked();

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PREF_AUTO_UPDATE, autoUpdateChecked);
        editor.putInt(PREF_UPDATE_FREQ_INDEX, updateIndex);
        editor.putInt(PREF_MIN_MAG_INDEX, minMagIndex);
        editor.commit();
    }

    private void updateUIFromPreferences(){
        boolean autoUpChecked = prefs.getBoolean(PREF_AUTO_UPDATE, false);
        int updateFreqIndex = prefs.getInt(PREF_UPDATE_FREQ_INDEX, 2);
        int minMagIndex = prefs.getInt(PREF_MIN_MAG_INDEX, 0);

        updateFreqSpinner.setSelection(updateFreqIndex);
        magnitudeSpinner.setSelection(minMagIndex);
        autoUpdate.setChecked(autoUpChecked);
    }

    private void populateSpinner(){
        ArrayAdapter<CharSequence> fAdapter;
        fAdapter = ArrayAdapter.createFromResource(this, R.array.update_freq_options,
                android.R.layout.simple_spinner_item);

        int spinner_dd_item = android.R.layout.simple_spinner_dropdown_item;
        fAdapter.setDropDownViewResource(spinner_dd_item);

        updateFreqSpinner.setAdapter(fAdapter);


        ArrayAdapter<CharSequence> mAdapter;
        mAdapter = ArrayAdapter.createFromResource(this,
                R.array.magnitude_options, android.R.layout.simple_spinner_item);

        mAdapter.setDropDownViewResource(spinner_dd_item);
        magnitudeSpinner.setAdapter(mAdapter);
    }
}
