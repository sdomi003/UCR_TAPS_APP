package com.example.project;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
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

import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Nullable;


public class Lot_Service extends Service {
    private static final String TAG = "Lot_Service";
    public static final String CHANNEL_1_ID = "channel1";
    public int counter = 0;
    public boolean hasCapacity = false;
    protected NotificationManager notifManager;
    private String lotSpotsAvail = "0";
    private int intLotSize;
    private String lotName = "";

    public Lot_Service() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundHelper();
        }
        else
            startForeground(1, new Notification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        Log.d(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startID);
        startTimer();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

        Intent broadcastIntent = new Intent(this, SensorRestarterBroadcastReceiver.class);
        sendBroadcast(broadcastIntent);
        stoptimertask();
    }

    private void findLotSize(int result)
    {
        Lot_Service.GetLotInfoAPI getLotInfoAPI = new Lot_Service.GetLotInfoAPI();
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

    //MAIN FUNCTION FOR GETTING A LOT NOTIFICATION
    private void runLotProgram()
    {
        notifManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        //Search parking lot API
        Log.d(TAG, "findLotSize");
        findLotSize(0);
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
    //--------------TIMER SETUP-----------------/
    //------------------------------------------/


    private Timer timer;
    private TimerTask timerTask;
    long oldTime=0;

    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 1000, 1000);

    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                Log.i("in timer", "in timer ++++  "+ (counter++));

                if(counter % 300 == 0) //5 minutes = 300 seconds, use 10 for testing purposes
                {
                    runLotProgram();
                }
            }
        };
    }

    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
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

        runLotProgram();

    }

    //Main notification button for lot capacity percentage
    private void lotCapacityNotification() {

        Log.d(TAG, "start notification");

        //Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_notif)
                .setContentTitle("EasyPark @ UCR")
                .setContentText(lotName + " is now 30% free, with " + intLotSize + " spaces available!")
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

//                //For testing purposes only
//                if(intLotSize >= percentFree)
//                    lotCapacityNotification();


                if((intLotSize >= percentFree) && !hasCapacity) { //If the parking lot gains capacity, switch hasCapacity to true and send a notification
                    hasCapacity = true;
                    lotCapacityNotification();
                }
                else
                    if ((intLotSize < percentFree) && hasCapacity) { //If the parking lot loses capacity, switch hasCapacity to false

                        hasCapacity = false;
                    } //Do not send a notification again (set boolean hasCapacity
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