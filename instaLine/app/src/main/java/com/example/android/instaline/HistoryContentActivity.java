package com.example.android.instaline;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class HistoryContentActivity extends AppCompatActivity {

    private Button lineUpBtn;
    private Button quitBtn;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private String restID;
    private String restNameString;

    private TextView resName;
    private TextView resCategory;
    private TextView resPrice;
    private TextView resWaitTime;
    private TextView resQueueNumber;
    private TextView resTimeStamp;
    private ImageView coverImage;

    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_content);

        // Get view components
        resName = findViewById(R.id.res_name);
        resCategory = findViewById(R.id.res_category);
        resPrice = findViewById(R.id.res_price);
        resWaitTime = findViewById(R.id.res_waitTime);
        resQueueNumber = findViewById(R.id.res_queueNumber);
        resTimeStamp = findViewById(R.id.res_timeStamp);
        coverImage = findViewById(R.id.res_coverImage);

        // Get database
        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        //===================================
        //=== Retrieve data from FIREBASE ===
        //===================================

        // Get current page restaurant id
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            restID = extras.getString("id");
        }

        //==============================================
        //=== Retrieve Restaurant data from FIREBASE ===
        //==============================================
        final DatabaseReference restRef = databaseReference.child("Restaurant").child(restID);
        restRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String restaurant_title = dataSnapshot.child("name").getValue(String.class);
                restNameString = restaurant_title;
                String category = dataSnapshot.child("category").getValue(String.class);
                String price = dataSnapshot.child("price").getValue(String.class);
                String waitTime = dataSnapshot.child("waitTime").getValue(String.class);
                String coverImageURL = dataSnapshot.child("coverImage").getValue(String.class);
                String address = dataSnapshot.child("address").getValue(String.class);

                // Set values into layout
                resName.setText(restaurant_title);
                resCategory.setText(category);
                resPrice.setText(price);
                resWaitTime.setText(waitTime);
                Picasso.with(HistoryContentActivity.this).load(coverImageURL).into(coverImage);

                //===================================
                //===         Display map         ===
                //===================================
                bundle = new Bundle();
                bundle.putString("address", address);
                RestaurantMapFragment mapFragment = new RestaurantMapFragment();
                mapFragment.setArguments(bundle);
                FragmentManager manager = getSupportFragmentManager();
                manager.beginTransaction().replace(R.id.restaurantMap, mapFragment, mapFragment.getTag()).commitAllowingStateLoss();

                System.out.println(restaurant_title + " " + category + " " + price + " "  + waitTime + " " + coverImageURL);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //==================================================
        //=== Retrieve Queuing information from FIREBASE ===
        //==================================================
        final String userID = firebaseAuth.getCurrentUser().getUid();
        final DatabaseReference userRef = databaseReference.child("Users").child(userID).child("Wait");
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println(dataSnapshot);
                if(dataSnapshot.child(restID).child("TimeStamp").getValue(Integer.class )!= null) {
                    int timeStamp = dataSnapshot.child(restID).child("TimeStamp").getValue(Integer.class);
                    String timeStampString = String.valueOf(timeStamp);
                    resTimeStamp.setText("Comfirm restaurant with number: " + timeStampString);
                }
                if(dataSnapshot.child(restID).child("lineNumber").getValue(Integer.class )!= null){
                    int queueNumber = dataSnapshot.child(restID).child("lineNumber").getValue(Integer.class);
                    String queueNumberString = String.valueOf(queueNumber);
                    resQueueNumber.setText(queueNumberString);

                    // notification
                    if (Integer.valueOf(queueNumberString) <= 5) {
                        Intent intent = new Intent(HistoryContentActivity.this, NotificationService.class);
                        intent.setAction("lineApproaching");
                        intent.putExtra("restName", restNameString);
                        intent.putExtra("qNum", Integer.valueOf(queueNumberString));
//                    intent.putExtra("waitTime", waitTime);
                        startService(intent);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        //========================
        //=== InstaQuit Button ===
        //========================
        quitBtn = (Button)findViewById(R.id.quitline);
        quitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String restaurant = restID;
                final String userID = firebaseAuth.getCurrentUser().getUid();
                final DatabaseReference restDict = databaseReference.child("Restaurant");
                final DatabaseReference chosenRest = restDict.child(restaurant);
                DatabaseReference userDict = databaseReference.child("Users");
                final DatabaseReference currentUser = userDict.child(userID);


                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        DataSnapshot restDict = dataSnapshot.child("Restaurant");
                        DataSnapshot userList = dataSnapshot.child("Users");

                        DataSnapshot Rest = restDict.child(restaurant);
                        DataSnapshot User = userList.child(userID);

                        int queueNumber = Rest.child("queueNumber").getValue(Integer.class);
                        int lastNumber = User.child("Wait").child(restaurant).child("lineNumber").getValue(Integer.class);
                        chosenRest.child("queueNumber").setValue(queueNumber - 1);
                        chosenRest.child("UserQueue").child(userID).removeValue();
                        currentUser.child("Wait").child(restaurant).removeValue();

                        for (DataSnapshot snapshot : Rest.child("UserQueue").getChildren()){

                            String userid = snapshot.getKey();

                            if (!userid.equals("placeholder") && !userid.equals(userID)){
                                int currentUserTime = dataSnapshot.child("Users").child(userid).child("Wait").child(restaurant).child("lineNumber").getValue(Integer.class);
                                if(currentUserTime > lastNumber){
                                    userDict.child(userid).child("Wait").child(restaurant).child("lineNumber").setValue(currentUserTime - 1);
                                }
                                //chosenRest.child("UserQueue").child(userid).child("queueNumber").setValue(currentUserTime - lastUserNumber);
                            }


                        }

                        finish();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
