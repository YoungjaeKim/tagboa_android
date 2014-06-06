package net.tagboa.app;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.androidquery.AQuery;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.tokenautocomplete.FilteredArrayAdapter;
import com.tokenautocomplete.TokenCompleteTextView;
import net.tagboa.app.model.TagboaItem;
import net.tagboa.app.model.TagboaTag;
import net.tagboa.app.model.VideoItem;
import net.tagboa.app.net.FileDownloadTask;
import net.tagboa.app.net.TagboaApi;
import net.tagboa.app.view.TagCompletionView;
import org.apache.http.Header;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class MainActivity extends BaseActivity implements View.OnClickListener, TagCompletionView.TokenListener  {
	private static final String TAG = "MainActivity";
	public static String ApplicationName = "TagBoa";
	public static String BaseUrl = "app.tagboa.net";
	public static String BaseRestUrl = "app.tagboa.net/api";
	public static ArrayList<VideoItem> VideoItems;
	AQuery aq = new AQuery(this);

	TagCompletionView completionView;
	TagboaTag[] tagboaTags;
	ArrayAdapter<TagboaTag> adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);


		tagboaTags = new TagboaTag[]{
				new TagboaTag("Marshall Weir"),
				new TagboaTag("Margaret Smith"),
				new TagboaTag("Max Jordan"),
				new TagboaTag("Meg Peterson"),
				new TagboaTag("Amanda Johnson"),
				new TagboaTag("Terry Anderson")
		};

		adapter = new FilteredArrayAdapter<TagboaTag>(this, android.R.layout.simple_list_item_1, tagboaTags) {
			@Override
			protected boolean keepObject(TagboaTag obj, String mask) {
				mask = mask.toLowerCase();
				return obj.title.toLowerCase().contains(mask);
			}
		};

		completionView = (TagCompletionView)findViewById(R.id.searchView);
		completionView.allowDuplicates(false);
		completionView.setTokenClickStyle(TokenCompleteTextView.TokenClickStyle.Delete);
		completionView.setAdapter(adapter);
		completionView.setTokenListener(this);

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
		Toast toast = Toast.makeText(context, String.format("%s: %s", context.getString(R.string.app_name), message), Toast.LENGTH_SHORT);
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

	private String _token;
	private String _username;

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.buttonLogin: {
				// 로그인 테스트.
				TagboaApi.Login(MainActivity.this, "tester", "tester", new JsonHttpResponseHandler() {
					@Override
					public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
						super.onSuccess(statusCode, headers, response);
						try {
							_token = response.getString("access_token");
							_username = response.getString("userName");
							_sharedPrefs.edit().putString("Authentication", response.toString()).commit();
							TagboaApi.InitializeHttpClient(MainActivity.this);
							MainActivity.ShowToast(MainActivity.this, String.format("%s 로그인", _username));
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}

					@Override
					public void onFailure(int statusCode, Throwable e, JSONObject errorResponse) {
						super.onFailure(statusCode, e, errorResponse);
						if (statusCode == 400) {
							MainActivity.ShowToast(MainActivity.this, "아이디 또는 비밀번호가 틀렸습니다.");
						}
					}
				});

			}
			break;
			case R.id.buttonGet: {
				try {
					TagboaApi.GetItems(MainActivity.this, _username, new JsonHttpResponseHandler() {
						@Override
						public void onSuccess(JSONArray response) {
							super.onSuccess(response);
							MainActivity.ShowToast(MainActivity.this, String.valueOf(response.length()) + "개 있습니다.");
						}

						@Override
						public void onFailure(Throwable e, JSONObject errorResponse) {
							super.onFailure(e, errorResponse);
							MainActivity.ShowToast(MainActivity.this, "조회 실패");
						}
					});
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			break;
			case R.id.buttonAddTag: {
				try {
					TagboaItem item = new TagboaItem();
					item.Title = "테스트";
					item.Genre = "다큐";
					item.Author = _username;
					item.Rating = 0.0d;
					item.Description = "설명";
					item.ReadCount = 0;
					item.Timestamp = DateTime.now();

					TagboaApi.PostItem(MainActivity.this, item, new JsonHttpResponseHandler() {

						@Override
						public void onSuccess(JSONObject response) {
							super.onSuccess(response);
						}

						@Override
						public void onSuccess(JSONArray response) {
							super.onSuccess(response);
						}

						@Override
						public void onFailure(Throwable e, JSONObject errorResponse) {
							super.onFailure(e, errorResponse);
						}
					});
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}

			break;
		}
	}


	private void updateTokenConfirmation() {
		StringBuilder sb = new StringBuilder("Current tokens:\n");
		for (Object token: completionView.getObjects()) {
			sb.append(token.toString());
			sb.append("\n");
		}

		((TextView)findViewById(R.id.tokens)).setText(sb);
	}


	@Override
	public void onTokenAdded(Object token) {
		updateTokenConfirmation();
		System.out.println();
	}


	@Override
	public void onTokenRemoved(Object token) {
		updateTokenConfirmation();
	}

}
