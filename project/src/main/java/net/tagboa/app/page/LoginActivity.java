package net.tagboa.app.page;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.EditText;
import android.widget.TextView;
import com.loopj.android.http.JsonHttpResponseHandler;
import net.tagboa.app.BaseActivity;
import net.tagboa.app.R;
import net.tagboa.app.RegisterFacebookActivity;
import net.tagboa.app.net.TagboaApi;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * 로그인 액티비티
 * Created by Youngjae on 2014-06-22.
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener {
	public static final int REQUEST_REGISTER = 101;
	private static final int REQUEST_PASSWORD_RESET = 102;
	private static final int REQUEST_REGISTER_FACEBOOK = 103;

	private EditText _editTextUserId;
	private EditText _editTextPassword;
	private TextView _textViewBottomLine;
	private String _token;
	private String _username;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_Tagboa); //Used for theme switching in samples
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setLogo(R.drawable.ic_launcher);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		_editTextUserId = (EditText) findViewById(R.id.editId);
		_editTextPassword = (EditText) findViewById(R.id.editPw);
		_textViewBottomLine = (TextView) findViewById(R.id.textViewLoginBottomLine);

		// 기존 로그인 정보 표시.
		_editTextUserId.setText(_sharedPrefs.getString("userId", ""));

		// 웹뷰 쿠키 삭제.
		CookieSyncManager.createInstance(getApplicationContext());
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.removeAllCookie();
		TagboaApi.BapulCookieStore.clear();

		// 사용자 셋팅 정보 삭제.
		SharedPreferences.Editor editor = _sharedPrefs.edit();
		if (editor != null) {
			editor.clear();
			editor.commit();
		}

		_editTextUserId.requestFocus();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK)
			return;
		switch (requestCode) {
			case REQUEST_REGISTER_FACEBOOK: {
				try {
					JSONObject response = new JSONObject(data.getStringExtra("result"));
					_token = response.getString("access_token");
					_username = response.getString("userName");
					TagboaApi.InitializeHttpClient(LoginActivity.this);
					TestActivity.ShowToast(LoginActivity.this, String.format("%s 로그인", _username));
				} catch (JSONException e) {
					e.printStackTrace();
				}
				try {
					TagboaApi.GetItems(LoginActivity.this, _username, null, new JsonHttpResponseHandler() {
						@Override
						public void onSuccess(JSONArray response) {
							super.onSuccess(response);
							TestActivity.ShowToast(LoginActivity.this, String.valueOf(response.length()) + "개 있습니다.");
						}

						@Override
						public void onFailure(Throwable e, JSONObject errorResponse) {
							super.onFailure(e, errorResponse);
							TestActivity.ShowToast(LoginActivity.this, "조회 실패");
						}

						@Override
						public void onFinish() {
							super.onFinish();
						}
					});
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			break;
			// 회원가입 후 처리.
			case REQUEST_REGISTER: {
				// 회원가입 완료 후 바로 로그인.
				String u = data.getExtras().getString("principal");
				String p = data.getExtras().getString("password");
				TagboaApi.Login(LoginActivity.this, u, p, new LoginCookieJsonHttpResponseHandler());
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.btnLogin: {
				String id = null;
				String password = null;
				if (_editTextUserId != null)
					id = _editTextUserId.getText().toString();
				if (_editTextPassword != null)
					password = _editTextPassword.getText().toString();
				try {
					TagboaApi.Login(LoginActivity.this, id, password, new LoginCookieJsonHttpResponseHandler());
				} catch (IllegalArgumentException e) {
					TestActivity.ShowToast(LoginActivity.this, e.getMessage(), true);
				}
			}
			break;
			case R.id.textViewLoginPrivacyPolicy: {
				Intent intent = new Intent(LoginActivity.this, PrivacyPolicyActivity.class);
				intent.putExtra("raw", "privacy_policy");
				intent.putExtra("title", getString(R.string.titlePrivacyPolicy));
				startActivity(intent);
			}
			break;
			case R.id.buttonLoginFacebook: {
				Intent intent = new Intent(LoginActivity.this, RegisterFacebookActivity.class);
				startActivityForResult(intent, REQUEST_REGISTER_FACEBOOK);
			}
			break;
		}
	}

	/**
	 * 로그인 결과값 처리.
	 * Youngjae (2013-11-03 01:50:28) : 작업 중.
	 * 문제점 1. field.required.tokenModel.channel 처리 부분에 문제가 있음.
	 * 문제점 2. 최초에는 success로 됨.
	 */
	private class LoginCookieJsonHttpResponseHandler extends JsonHttpResponseHandler {
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            super.onSuccess(statusCode, headers, response);
            try {
                _token = response.getString("access_token");
                _username = response.getString("userName");
                _sharedPrefs.edit().putString("Authentication", response.toString()).commit();
                TagboaApi.InitializeHttpClient(LoginActivity.this);
                setResult(RESULT_OK);
                finish();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(int statusCode, Throwable e, JSONObject errorResponse) {
            super.onFailure(statusCode, e, errorResponse);
            if (statusCode == 400) {
                TestActivity.ShowToast(LoginActivity.this, getString(R.string.errorIncorrectIdOrPassword));
            }
        }
	}
}
