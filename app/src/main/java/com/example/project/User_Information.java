package com.example.project;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class User_Information {
    private String FirstName;
    private String LastName;
    public String Phone;
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
                        classes =  (List<String>)document.get(weekday);
    }

    public String AccessFirst()
    {
        return FirstName;
    }

    public String AccessWeek()
    {
        return weekday;
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

}
