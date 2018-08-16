package com.example.android.instaline;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class FirstPage extends AppCompatActivity implements View.OnClickListener{

    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_page);
        logoutButton = (Button) findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(this);
    }

    public void signOut(){
        System.out.println("sign OUTTTTTTTTTTTTTTTTTTTTTT");
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(FirstPage.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View view){
        if(view == logoutButton){
            signOut();
        }
    }



//    @Override
//    public void onBackPressed() {
//        return;
//    }
}
