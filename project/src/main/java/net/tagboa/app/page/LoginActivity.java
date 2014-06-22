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
import net.tagboa.app.net.TagboaApi;
import org.apache.http.auth.AuthenticationException;
import org.json.JSONException;
import org.json.JSONObject;

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
			// 회원가입 후 처리.
			case REQUEST_REGISTER: {
				// 회원가입 완료 후 바로 로그인.
				String u = data.getExtras().getString("principal");
				String p = data.getExtras().getString("password");
				TagboaApi.Login(LoginActivity.this, u, p, new LoginCookieJsonHttpResponseHandler(u, p));
			}
			break;
			case REQUEST_REGISTER_FACEBOOK: {
				// 페이스북 가입 성공.
				if (data != null) {
					setResult(RESULT_OK, data);
					LoginActivity.this.finish();
				}
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
					TagboaApi.ProcessLogin(LoginActivity.this, id, password, new LoginCookieJsonHttpResponseHandler(id, password));
				} catch (IllegalArgumentException e) {
					MainActivity.ShowToast(LoginActivity.this, TagboaApi.getLabel(LoginActivity.this, e.getMessage()), true);
				}
			}
			break;
			case R.id.buttonLoginRegister: {
				Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
				startActivityForResult(intent, REQUEST_REGISTER);
			}
			break;
			case R.id.textViewLoginFindPassword: {
				Intent intent = new Intent(LoginActivity.this, PasswordResetActivity.class);
				startActivityForResult(intent, REQUEST_PASSWORD_RESET);
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
	 * Basic Authentication 기반 로그인 결과값 처리.
	 *
	 * @deprecated 쿠키방식으로 교체 후 사용 안함.
	 */
	private class LoginBasicAuthJsonHttpResponseHandler extends JsonHttpResponseHandler {
		private final String id;
		private final String password;

		public LoginBasicAuthJsonHttpResponseHandler(String id, String password) {
			this.id = id;
			this.password = password;
		}

		@Override
		public void onSuccess(JSONObject jsonObject) {
			try {
				if (jsonObject.getBoolean("success")) {
					super.onSuccess(jsonObject);
					// 로그인 정보 삽입.
					TagboaApi.HttpClient.setBasicAuth(id, password);
					Intent intent = new Intent();
					intent.putExtra("userId", id);
					intent.putExtra("password", password);
					setResult(RESULT_OK, intent);
					LoginActivity.this.finish();
				}
				else {
					throw new AuthenticationException();
				}
			} catch (JSONException e) {
				MainActivity.ShowToast(LoginActivity.this, getString(R.string.errorConnection), true);
			} catch (AuthenticationException e) {
				MainActivity.ShowToast(LoginActivity.this, getString(R.string.errorLogin), true);
			}
		}

		@Override
		public void onFailure(Throwable throwable, String s) {
			super.onFailure(throwable, s);
			MainActivity.ShowToast(LoginActivity.this, getString(R.string.errorLogin), true);
		}
	}

	/**
	 * 로그인 결과값 처리.
	 * Youngjae (2013-11-03 01:50:28) : 작업 중.
	 * 문제점 1. field.required.tokenModel.channel 처리 부분에 문제가 있음.
	 * 문제점 2. 최초에는 success로 됨.
	 */
	private class LoginCookieJsonHttpResponseHandler extends JsonHttpResponseHandler {
		private final String id;
		private final String password;

		public LoginCookieJsonHttpResponseHandler(String id, String password) {
			this.id = id;
			this.password = password;
		}

		@Override
		public void onSuccess(JSONObject jsonObject) {
			super.onSuccess(jsonObject);
			try {
				BapulMessage message = BapulMessage.FromJson(jsonObject);
				if (message.isSuccess) {
					// 로그인 정보 삽입.
					Intent intent = new Intent();
					intent.putExtra("userId", id);

					setResult(RESULT_OK, intent);
					MainActivity._member = Member.FromJson(jsonObject.getJSONObject("items"));
					LoginActivity.this.finish();
				}
				else {
					MainActivity.ShowToast(LoginActivity.this, getString(R.string.errorLogin), true);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

		@Override
		public void onFailure(Throwable throwable, String s) {
			MainActivity.ShowToast(LoginActivity.this, getString(R.string.errorConnection), true);
			super.onFailure(throwable, s);
		}

	}
}
