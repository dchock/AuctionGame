package com.example.donovan.auctiongame;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.DecimalFormat;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    TextView tvRound, tvRandom;
    EditText etYourBid;
    Double randomValue;
    Double rangeMin = 0.0;
    Double rangeMax = 100.0;



    DecimalFormat VALUE_FORMAT = new DecimalFormat("0.##");
    Random r = new Random();

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvRound = (TextView)findViewById(R.id.textViewRound);
        tvRandom = (TextView)findViewById(R.id.textViewYourRandom);
        etYourBid = (EditText)findViewById(R.id.editTextYourBid);

        randomValue =  rangeMin + (rangeMax - rangeMin) * r.nextDouble();

        tvRound.setText("This is a test");
        //tvRandom.setText("This is a test.");
        //VALUE_FORMAT.format(randomValue)

        mAuth = FirebaseAuth.getInstance(); //declare object for Firebase

        mAuthListener = new FirebaseAuth.AuthStateListener() { //initialized mAuthListener
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //track the user when they sign in or out using the firebaseAuth
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // User is signed out
                    Log.d("CSS3334","onAuthStateChanged - User NOT is signed in");
                    Intent signInIntent = new Intent(getBaseContext(), LoginActivity.class);
                    startActivity(signInIntent);
                }
            }
        };

    }

    public void onStart(){
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener); //adds a listener to the object
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    public void onStop(){
        super.onStop();
        if (mAuthListener != null) { //checks for an instance of mAuthListener
            mAuth.removeAuthStateListener(mAuthListener);  //if there is, it will be removed
        }
    }
}
