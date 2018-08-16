package com.example.android.instalineenterprise;

import android.Manifest;
import android.app.ProgressDialog;
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
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
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

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddProfileActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private EditText username;
    private EditText address;
    private EditText category;
    private Spinner priceRate;
    private Button galleryBtn;
    private CircleImageView profileImage;
    private static final int CHOOSE_IMAGE = 101;
    private Uri imageURI;
    private ProgressDialog progress;

    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    String profileImageUrl;
    String[] price_ranges = {"$", "$$", "$$$", "$$$$","$$$$$"};
    String priceRateValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_profile);

        username = (EditText)findViewById(R.id.username_input);
        address = (EditText)findViewById(R.id.address_input);
        category =(EditText)findViewById(R.id.category_input);
        priceRate = (Spinner) findViewById(R.id.price_rate);

        galleryBtn = (Button)findViewById(R.id.gallery_btn);
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

        priceRateValue = "$";
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

        // add listener to submit button
        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String userName = username.getText().toString();
                if(!TextUtils.isEmpty(userName) && (imageURI!=null)){
                    progress.show();
                    final String userID = firebaseAuth.getCurrentUser().getUid();
                    StorageReference image_path = storageReference.child("profile_images").child(userID + ".jpg");
                    final DatabaseReference user_path = databaseReference.child("Restaurant").child(userID);

                    image_path.putFile(imageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()){
                                // Retrieve download URL and save it to database
                                profileImageUrl = task.getResult().getDownloadUrl().toString();
                                user_path.child("coverImage").setValue(profileImageUrl);
                                user_path.child("name").setValue(userName).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            FirebaseUser user = firebaseAuth.getCurrentUser();
                                            String userName = username.getText().toString();
                                            // write address and category info into database
                                            String  userAddress =  address.getText().toString();
                                            String  userCategory = category.getText().toString();
                                            user_path.child("address").setValue(userAddress);
                                            user_path.child("category").setValue(userCategory);
                                            // write price rating into database
                                            user_path.child("price").setValue(priceRateValue);
                                            // other default settings
                                            int initialQueueNum = 0;
                                            int placeHolder = -1;
                                            int initialTimeStamp = 1;
                                            String initialWaitingTime = "10 mins";
                                            user_path.child("queueNumber").setValue(initialQueueNum);
                                            user_path.child("UserQueue").child("placeholder").setValue(placeHolder);
                                            user_path.child("TimeStamp").setValue(initialTimeStamp);
                                            user_path.child("waitTime").setValue(initialWaitingTime);

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
                                                            Intent intent = new Intent(AddProfileActivity.this, AddGalleryActivity.class);
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

    //Performing action onItemSelected and onNothing selected
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

}
