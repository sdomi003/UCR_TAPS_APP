package com.example.project;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Button LogSign, NotifTest;
    private Spinner spinner;
    private NotificationManagerCompat notificationManager;
    private static final String[] paths = {"Big Springs Structure", "Lot 6", "Lot 24", "Lot 26", "Lot 30", "Lot 32"};
    public static final String CHANNEL_1_ID = "channel1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNotificationChannels();

        LogSign = findViewById(R.id.Log_Sign_Button);
        LogSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(MainActivity.this, LogSignActivity.class);
                startActivity(myIntent);
            }
        });

        //---------------------------------------------------------------------------------
        //Need to add in dropdown list to xml and variable that saves value of drop down list
        //Need submit button functionality that goes back to MainActivity
        //----------------------------------------------------------------------------------

        spinner = (Spinner) findViewById(R.id.lot_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_spinner_item, paths);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        NotifTest = findViewById(R.id.notifTest);
        NotifTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                addNotificationLot30();
            }
        });

    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(CHANNEL_1_ID, "Lot 30", NotificationManager.IMPORTANCE_DEFAULT);
            channel1.setDescription("Show Lot 30 availability");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel1);
        }
    }

    private void addNotificationLot30() {

        //Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_notif)
                .setContentTitle("EasyPark @ UCR")
                .setContentText("Lot 30 is now 50% free!")
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        //Create notification intent
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);
        builder.setContentIntent(contentIntent);

        //Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        String r = "";
        CallParkingLotAPI callParkingLotAPI;
        switch (position) {
            case 0:
                // Whatever you want to happen when the first item gets selected
                r = "https://streetsoncloud.com/parking/rest/occupancy/id/84?callback=myCallback";
                break;
            case 1:
                // Whatever you want to happen when the second item gets selected
                r = "https://streetsoncloud.com/parking/rest/occupancy/id/238?callback=myCallback";
                break;
            case 2:
                // Whatever you want to happen when the thrid item gets selected
                r = "https://streetsoncloud.com/parking/rest/occupancy/id/243?callback=myCallback";
                break;
            case 3:
                // Whatever you want to happen when the first item gets selected
                r = "https://streetsoncloud.com/parking/rest/occupancy/id/80?callback=myCallback";
                break;
            case 4:
                // Whatever you want to happen when the second item gets selected
                r = "https://streetsoncloud.com/parking/rest/occupancy/id/82?callback=myCallback";
                break;
            case 5:
                // Whatever you want to happen when the thrid item gets selected
                r = "https://streetsoncloud.com/parking/rest/occupancy/id/83?callback=myCallback";
                break;

        }
        callParkingLotAPI = new CallParkingLotAPI();
        callParkingLotAPI.execute(r);
    }


    class CallParkingLotAPI extends AsyncTask<String, Void, String> {
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
            final TextView tv = (TextView) findViewById(R.id.guest_lot_info_view);

            try {
                JSONObject jsonResult = getJSONObject(jsonp);
                String location = getLocation(jsonResult);
                String spots_available = getSpotsAvailable(jsonResult);
                String to_display = location + "\nSpots Available: " + spots_available;
                tv.setText(to_display);
            } catch (Exception e) {
                // failed
                Log.d("ERROR GETTING JSONP FROM RESULT", e.getMessage());
                String errmsg = "Unable to get the live updates for this lot.";
                tv.setText(errmsg);
            }
        }

        private String getSpotsAvailable(JSONObject jsonObject) throws JSONException {
            return jsonObject.getString("free_spaces");
        }

        private JSONObject getJSONObject(String jsonp) throws JSONException {
            String json = jsonp_to_json(jsonp);
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonResults = jsonObject.getJSONArray("results");
            return jsonResults.getJSONObject(0);
        }

        private String getLocation(final JSONObject jsonObject) throws JSONException {
            return jsonObject.getString("location_name");
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

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // TODO Auto-generated method stub
    }
}
