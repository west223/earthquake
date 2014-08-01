package com.west.earthquake;


import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.SimpleCursorAdapter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


public class EarthQuake extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>{

    //ArrayAdapter<Quake> aa;
    ArrayList<Quake> earthquakes = new ArrayList<Quake>();
    SimpleCursorAdapter adapter;

    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);


        //---------- CAP 8 pag 308---------------
        adapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_list_item_1, null,
                new String[] { EarthquakeProvider.KEY_SUMMARY},
                new int[] { android.R.id.text1 }, 0);
        setListAdapter(adapter);

        getLoaderManager().initLoader(0, null, this);

        refreshEarthquakes();

    }

    private static final String TAG = "EARTHQUAKE";


    android.os.Handler handler = new android.os.Handler();


    public void refreshEarthquakes(){

        getLoaderManager().restartLoader(0, null, EarthQuake.this);

        getActivity().startService(new Intent(getActivity(), EarthquakeUpdateServices.class));
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = new String[] {
                EarthquakeProvider.KEY_ID,
                EarthquakeProvider.KEY_SUMMARY
        };

        MainActivity earthquakeActivity = (MainActivity)getActivity();
        String where = EarthquakeProvider.KEY_MAGNITUDE + " > " +
                earthquakeActivity.minimumMagnitude;

        CursorLoader loader = new CursorLoader(getActivity(),
                EarthquakeProvider.CONTENT_URI, projection, where, null, null);

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        adapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

}
