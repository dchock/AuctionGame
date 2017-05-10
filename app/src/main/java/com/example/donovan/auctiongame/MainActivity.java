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

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.Random;

import static java.lang.Double.parseDouble;

public class MainActivity extends AppCompatActivity {

    TextView tvRound, tvRandom, tvPayoff, tvTotal, tvYourInfo, tvTheirInfo;
    EditText etYourBid;
    Button buttonSubmit;
    Button buttonLogOut;
    Button buttonNextRound;
    Button buttonNewGame;
    Double rangeMin = 0.0;
    Double rangeMax = 100.0;
    Double randomValue;
    int roundCount = 1;


    Double yourBid, otherBid, yourRandom, otherRandom;
    String otherId;

    static Double yourTotal = 0.0;


    DecimalFormat VALUE_FORMAT = new DecimalFormat("#.##");

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


       // String round = "Round " + roundCount;

        tvRound.setText("Round" + roundCount);

        //Display the round
        //randomValue = rangeMin + (rangeMax - rangeMin) * Math.random();
        //tvRandom.setText(randomValue.toString());

        //Reset round values
        //myFishDbRef.child(round + roundCount).child(userId).child("Bid").setValue("");
        //myFishDbRef.child(round + roundCount).child(userId).child("Random").setValue("");

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
                } else {

                    userId = user.getUid();
                    randomValue = rangeMin + (rangeMax - rangeMin) * Math.random();
                    tvRandom.setText(VALUE_FORMAT.format(randomValue));

                }
            }
        };


        setupFirebaseDataChange();
        //openDB();
        setupAddButton();
        nextRound();
        //newGame();

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
            return null;
        } else {
            return user.getUid();
        }
    }

    // TEG added
    private void newGame(){
        buttonNextRound = (Button)findViewById(R.id.buttonNewGame);
        buttonNewGame.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                myFishDbRef.child("Round " + roundCount).setValue("");

            }
        });
    }

    private void nextRound() {
        buttonNextRound = (Button) findViewById(R.id.buttonNextRound);
        buttonNextRound.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                roundCount++;
                tvRound.setText("Round " + roundCount);
                //myFishDbRef.child(round).setValue("");

                //Display new random value
                randomValue = rangeMin + (rangeMax - rangeMin) * Math.random();
                tvRandom.setText(VALUE_FORMAT.format(randomValue));
            }
        });
    }

    private void setupAddButton() {
        // Set up the button to add a new fish using a seperate activity
        buttonSubmit = (Button) findViewById(R.id.buttonSubmit);
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                //write the bid to Firebase
                //myFishDbRef.child("Round 1").child(userId).child(key).setValue(etYourBid.getText());
                Log.d("CIS3334", "onClick for buttons writng bid to database bid = "+etYourBid.getText().toString());        // debugging log
                Log.d("CIS3334", "onClick for buttons writng bid to database random = "+tvRandom.getText().toString());     // debugging log

                myFishDbRef.child("Round " + roundCount).child(userId).child("Bid").setValue(etYourBid.getText().toString());
                myFishDbRef.child("Round " + roundCount).child(userId).child("Random").setValue(tvRandom.getText().toString());

            }
        });
    }


    private void checkAutionData(DataSnapshot dataSnapshot) {
        Log.d("CIS3334", "=== checkAutionData === ");
        if (userId!=null) {
            //myFishDbRef.child("Round 1").child(userId).setValue(etYourBid.getText().toString());

            for (DataSnapshot data : dataSnapshot.child("Round " + roundCount).getChildren()) {
                String UserIdForBid = data.getKey();
                String BidAmount = (String )data.child("Bid").getValue();
                String RandomValue = (String ) data.child("Random").getValue();

                Log.d("CIS3334", "=== checkAutionData  key = "+ UserIdForBid);
                Log.d("CIS3334", "=== checkAutionData  userID = "+ userId);
                Log.d("CIS3334", "=== checkAutionData bid amount = "+ BidAmount);
                Log.d("CIS3334", "=== checkAutionData random amount = "+ RandomValue);

                if (UserIdForBid.compareTo(userId)==0) {
                    Log.d("CIS3334", "=== checkAutionData Found OUR team's bid ");

                    yourBid = Double.parseDouble(BidAmount);
                    yourRandom = Double.parseDouble(RandomValue);


                } else {
                    Log.d("CIS3334", "=== checkAutionData Found OTHER team's bid ");

                    otherBid = Double.parseDouble(BidAmount);
                    otherRandom = Double.parseDouble(RandomValue);
                    otherId = UserIdForBid;

                }

            } //end for loop


        } //end if statement
        if (yourBid != null && otherBid != null) {

            if (yourBid > otherBid) {
                //Double yourPayoff = (randomValue - yourBid);
                //tvPayoff.setText("Your payoff is " + VALUE_FORMAT.format(yourPayoff));
                //yourTotal =+ yourPayoff;
                Toast.makeText(MainActivity.this, "You win this round", Toast.LENGTH_LONG).show();
                tvYourInfo.setText("Your user ID is " + userId + " and your bid is " + yourBid + " and your random is " + yourRandom);
                tvTheirInfo.setText("Their user ID is " + otherId + " and their bid is " + otherBid+ " and their random is " + otherRandom);


            } else {

                Toast.makeText(MainActivity.this, "You lose this round.", Toast.LENGTH_LONG).show();

            }

        } else if (yourBid != null && otherBid == null) {

            tvYourInfo.setText("Waiting for other player");

        } else if (yourBid == null && otherBid != null ) {

            tvYourInfo.setText("Enter a bid");

        } else {

            tvYourInfo.setText("Waiting for other player");
            tvYourInfo.setText("Enter a bid");

        }

    } //end checkAutionData


} //end MainActivity