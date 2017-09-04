package com.actions.bluetoothbox.ui.remotemusic;


import com.actions.bluetoothbox.ui.base.Vista;
import com.actions.ibluz.manager.BluzManagerData;

import java.util.List;

/**
 * Created by chenxiangjie on 2015/9/17.
 */
public interface RemoteMusicVista extends Vista {


    void showFolderModeToggle(String name);

    void hideFolderModeToggle();

    void showLoading();

    void updateLoadingMusic(int progress, int total);

    void updateLoadingFolders(int progress, int total);

    void hideLoading();

    void showCurrentMusicProgress(int position);

    void showCurrentMusicEntryInfo(BluzManagerData.MusicEntry entry);

    void showCurrentMusicDuration(int duration);

    //scroll and highlight
    void showPlayingMusic(int index);

    void showPlayingFolder(int position);

    void showLyric(BluzManagerData.MusicEntry entry);

    void updateLyric(int position);

    void updateLoopChanged(int loopMode);

    void updateStateChanged(int state);

    void showPList(List<BluzManagerData.PListEntry> entries);

    void hidePList();

    void showRemoteMusicFolders(List<BluzManagerData.RemoteMusicFolder> musicFolderEntries);

    void hideRemoteMusicFolders();
}
