package com.example.project;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

public class HomeScreen extends AppCompatActivity {

    private FirebaseUser user;
    private Button updatePersonal, updateSchedule,google_maps,log;
    private String nextLocation;
    private static User_Information userInfo;

    private static final String TAG = "User";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        user = getInstance().getCurrentUser();
        String uid = user.getUid();

        DocumentReference docRef = db.collection("User_Information").document(uid);            //CRASH

        super.onCreate(savedInstanceState);
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

        //----------------------------------------------------      Update Personal Information Button
        updatePersonal = findViewById(R.id.UpdatePersonalOption);
        updatePersonal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(HomeScreen.this, EditUserActivity.class);
                myIntent.putExtra("personalData",userInfo);
                startActivity(myIntent);
            }
        });
        //--------------------------------------------------------------------

        // ---------------------------------------------         Update Schedule Information button
        updateSchedule = findViewById(R.id.UpdateScheduleOption);
        updateSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(HomeScreen.this, ScheduleActivity.class);
                startActivity(myIntent);
            }
        });
        // -----------------------------------------------------------

        google_maps =  findViewById(R.id.launch_google_maps);
        google_maps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent;
                if(nextLocation == "N/A"){
                    myIntent = new Intent(HomeScreen.this, HomeScreen.class);
                }
                else {
                    myIntent = new Intent(HomeScreen.this, LaunchGoogleMaps.class);
                    if (userInfo.AccessLot().equals("None")) {
                        myIntent.putExtra("nextClass", nextLocation);
                        startActivity(myIntent);
                    } else {
                        //CallParkingLotAPI favParkingLotAPI = new HomeScreen.CallParkingLotAPI();
                        //favParkingLotAPI.execute(LotToAPI(userInfo.AccessLot()));
                        //CallParkingLotAPI actualParkingLotAPI = new HomeScreen.CallParkingLotAPI();
                        //actualParkingLotAPI.execute(LotToAPI(nextLocation));
                        //if()
                        myIntent.putExtra("nextClass", nextLocation);
                        startActivity(myIntent);
                    }
                }
            }
        });

        log = findViewById(R.id.log);
        log.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                signOut();
                Intent myIntent = new Intent(HomeScreen.this,MainActivity.class);
                startActivity(myIntent);
            }
        });
    }

    private String nextClass(User_Information user) {
        List<String> classes = user.AccessClass();
        SimpleDateFormat simpleDateformat = new SimpleDateFormat("HHmm");
        int time =Integer.parseInt(simpleDateformat.format(new Date()));
        if(user.AccessDay().equals("Sunday") || user.AccessDay().equals("Saturday"))
        {
            return "N/A";
        }
        String nextClassLocation = "N/A";
        for (int a = 0; nextClassLocation == "N/A" && a < classes.size(); a++) {
            int firstDash = classes.get(a).indexOf('-');
            int classTime = Integer.parseInt(classes.get(a).substring(firstDash + 1, classes.get(a).lastIndexOf('-')));
            if(time < classTime) {
                nextClassLocation = classes.get(a).substring(0,firstDash);
            }
        }
        return nextClassLocation;
    }

    //-------------------------------------------------------------------- Copy and Paste of Preferred Lot
    class CallParkingLotAPI extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                return callURL(urls[0]);
            } catch (Exception e) {
                return "";
            }

        }

        @Override                               //FIX ME
        protected void onPostExecute(String jsonp) {
            try {
                JSONObject jsonResult = getJSONObject(jsonp);
                String spots_available = getSpotsAvailable(jsonResult);                     ///FIX ME
            } catch (Exception e) {
                // failed
                Log.d("ERROR GETTING JSONP FRO" +
                        "M RESULT", e.getMessage());
            }
        }

        private String getSpotsAvailable(JSONObject jsonObject) throws JSONException {
            return jsonObject.getString("free_spaces");
        }

      /*  private String AccessSpots() throws JSONException {                             //FIX ME

        }
*/
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

    private String LotToAPI(String lot) {
        switch(lot){
            case "Big Springs Structure":
            // Whatever you want to happen when the first item gets selected
            return "https://streetsoncloud.com/parking/rest/occupancy/id/84?callback=myCallback";

            case "Lot 6":
            // Whatever you want to happen when the second item gets selected
            return "https://streetsoncloud.com/parking/rest/occupancy/id/238?callback=myCallback";

            case "Lot 24":
            case "Winston Chung Hall":
            case "Bourns Hall":
            // Whatever you want to happen when the thrid item gets selected
            return "https://streetsoncloud.com/parking/rest/occupancy/id/243?callback=myCallback";

            case "Lot 26":
            // Whatever you want to happen when the first item gets selected
            return "https://streetsoncloud.com/parking/rest/occupancy/id/80?callback=myCallback";

            case "Lot 30":
            case "Sproul Hall":
            case "Watkins Hall":
            case "Pierce Hall":
            // Whatever you want to happen when the second item gets selected
            return "https://streetsoncloud.com/parking/rest/occupancy/id/82?callback=myCallback";

            case "Lot 32":
            // Whatever you want to happen when the thrid item gets selected
            return "https://streetsoncloud.com/parking/rest/occupancy/id/83?callback=myCallback";

        }
        return "";
    }

    private void signOut() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this,gso);
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                    }
                });
    }
}
