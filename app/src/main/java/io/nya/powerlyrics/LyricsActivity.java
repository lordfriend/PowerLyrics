package io.nya.powerlyrics;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.LinearLayout;

import java.io.IOException;

import io.nya.powerlyrics.lyric.Lyric;
import io.nya.powerlyrics.model.SearchResult;
import io.nya.powerlyrics.service.NeteaseCloud;
import io.nya.powerlyrics.view._LyricView;


public class LyricsActivity extends Activity {

    private final static String LOG_TAG = LyricsActivity.class.getName();

    _LyricView mListView;
    LrcAdapter mAdapter;
    Handler mHandler = new Handler();

    Runnable mTimer =  new Runnable() {
        @Override
        public void run() {
            mListView.smoothScrollToPositionMiddle(14);
        }
    };

    Runnable mSetLyric = new Runnable() {
        @Override
        public void run() {
//            mListView.setAdapter(mAdapter);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_lyrics);

//        mListView = (LyricView) findViewById(R.id.lyric_view);
//        mLyricLinear = (LinearLayout) findViewById(R.id.lyric_linear);
//        Adapter adapter = new LrcAdapter();
//        String[] values = new String[] { "Android", "iPhone", "WindowsMobile",
//                "Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X",
//                "Linux", "OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux",
//                "OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux", "OS/2",
//                "Android", "iPhone", "WindowsMobile" };
//
//        final ArrayList<String> list = new ArrayList<String>();
//        for (int i = 0; i < values.length; ++i) {
//            list.add(i + " " + values[i]);
//        }
//        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
//                android.R.layout.simple_list_item_1, list);
//        mListView.setAdapter(adapter);

//        mHandler.postDelayed(mTimer, 5000);

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    NeteaseCloud cloud = new NeteaseCloud();
//                    SearchResult result = cloud.searchMusic("Sweet Treasure");
//                    String lyricStr = cloud.getLyric(result.songs[0].id).lrc.lyric;
//                    Lyric lyric = LyricParser.parse(lyricStr);
//                    mAdapter = new LrcAdapter(LyricsActivity.this.getApplicationContext(), lyric);
//                    mHandler.post(mSetLyric);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

}
