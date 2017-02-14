package com.sotaku.dolua;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.util.Log;

import java.util.Iterator;
import java.util.List;

/**
 * Created by coder on 1/23/17.
 */

public class FloatingControlService extends Service implements View.OnTouchListener, View.OnClickListener{
	public static final String TAG="LuaFloating";

	private WindowManager m_wm;
	private ImageButton m_overlayButton;
	private int m_buttonX = 0;
	private int m_buttonY = 0;
	private float m_offsetX;
	private float m_offsetY;
	private boolean m_buttonMoving = false;
	private boolean m_bIsScriptRunning = false;

	private Intent m_luaIntent = null;

	public String m_szLuaFile;

	public static final String FINISH_RUN_PROCEDURE = "com.sotaku.dolua.action.FCServiceFinishRunningProcedure";

	public class FCBinder extends Binder {
		public FloatingControlService getService() {
			return FloatingControlService.this;
		}
	}

	private FCBinder m_binder = new FCBinder();

	private LuaService m_currentLuaService = null;

	private ServiceConnection m_servConn = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			if (LuaService.class.getName().equals(name.getClassName())) {
				LuaService.LSBinder lsBinder = (LuaService.LSBinder) service;
				m_currentLuaService = lsBinder.getService();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			m_servConn = null;
		}
	};

	@Override
	public void onCreate() {
		Log.i(TAG, "onCreate");
		super.onCreate();
		m_wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		m_overlayButton = new ImageButton(this);
		m_overlayButton.setImageResource(android.R.drawable.ic_media_play);
		m_overlayButton.setBackgroundColor(Color.TRANSPARENT);
		m_overlayButton.setOnTouchListener(this);
		m_overlayButton.setOnClickListener(this);
		m_overlayButton.setAlpha(0.5f);

		WindowManager.LayoutParams layoutParam = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
																				WindowManager.LayoutParams.WRAP_CONTENT,
																				WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
																				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
																				PixelFormat.TRANSLUCENT);

		layoutParam.gravity = Gravity.LEFT | Gravity.TOP;
		layoutParam.x = m_buttonX;
		layoutParam.y = m_buttonY;
		m_wm.addView(m_overlayButton, layoutParam);

		IntentFilter ifilter = new IntentFilter(FINISH_RUN_PROCEDURE);
		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Log.i(TAG, "FCService receive: " + intent.getAction());
				if (FINISH_RUN_PROCEDURE.equals(intent.getAction())) {
					m_bIsScriptRunning = false;
					m_overlayButton.setImageResource(android.R.drawable.ic_media_play);
				}
			}
		}, ifilter);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "onStartCommand");
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "onDestroy");
		super.onDestroy();

		if (m_overlayButton != null && m_wm != null) {
			m_wm.removeView(m_overlayButton);
			m_overlayButton = null;
		}
		unbindService(m_servConn);
	}

	@Override
	public IBinder onBind(Intent i) {
		return m_binder;
	}

	@Override
	public void onClick(View v) {
		Log.i(TAG, "onClick");
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
			default:
				Log.i(TAG, "not process action: " + event.toString());
				break;
			case MotionEvent.ACTION_DOWN:
			{
				float x = event.getRawX();
				float y = event.getRawY();
				m_buttonMoving = false;

				int[] location = new int[2];
				m_overlayButton.getLocationOnScreen(location);
				m_buttonX = location[0];
				m_buttonY = location[1];

				m_offsetX = m_buttonX - x;
				m_offsetY = m_buttonY - y;

				Log.i(TAG, "ox: " + m_offsetX + ", oy: " + m_offsetY);

			} 	break;
			case MotionEvent.ACTION_UP:
			{
				if (m_buttonMoving) {
					Log.i(TAG, "stop moving");
				}
				else {
					Log.i(TAG, "running lua script: " + m_szLuaFile);
					if (!m_bIsScriptRunning) {
						m_luaIntent = new Intent(getApplicationContext(), LuaService.class);
						m_luaIntent.setAction(JLua.ACTION_RUN_LUA_FILE);
						m_luaIntent.putExtra(JLua.EXTRA_PARAM, m_szLuaFile);

						startService(m_luaIntent);
						bindService(m_luaIntent, m_servConn, Context.BIND_AUTO_CREATE);
						m_overlayButton.setImageResource(android.R.drawable.ic_media_pause);
						m_bIsScriptRunning = true;
					}
					else {
						m_bIsScriptRunning = false;
						if (m_currentLuaService != null) {
							m_currentLuaService.setLuaFlag(JLua.CONST_FLAG_EXIT, 1);
							//m_currentLuaService.closeLua();
						}

						m_overlayButton.setImageResource(android.R.drawable.ic_media_play);
					}
				}
				m_buttonMoving = false;
			}	break;
			case MotionEvent.ACTION_MOVE:
				float x = event.getRawX();
				float y = event.getRawY();

				WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) m_overlayButton.getLayoutParams();

				int newX = (int) (m_offsetX + x);
				int newY = (int) (m_offsetY + y);

				if (Math.abs(newX - m_buttonX) < 1 && Math.abs(newY - m_buttonY) < 1 && !m_buttonMoving) {
					return false;
				}

				layoutParams.x = newX;
				layoutParams.y = newY;

				m_wm.updateViewLayout(m_overlayButton, layoutParams);
				m_buttonMoving = true;
				break;
		}
		return true;
	}
}
