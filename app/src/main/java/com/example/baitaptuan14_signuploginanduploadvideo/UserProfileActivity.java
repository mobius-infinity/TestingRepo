package com.example.baitaptuan14_signuploginanduploadvideo;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.baitaptuan14_signuploginanduploadvideo.databinding.ActivityUserProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity {

    ActivityUserProfileBinding activityUserProfileBinding;
    Uri imageUri;
    FirebaseStorage storage;
    StorageReference storageRef;
    FirebaseDatabase database;
    DatabaseReference databaseRef;
    FirebaseAuth mAuth;
    FirebaseUser user;
    UploadTask uploadTask;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        activityUserProfileBinding = DataBindingUtil.setContentView(UserProfileActivity.this,R.layout.activity_user_profile);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        storage = FirebaseStorage.getInstance();
        storageRef =storage.getReference("user_icons/");
        database = FirebaseDatabase.getInstance("https://signuploginanduploadvideo-default-rtdb.asia-southeast1.firebasedatabase.app");
        databaseRef = database.getReference();

        setUserProfile();
        changeImageAction();
    }
    private void setUserProfile()
    {
        activityUserProfileBinding.loadingLayout.setVisibility(View.VISIBLE);
        activityUserProfileBinding.uploadProgressBar.setVisibility(View.VISIBLE);

        databaseRef.child("users/"+user.getUid()+"/icon").addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                String imageUrl = snapshot.getValue(String.class);
                if(imageUrl!=null && !imageUrl.isEmpty())
                {
                    Glide.with(UserProfileActivity.this).load(imageUrl).into(activityUserProfileBinding.userImage);
                }
                Log.d("UserProfile", "Retrieving user's image profile completed");
                databaseRef.child("users/"+user.getUid()+"/videos").addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        ArrayList<VideoModel> listUrls = new ArrayList<>();
                        for (DataSnapshot snapshotData : snapshot.getChildren())
                        {
                            VideoModel data = snapshotData.getValue(VideoModel.class);
                            listUrls.add(data);
                        }
                        activityUserProfileBinding.emailText.append(" "+user.getEmail());
                        activityUserProfileBinding.totalVideoText.append(" "+listUrls.size());
                        activityUserProfileBinding.loadingLayout.setVisibility(View.GONE);
                        activityUserProfileBinding.uploadProgressBar.setVisibility(View.GONE);
                        Log.d("UserProfile", "Retrieving user's video list completed");
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {
                        Log.d("UserProfile", "Retrieving user's video list failed");
                        activityUserProfileBinding.loadingLayout.setVisibility(View.GONE);
                        activityUserProfileBinding.uploadProgressBar.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                activityUserProfileBinding.loadingLayout.setVisibility(View.GONE);
                activityUserProfileBinding.uploadProgressBar.setVisibility(View.GONE);
                Log.d("UserProfile", "Retrieving user's image profile failed");
            }
        });
    }
    private void updateUserData(String url)
    {
        Map<String, Object> map = new HashMap<>();
        map.put("icon", url);
        databaseRef.child("users/"+user.getUid()).updateChildren(map, new DatabaseReference.CompletionListener()
        {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref)
            {
                activityUserProfileBinding.loadingLayout.setVisibility(View.GONE);
                activityUserProfileBinding.uploadProgressBar.setVisibility(View.GONE);
                if (error != null)
                    Toast.makeText(UserProfileActivity.this, "Profile is NOT saved", Toast.LENGTH_SHORT).show();
                else {
                    Toast.makeText(UserProfileActivity.this, "Profile is saved", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void uploadImageAction()
    {
        activityUserProfileBinding.loadingLayout.setVisibility(View.VISIBLE);
        activityUserProfileBinding.uploadProgressBar.setVisibility(View.VISIBLE);
        String imageName = user.getUid()+"_profileImage";
       uploadTask = storageRef.child(imageName).putFile(imageUri);
       uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
       {
           @Override
           public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
           {
               Log.d("UserProfile", "Upload image completed");

               storageRef.child(imageName).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
               {
                   @Override
                   public void onSuccess(Uri uri)
                   {
                       Log.d("UserProfile", "Get image's URL completed");

                       updateUserData(uri.toString());
                   }
               }).addOnFailureListener(new OnFailureListener()
               {
                   @Override
                   public void onFailure(@NonNull Exception e) {
                       Log.d("UserProfile", "Get image's URL failed");
                   }
               });
           }
       }).addOnFailureListener(new OnFailureListener()
       {
           @Override
           public void onFailure(@NonNull Exception e)
           {
               Log.d("UserProfile", "Upload Image failed");
           }
       });
    }
    private void changeImageAction()
    {
               activityUserProfileBinding.changeImageButton.setOnClickListener(new View.OnClickListener()
               {
                   @Override
                   public void onClick(View v)
                   {
                       Intent intent = new Intent();
                       intent.setType("image/*");
                       intent.setAction(Intent.ACTION_GET_CONTENT);
                       mActivityResultLauncher.launch(Intent.createChooser(intent, "Select image"));
                   }
               });
           }

           private final ActivityResultLauncher<Intent> mActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                   new ActivityResultCallback<ActivityResult>()
                   {
                       @Override
                       public void onActivityResult(ActivityResult result)
                       {
                           Log.e(TAG, "onUpdating");
                           if (result.getResultCode() == Activity.RESULT_OK)
                           {
                               //request code
                               Intent data = result.getData();
                               if (data == null) return;

                               imageUri = data.getData();
                               activityUserProfileBinding.userImage.setImageURI(imageUri);
                               uploadImageAction();
                           }
                       }
                   }
           );
       }