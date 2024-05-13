package com.example.baitaptuan14_signuploginanduploadvideo;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import com.example.baitaptuan14_signuploginanduploadvideo.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    SharedPreferences sharedPreference;
    Intent intent;
    User user;
    ActivityMainBinding activityMainBinding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = DataBindingUtil.setContentView(LoginActivity.this,R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        sharedPreference = getSharedPreferences("userInfo", Context.MODE_PRIVATE);

        //upload user's info (email, password) if user checked "remember me" box
        uploadLocalUserInfo();

        // make login button listening and doing its stuffs
        loginButtonAction();
        signupButtonAction();
    }
    private void loginButtonAction()
    {
        activityMainBinding.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email  = activityMainBinding.textEmailAddress.getText().toString().trim();
                String pass   = activityMainBinding.textPassword.getText().toString().trim();
                if(email.isEmpty())
                {
                    activityMainBinding.textEmailAddress.setError("Invalid email");
                    return;
                } else if (pass.isEmpty())
                {
                    activityMainBinding.textPassword.setError("Invalid password");
                    return;
                }
                boolean check  = activityMainBinding.checkBox.isChecked();
                if(activityMainBinding.checkBox.isChecked())
                {
                    setLocalUserInfo(email, pass, check);
                }
                else
                {
                    setLocalUserInfo("", "", false);
                }
                signinAction(email,pass);
            }
        });
    }
    private void uploadLocalUserInfo()
    {
        String userEmail = sharedPreference.getString("Email","");
        String userPass = sharedPreference.getString("Password","");
        boolean checkBox = sharedPreference.getBoolean("CheckBox",false);
        activityMainBinding.textEmailAddress.setText(userEmail);
        activityMainBinding.textPassword.setText(userPass);
        activityMainBinding.checkBox.setChecked(checkBox);
    }
    private void setLocalUserInfo(String email, String password, boolean checkBox)
    {
        SharedPreferences.Editor editor = sharedPreference.edit();
        editor.putString("Email",email);
        editor.putString("Password",password);
        editor.putBoolean("CheckBox",checkBox);
        editor.apply();
    }
    private void signinAction(String email, String password)
    {
        activityMainBinding.loadingLayout.setVisibility(View.VISIBLE);
        activityMainBinding.progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            intent = new Intent(LoginActivity.this, WatchVideoActivity.class);
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed!", Toast.LENGTH_SHORT).show();
                        }
                        activityMainBinding.loadingLayout.setVisibility(View.GONE);
                        activityMainBinding.progressBar.setVisibility(View.GONE);
                    }
                });
    }
    private void signupButtonAction()
    {
        activityMainBinding.signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(LoginActivity.this, RegisterActivity.class);
                Toast.makeText(LoginActivity.this, "Change Page!",Toast.LENGTH_LONG).show();
                startActivity(intent);
            }
        });
    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        if(currentUser != null){
//            reload();
//        }
    }
}