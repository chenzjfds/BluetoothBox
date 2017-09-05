package com.actions.bluetoothbox.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaMetadata;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.os.storage.StorageManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actions.bluetoothbox.R;
import com.actions.bluetoothbox.domain.BluetoothBoxControl;
import com.actions.bluetoothbox.ui.alarm.AlarmClockFragment;
import com.actions.bluetoothbox.ui.alarm.AlarmClockSettingFragment;
import com.actions.bluetoothbox.ui.base.BaseActivity;
import com.actions.bluetoothbox.ui.charge.ChargeFragment;
import com.actions.bluetoothbox.ui.connection.ConnectionFragment;
import com.actions.bluetoothbox.ui.linein.LineInFragment;
import com.actions.bluetoothbox.ui.localmusic.A2DPMusicFragment;
import com.actions.bluetoothbox.ui.ota.OTAFragment;
import com.actions.bluetoothbox.ui.radio.RadioFragment;
import com.actions.bluetoothbox.ui.record.RecordFragment;
import com.actions.bluetoothbox.ui.remotemusic.RemoteMusicFragment;
import com.actions.bluetoothbox.ui.usbsound.USBSoundFragment;
import com.actions.bluetoothbox.util.Constant;
import com.actions.bluetoothbox.util.Preferences;
import com.actions.bluetoothbox.util.Utils;
import com.actions.bluetoothbox.util.Utils.LittleEndian;
import com.actions.bluetoothbox.util.log.LogcatThread;
import com.actions.bluetoothbox.widget.VerticalSeekBar;
import com.actions.ibluz.factory.BluzDeviceFactory;
import com.actions.ibluz.factory.IBluzDevice;
import com.actions.ibluz.factory.IBluzDevice.OnConnectionListener;
import com.actions.ibluz.manager.BluzManager;
import com.actions.ibluz.manager.BluzManagerData;
import com.actions.ibluz.manager.BluzManagerData.CallbackListener;
import com.actions.ibluz.manager.BluzManagerData.DAEMode;
import com.actions.ibluz.manager.BluzManagerData.DAEOption;
import com.actions.ibluz.manager.BluzManagerData.EQMode;
import com.actions.ibluz.manager.BluzManagerData.FuncMode;
import com.actions.ibluz.manager.BluzManagerData.OnDAEChangedListener;
import com.actions.ibluz.manager.BluzManagerData.OnGlobalUIChangedListener;
import com.actions.ibluz.manager.BluzManagerData.OnHotplugChangedListener;
import com.actions.ibluz.manager.BluzManagerData.OnManagerReadyListener;
import com.actions.ibluz.manager.BluzManagerData.OnMessageListener;
import com.actions.ibluz.manager.BluzManagerData.OnSnoozeMessageReadyListener;
import com.actions.ibluz.manager.IAlarmManager;
import com.actions.ibluz.manager.IBluzManager;
import com.actions.ibluz.manager.IGlobalManager;
import com.actions.ibluz.ota.updater.OTAUpdater;
import com.actions.ibluz.ota.updater.OnCheckFirmwareListener;
import com.actions.ibluz.ota.updater.OnSendOtaDataListener;
import com.actions.ibluz.ota.updater.Update;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;


@SuppressLint("NewApi")
public class BrowserActivity extends BaseActivity implements OnAudioFocusChangeListener, OnSendOtaDataListener {
    public static final int OTA = 0x20;
    private static final String TAG = "BrowserActivity";
    private static final int[] STATE_CHARGE = {R.attr.state_incharge};
    private static final int[] STATE_NONE = {};
    private static final int[] mDialogRes = new int[]{R.array.array_dialog_normal, 0, 0, 0, 0, R.array.array_dialog_usbinsert, 0};
    public boolean mMediaFree = true;
    public int mEQMode = 0;
    public OTAUpdater mOtaUpdater;
    private IBluzDevice mBluzConnector;
    private BluzManager mBluzManager;
    private View mGlobalInfoLayout;
    private TextView mDeviceNameText;
    private SeekBar mVolumeSeekBar;
    private ImageView mBatteryImageView;
    private AudioManager mAudioManager;
    private ImageButton mSoundImageButton;
    private int mSeekBarVolume;
    private String mFragmentTag;
    private boolean mForeground = false;
    private File mFile;
    private Fragment mComingFragment = null;
    private Context mContext;
    private int mMode = FuncMode.UNKNOWN;
    private A2DPMusicFragment mA2DPMusicFragment;
    private View mEQDialogView;
    private View mEqSettingLayout;
    private View mSnoozeDialogView;
    private Spinner mEqTypeSpinner;
    private VerticalSeekBar[] mEqSeekBar;
    private List<int[]> mEqBandLevel = new ArrayList<int[]>();
    private String[] mEqTypes;
    private boolean mDaeChoose[] = new boolean[8];
    private boolean mDaeshow[] = new boolean[8];
    private String data = null;
    private LogcatThread mLogcatThread;
    private AlertDialog mUSBPlugDialog;
    private AlertDialog mDisconnectDialog;
    private AlertDialog mLowBatteryDialog;
    private AlertDialog mAlarmDialog = null;
    private AlertDialog mRebootDialog;
    private boolean mPausedByTransientLossOfFocus = false;
    private ComponentName mBluetoothBoxControl;
    private boolean isStopEQTrackingTouch = false;
    private int mSnoozeTime = 0;
    private int mSnoozeCount = 0;
    private int mSnoozeOvertime = 0;
    private IAlarmManager mAlarmManager = null;
    private MediaSession session;
    private long exitTime = 0;
    private OnSeekBarChangeListener mSeekBarChangeListener = new OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar verticalSeekBar, int progress, boolean fromUser) {
            if (mEQMode != EQMode.USER)
                return;

            int value = verticalSeekBar.getProgress() - 12;
            switch (verticalSeekBar.getId()) {
                case R.id.frequency80HzBar:
                    mEqBandLevel.get(mEQMode - 1)[0] = value;// EQMode.USER
                    break;
                case R.id.frequency200HzBar:
                    mEqBandLevel.get(mEQMode - 1)[1] = value;
                    break;
                case R.id.frequency500HzBar:
                    mEqBandLevel.get(mEQMode - 1)[2] = value;
                    break;
                case R.id.frequency1KHzBar:
                    mEqBandLevel.get(mEQMode - 1)[3] = value;
                    break;
                case R.id.frequency4KHzBar:
                    mEqBandLevel.get(mEQMode - 1)[4] = value;
                    break;
                case R.id.frequency8KHzBar:
                    mEqBandLevel.get(mEQMode - 1)[5] = value;
                    break;
                case R.id.frequency16KHzBar:
                    mEqBandLevel.get(mEQMode - 1)[6] = value;
                    break;
            }
            if (mEQMode == EQMode.USER && isStopEQTrackingTouch) {
                mBluzManager.setDAEEQParam(mEqBandLevel.get(mEQMode - 1));
                isStopEQTrackingTouch = false;
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar verticalSeekBar) {
            // TODO Auto-generated method stub
            Log.v(TAG, "onStartTrackingTouch");
        }

        @Override
        public void onStopTrackingTouch(SeekBar verticalSeekBar) {
            Log.v(TAG, "onStopTrackingTouch");
            isStopEQTrackingTouch = true;
        }
    };
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String cmd = intent.getStringExtra("command");
            Log.e(TAG, "cmd = " + cmd);
            A2DPMusicFragment fragment = (A2DPMusicFragment) getSupportFragmentManager().findFragmentByTag(SlideoutMenuFragment.FRAGMENT_TAG_A2DP);
            if (fragment != null) {
                if (cmd.equals("next")) {
                    fragment.playNextMusic();
                } else if (cmd.equals("pre")) {
                    fragment.playPreviousMusic();
                } else if (cmd.equals("play")) {
                    fragment.doPlay();
                } else if (cmd.equals("pause")) {
                    fragment.pause();
                } else if (cmd.equals("play-pause")) {
                    fragment.controlPauseResume();
                } else if (cmd.equals("fastforward")) {
                    fragment.doFastForward();
                } else if (cmd.equals("rewind")) {
                    fragment.doRewind();
                }
            }
        }
    };

    private OnConnectionListener mOnConnectionListener = new OnConnectionListener() {
        @Override
        public void onConnected(BluetoothDevice device) {
            Log.i(TAG, "onConnected");
            mDeviceNameText.setText((device == null) ? null : device.getName());
            setBluzDeviceChanged();
            stopBackgroundMusic();
        }

        @Override
        public void onDisconnected(BluetoothDevice device) {
            Log.i(TAG, "onDisconnected");
            resetMusicPlayState();
            mDeviceNameText.setText(null);
            setBluzDeviceDisconnected();
        }
    };
    private BroadcastReceiver mUnmountReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e(TAG, "actions :" + action);
            if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
                if (mLogcatThread.isAlive()) {
                    mLogcatThread.setState(LogcatThread.STATE_DONE);
                    Log.v(TAG, "STATE_DONE!");
                }
                mMediaFree = false;
                A2DPMusicFragment fragment = (A2DPMusicFragment) getSupportFragmentManager().findFragmentByTag(SlideoutMenuFragment.FRAGMENT_TAG_A2DP);
                if (fragment != null) {
                    fragment.doPauseResume();
                    fragment.release();
                    Constant.MusicPlayData.myMusicList.clear();
                    fragment.initTextShow();
                    fragment.updateListView();
                    Log.i(TAG, "i call fragment list clear ");
                    Toast.makeText(context, R.string.music_storge_busy, Toast.LENGTH_SHORT).show();
                }
            } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                mMediaFree = true;
                A2DPMusicFragment fragment = (A2DPMusicFragment) getSupportFragmentManager().findFragmentByTag(SlideoutMenuFragment.FRAGMENT_TAG_A2DP);
                if (fragment != null) {
                    // fragment.getMusicList();
                    try {
                        Thread.sleep(2000); // waitting 2s,for auto
                        // refresh music list .
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    fragment.updateList();
                    Log.i(TAG, "i call fragment updateList");
                }
            } else if (action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
                A2DPMusicFragment fragment = (A2DPMusicFragment) getSupportFragmentManager().findFragmentByTag(SlideoutMenuFragment.FRAGMENT_TAG_A2DP);
                if (fragment != null) {
                    Log.i(TAG, " i get ACTION_MEDIA_SCANNER_FINISHED");
                    fragment.updateList();
                }
            }

        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate");

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        Log.v(TAG, "density=" + displayMetrics.density + ", densityDpi=" + displayMetrics.densityDpi);

        setContentView(R.layout.fragment_main);
        mContext = this;
        mBluzConnector = getBluzConnector();
        File cardFile = Environment.getExternalStorageDirectory();
        mLogcatThread = new LogcatThread();

        if (!cardFile.canWrite()) {
            String[] paths;
            String extSdCard = null;
            try {
                StorageManager sm = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
                paths = (String[]) sm.getClass().getMethod("getVolumePaths", (Class[]) null).invoke(sm, (Object[]) null);
                String esd = Environment.getExternalStorageDirectory().getPath();
                for (int i = 0; i < paths.length; i++) {
                    if (paths[i].equals(esd)) {
                        continue;
                    }
                    File sdFile = new File(paths[i]);
                    if (sdFile.canWrite()) {
                        extSdCard = paths[i];
                        Log.i(TAG, "extsdcard:" + extSdCard);
                    }
                }
                data = extSdCard + "/" + this.getPackageName().toString() + "/" + this.getPackageName().toString() + ".log";
                mFile = new File(extSdCard + "/" + this.getPackageName().toString());
                mLogcatThread.setLogFilePath(data);
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                data = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + this.getPackageName().toString() + "/"
                        + this.getPackageName().toString() + ".log";
                mFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + this.getPackageName().toString());
                mLogcatThread.setLogFilePath(data);
            }
        } else {
            data = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + this.getPackageName().toString() + "/"
                    + this.getPackageName().toString() + ".log";
            mFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + this.getPackageName().toString());
            mLogcatThread.setLogFilePath(data);
        }

        Log.i(TAG, "data:" + data);

        if (!mFile.isDirectory()) {
            mFile.mkdir();
        }
        if (mBluzConnector == null) {
            showNotSupportedDialog();
        }

        if (mFile.canWrite() && mFile.canRead()) {
            mLogcatThread.start();
        }

        mGlobalInfoLayout = findViewById(R.id.globalInfoLayout);
        mDeviceNameText = (TextView) findViewById(R.id.deviceName_tv);
        mSoundImageButton = (ImageButton) findViewById(R.id.mute);
        mSoundImageButton.setImageResource(R.drawable.selector_muteon_button);
        mVolumeSeekBar = (SeekBar) findViewById(R.id.volume);
        mBatteryImageView = (ImageView) findViewById(R.id.battery);

        Utils.setAlphaForView(mGlobalInfoLayout, 0.5f);
        mVolumeSeekBar.setEnabled(false);
        mSoundImageButton.setEnabled(false);

        mForeground = false;
        initFragment();
        registerExternalStorageListener();
        actionBarSetup();
        avrcpSetup();
        resetMusicPlayState();
        mBluzConnector.setOnConnectionListener(mOnConnectionListener);
    }

    private void avrcpSetup() {
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        IntentFilter commandFilter = new IntentFilter();
        commandFilter.addAction(Constant.MusicPlayControl.SERVICECMD);
        commandFilter.addAction(Constant.MusicPlayControl.TOGGLEPAUSE_ACTION);
        commandFilter.addAction(Constant.MusicPlayControl.PAUSE_ACTION);
        commandFilter.addAction(Constant.MusicPlayControl.NEXT_ACTION);
        commandFilter.addAction(Constant.MusicPlayControl.PREVIOUS_ACTION);
        registerReceiver(mIntentReceiver, commandFilter);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG, "onResume");
        stopBackgroundMusic();
        if (mLogcatThread != null && mLogcatThread.getThreadState() == LogcatThread.STATE_DONE) {
            if (mFile.canWrite() && mFile.canRead() && mMediaFree) {
                mLogcatThread = null;
                mLogcatThread = new LogcatThread();
                mLogcatThread.setAppend();
                mLogcatThread.setLogFilePath(data);
                mLogcatThread.start();
            }
        }
    }

    @Override
    public void onResumeFragments() {
        super.onResumeFragments();
        Log.v(TAG, "onResumeFragments");

        mForeground = true;
        if (mComingFragment != null) {
            initFragment(mComingFragment, false, 0);
        }

        setBluzManagerForeground();
    }

    @Override
    public void finish() {
        super.finish();
        Log.v(TAG, "finish");
        /** android NOT guarantee that onDestroy() follows finish() */
        releaseAll();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v(TAG, "onPause");
        mForeground = false;
        if (mLogcatThread != null && mLogcatThread.isAlive()) {
            mLogcatThread.setState(LogcatThread.STATE_DONE);
            Log.v(TAG, "STATE_DONE!");
        }

        setBluzManagerForeground();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy");
        if (!isFinishing()) {
            Log.i(TAG, "not isFinishing");
            /**
             * consider the situation the normal destroy without finish(), when the app is in background
             */
            releaseAll();
        }
    }

    private void setBluzManagerForeground() {
        Log.v(TAG, "setBluzManagerForeground");
        if (mBluzManager != null) {
            mBluzManager.setForeground(mForeground);

        }
    }


    private void showLowElectricityRemindDialog() {
        if (mLowBatteryDialog == null) {
            Builder builder = new Builder(this);
            builder.setIcon(android.R.drawable.ic_dialog_alert);// ic_dialog_alert_holo_light
            builder.setTitle(R.string.charge_warm);
            builder.setMessage(R.string.charge_tip);
            builder.setNegativeButton(R.string.action_cancel, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            mLowBatteryDialog = builder.create();
        }
        if (!mLowBatteryDialog.isShowing()) {
            mLowBatteryDialog.show();
        }
    }


    private void releaseAll() {
        releaseReceiver();
        releaseOta();
        releaseManager();
        releaseDevice();
    }

    private void resetMusicPlayState() {
        Preferences.setPreferences(mContext, "PrePlayState", Constant.MusicPlayState.PLAY_STATE_PAUSE);
    }

    private void releaseReceiver() {
        mAudioManager.abandonAudioFocus(this);
        mAudioManager.unregisterMediaButtonEventReceiver(mBluetoothBoxControl);
        if (session != null) {
            session.setCallback(null);
            session.setActive(false);
            session.release();
        }

        unregisterReceiver(mIntentReceiver);
        unregisterReceiver(mUnmountReceiver);
    }

    private void releaseDevice() {
        Log.i(TAG, "releaseDevice: ");
        if (mBluzConnector != null) {
            mBluzConnector.setOnConnectionListener(null);
            mBluzConnector.release();
            mBluzConnector = null;
        }
    }

    private void releaseManager() {
        Log.i(TAG, "releaseManager: ");
        if (mBluzManager != null) {
            mBluzManager.setOnGlobalUIChangedListener(null);
            mBluzManager.release();
            mBluzManager = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - exitTime > 2000) {
            Toast.makeText(this, R.string.message_press_quit, Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    public SlidingMenu getSlidingMenu() {
        return super.getSlidingMenu();
    }

    public void setBluzDeviceChanged() {
        Log.i(TAG, "setBluzDeviceChanged");
        stopMusicPlayer();
        toggleGlobalInfo(true);
        createBluzManager();
        initOtaUpdater();
    }

    private void createBluzManager() {
        if (mBluzConnector == null) {
            mBluzManager = null;
        } else {
            mBluzManager = new BluzManager(mContext, mBluzConnector, new OnManagerReadyListener() {

                @Override
                public void onReady() {
                    Log.i(TAG, "BluzManager onReady: ");
                    if (mBluzManager == null) {
                        return;
                    }
                    mBluzManager.setSystemTime();
                    // fix when auto-connect in background, frequently
                    // data-exchange
                    // will interfere with phone call
                    mBluzManager.setForeground(mForeground);
                    mBluzManager.setOnMessageListener(new OnMessageListener() {

                        @Override
                        public void onToast(int messageId) {
                            Toast.makeText(mContext, 0, Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onDialog(int id, int messageId, CallbackListener listener) {
                            Log.v(TAG, "onDialog show");
                            final CallbackListener callback = listener;
                            callback.onReceive(5);
                            String[] res = getResources().getStringArray(mDialogRes[id]);

                            Builder builder = new Builder(mContext);
                            builder.setTitle(res[0]);
                            builder.setPositiveButton(res[1], new OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    callback.onPositive();
                                }
                            });
                            builder.setNegativeButton(res[2], new OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    callback.onNegative();
                                }
                            });

                            builder.setCancelable(false);
                            mUSBPlugDialog = builder.create();

                            if (!mUSBPlugDialog.isShowing()) {
                                mUSBPlugDialog.show();
                            }

                        }

                        @Override
                        public void onCancel() {
                            Log.v(TAG, "onCancel");
                            hideUSBPlugDialog();
                        }
                    });

                    mBluzManager.setOnHotplugChangedListener(new OnHotplugChangedListener() {

                        @Override
                        public void onUhostChanged(boolean visibility) {
                            mSlideoutMenuFragment.uhostMenuChanged(visibility);
                        }

                        @Override
                        public void onLineinChanged(boolean visibility) {
                            mSlideoutMenuFragment.lineinMenuChanged(visibility);
                        }

                        @Override
                        public void onCardChanged(boolean visibility) {
                            mSlideoutMenuFragment.cardMenuChanged(visibility);
                        }

                        @Override
                        public void onUSBSoundChanged(boolean visibility) {
                            mSlideoutMenuFragment.usbSoundMenuChanged(visibility);
                        }

                    });

                    mBluzManager.setOnGlobalUIChangedListener(new OnGlobalUIChangedListener() {

                        @Override
                        public void onVolumeChanged(int volume, boolean mute) {
                            mVolumeSeekBar.setProgress(volume);
                            if (mute) {
                                mSoundImageButton.setImageResource(R.drawable.selector_muteoff_button);
                                // mVolumeSeekBar.setEnabled(false);
                            } else {
                                mSoundImageButton.setImageResource(R.drawable.selector_muteon_button);
                                // mVolumeSeekBar.setEnabled(true);
                            }
                        }

                        @Override
                        public void onModeChanged(int mode) {
                            mMode = mode;
                            Log.v(TAG, "onModeChanged = " + mode);
                            Fragment newContentFragment = null;
                            mFragmentTag = SlideoutMenuFragment.FRAGMENT_TAG_A2DP;
                            boolean specialCatalogSelected = false;
                            switch (mode) {
                                case FuncMode.A2DP:
                                    if (mA2DPMusicFragment == null) {
                                        mA2DPMusicFragment = new A2DPMusicFragment();
                                    }
                                    newContentFragment = mA2DPMusicFragment;
                                    mFragmentTag = SlideoutMenuFragment.FRAGMENT_TAG_A2DP;
                                    break;

                                case FuncMode.USB:
                                    newContentFragment = new RemoteMusicFragment();
                                    mFragmentTag = SlideoutMenuFragment.FRAGMENT_TAG_UHOST;
                                    break;

                                case FuncMode.RADIO:
                                    newContentFragment = new RadioFragment();
                                    mFragmentTag = SlideoutMenuFragment.FRAGMENT_TAG_RADIO;
                                    break;

                                case FuncMode.LINEIN:
                                    newContentFragment = new LineInFragment();
                                    mFragmentTag = SlideoutMenuFragment.FRAGMENT_TAG_LINEIN;
                                    break;

                                case FuncMode.ALARM:
                                    newContentFragment = new AlarmClockFragment();
                                    mFragmentTag = SlideoutMenuFragment.FRAGMENT_TAG_ALARM;
                                    break;

                                case FuncMode.CARD:
                                    newContentFragment = new RemoteMusicFragment();
                                    mFragmentTag = SlideoutMenuFragment.FRAGMENT_TAG_CARD;
                                    break;

                                case FuncMode.CRECORD:
                                    newContentFragment = new RemoteMusicFragment();
                                    mFragmentTag = SlideoutMenuFragment.FRAGMENT_TAG_REC_CARDPLAYBACK;
                                    break;

                                case FuncMode.URECORD:
                                    newContentFragment = new RemoteMusicFragment();
                                    mFragmentTag = SlideoutMenuFragment.FRAGMENT_TAG_REC_UHOSTPLAYBACK;
                                    break;

                                case FuncMode.CARDREC:
                                    newContentFragment = new RecordFragment();
                                    mFragmentTag = SlideoutMenuFragment.FRAGMENT_TAG_REC_CARD;
                                    break;

                                case FuncMode.UHOSTREC:
                                    newContentFragment = new RecordFragment();
                                    mFragmentTag = SlideoutMenuFragment.FRAGMENT_TAG_REC_UHOST;
                                    break;

                                case FuncMode.USOUND:
                                    newContentFragment = new USBSoundFragment();
                                    mFragmentTag = SlideoutMenuFragment.FRAGMENT_TAG_USBSOUND;
                                    break;
                                case FuncMode.CHARGE:
                                    newContentFragment = new ChargeFragment();
                                    mFragmentTag = SlideoutMenuFragment.FRAGMENT_TAG_CHARGE;
                                    break;
                                default:
                                    newContentFragment = new RemoteMusicFragment();
                                    mFragmentTag = SlideoutMenuFragment.FRAGMENT_TAG_CARD;
                                    specialCatalogSelected = true;
                                    break;
                            }
                            initFragment(newContentFragment, specialCatalogSelected, mode);
                        }

                        @Override
                        public void onEQChanged(int eq) {
                            if (eq != EQMode.UNKNOWN) {
                                mEQMode = eq;
                                Preferences.setPreferences(mContext, Preferences.KEY_LINEIN_EQUALIZER_TYPE, mEQMode);
                            }
                        }

                        @Override
                        public void onBatteryChanged(int battery, boolean incharge) {
                            if (battery == 0 && !incharge) {
                                showLowElectricityRemindDialog();
                            }
                            mBatteryImageView.setImageLevel(battery);
                            mBatteryImageView.setImageResource(R.drawable.battery);
                            mBatteryImageView.setImageState(incharge ? STATE_CHARGE : STATE_NONE, true);// incharge
                        }
                    });
                    mBluzManager.setOnDAEChangedListener(new OnDAEChangedListener() {

                        @Override
                        public void onDAEModeChanged(int daeMode) {
                            Log.d(TAG, "onDAEModeChanged() called with: " + "daeMode = [" + daeMode + "]");
                            if (daeMode != DAEMode.UNKNOWN) {
                                Preferences.setPreferences(mContext, Preferences.KEY_DAE_MODE, daeMode);
                            }
                        }

                        @Override
                        public void onDAEOptionChanged(byte daeOption) {

                            Log.d(TAG, "onDAEOptionChanged() called with: " + "daeOption = [" + daeOption + "]");
                            if (daeOption != DAEOption.UNKNOWN) {
                                Preferences.setPreferences(mContext, Preferences.KEY_DAE_OPTION, (int) daeOption);
                            }
                        }

                    });
                    mAlarmManager = mBluzManager.getAlarmManager(new OnManagerReadyListener() {

                        @Override
                        public void onReady() {
                        }
                    });
                    // Initializes the state of whether to support the feature
                    mSlideoutMenuFragment.setFeatureFilter();
                    getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
                }
            });
        }
    }

    private void toggleGlobalInfo(boolean turnOn) {
        if (turnOn) {
            Utils.setAlphaForView(mGlobalInfoLayout, 1.0f);
            mVolumeSeekBar.setEnabled(true);
            mSoundImageButton.setEnabled(true);
            mSoundImageButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mBluzManager.switchMute();
                }
            });

            mVolumeSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    mBluzManager.setVolume(mSeekBarVolume);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (!fromUser) {
                        // We're not interested in programmatically generated
                        // changes to
                        // the progress bar's position.
                        return;
                    }

                    mSeekBarVolume = progress;
                }
            });
        } else {
            Utils.setAlphaForView(mGlobalInfoLayout, 0.5f);
            mVolumeSeekBar.setProgress(0);
            mVolumeSeekBar.setOnSeekBarChangeListener(null);
            mVolumeSeekBar.setEnabled(false);
            mSoundImageButton.setImageResource(R.drawable.selector_muteon_button);
            mSoundImageButton.setEnabled(false);
            mSoundImageButton.setOnClickListener(null);
            mBatteryImageView.setImageLevel(0);
            mBatteryImageView.setImageResource(R.drawable.battery);
            mBatteryImageView.setImageState(STATE_NONE, true);
        }
    }


    public void setMode(int mode) {
        if (mode == mMode) {
            toggle();
            if (mMode == FuncMode.ALARM) {
                //alarm clock should on top of other fragment, so need to clean fragments like connection and ota
                addFragmentToStack(SlideoutMenuFragment.FRAGMENT_TAG_ALARM);
            } else {
                //return to same mode, should clean up other top fragments like alarm ota connection
                clearFragmentBackstack();
            }
        } else {
            mBluzManager.setMode(mode);
        }
    }

    // these fragments should on top of other fragments
    public void addFragmentToStack(String tag) {
//        Log.i(TAG,getSupportFragmentManager().getBackStackEntryCount()+"");
        Fragment fragment;
        if (getSupportFragmentManager().findFragmentByTag(tag) != null) {
            Log.i(TAG, "old One");
            showContent();
        } else if (tag.contains(SlideoutMenuFragment.FRAGMENT_TAG_CONNECTION)) {
            clearFragmentBackstack();
            Log.i(TAG, "new One");
            fragment = new ConnectionFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.main_fragment, fragment, tag);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.addToBackStack(tag);
            ft.commit();
            showContent();
        } else if (tag.contains(SlideoutMenuFragment.FRAGMENT_TAG_OTA)) {
            clearFragmentBackstack();
            Log.i(TAG, "new One");
            fragment = new OTAFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.main_fragment, fragment, tag);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.addToBackStack(tag);
            ft.commit();
            showContent();
        } else if (tag.contains(SlideoutMenuFragment.FRAGMENT_TAG_ALARM)) {
            clearFragmentBackstack();
            Log.i(TAG, "new One");
            fragment = new AlarmClockFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.main_fragment, fragment, tag);
            ft.addToBackStack(tag);
            ft.commit();
            showContent();
        }
    }

    public void addFragmentToStack(Fragment fragment, String tag) {

        if (tag.contains(SlideoutMenuFragment.FRAGMENT_TAG_ALARM_SETTING)) {
            clearFragmentBackstack();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.main_fragment, fragment, tag);
            ft.addToBackStack(tag);
            ft.commit();
            showContent();
        }
    }

    private void clearFragmentBackstack() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (mForeground) {
            while (count > 0) {
                Log.i(TAG, "stack count" + count + "");
                //http://stackoverflow.com/questions/7575921/illegalstateexception-can-not-perform-this-action-after-onsaveinstancestate-wit
                try {
                    getSupportFragmentManager().popBackStack();
                } catch (IllegalStateException ignored) {
                    // There's no way to avoid getting this if saveInstanceState has already been called.
                }
                count--;
            }
        }

    }

    private void initFragment() {
        Log.i(TAG, "initFragment");
        getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
        mFragmentTag = SlideoutMenuFragment.FRAGMENT_TAG_CONNECTION;
        initFragment(new ConnectionFragment(), false, 0);
    }

    private void initFragment(Fragment fragment, boolean specialCatalogSelected, int selectedMode) {
        clearFragmentBackstack();
        if (mForeground) {
            mSlideoutMenuFragment.setMenuSelected(mFragmentTag, specialCatalogSelected, selectedMode);
            replaceFragment(fragment, mFragmentTag);
            mComingFragment = null;
        } else {
            mComingFragment = fragment;
        }
    }

    public void replaceFragment(Fragment fragment, String tag) {
        if (fragment != null) {

            if (getSupportFragmentManager().findFragmentByTag(SlideoutMenuFragment.FRAGMENT_TAG_A2DP) != null
                    && (tag.equals(SlideoutMenuFragment.FRAGMENT_TAG_ALARM) || tag.equals(SlideoutMenuFragment.FRAGMENT_TAG_ALARM_SETTING))) {

                if (fragment instanceof AlarmClockSettingFragment) {
                    // alarm clock settings fragment has bundle
                    addFragmentToStack(fragment, tag);
                } else {
                    addFragmentToStack(tag);
                }
            } else {
                // if (fragment != mA2DPMusicFragment && !tag.equals(SlideoutMenuFragment.FRAGMENT_TAG_ALARM)) {
                // if (mA2DPMusicFragment != null) {
                // mA2DPMusicFragment.pause();
                // }
                // }
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.main_fragment, fragment, tag);
                ft.commit();
                showContent();
            }


        }
    }

    private void setBluzDeviceDisconnected() {
        Log.i(TAG, "setBluzDeviceDisconnected");
        hideUSBPlugDialog();
        hideDisconnectDialog();
        hideOtaRebootDialog();
        stopMusicPlayer();
        toggleGlobalInfo(false);
        releaseOta();
        releaseManager();
        initFragment();
    }

    public String getFragmentTag() {
        return mFragmentTag;
    }

    public IBluzManager getIBluzManager() {
        return mBluzManager;
    }

    public BluzManager getBluzManager() {
        return mBluzManager;
    }

    public IGlobalManager getIGlobalManager() {
        return mBluzManager;
    }

    public void showSnoozeSettingDialog() {
        initSnoozeDialogView();
        AlertDialog dialog = new Builder(this).setTitle(R.string.menu_item_snooze).setView(mSnoozeDialogView)
                .setPositiveButton(R.string.action_submit, new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mAlarmManager != null) {
                            mAlarmManager.setSnoozeMessage(mSnoozeTime, mSnoozeCount, mSnoozeOvertime);
                        }
                        dialog.cancel();
                    }
                }).create();
        dialog.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
            }
        });
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = Utils.screenSize(mContext).x;
        // params.height = 600 ;
        dialog.getWindow().setAttributes(params);
        dialog.show();
    }

    private void initSnoozeDialogView() {
        LayoutInflater factory = LayoutInflater.from(BrowserActivity.this);
        mSnoozeDialogView = factory.inflate(R.layout.dialog_snoozesetting, null);
        final Spinner mTimeSpinner = (Spinner) mSnoozeDialogView.findViewById(R.id.time_Spinner);
        final Spinner mCountSpinner = (Spinner) mSnoozeDialogView.findViewById(R.id.count_Spinner);
        final Spinner mOverTimeSpinner = (Spinner) mSnoozeDialogView.findViewById(R.id.overtime_Spinner);
        String[] mTime = new String[31];
        String[] mCount = new String[11];
        String[] mOverTime = new String[31];
        for (int i = 0; i <= 30; i++) {
            if (i == 0) {
                mTime[i] = getString(R.string.snooze_time_none);
                mCount[i] = getString(R.string.snooze_count_infinite);
                mOverTime[i] = getString(R.string.snooze_overtime_always);
            } else {
                if (i < 11) {
                    mCount[i] = i + getString(R.string.snooze_times);
                }
                mTime[i] = i + getString(R.string.snooze_minute);
                mOverTime[i] = i + getString(R.string.snooze_minute);
            }
        }

        ArrayAdapter<String> timeAdapter = new ArrayAdapter<String>(mContext, R.layout.snooze_spinner_item, mTime);
        ArrayAdapter<String> countAdapter = new ArrayAdapter<String>(mContext, R.layout.snooze_spinner_item, mCount);
        ArrayAdapter<String> overtimeAdapter = new ArrayAdapter<String>(mContext, R.layout.snooze_spinner_item, mOverTime);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        overtimeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mTimeSpinner.setAdapter(timeAdapter);
        mCountSpinner.setAdapter(countAdapter);
        mOverTimeSpinner.setAdapter(overtimeAdapter);
        mTimeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSnoozeTime = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub

            }
        });
        mCountSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSnoozeCount = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub

            }
        });
        mOverTimeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSnoozeOvertime = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub

            }
        });
        if (mAlarmManager != null) {
            mAlarmManager.getSnoozeMessage(new OnSnoozeMessageReadyListener() {

                @Override
                public void onReady(int snooze_time, int snooze_count, int overtime) {
                    mTimeSpinner.setSelection(snooze_time);
                    mCountSpinner.setSelection(snooze_count);
                    mOverTimeSpinner.setSelection(overtime);
                }
            });
        }
    }

    public void showEQSettingDialog() {
        initEQDialogView();
        AlertDialog dialog = new Builder(this).setTitle(R.string.menu_title_sound).setView(mEQDialogView)
                .setPositiveButton(R.string.action_submit, new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).create();
        dialog.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                Preferences.setPreferences(mContext, Preferences.KEY_LINEIN_EQUALIZER_TYPE, mEQMode);
                if (mEQMode == EQMode.USER) {
                    int value = mEqBandLevel.get(mEQMode - 1)[0];
                    Preferences.setPreferences(mContext, Preferences.KEY_LINEIN_EQ_FREQUENCY_80, value);
                    value = mEqBandLevel.get(mEQMode - 1)[1];
                    Preferences.setPreferences(mContext, Preferences.KEY_LINEIN_EQ_FREQUENCY_200, value);
                    value = mEqBandLevel.get(mEQMode - 1)[2];
                    Preferences.setPreferences(mContext, Preferences.KEY_LINEIN_EQ_FREQUENCY_500, value);
                    value = mEqBandLevel.get(mEQMode - 1)[3];
                    Preferences.setPreferences(mContext, Preferences.KEY_LINEIN_EQ_FREQUENCY_1K, value);
                    value = mEqBandLevel.get(mEQMode - 1)[4];
                    Preferences.setPreferences(mContext, Preferences.KEY_LINEIN_EQ_FREQUENCY_4K, value);
                    value = mEqBandLevel.get(mEQMode - 1)[5];
                    Preferences.setPreferences(mContext, Preferences.KEY_LINEIN_EQ_FREQUENCY_8K, value);
                    value = mEqBandLevel.get(mEQMode - 1)[6];
                    Preferences.setPreferences(mContext, Preferences.KEY_LINEIN_EQ_FREQUENCY_16K, value);
                }
            }
        });
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = Utils.screenSize(mContext).x;
        // params.height = 600 ;
        dialog.getWindow().setAttributes(params);
        dialog.show();
    }

    public void showDAEDialog() {
        int daeOptionValue = Integer.valueOf(Preferences.getPreferences(mContext, Preferences.KEY_DAE_OPTION, 0).toString());
        boolean[] daeOption = LittleEndian.getBooleanArray((byte) daeOptionValue);
        // Bit0：VBASS Bit1：TREBLE Bit2：VIRTUALSURROUND Bit3：SpkCompensation Bit4：PEQ
        //   Bit5：MDRC Bit6：DAE_Enhancemen Bit7：DAE_Weaken
        //TODO 8DAE
        for (int i = 0; i < mDaeChoose.length; i++) {
            mDaeChoose[i] = daeOption[daeOption.length - 1 - i];
        }

        mDaeshow[0] = mDaeChoose[0];
        mDaeshow[1] = mDaeChoose[1];
        mDaeshow[2] = mDaeChoose[4];
        mDaeshow[3] = mDaeChoose[5];
        mDaeshow[4] = mDaeChoose[6];
        mDaeshow[5] = mDaeChoose[7];

        AlertDialog daeDialog = new Builder(this).setTitle(R.string.menu_item_daesound)
                .setMultiChoiceItems(R.array.array_dae_type, mDaeshow, new OnMultiChoiceClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        switch (which) {
//						case 0:
//							mDaeChoose[0] = isChecked;
//							break;
//						case 1:
//							mDaeChoose[1] = isChecked;
//							break;
//						case 2:
//							mDaeChoose[2] = isChecked;
//							break;
//						case 3:
//							mDaeChoose[3] = isChecked;
//							break;
//						case 4:
//							mDaeChoose[4] = isChecked;
//							break;
//						case 5:
//							mDaeChoose[5] = isChecked;
//							break;
//						case 6:
//							mDaeChoose[6] = isChecked;
//							break;
//						case 7:
//							mDaeChoose[7] = isChecked;
//							break;
                            case 0:
                                mDaeChoose[0] = isChecked;
                                break;
                            case 1:
                                mDaeChoose[1] = isChecked;
                                break;
                            case 2:
                                mDaeChoose[4] = isChecked;
                                break;
                            case 3:
                                mDaeChoose[5] = isChecked;
                                break;
                            case 4:
                                mDaeChoose[6] = isChecked;
                                break;
                            case 5:
                                mDaeChoose[7] = isChecked;
                                break;


                            default:
                                mDaeChoose = new boolean[]{false, false, false, false, false, false, false, false};
                                break;
                        }
                    }
                }).setPositiveButton(R.string.action_submit, new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        mDaeChoose[2] = false;
                        mDaeChoose[3] = false;
                        int option = LittleEndian.BitToByte(mDaeChoose);
                        Preferences.setPreferences(mContext, Preferences.KEY_DAE_OPTION, option);
                        mBluzManager.setDAEOption((byte) option);
                        dialog.cancel();
                    }
                }).create();
        daeDialog.show();
    }

    private void initEQDialogView() {
        LayoutInflater factory = LayoutInflater.from(BrowserActivity.this);
        mEQDialogView = factory.inflate(R.layout.dialog_eqsetting, null);
        mEqTypeSpinner = (Spinner) mEQDialogView.findViewById(R.id.eqTypeSpinner);
        mEqSettingLayout = mEQDialogView.findViewById(R.id.eqSettingLayout);
        mEqSeekBar = new VerticalSeekBar[7];
        mEqSeekBar[0] = (VerticalSeekBar) mEQDialogView.findViewById(R.id.frequency80HzBar);
        mEqSeekBar[1] = (VerticalSeekBar) mEQDialogView.findViewById(R.id.frequency200HzBar);
        mEqSeekBar[2] = (VerticalSeekBar) mEQDialogView.findViewById(R.id.frequency500HzBar);
        mEqSeekBar[3] = (VerticalSeekBar) mEQDialogView.findViewById(R.id.frequency1KHzBar);
        mEqSeekBar[4] = (VerticalSeekBar) mEQDialogView.findViewById(R.id.frequency4KHzBar);
        mEqSeekBar[5] = (VerticalSeekBar) mEQDialogView.findViewById(R.id.frequency8KHzBar);
        mEqSeekBar[6] = (VerticalSeekBar) mEQDialogView.findViewById(R.id.frequency16KHzBar);
        for (int i = 0; i < mEqSeekBar.length; i++) {
            mEqSeekBar[i].setOnSeekBarChangeListener(mSeekBarChangeListener);
        }
        mEqBandLevel.clear();
        // int[] normalLevel =
        // getResources().getIntArray(R.array.array_eq_normal);
        // mEqBandLevel.add(normalLevel);
        int[] jazzLevel = getResources().getIntArray(R.array.array_eq_jazz);
        mEqBandLevel.add(jazzLevel);
        int[] popLevel = getResources().getIntArray(R.array.array_eq_pop);
        mEqBandLevel.add(popLevel);
        int[] classicLevel = getResources().getIntArray(R.array.array_eq_classic);
        mEqBandLevel.add(classicLevel);
        int[] softLevel = getResources().getIntArray(R.array.array_eq_soft);
        mEqBandLevel.add(softLevel);
        int[] dbbLevel = getResources().getIntArray(R.array.array_eq_dbb);
        mEqBandLevel.add(dbbLevel);
        int[] rockLevel = getResources().getIntArray(R.array.array_eq_rock);
        mEqBandLevel.add(rockLevel);

        int[] userLevel = new int[7];
        userLevel[0] = Integer.valueOf(Preferences.getPreferences(mContext, Preferences.KEY_LINEIN_EQ_FREQUENCY_80, 0).toString());
        userLevel[1] = Integer.valueOf(Preferences.getPreferences(mContext, Preferences.KEY_LINEIN_EQ_FREQUENCY_200, 0).toString());
        userLevel[2] = Integer.valueOf(Preferences.getPreferences(mContext, Preferences.KEY_LINEIN_EQ_FREQUENCY_500, 0).toString());
        userLevel[3] = Integer.valueOf(Preferences.getPreferences(mContext, Preferences.KEY_LINEIN_EQ_FREQUENCY_1K, 0).toString());
        userLevel[4] = Integer.valueOf(Preferences.getPreferences(mContext, Preferences.KEY_LINEIN_EQ_FREQUENCY_4K, 0).toString());
        userLevel[5] = Integer.valueOf(Preferences.getPreferences(mContext, Preferences.KEY_LINEIN_EQ_FREQUENCY_8K, 0).toString());
        userLevel[6] = Integer.valueOf(Preferences.getPreferences(mContext, Preferences.KEY_LINEIN_EQ_FREQUENCY_16K, 0).toString());
        mEqBandLevel.add(userLevel);

        mEqTypes = getResources().getStringArray(R.array.array_eq_type);
        ArrayAdapter<String> eqTypeAdapter = new ArrayAdapter<String>(mContext, R.layout.eq_spinner_item, mEqTypes);
        eqTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mEqTypeSpinner.setAdapter(eqTypeAdapter);
        mEqTypeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mEQMode = position + 1;// position
                TextView tv = (TextView) view;
                tv.setTextColor(Color.BLACK);
                // mBluzManager.setEQMode(mEQMode);
                if (mEQMode == EQMode.USER) {
                    mBluzManager.setEQParam(mEqBandLevel.get(mEQMode - 1));
                } else {
                    mBluzManager.setDAEEQMode(mEQMode);
                }
                equalizerUpdateDisplay();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });
        mEQMode = Integer.valueOf(Preferences.getPreferences(mContext, Preferences.KEY_LINEIN_EQUALIZER_TYPE, 0).toString());
        mEqTypeSpinner.setSelection(mEQMode - 1);// mEQMode
    }

    private void equalizerUpdateDisplay() {
        int[] level = mEqBandLevel.get(mEQMode - 1);// mEQMode

        for (int i = 0; i < level.length; i++) {
            mEqSeekBar[i].setProgressAndThumb(level[i] + 12);
        }

        if (mEQMode == EQMode.USER) {
            Utils.setAlphaForView(mEqSettingLayout, 1.0f);
            for (int i = 0; i < mEqSeekBar.length; i++) {
                mEqSeekBar[i].setEnabled(true);
            }
        } else {
            Utils.setAlphaForView(mEqSettingLayout, 0.5f);
            for (int i = 0; i < mEqSeekBar.length; i++) {
                mEqSeekBar[i].setEnabled(false);
            }
        }
    }


    public void stopBackgroundMusic() {
        int focus = mAudioManager.requestAudioFocus(this,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        Log.v(TAG, "focus:" + focus);
        if (focus != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.i(TAG, "Audio focus request failed!");
        }
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            setMediaButtonEvent();
        } else {
            mBluetoothBoxControl = new ComponentName(getPackageName(),
                    BluetoothBoxControl.class.getName());
            mAudioManager
                    .registerMediaButtonEventReceiver(mBluetoothBoxControl);
        }

    }

    private void setMediaButtonEvent() {
        if (session == null) {
            session = new MediaSession(getApplicationContext(), TAG);
        }
        if (session == null) {
            Log.e(TAG, "initMediaSession: _mediaSession = null");
            return;
        }

        session.setCallback(new MediaSession.Callback() {

            @Override
            public boolean onMediaButtonEvent(Intent mediaButtonIntent) {
                // TODO Auto-generated method stub

                if (Intent.ACTION_MEDIA_BUTTON.equals(mediaButtonIntent.getAction())) {
                    final KeyEvent ke = (KeyEvent) mediaButtonIntent.getExtras().get(Intent.EXTRA_KEY_EVENT);
                    Log.i(TAG, "GOT EVENT " + ke);
                    if (ke != null && ke.getAction() == KeyEvent.ACTION_DOWN) {
                        A2DPMusicFragment fragment = (A2DPMusicFragment) getSupportFragmentManager()
                                .findFragmentByTag(
                                        SlideoutMenuFragment.FRAGMENT_TAG_A2DP);
                        switch (ke.getKeyCode()) {
                            case KeyEvent.KEYCODE_MEDIA_PLAY:
                                Log.d(TAG, "play button received");
                                if (fragment != null) {
                                    fragment.doPlay();
                                    updatePlaybackState(true);
                                }
                                break;
                            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                                Log.d(TAG, "pause button received");
                                if (fragment != null) {
                                    fragment.pause();
                                    updatePlaybackState(false);
                                }
                                break;

                            case KeyEvent.KEYCODE_MEDIA_NEXT:
                                Log.d(TAG, "next button received");
                                if (fragment != null) {
                                    fragment.playNextMusic();
                                }
                                break;
                            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                                Log.d(TAG, "previous button received");
                                if (fragment != null) {
                                    fragment.playPreviousMusic();
                                }

                                break;

                        }
                    }
                }
                return super.onMediaButtonEvent(mediaButtonIntent);
            }

            @Override
            public void onCommand(String command, Bundle args, ResultReceiver cb) {
                // TODO Auto-generated method stub
                super.onCommand(command, args, cb);
            }

            public void onStop() {
                //Log.d(TAG, "onStop called (media button pressed)");
                super.onStop();
            }

            @Override
            public void onRewind() {
                // TODO Auto-generated method stubfplay
                super.onRewind();
            }
        });

        session.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS
                | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        session.setActive(true);

        Intent intent = new Intent(mContext, BrowserActivity.class);
        PendingIntent pi = PendingIntent.getActivity(mContext,
                99 /* request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        session.setSessionActivity(pi);

    }

    public void updateMediaCenterInfo(String title, String artist) {
        if (session == null) return;
        MediaMetadata.Builder metadataBuilder = new MediaMetadata.Builder();
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_TITLE, title);
        metadataBuilder.putString(MediaMetadata.METADATA_KEY_ARTIST, artist);
        session.setMetadata(metadataBuilder.build());
    }

    /*
     * update mediaCenter state
     */
    private void updatePlaybackState(boolean isPlaying) {
        PlaybackState.Builder stateBuilder = new PlaybackState.Builder()
                .setActions(PlaybackState.ACTION_PLAY
                        | PlaybackState.ACTION_PLAY_PAUSE
                        | PlaybackState.ACTION_PLAY_FROM_MEDIA_ID
                        | PlaybackState.ACTION_PAUSE
                        | PlaybackState.ACTION_SKIP_TO_NEXT
                        | PlaybackState.ACTION_SKIP_TO_PREVIOUS);
        if (isPlaying) {
            stateBuilder.setState(PlaybackState.STATE_PLAYING,
                    PlaybackState.PLAYBACK_POSITION_UNKNOWN,
                    SystemClock.elapsedRealtime());
        } else {
            stateBuilder.setState(PlaybackState.STATE_PAUSED,
                    PlaybackState.PLAYBACK_POSITION_UNKNOWN,
                    SystemClock.elapsedRealtime());
        }
        session.setPlaybackState(stateBuilder.build());
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        Log.v(TAG, " onAudioFocusChange: " + focusChange);
        A2DPMusicFragment fragment = (A2DPMusicFragment) getSupportFragmentManager().findFragmentByTag(SlideoutMenuFragment.FRAGMENT_TAG_A2DP);
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
                Log.v(TAG, "AudioFocus: received AUDIOFOCUS_LOSS");
                if (fragment != null) {
                    if (fragment.isPlaying()) {
                        mPausedByTransientLossOfFocus = false;
                    }
                    fragment.pause();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                Log.v(TAG, "AudioFocus: received AUDIOFOCUS_LOSS_TRANSIENT");
                if (fragment != null) {
                    if (fragment.isPlaying()) {
                        mPausedByTransientLossOfFocus = true;
                    }
                    fragment.pause();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (fragment != null) {
                    // fragment.setFadeUpToDown(true);
                    if (fragment.isPlaying()) {
                        mPausedByTransientLossOfFocus = true;
                    }
                    fragment.pause();
                }

                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                Log.v(TAG, "AudioFocus: received AUDIOFOCUS_GAIN");
                if (fragment != null) {
                    if (!fragment.isPlaying() && mPausedByTransientLossOfFocus) {
                        mPausedByTransientLossOfFocus = false;
                        fragment.doPauseResumeOnlyForActons();
                    }
                    // else {
                    // fragment.setFadeUpToDown(false);
                    // }
                }
                break;
            default:
                Log.e(TAG, "Unknown audio focus change code");
        }
    }

    private void showNotSupportedDialog() {
        Builder builder = new Builder(mContext);
        builder.setTitle(R.string.dialog_title_warning);
        builder.setMessage(R.string.notice_bluetooth_not_supported);

        builder.setPositiveButton(R.string.ok, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                System.exit(0);
            }
        });
        builder.create().show();
    }

    private void stopMusicPlayer() {
        A2DPMusicFragment fragment = (A2DPMusicFragment) getSupportFragmentManager().findFragmentByTag(SlideoutMenuFragment.FRAGMENT_TAG_A2DP);
        if (fragment != null) {
            fragment.release();
        }
    }

    private void registerExternalStorageListener() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        filter.addDataScheme("file");
        registerReceiver(mUnmountReceiver, filter);
    }

    public int getCurrentMode() {
        return mMode;
    }

    public void showDisconnectDialog() {
        Builder builder = new Builder(mContext);
        builder.setTitle(R.string.dialog_title_warning);
        builder.setMessage(getResources().getString(R.string.dialog_message_disconncect));
        builder.setPositiveButton(R.string.ok, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mBluzConnector.disconnect(mBluzConnector.getConnectedDevice());
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.action_cancel, new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        mDisconnectDialog = builder.create();
        mDisconnectDialog.show();
    }

    public void hideDisconnectDialog() {
        if (mDisconnectDialog != null && mDisconnectDialog.isShowing()) {
            mDisconnectDialog.dismiss();
        }
    }

    public void hideUSBPlugDialog() {
        if (mUSBPlugDialog != null && mUSBPlugDialog.isShowing()) {
            mUSBPlugDialog.dismiss();
        }
    }

    public void setOtaRebootDialog(AlertDialog alertDialog) {
        mRebootDialog = alertDialog;
    }

    public void hideOtaRebootDialog() {
        if (mRebootDialog != null && mRebootDialog.isShowing()) {
            mRebootDialog.dismiss();
        }
    }

    public int getCurrentDAEMode() {
        return Integer.valueOf(Preferences.getPreferences(mContext, Preferences.KEY_DAE_MODE, 0).toString());
    }

    public void menuItemSelected(Menu menu, int itemIdSelected) {
        switch (itemIdSelected) {
            case R.id.nodigitalsoundeffect:
                if ( getCurrentDAEMode()!=DAEMode.NONE) {
                mBluzManager.setDAENoDigitalSound();
                 }
                // mBluzManager.setEQMode(0);
                break;
            // case R.id.eqsoundeffect:
            // // mBluzManager.setDAEEQMode(mEQMode);
            // showEQSettingDialog();
            // break;
            case R.id.daesoundeffect:
                showDAEDialog();
                break;
            case R.id.snoozesetting:
                showSnoozeSettingDialog();
                break;
            case android.R.id.home:
                if (getSupportActionBar().getTitle().equals(getString(R.string.app_name))) {
                    toggle();
                }
                break;
        }
        if (menu != null) {
            MenuItem noneItem, eqItem, daeItem;
            noneItem = menu.findItem(R.id.nodigitalsoundeffect);
            // eqItem = menu.findItem(R.id.eqsoundeffect);
            daeItem = menu.findItem(R.id.daesoundeffect);
            noneItem.setCheckable(false);
            // eqItem.setCheckable(false);
            daeItem.setCheckable(false);
            int currentDAEMode = getCurrentDAEMode();
            switch (currentDAEMode) {
                case DAEMode.NONE:
                    noneItem.setCheckable(true);
                    noneItem.setChecked(true);
                    break;
                // case DAEMode.EQ:
                // eqItem.setCheckable(true);
                // eqItem.setChecked(true);
                // break;
                case DAEMode.DAE:
                    daeItem.setCheckable(true);
                    daeItem.setChecked(true);
                    break;
                default:
                    break;
            }
        }
    }

    public void showAlarmDialog(AlertDialog adg) {
        this.mAlarmDialog = adg;
        if (mAlarmDialog != null) {
            mAlarmDialog.show();
        }
    }

    public void dismissAlarmDialog() {
        if (mAlarmDialog != null && mAlarmDialog.isShowing()) {
            mAlarmDialog.dismiss();
        }
    }

    /*
    * OTA start
    * */


    public void initOtaUpdater() {
        Log.i(TAG, "initOtaUpdater");
        if (mOtaUpdater == null) {
            Log.i(TAG, "getOtaUpdater: new one");
            mOtaUpdater = new OTAUpdater(BrowserActivity.this, this);
            mBluzManager.setOnCustomDataListener(new BluzManagerData.OnCustomDataListener() {
                @Override
                public void onReady(byte[] obj) {
                    mOtaUpdater.onReceive(obj);
                }
            });
        }
    }

    public void saveLastUpdateAddress() {
        Preferences.setPreferences(mContext, Preferences.KEY_OTA_LAST_UPDATE_DEVICE_ADDRESS, mBluzConnector.getConnectedDevice().getAddress());
    }

    public String getConnectedDeviceAddress() {
        return mBluzConnector.getConnectedDevice().getAddress();
    }


    public String getLastUpdateDeviceAdress() {
        return (String) Preferences.getPreferences(mContext, Preferences.KEY_OTA_LAST_UPDATE_DEVICE_ADDRESS, "-1");
    }

    public void getFirmWareVersion(OnCheckFirmwareListener onCheckFirmwareListener) {
        if (mOtaUpdater != null) {
            mOtaUpdater.getFirmWareVersion(onCheckFirmwareListener);
        }

    }

    public void startUpdate(Update update) {
        mOtaUpdater.startUpdate(update);
    }

    public void stopUpdate() {
        mOtaUpdater.suspendUpdate();
    }

    public void confirmUpdateAndReboot() {
        mOtaUpdater.confirmUpdateAndReboot();
    }

    public void resetMachine() {
        mOtaUpdater.resetMachineData();
    }

    public void updateVram(byte[] data) {
        mOtaUpdater.updateVram(data);
    }

    //OTA SendData
    @Override
    public void onSend(byte[] sendData) {
//        Log.i(TAG, "onSend");
        Log.i(TAG, "onSend " + Utils.byte2HexStr(sendData, sendData.length));
        if (mBluzManager != null) {
            mBluzManager.sendCustomData(sendData);
        }

    }

    private void releaseOta() {
        Log.i(TAG, "releaseOta");
        if (mOtaUpdater != null) {
            mOtaUpdater.release();
            mOtaUpdater = null;
        }
    }

    /*
    * OTA end
    * */
    public IBluzDevice getBluzConnector() {
        if (mBluzConnector == null) {
            mBluzConnector = BluzDeviceFactory.getDevice(this);
        }

        return mBluzConnector;
    }
}
