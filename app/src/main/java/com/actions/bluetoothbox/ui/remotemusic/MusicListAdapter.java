package com.actions.bluetoothbox.ui.remotemusic;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.actions.bluetoothbox.R;
import com.actions.ibluz.manager.BluzManagerData;

import java.util.List;


public class MusicListAdapter extends BaseAdapter {

    private static final String TAG = "MusicListAdapter";
    private LayoutInflater mInflater;
    private List<BluzManagerData.PListEntry> pListEntries;
    private int mSelectedPostion;
    public static final int MUSIC_NOT_IN_THE_LIST = -1;


    public MusicListAdapter(Context context, List<BluzManagerData.PListEntry> entries) {
        this.pListEntries = entries;
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return pListEntries.size();
    }

    @Override
    public BluzManagerData.PListEntry getItem(int position) {
        // must implement this so that espresso test can get listview item data
        return pListEntries.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MusicViewHolder musicViewHolder;

        if (convertView == null) {
            musicViewHolder = new MusicViewHolder();
            convertView = mInflater.inflate(R.layout.item_remote_music_music, parent, false);
            musicViewHolder.textMusicName = (TextView) convertView.findViewById(R.id.text_music_name);
            musicViewHolder.textArtistName = (TextView) convertView.findViewById(R.id.text_music_artist);
            convertView.setTag(musicViewHolder);
        } else {
            musicViewHolder = (MusicViewHolder) convertView.getTag();
        }

        musicViewHolder.textMusicName.setText(pListEntries.get(position).name);
        musicViewHolder.textArtistName.setText(pListEntries.get(position).artist);
        if (mSelectedPostion == position) {
            musicViewHolder.textMusicName.setSingleLine(true);
            musicViewHolder.textMusicName.setSelected(true);
            musicViewHolder.textMusicName.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            convertView.setBackgroundColor(Color.BLUE);
        } else {
            musicViewHolder.textMusicName.setEllipsize(TextUtils.TruncateAt.END);
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }

        return convertView;
    }

    void setSelectedPostion(int position) {
        mSelectedPostion = position;
    }


    int getMusicPositionOfTheList(int index){
        Log.d(TAG, "getMusicPositionOfTheList() called with: index = [" + index + "]");
        for (BluzManagerData.PListEntry pListEntry : pListEntries) {
            if (pListEntry.index == index) {
                return pListEntries.indexOf(pListEntry);
            }
        }
        return MUSIC_NOT_IN_THE_LIST;
    }


    private final class MusicViewHolder {
        public TextView textMusicName = null;
        public TextView textArtistName = null;
    }


}