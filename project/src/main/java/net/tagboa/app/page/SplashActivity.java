package net.tagboa.app.page;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import net.tagboa.app.R;

/**
 * 스플래시 스크린.
 * Created by Youngjae on 2014-06-22.
 */
public class SplashActivity extends Activity {
	// http://stackoverflow.com/questions/15452061/android-splash-screen?rq=1
	private static long SLEEP_TIME = 1000;    // Sleep for some time

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);    // Removes title bar

		setContentView(R.layout.activity_splash);

		// Start timer and launch main activity
		IntentLauncher launcher = new IntentLauncher();
		launcher.start();
	}

	/**
	 * 갤러리 앱에서 실행될 시.
	 */
	private class IntentLauncher extends Thread {
		Intent intent;
		String imgPath = null;

		public void run() {
			try {
				// Sleeping
				Thread.sleep(SLEEP_TIME);
			} catch (Exception e) {
				Log.e("bapul", e.getMessage());
			}

			// TODO: Youngjae (2014-06-22 15:17:25) : Share intent implementation.

			if (intent == null) {
				intent = new Intent(SplashActivity.this, MainActivity.class);
			}

			intent.putExtra("path", imgPath);
			SplashActivity.this.startActivity(intent);
			SplashActivity.this.finish();
		}
	}
}
