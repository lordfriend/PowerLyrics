package io.nya.powerlyrics;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import io.nya.powerlyrics.lyric.Lyric;
import io.nya.powerlyrics.lyric.LyricEntry;

/**
 * Created by nene on 2/25/17.
 */

public class LrcAdapter extends BaseAdapter {

    private Context mContext;
    private Lyric mLyric;

    public LrcAdapter(Context context, Lyric lyric) {
        this.mContext = context;
        this.mLyric = lyric;
    }

    @Override
    public int getCount() {
        return mLyric.size();
    }

    @Override
    public Object getItem(int i) {
        return mLyric.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            view = inflater.inflate(R.layout.lyric_item, null);
        }
        LyricEntry lrcItem = mLyric.get(i);
        TextView textView = (TextView) view.findViewById(R.id.lyric_text);
        textView.setText(i + "  " + lrcItem.lyric);
        return view;
    }

//    private TextView makeTextLine() {
//        TextView view = new TextView(mContext);
//        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//        view.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
////        view.setTextAlignment()
//        return view;
//    }
}
