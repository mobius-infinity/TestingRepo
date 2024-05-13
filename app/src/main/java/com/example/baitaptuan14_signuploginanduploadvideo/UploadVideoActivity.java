package com.example.baitaptuan14_signuploginanduploadvideo;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.example.baitaptuan14_signuploginanduploadvideo.databinding.ActivityUploadVideoBinding;
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


public class UploadVideoActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    ActivityUploadVideoBinding activityWatchVideoBinding;
    private Uri mUri;
    FirebaseUser user;
    // Create a storage and realtime database reference from our app
    FirebaseStorage storage;
    FirebaseDatabase database;
    DatabaseReference databaseRef;
    StorageReference videoRef;
    UploadTask uploadTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityWatchVideoBinding = DataBindingUtil.setContentView(UploadVideoActivity.this, R.layout.activity_upload_video);
        //initialize firebase's stuffs
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance("https://signuploginanduploadvideo-default-rtdb.asia-southeast1.firebasedatabase.app");
        databaseRef = database.getReference();
        chooseVideoButton();
        uploadVideoButton();
    }
    private void updateUserVideoData(String URL)
    {
        FirebaseUser user = mAuth.getCurrentUser();

        if(user==null)
        {
            Log.d(TAG, "User is null!");
            return;
        }
        databaseRef.child("videoURLs").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> listUrls = new ArrayList<>();
                for (DataSnapshot snapshotData : snapshot.getChildren()) {
                    String data = snapshotData.getValue(String.class);
                    listUrls.add(data);
                }

                Map<String, Object> userUpdates = new HashMap<>();
                userUpdates.put(String.valueOf(listUrls.size()), URL);
                databaseRef.child("videoURLs").updateChildren(userUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG,"Update user's video URLs successfully");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG,"Failed in Updating user's video URLs");
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "Retrieving user's video URLs failed");
            }
        });
    }
    private void uploadVideoButton()
    {
        activityWatchVideoBinding.uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(UploadVideoActivity.this,"Uploading",Toast.LENGTH_SHORT).show();
                uploadVideo();
            }
        });
    }
    private void uploadVideo()
    {
        activityWatchVideoBinding.loadingLayout.setVisibility(View.VISIBLE);
        activityWatchVideoBinding.uploadProgressBar.setVisibility(View.VISIBLE);

        String videoName = mUri.getLastPathSegment() +"_"+user.getUid()+"_"+databaseRef.push().getKey();

        videoRef = storage.getReference().child("videos/" + videoName);
        uploadTask = videoRef.putFile(mUri);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(UploadVideoActivity.this,"Uploading failed",Toast.LENGTH_SHORT).show();
                activityWatchVideoBinding.uploadProgressBar.setVisibility(View.GONE);
                activityWatchVideoBinding.loadingLayout.setVisibility(View.GONE);
            }
        }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                activityWatchVideoBinding.uploadProgressBar.setVisibility(View.GONE);
                activityWatchVideoBinding.loadingLayout.setVisibility(View.GONE);
                //progressDialog.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                videoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Toast.makeText(UploadVideoActivity.this,"Upload successfully!",Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Receive video's url successfully");
                        updateUserVideoData(uri.toString());
                    }
                });
            }
        });
    }
    private void chooseVideoButton()
    {
        activityWatchVideoBinding.chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                activityWatchVideoBinding.videoProgressBar.setVisibility(View.VISIBLE);
                mActivityResultLauncher.launch(Intent.createChooser(intent, "Select Video"));
                activityWatchVideoBinding.videoProgressBar.setVisibility(View.GONE);
            }
        });
    }
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mAuth.signOut();
    }
    private final ActivityResultLauncher<Intent> mActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.e(TAG, "onActivityResult");
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        //request code
                        Intent data = result.getData();
                        if (data == null) {
                            return;
                        }
                        mUri = data.getData();
                        previewVideo();
                    }
                }
            }
    );
    private void previewVideo()
    {
        if(mUri == null) return;
        activityWatchVideoBinding.uploadButton.setVisibility(View.VISIBLE);
        activityWatchVideoBinding.videoView.setVideoURI(mUri);

        activityWatchVideoBinding.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {

                mp.start();
                float videoRatio = mp.getVideoWidth() / (float) mp.getVideoHeight();
                float screenRatio = activityWatchVideoBinding.videoView.getWidth() / (float) activityWatchVideoBinding.videoView.getHeight();
                float scale = videoRatio /screenRatio;
                if(scale >= 1f) {
                    activityWatchVideoBinding.videoView.setScaleX(scale);
                }
                else{
                    activityWatchVideoBinding.videoView.setScaleY(1f/scale);
                }
            }
        });

        activityWatchVideoBinding.videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.start();
            }
        });
    }

}