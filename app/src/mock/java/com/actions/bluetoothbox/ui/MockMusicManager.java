package com.actions.bluetoothbox.ui;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.actions.ibluz.manager.BluzManagerData;
import com.actions.ibluz.manager.BluzManagerData.RemoteMusicFoldersStatus;
import com.actions.ibluz.manager.IMusicManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenxiangjie on 2016/9/1.
 */

public class MockMusicManager implements IMusicManager {
    private static final String TAG = "MockMusicManager";
    private RemoteMusicFoldersStatus remoteMusicFoldersStatus = new RemoteMusicFoldersStatus();

    private boolean shouldGetRemoteMusicFoldersStatusReturnSuccess = false;
    private int pListSize;
    private BluzManagerData.OnMusicEntryChangedListener onMusicEntryChangedListener;

    public MockMusicManager() {

        remoteMusicFoldersStatus.totalFolderNum = 10;
        remoteMusicFoldersStatus.showWay = 1;

        pListSize = 10;

    }

    @Override
    public void next() {

    }

    @Override
    public void previous() {

    }

    @Override
    public void play() {

    }

    @Override
    public void select(int index) {

    }

    @Override
    public void pause() {

    }


    @Override
    public int getCurrentPosition() {
        return 10;
    }

    @Override
    public int getDuration() {
        return 20;
    }

    @Override
    public void setLoopMode(int mode) {

    }

    @Override
    public void getLyric(BluzManagerData.OnLyricEntryReadyListener listener) {

    }

    public void setCurrentMusicEntry(final BluzManagerData.MusicEntry musicEntry) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (onMusicEntryChangedListener != null) {
                    onMusicEntryChangedListener.onChanged(musicEntry);
                }
            }
        });

    }

    @Override
    public void setOnMusicEntryChangedListener(BluzManagerData.OnMusicEntryChangedListener listener) {
        this.onMusicEntryChangedListener = listener;
    }

    @Override
    public void setOnMusicUIChangedListener(BluzManagerData.OnMusicUIChangedListener listener) {

    }

    public void setGetPListSizeReturn(int pListSize) {
        this.pListSize = pListSize;
    }

    @Override
    public int getPListSize() {
        return pListSize;
    }

    @Override
    public void setPList(short[] list) {

    }

    @Override
    public void getPList(int start, int count, BluzManagerData.OnPListEntryReadyListener listener) {
        Log.d(TAG, "getPList() called with: start = [" + start + "], count = [" + count + "], onMusicEntryChangedListener = [" + listener + "]");
        listener.onReady(generateFakePList(start, count));
    }

    public void setGetRemoteMusicFoldersStatusReturnSuccess(RemoteMusicFoldersStatus remoteMusicFoldersStatus) {
        shouldGetRemoteMusicFoldersStatusReturnSuccess = true;
        this.remoteMusicFoldersStatus.contentChangeId = remoteMusicFoldersStatus.contentChangeId;
        this.remoteMusicFoldersStatus.showWay = remoteMusicFoldersStatus.showWay;
        this.remoteMusicFoldersStatus.totalFolderNum = remoteMusicFoldersStatus.totalFolderNum;
        this.remoteMusicFoldersStatus.macAddress = remoteMusicFoldersStatus.macAddress;
        this.remoteMusicFoldersStatus.scanStatus = remoteMusicFoldersStatus.scanStatus;

    }

    @Override
    public void getRemoteMusicFoldersStatus(BluzManagerData.OnRemoteMusicFoldersStatusListener listener) {
        if (shouldGetRemoteMusicFoldersStatusReturnSuccess) {
            listener.onSuccess(remoteMusicFoldersStatus);
        } else {
            listener.onFail(RemoteMusicFoldersStatus.ErrorCode.UNSUPPORTED_FIRMWARE);
        }
    }

    @Override
    public void getRemoteMusicFolders(int start, int count, BluzManagerData.OnRemoteMusicFoldersReadyListener listener) {
        Log.d(TAG, "getRemoteMusicFolders() called with: start = [" + start + "], count = [" + count + "], onMusicEntryChangedListener = [" + listener + "]");
        listener.onReady(generateFakeRemoteMusicFoldersEntry(start, count));
    }

    private List<BluzManagerData.RemoteMusicFolder> generateFakeRemoteMusicFoldersEntry(int start, int count) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List<BluzManagerData.RemoteMusicFolder> remoteMusicFolders = new ArrayList<>();
        for (int i = start; i < start + count; i++) {
            BluzManagerData.RemoteMusicFolder remoteMusicFolder = new BluzManagerData.RemoteMusicFolder();
            remoteMusicFolder.name = "TestFolder" + i;
            remoteMusicFolder.musicBeginIndex = (i - 1) * 5 + 1;
            remoteMusicFolder.musicEndIndex = (i) * 5;
            remoteMusicFolders.add(remoteMusicFolder);

            Log.i(TAG, "generateFakeRemoteMusicFoldersEntry: remoteMusicFolder name " + remoteMusicFolder.name
                    + "   musicBeginIndex " + remoteMusicFolder.musicBeginIndex
                    + "  musicEndIndex " + remoteMusicFolder.musicEndIndex
            );
        }

        return remoteMusicFolders;
    }


    private List<BluzManagerData.PListEntry> generateFakePList(int start, int count) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List<BluzManagerData.PListEntry> pListEntries = new ArrayList<>();

        for (int i = start; i < start + count; i++) {
            BluzManagerData.PListEntry pListEntry = new BluzManagerData.PListEntry();
            pListEntry.name = "Test" + i + ".mp3";
            pListEntry.index = i;
            pListEntry.artist = "Faker" + i;
            pListEntries.add(pListEntry);
            Log.i(TAG, "generateFakePList: pListEntry name " + pListEntry.name
                    + "   musicBeginIndex " + pListEntry.index
                    + "  musicEndIndex " + pListEntry.artist
            );
        }
        return pListEntries;
    }


}
