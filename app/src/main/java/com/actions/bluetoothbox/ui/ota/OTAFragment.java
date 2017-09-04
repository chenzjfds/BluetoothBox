package com.actions.bluetoothbox.ui.ota;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actions.bluetoothbox.R;
import com.actions.bluetoothbox.ui.BrowserActivity;
import com.actions.bluetoothbox.util.Utils;
import com.actions.bluetoothbox.widget.NumberPickerDialog;
import com.actions.bluetoothbox.widget.NumberProgressBar;
import com.actions.bluetoothbox.widget.fileselector.FileConfig;
import com.actions.bluetoothbox.widget.fileselector.FileFilter;
import com.actions.bluetoothbox.widget.fileselector.FileSelectorAlertDialog;
import com.actions.bluetoothbox.widget.fileselector.FileType;
import com.actions.ibluz.ota.updater.FilePartGenerator;
import com.actions.ibluz.ota.updater.OTAUpdater;
import com.actions.ibluz.ota.updater.OnCheckFirmwareListener;
import com.actions.ibluz.ota.updater.OnUpdateListener;
import com.actions.ibluz.ota.updater.Update;
import com.actions.ibluz.ota.updater.UpdatePartConfig;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.actions.ibluz.ota.updater.Update.NORMAL_PART;


public class OTAFragment extends SherlockFragment {

    private static final String PREFS_OTA_FILE_PATHS = "prefs_ota_file_paths";
    private static final String PREFS__OTA = "prefs_ota";
    private static final String KEY_SP_IS_UPDATING = "key_sp_is_updating";
    private static final String KEY_SP_IS_UPDATE_PAUSED = "key_sp_is_update_paused";
    private static final String KEY_SP_UPDATE_CURRENT_PROGRESS = "key_sp_update_current_progress";
    private static final String KEY_SP_UPDATE_MAX_PROGRESS = "key_sp_update_total_progress";
    private static final String KEY_SP_FIRMWARE_PATH = "key_sp_firmware_path";
    private static final String KEY_SP_FILE_PATH_KEY = "key_sp_file_path_key";
    private final String TAG = OTAFragment.class.getSimpleName();
    private Button btnUpgrade;
    private Button btnPause;
    private Button btnContinue;
    private Button btnCancel;
    private Button btnVram;
    private TextView tvFileSelected;
    private TextView tvFirmwareSelected;
    private Button btnSelectFw;
    private Button btnSelectFile;
    private TextView tvMachineVersion;
    private TextView tvRemainSize;
    private TextView tvRemainTime;
    private TextView tvProgressVersion;
    private RelativeLayout mUpdateInfoWrapper;
    private LinearLayout mOtaInfoWrapper;
    private NumberProgressBar pbProgress;
    private BrowserActivity mActivity;
    private String mFirmwarePath;
    private String mMachineVersionName;


    private SharedPreferences mSharedPreferences;

    private AlertDialog mResetDialog;
    private AlertDialog mCancelDialog;
    private HashMap<Byte, String> mFilesMap = new HashMap<>();
    private LinearLayout mSelectContainer;
    private AlertDialog mRebootDialog;
    private String mFileVersion = "";
    private String mFileModuleNum = "";
    private int mCurrentProgress;
    private int mMaxProgress;
    private UpdatePartConfig mUpdatePartConfig;
    private String mMachineModuleNum;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        mActivity = (BrowserActivity) getActivity();
        mSharedPreferences = mActivity.getSharedPreferences(PREFS__OTA, Context.MODE_PRIVATE);
        View view = inflater.inflate(R.layout.fragment_ota, container, false);
        initView(view);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        initFilePartConfig();
        if (getIsUpdatingAndSameDevice()) {

            showUpdateStatusInfo();

        } else {
            showOnlySelectContainer();
        }

        if (getIsUpdatingAndSameDevice()) {
            mFirmwarePath = getFirwwarePath();
            mFilesMap = getFilePathsMap();
        }
        checkFWVersion();


    }

    private void initView(View view) {
        mOtaInfoWrapper = (LinearLayout) view.findViewById(R.id.ota_info_wrapper);
        mUpdateInfoWrapper = (RelativeLayout) view.findViewById(R.id.update_info_wrapper);
        btnSelectFw = (Button) view.findViewById(R.id.btn_select_fw);
        btnSelectFile = (Button) view.findViewById(R.id.btn_select_file);
        btnUpgrade = (Button) view.findViewById(R.id.btn_upgrade);
        btnPause = ((Button) view.findViewById(R.id.btn_pause_update));
        btnContinue = ((Button) view.findViewById(R.id.btn_continue_update));
        btnCancel = ((Button) view.findViewById(R.id.btn_cancel_update));
        btnVram = (Button) view.findViewById(R.id.btn_update_vram);
        tvMachineVersion = (TextView) view.findViewById(R.id.tv_machine_version);
        tvRemainSize = (TextView) view.findViewById(R.id.tv_size_remain);
        tvRemainTime = (TextView) view.findViewById(R.id.tv_time_remain);
        tvProgressVersion = (TextView) view.findViewById(R.id.tv_progress_version);
        pbProgress = (NumberProgressBar) view.findViewById(R.id.pb_ota);
        tvFileSelected = (TextView) view.findViewById(R.id.tv_file_selected);
        tvFirmwareSelected = (TextView) view.findViewById(R.id.tv_firmware_selected);
        mSelectContainer = (LinearLayout) view.findViewById(R.id.select_container);
        btnUpgrade.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                startUpdate();
                if (mFilesMap != null && mFilesMap.size() > 0) {
                    Log.i(TAG, "onClick: saveFilePathsMap");
                    saveFilePathsMap(mFilesMap);
                }
                if (mFirmwarePath != null && mFirmwarePath.length() > 0) {
                    saveFirmwarePath(mFirmwarePath);
                }
            }
        });

        btnPause.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setIsUpdatePaused(true);
                stopUpdate();
                setUpdateProgress(mCurrentProgress, mMaxProgress);
                showUpdateStatusInfo();

            }
        });

        btnContinue.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setIsUpdatePaused(false);
                startUpdate();
                showUpdateStatusInfo();
            }
        });

        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showCancelDialog();

            }
        });

        btnSelectFw.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                showFileSelectDialog();

            }
        });

        btnSelectFile.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showMultiFileSelectDialog();
            }
        });
        btnVram.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 2015/11/27
                mActivity.updateVram(new byte[]{});
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        hideRebootDialog();
        hideResetDialog();
        hideCancelDialog();
    }

    private void checkFWVersion() {
        mActivity.getFirmWareVersion(new OnCheckFirmwareListener() {
            @Override
            public void onCheckFWVersionSuccess(String versionName,String moduleNum) {
                mMachineVersionName = versionName;
                mMachineModuleNum = moduleNum;
                tvMachineVersion.setText(mActivity.getString(R.string.ota_update_machine_version, mMachineVersionName,moduleNum));
                if (getIsUpdatingAndSameDevice()) {
                    Log.i(TAG, "isUpdating" + getIsUpdatingAndSameDevice());
                    if (!getIsUpdatePaused()) {
                        startUpdate();
                    }

                } else {
                    showOnlySelectContainer();
                }
            }

            @Override
            public void onCheckFWVersionError(int erroCode) {

            }
        });
    }

    private void startUpdate() {

//        CustomPartManager customPartManager = new CustomPartManager(mFilesMap);
        Update.Builder builder = new Update.Builder();
        try {
            if (mFirmwarePath != null && mFirmwarePath.length() > 0) {
                builder.addFirmware(mFirmwarePath);
            }
            if (mFilesMap != null && mFilesMap.size() > 0) {
                for (Map.Entry<Byte, String> entry : mFilesMap.entrySet()) {
                    builder.addFile(entry.getKey(), entry.getValue());

                }
            }
            builder.listener(new OnUpdateListener() {
                @Override
                public void onUpdateProgress(int currentProgress, int maxProgress) {
                        if (mFileVersion.equals("普通分区")) {
                            tvProgressVersion.setText(R.string.ota_update_progress_updating_file);
                        } else {
                            tvProgressVersion.setText(mActivity.getString(R.string.ota_update_progress_updating_firmware, mFileVersion));
                        }
                        mCurrentProgress = currentProgress;
                        mMaxProgress = maxProgress;
                        pbProgress.setVisibility(View.VISIBLE);
                        tvRemainSize.setText(mActivity.getString(R.string.ota_update_progress_remain_size, currentProgress * OTAUpdater.PER_PACKAGE_SIZE / 1024, maxProgress * OTAUpdater.PER_PACKAGE_SIZE / 1024));
                        tvRemainTime.setText(mActivity.getString(R.string.ota_update_progress_remain_time, Utils.showTime((maxProgress - currentProgress) * 75)));
                        pbProgress.setMax(maxProgress);
                        pbProgress.setProgress(currentProgress);

                }

                @Override
                public void onUpdateError(int errorCode) {
                    switch (errorCode) {
                        case OTAUpdater.ERROR_FW_MISMATCH:
                            showResetDialog();
                            break;
                        case OTAUpdater.ERROR_FW_TOO_LARGE:
                            Toast.makeText(getActivity(), R.string.ota_error_too_large, Toast.LENGTH_SHORT).show();
                            setIsUpdating(false);
                            showOnlySelectContainer();
                            break;
                        case OTAUpdater.ERROR_VERSION_NAME_ILLEGAL:
                            Toast.makeText(getActivity(), R.string.ota_error_version_name_illegal, Toast.LENGTH_SHORT).show();
                            setIsUpdating(false);
                            showOnlySelectContainer();
                            break;
                        case OTAUpdater.ERROR_PART_CHEKSUM_FAIL:
                            showResetDialog();
                            break;
                        case OTAUpdater.ERROR_UNKNOWN:
                            Toast.makeText(mActivity, R.string.ota_error_unknown, Toast.LENGTH_SHORT).show();
                            setIsUpdating(false);
                            showOnlySelectContainer();
                            break;
                    }
                }

                @Override
                public void onUpdateComplete() {
                    pbProgress.setMax(100);
                    pbProgress.setProgress(100);
                    showRebootDialog();
                }
            });
            builder.partConfig(mUpdatePartConfig);
            Update update = builder.build();
            mFileVersion = update.getFileVersion();
            mFileModuleNum = update.getModuleNum();
            if (mFileVersion.equals(mMachineVersionName)) {
                throw new IllegalArgumentException(
                        mActivity.getString(R.string.ota_error_updating_same_version));
            }
            //must check
            if (!mFileModuleNum.equals(mMachineModuleNum)&&!mFileModuleNum.equals(NORMAL_PART)) {
                throw new IllegalArgumentException(
                        mActivity.getString(R.string.ota_error_updating_different_module));
            }
            mActivity.startUpdate(update);
            showUpdateStatusInfo();
            setIsUpdating(true);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            showOnlySelectContainer();
            setIsUpdating(false);
            mActivity.stopUpdate();
        }
    }

    private void stopUpdate() {
        mActivity.stopUpdate();
    }

    private void reboot() {
        mActivity.confirmUpdateAndReboot();
    }

    private void resetMachine() {
        mActivity.resetMachine();
    }


    private void initFilePartConfig(){
        try {
            InputStream inputStream = getActivity().getAssets().open("OTA.xml");
            mUpdatePartConfig = new UpdatePartConfig(inputStream);

        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showRebootDialog() {
        if (mRebootDialog == null) {
            Builder builder = new Builder(mActivity);
            builder.setIcon(android.R.drawable.ic_dialog_alert);// ic_dialog_alert_holo_light
            builder.setTitle(R.string.ota_reboot_dialog_title);
            builder.setMessage(R.string.ota_reboot_dialog_message);
            builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
//                    showOnlySelectContainer();
                    dialog.dismiss();
                }
            });
            builder.setPositiveButton(R.string.action_submit, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    setIsUpdating(false);
                    reboot();
                }
            });
            mRebootDialog = builder.create();
            mActivity.setOtaRebootDialog(mRebootDialog);
        }
        if (!mRebootDialog.isShowing()) {
            mRebootDialog.show();
        }

    }

    public void hideRebootDialog() {
        mActivity.hideOtaRebootDialog();
    }


    private void showResetDialog() {
        if (mResetDialog == null) {
            Builder builder = new Builder(mActivity);
            builder.setIcon(android.R.drawable.ic_dialog_alert);// ic_dialog_alert_holo_light
            builder.setTitle(R.string.ota_reset_dialog_title);
            builder.setMessage(R.string.ota_reset_dialog_message);
            builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    setIsUpdating(false);
                    showOnlySelectContainer();
                    dialog.dismiss();
                }
            });
            builder.setPositiveButton(R.string.action_submit, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    resetMachine();
                    setUpdateProgress(0, 100);
                    startUpdate();
                }
            });
            mResetDialog = builder.create();
        }
        if (!mResetDialog.isShowing()) {
            mResetDialog.show();
        }
    }

    private void hideResetDialog() {
        if (mResetDialog != null && mResetDialog.isShowing()) {
            mResetDialog.dismiss();
        }
    }

    private void showCancelDialog() {
        if (mCancelDialog == null) {
            Builder builder = new Builder(mActivity);
            builder.setIcon(android.R.drawable.ic_dialog_alert);// ic_dialog_alert_holo_light
            builder.setTitle(R.string.ota_cancel_dialog_title);
            builder.setMessage(R.string.ota_cancel_dialog_message);
            builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.setPositiveButton(R.string.action_submit, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    setIsUpdatePaused(false);
                    stopUpdate();
                    resetMachine();
                    setIsUpdating(false);
                    showOnlySelectContainer();
                }
            });
            mCancelDialog = builder.create();
        }
        if (!mCancelDialog.isShowing()) {
            mCancelDialog.show();
        }
    }

    private void hideCancelDialog() {
        if (mCancelDialog != null && mCancelDialog.isShowing()) {
            mCancelDialog.dismiss();
        }
    }

    private void showFileSelectDialog() {
        Log.i(TAG, "showFileSelectDialog: ");
        //通过参数实例化
        FileConfig fileConfig = new FileConfig.Builder()
                .startPath(Environment.getExternalStorageDirectory().getPath() + "/ActionsFirmware/")
                .selectType(FileType.FILE)
                .filterModel(FileFilter.FILTER_CUSTOM)
                .filter(new String[]{"OTA"})
                .multiModel(false)
                .build();
        FileSelectorAlertDialog fileSelectorAlertDialog = new FileSelectorAlertDialog(mActivity, fileConfig);
        fileSelectorAlertDialog.setOnSelectFinishListener(new FileSelectorAlertDialog.OnSelectFinishListener() {
            @Override
            public void selectFinish(ArrayList<String> paths) {
                for (String path : paths) {
                    Log.i(TAG, "selectFinish: " + path);
                }
                mFirmwarePath = paths.get(0);
                tvFirmwareSelected.setText(mActivity.getString(R.string.ota_firmware_selected, paths.get(0)));
                tvFileSelected.setText("");
                mFilesMap.clear();
                Log.i(TAG, "selectFinish FilesMap " + mFilesMap.size());
                showUpgradeButton();
            }
        });
        //在需要显示的时候调用show方法
        fileSelectorAlertDialog.show();

    }

    private void showMultiFileSelectDialog() {
        Log.d(TAG, "showMultiFileSelectDialog() called with: " + "");
        List<String> allValidFileTypes = mUpdatePartConfig.getAllValidFileTypes();
        //通过参数实例化
        FileConfig fileConfig = new FileConfig.Builder()
                .startPath(Environment.getExternalStorageDirectory().getPath() + "/ActionsFirmware/")
                .selectType(FileType.FILE)
                .filterModel(FileFilter.FILTER_CUSTOM)
                .filter(allValidFileTypes.toArray(new String[allValidFileTypes.size()]))
                .multiModel(true)
                .build();
        FileSelectorAlertDialog fileSelectorAlertDialog = new FileSelectorAlertDialog(mActivity, fileConfig);
        fileSelectorAlertDialog.setOnSelectFinishListener(new FileSelectorAlertDialog.OnSelectFinishListener() {
            @Override
            public void selectFinish(final ArrayList<String> paths) {


                if (paths.size() > 0) {
                    showNumberPickerDialog( paths);

                } else {
                    //Nothing selected
                    mFilesMap.clear();
                    tvFileSelected.setText(R.string.ota_no_file_selected);
                }
            }
        });
        //在需要显示的时候调用show方法
        fileSelectorAlertDialog.show();
    }


    private void showNumberPickerDialog( final List<String> paths) {
       NumberPickerDialog numberPickerDialog = new NumberPickerDialog(getActivity(),mUpdatePartConfig);
        numberPickerDialog.setNumberPickerListener(new NumberPickerDialog.NumberPickerListener() {
            @Override
            public void onPick(final int number) {
                Log.d(TAG, "onPick() called with: " + "number = [" + number + "]");
                final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setCancelable(false);
                progressDialog.setTitle(R.string.ota_generating_part);
                progressDialog.show();
                FilePartGenerator.generateFilePartWithConfig(number,mUpdatePartConfig,paths, getActivity(), new FilePartGenerator.FilePartGenerateListener() {
                    @Override
                    public void onGenerateSuccess(String path) {
                        progressDialog.dismiss();
                        mFilesMap.clear();
                        mFilesMap.put((byte) number, path);
                        tvFileSelected.setText(mActivity.getString(R.string.ota_file_selected, paths.get(0), number));
                        //can only update one type:file or firmware
                        mFirmwarePath = null;
                        tvFirmwareSelected.setText(null);
                    }

                    @Override
                    public void onGenerateFail(int errorCode) {
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), R.string.ota_error_generate_file_part, Toast.LENGTH_SHORT).show();
                        mFilesMap.clear();
                        tvFileSelected.setText(R.string.ota_no_file_selected);
                        showOnlySelectContainer();

                    }
                });
                showUpgradeButton();

            }

            @Override
            public void onCancel() {
                Log.d(TAG, "onCancel() called with: " + "");
                setIsUpdating(false);
                showOnlySelectContainer();
            }
        });
        numberPickerDialog.show();
    }

    private void saveFirmwarePath(String fwPath) {
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putString(KEY_SP_FIRMWARE_PATH, fwPath);
        mEditor.commit();
    }

    private String getFirwwarePath() {
        Log.i(TAG, mSharedPreferences.getString(KEY_SP_FIRMWARE_PATH, "no fw save"));
        return mSharedPreferences.getString(KEY_SP_FIRMWARE_PATH, null);
    }

    private void clearFirwwarePath() {
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.remove(KEY_SP_FIRMWARE_PATH);
        mEditor.commit();
    }

    public void saveFilePathsMap(HashMap<Byte, String> map) {
        SharedPreferences.Editor mEditor = mActivity.getSharedPreferences(PREFS_OTA_FILE_PATHS, Context.MODE_PRIVATE).edit();
        StringBuilder b = new StringBuilder();
        for (Map.Entry entry : map.entrySet()) {
            Log.i(TAG, "saveFilePathsMap: " + entry.getKey() + "");
            mEditor.putString(entry.getKey() + "", (String) entry.getValue());
            if (b.length() != 0) {
                b.append(",");
            }
            b.append(entry.getKey());
        }
        Log.i(TAG, "saveFilePathsMap: " + b.toString());
        mEditor.putString(KEY_SP_FILE_PATH_KEY, b.toString());
        mEditor.commit();
    }

    public HashMap<Byte, String> getFilePathsMap() {
        SharedPreferences sharedPreferences = mActivity.getSharedPreferences(PREFS_OTA_FILE_PATHS, Context.MODE_PRIVATE);
        Log.i(TAG, "getFilePathsMap: " + sharedPreferences.getString(KEY_SP_FILE_PATH_KEY, ""));
        String[] keys = sharedPreferences.getString(KEY_SP_FILE_PATH_KEY, "").split(",");
        HashMap<Byte, String> hashMap = new HashMap<>();
        if (keys.length > 0 && !keys[0].isEmpty()) {
            for (int i = 0; i < keys.length; i++) {
                Log.i(TAG, "getFilePathsMap: " + "key" + i + "  =" + keys[i]);
                hashMap.put(Byte.parseByte(keys[i]), sharedPreferences.getString(keys[i], "-1"));
            }
        }

        return hashMap;
    }

    public void clearFilePathsMap() {
        HashMap<Byte, String> map = getFilePathsMap();
        for (String path : map.values()) {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
        }
        SharedPreferences.Editor mEditor = mActivity.getSharedPreferences(PREFS_OTA_FILE_PATHS, Context.MODE_PRIVATE).edit();
        mEditor.clear();
        mEditor.commit();

    }


    private boolean getIsUpdatingAndSameDevice() {
        return getIsUpdating() && getIsSameDevice();
    }

    private boolean getIsUpdating() {
        return mSharedPreferences.getBoolean(KEY_SP_IS_UPDATING, false);
    }

    private void setIsUpdating(boolean isUpdating) {
        if (isUpdating) {
            mActivity.saveLastUpdateAddress();
        } else {
            clearFilePathsMap();
            clearFirwwarePath();
            mFirmwarePath = null;
            mFilesMap.clear();
            tvFirmwareSelected.setText("");
            tvFileSelected.setText(R.string.ota_no_file_selected);
        }
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putBoolean(KEY_SP_IS_UPDATING, isUpdating);
        mEditor.commit();
    }

    private boolean getIsSameDevice() {
        return mActivity.getLastUpdateDeviceAdress().equals(mActivity.getConnectedDeviceAddress());
    }

    private boolean getIsUpdatePaused() {
        return mSharedPreferences.getBoolean(KEY_SP_IS_UPDATE_PAUSED, false);
    }

    private void setIsUpdatePaused(boolean isUpdatePaused) {
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putBoolean(KEY_SP_IS_UPDATE_PAUSED, isUpdatePaused);
        mEditor.commit();
    }


    private int getUpdateTotalProgress() {
        return mSharedPreferences.getInt(KEY_SP_UPDATE_MAX_PROGRESS, 0);
    }


    private int getUpdateCurrentProgress() {
        return mSharedPreferences.getInt(KEY_SP_UPDATE_CURRENT_PROGRESS, 0);
    }

    private void setUpdateProgress(int updateProgress, int totalProgress) {
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putInt(KEY_SP_UPDATE_CURRENT_PROGRESS, updateProgress);
        mEditor.putInt(KEY_SP_UPDATE_MAX_PROGRESS, totalProgress);
        mEditor.commit();
    }

    private void showUpdateStatusInfo() {
        mUpdateInfoWrapper.setVisibility(View.VISIBLE);
        mOtaInfoWrapper.setVisibility(View.GONE);
        mSelectContainer.setVisibility(View.GONE);
        if (getIsUpdatePaused()) {
            tvProgressVersion.setText(R.string.ota_update_progress_paused);
        } else {
            tvProgressVersion.setText(R.string.ota_update_progress_preparing);
        }


        if (getIsUpdatePaused()) {
            showContinueButton();
        } else {
            showPauseButton();
        }
        pbProgress.setMax(getUpdateTotalProgress());
        pbProgress.setProgress(getUpdateCurrentProgress());
        tvRemainSize.setText(mActivity.getString(R.string.ota_update_progress_remain_size,0,0));
        tvRemainTime.setText(mActivity.getString(R.string.ota_update_progress_remain_time,0));


    }

    private void showOnlySelectContainer() {
        Log.i(TAG, "showUpdateError");
        Handler mHandler = new Handler();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mUpdateInfoWrapper.setVisibility(View.GONE);
                mOtaInfoWrapper.setVisibility(View.GONE);
                mSelectContainer.setVisibility(View.VISIBLE);

            }
        });
    }

    private void showUpgradeButton() {
        mUpdateInfoWrapper.setVisibility(View.GONE);
        mOtaInfoWrapper.setVisibility(View.VISIBLE);
        mSelectContainer.setVisibility(View.VISIBLE);
    }


    private void showPauseButton() {
        btnContinue.setVisibility(View.GONE);
        btnPause.setVisibility(View.VISIBLE);

    }

    private void showContinueButton() {
        btnPause.setVisibility(View.GONE);
        btnContinue.setVisibility(View.VISIBLE);

    }

}
