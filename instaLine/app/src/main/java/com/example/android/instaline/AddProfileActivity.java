package com.example.android.instaline;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddProfileActivity extends AppCompatActivity {

    private EditText username;
    private Button submitBtn;
    private CircleImageView profileImage;
    private static final int CHOOSE_IMAGE = 101;
    private Uri imageURI;
    private ProgressDialog progress;

    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    String profileImageUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_profile);

        username = (EditText)findViewById(R.id.username_input);
        submitBtn = (Button)findViewById(R.id.summit_button);
        profileImage = (CircleImageView) findViewById(R.id.profile_img);
        progress = new ProgressDialog(this);
        progress.setMessage("Setting up User");

        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // add listener to profile image view
        profileImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
//                showImageChooser();
                // Permission is required for Marshmallow and up
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(ContextCompat.checkSelfPermission(AddProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(AddProfileActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                        // Request Permission
                        ActivityCompat.requestPermissions(AddProfileActivity.this, new String[]{
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                        }, 1);
                    }else if(ContextCompat.checkSelfPermission(AddProfileActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(AddProfileActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                        // Request Permission
                        ActivityCompat.requestPermissions(AddProfileActivity.this, new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                        }, 1);
                    } else{
                        // start picker to get image for cropping and then use the image in cropping activity
                        CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setAspectRatio(1,1)
                                .start(AddProfileActivity.this);
                    }
                }
            }
        });

        // add listener to submit button
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(AddProfileActivity.this, BottomNavigationBarActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
//                finish();
                final String userName = username.getText().toString();
                if(!TextUtils.isEmpty(userName) && (imageURI!=null)){
                    progress.show();
                    String userID = firebaseAuth.getCurrentUser().getUid();
                    StorageReference image_path = storageReference.child("profile_images").child(userID + ".jpg");
                    final DatabaseReference username_path = databaseReference.child("Users").child(userID).child("username");
                    final DatabaseReference profile_path = databaseReference.child("Users").child(userID).child("profileImage");
                    image_path.putFile(imageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()){
                                // Retrieve download URL
                                profileImageUrl = task.getResult().getDownloadUrl().toString();
                                profile_path.setValue(profileImageUrl);
                                username_path.setValue(userName).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            FirebaseUser user = firebaseAuth.getCurrentUser();
                                            String userName = username.getText().toString();
                                            if(user != null && profileImageUrl != null){
                                                UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                                                        .setDisplayName(userName)
                                                        .setPhotoUri(Uri.parse(profileImageUrl))
                                                        .build();
                                                user.updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            progress.dismiss();
                                                            Intent intent = new Intent(AddProfileActivity.this, BottomNavigationBarActivity.class);
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                            startActivity(intent);
                                                            finish();
                                                        }else{
                                                            progress.dismiss();
                                                            String exception = task.getException().getMessage();
                                                            System.out.println(exception);
                                                            Toast.makeText(AddProfileActivity.this, exception, Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                            }
                                        }else{
                                            progress.dismiss();
                                            String exception = task.getException().getMessage();
                                            System.out.println(exception);
                                            Toast.makeText(AddProfileActivity.this, exception, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }else{
                                progress.dismiss();
                                String exception = task.getException().getMessage();
                                System.out.println(exception);
                                Toast.makeText(AddProfileActivity.this, exception, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    // Get image from intent and set to image view
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageURI = result.getUri();
                profileImage.setImageURI(imageURI);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

}
