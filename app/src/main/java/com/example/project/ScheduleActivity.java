package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ScheduleActivity extends AppCompatActivity {
   private Button Schedule_Update,Schedule_Delete,Back;
    private FirebaseAuth Authentication;
    private FirebaseUser user;
    private static final String TAG = "ScheduleActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Authentication = FirebaseAuth.getInstance();
        user = Authentication.getCurrentUser();
        String uid = user.getUid();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        DocumentReference docRef = db.collection("User_Information").document(uid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        List<String> monday = (ArrayList<String>) document.get("Monday");
                        List<String> tuesday = (ArrayList<String>) document.get("Tuesday");
                        List<String> wednesday = (List<String>) document.get("Wednesday");
                        List<String> thursday = (List<String>) document.get("Thursday");
                        List<String> friday = (List<String>) document.get("Friday");
                        if(monday.size() != 0)
                        {
                                ListView lv = findViewById(R.id.monday_list);
                                ArrayAdapter<String> monday_adapter = new ArrayAdapter<String>(ScheduleActivity.this,android.R.layout.simple_list_item_1,monday);
                                lv.setAdapter(monday_adapter);
                        }
                        if(tuesday.size() != 0)
                        {
                                ListView lv = findViewById(R.id.tuesday_list);
                                ArrayAdapter<String> tuesday_adapter = new ArrayAdapter<String>(ScheduleActivity.this,android.R.layout.simple_list_item_1,tuesday);
                                lv.setAdapter(tuesday_adapter);
                        }
                        if(wednesday.size() != 0)
                        {
                                ListView lv = findViewById(R.id.wednesday_list);
                                ArrayAdapter<String> wednesday_adapter = new ArrayAdapter<String>(ScheduleActivity.this,android.R.layout.simple_list_item_1,wednesday);
                                lv.setAdapter(wednesday_adapter);
                        }
                        if(thursday.size() != 0)
                        {
                                ListView lv = (ListView) findViewById(R.id.thursday_list);
                                ArrayAdapter<String> thursday_adapter = new ArrayAdapter<String>(ScheduleActivity.this,android.R.layout.simple_list_item_1,thursday);
                                lv.setAdapter(thursday_adapter);
                        }
                        if(friday.size() != 0)
                        {
                                ListView lv = (ListView) findViewById(R.id.friday_list);
                                ArrayAdapter<String> friday_adapter = new ArrayAdapter<String>(ScheduleActivity.this,android.R.layout.simple_list_item_1,friday);
                                lv.setAdapter(friday_adapter);
                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        Schedule_Update = findViewById(R.id.newClass);
        Schedule_Update.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(ScheduleActivity.this,NewClass.class);
                startActivity(myIntent);
            }
        });

        Schedule_Delete = findViewById(R.id.delete);
        Schedule_Delete.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(ScheduleActivity.this,Delete_Class.class);
                startActivity(myIntent);
            }
        });


    }

    @Override
    public void onBackPressed() {
        Intent myIntent = new Intent(this, HomeScreen.class);

        startActivity(myIntent);
    }
}
