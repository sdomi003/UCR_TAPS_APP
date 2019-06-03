package com.example.project;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class LaunchGoogleMaps extends AppCompatActivity {
    String next_class_url;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Uri gmmIntentUri;
        String next_lot_URL_and_preferred_transport = getIntent().getStringExtra("next_lot_URL_and_preferred_transport");

        String arr[] = next_lot_URL_and_preferred_transport.split(" ", 2);

        next_class_url = arr[0];
        String preferred_transport = arr[1];

        String symbol_for_tran_option;
        switch (preferred_transport) {
            case "Bike":
                symbol_for_tran_option = "b";
                break;
            case "Walk":
                symbol_for_tran_option = "w";
                break;
            default:
                symbol_for_tran_option = "d";
        }


        switch (next_class_url) {
            case "https://streetsoncloud.com/parking/rest/occupancy/id/84?callback=myCallback":
                // Whatever you want to happen when the first item gets selected
                gmmIntentUri = Uri.parse("google.navigation:q=Big+Springs+Structure+Riverside,+CA+92507&mode="+ symbol_for_tran_option);
                break;
            case "https://streetsoncloud.com/parking/rest/occupancy/id/238?callback=myCallback":
                // Whatever you want to happen when the second item gets selected
                gmmIntentUri = Uri.parse("google.navigation:q=Parking+Lot+6+Riverside,+CA+92507&mode="+ symbol_for_tran_option);
                break;
            case "https://streetsoncloud.com/parking/rest/occupancy/id/243?callback=myCallback":
                // Whatever you want to happen when the thrid item gets selected
                gmmIntentUri = Uri.parse("google.navigation:q=Parking+Lot+24+Riverside,+CA+92507&mode="+ symbol_for_tran_option);
                break;
            case "https://streetsoncloud.com/parking/rest/occupancy/id/80?callback=myCallback":
                // Whatever you want to happen when the first item gets selected
                gmmIntentUri = Uri.parse("google.navigation:q=Parking+Lot+26+Riverside,+CA+92507&mode="+ symbol_for_tran_option);
                break;
            case "https://streetsoncloud.com/parking/rest/occupancy/id/82?callback=myCallback":
                // Whatever you want to happen when the second item gets selected
                gmmIntentUri = Uri.parse("google.navigation:q=Parking+Lot+30+Riverside,+CA+92507&mode="+ symbol_for_tran_option);
                break;
            case "https://streetsoncloud.com/parking/rest/occupancy/id/83?callback=myCallback":
                // Whatever you want to happen when the thrid item gets selected
                gmmIntentUri = Uri.parse("google.navigation:q=Parking+Lot+32+Riverside,+CA+92507&mode="+ symbol_for_tran_option);
                break;
            default:
                gmmIntentUri = Uri.parse("google.navigation:q=Parking+Lot+30+Riverside,+CA+92507&mode="+ symbol_for_tran_option);

        }
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }


}


