package com.example.android.instaline;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    // Firebase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
//        Intent intent = new Intent(MainActivity.this, MainIntroSlideActivity.class);
//        startActivity(intent);
//        finish();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null){
            System.out.println("User Authetication needed");
            Intent intent = new Intent(MainActivity.this, SignInMethodActivity.class);
            startActivity(intent);
            finish();
        } else{
            Intent intent = new Intent(MainActivity.this, BottomNavigationBarActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void openSignIn(View view){
        Intent i = new Intent(this, SignInActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    public void openSignUp(View view){
        Intent i = new Intent(this, SignUpActivity.class);
        startActivity(i);
    }

    public void openSignInGoogle(View view){
        Intent i = new Intent(this, SignInGoogleActivity.class);
        startActivity(i);
    }
}
