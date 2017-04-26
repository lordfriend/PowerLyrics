package io.nya.powerlyrics;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

public class LyricChooserActivity extends Activity {

    ListView mSongChooser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lyric_chooser);
        mSongChooser = (ListView) findViewById(R.id.song_chooser);
    }
}
