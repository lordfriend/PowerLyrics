package io.nya.powerlyrics.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import io.nya.powerlyrics.R;
import io.nya.powerlyrics.model.SearchResult;

/**
 * Adapter for SongChooser list view
 */

public class SongChooserAdapter extends BaseAdapter {

    private ArrayList<SearchResult.Song> mSongs;
    private Context mContext;

    public SongChooserAdapter(Context context, ArrayList<SearchResult.Song> songs) {
        mSongs = songs;
        mContext = context;
    }

    @Override
    public int getCount() {
        return mSongs.size();
    }

    @Override
    public Object getItem(int position) {
        return mSongs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mSongs.get(position).id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.song_chooser_item, parent, false);
            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.song_title);
            holder.album = (TextView) convertView.findViewById(R.id.song_album);
            holder.artist = (TextView) convertView.findViewById(R.id.song_artist);
            convertView.setTag(0, holder);
        } else {
            holder = (ViewHolder) convertView.getTag(0);
        }
        SearchResult.Song song = mSongs.get(position);
        holder.title.setText(song.name);
        if (song.album != null) {
            holder.album.setText(song.album.name);
        } else {
            holder.album.setText(mContext.getResources().getString(R.string.unknown_album));
        }

        if (song.artists != null) {
            String artists = "";
            for (int i = 0; i < song.artists.length; i++) {
                artists += song.artists[i].name;
                if (i != song.artists.length - 1) {
                    artists += ", ";
                }
            }
            holder.artist.setText(artists);
        }

        return convertView;
    }

    static class ViewHolder {
        TextView title;
        TextView album;
        TextView artist;
    }
}
