package com.example.android.instaline;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import de.hdodenhof.circleimageview.CircleImageView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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

public class EditProfileActivity extends AppCompatActivity implements View.OnClickListener{

    private CircleImageView profileImage;
    private Uri imageURI;

    private EditText username;
    private EditText userEmail;
    private TextView resetPass;
    private Button saveBtn;

    private ProgressDialog progress;

    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;

    String profileImageUrl;
    String usernameString;
    String emailString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        username = findViewById(R.id.username_input);
        userEmail = findViewById(R.id.useremail_input);
        resetPass = findViewById(R.id.edit_password);
        saveBtn = findViewById(R.id.save_changes_button);
        profileImage = findViewById(R.id.profile_img);

        progress = new ProgressDialog(this);
        progress.setMessage("Edit user profile");

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        usernameString = user.getDisplayName();
        emailString = user.getEmail();
        profileImageUrl = user.getPhotoUrl().toString();

        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        //show user information as default contents in edit test username and useremail
        loadUserInformation(user);

        //listen for profile photo
        profileImage.setOnClickListener(this);

        //add EditText onChange listener for username
        username.addTextChangedListener(new TextWatcher(){

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                //enable save button
                saveBtn.setEnabled(false);
                //fetch new username
                String newUsername = username.getText().toString().trim();
                if (TextUtils.isEmpty(newUsername)) {
                    username.setError("New username can not be empty");
                    username.requestFocus();
                } else { // valid string as new username
                    usernameString = newUsername;
                    saveBtn.setEnabled(true);
                }
            }
        });

        //add EditText onChange listener for userEmail
        userEmail.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //enable save button
                saveBtn.setEnabled(false);
                //fetch new userEmail
                String newUserEmail = userEmail.getText().toString().trim();
                //check if it is not empty and looks like an valid email
                if(TextUtils.isEmpty(newUserEmail)) {
                    userEmail.setError("New email can not be empty");
                    userEmail.requestFocus();
                } else if (!(newUserEmail.contains("@") && newUserEmail.contains("."))) {
                    userEmail.setError("Please enter an valid email with format: xxx@xxx.xxx");
                    userEmail.requestFocus();
                } else { //valid email string
                    emailString = newUserEmail;
                    saveBtn.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        //listen for password reset
        resetPass.setOnClickListener(this);

        //set up on click for save button
        saveBtn.setOnClickListener(this);

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
                System.out.println(error);
            }
        }
    }

    //instructions for clicks on different view
    @Override
    public void onClick(View v) {

        if (v == profileImage) {
            resetProfileImage();
        }
        if(v == resetPass){
            resetPassword();
        }
        if(v == saveBtn){
            saveChanges();
        }
    }

    // ------------------------   Helper  Functions  -----------------------------------------------

    private void loadUserInformation(FirebaseUser user) {

        if(user != null) {
            if(user.getDisplayName()!=null){
                username.setText(usernameString, TextView.BufferType.EDITABLE);
                System.out.println("Firebase: ******************* getDisplayName:" + usernameString);
            }
            if(user.getEmail()!=null){
                userEmail.setText(emailString,TextView.BufferType.EDITABLE);
                System.out.println("Firebase: ******************* getEmail:" + emailString);
            }
            if(user.getPhotoUrl()!=null){
                Glide.with(this).load(profileImageUrl).into(profileImage);
                System.out.println("Firebase: ******************* getPhotoUrl:" + profileImageUrl);
            }
        } else {
            Intent intent = new Intent(this, EditProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    private void resetProfileImage() {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(EditProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(EditProfileActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                // Request Permission
                ActivityCompat.requestPermissions(EditProfileActivity.this, new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                }, 1);
            }else if(ContextCompat.checkSelfPermission(EditProfileActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(EditProfileActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                // Request Permission
                ActivityCompat.requestPermissions(EditProfileActivity.this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                }, 1);
            } else{
                // start picker to get image for cropping and then use the image in cropping activity
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(EditProfileActivity.this);
            }
        }
    }

    //trigger password reset via email when textview is clicked
    private void resetPassword() {

        String curEmail = user.getEmail();
        firebaseAuth.sendPasswordResetEmail(curEmail)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(EditProfileActivity.this, "An email has been sent to " +
                                    emailString + ", please follow the instructions on it", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(EditProfileActivity.this, "Can not sent resetting email to " +
                                    emailString, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void saveChanges() {

        // when save button is enabled, it means that username and email have been checked,
        // so no validity verify added here.
        if(imageURI!=null){ //photo changed
            photoChanged();
        } else {//photo did not changed
            photoDidNotChanged();
        }
    }

    private void photoChanged() {

        progress.show();
        String userID = user.getUid();
        StorageReference image_path = storageReference.child("profile_images").child(userID + ".jpg");
        final DatabaseReference username_path = databaseReference.child("Users").child(userID).child("username");
        final DatabaseReference user_path = databaseReference.child("Users").child(userID);

        image_path.putFile(imageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    // Retrieve download URL
                    profileImageUrl = task.getResult().getDownloadUrl().toString();
                    user_path.child("profileImage").setValue(profileImageUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                user_path.child("username").setValue(usernameString).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {

                                            if (user != null && profileImageUrl != null) {
                                                UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                                                        .setDisplayName(usernameString)
                                                        .setPhotoUri(Uri.parse(profileImageUrl))
                                                        .build();
                                                user.updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {// update email
                                                            saveCurrEmailAndReDirect();
                                                        } else {
                                                            progress.dismiss();
                                                            String exception = task.getException().getMessage();
                                                            System.out.println(exception);
                                                            Toast.makeText(EditProfileActivity.this, exception, Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                            }
                                        } else {
                                            progress.dismiss();
                                            String exception = task.getException().getMessage();
                                            System.out.println(exception);
                                            Toast.makeText(EditProfileActivity.this, exception, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                progress.dismiss();
                                String exception = task.getException().getMessage();
                                System.out.println(exception);
                                Toast.makeText(EditProfileActivity.this, exception, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }else{
                    progress.dismiss();
                    String exception = task.getException().getMessage();
                    System.out.println(exception);
                    Toast.makeText(EditProfileActivity.this, exception, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void photoDidNotChanged(){

        progress.show();
        String userID = user.getUid();
        databaseReference.child("Users").child(userID).child("username")
                .setValue(usernameString)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                           UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                                .setDisplayName(usernameString)
                                    .build();
                           user.updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                        saveCurrEmailAndReDirect();
                                    } else {
                                        progress.dismiss();
                                        String exception = task.getException().getMessage();
                                        System.out.println(exception);
                                        Toast.makeText(EditProfileActivity.this, exception, Toast.LENGTH_SHORT).show();
                                    }
                                }
                           });
                        }else{
                            progress.dismiss();
                            String exception = task.getException().getMessage();
                            System.out.println(exception);
                            Toast.makeText(EditProfileActivity.this, exception, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveCurrEmailAndReDirect(){

        user.updateEmail(emailString).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    progress.dismiss();
                    Intent intent = new Intent(EditProfileActivity.this, BottomNavigationBarActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    progress.dismiss();
                    String exception = task.getException().getMessage();
                    System.out.println("Save profile changes");
                    System.out.println(exception);
                    Toast.makeText(EditProfileActivity.this, exception, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
