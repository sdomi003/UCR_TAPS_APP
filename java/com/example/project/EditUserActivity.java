package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.data.model.User;
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
    private static User_Information userInfo;
    private static String uid;

    private static final String TAG = "EditUserActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        userInfo = (User_Information) getIntent().getSerializableExtra("personalData");
        Authentication = FirebaseAuth.getInstance();
        user = Authentication.getCurrentUser();
        uid = user.getUid();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);
        TextView textView = findViewById(R.id.First_Name);
        textView.setText(userInfo.AccessFirst());
        TextView textView1 = findViewById(R.id.Last_Name);
        textView1.setText(userInfo.AccessLast());
        TextView textView2 = findViewById(R.id.Phone_Number);
        textView2.setText(userInfo.AccessPhone());
        Spinner FavLot=findViewById(R.id.lot);
        ArrayAdapter Adapt= (ArrayAdapter) FavLot.getAdapter();
        int pos = Adapt.getPosition(userInfo.AccessLot());
        FavLot.setSelection(pos);
        Spinner FavTrans=findViewById(R.id.trans);
        ArrayAdapter Adapt_trans= (ArrayAdapter) FavTrans.getAdapter();
        int pos_trans = Adapt_trans.getPosition(userInfo.AccessTransport());
        FavTrans.setSelection(pos_trans);

        Personal_Update = findViewById(R.id.button);
        Personal_Update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(EditUserActivity.this, HomeScreen.class);
                Spinner Fav = findViewById(R.id.lot);
                Spinner Trans = findViewById(R.id.trans);
                userInfo.UpdatePersonal(uid,((EditText)findViewById(R.id.First_Name)).getText().toString(),
                        ((EditText)findViewById(R.id.Last_Name)).getText().toString(),
                        ((EditText)findViewById(R.id.Phone_Number)).getText().toString(),
                        Fav.getSelectedItem().toString(),
                        Trans.getSelectedItem().toString()
                        );
                //myIntent.putExtra("personalData",userInfo);
                Toast.makeText(EditUserActivity.this, "Personal Information Updated", Toast.LENGTH_LONG).show();
                startActivity(myIntent);
            }
        });
    }
}
