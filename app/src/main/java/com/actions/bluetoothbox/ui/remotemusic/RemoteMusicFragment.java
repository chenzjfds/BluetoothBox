package com.actions.bluetoothbox.ui.remotemusic;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actions.bluetoothbox.R;
import com.actions.bluetoothbox.data.local.PreferencesHelper;
import com.actions.bluetoothbox.ui.BrowserActivity;
import com.actions.bluetoothbox.util.Utils;
import com.actions.bluetoothbox.widget.VerticalSeekBar;
import com.actions.bluetoothbox.widget.lyric.LyricView;
import com.actions.ibluz.manager.BluzManagerData;
import com.actions.ibluz.manager.BluzManagerData.EQMode;
import com.actions.ibluz.manager.BluzManagerData.LoopMode;
import com.actions.ibluz.manager.BluzManagerData.MusicEntry;
import com.actions.ibluz.manager.BluzManagerData.PListEntry;
import com.actions.ibluz.manager.BluzManagerData.PlayState;
import com.actions.ibluz.manager.BluzManagerData.RemoteMusicFolder;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class RemoteMusicFragment extends SherlockFragment implements RemoteMusicVista {
    private static final String TAG = "RemoteMusicFragment";


    private RemoteMusicPresenter mRemoteMusicPresenter;
    private BrowserActivity mActivity;
    private ViewPager mViewPager;
    private View mListPager;
    private View mInfoPager;
    private View mLyricPager;
    private ImageButton mLoopModeButton;
    private ImageButton mPlayPauseButton;
    private ImageButton mPreviousButton;
    private ImageButton mNextButton;
    private ImageButton mEQImageButton;
    private SeekBar mSeekBar;
    private ListView mMusicListView;
    private ListView musicFolderListView;
    private TextView mCurrentText;
    private TextView mDurationText;
    private TextView mMusicNameText;
    private TextView mArtistNameText;
    private TextView mMusicTitleText;
    private TextView mMusicArtistText;
    private TextView mMusicAblumText;
    private TextView mMusicGenreText;
    private TextView mMusicMimeTypeText;

    //pager music list loading
    private RelativeLayout viewLoading;
    private TextView textLoadingProgress;
    private TextView textLoadingTotal;

    private LyricView mLyricView;
    private View mEqSettingLayout;
    private VerticalSeekBar[] mEqSeekBar;
    private String mLrcFilePath;
    private SlidingMenu mSlidingMenu;
    private List<View> mPagerList;
    private MusicPagerAdapter mPagerAdapter;
    private MusicListAdapter mMusicListAdapter;
    private MusicFolderListAdapter musicFolderListAdapter;
    private int mEqPreset = EQMode.NORMAL;
    private List<int[]> mEqBandLevel = new ArrayList<int[]>();
    private List<PListEntry> mPListEntryList = new ArrayList<PListEntry>();
    private List<BluzManagerData.RemoteMusicFolder> musicFolderEntries = new ArrayList<BluzManagerData.RemoteMusicFolder>();
    private boolean mResumeFlag = false;
    private Menu mMenu = null;

    private OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.musicLoopModeButton:
                    mRemoteMusicPresenter.switchLoop();
                    break;
                case R.id.musicPlayPauseButton:
                    mRemoteMusicPresenter.playPause();
                    break;
                case R.id.musicPreviousButton:
                    mRemoteMusicPresenter.previous();
                    break;
                case R.id.musicNextButton:
                    mRemoteMusicPresenter.next();
                    break;
                case R.id.musicPlaceholderButton:
                    break;
                default:
                    break;
            }
        }
    };
    private String folderModeToggleTitle = "";
    private int LIST_PAGER_POSITION = 1;
    private boolean isShowingPList = false;
    private int LYRIC_PAGER_POSITION = 2;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView!");
        setHasOptionsMenu(true);
        mActivity = (BrowserActivity) getActivity();
        View view = inflater.inflate(R.layout.fragment_remotemusic, container, false);
        mListPager = inflater.inflate(R.layout.pager_music_list, null);
        mInfoPager = inflater.inflate(R.layout.pager_music_info, null);
        mLyricPager = inflater.inflate(R.layout.pager_music_lyric, null);
        mSlidingMenu = mActivity.getSlidingMenu();
        initView(view);

        // clearFragmentBackstack in browserActivity will invoke all backstack fragments'lifecycle event like onCreateView
        if (mActivity.getBluzManager() != null) {
            initInjector();
            initPresenter();
        }
        return view;
    }

    public void initInjector() {
        Log.d(TAG, "initInjector() called with: " + "");
        mRemoteMusicPresenter = new RemoteMusicPresenterImp(mActivity, getActivity(), mActivity.getBluzManager(), PreferencesHelper.getInstance(getActivity().getApplicationContext()));

    }

    public void initPresenter() {
        mRemoteMusicPresenter.setVista(this);
        Log.d(TAG, "initPresenter() called with: " + "onStart");
        mRemoteMusicPresenter.onStart();
    }


    @Override
    public void showLoading() {
        Log.d(TAG, "showLoading() called with: " + "");
        viewLoading.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        Log.d(TAG, "hideLoading() called with: " + "");
        viewLoading.setVisibility(View.GONE);
        textLoadingProgress.setVisibility(View.GONE);
        textLoadingTotal.setVisibility(View.GONE);
    }

    @Override
    public void updateLoadingMusic(int progress, int total) {
        Log.d(TAG, "updateLoadingMusic() called with: " + "total = [" + total + "]");
        textLoadingProgress.setVisibility(View.VISIBLE);
        textLoadingTotal.setVisibility(View.VISIBLE);
        textLoadingProgress.setText(String.format(getString(R.string.notice_loadingcurrentnum), progress));
        textLoadingTotal.setText(String.format(getString(R.string.notice_loadingnum), total));
    }


    @Override
    public void updateLoadingFolders(int progress, int total) {
        Log.d(TAG, "updateLoadingMusic() called with: " + "total = [" + total + "]");
        textLoadingProgress.setVisibility(View.VISIBLE);
        textLoadingTotal.setVisibility(View.VISIBLE);
        textLoadingProgress.setText(String.format(getString(R.string.notice_loadingcurrentnum), progress));
        textLoadingTotal.setText(String.format(getString(R.string.remote_music_loding_folders_totol_num), total));
    }

    @Override
    public void showCurrentMusicProgress(int position) {
        mCurrentText.setText(Utils.showTime(position));
        mSeekBar.setProgress(position);
    }

    @Override
    public void showLyric(MusicEntry entry) {
        mLrcFilePath = Utils.getPrivateFilePath(getActivity().getApplicationContext(), entry.title + ".lrc");
        mLyricView.setLyric(mLrcFilePath, entry.title);
    }

    @Override
    public void updateLyric(int position) {
        long delay = mLyricView.updateIndex(position);
        if (delay == 0) {
            delay = 200;
        }
        mRemoteMusicPresenter.refreshLyricProgress(delay);
    }

    @Override
    public void showPList(List<PListEntry> entries) {
        Log.d(TAG, "showPList() called with: entries = [" + entries + "]");
        isShowingPList = true;
        mMusicListView.setVisibility(View.VISIBLE);
        mPListEntryList.clear();
        mPListEntryList.addAll(entries);
        mMusicListAdapter.notifyDataSetChanged();
    }

    @Override
    public void hidePList() {
        mMusicListView.setVisibility(View.GONE);
    }

    @Override
    public void showRemoteMusicFolders(List<RemoteMusicFolder> entries) {
        Log.d(TAG, "showRemoteMusicFolders() called with: entries = [" + entries + "]");
        isShowingPList = false;
        musicFolderListView.setVisibility(View.VISIBLE);
        musicFolderEntries.clear();
        musicFolderEntries.addAll(entries);
        musicFolderListAdapter.notifyDataSetChanged();
    }

    @Override
    public void hideRemoteMusicFolders() {
        musicFolderListView.setVisibility(View.GONE);
    }

    private void initView(View view) {
        mLoopModeButton = (ImageButton) view.findViewById(R.id.musicLoopModeButton);
        mPlayPauseButton = (ImageButton) view.findViewById(R.id.musicPlayPauseButton);
        mPreviousButton = (ImageButton) view.findViewById(R.id.musicPreviousButton);
        mNextButton = (ImageButton) view.findViewById(R.id.musicNextButton);
        mEQImageButton = (ImageButton) view.findViewById(R.id.musicPlaceholderButton);
        mSeekBar = (SeekBar) view.findViewById(R.id.musicSeekBar);
        mSeekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        mCurrentText = (TextView) view.findViewById(R.id.musicCurrentText);
        mDurationText = (TextView) view.findViewById(R.id.musicDurationText);
        mMusicNameText = (TextView) view.findViewById(R.id.musicNameText);
        mArtistNameText = (TextView) view.findViewById(R.id.artistNameText);
        mViewPager = (ViewPager) view.findViewById(R.id.advancedViewPager);

        mLoopModeButton.setOnClickListener(mOnClickListener);
        mPlayPauseButton.setOnClickListener(mOnClickListener);
        mPreviousButton.setOnClickListener(mOnClickListener);
        mNextButton.setOnClickListener(mOnClickListener);
        mEQImageButton.setOnClickListener(mOnClickListener);
        mViewPager.addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int arg0) {

            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Log.d(TAG, "onPageSelected() called with: position = [" + position + "]");
                if (position == 0) {
                    mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
                } else {
                    mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
                }

                if (position == LYRIC_PAGER_POSITION) {
                    mRemoteMusicPresenter.getLyric();

                } else {
                    mRemoteMusicPresenter.cancelLyric();
                }

                //no need
                if (position == 3) {
                    equalizerUpdateDisplay();
                }

                Log.d(TAG, "onPageSelected() called with: position = [" + position + "] isShowingPList = [" + isShowingPList + " ] folderModeToggleTitle = [" + folderModeToggleTitle);
                if (isShowingPList && !folderModeToggleTitle.isEmpty() && position == LIST_PAGER_POSITION) {
                    showFolderModeToggle(folderModeToggleTitle);
                } else {
                    hideFolderModeToggle();
                }
            }
        });

        //pager music list
        viewLoading = (RelativeLayout) mListPager.findViewById(R.id.view_loading);
        textLoadingTotal = (TextView) mListPager.findViewById(R.id.text_loading_total);
        textLoadingProgress = (TextView) mListPager.findViewById(R.id.text_loading_progress);

        mMusicListView = (ListView) mListPager.findViewById(R.id.music_list);
        mMusicListAdapter = new MusicListAdapter(getActivity(), mPListEntryList);

        mMusicListView.setAdapter(mMusicListAdapter);
        mMusicListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                mRemoteMusicPresenter.selectMusic(mPListEntryList.get(position).index);
            }
        });

        musicFolderListView = (ListView) mListPager.findViewById(R.id.view_music_folders);
        musicFolderListAdapter = new MusicFolderListAdapter(getActivity(), musicFolderEntries);

        musicFolderListView.setAdapter(musicFolderListAdapter);
        musicFolderListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                mRemoteMusicPresenter.selectFolder(musicFolderEntries.get(position));
            }
        });

        mMusicTitleText = (TextView) mInfoPager.findViewById(R.id.musicTitleText);
        mMusicArtistText = (TextView) mInfoPager.findViewById(R.id.musicArtistText);
        mMusicAblumText = (TextView) mInfoPager.findViewById(R.id.musicAblumText);
        mMusicGenreText = (TextView) mInfoPager.findViewById(R.id.musicGenreText);
        mMusicMimeTypeText = (TextView) mInfoPager.findViewById(R.id.musicMimeTypeText);

        mLyricView = (LyricView) mLyricPager.findViewById(R.id.musicLyricView);

        mPagerList = new ArrayList<View>();
        mPagerList.add(mInfoPager);
        mPagerList.add(mListPager);
        mPagerList.add(mLyricPager);
        mPagerAdapter = new MusicPagerAdapter(mPagerList);
        mViewPager.setAdapter(mPagerAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG, "onResume!");
        // review current slidingmenu state
        if (mViewPager.getCurrentItem() != 0) {
            if (mSlidingMenu != null) {
                mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
            }
        }
        if (mResumeFlag) {
            //check if remote music content change
            Log.v(TAG, "onResume again getPListOrRemoteMusicFolders ");
            mRemoteMusicPresenter.getPListOrRemoteMusicFolders();
        }
        mResumeFlag = true;
        mRemoteMusicPresenter.getCurrentEntry();
        mRemoteMusicPresenter.startUpdateMusicProgress();
    }


    @Override
    public void onPause() {
        super.onPause();
        Log.v(TAG, "onPause!");
        if (mSlidingMenu != null) {
            mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        }
        mRemoteMusicPresenter.cancelLyric();
        mRemoteMusicPresenter.stopUpdateMusicProgress();
    }

    @Override
    public void onDestroyView() {
        Log.v(TAG, "onDestroyView!");
        super.onDestroyView();
        mRemoteMusicPresenter.onStop();
        hideLoading();
    }

    private void equalizerUpdateDisplay() {
        int[] level = mEqBandLevel.get(mEqPreset);

        for (int i = 0; i < level.length; i++) {
            mEqSeekBar[i].setProgressAndThumb(level[i] + 12);
        }

        if (mEqPreset == EQMode.USER) {
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

    @Override
    public void showFolderModeToggle(String name) {
        Log.d(TAG, "showFolderModeToggle() called with: name = [" + name + "]");
        folderModeToggleTitle = name;
        ((BrowserActivity) getActivity()).getSupportActionBar().setTitle(name);
        ((BrowserActivity) getActivity()).getSupportActionBar().setIcon(R.drawable.selector_navigate_back);
    }

    @Override
    public void hideFolderModeToggle() {
        Log.d(TAG, "hideFolderModeToggle() called");
        ((BrowserActivity) getActivity()).getSupportActionBar().setTitle(R.string.app_name);
        ((BrowserActivity) getActivity()).getSupportActionBar().setIcon(R.drawable.ic_launcher);
    }

    @Override
    public void updateLoopChanged(int loop) {
        if (loop == LoopMode.ALL) {
            mLoopModeButton.setImageResource(R.drawable.selector_loop_all_button);
        } else if (loop == LoopMode.SINGLE) {
            mLoopModeButton.setImageResource(R.drawable.selector_loop_single_button);
        } else if (loop == LoopMode.SHUFFLE) {
            mLoopModeButton.setImageResource(R.drawable.selector_loop_shuffle_button);
        } else {
            mLoopModeButton.setImageResource(R.drawable.selector_loop_all_button);
        }
    }

    @Override
    public void updateStateChanged(int state) {
        if (state == PlayState.PLAYING) {
            mPlayPauseButton.setImageResource(R.drawable.selector_pause_button);
        } else {
            mPlayPauseButton.setImageResource(R.drawable.selector_play_button);
        }
    }

    @Override
    public void showCurrentMusicEntryInfo(MusicEntry entry) {
        Log.d(TAG, "showCurrentMusicEntryInfo() called with: entry = [" + entry + "]");
        mMusicTitleText.setText(entry.title);
        mMusicArtistText.setText(entry.artist);
        mMusicAblumText.setText(entry.album);
        mMusicGenreText.setText(entry.genre);
        mMusicMimeTypeText.setText(entry.mimeType);
        mMusicNameText.setText(entry.title);
        mArtistNameText.setText(entry.artist);
    }

    @Override
    public void showPlayingMusic(final int musicIndex) {
        Log.d(TAG, "showPlayingMusic() called with: musicIndex = [" + musicIndex + "]");
        mMusicListAdapter.setSelectedPostion(mMusicListAdapter.getMusicPositionOfTheList(musicIndex));
        mMusicListAdapter.notifyDataSetChanged();

        if (mMusicListAdapter.getMusicPositionOfTheList(musicIndex) != MusicListAdapter.MUSIC_NOT_IN_THE_LIST) {
            mMusicListView.post(new Runnable() {
                @Override
                public void run() {
                    mMusicListView.setSelectionFromTop(mMusicListAdapter.getMusicPositionOfTheList(musicIndex), 100);
                }
            });
        }
    }

    @Override
    public void showPlayingFolder(final int position) {
        Log.d(TAG, "showPlayingFolder() called with: position = [" + position + "]");
        if (position < musicFolderListAdapter.getCount()) {
            musicFolderListAdapter.setPlayingPosition(position);
            musicFolderListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void showCurrentMusicDuration(int duration) {
        if (mSeekBar.getMax() != duration) {
            mDurationText.setText(Utils.showTime(duration));
            mSeekBar.setMax(duration);
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, com.actionbarsherlock.view.MenuInflater inflater) {
        inflater.inflate(R.menu.soundsetting_menu, menu);
        mMenu = menu;
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected() called with: item = [" + item + "]");
        mActivity.menuItemSelected(mMenu, item.getItemId());
        if (item.getItemId() == android.R.id.home) {
            if (mRemoteMusicPresenter != null) {
                mRemoteMusicPresenter.toggleFolderMode();
            }
        }
        return super.onOptionsItemSelected(item);
    }

//    private void sortMusicPList() {
//        List<PListEntry> mTempPListList = new ArrayList<PListEntry>();
//        SortName comparator = new SortName();
//        Collections.sort(mPListEntryList, comparator);
//        short[] list = new short[mPListEntryList.size()];
//        for (int i = 0; i < mPListEntryList.size(); i++) {
//            list[i] = (short) mPListEntryList.get(i).index;
//        }
//        mMusicManager.setPList(list);
//        for (short i : list) {
//            for (PListEntry pe : mPListEntryList) {
//                if (i == (short) pe.index) {
//                    mTempPListList.add(pe);
//                }
//            }
//        }
//        mPListEntryList.clear();
//        for (PListEntry pe : mTempPListList) {
//            mPListEntryList.add(pe);
//            mMusicListAdapter.notifyDataSetChanged();
//        }
//        storeMusicPListAndLength();
//        mMusicListView.setSelectionFromTop(mCurrentMusicEntry.index - 1, 200);
//    }


    public class MusicPagerAdapter extends PagerAdapter {
        private List<View> mListViews;

        public MusicPagerAdapter(List<View> listViews) {
            this.mListViews = listViews;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mListViews.get(position));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mListViews.get(position), 0);
            return mListViews.get(position);
        }

        @Override
        public int getCount() {
            return mListViews.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }
    }

    private class SortName implements Comparator<PListEntry> {
        Collator cmp = Collator.getInstance(java.util.Locale.CHINA);

        @Override
        public int compare(PListEntry o1, PListEntry o2) {
            if (cmp.compare(o1.name, o2.name) > 0) {
                return 1;
            } else if (cmp.compare(o1.name, o2.name) < 0) {
                return -1;
            }
            return 0;
        }
    }
}
