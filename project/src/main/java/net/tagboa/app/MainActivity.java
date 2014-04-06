package net.tagboa.app;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockActivity;
import com.csvreader.CsvReader;
import net.tagboa.app.model.VideoItem;
import net.tagboa.app.net.FileDownloadTask;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class MainActivity extends SherlockActivity {
	private static final String TAG = "MainActivity";
	public static String ApplicationName = "TagBoa";
	public static ArrayList<VideoItem> VideoItems;

	// Youngjae (2014-04-06 12:25:03) : 구글에서 직접 다운로드받는 것은 실패. 참고: http://stackoverflow.com/a/2845257/361100
//	public static String DataUrlKey = "0AgSMns-6evXtdHhfa0tSbkF6UmM3RkNmNDJRbVNpcUE";
//	public static String DataWebUrl = "https://docs.google.com/spreadsheet/ccc?key=%s&usp=drive_web#gid=0";
//	public static String DataCSVUrl = "https://docs.google.com/feeds/download/spreadsheets/Export?key=%s&exportFormat=csv&gid=0";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// 데이터 로딩.
		try {
			InputStream inputStream = getResources().openRawResource(R.raw.data);
			byte[] b = new byte[inputStream.available()];
			inputStream.read(b);

			// http://www.csvreader.com/java_csv_samples.php
			CsvReader item = new CsvReader(inputStream, Charset.forName("utf8"));
			item.readHeaders();

			VideoItems = new ArrayList<VideoItem>();
			while (item.readRecord()) {
				VideoItems.add(new VideoItem(item.get(0), item.get(1), item.get(2), item.get(3), item.get(4)));
			}
			item.close();
		} catch (IOException e) {
			MainActivity.ShowToast(MainActivity.this, e.getMessage());
			e.printStackTrace();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		else if (id == R.id.action_download) {

			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * 토스트 메시지.
	 *
	 * @param context 메시지 표출할 콘텍스트(액티비티)
	 * @param message 토스트로 보일 메시지.
	 */
	public static void ShowToast(Context context, String message) {
		ShowToast(context, message, false);
	}

	/**
	 * 토스트 메시지.
	 *
	 * @param context
	 * @param message
	 * @param isCenter
	 */
	public static void ShowToast(Context context, String message, boolean isCenter) {
		Toast toast = Toast.makeText(context, context.getString(R.string.app_name) + message, Toast.LENGTH_SHORT);
		if (isCenter)
			toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	/**
	 * 업로드 완료 시 이벤트 발생.
	 */
	protected FileDownloadTask.OnResponseListener onResponseListener = new FileDownloadTask.OnResponseListener() {
		public static final String TAG = "FileDownloadTask.OnResponseListener";

		public void onSuccess() {
			Log.d(TAG, "download success");
		}

		public void onFailure(String message) {
			MainActivity.ShowToast(MainActivity.this, message);
		}
	};

}
