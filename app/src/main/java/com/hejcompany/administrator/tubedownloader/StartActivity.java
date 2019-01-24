package com.hejcompany.administrator.tubedownloader;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

/**
 * Created by Administrator on 2018-11-01.
 */

public class StartActivity extends YouTubeBaseActivity implements
        YouTubePlayer.OnInitializedListener{
    private YouTubePlayerView ytpv;
    private YouTubePlayer ytp;
    final String serverKey = "AIzaSyDAXx2E0a6_0FskH8o9Qd5n9oVw36ZLrQ4";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ytpv = (YouTubePlayerView) findViewById(R.id.youtubeplayer);
        ytpv.initialize(serverKey, this);
    }

    @Override
    public void onInitializationFailure(
            com.google.android.youtube.player.YouTubePlayer.Provider arg0,
            YouTubeInitializationResult arg1) {
        Toast.makeText(this, "Initialization Fail", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onInitializationSuccess(
            com.google.android.youtube.player.YouTubePlayer.Provider arg0,
            YouTubePlayer arg1, boolean arg2) {
        ytp = arg1;

        Intent gt = getIntent();
        ytp.loadVideo(gt.getStringExtra("id"));   // videoID 값으로 video 재생
    }
}
