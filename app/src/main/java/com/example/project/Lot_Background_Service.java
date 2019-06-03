package com.example.project;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.lang.String;


public class Lot_Background_Service extends IntentService {
    private static final String TAG = "Lot_Background_Service";
    public static final String CHANNEL_1_ID = "channel1";
    private PowerManager.WakeLock wakeLock;
    private String lotSpotsAvail = "0";
    private int intLotSize;
    private String lotName = "";

    public Lot_Background_Service() {
        super(TAG);
        setIntentRedelivery(true);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ExampleApp:WakeLock");
        wakeLock.acquire(6000000);
        Log.d(TAG, "wakelock acquired");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundHelper();
        }
        else
            startForeground(1, new Notification());
    }

    @Override
    public void onHandleIntent(Intent intent) {

        //Search parking lot API
        Log.d(TAG, "findLotSize");
        findLotSize(0);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

        wakeLock.release();
        Log.d(TAG, "Wakelock released");
    }

    private void findLotSize(int result)
    {
        GetLotInfoAPI getLotInfoAPI = new GetLotInfoAPI();
        String r = "";

        //List of lots to choose from
        if(result == 0)
                r = "https://streetsoncloud.com/parking/rest/occupancy/id/84?callback=myCallback";
        else
            if(result == 1)
                r = "https://streetsoncloud.com/parking/rest/occupancy/id/238?callback=myCallback";
            else
                if(result == 2)
                    r = "https://streetsoncloud.com/parking/rest/occupancy/id/243?callback=myCallback";
                else
                    if(result == 3)
                        r = "https://streetsoncloud.com/parking/rest/occupancy/id/80?callback=myCallback";
                    else
                        if(result == 4)
                            r = "https://streetsoncloud.com/parking/rest/occupancy/id/82?callback=myCallback";
                        else
                            if(result == 5)
                                r = "https://streetsoncloud.com/parking/rest/occupancy/id/83?callback=myCallback";


        getLotInfoAPI.execute(r);
    }

    //Helper function that sets selectedLot from GetLotInfoAPI class
    private void returnLotInfo(String temp1, String temp2)
    {
        lotSpotsAvail = temp1;
        lotName = temp2;
        Log.d(TAG, "returnLotInfo: lotSpotsAvail = " + lotSpotsAvail + " lotName = " + lotName);
    }

    //Used for getting the maximum lot capacity
    private int FindLotMaxCapacity(String toCheck)
    {
        if(toCheck.equals("Lot 30"))
            return 2188;
        else
            if(toCheck.equals("Lot 6") )
                return 328;
            else
                if(toCheck.equals("LOT 24"))
                    return 387;
                else
                    if(toCheck.equals("Big Springs Structure"))
                        return 559;


        return 0;
    }


    //------------------------------------------/
    //----------NOTIFICATION SETUPS-------------/
    //------------------------------------------/

    //Set up notification channels
    private void startForegroundHelper() {
        NotificationChannel channel1 = new NotificationChannel(CHANNEL_1_ID, "Lot Service Channel", NotificationManager.IMPORTANCE_DEFAULT);
        channel1.setDescription("Show Lot availability");

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel1);

    }

    //Main notification button for lot capacity percentage
    private void lotCapacityNotification() {

        Log.d(TAG, "start notification");

        //Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_notif)
                .setContentTitle("EasyPark @ UCR")
                .setContentText(lotName + " is now 25% free, with " + intLotSize + " spaces available!")
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        //Create notification intent
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);
        builder.setContentIntent(contentIntent);

        //Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }

    //------------------------------------------/
    //---------------GET LOT INFO---------------/
    //------------------------------------------/
    class GetLotInfoAPI extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                return callURL(urls[0]);
            } catch (Exception e) {
                return "";
            }

        }

        @Override
        protected void onPostExecute(String jsonp) {
            try
            {
                JSONObject jsonResult = getJSONObject(jsonp);
                String spots_available = getSpotsAvailable(jsonResult);
                String location = getLocation(jsonResult);
                Log.d(TAG, spots_available + " " + location); //test

                returnLotInfo(spots_available, location);
                intLotSize = Integer.valueOf(lotSpotsAvail);
                double percentFree = FindLotMaxCapacity(location) * 0.30;

                if (intLotSize > percentFree) {
                    lotCapacityNotification();
                }
            }
            catch (Exception e)
            {
                // failed
                Log.d("ERROR GETTING JSON FROM RESULT", e.getMessage());
                returnLotInfo( "Unable to get the live updates for this lot." , "");
            }
        }

        private String getSpotsAvailable(JSONObject jsonObject) throws JSONException {
            return jsonObject.getString("free_spaces");
        }

        private String getLocation(final JSONObject jsonObject) throws JSONException {
            return jsonObject.getString("location_name");
        }

        private JSONObject getJSONObject(String jsonp) throws JSONException {
            String json = jsonp_to_json(jsonp);
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonResults = jsonObject.getJSONArray("results");
            return jsonResults.getJSONObject(0);
        }

        private String callURL(String myURL) {
            System.out.println("Requested URL:" + myURL);
            StringBuilder sb = new StringBuilder();
            URLConnection urlConn = null;
            InputStreamReader in = null;
            try {
                URL url = new URL(myURL);
                urlConn = url.openConnection();
                if (urlConn != null)
                    urlConn.setReadTimeout(60 * 1000);
                if (urlConn != null && urlConn.getInputStream() != null) {
                    in = new InputStreamReader(urlConn.getInputStream(),
                            Charset.defaultCharset());
                    BufferedReader bufferedReader = new BufferedReader(in);
                    if (bufferedReader != null) {
                        int cp;
                        while ((cp = bufferedReader.read()) != -1) {
                            sb.append((char) cp);
                        }
                        bufferedReader.close();
                    }
                }
                in.close();
            } catch (Exception e) {
                throw new RuntimeException("Exception while calling URL:" + myURL, e);
            }

            return sb.toString();
        }

        private String jsonp_to_json(final String jsonp) {
            int left = jsonp.indexOf('(') + 1;
            int right = jsonp.length() - 1;
            return jsonp.substring(left, right);
        }
    }
}