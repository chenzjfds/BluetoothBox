package com.actions.bluetoothbox.ui.remotemusic;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.actions.bluetoothbox.R;
import com.actions.ibluz.manager.BluzManagerData;

import java.util.List;

public class MusicFolderListAdapter extends BaseAdapter {

    private final Context context;
    private LayoutInflater mInflater;
    private List<BluzManagerData.RemoteMusicFolder> musicFolderEntries;
    private int mPlayingPostion;


    public MusicFolderListAdapter(Context context, List<BluzManagerData.RemoteMusicFolder> entries) {
        this.context = context;
        this.musicFolderEntries = entries;
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return musicFolderEntries.size();
    }

    @Override
    public BluzManagerData.RemoteMusicFolder getItem(int position) {
        return musicFolderEntries.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FolderViewHolder folderViewHolder;
        if (convertView == null) {
            folderViewHolder = new FolderViewHolder();
            convertView = mInflater.inflate(R.layout.item_remote_music_folder, parent, false);
            folderViewHolder.textFolderMusicNum = (TextView) convertView.findViewById(R.id.text_folder_music_num);
            folderViewHolder.textFolderName = (TextView) convertView.findViewById(R.id.text_folder_name);

            convertView.setTag(folderViewHolder);
        } else {
            folderViewHolder = (FolderViewHolder) convertView.getTag();
        }

        folderViewHolder.textFolderName.setText(musicFolderEntries.get(position).name);
        int num = musicFolderEntries.get(position).musicEndIndex - musicFolderEntries.get(position).musicBeginIndex+1;
        folderViewHolder.textFolderMusicNum.setText(String.format(context.getString(R.string.remote_music_folder_song_num), num));

        if (mPlayingPostion == position) {
            folderViewHolder.textFolderName.setSingleLine(true);
            folderViewHolder.textFolderName.setSelected(true);
            folderViewHolder.textFolderName.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            convertView.setBackgroundColor(Color.BLUE);
        } else {
            folderViewHolder.textFolderName.setEllipsize(TextUtils.TruncateAt.END);
            convertView.setBackgroundColor(Color.TRANSPARENT);
        }

        return convertView;
    }


    void setPlayingPosition(int position) {
        mPlayingPostion = position;
    }

    private final class FolderViewHolder {
        public TextView textFolderName = null;
        public TextView textFolderMusicNum = null;
    }
}