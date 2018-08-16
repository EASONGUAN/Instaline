package com.example.android.instaline;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.text.TextUtils;
import android.view.animation.AccelerateInterpolator;
import android.widget.SearchView;
import android.widget.TextView;

import com.miguelcatalan.materialsearchview.MaterialSearchView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ExploreFragment extends Fragment {
    SearchView searchView;
    RecyclerView recyclerView;
    RestaurantAdapter adapter;


    List<RestaurantModel> restaurantModelList;

    private DatabaseReference mDatabase;
    private DatabaseReference restaurantRef;

    public ExploreFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_explore, container, false);

        restaurantModelList = new ArrayList<>();
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        searchView = (SearchView) view.findViewById(R.id.search_tag_view);
        searchView.setSubmitButtonEnabled(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Firebase Retrive data
        mDatabase = FirebaseDatabase.getInstance().getReference();
        restaurantRef = mDatabase.child("Restaurant");

        restaurantRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String id = dataSnapshot.getKey();
                String restaurant_title = dataSnapshot.child("name").getValue(String.class);
                String time = dataSnapshot.child("waitTime").getValue(String.class);
                String coverImage = dataSnapshot.child("coverImage").getValue(String.class);
                if (restaurant_title != null || time != null) {
                    System.out.println("============================");
                    System.out.println("id: " + id + "   restaurant name: " + restaurant_title + "   wait time: " + time + "   image URL: " + coverImage);
                    System.out.println("============================");
                }
                restaurantModelList.add(new RestaurantModel(id, restaurant_title, time, coverImage));
                adapter.notifyDataSetChanged();
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
        int id = searchView.getContext()
                .getResources()
                .getIdentifier("android:id/search_src_text", null, null);
        TextView textView = (TextView) searchView.findViewById(id);
        textView.setTextColor(Color.GRAY);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                List<RestaurantModel> foundList = new ArrayList<>();
                if (!TextUtils.isEmpty(newText.trim())) {
                    foundList = searchTags(newText.trim());
                    adapter = new RestaurantAdapter(getActivity(), foundList);
                    recyclerView.setAdapter(adapter);
                    recyclerView.scrollToPosition(0);
                } else {
                    foundList.clear();
                    adapter = new RestaurantAdapter(getActivity(), restaurantModelList);
                    recyclerView.setAdapter(adapter);
                    recyclerView.scrollToPosition(0);
                }
                return false;
            }
        });
        adapter = new RestaurantAdapter(getActivity(), restaurantModelList);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

            }
        });
//        searchView.animate().translationY(-searchView.getBottom()).setInterpolator(new AccelerateInterpolator()).start();




        return view;
    }

    private List<RestaurantModel> searchTags(String searchTagName) {
        List<RestaurantModel> foundList = new ArrayList<>();
        if (searchTagName != null && !searchTagName.isEmpty()) {
            for (RestaurantModel rm : restaurantModelList) {
                if (rm.getRestaurantTitle().toLowerCase().contains(searchTagName.toLowerCase()))
                    foundList.add(rm);
            }

        }
        return foundList;
    }
}
