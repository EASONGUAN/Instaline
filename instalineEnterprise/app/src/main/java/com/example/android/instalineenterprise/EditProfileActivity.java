package com.example.android.instalineenterprise;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private CircleImageView profileImage;
    private Uri imageURI;

    private EditText username;
    private EditText userEmail;
    private TextView resetPass;

    public TextView userAddress;
    private AlertDialog editAddressDialog;
    private EditText editAddress;
    public TextView userCategory;
    private AlertDialog editCategoryDialog;
    private EditText editCategory;
    private Spinner priceRate;
    private Button saveBtn;

    private ProgressDialog progress;

    private StorageReference storageReference;
    public DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;

    String profileImageUrl;
    String usernameString;
    String emailString;
    String priceRateValue;
    String[] price_ranges = {"$", "$$", "$$$", "$$$$","$$$$$"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        username = findViewById(R.id.username_input);
        userEmail = findViewById(R.id.useremail_input);
        resetPass = findViewById(R.id.edit_password);
        saveBtn = findViewById(R.id.save_changes_button);
        profileImage = findViewById(R.id.profile_img);

        //Edit address
        userAddress = findViewById(R.id.edit_address);
        editAddressDialog = new AlertDialog.Builder(this).create();
        editAddress = new EditText(this);
        editAddressDialog.setTitle("Please type in the next line ");
        editAddressDialog.setView(editAddress);

        editAddressDialog.setButton(DialogInterface.BUTTON_POSITIVE, "SAVE ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                userAddress.setText(editAddress.getText());
            }
        });

        userAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editAddress.setText(userAddress.getText());
                editAddressDialog.setView(editAddress);
                editAddressDialog.show();
            }
        });

        //Edit category
        userCategory= findViewById(R.id.edit_category);
        editCategoryDialog = new AlertDialog.Builder(this).create();
        editCategory = new EditText(this);
        editCategoryDialog.setTitle("Please type your new category");
        editCategoryDialog.setView(editCategory);

        editCategoryDialog.setButton(DialogInterface.BUTTON_POSITIVE, "SAVE ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                userCategory.setText(editCategory.getText());
            }
        });

        userCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editCategory.setText(userCategory.getText());
                editCategoryDialog.setView(editCategory);
                editCategoryDialog.show();
            }
        });

        //Edit price rate
        priceRate = findViewById(R.id.edit_price);
        if(priceRate.getSelectedItem() != null) {
            priceRateValue = priceRate.getSelectedItem().toString();
        } else {
            priceRateValue = "";
        }
        priceRate.setOnItemSelectedListener(this);
        //Creating the ArrayAdapter instance having the bank name list
        ArrayAdapter aa = new ArrayAdapter(this,android.R.layout.simple_spinner_item,price_ranges);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_item);
        //Setting the ArrayAdapter data on the Spinner
        priceRate.setAdapter(aa);
        priceRate.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ((TextView) priceRate.getSelectedView()).setTextColor(Color.WHITE);
            }
        });

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
            final DatabaseReference restaurantRef = databaseReference.child("Restaurant").child(user.getUid());
            restaurantRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    userAddress.setText(dataSnapshot.child("address").getValue(String.class));
                    userCategory.setText(dataSnapshot.child("category").getValue(String.class));
                    String currentPriceRate = dataSnapshot.child("price").getValue(String.class);
                    selectSpinnerValue(priceRate, currentPriceRate);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
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
        final DatabaseReference user_path = databaseReference.child("Restaurant").child(userID);

        image_path.putFile(imageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    String userID = user.getUid();

                    // Retrieve download URL
                    profileImageUrl = task.getResult().getDownloadUrl().toString();
                    user_path.child("coverImage")
                            .setValue(profileImageUrl)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                user_path.child("name")
                                        .setValue(usernameString)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){

                                            if(user != null && profileImageUrl != null){
                                                UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                                                        .setDisplayName(usernameString)
                                                        .setPhotoUri(Uri.parse(profileImageUrl))
                                                        .build();
                                                user.updateProfile(profile).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()) {// update email
                                                            saveOtherInfoAndReDirect();
                                                        } else{
                                                            progress.dismiss();
                                                            String exception = task.getException().getMessage();
                                                            System.out.println(exception);
                                                            Toast.makeText(EditProfileActivity.this, exception, Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                            }
                                        }else{
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
        databaseReference.child("Restaurant").child(userID).child("name")
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
                                        saveOtherInfoAndReDirect();
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

    private void saveOtherInfoAndReDirect(){
        String userID = user.getUid();

        final DatabaseReference restaurant = databaseReference.child("Restaurant").child(userID);
        restaurant.child("price").setValue(priceRateValue).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    restaurant.child("category")
                            .setValue(userCategory.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
//                            String userID = user.getUid();
                                        restaurant.child("address")
                                                .setValue(userAddress.getText().toString())
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()) {
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
                                                        } else {
                                                            progress.dismiss();
                                                            String exception = task.getException().getMessage();
                                                            System.out.println("Save profile changes");
                                                            System.out.println(exception);
                                                            Toast.makeText(EditProfileActivity.this, exception, Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    } else {
                                        progress.dismiss();
                                        String exception = task.getException().getMessage();
                                        System.out.println("Save profile changes");
                                        System.out.println(exception);
                                        Toast.makeText(EditProfileActivity.this, exception, Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
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

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
        Toast.makeText(getApplicationContext(), price_ranges[position], Toast.LENGTH_LONG).show();
        priceRateValue = price_ranges[position];
        System.out.println("###########################3  " +  priceRateValue );
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub

    }

    private void selectSpinnerValue(Spinner spinner, String myString) {

        for(int i = 0; i < spinner.getCount(); i++){
            if(spinner.getItemAtPosition(i).toString().equals(myString)){
                spinner.setSelection(i);
                break;
            }
        }
    }

}
