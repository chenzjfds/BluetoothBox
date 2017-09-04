package com.actions.bluetoothbox.data.local;

import android.support.annotation.NonNull;

import com.actions.bluetoothbox.BuildConfig;
import com.actions.ibluz.manager.BluzManagerData;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

/**
 * Created by chenxiangjie on 2016/9/13.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class PreferencesHelperTest {


    private PreferencesHelper mPreferencesHelper;

    @Before
    public void setUp() throws Exception {
        mPreferencesHelper = PreferencesHelper.getInstance(RuntimeEnvironment.application);
        mPreferencesHelper.clearAll();
    }

    @Test
    public void defaultContentChangeId() throws Exception {
        assertThat(mPreferencesHelper.getLastContentChangeId()).isEqualTo(-1);
    }


    @Test
    public void saveContentChangeId() throws Exception {
        mPreferencesHelper.saveAsLastContentChangeId(2);
        assertThat(mPreferencesHelper.getLastContentChangeId()).isEqualTo(2);
    }

    @Test
    public void defaultDeviceAddress() throws Exception {
        assertThat(mPreferencesHelper.getLastConnectedDeviceAddress()).isEqualTo("");
    }

    @Test
    public void saveDeviceAddress() throws Exception {
        mPreferencesHelper.saveAsLastConnectedDeviceAddress("FF:FF:FF:FF:FF:FF");
        assertThat(mPreferencesHelper.getLastConnectedDeviceAddress()).isEqualTo("FF:FF:FF:FF:FF:FF");
    }


    @Test
    public void defaultCardMusicPListLength() throws Exception {
        assertThat(mPreferencesHelper.getCardMusicPListLength()).isEqualTo(0);
    }

    @Test
    public void saveCardMusicPListLength() throws Exception {
        mPreferencesHelper.storeMusicPListAndLength(BluzManagerData.FuncMode.CARD, generatePList(2), null);
        assertThat(mPreferencesHelper.getCardMusicPListLength()).isEqualTo(2);
    }

    @Test
    public void defaultUHostMusicPListLength() throws Exception {
        assertThat(mPreferencesHelper.getUHostMusicPListLength()).isEqualTo(0);
    }

    @Test
    public void saveUHostMusicPListLength() throws Exception {
        mPreferencesHelper.storeMusicPListAndLength(BluzManagerData.FuncMode.USB, generatePList(2), null);
        assertThat(mPreferencesHelper.getUHostMusicPListLength()).isEqualTo(2);
    }

    @Test
    public void defaultCRecordMusicPListLength() throws Exception {
        assertThat(mPreferencesHelper.getCRecordMusicPListLength()).isEqualTo(0);
    }

    @Test
    public void saveCRecordMusicPListLength() throws Exception {

        mPreferencesHelper.storeMusicPListAndLength(BluzManagerData.FuncMode.CRECORD, generatePList(2), null);
        assertThat(mPreferencesHelper.getCRecordMusicPListLength()).isEqualTo(2);
    }

    @Test
    public void defaultURecordMusicPListLength() throws Exception {
        assertThat(mPreferencesHelper.getURecordMusicPListLength()).isEqualTo(0);
    }

    @Test
    public void saveURecordMusicPListLength() throws Exception {
        mPreferencesHelper.storeMusicPListAndLength(BluzManagerData.FuncMode.URECORD, generatePList(2), null);
        assertThat(mPreferencesHelper.getURecordMusicPListLength()).isEqualTo(2);
    }

    @Test
    public void defaultGetStoredPList() throws Exception {
        assertThat(mPreferencesHelper.getStoredPList(BluzManagerData.FuncMode.CARD, null)).isEmpty();
    }

    @Test
    public void storePList_cardMode() throws Exception {
        mPreferencesHelper.storeMusicPListAndLength(BluzManagerData.FuncMode.CARD, generatePList(10), null);
        assertThat(mPreferencesHelper.getStoredPList(BluzManagerData.FuncMode.CARD, null)).hasSize(10);
        assertThat(mPreferencesHelper.getStoredPList(BluzManagerData.FuncMode.CARD, null).get(0).index).isEqualTo(1);
        assertThat(mPreferencesHelper.getStoredPList(BluzManagerData.FuncMode.CARD, null).get(9).index).isEqualTo(10);
    }

    @Test
    public void storePList_uhostMode() throws Exception {

        mPreferencesHelper.storeMusicPListAndLength(BluzManagerData.FuncMode.USB, generatePList(9), null);
        assertThat(mPreferencesHelper.getStoredPList(BluzManagerData.FuncMode.USB, null)).hasSize(9);
        assertThat(mPreferencesHelper.getStoredPList(BluzManagerData.FuncMode.USB, null).get(0).index).isEqualTo(1);
        assertThat(mPreferencesHelper.getStoredPList(BluzManagerData.FuncMode.USB, null).get(8).index).isEqualTo(9);
    }

    @Test
    public void storePList_uRecordMode() throws Exception {

        mPreferencesHelper.storeMusicPListAndLength(BluzManagerData.FuncMode.URECORD, generatePList(9), null);
        assertThat(mPreferencesHelper.getStoredPList(BluzManagerData.FuncMode.URECORD, null)).hasSize(9);
        assertThat(mPreferencesHelper.getStoredPList(BluzManagerData.FuncMode.URECORD, null).get(0).index).isEqualTo(1);
        assertThat(mPreferencesHelper.getStoredPList(BluzManagerData.FuncMode.URECORD, null).get(7).index).isEqualTo(8);
    }

    @Test
    public void storePList_cRecordMode() throws Exception {

        mPreferencesHelper.storeMusicPListAndLength(BluzManagerData.FuncMode.CRECORD, generatePList(8), null);
        assertThat(mPreferencesHelper.getStoredPList(BluzManagerData.FuncMode.CRECORD, null)).hasSize(8);
        assertThat(mPreferencesHelper.getStoredPList(BluzManagerData.FuncMode.CRECORD, null).get(0).index).isEqualTo(1);
        assertThat(mPreferencesHelper.getStoredPList(BluzManagerData.FuncMode.CRECORD, null).get(6).index).isEqualTo(7);
    }

    @Test
    public void defaultGetStoredRemoteMusicFolders() throws Exception {
        assertThat(mPreferencesHelper.getStoredRemoteMusicFolders(BluzManagerData.FuncMode.CARD)).isEmpty();
    }

    @Test
    public void storeRemoteMusicFolders_cardMode() throws Exception {
        mPreferencesHelper.storeRemoteMusicFolders(generateRemoteMusicFolders(5), BluzManagerData.FuncMode.CARD);
        assertThat(mPreferencesHelper.getStoredRemoteMusicFolders(BluzManagerData.FuncMode.CARD)).hasSize(5);
        assertThat(mPreferencesHelper.getStoredRemoteMusicFolders(BluzManagerData.FuncMode.CARD).get(0).name).isEqualTo("Test0");
    }


    @Test
    public void storeRemoteMusicFolders_uHostMode() throws Exception {
        mPreferencesHelper.storeRemoteMusicFolders(generateRemoteMusicFolders(4), BluzManagerData.FuncMode.USB);
        assertThat(mPreferencesHelper.getStoredRemoteMusicFolders(BluzManagerData.FuncMode.USB)).hasSize(4);
        assertThat(mPreferencesHelper.getStoredRemoteMusicFolders(BluzManagerData.FuncMode.USB).get(0).name).isEqualTo("Test0");
    }

    @Test
    public void storeRemoteMusicFolders_clearStoredRemoteMusicFolders() throws Exception {

        mPreferencesHelper.storeRemoteMusicFolders(generateRemoteMusicFolders(4), BluzManagerData.FuncMode.USB);
        mPreferencesHelper.storeRemoteMusicFolders(generateRemoteMusicFolders(4), BluzManagerData.FuncMode.CARD);

        assertThat(mPreferencesHelper.getStoredRemoteMusicFolders(BluzManagerData.FuncMode.USB)).hasSize(4);
        assertThat(mPreferencesHelper.getStoredRemoteMusicFolders(BluzManagerData.FuncMode.USB).get(0).name).isEqualTo("Test0");
        assertThat(mPreferencesHelper.getStoredRemoteMusicFolders(BluzManagerData.FuncMode.CARD)).hasSize(4);
        assertThat(mPreferencesHelper.getStoredRemoteMusicFolders(BluzManagerData.FuncMode.CARD).get(0).name).isEqualTo("Test0");

        mPreferencesHelper.clearStoredRemoteMusicFolders();
        assertThat(mPreferencesHelper.getStoredRemoteMusicFolders(BluzManagerData.FuncMode.USB)).isEmpty();
        assertThat(mPreferencesHelper.getStoredRemoteMusicFolders(BluzManagerData.FuncMode.CARD)).isEmpty();
    }


    @Test
    public void storePListAndLength_cRecordMode() throws Exception {

        mPreferencesHelper.storeMusicPListAndLength(BluzManagerData.FuncMode.CARD, generatePList(5), null);
        mPreferencesHelper.storeMusicPListAndLength(BluzManagerData.FuncMode.USB, generatePList(5), null);
        mPreferencesHelper.storeMusicPListAndLength(BluzManagerData.FuncMode.CRECORD, generatePList(5), null);
        mPreferencesHelper.storeMusicPListAndLength(BluzManagerData.FuncMode.URECORD, generatePList(5), null);

        assertThat(mPreferencesHelper.getStoredPList(BluzManagerData.FuncMode.CARD, null)).hasSize(5);
        assertThat(mPreferencesHelper.getStoredPList(BluzManagerData.FuncMode.USB, null)).hasSize(5);
        assertThat(mPreferencesHelper.getStoredPList(BluzManagerData.FuncMode.CRECORD, null)).hasSize(5);
        assertThat(mPreferencesHelper.getStoredPList(BluzManagerData.FuncMode.URECORD, null)).hasSize(5);
        assertThat(mPreferencesHelper.getStoredPList(BluzManagerData.FuncMode.CRECORD, null).get(0).index).isEqualTo(1);
        assertThat(mPreferencesHelper.getStoredPList(BluzManagerData.FuncMode.CRECORD, null).get(4).index).isEqualTo(5);

        mPreferencesHelper.clearStoredMusicPListAndLength(null);

        assertThat(mPreferencesHelper.getStoredRemoteMusicFolders(BluzManagerData.FuncMode.CARD)).isEmpty();
        assertThat(mPreferencesHelper.getStoredRemoteMusicFolders(BluzManagerData.FuncMode.USB)).isEmpty();
        assertThat(mPreferencesHelper.getStoredRemoteMusicFolders(BluzManagerData.FuncMode.CRECORD)).isEmpty();
        assertThat(mPreferencesHelper.getStoredRemoteMusicFolders(BluzManagerData.FuncMode.URECORD)).isEmpty();

        assertThat(mPreferencesHelper.getCardMusicPListLength()).isEqualTo(0);
        assertThat(mPreferencesHelper.getUHostMusicPListLength()).isEqualTo(0);
        assertThat(mPreferencesHelper.getCRecordMusicPListLength()).isEqualTo(0);
        assertThat(mPreferencesHelper.getURecordMusicPListLength()).isEqualTo(0);
    }

    @NonNull
    private List<BluzManagerData.RemoteMusicFolder> generateRemoteMusicFolders(int size) {
        List<BluzManagerData.RemoteMusicFolder> folders = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            BluzManagerData.RemoteMusicFolder folder = new BluzManagerData.RemoteMusicFolder();
            folder.name = "Test" + i;
            folders.add(folder);
        }
        return folders;
    }


    private List<BluzManagerData.PListEntry> generatePList(int size) {
        List<BluzManagerData.PListEntry> entries = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            BluzManagerData.PListEntry pListEntry = new BluzManagerData.PListEntry();
            pListEntry.index = i + 1;
            entries.add(pListEntry);
        }
        return entries;
    }


}