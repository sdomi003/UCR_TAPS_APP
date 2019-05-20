package com.example.project;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class LaunchGoogleMaps extends AppCompatActivity {
    String next_class_loc;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Uri gmmIntentUri;
        next_class_loc = getIntent().getStringExtra("nextClass");

        String nextClass = next_class_loc.replace(' ', '+');
        nextClass = nextClass + "+Riverside,+California";
        nextClass = "google.navigation:q=" + nextClass;
        gmmIntentUri = Uri.parse(nextClass);

        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }
}
