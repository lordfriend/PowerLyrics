package io.nya.powerlyrics.powerlyrics;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;


public class LyricsActivity extends Activity {


    private View mContentView;

    private View mControlsView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_lyrics);

        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

}
