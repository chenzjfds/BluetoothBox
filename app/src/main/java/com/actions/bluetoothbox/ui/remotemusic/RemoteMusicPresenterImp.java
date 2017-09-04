package com.actions.bluetoothbox.ui.remotemusic;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.actions.bluetoothbox.R;
import com.actions.bluetoothbox.data.local.PreferencesHelper;
import com.actions.bluetoothbox.ui.BrowserActivity;
import com.actions.bluetoothbox.util.Utils;
import com.actions.ibluz.manager.BluzManager;
import com.actions.ibluz.manager.BluzManagerData;
import com.actions.ibluz.manager.BluzManagerData.LoopMode;
import com.actions.ibluz.manager.IMusicManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenxiangjie on 2015/9/17.
 */
public class RemoteMusicPresenterImp extends RemoteMusicPresenter {

    public static final int CONTENT_CHANGE_ID_NOT_FETCHING_DATA = -1;
    private static final String TAG = "RemoteMusicPresenterImp";
    private static final int MESSAGE_REFRESH_UI = 1;
    private static final int MESSAGE_REFRESH_LYRIC = 2;
    private static final int MESSAGE_SET_LYRIC = 3;
    private static final int MESSAGE_GET_PLISTENTRY = 4;
    private static final int MESSAGE_GET_REMOTE_MUSIC_FOLDERS = 5;
    private final Context mContext;
    private final BrowserActivity mActivity;
    private final PreferencesHelper preferencesHelper;
    private boolean isNewFirmware = false;
    private int mSelectedMode = BluzManagerData.FuncMode.UNKNOWN;
    private BluzManager mBluzManager;
    private IMusicManager mMusicManager;
    private int mLoopPreset = LoopMode.UNKNOWN;
    private int mPlayStatePreset = BluzManagerData.PlayState.UNKNOWN;
    private boolean isESSShowed = true;
    // to indicated is showingPList in folders mode to handle different item click event
    private boolean isShowingPList = false;
    //the default showWay of old firmware
    private int showWay = BluzManagerData.RemoteMusicFoldersStatus.ShowWay.PLIST_VIEW;
    private List<BluzManagerData.PListEntry> mCachePListEntries = new ArrayList<>();
    private List<BluzManagerData.RemoteMusicFolder> mCacheRemoteMusicFolders = new ArrayList<BluzManagerData.RemoteMusicFolder>();
    private BluzManagerData.MusicEntry mCurrentMusicEntry;
    private BluzManagerData.OnMusicUIChangedListener mMusicUIChangedListener = new BluzManagerData.OnMusicUIChangedListener() {

        @Override
        public void onLoopChanged(int loop) {
            mLoopPreset = loop;
            if (loop == LoopMode.UNKNOWN) {
                mMusicManager.setLoopMode(LoopMode.ALL);
            } else {
                vista.updateLoopChanged(mLoopPreset);
            }
        }

        @Override
        public void onStateChanged(int state) {
            mPlayStatePreset = state;
            vista.updateStateChanged(mPlayStatePreset);
        }

    };
    private int totalFolderNum = 0;
    private int contentChangeId;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_REFRESH_UI:
                    vista.showCurrentMusicDuration(mMusicManager.getDuration());
                    vista.showCurrentMusicProgress(mMusicManager.getCurrentPosition());
                    mHandler.sendEmptyMessageDelayed(MESSAGE_REFRESH_UI, 300);
                    break;
                case MESSAGE_REFRESH_LYRIC:
                    vista.updateLyric(mMusicManager.getCurrentPosition());
                    break;
                case MESSAGE_SET_LYRIC:
                    vista.showLyric(mCurrentMusicEntry);
                    break;
                case MESSAGE_GET_PLISTENTRY:

                    if (mCachePListEntries.size() < mMusicManager.getPListSize()) {
                        Log.i(TAG, "handleMessage:mCachePListEntries.size()" + mCachePListEntries.size() + " < mMusicManager.getPListSize() ");
                        int left = mMusicManager.getPListSize() - mCachePListEntries.size();
                        mMusicManager.getPList(mCachePListEntries.size() + 1, left >= 5 ? 5 : left, mOnPListEntryReadyListener);
                    } else if (mCachePListEntries.size() == mMusicManager.getPListSize()) {
                        // sortMusicPList();
                        Log.i(TAG, "handleMessage:mCachePListEntries.size() == mMusicManager.getPListSize() ");
                        preferencesHelper.storeMusicPListAndLength(mSelectedMode, mCachePListEntries, mBluzManager.getMusicFolderList());
                        vista.showPlayingMusic(mCurrentMusicEntry.index);
                        vista.showCurrentMusicDuration(mMusicManager.getDuration());
                        if (showWay == BluzManagerData.RemoteMusicFoldersStatus.ShowWay.MUSIC_FOLDERS_VIEW) {
                            // if firmware support music folders view, we need to get the remote music folders after finish getting plist
                            Log.i(TAG, "handleMessage: new firmware getRemoteMusicFolders");
                            getRemoteMusicFolders();
                        } else {
                            // this branch is the original getPList branch
                            Log.i(TAG, "handleMessage: old firmware showPList");
                            vista.hideLoading();
                            vista.showCurrentMusicDuration(mMusicManager.getDuration());
                            vista.showPList(mCachePListEntries);
                        }
                    }
                    break;
                case MESSAGE_GET_REMOTE_MUSIC_FOLDERS:
                    if (mCacheRemoteMusicFolders.size() < totalFolderNum) {
                        Log.i(TAG, "handleMessage:mCacheRemoteMusicFolders.size() " + mCacheRemoteMusicFolders.size() + " < totalFolderNum() " + totalFolderNum);
                        int foldersLeft = totalFolderNum - mCacheRemoteMusicFolders.size();
                        mMusicManager.getRemoteMusicFolders(mCacheRemoteMusicFolders.size() + 1, foldersLeft >= 5 ? 5 : foldersLeft, mOnRemoteMusicFoldersReadyListener);
                    } else if (mCacheRemoteMusicFolders.size() == totalFolderNum) {
                        Log.i(TAG, "handleMessage: showRemoteMusicFolders");

                        preferencesHelper.saveFetchingContentChangeId(CONTENT_CHANGE_ID_NOT_FETCHING_DATA);
                        preferencesHelper.saveFetchingDeviceAddress("");

                        preferencesHelper.storeRemoteMusicFolders(mCacheRemoteMusicFolders, mSelectedMode);
                        //save contentChangeId only when we get the complete plist and remote music folder list
                        preferencesHelper.saveAsLastContentChangeId(contentChangeId);
                        //save device address here (getPList() save it immediately after comparing device address) because song length is not used to identify contentChange,
                        String currentConnectDeviceAddress = getConnectedDeviceAddress();
                        preferencesHelper.saveAsLastConnectedDeviceAddress(currentConnectDeviceAddress);
                        vista.hideLoading();
                        vista.showRemoteMusicFolders(mCacheRemoteMusicFolders);
                        vista.showPlayingFolder(getCurrentMusicFolder(mCurrentMusicEntry));
                        vista.showCurrentMusicDuration(mMusicManager.getDuration());

                    }

                    break;
            }
        }
    };
    private BluzManagerData.OnPListEntryReadyListener mOnPListEntryReadyListener = new BluzManagerData.OnPListEntryReadyListener() {

        @Override
        public void onReady(List<BluzManagerData.PListEntry> entry) {
            mCachePListEntries.addAll(entry);
            vista.updateLoadingMusic(mCachePListEntries.size(), mMusicManager.getPListSize());
            mHandler.sendEmptyMessage(MESSAGE_GET_PLISTENTRY);
        }
    };
    private BluzManagerData.OnRemoteMusicFoldersReadyListener mOnRemoteMusicFoldersReadyListener = new BluzManagerData.OnRemoteMusicFoldersReadyListener() {


        @Override
        public void onReady(List<BluzManagerData.RemoteMusicFolder> entries) {
            mCacheRemoteMusicFolders.addAll(entries);
            vista.updateLoadingFolders(mCacheRemoteMusicFolders.size(), totalFolderNum);
            mHandler.sendEmptyMessage(MESSAGE_GET_REMOTE_MUSIC_FOLDERS);
        }
    };
    private BluzManagerData.OnLyricEntryReadyListener mOnLyricEntryReadyListener = new BluzManagerData.OnLyricEntryReadyListener() {

        @Override
        public void onReady(byte[] buffer) {
            Utils.createExternalStoragePrivateFile(mContext, mCurrentMusicEntry.title + ".lrc", buffer);
            mHandler.sendEmptyMessage(MESSAGE_SET_LYRIC);
            mHandler.sendEmptyMessage(MESSAGE_REFRESH_LYRIC);
        }
    };
    private BluzManagerData.OnMusicEntryChangedListener mMusicEntryChangedListener = new BluzManagerData.OnMusicEntryChangedListener() {

        @Override
        public void onChanged(BluzManagerData.MusicEntry entry) {
            Log.d(TAG, "onChanged() called with: entry = [" + entry + "]");
            mCurrentMusicEntry = entry;
            vista.showPlayingMusic(mCurrentMusicEntry.index);
            vista.showPlayingFolder(getCurrentMusicFolder(mCurrentMusicEntry));

            vista.showCurrentMusicDuration(mMusicManager.getDuration());
            vista.showCurrentMusicEntryInfo(entry);
            initLyric(entry);

        }
    };
    private Runnable getPListDelayRunnable = new Runnable() {
        @Override
        public void run() {
            getPList();
        }
    };
    private boolean isStopped = false;
    private Runnable getRemoteFoldersStatusRetryRunnable = new Runnable() {
        @Override
        public void run() {
            getRemoteMusicFoldersStatus();
        }
    };

    public RemoteMusicPresenterImp(BrowserActivity activity, Context context, BluzManager bluzManager, PreferencesHelper preferencesHelper) {
        mActivity = activity;
        mContext = context;
        this.mBluzManager = bluzManager;
        this.preferencesHelper = preferencesHelper;
    }

    private int getCurrentMusicFolder(BluzManagerData.MusicEntry entry) {
        if (entry != null) {
            for (BluzManagerData.RemoteMusicFolder remoteMusicFolder : mCacheRemoteMusicFolders) {
                if (entry.index >= remoteMusicFolder.musicBeginIndex && entry.index <= remoteMusicFolder.musicEndIndex) {
                    return mCacheRemoteMusicFolders.indexOf(remoteMusicFolder);
                }
            }

        }
        return 0;
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart() called");
        isStopped = false;
        vista.showLoading();
        mMusicManager = mBluzManager.getMusicManager(new BluzManagerData.OnManagerReadyListener() {
            @Override
            public void onReady() {
                Log.d(TAG, "mMusicManager onReady() called with: " + "");
                getPListOrRemoteMusicFolders();

            }
        });
        mMusicManager.setOnMusicUIChangedListener(mMusicUIChangedListener);
        mMusicManager.setOnMusicEntryChangedListener(mMusicEntryChangedListener);
    }

    @Override
    public void onStop() {
        vista.hideFolderModeToggle();
        mHandler.removeMessages(MESSAGE_GET_PLISTENTRY);
        mHandler.removeMessages(MESSAGE_GET_REMOTE_MUSIC_FOLDERS);
        mHandler.removeCallbacks(getRemoteFoldersStatusRetryRunnable);
        mHandler.removeCallbacks(getPListDelayRunnable);
        isStopped = true;
        preferencesHelper.saveFetchingContentChangeId(CONTENT_CHANGE_ID_NOT_FETCHING_DATA);
        preferencesHelper.saveFetchingDeviceAddress("");
    }

    @Override
    void selectMusic(int index) {
        mMusicManager.select(index);
    }

    @Override
    void selectFolder(BluzManagerData.RemoteMusicFolder folder) {
        Log.d(TAG, "selectFolder() called with: folder = [" + folder + "]");
        isShowingPList = true;
        List<BluzManagerData.PListEntry> songsInFolder = new ArrayList<>();
        for (BluzManagerData.PListEntry mCachePListEntry : mCachePListEntries) {
            if (mCachePListEntry.index <= folder.musicEndIndex && mCachePListEntry.index >= folder.musicBeginIndex) {
                songsInFolder.add(mCachePListEntry);
            }
        }
        vista.hideRemoteMusicFolders();
        vista.showPList(songsInFolder);
        vista.showPlayingMusic(mCurrentMusicEntry.index);
        vista.showFolderModeToggle(folder.name);
    }

    @Override
    void toggleFolderMode() {
        if (isShowingPList) {
            vista.hidePList();
            vista.showRemoteMusicFolders(mCacheRemoteMusicFolders);
            vista.hideFolderModeToggle();
            isShowingPList = false;
        }
    }

    @Override
    void next() {
        mMusicManager.next();
    }

    @Override
    void playPause() {
        if (mPlayStatePreset == BluzManagerData.PlayState.PAUSED) {
            mMusicManager.play();
        } else {
            mMusicManager.pause();
        }
    }

    @Override
    void previous() {
        mMusicManager.previous();
    }

    @Override
    void switchLoop() {
        int loop = mLoopPreset;// mMusicManager.getLoopMode()
        switch (loop) {
            case LoopMode.ALL:
                loop = LoopMode.SINGLE;
                break;
            case LoopMode.SINGLE:
                loop = LoopMode.SHUFFLE;
                break;
            case LoopMode.SHUFFLE:
                loop = LoopMode.ALL;
                break;
            default:
                loop = LoopMode.ALL;
                break;
        }
        mMusicManager.setLoopMode(loop);
    }

    @Override
    void getPListOrRemoteMusicFolders() {
        vista.showLoading();
        getRemoteMusicFoldersStatus();
    }

    //this method is only called in older firmware which doesn't support remote music folders
    private void getPList() {
        Log.d(TAG, "getPList() called with: " + "");
        boolean isSDCardPListUpdate = false;
        boolean isUhostPListUpdate = false;
        boolean isCRecordPListUpdate = false;
        boolean isURecordPListUpdate = false;
        boolean isSpecialcatalogUpdate = false;
        boolean isChanged = mBluzManager.isContentChanged();

        String currentConnectDeviceAddress = getConnectedDeviceAddress();
        String preConnectDevcieAddress = preferencesHelper.getLastConnectedDeviceAddress();
        boolean isDeviceTheSame = currentConnectDeviceAddress.equalsIgnoreCase(preConnectDevcieAddress);
        preferencesHelper.saveAsLastConnectedDeviceAddress(currentConnectDeviceAddress);


        mSelectedMode = mActivity.getCurrentMode();
        List<BluzManagerData.FolderEntry> mFolderEntryList = mBluzManager.getMusicFolderList();
        int currentMusicListLength = mMusicManager.getPListSize();
        switch (mSelectedMode) {
            case BluzManagerData.FuncMode.CARD:
                int sdCardPlistLen = preferencesHelper.getCardMusicPListLength();
                Log.i(TAG, "getPList:sdCardPlistLen " + sdCardPlistLen);
                Log.i(TAG, "getPList:currentMusicListLength " + currentMusicListLength);
                if (sdCardPlistLen == 0 || currentMusicListLength != sdCardPlistLen) {
                    isSDCardPListUpdate = true;
                }
                break;
            case BluzManagerData.FuncMode.USB:
                int uHostCardPlistLen = preferencesHelper.getUHostMusicPListLength();
                if (uHostCardPlistLen == 0 || currentMusicListLength != uHostCardPlistLen) {
                    isUhostPListUpdate = true;
                }
                break;
            case BluzManagerData.FuncMode.CRECORD:
                int cRecordPlistLen = preferencesHelper.getCRecordMusicPListLength();
                if (cRecordPlistLen == 0 || currentMusicListLength != cRecordPlistLen) {
                    isCRecordPListUpdate = true;
                }
                break;
            case BluzManagerData.FuncMode.URECORD:
                int uRecordPlistLen = preferencesHelper.getURecordMusicPListLength();
                if (uRecordPlistLen == 0 || currentMusicListLength != uRecordPlistLen) {
                    isURecordPListUpdate = true;
                }
                break;
            default:
                for (int i = 0; i < mFolderEntryList.size(); i++) {
                    if (mSelectedMode == mFolderEntryList.get(i).value) {
                        int specialCatalogPlistLen = preferencesHelper.getSpecialFoldersMusicPListLength(mFolderEntryList.get(i).name);
                        if (specialCatalogPlistLen == 0 || currentMusicListLength != specialCatalogPlistLen) {
                            isSpecialcatalogUpdate = true;
                        }
                    }
                }
                break;
        }

        if (!isDeviceTheSame || isChanged || isSpecialcatalogUpdate || isSDCardPListUpdate || isUhostPListUpdate || isCRecordPListUpdate
                || isURecordPListUpdate) {

            Log.d(TAG, "getPList() content change " + " isChanged " + isChanged + " isSpecialcatalogUpdate " + isSpecialcatalogUpdate + " isSDCardPListUpdate " + isSDCardPListUpdate +
                    " isUhostPListUpdate " + isUhostPListUpdate + " isCRecordPListUpdate " + isCRecordPListUpdate + " isURecordPListUpdate "
                    + isURecordPListUpdate);
            if (isChanged || !isDeviceTheSame) {
                // Reset
                preferencesHelper.clearStoredMusicPListAndLength(mBluzManager.getMusicFolderList());
            }
            mCachePListEntries.clear();
            mHandler.sendEmptyMessage(MESSAGE_GET_PLISTENTRY);
        } else {
            Log.d(TAG, "getPList() nothing change " + "");
            vista.hideLoading();
            vista.showCurrentMusicDuration(mMusicManager.getDuration());
            mCachePListEntries.clear();
            mCachePListEntries.addAll(preferencesHelper.getStoredPList(mSelectedMode, mBluzManager.getMusicFolderList()));
            vista.showPList(mCachePListEntries);
        }
    }

    private String getConnectedDeviceAddress() {
        return mActivity.getConnectedDeviceAddress();
    }

    private void getRemoteMusicFoldersStatus() {
        Log.d(TAG, "getRemoteMusicFoldersStatus() called");
        //getCurrentMode just like original did in getPList
        mSelectedMode = mActivity.getCurrentMode();

        mMusicManager.getRemoteMusicFoldersStatus(new BluzManagerData.OnRemoteMusicFoldersStatusListener() {
            @Override
            public void onSuccess(BluzManagerData.RemoteMusicFoldersStatus status) {
                isNewFirmware = true;
                mHandler.removeCallbacks(getRemoteFoldersStatusRetryRunnable);
                Log.d(TAG, "onSuccess() called with: status = [" + status + "] ");
                if (status.scanStatus == BluzManagerData.RemoteMusicFoldersStatus.ScanStatus.SCANEND) {
                    showWay = status.showWay;
                    int lastContentChangeId = preferencesHelper.getLastContentChangeId();
                    int fetchingContentChangeId = preferencesHelper.getFetchingContentChangeId();
                    String fetchingDeviceAddress = preferencesHelper.getFetchingDeviceAddress();
                    String lastDeviceAddress = preferencesHelper.getLastConnectedDeviceAddress();
                    Log.i(TAG, "last contentChangeId " + lastContentChangeId + " contentChangeId " + contentChangeId +
                            " fetchingContentChangeId " + fetchingContentChangeId +
                            " lastDeviceAddress " + lastDeviceAddress + " new mac address " + status.macAddress +
                            " fetchingDeviceAddress " + fetchingDeviceAddress
                    );
                    if ((status.contentChangeId != lastContentChangeId && status.contentChangeId != fetchingContentChangeId) || (!status.macAddress.equals(lastDeviceAddress) && !status.macAddress.equals(fetchingDeviceAddress))) {
                        Log.i(TAG, "onSuccess: contentChange");
                        //both contentChangeID and macAddress is necessary to check is content change

                        totalFolderNum = status.totalFolderNum;
                        contentChangeId = status.contentChangeId;
                        //use contentChangeId and macAddress to indicate that if we are fetching data and is from the same sdcard
                        //when app in background mode, mode change is not notify and sdcard eject and insert is not known, only boolean value isFetchingData is not enough
                        preferencesHelper.saveFetchingContentChangeId(status.contentChangeId);
                        preferencesHelper.saveFetchingDeviceAddress(status.macAddress);
                        preferencesHelper.clearStoredMusicPListAndLength(mBluzManager.getMusicFolderList());
                        preferencesHelper.clearStoredRemoteMusicFolders();
                        mCachePListEntries.clear();
                        mCacheRemoteMusicFolders.clear();
                        mHandler.sendEmptyMessage(MESSAGE_GET_PLISTENTRY);
                    } else if (fetchingContentChangeId == CONTENT_CHANGE_ID_NOT_FETCHING_DATA) {
                        Log.i(TAG, "onSuccess: content the same");
                        vista.hideLoading();
                        vista.showCurrentMusicDuration(mMusicManager.getDuration());

                        mCachePListEntries.clear();
                        mCachePListEntries.addAll(preferencesHelper.getStoredPList(mSelectedMode, mBluzManager.getMusicFolderList()));
                        vista.hidePList();
                        vista.hideFolderModeToggle();
                        isShowingPList = false;

                        mCacheRemoteMusicFolders.clear();
                        mCacheRemoteMusicFolders.addAll(preferencesHelper.getStoredRemoteMusicFolders(mSelectedMode));
                        vista.showRemoteMusicFolders(mCacheRemoteMusicFolders);
                        vista.showPlayingFolder(getCurrentMusicFolder(mCurrentMusicEntry));
                    }
                } else if (status.scanStatus == BluzManagerData.RemoteMusicFoldersStatus.ScanStatus.SCANNING && preferencesHelper.getFetchingContentChangeId() == CONTENT_CHANGE_ID_NOT_FETCHING_DATA) {
                    Log.i(TAG, "onSuccess: but scan not end and not fetching data  so retry");
                    mHandler.postDelayed(getRemoteFoldersStatusRetryRunnable, 1000);
                }

            }

            @Override
            public void onFail(int errorCode) {
                Log.d(TAG, "onFail() called with: errorCode = [" + errorCode + "]" + " isStopped = [" + isStopped + "]" + " isNewFirmware = " + isNewFirmware);
                //if the firmware doesn't support remote music folders, we getPList like old times

                // when disconnect in connection fragment,
                // clearBackStack -> remote music fragment's onCreateView -> clearBackStack -> fragments's onDestroyView -> presenter onStop --some time later-->onFail
                // don't need to getPListDelayRunnable after onStop, isStopped here to prevent that

                //sometimes when we are getting plist entry, we can't get getRemoteMusicFoldersStatus response, so onFail is invoke, but
                // we are actualyy getting plist from new firmware
                // isNewFirmware to prevent us from getting plist in the old way
                if (!isStopped && !isNewFirmware && preferencesHelper.getFetchingContentChangeId() == CONTENT_CHANGE_ID_NOT_FETCHING_DATA) {
                    //delay to ensure content change because only after scan end content change
                    //getPList in manager ready already indicates scanEnd, but the call in onResume doesn't
                    //so we delay to ensure that
                    mHandler.postDelayed(getPListDelayRunnable, 1000);
                }

            }
        });
    }

    private void getRemoteMusicFolders() {
        mHandler.sendEmptyMessage(MESSAGE_GET_REMOTE_MUSIC_FOLDERS);
    }


    @Override
    void getCurrentEntry() {
        Log.d(TAG, "getCurrentEntry() called index " + mCurrentMusicEntry);
        if (mCurrentMusicEntry == null) {
            String unknown = mContext.getResources().getString(R.string.unknown);
            mCurrentMusicEntry = new BluzManagerData.MusicEntry();
            mCurrentMusicEntry.title = unknown;
            mCurrentMusicEntry.album = unknown;
            mCurrentMusicEntry.artist = unknown;
            mCurrentMusicEntry.genre = unknown;
            mCurrentMusicEntry.name = unknown;
        }
        if (mLoopPreset != LoopMode.UNKNOWN) {
            vista.updateLoopChanged(mLoopPreset);
        }
        vista.showCurrentMusicEntryInfo(mCurrentMusicEntry);
    }

    private void initLyric(BluzManagerData.MusicEntry entry) {
        mHandler.removeMessages(MESSAGE_SET_LYRIC);
        mHandler.removeMessages(MESSAGE_REFRESH_LYRIC);
        if (Utils.hasExternalStoragePrivateFile(mContext, entry.title + ".lrc")) {
            mHandler.sendEmptyMessage(MESSAGE_SET_LYRIC);
            mHandler.sendEmptyMessage(MESSAGE_REFRESH_LYRIC);
        } else {
            if (entry.lyric) {
                if ((!Utils.checkExternalStorageAvailable()[0] || !Utils.checkExternalStorageAvailable()[1]) && isESSShowed) {
                    isESSShowed = false;
                    Utils.displayToast(mContext, R.string.notice_lyric_warn);
                }
                mMusicManager.getLyric(mOnLyricEntryReadyListener);
            } else {
                mHandler.sendEmptyMessage(MESSAGE_SET_LYRIC);
            }
        }
    }

    @Override
    void getLyric() {
        mHandler.sendEmptyMessage(MESSAGE_SET_LYRIC);
        mHandler.sendEmptyMessage(MESSAGE_REFRESH_LYRIC);
    }

    @Override
    void cancelLyric() {
        mHandler.removeMessages(MESSAGE_SET_LYRIC);
        mHandler.removeMessages(MESSAGE_REFRESH_LYRIC);
    }

    @Override
    public void refreshLyricProgress(long delay) {
        mHandler.removeMessages(MESSAGE_REFRESH_LYRIC);
        mHandler.sendEmptyMessageDelayed(MESSAGE_REFRESH_LYRIC, delay);
    }

    @Override
    void startUpdateMusicProgress() {
        mHandler.sendEmptyMessage(MESSAGE_REFRESH_UI);
    }

    @Override
    void stopUpdateMusicProgress() {
        mHandler.removeMessages(MESSAGE_REFRESH_UI);
    }


}
