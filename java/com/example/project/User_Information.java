package com.example.project;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class User_Information implements Serializable {
    private String FirstName;
    private String LastName;
    private String Phone;
    private String Lot;
    private String Transport;

    private List<String> classes;

    private String weekday;
    private static final String TAG = "User";

    public User_Information(DocumentSnapshot document)
    {
        SimpleDateFormat simpleDateformat = new SimpleDateFormat("EEEE");
        weekday = simpleDateformat.format(new Date());
        FirstName = document.get("First Name").toString();
        LastName = document.get("Last Name").toString();
        Phone = document.get("Phone Number").toString();
        Lot = document.get("Favorite Lot").toString();
        Transport = document.get("Favorite Transportation").toString();
        classes =  (List<String>)document.get(weekday);
    }


    public String AccessFirst()
    {
        return FirstName;
    }

    public String AccessDay() {
        return weekday;
    }

    public String AccessTransport(){
        return Transport;
    }

    public String AccessLast()
    {
        return LastName;
    }

    public List<String> AccessClass()
    {
        return classes;
    }

    public String AccessPhone()
    {
        return Phone;
    }

    public String AccessLot() { return Lot; }

    public String UpdatePersonal(String uid, String first, String last, String phone, String lot, String transport){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("User_Information").document(uid)
                .update(
                        "First Name",first,
                        "Last Name",last,
                        "Phone Number",phone,
                        "Favorite Lot",lot,
                        "Favorite Transportation",transport
                );
        FirstName=first;
        LastName=last;
        Phone=phone;
        Lot=lot;
        Transport=transport;

        return uid;
    }
}
