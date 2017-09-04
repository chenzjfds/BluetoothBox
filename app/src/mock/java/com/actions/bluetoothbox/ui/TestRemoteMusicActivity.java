package com.actions.bluetoothbox.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.actions.bluetoothbox.util.Preferences;
import com.actions.ibluz.manager.BluzManagerData;
import com.actions.ibluz.manager.IMusicManager;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.isA;

/**
 * Created by chenxiangjie on 2016/9/2.
 */

public class TestRemoteMusicActivity extends BrowserActivity {


    private BluzManagerData.OnManagerReadyListener onManagerReadyListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mockMusicManager = new MockMusicManager();
        Mockito.doAnswer(new Answer<IMusicManager>() {
            @Override
            public IMusicManager answer(final InvocationOnMock invocation) throws Throwable {
                onManagerReadyListener = ((BluzManagerData.OnManagerReadyListener) invocation.getArguments()[0]);
                return mockMusicManager;

            }
        }).when(bluzManager).getMusicManager(isA(BluzManagerData.OnManagerReadyListener.class));
    }

    public void  onManagerReady(){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                getOnManagerReadyListener().onReady();
            }
        });
    }
    public BluzManagerData.OnManagerReadyListener getOnManagerReadyListener() {
        return onManagerReadyListener;
    }



    public void setupOldFirmware(int pListSize){
        setCurrentMode(BluzManagerData.FuncMode.CARD);
        mockMusicManager.setGetPListSizeReturn(pListSize);
    }


    public void setupNewFirmwareDifferenctContentChangeIdDifferentMacAddress(int pListSize){
        setCurrentMode(BluzManagerData.FuncMode.CARD);
        mockMusicManager.setGetPListSizeReturn(pListSize);
        BluzManagerData.RemoteMusicFoldersStatus remoteMusicFoldersStatus = new BluzManagerData.RemoteMusicFoldersStatus();
        remoteMusicFoldersStatus.showWay = 1;
        remoteMusicFoldersStatus.scanStatus = BluzManagerData.RemoteMusicFoldersStatus.ScanStatus.SCANEND;
        setLastConnectedDeviceAddress("FF:FF:FF:FF:FF:FF");
        remoteMusicFoldersStatus.macAddress = "00:FF:FF:FF:FF:FF";
        remoteMusicFoldersStatus.totalFolderNum = 10;
        setLastContentChangeId(50);
        remoteMusicFoldersStatus.contentChangeId = 51;

        mockMusicManager.setGetRemoteMusicFoldersStatusReturnSuccess(remoteMusicFoldersStatus);
    }

    public void setupNewFirmwareSameContentChangeIdsameMacAddress(){
        setCurrentMode(BluzManagerData.FuncMode.CARD);
        setupStoredRemoteMusicFolders();
        setupStoredPList();
        BluzManagerData.RemoteMusicFoldersStatus remoteMusicFoldersStatus = new BluzManagerData.RemoteMusicFoldersStatus();
        remoteMusicFoldersStatus.showWay = 1;
        remoteMusicFoldersStatus.scanStatus = BluzManagerData.RemoteMusicFoldersStatus.ScanStatus.SCANEND;
        setLastConnectedDeviceAddress("FF:FF:FF:FF:FF:FF");
        remoteMusicFoldersStatus.macAddress = "FF:FF:FF:FF:FF:FF";
        remoteMusicFoldersStatus.totalFolderNum = 10;
        setLastContentChangeId(50);
        remoteMusicFoldersStatus.contentChangeId = 50;
        mockMusicManager.setGetRemoteMusicFoldersStatusReturnSuccess(remoteMusicFoldersStatus);

    }

    private void setupStoredRemoteMusicFolders(){

        List<BluzManagerData.RemoteMusicFolder> folders = new ArrayList<>();
        BluzManagerData.RemoteMusicFolder remoteMusicFolder = new BluzManagerData.RemoteMusicFolder();
        remoteMusicFolder.name = "StoredTestFolder" + 1;
        remoteMusicFolder.musicBeginIndex = 1;
        remoteMusicFolder.musicEndIndex = 1;

        folders.add(remoteMusicFolder);
        Preferences.storeComplexDataInPreference(this, Preferences.KEY_SDCARD_MUSIC_FOLDER_LIST, folders);
    }

    private void setupStoredPList(){
        List<BluzManagerData.PListEntry> pListEntries = new ArrayList<>();
        BluzManagerData.PListEntry pListEntry = new BluzManagerData.PListEntry();
        pListEntry.name = "Stored Test" + 1 + ".mp3";
        pListEntry.index = 1;
        pListEntry.artist = "Faker" + 1;
        pListEntries.add(pListEntry);

        Preferences.storeComplexDataInPreference(this, Preferences.KEY_MUSIC_PLIST, pListEntries);

    }

    public void setLastConnectedDeviceAddress(String macAddress){
        Preferences.setPreferences(this, Preferences.KEY_DEVICE_ADDRESS, macAddress);
    }

    public void setLastContentChangeId(int contentChangeId){
        Preferences.setPreferences(this, Preferences.KEY_CONTENT_CHANGE_ID, contentChangeId);
    }


    public void setCurrentMusicEntryIndex(int index){

        BluzManagerData.MusicEntry musicEntry = new BluzManagerData.MusicEntry();
        musicEntry.index = index;

        mockMusicManager.setCurrentMusicEntry(musicEntry);
    }

}
