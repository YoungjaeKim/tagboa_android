package net.tagboa.app.page;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;
import net.tagboa.app.R;

/**
 * 스플래시 스크린.
 * Created by Youngjae on 2014-06-22.
 */
public class SplashActivity extends Activity {
	// http://stackoverflow.com/questions/15452061/android-splash-screen?rq=1
	private static long SLEEP_TIME = 1000;    // Sleep for some time
    private android.widget.TextView textViewBottom;
    private String appVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);    // Removes title bar

        setContentView(R.layout.activity_splash);
        this.textViewBottom = (TextView) findViewById(R.id.textViewBottom);

        // 버전명 표시.
        textViewBottom.setText("version " + getAppVersionName());

        // Start timer and launch main activity
        IntentLauncher launcher = new IntentLauncher();
        launcher.start();
    }

    /**
     * 버전 가져오기.
     * @return Android versionName
     */
    public String getAppVersionName() {
        PackageInfo pi;
        try {
            pi = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "0.0";
        }

        return pi.versionName;
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
				intent = new Intent(SplashActivity.this, HomeActivity.class);
			}

			intent.putExtra("path", imgPath);
			SplashActivity.this.startActivity(intent);
			SplashActivity.this.finish();
		}
	}
}
