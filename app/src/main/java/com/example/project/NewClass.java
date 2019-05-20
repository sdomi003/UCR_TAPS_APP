package com.example.project;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class NewClass extends AppCompatActivity {
    private Button AddClass;
    private static String uid;
    private static FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
        FirebaseAuth Authentication = FirebaseAuth.getInstance();
        FirebaseUser user = Authentication.getCurrentUser();
        uid = user.getUid();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_class);

        AddClass = findViewById(R.id.Add);
        AddClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(NewClass.this, ScheduleActivity.class);

                Spinner schoolDay=findViewById(R.id.schoolday);
                Spinner location=findViewById(R.id.location);
                String ClassName= ((EditText)findViewById(R.id.Class_Name)).getText().toString();
                String Time=  ((EditText)findViewById(R.id.Time)).getText().toString();

                String Entry= location.getSelectedItem().toString() + '-' + Time + '-' + ClassName;
                DocumentReference docRef = db.collection("User_Information").document(uid);
                docRef.update(schoolDay.getSelectedItem().toString(), FieldValue.arrayUnion(Entry));
                startActivity(myIntent);
            }
        });
    }
}
