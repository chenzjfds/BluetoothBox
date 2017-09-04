package com.actions.bluetoothbox.ui.usbsound;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actions.bluetoothbox.R;
import com.actions.bluetoothbox.ui.BrowserActivity;
import com.actions.ibluz.manager.BluzManagerData.OnUSoundUIChangedListener;
import com.actions.ibluz.manager.BluzManagerData.USoundPlayState;
import com.actions.ibluz.manager.IUSoundManager;


public class USBSoundFragment extends SherlockFragment {
	private static final String TAG = "USBSoundFragment";

	private BrowserActivity mActivity;
	private View mMainView;
	private ImageButton mPlayStopButton;
	private ImageButton mPreButton;
	private ImageButton mNextButton;

	private IUSoundManager mUSoundManager;
	private int mPlayState;
	private Menu mMenu = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(TAG, "onCreate");
		setHasOptionsMenu(true);
		mActivity = (BrowserActivity) getActivity();
		if(mActivity.getIBluzManager()!=null) {
			mUSoundManager = mActivity.getIBluzManager().getUSoundManager(null);
			mUSoundManager.setOnUSoundUIChangedListener(mOnUSoundUIChangedListener);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.v(TAG, "onCreateView");
		mMainView = inflater.inflate(R.layout.fragment_usbsound, container, false);
		mPlayStopButton = (ImageButton) mMainView.findViewById(R.id.btn_playAndPause);
		mPreButton = (ImageButton) mMainView.findViewById(R.id.btn_playPre);
		mNextButton = (ImageButton) mMainView.findViewById(R.id.btn_playNext);
		mPreButton.setOnClickListener(mOnClickListener);
		mPlayStopButton.setOnClickListener(mOnClickListener);
		mNextButton.setOnClickListener(mOnClickListener);
		return mMainView;
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

	}

	private OnUSoundUIChangedListener mOnUSoundUIChangedListener = new OnUSoundUIChangedListener() {

		@Override
		public void onStateChanged(int state) {
//			Log.d(TAG, "OnUSoundUIChangedListener onStateChanged = " + state);
			mPlayState = state;
			if (mPlayState == USoundPlayState.PLAYING) {
				mPlayStopButton.setImageResource(R.drawable.ic_music_pause);
			} else {
				mPlayStopButton.setImageResource(R.drawable.ic_music_play);
			}
		}
	};

	private OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_playAndPause:
				if (mPlayState == USoundPlayState.PLAYING) {
					mUSoundManager.pause();
				} else {
					mUSoundManager.play();
				}
				break;
			case R.id.btn_playPre:
				mUSoundManager.previous();
				break;
			case R.id.btn_playNext:
				mUSoundManager.next();
				break;
			default:
				break;
			}
		}

	};

	@Override
	public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu, com.actionbarsherlock.view.MenuInflater inflater) {
		inflater.inflate(R.menu.soundsetting_menu, menu);
		mMenu = menu;
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
		mActivity.menuItemSelected(mMenu, item.getItemId());
		return super.onOptionsItemSelected(item);
	}
}
