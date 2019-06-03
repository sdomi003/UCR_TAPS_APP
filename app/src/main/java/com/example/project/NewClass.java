package com.example.project;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class NewClass extends AppCompatActivity {
    private Button AddClass,Time;
    private static String uid;
    private static FirebaseFirestore db;


    static final int DIALOG = 0;
    public int hour,min;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
        FirebaseAuth Authentication = FirebaseAuth.getInstance();
        FirebaseUser user = Authentication.getCurrentUser();
        uid = user.getUid();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_class);

        Time = findViewById(R.id.SetTime);
        Time.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                showDialog(DIALOG);
            }
        });

        AddClass = findViewById(R.id.Add);
        AddClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(NewClass.this, ScheduleActivity.class);

                Spinner schoolDay=findViewById(R.id.schoolday);
                Spinner location=findViewById(R.id.location);
                String ClassName= ((EditText)findViewById(R.id.Class_Name)).getText().toString();
                String Time= hour + "" + min;

                String Entry= location.getSelectedItem().toString() + '-' + Time + '-' + ClassName;
                DocumentReference docRef = db.collection("User_Information").document(uid);
                docRef.update(schoolDay.getSelectedItem().toString(), FieldValue.arrayUnion(Entry));
                startActivity(myIntent);
            }
        });
    }
    @Override
    protected Dialog onCreateDialog(int id){
        if(id == DIALOG){
            return new TimePickerDialog(this,kTimePickerListener,hour,min,true);
        }
        return null;
    }
    protected TimePickerDialog.OnTimeSetListener kTimePickerListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            hour = hourOfDay;
            min = minute;
            TextView textView = findViewById(R.id.Time);
            textView.setText("Time Of Class: \t\t" + hour + ":" + min);
        }
    };
}
