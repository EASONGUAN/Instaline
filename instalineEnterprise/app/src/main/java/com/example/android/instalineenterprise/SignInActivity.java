package com.example.android.instalineenterprise;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener{

    private Button signInButton;
    private EditText email;
    private EditText password;
    private TextView signUp;
    private ProgressDialog progress;

    // Firebase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        signInButton = (Button) findViewById(R.id.buttonSignUp);
        email = (EditText) findViewById(R.id.editTextEmail);
        password = (EditText) findViewById(R.id.editTextPassword);
        signUp = (TextView) findViewById(R.id.signIn);
        progress = new ProgressDialog(this);

        // Firebase
        mAuth = FirebaseAuth.getInstance();

        signInButton.setOnClickListener(this);
        signUp.setOnClickListener(this);
    }

    private void signInUser(){
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

        // Show progress
        progress.setMessage("Signing in");
        progress.show();

        System.out.println(emailInput);
        System.out.println(passwordInput);

        // Sign in with Firebase
        mAuth.signInWithEmailAndPassword(emailInput, passwordInput).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progress.dismiss();
                if(task.isSuccessful()){
                    // Login success
//                    Intent intent = new Intent(SignInActivity.this, FirstPage.class);
                    Intent intent = new Intent(SignInActivity.this, BottomNavigationBarActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else{
                    // Login failed
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onClick(View view){
        if(view == signInButton){
            signInUser();
        }

        if(view == signUp){
            // will open login activity here
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
