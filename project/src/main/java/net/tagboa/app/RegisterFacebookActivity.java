package net.tagboa.app;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.actionbarsherlock.view.MenuItem;
import com.facebook.Session;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebookConfiguration;
import com.sromku.simple.fb.entities.Profile;
import com.sromku.simple.fb.listeners.OnLoginListener;
import com.sromku.simple.fb.listeners.OnProfileListener;
import com.sromku.simple.fb.utils.Attributes;
import com.sromku.simple.fb.utils.PictureAttributes;
import net.tagboa.app.model.TagboaExternalLogin;
import net.tagboa.app.net.TagboaApi;
import net.tagboa.app.page.TestActivity;
import net.tagboa.app.page.PrivacyPolicyActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class RegisterFacebookActivity extends BaseActivity implements View.OnClickListener, CheckBox.OnCheckedChangeListener {

	private static final String TAG = "RegisterFacebookActivity";
	private static final int MESSAGE_TEXT_CHANGED = 0;
	private static final long AUTOCOMPLETE_DELAY = 1500;
	String _principal;
	String _password;
	Profile _profile;
	EditText _editTextUserId;

	// Youngjae (2013-11-16 20:21:36) : 페이스북 퍼미션.
	boolean _isNewUser;
	public Permission[] permissions = new Permission[]
			{
					Permission.READ_FRIENDLISTS,
					Permission.USER_ABOUT_ME,
					Permission.EMAIL,
					Permission.PUBLISH_ACTION
			};
	private SimpleFacebook mSimpleFacebook;
	private boolean _isAgreementChecked;
	private TextView _textViewTitle;
	private Button _buttonSubmit;
	private LinearLayout _layoutSelectOne;
	private LinearLayout _layoutForm;
	private LinearLayout _layoutCurrentUserPassword;
	private LinearLayout _layoutAgreement;
	private TextView _textViewSelectOther;
	private TextView _textViewDescription;
	private EditText _editTextPasswordCurrent;
	private LinearLayout _layoutPreCheck;
	private ImageView _imageViewUserIdCheck;
	private Boolean _isUserIdOk;
	private RelativeLayout _layoutClose;

	public void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_Tagboa); //Used for theme switching in samples
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register_facebook);

		// UI 연결.
		_editTextUserId = (EditText) findViewById(R.id.editTextFacebookRegisterUserId);
		_editTextPasswordCurrent = (EditText) findViewById(R.id.editTextFacebookRegisterCurrentPassword);
		_textViewTitle = (TextView) findViewById(R.id.textViewFacebookRegisterTitle);
		CheckBox checkBoxAgreement = (CheckBox) findViewById(R.id.checkBoxFacebookRegisterAgreement);
		checkBoxAgreement.setOnCheckedChangeListener(this);
		_layoutSelectOne = (LinearLayout) findViewById(R.id.linearLayoutFacebookRegisterSelectOne);
		_layoutForm = (LinearLayout) findViewById(R.id.linearLayoutFacebookRegisterForm);
		_layoutCurrentUserPassword = (LinearLayout) findViewById(R.id.linearLayoutFacebookRegisterCurrentUserPassword);
		_layoutAgreement = (LinearLayout) findViewById(R.id.linearLayoutFacebookRegisterAgreement);
		_textViewSelectOther = (TextView) findViewById(R.id.textViewFacebookRegisterSelectOther);
		_textViewDescription = (TextView) findViewById(R.id.textViewFacebookRegisterInformation);
		_layoutPreCheck = (LinearLayout) findViewById(R.id.linearLayoutFacebookRegisterPreCheck);
		_imageViewUserIdCheck = (ImageView) findViewById(R.id.imageViewFacebookRegisterUserIdCheck);
		_layoutClose = (RelativeLayout) findViewById(R.id.relativeLayoutFacebookRegisterClose);

		_editTextUserId.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
				mHandler.removeMessages(MESSAGE_TEXT_CHANGED);
				final Message msg = Message.obtain(mHandler, MESSAGE_TEXT_CHANGED, _editTextUserId.getText().toString());
				mHandler.sendMessageDelayed(msg, AUTOCOMPLETE_DELAY);
			}

			@Override
			public void afterTextChanged(Editable editable) {
			}
		});

		// 선택폼도 가리기.
		_layoutSelectOne.setVisibility(View.GONE);

		// 기존 토큰 비우기.
		_sharedPrefs.edit().remove("Authentication").commit();

		// Facebook setting
		SimpleFacebookConfiguration configuration = new SimpleFacebookConfiguration.Builder()
				.setAppId(getString(R.string.app_id))
				.setNamespace("tagboa")
				.setPermissions(permissions)
				.build();
		SimpleFacebook.setConfiguration(configuration);
	}

	@Override
	public void onResume() {
		super.onResume();
		mSimpleFacebook = SimpleFacebook.getInstance(this);
		if (mSimpleFacebook != null) {
			// login
			mSimpleFacebook.login(onLoginListener);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// 홈버튼 클릭시.
		if (item.getItemId() == android.R.id.home) {
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * 페이스북과의 통신 에러시 토스트를 띄우고 액티비티를 닫는다.
	 *
	 * @param message
	 */
	private void showFacebookErrorAndFinish(String message) {
		_textViewTitle.setText(message);
		_layoutClose.setVisibility(View.VISIBLE);
	}

	/**
	 * 페이스북 로그인 결과에 대한 콜백.
	 */
	OnLoginListener onLoginListener = new OnLoginListener() {

		@Override
		public void onFail(String reason) {
			Log.w(TAG, reason);
			showFacebookErrorAndFinish(getString(R.string.textFacebookAuthenticateFailure));
		}

		@Override
		public void onException(Throwable throwable) {
			Log.e(TAG, "Bad thing happened", throwable);
			showFacebookErrorAndFinish(getString(R.string.errorFacebookConnection));
		}

		@Override
		public void onThinking() {
			// show progress bar or something to the user while login is happening
			Log.i(TAG, "In progress");
		}

		@Override
		public void onLogin() {
			// change the state of the button or do whatever you want
			Log.i(TAG, "Logged in");

			// prepare specific picture that we need
			PictureAttributes pictureAttributes = Attributes.createPictureAttributes();
			pictureAttributes.setHeight(500);
			pictureAttributes.setWidth(500);
			pictureAttributes.setType(PictureAttributes.PictureType.SQUARE);

// prepare the properties that you need
			Profile.Properties properties = new Profile.Properties.Builder()
					.add(Profile.Properties.ID)
					.add(Profile.Properties.NAME)
					.add(Profile.Properties.EMAIL)
					.add(Profile.Properties.VERIFIED)
					.add(Profile.Properties.PICTURE, pictureAttributes)
					.build();

			// 로그인에 성공하면 페이스북 프로필을 받는다.
			// 받은 결과는 onProfileRequestListener 콜백에서 처리됨.
			mSimpleFacebook.getProfile(properties, onProfileRequestListener);
		}

		@Override
		public void onNotAcceptingPermissions(Permission.Type type) {
			Log.w(TAG, "User didn't accept read permissions");
		}
	};

	/**
	 * 프로필 요구 콜백.
	 */
	OnProfileListener onProfileRequestListener = new OnProfileListener() {

		@Override
		public void onFail(String reason) {
			// insure that you are logged in before getting the profile
			Log.w(TAG, reason);
			showFacebookErrorAndFinish(getString(R.string.errorFacebookConnection));
		}

		@Override
		public void onException(Throwable throwable) {
			Log.e(TAG, "Bad thing happened", throwable);
			showFacebookErrorAndFinish(getString(R.string.errorFacebookConnection));
		}

		@Override
		public void onThinking() {
			// show progress bar or something to the user while fetching profile
			Log.i(TAG, "Thinking...");
		}

		@Override
		public void onComplete(final Profile profile) {
//			Log.i(TAG, "My profile id = " + profile.getId());
			_textViewTitle.setText(String.format(getString(R.string.textFacebookAuthenticateSuccess), profile.getName()));

			String session = Session.getActiveSession().getAccessToken();
			Log.d("tagboa", session);
			TagboaApi.FacebookLogin(RegisterFacebookActivity.this, session, profile.getId(), null, new JsonHttpResponseHandler() {
				@Override
				public void onFailure(int statusCode, Throwable e, JSONObject errorResponse) {
					super.onFailure(statusCode, e, errorResponse);
					if (statusCode == 400) {
						if (errorResponse.optString("Message", "").equalsIgnoreCase("unregistered user")) {
							// 바풀 ID가 없는(=바풀에 가입한적 없는) 페이스북 유저였다.
							// 사용자에게 ID/Password 입력창을 띄워준다.
							_layoutPreCheck.setVisibility(View.GONE);
							_layoutSelectOne.setVisibility(View.VISIBLE);
							_layoutForm.setVisibility(View.GONE);
							_profile = profile;
						}
					}
				}

				@Override
				public void onSuccess(JSONObject response) {
					super.onSuccess(response);
					// 가입 성공
					String auth = response.toString();
					_sharedPrefs.edit().putString("Authentication", auth).commit(); // 토큰 정보 저장.

					Intent intent = new Intent();
					intent.putExtra("result", auth);
					setResult(RESULT_OK, intent);
					RegisterFacebookActivity.this.finish();
				}

				@Override
				public void onSuccess(JSONArray response) {
					super.onSuccess(response);
				}

				@Override
				public void onFinish() {
					super.onFinish();
				}
			});
//
//			// 바풀 principal 존재 확인 절차 진행.
//			try {
//				TagboaApi.GetExternalLogins(RegisterFacebookActivity.this, new CheckOAuthJsonResponseHandler(profile));
//			} catch (IllegalArgumentException e) {
//				TestActivity.ShowToast(RegisterFacebookActivity.this, e.getMessage(), true);
//
//			}
		}
	};

	/**
	 * 가입창 띄움.
	 */
	private void processRegister() {
		// 먼저 해당 ID로 로그인 시도.

		// TODO: 일단 새로 가입 테스트.
		TagboaApi.FacebookLogin(RegisterFacebookActivity.this, Session.getActiveSession().getAccessToken(), _profile.getId(), _principal, new JsonHttpResponseHandler() {
			@Override
			public void onFailure(int statusCode, Throwable e, JSONObject errorResponse) {
				super.onFailure(statusCode, e, errorResponse);
			}

			@Override
			public void onSuccess(JSONObject response) {
				super.onSuccess(response);
				// 가입 성공
				String auth = response.toString();
				_sharedPrefs.edit().putString("Authentication", auth).commit(); // 토큰 정보 저장.

				Intent intent = new Intent();
				intent.putExtra("result", auth);
				setResult(RESULT_OK, intent);
				RegisterFacebookActivity.this.finish();
			}

			@Override
			public void onSuccess(JSONArray response) {
				super.onSuccess(response);
			}

			@Override
			public void onFinish() {
				super.onFinish();
			}
		});
//		try {
//			BapulApi.ProcessLogin(RegisterFacebookActivity.this, _principal, _password, new ProcessLoginJsonResponseHandler());
//		} catch (IllegalArgumentException e) {
//			TestActivity.ShowToast(RegisterFacebookActivity.this, BapulApi.getLabel(RegisterFacebookActivity.this, e.getMessage()), true);
//		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		mSimpleFacebook.onActivityResult(this, requestCode, resultCode, data);
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.buttonFacebookRegisterClose: {
				setResult(RESULT_CANCELED);
				RegisterFacebookActivity.this.finish();
			}
			break;
			case R.id.buttonFacebookRegisterCancel: {
				setResult(RESULT_CANCELED);
				RegisterFacebookActivity.this.finish();
			}
			break;
//			// 약관 보기
			case R.id.textViewFacebookRegisterAgreement: {
				Intent intent = new Intent(RegisterFacebookActivity.this, PrivacyPolicyActivity.class);
				intent.putExtra("raw", "agreement");
				intent.putExtra("title", getString(R.string.titleAgreement));
				startActivity(intent);
			}
			break;
//			// 개인정보 보호정책
			case R.id.textViewFacebookRegisterPrivacyPolicy: {
				Intent intent = new Intent(RegisterFacebookActivity.this, PrivacyPolicyActivity.class);
				intent.putExtra("raw", "privacy_policy");
				intent.putExtra("title", getString(R.string.titlePrivacyPolicy));
				startActivity(intent);
			}
			break;
			case R.id.buttonFacebookRegisterNewUser: {
				_isNewUser = toggleUserMode(false);
			}
			break;
			case R.id.buttonFacebookRegisterCurrentUser: {
				_isNewUser = toggleUserMode(true);
			}
			break;
			case R.id.buttonFacebookRegisterSelectOther: {
				// 다른 선택.
				_isNewUser = toggleUserMode(_isNewUser);
			}
			break;
			case R.id.buttonFacebookRegisterSubmit: {
				try {
					if (_isNewUser) {
						// 신규 유저용 폼 검증.
						if (_editTextUserId.getText() == null || _editTextUserId.getText().toString().trim().length() == 0)
							throw new IllegalArgumentException(getString(R.string.validationUserId));
						if (_isUserIdOk == null)
							throw new IllegalArgumentException(getString(R.string.validationIdCheckFailure));
						if (!_isUserIdOk)
							throw new IllegalArgumentException(getString(R.string.validationUserIdExist));
						if (!_isAgreementChecked)
							throw new IllegalArgumentException(getString(R.string.validationAgreementCheck));
						_principal = _editTextUserId.getText().toString().trim();
					}
					else {
						// 기존 유저용 폼 검증.
						if (_editTextUserId.getText() == null || _editTextUserId.getText().toString().trim().length() == 0)
							throw new IllegalArgumentException(getString(R.string.validationUserId));
						if (_editTextPasswordCurrent.getText() == null)
							throw new IllegalArgumentException(getString(R.string.validationUserPasswdLength6));
						_principal = _editTextUserId.getText().toString().trim();
						_password = _editTextPasswordCurrent.getText().toString();
						if (_password.length() < 6)
							throw new IllegalArgumentException(getString(R.string.validationUserPasswdLength6));
						if (_principal.equals(_password))
							throw new IllegalArgumentException(getString(R.string.validationUserIdAndPasswordMatch));
					}

					if (_principal.length() < 6)
						throw new IllegalArgumentException(getString(R.string.validationUserIdLength6));
				} catch (IllegalArgumentException e) {
					TestActivity.ShowToast(RegisterFacebookActivity.this, e.getMessage(), true);
					return;
				} catch (Exception e) {
					TestActivity.ShowToast(RegisterFacebookActivity.this, getString(R.string.errorConnection), true);
					return;
				}

				processRegister();
			}
			break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
		switch (compoundButton.getId()) {
			case R.id.checkBoxFacebookRegisterAgreement: {
				_isAgreementChecked = b;
			}
			break;
		}
	}

	/**
	 * 사용자 폼 전환.
	 *
	 * @param isNewUser
	 */
	private boolean toggleUserMode(boolean isNewUser) {
		if (isNewUser) {
			_layoutSelectOne.setVisibility(View.GONE);
			_layoutForm.setVisibility(View.VISIBLE);
			// 기존 유저 가입폼 보이기.
			_layoutAgreement.setVisibility(View.GONE);
			_layoutCurrentUserPassword.setVisibility(View.VISIBLE);
			_textViewDescription.setText(getString(R.string.textFacebookRegisterCurrentAccount));
			_textViewSelectOther.setText(getString(R.string.buttonFacebookRegisterNewUser));
			_imageViewUserIdCheck.setVisibility(View.GONE);

			return false;
		}
		else {
			_layoutSelectOne.setVisibility(View.GONE);
			_layoutForm.setVisibility(View.VISIBLE);
			// 신규 가입폼 보이기
			_layoutAgreement.setVisibility(View.VISIBLE);
			_layoutCurrentUserPassword.setVisibility(View.GONE);
			_textViewDescription.setText(getString(R.string.textFacebookRegisterDescription));
			_textViewSelectOther.setText(getString(R.string.buttonFacebookRegisterAccountExist));
			_imageViewUserIdCheck.setVisibility(View.VISIBLE);

			// 기본 이름 입력.
			_editTextUserId.setText(_profile.getEmail().substring(0, _profile.getEmail().indexOf("@")));
			return true;
		}
	}

	/**
	 * ID 검사.
	 *
	 * @param id
	 */
	private void checkUserIdExist(final String id) {
		if (id == null || id.length() < 6)
			throw new IllegalArgumentException("id");

		_isUserIdOk = true; // TODO: 나중에 false로 바꿔야 함.
//		BapulApi.CheckPrincipalExist(RegisterFacebookActivity.this, id, new CheckPrincipalJsonHttpResponseHandler(id));
	}

	private void drawChecker(ImageView imageView, Boolean isOk) {
		imageView.clearColorFilter();
		if (isOk == null) {
			imageView.setVisibility(View.INVISIBLE);
			return;
		}
		imageView.setVisibility(View.VISIBLE);
		if (isOk) {
			imageView.setImageResource(R.drawable.ic_action_done);
			imageView.setColorFilter(Color.GREEN, PorterDuff.Mode.LIGHTEN);
		}
		else {
			imageView.setImageResource(R.drawable.ic_alerts_and_states_error);
			imageView.setColorFilter(Color.RED, PorterDuff.Mode.LIGHTEN);
		}
	}

	/**
	 * ID 입력 딜레이 처리.
	 * http://stackoverflow.com/a/5776469/361100
	 */
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == MESSAGE_TEXT_CHANGED && _isNewUser) {
				try {
					checkUserIdExist((String) msg.obj);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	};

	/**
	 * ID 존재여부 검사.
	 */
	private class CheckPrincipalJsonHttpResponseHandler extends JsonHttpResponseHandler {
		private final String id;

		public CheckPrincipalJsonHttpResponseHandler(String id) {
			this.id = id;
		}

		@Override
		public void onSuccess(JSONObject jsonObject) {
//			try {
//				if (!jsonObject.getBoolean("success")) {
//					super.onSuccess(jsonObject);
//					_isUserIdOk = true;
////					TestActivity.ShowToast(RegisterFacebookActivity.this, String.format(getString(R.string.toastRegisterIdCheckOk), id), true);
//				}
//				else {
//					_isUserIdOk = false;
//					throw new AuthenticationException();
//				}
//			} catch (JSONException e) {
//				_isUserIdOk = false;
//				TestActivity.ShowToast(RegisterFacebookActivity.this, getString(R.string.errorConnection), true);
//			} catch (AuthenticationException e) {
//				_isUserIdOk = false;
//				TestActivity.ShowToast(RegisterFacebookActivity.this, " " + id + " " + getString(R.string.errorRegisterUserIdDuplicate), true);
//			} finally {
//				drawChecker(_imageViewUserIdCheck, _isUserIdOk);
//			}
		}

//		@Override
//		public void onFailure(Throwable throwable, String s) {
//			super.onFailure(throwable, s);
//			_isUserIdOk = false;
//			drawChecker(_imageViewUserIdCheck, _isUserIdOk);
//			TestActivity.ShowToast(RegisterFacebookActivity.this, getString(R.string.errorConnection), true);
//		}
	}

	private class CheckOAuthJsonResponseHandler extends JsonHttpResponseHandler {
		private final Profile profile;

		public CheckOAuthJsonResponseHandler(Profile profile) {
			this.profile = profile;
		}

		@Override
		public void onSuccess(JSONArray response) {
			super.onSuccess(response);
			TagboaExternalLogin login = null;
			for (int i = 0; i < response.length(); i++)
				try {
					login = TagboaExternalLogin.fromJson(response.getJSONObject(i));
					if (login.name.equalsIgnoreCase("facebook")) {
						break;
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}

			if (login != null && login.name.equalsIgnoreCase("facebook")) {
				String session = Session.getActiveSession().getAccessToken();
				Log.d("tagboa", session);
				TagboaApi.FacebookLogin(RegisterFacebookActivity.this, session, profile.getId(), null, new JsonHttpResponseHandler() {
					@Override
					public void onFailure(Throwable e, JSONObject errorResponse) {
						super.onFailure(e, errorResponse);
					}

					@Override
					public void onSuccess(JSONObject response) {
						super.onSuccess(response);
					}

					@Override
					public void onFinish() {
						super.onFinish();
					}
				});
			}

		}

		@Override
		public void onFailure(Throwable e, JSONObject errorResponse) {
			super.onFailure(e, errorResponse);
		}

		@Override
		public void onFinish() {
			super.onFinish();
		}

		@Override
		public void onSuccess(JSONObject jsonObject) {
			super.onSuccess(jsonObject);


//			BapulMessage message = null;
//			try {
//				message = BapulMessage.fromJson(jsonObject);
//			} catch (JSONException e) {
//				e.printStackTrace();
//			}
//
//			if (message != null && message.isSuccess) {
//				// 이전에 가입 기록 있음. 로그인 성공으로 정보 삽입 후 액티비티 종료.
//				String principal = "";
//				try {
//					principal = jsonObject.getJSONObject("items").getString("principal");
//				} catch (JSONException e) {
//					e.printStackTrace();
//				}
//
//				Intent intent = new Intent();
//				intent.putExtra("oauth", true);
//				intent.putExtra("userId", principal);
//				intent.putExtra("providerUserId", profile.getId());
//				intent.putExtra("provider", "facebook");
//				intent.putExtra("password", "");
//
//				setResult(RESULT_OK, intent);
//				RegisterFacebookActivity.this.finish();
//			}
//			else {
//				// 바풀 ID가 없는(=바풀에 가입한적 없는) 페이스북 유저였다.
//				// 사용자에게 ID/Password 입력창을 띄워준다.
//				_layoutPreCheck.setVisibility(View.GONE);
//				_layoutSelectOne.setVisibility(View.VISIBLE);
//				_layoutForm.setVisibility(View.GONE);
//				_profile = profile;
//			}
		}
	}
//
//	private class ProcessLoginJsonResponseHandler extends JsonHttpResponseHandler {
//		@Override
//		public void onSuccess(JSONObject jsonObject) {
//			super.onSuccess(jsonObject);
//			BapulMessage message = null;
//			try {
//				message = BapulMessage.fromJson(jsonObject);
//			} catch (JSONException e) {
//				e.printStackTrace();
//			}
//
//			if (message != null && message.isSuccess) {
//				BapulApi.AddOpenAuth(RegisterFacebookActivity.this, _principal, "facebook", _profile.getId(), _profile.getName(),
//						mSimpleFacebook.getSession().getAccessToken(), mSimpleFacebook.getSession().getExpirationDate().getTime(), new RegisterFacebookJsonHttpResponseHandler(_isNewUser));
//			}
//			else {
//				// 페이스북 정보와 입력한 신규 바풀ID로 가입을 시도.
//				BapulApi.RegisterOpenAuth(RegisterFacebookActivity.this, "facebook", _profile.getId(), _profile.getName(),
//						mSimpleFacebook.getSession().getAccessToken(), mSimpleFacebook.getSession().getExpirationDate().getTime(),
//						_principal, _password, new RegisterFacebookJsonHttpResponseHandler(_isNewUser));
//			}
//		}
//	}

}
