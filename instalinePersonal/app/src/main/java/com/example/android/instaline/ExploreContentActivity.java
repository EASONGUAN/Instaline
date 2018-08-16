package com.example.android.instaline;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Iterator;
import java.util.Map;

public class ExploreContentActivity extends AppCompatActivity {

    private Button lineUpBtn;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private String restID;

    private TextView resName;
    private TextView resCategory;
    private TextView resPrice;
    private TextView resWaitTime;
    private TextView resQueueNumber;
    private ImageView coverImage;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore_content);

        // Get view components
        resName = findViewById(R.id.res_name);
        resCategory = findViewById(R.id.res_category);
        resPrice = findViewById(R.id.res_price);
        resWaitTime = findViewById(R.id.res_waitTime);
        resQueueNumber = findViewById(R.id.res_queueNumber);
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

        final DatabaseReference restRef = databaseReference.child("Restaurant").child(restID);
        restRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String restaurant_title = dataSnapshot.child("name").getValue(String.class);
                String category = dataSnapshot.child("category").getValue(String.class);
                String price = dataSnapshot.child("price").getValue(String.class);
                String waitTime = dataSnapshot.child("waitTime").getValue(String.class);
                int queueNumber = dataSnapshot.child("queueNumber").getValue(Integer.class);
                String coverImageURL = dataSnapshot.child("coverImage").getValue(String.class);
                String queueNumberString = String.valueOf(queueNumber);

                // Set values into layout
                resName.setText(restaurant_title);
                resCategory.setText(category);
                resPrice.setText(price);
                resWaitTime.setText(waitTime);
                resQueueNumber.setText(queueNumberString);
                Picasso.with(ExploreContentActivity.this).load(coverImageURL).into(coverImage);

                System.out.println(restaurant_title + " " + category + " " + price + " " + queueNumber + " " + waitTime + " " + coverImageURL);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //======================================
        //=== Instaline Line up button SETUP ===
        //======================================
        lineUpBtn = (Button)findViewById(R.id.lineup);
        lineUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String restaurant = restID;
                final String userID = firebaseAuth.getCurrentUser().getUid();
                final DatabaseReference restDict = databaseReference.child("Restaurant");
                DatabaseReference userDict = databaseReference.child("Users");
                final DatabaseReference currentUser = userDict.child(userID);
                final DatabaseReference chosenRest = restDict.child(restaurant);

                DatabaseReference checkNumRest = currentUser.child("Wait");

                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        DataSnapshot restDict = dataSnapshot.child("Restaurant");
                        DataSnapshot userList = dataSnapshot.child("Users");

                        DataSnapshot Rest = restDict.child(restaurant);
                        DataSnapshot User = userList.child(userID);

                        String username = User.child("username").getValue(String.class);
                        String profile = User.child("profileImage").getValue(String.class);

                        int queueNumber = Rest.child("TimeStamp").getValue(Integer.class);

                        Iterable<DataSnapshot> iterable = User.child("Wait").getChildren();
                        Iterator iterator = iterable.iterator();
                        int count = 0;

                        while (iterator.hasNext()){
                            Object placeholder = iterator.next();
                            System.out.println(placeholder);
                            count ++;
                        }

                        boolean checkExist = User.child("Wait").hasChild(restaurant);

                        if (count >= 5){
                            Toast.makeText(ExploreContentActivity.this, "No more than 5 Lineup is allowed",
                                    Toast.LENGTH_SHORT).show();
                        }
                        if (checkExist){
                            Toast.makeText(ExploreContentActivity.this, "Already lined up for this restaurant",
                                    Toast.LENGTH_SHORT).show();
                        }

                        if (!checkExist & count < 5) {
                            int TimeStamp = Rest.child("TimeStamp").getValue(Integer.class);
                            int queue = Rest.child("queueNumber").getValue(Integer.class);
                            //int last = Rest.child("lastDelete").getValue(Integer.class);
                            currentUser.child("Wait").child(restaurant).child("TimeStamp").setValue(TimeStamp);
                            currentUser.child("Wait").child(restaurant).child("lineNumber").setValue(queue + 1);
                            chosenRest.child("queueNumber").setValue(queue + 1);
                            chosenRest.child("TimeStamp").setValue(TimeStamp + 1);
                            chosenRest.child("UserQueue").child(userID).child("queueNumber").setValue(TimeStamp);
                            chosenRest.child("UserQueue").child(userID).child("nickName").setValue(username);
                            chosenRest.child("UserQueue").child(userID).child("profile").setValue(profile);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });
    }
}
