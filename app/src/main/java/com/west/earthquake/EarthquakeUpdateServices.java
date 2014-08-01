package com.west.earthquake;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by usr1 on 7/31/14.
 */
public class EarthquakeUpdateServices extends IntentService {

    public static String TAG = "EARTHQUAKE_UPDATE_SERVICE";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    //CAP 9/3
    public EarthquakeUpdateServices(){
        super("EarthquakeUpdateServices");

    }
    //CAP 9/3
    public EarthquakeUpdateServices(String name){
        super(name);
    }

    // cod adaugat in CAP 9/3
    @Override
    protected void onHandleIntent(Intent intent) {
        Context context = getApplicationContext();
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(context);

        int updateFreq =
                Integer.parseInt(prefs.getString(PreferencesActivity.PREF_UPDATE_FREQ_INDEX, "60"));

        boolean autoUpdateChecked =
                prefs.getBoolean(PreferencesActivity.PREF_AUTO_UPDATE, false);

        if (autoUpdateChecked){
            int alarmType = AlarmManager.ELAPSED_REALTIME_WAKEUP;
            long timeToRefresh = SystemClock.elapsedRealtime() +
                    updateFreq*60*1000;
            alarmManager.setInexactRepeating(alarmType, timeToRefresh,
                    updateFreq*60*1000, alarmIntent);
        }
        else
            alarmManager.cancel(alarmIntent);

        refreshEarthquakes();

    }

    //----------- CAP 9/1
    private void addNewQuake(Quake _quake){

        ContentResolver cr = getContentResolver();

        String w = EarthquakeProvider.KEY_DATE + " = " + _quake.getDate().getTime();

        Cursor query = cr.query(EarthquakeProvider.CONTENT_URI, null, w, null, null);
        if (query.getCount() == 0){
            ContentValues values = new ContentValues();

            values.put(EarthquakeProvider.KEY_DATE, _quake.getDate().getTime());
            values.put(EarthquakeProvider.KEY_DETAILS, _quake.getDetails());
            values.put(EarthquakeProvider.KEY_SUMMARY, _quake.toString());

            double lat = _quake.getLocation().getAltitude();
            double lng = _quake.getLocation().getLongitude();
            values.put(EarthquakeProvider.KEY_LOCATION_LAT, lat);
            values.put(EarthquakeProvider.KEY_LOCATION_LNG, lng);
            values.put(EarthquakeProvider.KEY_MAGNITUDE, _quake.getMagnitude());

            cr.insert(EarthquakeProvider.CONTENT_URI, values);
        }
        query.close();

    }

    //--------------CAP 9/1
    public void refreshEarthquakes(){

        URL url;

        try{
            String quakeFeed = getString(R.string.quake_feed);

            url = new URL(quakeFeed);

            URLConnection connection;
            connection = url.openConnection();

            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            int responseCode = httpConnection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK){
                InputStream in = httpConnection.getInputStream();

                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();

                Document dom = db.parse(in);
                Element docEle = dom.getDocumentElement();


                NodeList nl = docEle.getElementsByTagName("entry");
                if (nl != null && nl.getLength()>0){
                    for (int i = 0; i < nl.getLength(); i++){

                        Element entry = (Element) nl.item(i);
                        Element title = (Element) entry.getElementsByTagName("titlse").item(0);
                        Element g = (Element) entry.getElementsByTagName("georss:point").item(0);
                        Element when = (Element) entry.getElementsByTagName("updated").item(0);
                        Element link = (Element) entry.getElementsByTagName("link").item(0);

                        String details = title.getFirstChild().getNodeValue();
                        String hostname = "http://earthquake.usgs.gov";
                        String linkString = hostname + link.getAttribute("href");

                        String point = g.getFirstChild().getNodeValue();
                        String dt = when.getFirstChild().getNodeValue();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd'T'hh:mm:ss'Z'");
                        Date qdate = new GregorianCalendar(0,0,0).getTime();

                        try {
                            qdate = sdf.parse(dt);
                        }catch (ParseException e){
                            Log.e(TAG, "Date parsing exception.", e);
                        }

                        String[] location = point.split(" ");
                        Location l = new Location("dummyGPS");
                        l.setLatitude(Double.parseDouble(location[0]));
                        l.setLongitude(Double.parseDouble(location[1]));

                        String magnitudeString = details.split(" ")[1];
                        int end = magnitudeString.length()-1;
                        double magnitude = Double.parseDouble(magnitudeString.substring(0, end));

                        details = details.split(" , ")[1].trim();

                        Quake quake = new Quake(qdate, details, l, magnitude, linkString);

                        addNewQuake(quake);
                    }
                }
            }
        }catch (MalformedURLException e){
            Log.e(TAG, "Malformed URL Exception", e);
        }catch (IOException e){
            Log.e(TAG, "IO Exception", e);
        }catch (ParserConfigurationException e){
            Log.e(TAG, "Parse Configuration Exception", e);
        }catch (SAXException e){
            Log.e(TAG, "SAX Exception", e);
        }
    }

   // private Timer updateTimer;  //CAB 9/1

//    public int onStartCommand(Intent intent, int flags, int startId) {
//        Context context = getApplicationContext();
//        SharedPreferences prefs =
//                PreferenceManager.getDefaultSharedPreferences(context);
//
//        int updateFreq =
//                Integer.parseInt(prefs.getString(PreferencesActivity.PREF_UPDATE_FREQ_INDEX, "60"));
//        boolean autoUpdateChecked =
//                prefs.getBoolean(PreferencesActivity.PREF_AUTO_UPDATE, false);
//
//        if (autoUpdateChecked) {                                       //---CAP 9/2 start
//            int alarmType = AlarmManager.ELAPSED_REALTIME_WAKEUP;
//            long timeToRefresh = SystemClock.elapsedRealtime() +
//                    updateFreq * 60 * 1000;
//            alarmManager.setInexactRepeating(alarmType, timeToRefresh,
//                    updateFreq * 60 * 1000, alarmIntent);
//        } else
//            alarmManager.cancel(alarmIntent);                             //---CAP 9/2 end
//
//
//        Thread t = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                refreshEarthquakes();
//            }
//        });
//
//        t.start();
//
//        return Service.START_NOT_STICKY;  //---CAP 9/3



          //---CAP 9/1
//        updateTimer.cancel();
//        if (autoUpdateChecked){
//            updateTimer = new Timer("earthquakeUpdates");
//            updateTimer.scheduleAtFixedRate(doRefresh, 0,
//                    updateFreq*60*1000);
//        }
//        else {
//            Thread t = new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    refreshEarthquakes();
//                }
//            });
//            t.start();
//
//        }
       // return Service.START_STICKY;  //CAB 9/1
//    }

    private TimerTask doRefresh = new TimerTask() {
        @Override
        public void run() {
            refreshEarthquakes();
        }
    };

    //---CAP 9/1
//    public void onCreate(){
//        updateTimer = new Timer("earthquakes");
//    }

    private AlarmManager alarmManager; //CAB 9/2
    private PendingIntent alarmIntent; //CAB 9/2

    //---CAP 9/2
    public void onCreate(){
        super.onCreate();
        alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        String ALARM_ACTION =
                EarthquakeAlarmReceiver.ACTION_REFRESH_EARTHQUAKE_ALARM;

        Intent intentToFire = new Intent(ALARM_ACTION);

        alarmIntent =
                PendingIntent.getBroadcast(this, 0, intentToFire, 0);
    }

}
