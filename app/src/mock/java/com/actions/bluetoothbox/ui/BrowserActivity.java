package com.actions.bluetoothbox.ui;

import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.FrameLayout;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actions.bluetoothbox.R;
import com.actions.ibluz.factory.IBluzDevice;
import com.actions.ibluz.manager.BluzManager;
import com.actions.ibluz.manager.IBluzManager;
import com.actions.ibluz.manager.IGlobalManager;
import com.actions.ibluz.ota.updater.OnCheckFirmwareListener;
import com.actions.ibluz.ota.updater.Update;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import static org.mockito.Mockito.mock;

/**
 * Created by chenxiangjie on 2016/8/31.
 */

public class BrowserActivity extends SherlockFragmentActivity {

    //mock
    public boolean mMediaFree = true;

    SlidingMenu slidingMenu;
    BluzManager bluzManager;
    IBluzDevice iBluzDevice;

    MockMusicManager mockMusicManager;
    private int currentMode;
    private String connectedDeviceAddress = "FAKE";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupActionBar();
        initMocks();

        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.setId(R.id.container);
        setContentView(frameLayout);

    }

    private void initMocks() {
        slidingMenu = mock(SlidingMenu.class);
        bluzManager = mock(BluzManager.class);
        iBluzDevice = mock(IBluzDevice.class);

    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        // actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.app_name);
        // actionBar.setSubtitle(R.string.ab_subtitle);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            actionBar.setHomeButtonEnabled(true);
        }
    }




    /*
        * mock start
        * */
    public void setMode(int mode) {

    }

    public void addFragmentToStack(String tag) {

    }


    public IGlobalManager getIGlobalManager() {

        return bluzManager;
    }

    public IBluzManager getIBluzManager() {

        return bluzManager;
    }

    public void menuItemSelected(Menu menu, int itemIdSelected) {

    }

    public IBluzDevice getBluzConnector() {
        return iBluzDevice;
    }


    public SlidingMenu getSlidingMenu() {
        return slidingMenu;
    }


    public BluzManager getBluzManager() {
        return bluzManager;
    }

    public void showAlarmDialog(AlertDialog adg) {

    }

    public void dismissAlarmDialog() {

    }

    public void replaceFragment(Fragment fragment, String tag) {

    }

    public void showDisconnectDialog() {

    }

    public void hideDisconnectDialog() {

    }

    public void stopBackgroundMusic() {

    }

    public void updateMediaCenterInfo(String title, String artist) {

    }

    public void startUpdate(Update update) {
    }

    public void stopUpdate() {
    }

    public void confirmUpdateAndReboot() {
    }

    public void resetMachine() {
    }

    public void updateVram(byte[] data) {
    }

    public void saveDeviceAdress() {
    }

    public void saveLastUpdateAddress() {
    }

    public String getLastUpdateDeviceAdress() {
        return "FAKE";
    }

    public void setConnectedDeviceAddress(String connectedDeviceAddress){
         this.connectedDeviceAddress = connectedDeviceAddress;
    }
    public String getConnectedDeviceAddress(){
        return connectedDeviceAddress;
    }

    public void getFirmWareVersion(OnCheckFirmwareListener onCheckFirmwareListener) {
    }

    public void setCurrentMode(int mode){
        currentMode = mode;
    }

    public int getCurrentMode() {
        return currentMode;
    }

    public void setOtaRebootDialog(AlertDialog alertDialog) {
    }

    public void hideOtaRebootDialog() {
    }
 /*
    * mock end
    * */
}
