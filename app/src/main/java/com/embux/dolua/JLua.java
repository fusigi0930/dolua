package com.embux.dolua;

/**
 * Created by coder on 12/28/16.
 */

import android.app.Instrumentation;
import android.util.Log;


import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.PipedInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

import dalvik.system.DexClassLoader;


public class JLua {
	public static final String TAG="JDoLua";
	public static final String ACTION_RUN_LUA_FILE = "com.embux.dolua.action.RUN_LUA_FILE";
	public static final String ACTION_RUN_LUA_SCRIPT = "com.embux.dolua.action.RUN_LUA_SCRIPT";
	public static final String ACTION_STOP_ALL = "com.embux.dolua.action.STOP_ALL";
	public static final String EXTRA_PARAM = "PARAM";

	public static final String CONST_EVENT_KEY = "0";
	public static final String CONST_EVENT_MOUSE = "1";
	public static final String CONST_EVENT_TOUCH = "2";
	public static final String CONST_EVENT_CUSTOM = "3";

	public static final String CONST_EVENT_ACTION_DOWN = "0";
	public static final String CONST_EVENT_ACTION_UP = "1";
	public static final String CONST_EVENT_ACTION_CLICK = "2";
	public static final String CONST_EVENT_ACTION_RDOWN = "3";
	public static final String CONST_EVENT_ACTION_RUP = "4";
	public static final String CONST_EVENT_ACTION_RCLICK = "5";
	public static final String CONST_EVENT_ACTION_MOVE = "6";
	public static final String CONST_EVENT_ACTION_TOUCH = "7";
	public static final String CONST_EVENT_ACTION_SWIPE = "8";

	private long mLua = 0;

	private static DexClassLoader mJarLoader = null;
	private static Class<?> mInputClass = null;
	private static Object mInputObj = null;
	private static Method mMainFunc = null;

	private static final String LIB_INPUT = "/system/framework/input.jar";
	private static final String CLASS_INPUT = "com.android.commands.input.Input";
	private static final String MAIN_INPUT = "main";

	public String mDexPath;

	public JLua() {
		mLua=0;
	}

	public JLua(String dexPath) {
		mDexPath=dexPath;
		Log.i(TAG, "dex path: " + mDexPath);

		init();
	}

	public boolean init() {
		if (0 == mLua)
			mLua=initLua();

		try {
			Log.i(TAG, "dex path: " + mDexPath);
				mJarLoader=new DexClassLoader(LIB_INPUT, mDexPath, null, ClassLoader.getSystemClassLoader());
				mInputClass = mJarLoader.loadClass(CLASS_INPUT);
				mInputObj = (Object) mInputClass.newInstance();
				mMainFunc = mInputClass.getMethod(MAIN_INPUT, new Class<?>[] {String[].class});
		}

		catch(ClassNotFoundException e) {
			Log.e(TAG, e.toString());
		}
		catch(IllegalAccessException e) {
			Log.e(TAG, e.toString());
		}
		catch(InstantiationException e) {
			Log.e(TAG, e.toString());
		}
		catch(NoSuchMethodException e) {
			Log.e(TAG, e.toString());
		}

		return 0 == mLua ? false : true;
	}

	public void close() {
		closeLua(mLua);
	}

	public void runString(String str) {
		luaString(mLua, str);
	}

	public void runFile(String str) {
		final String szFile=str;
		new Runnable() {
			public void run() {
				Log.i(TAG, "runFile: " + szFile);
				luaFile(mLua, szFile);
			}
		}.run();
	}

	public static void nativeSendEvent(String [] arg) {
		Log.i(TAG, arg[0]);
		// argument 0: event type, key, mouse, touch
		// argument 1: event value
		// argument 2: event action
		// argument 3: event duration or repeat times

		if (CONST_EVENT_KEY.equals(arg[0])) {
			nativeSendKey(arg);
		}
		else if (CONST_EVENT_MOUSE.equals(arg[0])) {
			nativeSendMouse(arg);
		}
		else if (CONST_EVENT_TOUCH.equals(arg[0])) {
			nativeSendTouch(arg);
		}
		else if (CONST_EVENT_CUSTOM.equals(arg[0])) {

		}
	}

	// private functions
	private static void nativeSendKey(String [] arg) {
		// argument 1: event value
		// argument 2: event action
		// argument 3: event duration or repeat times

		Log.i(TAG, "send_event key: " + arg[1]);
	}

	private static void nativeSendMouse(String [] arg) {
		Log.i(TAG, "send_event mouse");
	}

	private static void nativeSendTouch(String [] arg) {
		// argument 1: event x
		// argument 2: event y
		// argument 3: event action
		// argument 4: event duration or repeat times
		// or argument 4: x2, argument 5: y2, argument 6: duration
		Log.i(TAG, "send_event touch argument count: " + arg.length + " content: " + Arrays.toString(arg));

		Object[] sendObj=new Object[1];

		switch(arg[3]) {
			case CONST_EVENT_ACTION_CLICK:
			case CONST_EVENT_ACTION_TOUCH:
				if (arg.length != 5) break;

				sendObj[0] = new String[] {
					"tap", arg[1], arg[2]
				};

				try {
					if (mMainFunc != null) {
						for (int i=0; i < Integer.valueOf(arg[4]); i++) {
							mMainFunc.invoke(mInputObj, sendObj);
							Thread.sleep(150, 0);
						}
					}
				}
				catch(IllegalAccessException e) {
					Log.e(TAG, e.toString());
				}
				catch(InvocationTargetException e) {
					Log.e(TAG, e.toString());
				}
				catch(InterruptedException e) {
					Log.e(TAG, e.toString());
				}
				break;
			case CONST_EVENT_ACTION_SWIPE:
			case CONST_EVENT_ACTION_MOVE:
				if (arg.length != 7) break;

				sendObj[0] = new String[] {
						"swipe", arg[1], arg[2], arg[4], arg[5], arg[6]
				};

				try {
					if (mMainFunc != null) {
						mMainFunc.invoke(mInputObj, sendObj);
					}
				}
				catch(IllegalAccessException e) {
					Log.e(TAG, e.toString());
				}
				catch(InvocationTargetException e) {
					Log.e(TAG, e.toString());
				}
				break;
			default:
				Log.e(TAG, "no!!");
				return;
		}

		/*
		try {
			Process p = Runtime.getRuntime().exec("/system/bin/sh");
			DataOutputStream sout = new DataOutputStream(p.getOutputStream());
			sout.writeBytes("input tap 200 200\n");
			sout.flush();

		}
		catch(IOException e) {
			Log.e(TAG, e.toString());
		}
		/*
		catch(InterruptedException e) {
			Log.e(TAG, e.toString());
		}
		*/
	}

	// native functions
	private native long initLua();
	private native void closeLua(long lua);
	private native void luaString(long lua, String str);
	private native void luaFile(long lua, String str);
}
