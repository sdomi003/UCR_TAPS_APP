package com.example.project;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.Context;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.google.firebase.auth.FirebaseAuth.getInstance;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
public class HomeScreen extends AppCompatActivity {

    private FirebaseAuth Authentication;
    private Button google_maps,button;
    private String nextLocation;                                                //0-------------------- test
    private static User_Information userInfo;
    private static final String TAG = "User";

    Intent serviceIntent;
    private static final String TAG_2 = "lyft:Example";
    private static final String LYFT_PACKAGE = "me.lyft.android";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Authentication = getInstance();
        String uid = Authentication.getCurrentUser().getUid();

        DocumentReference docRef = db.collection("User_Information").document(uid);

        //Start notification service pt 1
        Log.d(TAG, "Initiate new intent");
        serviceIntent = new Intent(this, Lot_Service.class);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        addListenerOnButton();
        startService(new Intent(getBaseContext(),MyService.class));
        //------------------------------------------------------
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        userInfo = new User_Information(document);

                        String favLot = userInfo.AccessLot(); //Pass user's favorite lot
                        serviceIntent.putExtra("Favorite Lot", favLot);
                        Log.d(TAG, "HomeScreen favLot: " + favLot);

                        Lot_Service lotService = new Lot_Service(); //Start notification service pt 2
                        if (!isMyServiceRunning(lotService.getClass())) {
                            startService(serviceIntent);
                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
                nextLocation = nextClass(userInfo);
                //System.out.println(nextLocation);
                TextView textView = findViewById(R.id.Greeting);
                textView.setText("Hello " + userInfo.AccessFirst() +"!");
                ListView lv = findViewById(R.id.today_classes);
                if(nextLocation != "N/A") {
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(HomeScreen.this, android.R.layout.simple_list_item_1, userInfo.AccessClass());
                    lv.setAdapter(adapter);
                }
            }
        });
        //----------------------------------------------------

        setContentView(R.layout.activity_home_screen);


        google_maps =  findViewById(R.id.launch_google_maps);
        google_maps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextLocation = nextClass(userInfo);
                String lot_to_route_to = "None";

                Intent myIntent;

                if(nextLocation == "N/A"){
                    myIntent = new Intent(HomeScreen.this, HomeScreen.class);
                }
                else {
                    String preferred_lot = userInfo.AccessLot();
                    myIntent = new Intent(HomeScreen.this, LaunchGoogleMaps.class);
                    if (userInfo.AccessLot().equals("None")) {

                        LaunchNearestClass launchNearestClass = new LaunchNearestClass();
                        launchNearestClass.execute(nextLocation);
                    } else {
                        // try the preferred lot
                        String preferred_lot_URL = lot_to_URL(preferred_lot);
                        TryPreferredLot tryPreferredLot = new TryPreferredLot();
                        tryPreferredLot.execute(preferred_lot_URL);

                    }
                }
            }
        });

        findViewById(R.id.LyftButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deepLinkIntoLyft();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_options, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //passes unison pr list to differencelistactivity
        if (id == R.id.updateScheduleMenuOp) {
            System.out.println("Hit unison button");
            Intent myIntent = new Intent(this, ScheduleActivity.class);

            startActivity(myIntent);
        }

        if (id == R.id.updatePersonalMenuOp) {
            System.out.println("Hit unison button");
            Intent myIntent = new Intent(this, EditUserActivity.class);
            myIntent.putExtra("personalData",userInfo);

            startActivity(myIntent);
        }
        if (id == R.id.logOp) {
            signOut();
            Intent myIntent = new Intent(HomeScreen.this,MainActivity.class);
            startActivity(myIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    private void deepLinkIntoLyft() {
        if (isPackageInstalled(this, LYFT_PACKAGE)) {
            //This intent will help you to launch if the package is already installed
            String l = lot_to_DeepLink(nextClass(userInfo));
            openLink(this, l);

            Log.d(TAG, "Lyft is already installed on your phone.");
        } else {
            openLink(this, "https://www.lyft.com/signup/SDKSIGNUP?clientId=YOUR_CLIENT_ID&sdkName=android_direct");

            Log.d(TAG, "Lyft is not currently installed on your phone..");
        }
    }

    static void openLink(Activity activity, String link) {
        Intent playStoreIntent = new Intent(Intent.ACTION_VIEW);
        playStoreIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        playStoreIntent.setData(Uri.parse(link));
        activity.startActivity(playStoreIntent);
    }

    static boolean isPackageInstalled(Context context, String packageId) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packageId, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            // ignored.
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        stopService(serviceIntent);
        Log.d(TAG, "onDestroy");
        super.onDestroy();;
    }

    private String lot_to_URL(String preferred_lot) {
        String r;
        switch (preferred_lot) {
            case "Big Springs Structure":
                // Whatever you want to happen when the first item gets selected
                r = "https://streetsoncloud.com/parking/rest/occupancy/id/84?callback=myCallback";
                break;
            case "Lot 6":
                // Whatever you want to happen when the second item gets selected
                r = "https://streetsoncloud.com/parking/rest/occupancy/id/238?callback=myCallback";
                break;
            case "Lot 24":
                // Whatever you want to happen when the thrid item gets selected
                r = "https://streetsoncloud.com/parking/rest/occupancy/id/243?callback=myCallback";
                break;
            case "Lot 26":
                // Whatever you want to happen when the first item gets selected
                r = "https://streetsoncloud.com/parking/rest/occupancy/id/80?callback=myCallback";
                break;
            case "Lot 30":
                // Whatever you want to happen when the second item gets selected
                r = "https://streetsoncloud.com/parking/rest/occupancy/id/82?callback=myCallback";
                break;
            case "Lot 32":
                // Whatever you want to happen when the thrid item gets selected
                r = "https://streetsoncloud.com/parking/rest/occupancy/id/83?callback=myCallback";
                break;
            default:
                r = "ERROR NO PREFERRED LOT";
        }
        return r;
    }

    private String lot_to_DeepLink(String next_class_loc) {
        String r;
        switch (next_class_loc) {
            case "Winston Chung Hall":
                // Whatever you want to happen when the first item gets selected
                r = "lyft://ridetype?id=lyft&destination[latitude]=33.9753&destination[longitude]=-117.3259";
                break;
            case "Bourns Hall":
                // Whatever you want to happen when the second item gets selected
                r = "lyft://ridetype?id=lyft&destination[latitude]=33.9753&destination[longitude]=-117.3269";
                break;
            case "Sproul Hall":
                // Whatever you want to happen when the thrid item gets selected
                r = "lyft://ridetype?id=lyft&destination[latitude]=33.9718&destination[longitude]=-117.3298";
                break;
            case "Watkins Hall":
                // Whatever you want to happen when the first item gets selected
                r = "lyft://ridetype?id=lyft&destination[latitude]=33.9718&destination[longitude]=-117.3298";
                break;
            case "Pierce Hall":
                // Whatever you want to happen when the second item gets selected
                r = "lyft://ridetype?id=lyft&destination[latitude]=33.9740&destination[longitude]=-117.3275";
                break;
            default:
                r = "lyft://ridetype?id=lyft&destination[latitude]=33.9718&destination[longitude]=-117.3298";
                break;
        }
        return r;
    }

    class TryPreferredLot extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                String preferred_lot_URL = urls[0];
                String jsonp = callURL(preferred_lot_URL);
                JSONObject jsonResult = getJSONObject(jsonp);
                String spots_available = getSpotsAvailable(jsonResult);
                return spots_available + " " + preferred_lot_URL;
            } catch (Exception e) {
                return "";
            }

        }

        @Override
        protected void onPostExecute(String spots_and_lot) {

            try {
                String strings[] = spots_and_lot.split(" ", 2);
                String spots_available = strings[0];
                String next_lot_URL = strings[1];
                if (Integer.parseInt(spots_available) > 0) {
                    Intent myIntent = new Intent(HomeScreen.this, LaunchGoogleMaps.class);
                    String next_lot_URL_and_preferred_transport = next_lot_URL + " " + userInfo.AccessTransport();
                    myIntent.putExtra("next_lot_URL_and_preferred_transport", next_lot_URL_and_preferred_transport);
                    startActivity(myIntent);
                } else {
                    LaunchNearestClass launchNearestClass = new LaunchNearestClass();
                    launchNearestClass.execute(nextLocation);
                }

            } catch (Exception e) {
                // failed
                Log.d("ERROR GETTING JSONP FROM RESULT", e.getMessage());
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


    class LaunchNearestClass extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                String next_class_loc = urls[0];
                String closest_URL = getClosestURL(next_class_loc);
                String jsonp = callURL(closest_URL);
                JSONObject jsonResult = getJSONObject(jsonp);
                String spots_available = getSpotsAvailable(jsonResult);
                if (Integer.parseInt(spots_available) <= 0) {
                    closest_URL = "https://streetsoncloud.com/parking/rest/occupancy/id/84?callback=myCallback";
                }

                return closest_URL;
            } catch (Exception e) {
                return "";
            }

        }

        private String getClosestURL(String next_class_loc) {
            String r;
            switch (next_class_loc) {
                case "Winston Chung Hall":
                    // Whatever you want to happen when the first item gets selected
                    r = "https://streetsoncloud.com/parking/rest/occupancy/id/243?callback=myCallback";
                    break;
                case "Bourns Hall":
                    // Whatever you want to happen when the second item gets selected
                    r = "https://streetsoncloud.com/parking/rest/occupancy/id/243?callback=myCallback";
                    break;
                case "Sproul Hall":
                    // Whatever you want to happen when the thrid item gets selected
                    r = "https://streetsoncloud.com/parking/rest/occupancy/id/82?callback=myCallback";
                    break;
                case "Watkins Hall":
                    // Whatever you want to happen when the first item gets selected
                    r = "https://streetsoncloud.com/parking/rest/occupancy/id/82?callback=myCallback";
                    break;
                case "Pierce Hall":
                    // Whatever you want to happen when the second item gets selected
                    r = "https://streetsoncloud.com/parking/rest/occupancy/id/82?callback=myCallback";
                    break;
                default:
                    r = "";
                    break;
            }
            return r;
        }

        @Override
        protected void onPostExecute(String closestURL) {

            try {
                String next_lot_URL_and_preferred_transport = closestURL + " " + userInfo.AccessTransport();
                Intent myIntent = new Intent(HomeScreen.this, LaunchGoogleMaps.class);
                myIntent.putExtra("next_lot_URL_and_preferred_transport", next_lot_URL_and_preferred_transport);
                startActivity(myIntent);

            } catch (Exception e) {
                // failed
                Log.d("ERROR GETTING JSONP FROM RESULT", e.getMessage());
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

    private String nextClass(User_Information user) {
        List<String> classes = user.AccessClass();
        SimpleDateFormat simpleDateformat = new SimpleDateFormat("HHmm");
        int time =Integer.parseInt(simpleDateformat.format(new Date()));
        int earliest_time=2400;
        if(user.AccessDay().equals("Sunday") || user.AccessDay().equals("Saturday"))
        {
            return "N/A";
        }
        String nextClassLocation = "N/A";
        for (int a = 0; a < classes.size(); a++) {
            String nice = classes.get(a).replace(":","");
            int firstDash = nice.indexOf('-');
            int classTime = Integer.parseInt(nice.substring(firstDash + 1, nice.lastIndexOf('-')));
            if(time < classTime && classTime < earliest_time) {
                nextClassLocation = nice.substring(0,firstDash);
                earliest_time = classTime;
            }
        }
        return nextClassLocation;
    }

    public void addListenerOnButton() {

        button = (Button) findViewById(R.id.weblink);

        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://flexport.ucr.edu/ebusiness/Account/Portal"));
                startActivity(browserIntent);

            }

        });

    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("Service status", "Running");
                return true;
            }
        }
        Log.i("Service status", "Not running");
        return false;
    }

    private void signOut() {
        Authentication.getInstance().signOut();
    }

    @Override
    public void onBackPressed() {
        Intent myIntent = new Intent(this, HomeScreen.class);

        startActivity(myIntent);
    }

}
