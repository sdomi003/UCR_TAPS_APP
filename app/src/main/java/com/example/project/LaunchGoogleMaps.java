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
        switch (next_class_loc) {
            case "Winston Chung Hall":
                gmmIntentUri = Uri.parse("google.navigation:q=Parking+Lot+24+Riverside,+CA+92507");
                break;
            case "Bourns Hall":
                gmmIntentUri = Uri.parse("google.navigation:q=Parking+Lot+24+Riverside,+CA+92507");
                break;
            case "Sproul Hall":
                gmmIntentUri = Uri.parse("google.navigation:q=Parking+Lot+30+Riverside,+CA+92507");
                break;
            case "Watkins Hall":
                gmmIntentUri = Uri.parse("google.navigation:q=Parking+Lot+30+Riverside,+CA+92507");
                break;
            case "Pierce Hall":
                gmmIntentUri = Uri.parse("google.navigation:q=Parking+Lot+30+Riverside,+CA+92507");
                break;
            default:
                next_class_loc.replace(' ','+');
                String url= "google.navigation:q=Parking+"+next_class_loc+"+Riverside,+CA+92507";
                gmmIntentUri = Uri.parse(url);
                break;
        }

        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }


}


