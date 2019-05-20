package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.google.firebase.auth.FirebaseAuth.getInstance;

public class HomeScreen extends AppCompatActivity {

    private FirebaseUser user;
    private Button updatePersonal, updateSchedule,google_maps;
    private String nextLocation;                                                //0-------------------- test
    private User_Information userInfo;

    private static final String TAG = "User";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        user = getInstance().getCurrentUser();
        String uid = user.getUid();

        DocumentReference docRef = db.collection("User_Information").document(uid);

        super.onCreate(savedInstanceState);
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
                TextView textView = findViewById(R.id.Greeting);
                textView.setText("Hello " + userInfo.AccessFirst() +"!");
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
                else{
                    myIntent = new Intent(HomeScreen.this, ScheduleActivity.class);      // CHANGE ACTIVITY DESTINATION
                    myIntent.putExtra("nextClass",nextLocation);
                }
                startActivity(myIntent);
            }
        });

        //---------------------------------------------
    }

    private String nextClass(User_Information user) {
        List<String> classes = user.AccessClass();
        SimpleDateFormat simpleDateformat = new SimpleDateFormat("HHmm");
        int time =Integer.parseInt(simpleDateformat.format(new Date()));
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
}
