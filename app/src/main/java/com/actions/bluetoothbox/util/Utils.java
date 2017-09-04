package com.actions.bluetoothbox.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import java.util.Random;

public class Utils {
	static private Toast sToast = null;

	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	public static Point screenSize(Context ctx) {
		WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		if (android.os.Build.VERSION.SDK_INT >= 13) {
			display.getSize(size);
		} else {
			size.x = display.getWidth();
			size.y = display.getHeight();
		}
		return size;
	}

	public static float screenDensity(Context ctx) {
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		display.getMetrics(dm);
		return dm.density;
	}

	public static String showTime(int time) {
		// make ms change to s
		time /= 1000;
		int hour;
		int minute;
		int second;
		if (time >= 3600) {
			hour = time / 3600;
			minute = (time % 3600) / 60;
			second = (time % 3600) % 60;// time-hour*3600-minute*60
			return String.format("%02d:%02d:%02d", hour, minute, second);
		} else {
			minute = time / 60;
			second = time % 60;
			return String.format("%02d:%02d", minute, second);
		}

	}

	public static int generateRandom(int num, int index) {
		Random ran = new Random();
		int ranNum = ran.nextInt(num);
		if (num > 0) {
			if (ranNum == index) {
				ranNum = generateRandom(num, index);
			}
		}
		return ranNum;
	}

	public static void setAlphaForView(View v, float alpha) {
		AlphaAnimation animation = new AlphaAnimation(alpha, alpha);
		animation.setDuration(0);
		animation.setFillAfter(true);
		v.startAnimation(animation);
	}

	public static void displayToast(Context context,int resId) {
		if (sToast == null) {
			sToast = Toast.makeText(context , resId, Toast.LENGTH_SHORT);
		} else {
			sToast.setText(resId);
		}
		sToast.show();
	}

	public static boolean[] checkExternalStorageAvailable() {
		boolean mExternalStorageReadable = false;
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			mExternalStorageReadable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			mExternalStorageReadable = true;
			mExternalStorageWriteable = false;
		} else if (Environment.MEDIA_REMOVED.equals(state)) {
			// For Android 4.1 ,in case primary storage is sdcard but null
			mExternalStorageReadable = true;
			mExternalStorageWriteable = true;
		} else {
			// Something else is wrong. It may be one of many other states, but
			// all we need
			// to know is we can neither read nor write
			mExternalStorageReadable = mExternalStorageWriteable = false;
		}
		boolean[] rwstate = { mExternalStorageReadable, mExternalStorageWriteable };
		return rwstate;
	}

	public static boolean hasExternalStoragePrivateFile(Context context,String filename) {
		if (checkExternalStorageAvailable()[0]) {
			File file = new File(getPrivateFilePath(context,filename));
			return file.exists();
		} else {
			Log.w("ExternalStorage", "Error reading");
			return false;
		}
	}

	public static String getPrivateFilePath(Context context, String filename) {
		File cardFile = Environment.getExternalStorageDirectory();
		if (!cardFile.canWrite()) {
			// We can only read the media
			String[] paths;
			String extSdCard = null;
			try {
				StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
				paths = (String[]) sm.getClass().getMethod("getVolumePaths",(Class[]) null).invoke(sm,(Object[]) null);
				String esd = Environment.getExternalStorageDirectory().getPath();
				for (int i = 0; i < paths.length; i++) {
					if (paths[i].equals(esd)) {
						continue;
					}
					File sdFile = new File(paths[i]);
					if (sdFile.canWrite()) {
						extSdCard = paths[i];
					}
				}
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
			}
            //can't getExternalDirectory and internal wasn' mount, use getFilesDir() for example -> /data/data/com.actions.bluetoothbox/files
            if (extSdCard == null) {
                extSdCard = context.getFilesDir().getAbsolutePath();
                return extSdCard + "/" + filename;
            }
			File mFile = new File(extSdCard + "/Android/data/" + context.getPackageName()+"/files");
			if (!mFile.isDirectory()) {
				mFile.mkdirs();
			}
			return extSdCard + "/Android/data/" + context.getPackageName()+ "/files/" + filename;
		} else {
			
			return context.getExternalFilesDir(null) + "/" + filename;
		}

	}

	public static void createExternalStoragePrivateFile(Context context,String filename, byte[] buffer) {
		if (checkExternalStorageAvailable()[1]) {
			File file = new File(getPrivateFilePath(context,filename));
			OutputStream os = null;
			try {
				os = new FileOutputStream(file);
				if (buffer != null) {
					os.write(buffer);
				}
			} catch (IOException e) {
				Log.w("ExternalStorage", "Error writing " + file, e);
			} finally {
				try {
					if (os!=null) {
						os.close();
					}
				} catch (IOException e) {
					Log.w("ExternalStorage", "Error writing " + file, e);
				}
			}
		}
	}

	public static void appendExternalStoragePrivateFile(Context context,String filename, byte[] buffer) {
		if (checkExternalStorageAvailable()[1]) {
			File file = new File(getPrivateFilePath(context, filename));
			OutputStream os = null;
			try {
				os = new FileOutputStream(file, true);
				os.write(buffer);
			} catch (IOException e) {
				Log.w("ExternalStorage", "Error writing " + file, e);
			} finally {
				try {
					if (os!=null) {
						os.close();
					}
				} catch (IOException e) {
					Log.w("ExternalStorage", "Error writing " + file, e);
				}
			}
		}
	}

	// 复制文件
	public static void copyFile(File sourceFile, File targetFile) throws IOException {
		BufferedInputStream inBuff = null;
		BufferedOutputStream outBuff = null;
		try {
			inBuff = new BufferedInputStream(new FileInputStream(sourceFile));
			outBuff = new BufferedOutputStream(new FileOutputStream(targetFile));
			byte[] b = new byte[1024];
			int len;
			while ((len = inBuff.read(b)) != -1) {
				outBuff.write(b, 0, len);
			}
			outBuff.flush();
		} finally {
			// 关闭
			if (inBuff != null)
				inBuff.close();
			if (outBuff != null)
				outBuff.close();
		}
	}

	public static class LittleEndian {
		public static int ByteArrayToInt(byte[] b) {
			return ByteArrayToInt(b, 0);
		}

		public static int ByteArrayToInt(byte[] b, int index) {
			return ((b[index + 3] & 0xff) << 24) + ((b[index + 2] & 0xff) << 16) + ((b[index + 1] & 0xff) << 8) + (b[index] & 0xff);
		}

		public static short ByteArrayToShort(byte[] b) {
			return ByteArrayToShort(b, 0);
		}

		public static short ByteArrayToShort(byte[] b, int index) {
			return (short) (((b[index + 1] & 0xff) << 8) + (b[index] & 0xff));
		}

		public static void fillByteArrayInt(byte[] b, int index, int value) {
			b[index++] = (byte) ((value >> 0) & 0xff);
			b[index++] = (byte) ((value >> 8) & 0xff);
			b[index++] = (byte) ((value >> 16) & 0xff);
			b[index++] = (byte) ((value >> 24) & 0xff);
		}

		public static void fillByteArrayShort(byte[] b, int index, short value) {
			b[index++] = (byte) ((value >> 0) & 0xff);
			b[index++] = (byte) ((value >> 8) & 0xff);
		}

		public static boolean[] getBooleanArray(byte b) {
			boolean[] array = new boolean[8];
			for (int i = 7; i >= 0; i--) {
				array[i] = (b & 1) == 1;
				b = (byte) (b >> 1);
			}
			return array;
		}

		public static byte BitToByte(boolean[] array) {
			if (array != null && array.length > 0) {
				byte b = 0;
				for (int i = 0; i < Math.min(array.length, 8); i++) {
					if (array[i]) {
						b += (1 << i);
					}
				}
				return b;
			}
			return 0;
		}
	}

	public static String byte2HexStr(byte[] b, int iLen) {
		final char[] mChars = "0123456789ABCDEF".toCharArray();
		StringBuilder sb = new StringBuilder();
		for (int n = 0; n < iLen; n++) {
			sb.append(mChars[(b[n] & 0xFF) >> 4]);
			sb.append(mChars[b[n] & 0x0F]);
			sb.append(' ');
		}
		return sb.toString().trim().toUpperCase(Locale.US);
	}
}
