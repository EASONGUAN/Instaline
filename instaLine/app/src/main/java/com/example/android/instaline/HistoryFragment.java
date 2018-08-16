package com.example.android.instaline;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class HistoryFragment extends Fragment {
    private FirebaseAuth mAuth;

    RecyclerView recyclerView;
    HistoryAdapter adapter;
    SwipeRefreshLayout swipeRefreshLayout;

    List<RestaurantModel> restaurantModelList;

    public HistoryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        restaurantModelList = new ArrayList<>();
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout_history);

        // Firebase Retrieve data
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();
        DatabaseReference waitRef = mDatabase.child("Users").child(user.getUid()).child("Wait");
        DatabaseReference restaurantRef = mDatabase.child("Restaurant");
        waitRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String waitingResName = dataSnapshot.getKey();
                if (dataSnapshot.child("lineNumber").getValue(Integer.class) != null){
                    int rank = dataSnapshot.child("lineNumber").getValue(Integer.class);
                    String waitingResRank = String.valueOf(rank);
                    DatabaseReference curRestaurantRef = restaurantRef.child(waitingResName);
                    curRestaurantRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
//                        System.out.println(dataSnapshot);
                            String id = dataSnapshot.getKey();
                            String restaurant_title = dataSnapshot.child("name").getValue(String.class);
                            String time = dataSnapshot.child("waitTime").getValue(String.class);
                            String coverImage = dataSnapshot.child("coverImage").getValue(String.class);
                            restaurantModelList.add(new RestaurantModel(id, restaurant_title, time, coverImage, waitingResRank));
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String waitingResName = dataSnapshot.getKey();
                DatabaseReference curRestaurantRef = restaurantRef.child(waitingResName);
                curRestaurantRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        System.out.println(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String waitingResName = dataSnapshot.getKey();
                int rank = dataSnapshot.child("lineNumber").getValue(Integer.class);
                String waitingResRank = String.valueOf(rank);
                List<RestaurantModel> newRestaurantModelList;
                newRestaurantModelList = new ArrayList<>();
                DatabaseReference curRestaurantRef = restaurantRef.child(waitingResName);
                curRestaurantRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        System.out.println(dataSnapshot);
                        String id = dataSnapshot.getKey();
                        String restaurant_title = dataSnapshot.child("name").getValue(String.class);
                        String time = dataSnapshot.child("waitTime").getValue(String.class);
                        String coverImage = dataSnapshot.child("coverImage").getValue(String.class);
                        newRestaurantModelList.add(new RestaurantModel(id, restaurant_title, time, coverImage, waitingResRank));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                adapter = new HistoryAdapter(getActivity(), newRestaurantModelList);
                recyclerView.setAdapter(adapter);

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        adapter = new HistoryAdapter(getActivity(), restaurantModelList);
        recyclerView.setAdapter(adapter);


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                List<RestaurantModel> newRestaurantModelList;
                newRestaurantModelList = new ArrayList<>();
                waitRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        String waitingResName = dataSnapshot.getKey();
                        int rank = dataSnapshot.child("lineNumber").getValue(Integer.class);
                        String waitingResRank = String.valueOf(rank);
                        System.out.println(waitingResName + waitingResRank);
                        DatabaseReference curRestaurantRef = restaurantRef.child(waitingResName);
                        curRestaurantRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
//                        System.out.println(dataSnapshot);
                                String id = dataSnapshot.getKey();
                                String restaurant_title = dataSnapshot.child("name").getValue(String.class);
                                String time = dataSnapshot.child("waitTime").getValue(String.class);
                                String coverImage = dataSnapshot.child("coverImage").getValue(String.class);
                                newRestaurantModelList.add(new RestaurantModel(id, restaurant_title, time, coverImage, waitingResRank));
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        if (swipeRefreshLayout.isRefreshing()) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }

                });
                adapter = new HistoryAdapter(getActivity(), newRestaurantModelList);
                recyclerView.setAdapter(adapter);

                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }


            }
        });

        // Inflate the layout for this fragment
        return view;
    }
}
