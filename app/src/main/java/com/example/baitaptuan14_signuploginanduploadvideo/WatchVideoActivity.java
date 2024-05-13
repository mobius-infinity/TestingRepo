package com.example.baitaptuan14_signuploginanduploadvideo;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.baitaptuan14_signuploginanduploadvideo.databinding.ActivityWatchVideoBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WatchVideoActivity extends AppCompatActivity {
    FirebaseUser user;
    FirebaseStorage storage;
    StorageReference storageRef;
    FirebaseDatabase database;
    DatabaseReference databaseRef;
    String userIconURL;
    ActivityWatchVideoBinding activityWatchVideoBinding;
    List<VideoModel> listVideo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityWatchVideoBinding = DataBindingUtil.setContentView(this, R.layout.activity_watch_video);
        user = FirebaseAuth.getInstance().getCurrentUser();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance("https://signuploginanduploadvideo-default-rtdb.asia-southeast1.firebasedatabase.app");
        databaseRef = database.getReference();
        listVideo = new ArrayList<>();
        setUserInfo();
        getVideoInfo(0);
        userIconButtonAction();
        Toast.makeText(WatchVideoActivity.this,"Welcome " + user.getEmail(),Toast.LENGTH_SHORT).show();
    }
    private void getVideoInfo(int videoPosition)
    {
        activityWatchVideoBinding.videoProgressBar.setVisibility(View.VISIBLE);
        databaseRef.child("users/"+user.getUid()+"/videos").addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot videoData: snapshot.getChildren())
                {
                    VideoModel video = videoData.getValue(VideoModel.class);
                    listVideo.add(video);
                }
                Log.d("WatchVideo", "Getting videos completed");
                activityWatchVideoBinding.likedText.setText(listVideo.get(videoPosition).getLikes());
                activityWatchVideoBinding.dislikedText.setText(listVideo.get(videoPosition).getDislikes());
                activityWatchVideoBinding.guestEmailText.setText(user.getEmail());
                String videoURL = listVideo.get(videoPosition).getUrl();
                watchVideo(Uri.parse(videoURL));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("WatchVideo", "Getting videos failed");

            }
        });
        activityWatchVideoBinding.loadingLayout.setVisibility(View.VISIBLE);
        activityWatchVideoBinding.uploadProgressBar.setVisibility(View.VISIBLE);
        databaseRef.child("users/"+user.getUid()+"/icon").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userIconURL = snapshot.getValue(String.class);
                if( userIconURL!= null && !userIconURL.isEmpty())
                {
                    Glide.with(WatchVideoActivity.this).load(userIconURL).into(activityWatchVideoBinding.guestPerson);
                }
                else {
                    Log.d("User ERROR", "User's icon is null");
                }
                activityWatchVideoBinding.loadingLayout.setVisibility(View.GONE);
                activityWatchVideoBinding.uploadProgressBar.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("User ERROR", "Retrieve user's icon URL unsuccessfully");
                activityWatchVideoBinding.loadingLayout.setVisibility(View.GONE);
                activityWatchVideoBinding.uploadProgressBar.setVisibility(View.GONE);
            }
        });
    }
    private void watchVideo(Uri uri)
    {
        if(uri == null) {
            Log.d("WatchVideo", "Video is null");
            activityWatchVideoBinding.closeImage.setVisibility(View.VISIBLE);
            return;
        }
        activityWatchVideoBinding.videoView.setVideoURI(uri);
        activityWatchVideoBinding.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                activityWatchVideoBinding.videoProgressBar.setVisibility(View.GONE);
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
        activityWatchVideoBinding.videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                activityWatchVideoBinding.videoProgressBar.setVisibility(View.VISIBLE);
                return false;
            }
        });
    }
    private void setUserInfo()
    {
        activityWatchVideoBinding.loadingLayout.setVisibility(View.VISIBLE);
        activityWatchVideoBinding.uploadProgressBar.setVisibility(View.VISIBLE);
        databaseRef.child("users/"+user.getUid()+"/icon").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userIconURL = snapshot.getValue(String.class);
                if( userIconURL!= null && !userIconURL.isEmpty())
                {
                    Glide.with(WatchVideoActivity.this).load(userIconURL).into(activityWatchVideoBinding.userIconImage);
                }
                else {
                    Log.d("User ERROR", "User's icon is null");
                }
                activityWatchVideoBinding.loadingLayout.setVisibility(View.GONE);
                activityWatchVideoBinding.uploadProgressBar.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("User ERROR", "Retrieve user's icon URL unsuccessfully");
                activityWatchVideoBinding.loadingLayout.setVisibility(View.GONE);
                activityWatchVideoBinding.uploadProgressBar.setVisibility(View.GONE);
            }
        });
    }
    private void userIconButtonAction()
    {
        activityWatchVideoBinding.userIconImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WatchVideoActivity.this, UserProfileActivity.class);
                startActivity(intent);
            }
        });
    }

}