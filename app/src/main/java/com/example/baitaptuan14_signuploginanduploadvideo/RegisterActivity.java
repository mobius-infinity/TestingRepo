package com.example.baitaptuan14_signuploginanduploadvideo;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.baitaptuan14_signuploginanduploadvideo.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    ActivityRegisterBinding activityRegisterBinding;
    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityRegisterBinding = DataBindingUtil.setContentView(RegisterActivity.this, R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        singUpButtonAction();
        gologinButtonAction();
    }
    private void singUpButtonAction()
    {
        activityRegisterBinding.signupButton.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v)
          {
              String email = activityRegisterBinding.textEmailAddress.getText().toString().trim();
              String pass = activityRegisterBinding.textPassword.getText().toString().trim();
              if(email.isEmpty() )
              {
                  Toast.makeText(RegisterActivity.this, "Email should not be empty!", Toast.LENGTH_SHORT).show();
                    return;
              }
              else if(pass.isEmpty())
              {
                  Toast.makeText(RegisterActivity.this, "Password should not be empty!", Toast.LENGTH_SHORT).show();
                  return;
              }
              signupAction(email, pass);
          }
      });
    }
    private void signupAction(String email, String password)
    {
        activityRegisterBinding.loadingLayout.setVisibility(View.VISIBLE);
        activityRegisterBinding.progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            Toast.makeText(RegisterActivity.this, "Sign up succesfully", Toast.LENGTH_SHORT).show();
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                        }
                        activityRegisterBinding.loadingLayout.setVisibility(View.GONE);
                        activityRegisterBinding.progressBar.setVisibility(View.GONE);
                    }
                });
    }
    private void gologinButtonAction()
    {
        activityRegisterBinding.goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}