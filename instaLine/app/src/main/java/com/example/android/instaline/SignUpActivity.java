package com.example.android.instaline;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private Button signUpButton;
    private EditText email;
    private EditText password;
    private TextView signIn;
    private ProgressDialog progress;

    // Firebase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        signUpButton = (Button) findViewById(R.id.buttonSignUp);
        email = (EditText) findViewById(R.id.editTextEmail);
        password = (EditText) findViewById(R.id.editTextPassword);
        signIn = (TextView) findViewById(R.id.signIn);
        progress = new ProgressDialog(this);

        // Firebase
        mAuth = FirebaseAuth.getInstance();

        signUpButton.setOnClickListener(this);
        signIn.setOnClickListener(this);
    }

    private void registerUser(){
        String emailInput = email.getText().toString().trim();
        String passwordInput = password.getText().toString().trim();

        if(TextUtils.isEmpty(emailInput)){
            // email is empty
            email.setError("Please enter your email.");
            email.requestFocus();
            return;
        }

        if(TextUtils.isEmpty(passwordInput)){
            // password is empty
            password.setError("Please enter your password");
            password.requestFocus();
            return;
        }

        if (passwordInput.length()<6){
            password.setError("Minimum length of password should be 6");
            password.requestFocus();
            return;
        }

        // Show progress
        progress.setMessage("Resgistering User");
        progress.show();

        System.out.println(emailInput);
        System.out.println(passwordInput);

//        progress.dismiss();
//        Intent intent = new Intent(this, MainIntroSlideActivity.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(intent);
//                    finish();

        mAuth.createUserWithEmailAndPassword(emailInput, passwordInput).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    // User is successfully registered and logged in
                    progress.dismiss();
                    // Login success
                    Intent intent = new Intent(SignUpActivity.this, MainIntroSlideActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else{
                    String exception = task.getException().getMessage();
                    System.out.println(exception);
                    progress.dismiss();
                    Toast.makeText(SignUpActivity.this, exception, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    public void onClick(View view){
        if(view == signUpButton){
            registerUser();
        }

        if(view == signIn){
            // will open login activity here
            Intent i = new Intent(this, SignInActivity.class);
            startActivity(i);
            finish();
        }
    }
}
