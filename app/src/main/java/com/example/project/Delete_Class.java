package com.example.project;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Delete_Class extends AppCompatActivity {

    private Button Done;
    private FirebaseAuth Authentication;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private static final String TAG = "ScheduleActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
        Authentication = FirebaseAuth.getInstance();
        user = Authentication.getCurrentUser();
        String uid = user.getUid();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete__class);

        Done = findViewById(R.id.done);
        Done.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(Delete_Class.this,ScheduleActivity.class);
                startActivity(myIntent);
            }
        });

        final DocumentReference docRef = db.collection("User_Information").document(uid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        final List<String> monday = (ArrayList<String>) document.get("Monday");
                        final List<String> tuesday = (ArrayList<String>) document.get("Tuesday");
                        final List<String> wednesday = (List<String>) document.get("Wednesday");
                        final List<String> thursday = (List<String>) document.get("Thursday");
                        final List<String> friday = (List<String>) document.get("Friday");
                        if(monday.size() != 0)
                        {
                            ListView lv = findViewById(R.id.monday_list);
                            final ArrayAdapter<String> monday_adapter = new ArrayAdapter<String>(Delete_Class.this,android.R.layout.simple_list_item_1,monday);
                            lv.setAdapter(monday_adapter);
                            lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

                                @Override
                                public boolean onItemLongClick(AdapterView<?> parent, View view,
                                                               int position, long id) {
                                    delete(docRef,monday.get(position),"Monday");

                                    monday.remove(position);

                                    monday_adapter.notifyDataSetChanged();

                                    Toast.makeText(Delete_Class.this, "Class Removed", Toast.LENGTH_LONG).show();

                                    return true;
                                }

                            });
                        }
                        if(tuesday.size() != 0)
                        {
                            ListView lv = findViewById(R.id.tuesday_list);
                            final ArrayAdapter<String> tuesday_adapter = new ArrayAdapter<String>(Delete_Class.this,android.R.layout.simple_list_item_1,tuesday);
                            lv.setAdapter(tuesday_adapter);
                            lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

                                @Override
                                public boolean onItemLongClick(AdapterView<?> parent, View view,
                                                               int position, long id) {
                                    delete(docRef,tuesday.get(position),"Tuesday");

                                    tuesday.remove(position);

                                    tuesday_adapter.notifyDataSetChanged();

                                    Toast.makeText(Delete_Class.this, "Class Removed", Toast.LENGTH_LONG).show();

                                    return true;
                                }

                            });
                        }
                        if(wednesday.size() != 0)
                        {
                            ListView lv = findViewById(R.id.wednesday_list);
                            final ArrayAdapter<String> wednesday_adapter = new ArrayAdapter<String>(Delete_Class.this,android.R.layout.simple_list_item_1,wednesday);
                            lv.setAdapter(wednesday_adapter);
                            lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

                                @Override
                                public boolean onItemLongClick(AdapterView<?> parent, View view,
                                                               int position, long id) {
                                    delete(docRef,wednesday.get(position),"Wednesday");

                                    wednesday.remove(position);

                                    wednesday_adapter.notifyDataSetChanged();

                                    Toast.makeText(Delete_Class.this, "Class Removed", Toast.LENGTH_LONG).show();

                                    return true;
                                }

                            });
                        }
                        if(thursday.size() != 0)
                        {
                            ListView lv = findViewById(R.id.thursday_list);
                            final ArrayAdapter<String> thursday_adapter = new ArrayAdapter<String>(Delete_Class.this,android.R.layout.simple_list_item_1,thursday);
                            lv.setAdapter(thursday_adapter);
                            lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

                                @Override
                                public boolean onItemLongClick(AdapterView<?> parent, View view,
                                                               int position, long id) {
                                    delete(docRef,thursday.get(position),"Thursday");

                                    thursday.remove(position);

                                    thursday_adapter.notifyDataSetChanged();

                                    Toast.makeText(Delete_Class.this, "Class Removed", Toast.LENGTH_LONG).show();

                                    return true;
                                }

                            });
                        }
                        if(friday.size() != 0)
                        {
                            ListView lv = findViewById(R.id.friday_list);
                            final ArrayAdapter<String> friday_adapter = new ArrayAdapter<String>(Delete_Class.this,android.R.layout.simple_list_item_1,friday);
                            lv.setAdapter(friday_adapter);
                            lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

                                @Override
                                public boolean onItemLongClick(AdapterView<?> parent, View view,
                                                               int position, long id) {
                                    delete(docRef,friday.get(position),"Friday");

                                    friday.remove(position);

                                    friday_adapter.notifyDataSetChanged();

                                    Toast.makeText(Delete_Class.this, "Class Removed", Toast.LENGTH_LONG).show();

                                    return true;
                                }

                            });
                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    public void delete(DocumentReference doc, String entry, String date) {
        doc.update(date,FieldValue.arrayRemove(entry));
    }
}
