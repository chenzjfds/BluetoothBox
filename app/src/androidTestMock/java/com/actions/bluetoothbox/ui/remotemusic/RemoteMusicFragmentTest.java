package com.actions.bluetoothbox.ui.remotemusic;

import android.graphics.Color;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.app.Fragment;

import com.actions.bluetoothbox.FragmentTestRule;
import com.actions.bluetoothbox.R;
import com.actions.bluetoothbox.ui.TestRemoteMusicActivity;
import com.actions.ibluz.manager.BluzManagerData;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withResourceName;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.actions.bluetoothbox.CustomMatcher.withBgColor;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class RemoteMusicFragmentTest {

    @Rule
    public FragmentTestRule<RemoteMusicFragment, TestRemoteMusicActivity> mFragmentTestRule = new FragmentTestRule<>(RemoteMusicFragment.class, TestRemoteMusicActivity.class);
    private TestRemoteMusicActivity activity;
    private RemoteMusicFragment fragment;


    @Before
    public void beforeEachTest() {


        // Launch the activity to make the fragment visible
        mFragmentTestRule.launchActivity(null);
        //wait for the fragment to be initialize
        getInstrumentation().waitForIdleSync();

        activity = mFragmentTestRule.getActivity();
        fragment =  mFragmentTestRule.getFragment();
    }

    @Test
    public void oldFirmware_showPList() {
        //set up mock music manager
        activity.setupOldFirmware(10);
        onView(withId(R.id.advancedViewPager)).perform(swipeLeft());
        activity.onManagerReady();
        waitUntilUiIsBusy();
        getInstrumentation().waitForIdleSync();

        //assert pList is shown
        onView(withId(R.id.music_list)).check(matches(isDisplayed()));
        onView(withId(R.id.view_music_folders)).check(matches(not(isDisplayed())));

        //assert plist info is shown
        onData(instanceOf(BluzManagerData.PListEntry.class))
                .inAdapterView(withId(R.id.music_list))
                .atPosition(0)
                .onChildView(withId(R.id.text_music_name))
                .check(matches(withText("Test1.mp3")));
        onData(instanceOf(BluzManagerData.PListEntry.class))
                .inAdapterView(withId(R.id.music_list))
                .atPosition(0)
                .onChildView(withId(R.id.text_music_artist))
                .check(matches(withText("Faker1")));

    }

    @Test
    public void newFirmware_sameContentChangeId_sameMacAddress_showStoredFolders() {

        activity.setupNewFirmwareSameContentChangeIdsameMacAddress();
        onView(withId(R.id.advancedViewPager)).perform(swipeLeft());
        activity.onManagerReady();
        getInstrumentation().waitForIdleSync();

        //assert folders is shown
        onView(withId(R.id.view_music_folders)).check(matches(isDisplayed()));
        onView(withId(R.id.music_list)).check(matches(not(isDisplayed())));

        //assert stored folder info is shown
        onData(instanceOf(BluzManagerData.RemoteMusicFolder.class))
                .inAdapterView(withId(R.id.view_music_folders))
                .atPosition(0)
                .onChildView(withId(R.id.text_folder_name))
                .check(matches(withText("StoredTestFolder1")));
        onData(instanceOf(BluzManagerData.RemoteMusicFolder.class))
                .inAdapterView(withId(R.id.view_music_folders))
                .atPosition(0)
                .onChildView(withId(R.id.text_folder_music_num))
                .check(matches(withText("1 首")));
    }


    @Test
    public void newFirmware_sameContentChangeId_sameMacAddress_clickFolder_showStoredPList() {

        activity.setupNewFirmwareSameContentChangeIdsameMacAddress();
        onView(withId(R.id.advancedViewPager)).perform(swipeLeft());
        activity.onManagerReady();
        waitUntilUiIsBusy();
        getInstrumentation().waitForIdleSync();

        //assert folders is shown
        onView(withId(R.id.view_music_folders)).check(matches(isDisplayed()));
        onView(withId(R.id.music_list)).check(matches(not(isDisplayed())));


        //click on stored folder1
        onData(instanceOf(BluzManagerData.RemoteMusicFolder.class))
                .inAdapterView(withId(R.id.view_music_folders))
                .atPosition(0)
                .perform(click());


        onData(instanceOf(BluzManagerData.PListEntry.class))
                .inAdapterView(withId(R.id.music_list))
                .atPosition(0)
                .onChildView(withId(R.id.text_music_name))
                .check(matches(withText("Stored Test1.mp3")));

    }


    @Test
    public void newFirmware_differentContentChangeId_differentMacAddress_showFolders() {

        //set up mock music manager
        activity.setupNewFirmwareDifferenctContentChangeIdDifferentMacAddress(10);
        onView(withId(R.id.advancedViewPager)).perform(swipeLeft());
        activity.onManagerReady();
        getInstrumentation().waitForIdleSync();

        //assert folders is shown
        onView(withId(R.id.view_music_folders)).check(matches(isDisplayed()));
        onView(withId(R.id.music_list)).check(matches(not(isDisplayed())));

        //assert folder info is shown
        onData(instanceOf(BluzManagerData.RemoteMusicFolder.class))
                .inAdapterView(withId(R.id.view_music_folders))
                .atPosition(0)
                .onChildView(withId(R.id.text_folder_name))
                .check(matches(withText("TestFolder1")));
        onData(instanceOf(BluzManagerData.RemoteMusicFolder.class))
                .inAdapterView(withId(R.id.view_music_folders))
                .atPosition(0)
                .onChildView(withId(R.id.text_folder_music_num))
                .check(matches(withText("5 首")));

    }


    @Test
    public void newFirmware_clickFolder_showPList_clickBack_showFolders() {
        //set up mock music manager
        activity.setupNewFirmwareDifferenctContentChangeIdDifferentMacAddress(10);
        onView(withId(R.id.advancedViewPager)).perform(swipeLeft());
        activity.onManagerReady();
        getInstrumentation().waitForIdleSync();

        //assert folders is shown
        onView(withId(R.id.view_music_folders)).check(matches(isDisplayed()));
        onView(withId(R.id.music_list)).check(matches(not(isDisplayed())));

        //click on folder
        onData(instanceOf(BluzManagerData.RemoteMusicFolder.class))
                .inAdapterView(withId(R.id.view_music_folders))
                .atPosition(0)
                .perform(click());
        getInstrumentation().waitForIdleSync();


        //assert plist is shown
        onView(withId(R.id.view_music_folders)).check(matches(not(isDisplayed())));
        onView(withId(R.id.music_list)).check(matches(isDisplayed()));
        onData(instanceOf(BluzManagerData.PListEntry.class))
                .inAdapterView(withId(R.id.music_list))
                .atPosition(0)
                .onChildView(withId(R.id.text_music_name))
                .check(matches(withText("Test1.mp3")));
        //verify icon has change
        onView(withResourceName("action_bar_title")).check(matches(withText("TestFolder1")));


        //click back
        onView(withResourceName("home")).perform(click());

        //assert folders is shown
        onView(withId(R.id.view_music_folders)).check(matches(isDisplayed()));
        onView(withId(R.id.music_list)).check(matches(not(isDisplayed())));
        //verify icon has change
        onView(withResourceName("action_bar_title")).check(matches(withText(R.string.app_name)));

    }


    @Test
    public void oldFirmware_musicEntryChange_highlightMusic() {
        //set up mock music manager
        activity.setupOldFirmware(10);
        onView(withId(R.id.advancedViewPager)).perform(swipeLeft());
        activity.onManagerReady();
        waitUntilUiIsBusy();

        //assert pList is shown
        onView(withId(R.id.music_list)).check(matches(isDisplayed()));
        onView(withId(R.id.view_music_folders)).check(matches(not(isDisplayed())));

        activity.setCurrentMusicEntryIndex(1);
        onData(instanceOf(BluzManagerData.PListEntry.class))
                .inAdapterView(withId(R.id.music_list))
                .atPosition(0)
                .check(matches(withBgColor(Color.BLUE)));

        activity.setCurrentMusicEntryIndex(3);
        onData(instanceOf(BluzManagerData.PListEntry.class))
                .inAdapterView(withId(R.id.music_list))
                .atPosition(2)
                .check(matches(withBgColor(Color.BLUE)));

    }


    @Test
    public void newFirmware_musicEntryChange_highlightFolder() {
        //set up mock music manager
        activity.setupNewFirmwareDifferenctContentChangeIdDifferentMacAddress(10);
        onView(withId(R.id.advancedViewPager)).perform(swipeLeft());
        activity.onManagerReady();
        getInstrumentation().waitForIdleSync();

        //assert folders is shown
        onView(withId(R.id.view_music_folders)).check(matches(isDisplayed()));
        onView(withId(R.id.music_list)).check(matches(not(isDisplayed())));

        //music in folder0
        activity.setCurrentMusicEntryIndex(1);
        onData(instanceOf(BluzManagerData.RemoteMusicFolder.class))
                .inAdapterView(withId(R.id.view_music_folders))
                .atPosition(0)
                .check(matches(withBgColor(Color.BLUE)));

        activity.setCurrentMusicEntryIndex(3);
        onData(instanceOf(BluzManagerData.RemoteMusicFolder.class))
                .inAdapterView(withId(R.id.view_music_folders))
                .atPosition(0)
                .check(matches(withBgColor(Color.BLUE)));

        //music in folder1
        activity.setCurrentMusicEntryIndex(6);
        onData(instanceOf(BluzManagerData.RemoteMusicFolder.class))
                .inAdapterView(withId(R.id.view_music_folders))
                .atPosition(1)
                .check(matches(withBgColor(Color.BLUE)));

        activity.setCurrentMusicEntryIndex(9);
        onData(instanceOf(BluzManagerData.RemoteMusicFolder.class))
                .inAdapterView(withId(R.id.view_music_folders))
                .atPosition(1)
                .check(matches(withBgColor(Color.BLUE)));
    }

    @Test
    public void newFirmware_enterFolder_musicEntryChangeToMusicInTheSameFolder_highlightMusic() {
        //set up mock music manager
        activity.setupNewFirmwareDifferenctContentChangeIdDifferentMacAddress(10);
        onView(withId(R.id.advancedViewPager)).perform(swipeLeft());
        activity.onManagerReady();
        getInstrumentation().waitForIdleSync();

        //assert folders is shown
        onView(withId(R.id.view_music_folders)).check(matches(isDisplayed()));
        onView(withId(R.id.music_list)).check(matches(not(isDisplayed())));

        //enter folder
        onData(instanceOf(BluzManagerData.RemoteMusicFolder.class))
                .inAdapterView(withId(R.id.view_music_folders))
                .atPosition(0)
                .perform(click());

        activity.setCurrentMusicEntryIndex(1);
        onData(instanceOf(BluzManagerData.PListEntry.class))
                .inAdapterView(withId(R.id.music_list))
                .atPosition(0)
                .check(matches(withBgColor(Color.BLUE)));

        activity.setCurrentMusicEntryIndex(3);
        onData(instanceOf(BluzManagerData.PListEntry.class))
                .inAdapterView(withId(R.id.music_list))
                .atPosition(2)
                .check(matches(withBgColor(Color.BLUE)));


    }

    @Test
    public void newFirmware_enterFolder_musicEntryChangeToMusicInDifferentFolder_highlightMusic_clickBack_highlightDifferentFolder() {
        //set up mock music manager
        activity.setupNewFirmwareDifferenctContentChangeIdDifferentMacAddress(10);
        activity.setCurrentMusicEntryIndex(1);
        onView(withId(R.id.advancedViewPager)).perform(swipeLeft());
        activity.onManagerReady();
        getInstrumentation().waitForIdleSync();

        //assert folders is shown
        onView(withId(R.id.view_music_folders)).check(matches(isDisplayed()));
        onView(withId(R.id.music_list)).check(matches(not(isDisplayed())));

        //enter folder
        onData(instanceOf(BluzManagerData.RemoteMusicFolder.class))
                .inAdapterView(withId(R.id.view_music_folders))
                .atPosition(1)
                .perform(click());

        activity.setCurrentMusicEntryIndex(6);
        onData(instanceOf(BluzManagerData.PListEntry.class))
                .inAdapterView(withId(R.id.music_list))
                .atPosition(0)
                .check(matches(withBgColor(Color.BLUE)));

        //click back
        onView(withResourceName("home")).perform(click());

        onData(instanceOf(BluzManagerData.RemoteMusicFolder.class))
                .inAdapterView(withId(R.id.view_music_folders))
                .atPosition(1)
                .check(matches(withBgColor(Color.BLUE)));


    }

    @Test
    public void newFirmware_enterFolder_swipeRight_hideFolderToggle_swipeLeft_showFolderToggle_exitFolder_hideFolderToggle_swipeRight_hideFolderToggle() {
        //set up mock music manager
        activity.setupNewFirmwareDifferenctContentChangeIdDifferentMacAddress(10);
        activity.setCurrentMusicEntryIndex(1);
        onView(withId(R.id.advancedViewPager)).perform(swipeLeft());
        activity.onManagerReady();
        getInstrumentation().waitForIdleSync();

        //assert folders is shown
        onView(withId(R.id.view_music_folders)).check(matches(isDisplayed()));
        onView(withId(R.id.music_list)).check(matches(not(isDisplayed())));

        //enter folder
        onData(instanceOf(BluzManagerData.RemoteMusicFolder.class))
                .inAdapterView(withId(R.id.view_music_folders))
                .atPosition(0)
                .perform(click());


        //assert plist is shown
        onView(withId(R.id.view_music_folders)).check(matches(not(isDisplayed())));
        onView(withId(R.id.music_list)).check(matches(isDisplayed()));

        //show folder toggle
        onView(withResourceName("action_bar_title")).check(matches(withText("TestFolder1")));

        //swipe left
        onView(withId(R.id.advancedViewPager)).perform(swipeLeft());

        //hide folder toggle
        onView(withResourceName("action_bar_title")).check(matches(withText(R.string.app_name)));

        //swipe right
        onView(withId(R.id.advancedViewPager)).perform(swipeRight());

        //show folder toggle
        onView(withResourceName("action_bar_title")).check(matches(withText("TestFolder1")));

        //swipe right
        onView(withId(R.id.advancedViewPager)).perform(swipeRight());

        //show folder toggle
        onView(withResourceName("action_bar_title")).check(matches(withText(R.string.app_name)));

        //swipe left
        onView(withId(R.id.advancedViewPager)).perform(swipeLeft());

        //show folder toggle
        onView(withResourceName("action_bar_title")).check(matches(withText("TestFolder1")));

        //click back
        onView(withResourceName("home")).perform(click());

        //hide folder toggle
        onView(withResourceName("action_bar_title")).check(matches(withText(R.string.app_name)));

        //swipe left
        onView(withId(R.id.advancedViewPager)).perform(swipeLeft());

        // icon not change
        onView(withResourceName("action_bar_title")).check(matches(withText(R.string.app_name)));
    }

    @Test
    public void newFirmware_enterFolder_swipeRight_hideFolderToggle_swipeLeft_showFolderToggle_exitFolder_enterDifferentFolder() {
        //set up mock music manager
        activity.setupNewFirmwareDifferenctContentChangeIdDifferentMacAddress(10);
        activity.setCurrentMusicEntryIndex(1);
        onView(withId(R.id.advancedViewPager)).perform(swipeLeft());
        activity.onManagerReady();
        getInstrumentation().waitForIdleSync();

        //assert folders is shown
        onView(withId(R.id.view_music_folders)).check(matches(isDisplayed()));
        onView(withId(R.id.music_list)).check(matches(not(isDisplayed())));

        //enter folder
        onData(instanceOf(BluzManagerData.RemoteMusicFolder.class))
                .inAdapterView(withId(R.id.view_music_folders))
                .atPosition(0)
                .perform(click());


        //assert plist is shown
        onView(withId(R.id.view_music_folders)).check(matches(not(isDisplayed())));
        onView(withId(R.id.music_list)).check(matches(isDisplayed()));

        //show folder toggle
        onView(withResourceName("action_bar_title")).check(matches(withText("TestFolder1")));

        //swipe left
        onView(withId(R.id.advancedViewPager)).perform(swipeLeft());

        //hide folder toggle
        onView(withResourceName("action_bar_title")).check(matches(withText(R.string.app_name)));

        //swipe right
        onView(withId(R.id.advancedViewPager)).perform(swipeRight());

        //show folder toggle
        onView(withResourceName("action_bar_title")).check(matches(withText("TestFolder1")));

        //click back
        onView(withResourceName("home")).perform(click());

        //enter folder
        onData(instanceOf(BluzManagerData.RemoteMusicFolder.class))
                .inAdapterView(withId(R.id.view_music_folders))
                .atPosition(1)
                .perform(click());

        //show folder toggle
        onView(withResourceName("action_bar_title")).check(matches(withText("TestFolder2")));

        //swipe left
        onView(withId(R.id.advancedViewPager)).perform(swipeLeft());

        //hide folder toggle
        onView(withResourceName("action_bar_title")).check(matches(withText(R.string.app_name)));

        //swipe right
        onView(withId(R.id.advancedViewPager)).perform(swipeRight());

        //show folder toggle
        onView(withResourceName("action_bar_title")).check(matches(withText("TestFolder2")));

        //click back
        onView(withResourceName("home")).perform(click());

        //hide folder toggle
        onView(withResourceName("action_bar_title")).check(matches(withText(R.string.app_name)));

        //swipe left
        onView(withId(R.id.advancedViewPager)).perform(swipeLeft());

        // icon not change
        onView(withResourceName("action_bar_title")).check(matches(withText(R.string.app_name)));
    }

    @Test
    public void newFirmware_enterFolder_showFolderToggle_fragmentStop_hideFolderToggle() {
        //set up mock music manager
        activity.setupNewFirmwareDifferenctContentChangeIdDifferentMacAddress(10);
        activity.setCurrentMusicEntryIndex(1);
        onView(withId(R.id.advancedViewPager)).perform(swipeLeft());
        activity.onManagerReady();
        getInstrumentation().waitForIdleSync();

        //assert folders is shown
        onView(withId(R.id.view_music_folders)).check(matches(isDisplayed()));
        onView(withId(R.id.music_list)).check(matches(not(isDisplayed())));

        //enter folder
        onData(instanceOf(BluzManagerData.RemoteMusicFolder.class))
                .inAdapterView(withId(R.id.view_music_folders))
                .atPosition(0)
                .perform(click());


        //assert plist is shown
        onView(withId(R.id.view_music_folders)).check(matches(not(isDisplayed())));
        onView(withId(R.id.music_list)).check(matches(isDisplayed()));

        //show folder toggle
        onView(withResourceName("action_bar_title")).check(matches(withText("TestFolder1")));

        activity.getSupportFragmentManager().beginTransaction().remove(fragment).commit();

        // icon not change
        onView(withResourceName("action_bar_title")).check(matches(withText(R.string.app_name)));
    }

    @Test
    public void oldFirmware_showPList_hideFolderToggle() {
        //set up mock music manager
        activity.setupOldFirmware(10);
        onView(withId(R.id.advancedViewPager)).perform(swipeLeft());
        activity.onManagerReady();
        waitUntilUiIsBusy();
        getInstrumentation().waitForIdleSync();

        //assert pList is shown
        onView(withId(R.id.music_list)).check(matches(isDisplayed()));
        onView(withId(R.id.view_music_folders)).check(matches(not(isDisplayed())));

        //swipe left
        onView(withId(R.id.advancedViewPager)).perform(swipeLeft());

        //hide folder toggle
        onView(withResourceName("action_bar_title")).check(matches(withText(R.string.app_name)));

        //swipe right
        onView(withId(R.id.advancedViewPager)).perform(swipeRight());

        //hide folder toggle
        onView(withResourceName("action_bar_title")).check(matches(withText(R.string.app_name)));

        //swipe right
        onView(withId(R.id.advancedViewPager)).perform(swipeRight());

        //hide folder toggle
        onView(withResourceName("action_bar_title")).check(matches(withText(R.string.app_name)));

    }

    @Test
    public void newFirmware_enterFolder_leave_onResume_showFolders() {
        //set up mock music manager
        activity.setupNewFirmwareDifferenctContentChangeIdDifferentMacAddress(10);
        activity.setCurrentMusicEntryIndex(1);
        onView(withId(R.id.advancedViewPager)).perform(swipeLeft());
        activity.onManagerReady();
        getInstrumentation().waitForIdleSync();

        //assert folders is shown
        onView(withId(R.id.view_music_folders)).check(matches(isDisplayed()));
        onView(withId(R.id.music_list)).check(matches(not(isDisplayed())));

        //enter folder
        onData(instanceOf(BluzManagerData.RemoteMusicFolder.class))
                .inAdapterView(withId(R.id.view_music_folders))
                .atPosition(0)
                .perform(click());


        //assert plist is shown
        onView(withId(R.id.view_music_folders)).check(matches(not(isDisplayed())));
        onView(withId(R.id.music_list)).check(matches(isDisplayed()));

        //show folder toggle
        onView(withResourceName("action_bar_title")).check(matches(withText("TestFolder1")));

        activity.setupNewFirmwareSameContentChangeIdsameMacAddress();
        //leave
        activity.getSupportFragmentManager().beginTransaction()
         .replace(R.id.container, new Fragment())
        .addToBackStack(null).commit();
        activity.getSupportFragmentManager().popBackStack();

        //assert folders is shown
        onView(withId(R.id.view_music_folders)).check(matches(isDisplayed()));
        onView(withId(R.id.music_list)).check(matches(not(isDisplayed())));

    }

    // mock music manager takes time before returning plist and remote music folders,
    // it will make espresso think that ui is idle now, and begin view assertion which makes test flasky
    private void waitUntilUiIsBusy(){
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}