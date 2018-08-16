package com.example.android.instaline;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class MeFragment extends Fragment {

    private Button edit_profile;
    private Button signOut;
    private TextView username;
    private TextView useremail;
    private CircleImageView profile_img;

    private FirebaseAuth mAuth;

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
        profile_img = (CircleImageView)view.findViewById(R.id.profile_img_view);

        mAuth = FirebaseAuth.getInstance();

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
        FirebaseUser user = mAuth.getCurrentUser();
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
        } else{
            Intent intent = new Intent(getContext(), MeFragment.class);
            startActivity(intent);
            getActivity().finish();
        }
    }

}
