package com.example.project;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.google.firebase.auth.FirebaseAuth.getInstance;

public class LogSignActivity extends AppCompatActivity {

    private Button Log_In, Sign_Up,Google;
    private FirebaseAuth Authentication;
    private static final String TAG = "LogSignActivity";
    private static final int RC_SIGN_IN = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Authentication = getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_sign);

        GoogleSignInOptions googlesign = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        final GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, googlesign);

        Google=findViewById(R.id.Google_Sign);
        Google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.Google_Sign:
                        signIn(mGoogleSignInClient);
                        break;
                }
            }
        });

        Log_In = findViewById(R.id.LogIn);
        Log_In.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText pass_field = (EditText)findViewById(R.id.password);
                EditText user_field = (EditText)findViewById(R.id.email);
                String email = user_field.getText().toString();
                String password = pass_field.getText().toString();

                Authentication.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LogSignActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "signInWithEmail:success");
                                    Intent myIntent = new Intent(LogSignActivity.this, HomeScreen.class);
                                    startActivity(myIntent);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(LogSignActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                    //updateUI(null);
                                    Intent myIntent = new Intent(LogSignActivity.this, LogSignActivity.class);
                                    startActivity(myIntent);
                                }
                            }
                        });
            }
        });

        Sign_Up = findViewById(R.id.SignUp);
        Sign_Up.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(LogSignActivity.this,SignUpActivity.class);
                startActivity(myIntent);
            }
        });
    }

    private void signIn(GoogleSignInClient mGoogleSignInClient) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        Authentication.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            GoogleFindEntry(Authentication.getCurrentUser());
                            Intent myIntent = new Intent(LogSignActivity.this,HomeScreen.class);
                            startActivity(myIntent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                           Toast.makeText(LogSignActivity.this,"Login Failed", Toast.LENGTH_LONG);
                        }

                        // ...
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    public void GoogleFindEntry(final FirebaseUser user)
    {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference usersRef = db.collection("User_Information").document(user.getUid());
        usersRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                    } else {
                        Log.d(TAG, "No such document");
                        Map<String,Object> Data= new HashMap<>();
                        if(user.getDisplayName() == null) {
                            Data.put("First Name", "");
                            Data.put("Last Name", "");
                        }
                        else {
                            if (user.getDisplayName().indexOf(" ") != -1) {
                                Data.put("First Name", user.getDisplayName().substring(0,user.getDisplayName().indexOf(" ")));
                                Data.put("Last Name", user.getDisplayName().substring(user.getDisplayName().indexOf(" ") + 1));
                            }
                            else{
                                Data.put("First Name", user.getDisplayName());
                                Data.put("Last Name","");
                            }
                        }
                        if(user.getPhoneNumber() == null){
                            Data.put("Phone Number", "");
                        }
                        else {
                            Data.put("Phone Number", user.getPhoneNumber());
                        }
                        Data.put("Monday", Arrays.asList());
                        Data.put("Tuesday", Arrays.asList());
                        Data.put("Wednesday", Arrays.asList());
                        Data.put("Thursday", Arrays.asList());
                        Data.put("Friday", Arrays.asList());
                        Data.put("Favorite Lot", "None");
                        Data.put("Favorite Transportation", "Car");

                        db.collection("User_Information").document(user.getUid())
                                .set(Data)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "DocumentSnapshot successfully written!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error writing document", e);
                                    }
                                });

                    }
                } else {
                    //Log.d(TAG, "get failed with ", task.getException());
                    Map<String,Object> Data= new HashMap<>();
                    if(user.getDisplayName() == null) {
                        Data.put("First Name", "");
                        Data.put("Last Name", "");
                    }
                    else {
                        if (user.getDisplayName().indexOf(" ") != -1) {
                            Data.put("First Name", user.getDisplayName().substring(0,user.getDisplayName().indexOf(" ")));
                            Data.put("Last Name", user.getDisplayName().substring(user.getDisplayName().indexOf(" ") + 1));
                        }
                        else{
                            Data.put("First Name", user.getDisplayName());
                            Data.put("Last Name","");
                        }
                    }
                    if(user.getPhoneNumber() == null){
                        Data.put("Phone Number", "");
                    }
                    else {
                        Data.put("Phone Number", user.getPhoneNumber());
                    }
                    Data.put("Monday", Arrays.asList());
                    Data.put("Tuesday", Arrays.asList());
                    Data.put("Wednesday", Arrays.asList());
                    Data.put("Thursday", Arrays.asList());
                    Data.put("Friday", Arrays.asList());
                    Data.put("Favorite Lot", "None");
                    Data.put("Favorite Transportation", "Car");

                    db.collection("User_Information").document(user.getUid())
                            .set(Data)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "DocumentSnapshot successfully written!");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error writing document", e);
                                }
                            });

                }
            }
        });
    }
}
