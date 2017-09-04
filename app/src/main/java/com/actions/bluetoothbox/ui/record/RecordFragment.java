package com.actions.bluetoothbox.ui.record;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actions.bluetoothbox.R;
import com.actions.bluetoothbox.ui.BrowserActivity;
import com.actions.ibluz.manager.BluzManagerData.OnRecordUIChangedListener;
import com.actions.ibluz.manager.BluzManagerData.RECStatus;
import com.actions.ibluz.manager.IRecordManager;

import java.text.DecimalFormat;


public class RecordFragment extends SherlockFragment {
	private static final String TAG = "RecordFragment";

	private BrowserActivity mActivity;
	private View mMainView;
	private TextView mRecordTimeTv;
	private ImageButton mStartBtn;
	private ImageButton mStopBtn;
	private ImageButton mPauseBtn;

	private IRecordManager mRecordManager;
	private Menu mMenu = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(TAG, "onCreate");
		setHasOptionsMenu(true);
		mActivity = (BrowserActivity) getActivity();
		if(mActivity.getIBluzManager()!=null) {
			mRecordManager = mActivity.getIBluzManager().getRecordManager(null);
			mRecordManager.setOnRecordUIChangedListener(mOnAuxUIChangedListener);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.v(TAG, "onCreateView");
		mMainView = inflater.inflate(R.layout.fragment_record, container, false);
		mRecordTimeTv = (TextView) mMainView.findViewById(R.id.rec_time);
		mStartBtn = (ImageButton) mMainView.findViewById(R.id.rec_start);
		mStopBtn = (ImageButton) mMainView.findViewById(R.id.rec_stop);
		mPauseBtn = (ImageButton) mMainView.findViewById(R.id.rec_pause);
		mStartBtn.setOnClickListener(mOnClickListener);
		mStopBtn.setOnClickListener(mOnClickListener);
		mPauseBtn.setOnClickListener(mOnClickListener);
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

	private OnRecordUIChangedListener mOnAuxUIChangedListener = new OnRecordUIChangedListener() {

		@Override
		public void onRecordTimeChanged(int hour, int minute, int second) {
			DecimalFormat df = new DecimalFormat("00");
			String recordTime = df.format(hour) + ":" + df.format(minute) + ":" + df.format(second);
			mRecordTimeTv.setText(recordTime);
		}

		@Override
		public void onStateChanged(int state) {
			updateStateChanged(state);
		}
	};

	private OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.rec_start:
				mRecordManager.recStart();
				break;
			case R.id.rec_stop:
				mRecordManager.recStop();
				break;
			case R.id.rec_pause:
				mRecordManager.recPause();
				break;
			default:
				break;
			}
		}

	};

	private void updateStateChanged(int state) {
		switch (state) {
		case RECStatus.PLAY:
			mStartBtn.setImageResource(R.drawable.ic_record_start_l);
			mPauseBtn.setImageResource(R.drawable.ic_record_pause_h);
			mStartBtn.setEnabled(false);
			mPauseBtn.setEnabled(true);
			break;
		case RECStatus.PAUSE:
			mStartBtn.setImageResource(R.drawable.ic_record_start_h);
			mPauseBtn.setImageResource(R.drawable.ic_record_pause_l);
			mPauseBtn.setEnabled(false);
			mStartBtn.setEnabled(true);
			break;
		case RECStatus.STOP:
			mStartBtn.setImageResource(R.drawable.ic_record_start_h);
			mPauseBtn.setImageResource(R.drawable.ic_record_pause_l);
			mPauseBtn.setEnabled(false);
			mStartBtn.setEnabled(true);
			break;
		}
	}

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
