package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class EditUserActivity extends AppCompatActivity {

    private Button Personal_Update;
    private FirebaseAuth Authentication;
    private FirebaseUser user;
    private static final String TAG = "EditUserActivity";
    private static UserInfo userinfo

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Authentication = FirebaseAuth.getInstance();
        user = Authentication.getCurrentUser();
        String uid = user.getUid();
        userinfo = (UserInfo) getIntent().getSerializableExtra("personalData");


        DocumentReference docRef = db.collection("User_Information").document(uid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        TextView textView = (TextView) findViewById(R.id.First_Name); // first name                  
                        textView.setText(userInfo.AccessFirst());
                        TextView textView1 = findViewById(R.id.Last_Name); 
                        textView1.setText(userInfo.AccessLast());
                        TextView textView2 = findViewById(R.id.Phone_Number);
                        textView2.setText(userInfo.AccessPhone());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);

        Personal_Update = findViewById(R.id.button);
        Personal_Update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(EditUserActivity.this, EditUserActivity.class);
                //Call function that updates personal data                                                        //Once this button is clicked, should call an update function that updates First Name
                                                                                                                 //Last Name and Phone Number even if changes were not made
                                                                                                                // Function is not implemented yet
                startActivity(myIntent);
            }
        });
    }
}
