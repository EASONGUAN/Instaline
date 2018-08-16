package com.example.android.instalineenterprise;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class MeFragment extends Fragment {

    private Button edit_profile;
    private Button signOut;
    private TextView username;
    private TextView useremail;
    private TextView userAddress;
    private TextView userCategory;
    private TextView userPriceRate;
    private CircleImageView profile_img;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    private List<Image> images;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;

    private FirebaseUser user;

    public MeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_me, container, false);

        edit_profile = (Button)view.findViewById(R.id.edit_profile_button);
        signOut = (Button)view.findViewById(R.id.logout_button);
        username = (TextView)view.findViewById(R.id.username);
        useremail = (TextView)view.findViewById(R.id.useremail);
        userAddress = (TextView)view.findViewById(R.id.userAddress);
        userCategory = (TextView)view.findViewById(R.id.userCategory);
        userPriceRate = (TextView)view.findViewById(R.id.userPriceRate);
        profile_img = (CircleImageView)view.findViewById(R.id.profile_img_view);


        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        edit_profile.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                System.out.println("Firebase **************: Edit Profile");
                Intent intent = new Intent(getContext(), EditProfileActivity.class);
                startActivity(intent);
            }
        });

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Firebase **************: Logout");
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                getActivity().finish();
            }

        });


        loadUserInformation();

        return view;
    }



    @Override
    public void onStart() {

        super.onStart();
        user = mAuth.getCurrentUser();
        if(user == null){
            System.out.println("Firebase *****************: User Authetication needed");
            Intent intent = new Intent(getContext(), SignInMethodActivity.class);
            startActivity(intent);
            getActivity().finish();
        }

    }



    private void loadUserInformation() {

        FirebaseUser user = mAuth.getCurrentUser();
        
        if(user != null){
            if(user.getPhotoUrl()!=null){
                Glide.with(getContext()).load(user.getPhotoUrl().toString()).into(profile_img);
                String profileUrl = user.getPhotoUrl().toString();
                System.out.println("Firebase: ******************* getPhotoUrl:" + profileUrl);
            }
            if(user.getDisplayName()!=null){
                String displayName = user.getDisplayName();
                username.setText(user.getDisplayName());
                System.out.println("Firebase: ******************* getDisplayName:" + displayName);
            }
            if(user.getEmail()!=null){
                String displayEmail = user.getEmail();
                useremail.setText(user.getEmail());
                System.out.println("Firebase: ******************* getEmail:" + displayEmail);
            }
            final DatabaseReference restaurantRef = databaseReference.child("Restaurant").child(user.getUid());
            restaurantRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    userAddress.setText(dataSnapshot.child("address").getValue(String.class));
                    userCategory.setText(dataSnapshot.child("category").getValue(String.class));
                    userPriceRate.setText(dataSnapshot.child("price").getValue(String.class));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        } else{
            Intent intent = new Intent(getContext(), MeFragment.class);
            startActivity(intent);
            getActivity().finish();
        }
    }

}
