package com.example.donovan.auctiongame;

import android.content.Intent;
import android.provider.ContactsContract;
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

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    TextView tvRound, tvRandom, tvPayoff, tvTotal, tvYourInfo, tvTheirInfo;
    EditText etYourBid;
    Button buttonSubmit;
    Button buttonLogOut;
    Double rangeMin = 0.0;
    Double rangeMax = 100.0;
    Double randomValue;



    //DecimalFormat VALUE_FORMAT = new DecimalFormat("0.##");


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
        tvPayoff = (TextView)findViewById(R.id.textViewPayoff);
        tvTotal = (TextView)findViewById(R.id.textViewTotal);
        tvYourInfo = (TextView)findViewById(R.id.textViewYourInfo);
        tvTheirInfo = (TextView)findViewById(R.id.textViewTheirInfo);



        Random r = new Random();
        randomValue = rangeMin + (rangeMax - rangeMin) * r.nextDouble();
        //String.format("%.02f", randomValue);
        //VALUE_FORMAT.format(randomValue);
       // tvPayoff.setText(randomValue.toString());

        String yourRandomValue = String.format("%.2f", randomValue);
        tvRandom.setText(yourRandomValue);



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

       // myFishDbRef.child("Round 1").child(userId).child("Random").setValue(tvRandom.getText().toString());

        //TEG added
        setupFirebaseDataChange();
        setupAddButton();

    }


    public void onStart(){
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener); //adds a listener to the object
        //FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    public void onStop(){
        super.onStop();
        if (mAuthListener != null) { //checks for an instance of mAuthListener
            mAuth.removeAuthStateListener(mAuthListener);  //if there is, it will be removed
        }
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

    // TEG added

    private void setupAddButton() {
        // Set up the button to add a new fish using a seperate activity
        buttonSubmit = (Button) findViewById(R.id.buttonSubmit);
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                //write the bid to Firebase
                //myFishDbRef.child("Round 1").child(userId).child(key).setValue(etYourBid.getText());
                Log.d("CIS3334", "onClick for buttons writng bid to database bid = "+etYourBid.getText().toString());        // debugging log
                Log.d("CIS3334", "onClick for buttons writng bid to database random = "+tvRandom.getText().toString());        // debugging log
                myFishDbRef.child("Round 1").child(userId).child("Bid").setValue(etYourBid.getText().toString());
                myFishDbRef.child("Round 1").child(userId).child("Random").setValue(tvRandom.getText().toString());

            }
        });
    }

    /*
    private String getRandom(AppCompatActivity activity) {
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        Double RandomValue =  rangeMin + (rangeMax - rangeMin) * r.nextDouble();

        return null;
    }
    */

    /*
    private void checkAutionData(DataSnapshot dataSnapshot) {
        Log.d("CIS3334", "=== checkAutionData === ");
        //myFishDbRef.child("Round 1").child(userId).setValue(etYourBid.getText().toString());
        for (DataSnapshot data : dataSnapshot.child("Round 1").child(userId).getChildren()) {
            String bidAmount = (String )data.getValue();
           // String rValue = (String )data.getValue();
            Log.d("CIS3334", "=== checkAutionData bid amount = "+ bidAmount);
            String UserIdForBid = data.getKey();
            Log.d("CIS3334", "=== checkAutionData  key = "+ UserIdForBid);
            if (UserIdForBid.compareTo(userId)==0) {
                Log.d("CIS3334", "=== checkAutionData Found OUR team's bid ");
            } else {
                Log.d("CIS3334", "=== checkAutionData Found OTHER team's bid ");
            }


            //tvPayoff.setText(tvRandom.toString());
            //String randomValue = (String )data.getValue();
            //Log.d("CIS3334", "=== checkAutionData random value = " + randomValue);
        }
    } */

    private void checkAutionData(DataSnapshot dataSnapshot) {
        Log.d("CIS3334", "=== checkAutionData === ");
        if (userId!=null) {
            //myFishDbRef.child("Round 1").child(userId).setValue(etYourBid.getText().toString());
            for (DataSnapshot data : dataSnapshot.child("Round 1").getChildren()) {
                String UserIdForBid = data.getKey();
                String theirBidAmount = (String )data.child("Bid").getValue();
                String theirRandomValue = (String )data.child("Random").getValue();
                Log.d("CIS3334", "=== checkAutionData  key = "+ UserIdForBid);
                Log.d("CIS3334", "=== checkAutionData  userID = "+ userId);
                Log.d("CIS3334", "=== checkAutionData bid amount = "+ theirBidAmount);
                Log.d("CIS3334", "=== checkAutionData random amount = "+ theirRandomValue);
                if (UserIdForBid.compareTo(userId)==0) {
                    Log.d("CIS3334", "=== checkAutionData Found OUR team's bid ");
                } else {
                    Log.d("CIS3334", "=== checkAutionData Found OTHER team's bid ");
                }

                String yourBid = (String ) data.child("Round 1").child(userId).child("Bid").getValue();
                String yourRandom = (String )data.child("Round 1").child("Random").child(userId).getValue();

                tvYourInfo.setText("Your user Id is " + userId + " and your bid amount is "
                        + yourBid + "\n " + " and your random value is " + yourRandom);

                tvTheirInfo.setText("Their user Id is " + UserIdForBid + " and their bid amount is "
                        + theirBidAmount + "\n" + " and their random value is " + theirRandomValue);


                //dataSnapshot.child(userId).getValue();

                /*

                dataSnapshot.child(UserIdForBid).getValue();
                dataSnapshot.child(rValue).getValue();


                Double myBid = Double.parseDouble(userId);
                Double theirBid = Double.parseDouble(UserIdForBid);
                Double youTotal;



                if (myBid > theirBid ) {

                   tvPayoff.setText("Your bid is " + myBid.toString());
                   // tvTotal.setText("Your Total is " + );

               } else if (myBid < theirBid) {

                    tvPayoff.setText("Your payoff is 0");


                }
                dataSnapshot.child()

                        */


            }


        }
    }


}