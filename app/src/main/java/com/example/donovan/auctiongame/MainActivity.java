package com.example.donovan.auctiongame;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    TextView tvRound, tvRandom;
    EditText etYourBid;
    Button buttonSubmit;
    Button buttonLogOut;
    Double rangeMin = 0.0;
    Double rangeMax = 100.0;


    DecimalFormat VALUE_FORMAT = new DecimalFormat("0.##");


    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    //TEG added
    public static final String AuctionGameTag = "Auction Game Data";
    DatabaseReference myFishDbRef;
    private String userId;              // Firebase authentication ID for the current logged int user



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvRound = (TextView)findViewById(R.id.textViewRound);
        tvRandom = (TextView)findViewById(R.id.textViewYourRandom);
        etYourBid = (EditText)findViewById(R.id.editTextYourBid);
        buttonLogOut = (Button)findViewById(R.id.buttonLogOut);


        //tvRound.setText(myFishDbRef.child("Round 1").toString());


        Random r = new Random();
        Double randomValue = rangeMin + (rangeMax - rangeMin) * r.nextDouble();
        //VALUE_FORMAT.format(randomValue);
        String rValue = String.format("%.2f", randomValue);
        tvRandom.setText(rValue);


        buttonLogOut.setOnClickListener(new View.OnClickListener() {
            public void onClick (View v) {
                Log.d("CIS3334", "Normal Logout");
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(MainActivity.this, "Signed Out",Toast.LENGTH_LONG).show();
            }
        });


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

        //TEG added
        setupFirebaseDataChange();
        setupAddButton();

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

    // TEG added
    private void setupAddButton() {
        // Set up the button to add a new fish using a seperate activity
        buttonSubmit = (Button) findViewById(R.id.buttonSubmit);
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                //write the bid to Firebase
                //myFishDbRef.child("Round 1").child(userId).child(key).setValue(etYourBid.getText());
                Log.d("CIS3334", "onClick for buttons writng bid to database");        // debugging log
                myFishDbRef.child("Round 1").child(userId).setValue(etYourBid.getText().toString());
                myFishDbRef.child("Round 1").child(userId).setValue(tvRandom.getText().toString());

            }
        });
    }

    private void setupFirebaseDataChange() {
        Log.d("CIS3334", "setupFirebaseDataChange openning the database");        // debugging log
        openDB();
        myFishDbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("CIS3334", "Starting onDataChange()");        // debugging log
                checkAutionData(dataSnapshot);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("CIS3334", "onCancelled: ");
            }
        });
    }

    public DatabaseReference openDB()  {
        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myFishDbRef = database.getReference(AuctionGameTag);
        // set the user id for the current logged in user
        userId = getUserId(this);

        return myFishDbRef;
    }

    // get the current logged in user's id from Firebase
    private String getUserId(AppCompatActivity activity) {
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            // User is signed out
            Log.d("CSS3334","onAuthStateChanged - User NOT is signed in");
            Intent signInIntent = new Intent(activity, LoginActivity.class);
            activity.startActivity(signInIntent);
            return null;
        } else {
            return user.getUid();
        }
    }

    /*
    private String getRandom(AppCompatActivity activity) {
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        Double RandomValue =  rangeMin + (rangeMax - rangeMin) * r.nextDouble();

        return null;
    }
    */


    private void checkAutionData(DataSnapshot dataSnapshot) {
        Log.d("CIS3334", "=== checkAutionData === ");
        //myFishDbRef.child("Round 1").child(userId).setValue(etYourBid.getText().toString());
        for (DataSnapshot data : dataSnapshot.child("Round 1").getChildren()) {
            String bidAmount = (String )data.getValue();
            String rValue = (String )data.getValue();
            Log.d("CIS3334", "=== checkAutionData bid amount = "+ bidAmount);
            String UserIdForBid = data.getKey();
            Log.d("CIS3334", "=== checkAutionData  key = "+ UserIdForBid);
            if (UserIdForBid.compareTo(userId)==0) {
                Log.d("CIS3334", "=== checkAutionData Found OUR team's bid ");
            } else {
                Log.d("CIS3334", "=== checkAutionData Found OTHER team's bid ");
            }
            //String randomValue = (String )data.getValue();
            //Log.d("CIS3334", "=== checkAutionData random value = " + randomValue);
        }
    }
}
