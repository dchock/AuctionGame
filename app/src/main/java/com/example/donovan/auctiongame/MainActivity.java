package com.example.donovan.auctiongame;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
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

import static java.lang.Double.doubleToLongBits;
import static java.lang.Double.parseDouble;
import static java.lang.Double.toString;
import static java.lang.Double.valueOf;

public class MainActivity extends AppCompatActivity {

    TextView tvRound, tvRandom, tvPayoff, tvTotal, tvYourInfo, tvTheirInfo, tvUserID;
    EditText etYourBid;
    Button buttonSubmit;
    Button buttonLogOut;
    Button buttonNextRound;
    Button buttonNewGame;
    Double rangeMin = 0.0;
    Double rangeMax = 100.0;
    Double randomValue;



    Double yourBid, otherBid, yourRandom, otherRandom;
    String otherId;
    int yourRound = 0, otherRound =0;

    static Double yourTotal = 0.0;

    int roundCount = 1;

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
        tvUserID = (TextView)findViewById(R.id.textViewUserID);


        openDB();
        myFishDbRef.child(userId).getParent().setValue(null);

        etYourBid.setText("");
        tvRound.setText("Round  " + roundCount);
        tvYourInfo.setText("");
        tvTheirInfo.setText("");


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
                    tvUserID.setText("Your User ID is: " + userId);
                    randomValue = rangeMin + (rangeMax - rangeMin) * Math.random();
                    tvRandom.setText(VALUE_FORMAT.format(randomValue));

                }
            }
        };


        setupFirebaseDataChange();
        //openDB();
        setupAddButton();
        nextRound();
        newGame();

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
        buttonNewGame = (Button)findViewById(R.id.buttonNewGame);
        buttonNewGame.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){

                openDB();
                myFishDbRef.child(userId).getParent().setValue(null);
                roundCount = 1;
                tvRound.setText("Round " + roundCount);
                tvYourInfo.setText("");
                tvTheirInfo.setText("");
                tvPayoff.setText("");
                yourTotal = 0.0;
                tvTotal.setText(yourTotal.toString());
                etYourBid.setText("");
                randomValue = rangeMin + (rangeMax - rangeMin) * Math.random();
                tvRandom.setText(VALUE_FORMAT.format(randomValue));


            }
        });
    }

    private void nextRound() {
        buttonNextRound = (Button) findViewById(R.id.buttonNextRound);
        buttonNextRound.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                openDB();
                roundCount++;


                tvRound.setText("Round " + roundCount);
                //Display new random value
                randomValue = rangeMin + (rangeMax - rangeMin) * Math.random();
                tvRandom.setText(VALUE_FORMAT.format(randomValue));
                etYourBid.setText("");
                tvPayoff.setText("");

                myFishDbRef.child("Round " + roundCount).child(userId).child("Random").setValue(null);
                myFishDbRef.child("Round " + roundCount).child(userId).child("Bid").setValue(null);
                myFishDbRef.child("Round " + roundCount).child(userId).child("thisRound").setValue(null);

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


                myFishDbRef.child("Round " + roundCount).child(userId).child("Random").setValue(tvRandom.getText().toString());
                myFishDbRef.child("Round " + roundCount).child(userId).child("Bid").setValue(etYourBid.getText().toString());
                myFishDbRef.child("Round " + roundCount).child(userId).child("thisRound").setValue(String.valueOf(roundCount));

            }
        });
    }


    private void checkAutionData(DataSnapshot dataSnapshot) {

        Log.d("CIS3334", "=== checkAutionData === ");
        if (userId!=null) {

            for (DataSnapshot data : dataSnapshot.child("Round " + roundCount).getChildren()) {//.child("Round " + roundCount).getChildren()) {
                String UserIdForBid = data.getKey();
                String BidAmount = (String )data.child("Bid").getValue();
                String RandomValue = (String ) data.child("Random").getValue();
                String CurrentRound = (String ) data.child("thisRound").getValue();


                Log.d("CIS3334", "=== checkAutionData  key = "+ UserIdForBid);
                Log.d("CIS3334", "=== checkAutionData  userID = "+ userId);
                Log.d("CIS3334", "=== checkAutionData bid amount = "+ BidAmount);
                Log.d("CIS3334", "=== checkAutionData random amount = "+ RandomValue);

                if (BidAmount != null && RandomValue != null && CurrentRound != null) {

                    if (UserIdForBid.compareTo(userId) == 0) {
                        Log.d("CIS3334", "=== checkAutionData Found OUR team's bid ");

                        yourBid = Double.parseDouble(BidAmount);
                        yourRandom = Double.parseDouble(RandomValue);
                        yourRound = Integer.parseInt(CurrentRound);

                    } else {
                        Log.d("CIS3334", "=== checkAutionData Found OTHER team's bid ");

                        otherBid = Double.parseDouble(BidAmount);
                        otherRandom = Double.parseDouble(RandomValue);
                        otherId = UserIdForBid;
                        otherRound = Integer.parseInt(CurrentRound);

                    }

                    if (yourRound != otherRound) {

                        tvYourInfo.setText("Rounds are not the same. Wait for other player");
                        tvTheirInfo.setText("");

                    } else {

                        //If both values are submitted
                        if (yourBid != null && otherBid != null) {

                            if (yourBid > otherBid) {
                                Double yourPayoff = (yourRandom - yourBid);
                                tvPayoff.setText("Your payoff is " + VALUE_FORMAT.format(yourPayoff));
                                yourTotal =+ yourPayoff;
                                tvTotal.setText("Your total is " + VALUE_FORMAT.format(yourTotal));
                                Toast.makeText(MainActivity.this, "You win this round", Toast.LENGTH_SHORT).show();
                                tvYourInfo.setText("");
                                tvTheirInfo.setText("");
                                //myFishDbRef.child("Round " + roundCount).child(userId).child("Total").setValue(yourTotal.toString());

                            } else {

                                Toast.makeText(MainActivity.this, "You lose this round.", Toast.LENGTH_SHORT).show();
                                Double yourPayoff = 0.0;
                                tvPayoff.setText("Your payoff is " + VALUE_FORMAT.format(yourPayoff));
                                yourTotal = +yourPayoff;
                                tvTotal.setText("Your total is " + VALUE_FORMAT.format(yourTotal));
                                tvYourInfo.setText("");
                                tvTheirInfo.setText("");
                                //myFishDbRef.child("Round " + roundCount).child(userId).child("Total").setValue(yourTotal.toString());
                            }

                        } else if (yourBid != null && otherBid == null) {

                            tvYourInfo.setText("Waiting for other player");

                        } else if (yourBid == null && otherBid != null) {

                            tvYourInfo.setText("Enter a bid");

                        }

                    } // end if for rounds

                } else {

                    Log.d("CIS3334", "=== either BidAmount or RandomValue have not been updated yet ");
                    //Toast.makeText(MainActivity.this, "Someones BidAmount or RandomValue have not been updated yet", Toast.LENGTH_SHORT).show();
                    tvTheirInfo.setText("Waiting for other player");
                }

                //tvYourInfo.setText("Current round is " + yourRound);

            } //end for loop

        } //end if statement for checkAutionData

    } //end checkAutionData

} //end MainActivity

