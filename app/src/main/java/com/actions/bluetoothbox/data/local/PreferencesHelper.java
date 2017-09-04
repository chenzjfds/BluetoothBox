package com.actions.bluetoothbox.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.actions.bluetoothbox.util.Preferences;
import com.actions.ibluz.manager.BluzManagerData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by chenxiangjie on 2016/9/13.
 */

public class PreferencesHelper {

    private static PreferencesHelper preferencesHelper;
    private final Context context;

    private PreferencesHelper(Context context) {
        this.context = context;
    }


    public static PreferencesHelper getInstance(Context context) {
        if (preferencesHelper == null) {
            preferencesHelper = new PreferencesHelper(context.getApplicationContext());
        }
        return preferencesHelper;
    }

    public void saveAsLastContentChangeId(int contentChangeId) {
        Preferences.setPreferences(context, Preferences.KEY_CONTENT_CHANGE_ID, contentChangeId);
    }

    public int getLastContentChangeId() {
        return (int) Preferences.getPreferences(context, Preferences.KEY_CONTENT_CHANGE_ID, -1);
    }

    public void saveFetchingContentChangeId(int contentChangeId) {
        Preferences.setPreferences(context, Preferences.KEY_FETCHING_CONTENT_CHANGE_ID, contentChangeId);
    }

    public int getFetchingContentChangeId() {
        return (int) Preferences.getPreferences(context, Preferences.KEY_FETCHING_CONTENT_CHANGE_ID, -1);
    }

    //for remote music
    public void saveFetchingDeviceAddress(String deviceAddress) {
        Preferences.setPreferences(context, Preferences.KEY_FETCHING_DEVICE_MAC_ADDRESS, deviceAddress);
    }

    //for remote music
    public String getFetchingDeviceAddress() {
        return (String) Preferences.getPreferences(context, Preferences.KEY_FETCHING_DEVICE_MAC_ADDRESS, "");
    }

    //for remote music
    public void saveAsLastConnectedDeviceAddress(String deviceAddress) {
        Preferences.setPreferences(context, Preferences.KEY_DEVICE_ADDRESS, deviceAddress);
    }

    //for remote music
    public String getLastConnectedDeviceAddress() {
        return (String) Preferences.getPreferences(context, Preferences.KEY_DEVICE_ADDRESS, "");
    }

    public int getCardMusicPListLength() {
        return (Integer) Preferences.getPreferences(context, Preferences.KEY_MUSIC_PLIST_LENGTH, 0);
    }

    public int getUHostMusicPListLength() {
        return (Integer) Preferences.getPreferences(context, Preferences.KEY_UHOST_PLIST_LENGTH, 0);
    }

    public int getCRecordMusicPListLength() {
        return (Integer) Preferences.getPreferences(context, Preferences.KEY_CRECORD_PLIST_LENGTH, 0);
    }

    public int getURecordMusicPListLength() {
        return (Integer) Preferences.getPreferences(context, Preferences.KEY_URECORD_PLIST_LENGTH, 0);
    }

    public int getSpecialFoldersMusicPListLength(String specialFolderName) {
        return (Integer) Preferences.getPreferences(context, specialFolderName + "_length", 0);
    }

    public void clearStoredMusicPListAndLength(List<BluzManagerData.FolderEntry> folderEntries) {
        Preferences.setPreferences(context, Preferences.KEY_MUSIC_PLIST_LENGTH, 0);
        Preferences.setPreferences(context, Preferences.KEY_UHOST_PLIST_LENGTH, 0);
        Preferences.setPreferences(context, Preferences.KEY_CRECORD_PLIST_LENGTH, 0);
        Preferences.setPreferences(context, Preferences.KEY_URECORD_PLIST_LENGTH, 0);
        if (folderEntries != null) {
            for (int i = 0; i < folderEntries.size(); i++) {
                Preferences.setPreferences(context, folderEntries.get(i).name + "_length", 0);
            }
        }

    }

    public void storeMusicPListAndLength(int mode, List<BluzManagerData.PListEntry> pListEntries, List<BluzManagerData.FolderEntry> folderEntries) {
        Log.d(TAG, "storeMusicPListAndLength() called with: " + "");
        if (mode == BluzManagerData.FeatureFlag.SDCARD) {
            Preferences.setPreferences(context, Preferences.KEY_MUSIC_PLIST_LENGTH, pListEntries.size());
            Preferences.storeComplexDataInPreference(context, Preferences.KEY_MUSIC_PLIST, pListEntries);
        } else if (mode == BluzManagerData.FeatureFlag.UHOST) {
            Preferences.setPreferences(context, Preferences.KEY_UHOST_PLIST_LENGTH, pListEntries.size());
            Preferences.storeComplexDataInPreference(context, Preferences.KEY_UHOST_PLIST, pListEntries);
        } else if (mode == BluzManagerData.FuncMode.CRECORD) {
            Preferences.setPreferences(context, Preferences.KEY_CRECORD_PLIST_LENGTH, pListEntries.size());
            Preferences.storeComplexDataInPreference(context, Preferences.KEY_CRECORD_PLIST, pListEntries);
        } else if (mode == BluzManagerData.FuncMode.URECORD) {
            Preferences.setPreferences(context, Preferences.KEY_URECORD_PLIST_LENGTH, pListEntries.size());
            Preferences.storeComplexDataInPreference(context, Preferences.KEY_URECORD_PLIST, pListEntries);
        } else {
            if (folderEntries != null) {
                for (int i = 0; i < folderEntries.size(); i++) {
                    if (mode == folderEntries.get(i).value) {
                        Preferences.setPreferences(context, folderEntries.get(i).name + "_length",  pListEntries.size());
                        Preferences.storeComplexDataInPreference(context, folderEntries.get(i).name, pListEntries);
                    }
                }
            }
        }
    }

    public List<BluzManagerData.PListEntry> getStoredPList(int mode, List<BluzManagerData.FolderEntry> folderEntries) {
        List<BluzManagerData.PListEntry> tempPListList = new ArrayList<BluzManagerData.PListEntry>();
        try {
            if (mode == BluzManagerData.FeatureFlag.SDCARD) {
                tempPListList = (List<BluzManagerData.PListEntry>) Preferences.getComplexDataInPreference(context, Preferences.KEY_MUSIC_PLIST);
            } else if (mode == BluzManagerData.FeatureFlag.UHOST) {
                tempPListList = (List<BluzManagerData.PListEntry>) Preferences.getComplexDataInPreference(context, Preferences.KEY_UHOST_PLIST);
            } else if (mode == BluzManagerData.FuncMode.CRECORD) {
                tempPListList = (List<BluzManagerData.PListEntry>) Preferences.getComplexDataInPreference(context, Preferences.KEY_CRECORD_PLIST);
            } else if (mode == BluzManagerData.FuncMode.URECORD) {
                tempPListList = (List<BluzManagerData.PListEntry>) Preferences.getComplexDataInPreference(context, Preferences.KEY_URECORD_PLIST);
            } else {
                if (folderEntries != null) {
                    for (int i = 0; i < folderEntries.size(); i++) {
                        if (mode == folderEntries.get(i).value) {
                            tempPListList = (List<BluzManagerData.PListEntry>) Preferences.getComplexDataInPreference(context, folderEntries.get(i).name);
                        }
                    }
                }
            }
            return tempPListList;
        } catch (ClassCastException e) {
            return Collections.emptyList();
        }

    }

    public void clearStoredRemoteMusicFolders() {
        Preferences.removePreferences(context, Preferences.KEY_SDCARD_MUSIC_FOLDER_LIST);
        Preferences.removePreferences(context, Preferences.KEY_UHOST_MUSIC_FOLDER_LIST);
    }

    public void storeRemoteMusicFolders(List<BluzManagerData.RemoteMusicFolder> folders, int mode) {
        Log.d(TAG, "storeRemoteMusicFolders() called with: folders = [" + folders + "]");
        if (mode == BluzManagerData.FeatureFlag.SDCARD) {
            Preferences.storeComplexDataInPreference(context, Preferences.KEY_SDCARD_MUSIC_FOLDER_LIST, folders);
        } else if (mode == BluzManagerData.FeatureFlag.UHOST) {
            Preferences.storeComplexDataInPreference(context, Preferences.KEY_UHOST_MUSIC_FOLDER_LIST, folders);
        }
    }

    public List<BluzManagerData.RemoteMusicFolder> getStoredRemoteMusicFolders(int mode) {
        try {
            List<BluzManagerData.RemoteMusicFolder> tempRemoteMusicFolders = new ArrayList<BluzManagerData.RemoteMusicFolder>();
            // we only need to take care sdcard mode and uhost mode
            // we don't need to store length because we have contentChangeId and macAddress to check is content change
            if (mode == BluzManagerData.FeatureFlag.SDCARD) {
                tempRemoteMusicFolders = (List<BluzManagerData.RemoteMusicFolder>) Preferences.getComplexDataInPreference(context, Preferences.KEY_SDCARD_MUSIC_FOLDER_LIST);
            } else if (mode == BluzManagerData.FeatureFlag.UHOST) {
                tempRemoteMusicFolders = (List<BluzManagerData.RemoteMusicFolder>) Preferences.getComplexDataInPreference(context, Preferences.KEY_UHOST_MUSIC_FOLDER_LIST);
            }

            Log.d(TAG, "getStoredRemoteMusicFolders() called " + "[ " + tempRemoteMusicFolders + "]");
            return tempRemoteMusicFolders;
        } catch (ClassCastException e) {
            return Collections.emptyList();
        }
    }


    public void clearAll() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        settings.edit().clear().commit();
    }
}
