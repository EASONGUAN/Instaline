package com.example.android.instalineenterprise;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddGalleryActivity extends AppCompatActivity {

    private Button selectFromAlbum;
    private Button submit;
    private CircleImageView profileImage;
    private List<Image> images;
    private FirebaseUser user;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private LinearLayout linearLayout;
    private String uid;
    private FirebaseAuth mAuth;

    private StorageReference storage_image_path;
    private DatabaseReference db_image_Path;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_gallery);

        selectFromAlbum = (Button)findViewById(R.id.select_photo_button);
        submit = (Button)findViewById(R.id.summit_button);
        linearLayout = (LinearLayout)findViewById(R.id.scrollView_linear_layout);
        profileImage = (CircleImageView)findViewById(R.id.cur_profile_img);

        selectFromAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.create(AddGalleryActivity.this).start();
            }
        });
        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        Glide.with(this).load(user.getPhotoUrl().toString()).into(profileImage);

        uid = user.getUid();


        storage_image_path = storageReference.child("restaurant_album").child(uid);
        db_image_Path = databaseReference.child("Restaurant").child(uid).child("Gallery");

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddGalleryActivity.this, BottomNavigationBarActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, final int resultCode, Intent data) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            // Get a list of picked images
            images = ImagePicker.getImages(data);

            for (int i = 0; i < images.size(); i ++) {
                final Uri imageUri = Uri.fromFile(new File(images.get(i).getPath()));

                ImageView imageView = new ImageView(AddGalleryActivity.this);
                imageView.setImageURI(imageUri);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                linearLayout.addView(imageView);

                storage_image_path = storage_image_path.child(String.valueOf(i) + ".jpg");
                db_image_Path = db_image_Path.child(String.valueOf(i));

                storage_image_path.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            String imageUrl = task.getResult().getDownloadUrl().toString();
                            db_image_Path.setValue(imageUrl);
                        }
                    }
                });
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
