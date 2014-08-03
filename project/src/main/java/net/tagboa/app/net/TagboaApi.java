package net.tagboa.app.net;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import net.tagboa.app.listener.OnRefreshListener;
import net.tagboa.app.model.TagboaClient;
import net.tagboa.app.model.TagboaItem;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * 태그보아 통신관련 API
 * Created by Youngjae on 2014-06-02.
 */
public class TagboaApi {

	public static AsyncHttpClient HttpClient;
	public static PersistentCookieStore BapulCookieStore;
	public static String BaseRestUrl;
	public static String BaseUrl;

	/**
	 * 어플리케이션 ID.
	 *
	 * @param tagboaClient
	 * @return
	 */
	public static String ApplicationName(TagboaClient tagboaClient) {
		return tagboaClient.GetApplicationName();
	}

	public static String BaseUrl(TagboaClient tagboaClient) {
		return tagboaClient.GetBaseUrl();
	}

	public static String BaseRestUrl(TagboaClient tagboaClient) {
		return tagboaClient.GetBaseRestUrl();
	}

	/**
	 * HttpClient 초기화.
	 * 오랜 시간 후에 리쥼할 때 null이 되는 문제를 해결한다.
	 */
	public static void InitializeHttpClient(final Context context) {
		InitializeHttpClient(context, null);
	}

	/**
	 * HttpClient 초기화.
	 * 오랜 시간 후에 리쥼할 때 null이 되는 문제를 해결한다.
	 */
	public static void InitializeHttpClient(final Context context, final OnRefreshListener listener) {
		if (HttpClient == null) {
			HttpClient = new AsyncHttpClient();
//			HttpClient.addHeader("X-Requested-With", "XMLHttpRequest");
			HttpClient.getHttpClient().getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, false);
		}
		if (BapulCookieStore == null)
			BapulCookieStore = new PersistentCookieStore(context.getApplicationContext());
		HttpClient.setCookieStore(BapulCookieStore);

		// 로그인 쿠키가 잘 있는지 검사.
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		BaseUrl = BaseUrl((TagboaClient) context);
		BaseRestUrl = BaseRestUrl((TagboaClient) context);

		// 로그인 복원.
		if (!"".equals(sharedPrefs.getString("Authentication", ""))) {
			try {
				JSONObject jsonObject = new JSONObject(sharedPrefs.getString("Authentication", ""));
				HttpClient.removeHeader("Authorization");
				HttpClient.addHeader("Authorization", "Bearer " + jsonObject.getString("access_token"));
			} catch (JSONException e) {
				e.printStackTrace();
			}

			if (listener != null)
				listener.onRefresh();
		}
		else {
			if (listener != null)
				listener.onError();
		}
	}

    /**
     * 로그인 토큰이 존재하는지 여부.
     * @param context
     * @return
     */
    public static boolean HasLoginToken(final Context context){
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return !"".equals(sharedPrefs.getString("Authentication", ""));
    }

	/**
	 * 회원 가입.
	 *
	 * @param context
	 * @param id
	 * @param password
	 * @param jsonHttpResponseHandler
	 */
	public static void Register(Context context, String id, String password, String confirmPassword, JsonHttpResponseHandler jsonHttpResponseHandler) throws JSONException, UnsupportedEncodingException {
		if (HttpClient == null)
			InitializeHttpClient(context);

		JSONObject jsonParams = new JSONObject();
		jsonParams.put("application", ApplicationName((TagboaClient) context));
		jsonParams.put("UserName", id);
		jsonParams.put("Password", password);
		jsonParams.put("ConfirmPassword", confirmPassword);
		StringEntity entity = new StringEntity(jsonParams.toString());

		HttpClient.post(context, TagboaUrl.LOGIN.toString(false, false), entity, "application/json; charset=utf-8", jsonHttpResponseHandler);
	}

	/**
	 * 로그인.
	 *
	 * @param context
	 * @param id
	 * @param password
	 * @param jsonHttpResponseHandler
	 */
	public static void Login(Context context, String id, String password, final JsonHttpResponseHandler jsonHttpResponseHandler) {
		if (HttpClient == null)
			InitializeHttpClient(context);

		RequestParams params = new RequestParams();

		params.put("application", ApplicationName((TagboaClient) context));
		params.put("grant_type", "password");
		params.put("username", id);
		params.put("password", password);

		HttpClient.post(context, TagboaUrl.LOGIN.toString(false, false), params, jsonHttpResponseHandler);
	}

    /**
     * 아이템 가져오기.
     * @param context
     * @param username
     * @param lastKey
     * @param jsonHttpResponseHandler
     * @throws JSONException
     * @throws UnsupportedEncodingException
     */
	public static void GetItems(Context context, String username, String lastKey, JsonHttpResponseHandler jsonHttpResponseHandler) throws JSONException, UnsupportedEncodingException {
		if (HttpClient == null)
			InitializeHttpClient(context);

		RequestParams params = new RequestParams();

		params.put("application", ApplicationName((TagboaClient) context));
		params.put("username", username);
        if(lastKey != null)
    		params.put("lastKey", lastKey);

		HttpClient.get(context, TagboaUrl.ITEM.toString(), params, jsonHttpResponseHandler);
	}

	public static void PostItem(Context context, TagboaItem item, JsonHttpResponseHandler jsonHttpResponseHandler) throws JSONException, UnsupportedEncodingException {
		if (HttpClient == null)
			InitializeHttpClient(context);

		JSONObject jsonParams = item.toJson();
		StringEntity entity = new StringEntity(jsonParams.toString(), "UTF-8");
		HttpClient.post(context, TagboaUrl.ITEM.toString(), entity, "application/json", jsonHttpResponseHandler);
	}

	public static void GetTags(Context context, String query, String locale, JsonHttpResponseHandler jsonHttpResponseHandler) {

		if (HttpClient == null)
			InitializeHttpClient(context);

		// 폰의 지역정보 가져오기: http://stackoverflow.com/a/4212417/361100
		if (locale != null && !"".equals(locale))
			if (!locale.equals("en_US") && !locale.equals("ja_JP") && !locale.equals("ko_KR"))
				locale = "ko_KR";

		RequestParams params = new RequestParams();
		if (locale != null && !"".equals(locale))
			params.put("locale", locale);
		params.put("query", query);
		HttpClient.get(TagboaUrl.TAGS.toString(), params, jsonHttpResponseHandler);
	}

	public static void GetExternalLogins(Context context, JsonHttpResponseHandler jsonHttpResponseHandler) {
		if (HttpClient == null)
			InitializeHttpClient(context);
		HttpClient.removeHeader("Authorization");

		RequestParams params = new RequestParams();
		params.put("returnUrl", "%2F");
		params.put("generateState", "true");
		HttpClient.get(TagboaUrl.EXTERNAL_LOGINS.toString(), params, jsonHttpResponseHandler);
	}

	public static void GetExternalLogin(Context context, String provider, String state, JsonHttpResponseHandler jsonHttpResponseHandler) {
		if (HttpClient == null)
			InitializeHttpClient(context);

		RequestParams params = new RequestParams();
		params.put("provider", provider);
		params.put("response_type", "token");
		params.put("client_id", "self");
		params.put("redirect_uri", "app.tagboa.net");
		params.put("state", state);
		HttpClient.get(TagboaUrl.EXTERNAL_LOGIN.toString(), params, jsonHttpResponseHandler);
	}

	/**
	 * Facebook Login
	 * @param context
	 * @param access_token
	 * @param userid Facebook User ID (1023......)
	 *               @param username null일 경우 기존 유저.
	 * @param jsonHttpResponseHandler
	 */
	public static void FacebookLogin(Context context, String access_token, String userid, String username, JsonHttpResponseHandler jsonHttpResponseHandler) {
		if (HttpClient == null)
			InitializeHttpClient(context);
		RequestParams params = new RequestParams();
		params.put("token", access_token);
		params.put("userid", userid);
		params.put("username", username == null ? "" : username);
		HttpClient.post(context, TagboaUrl.FACEBOOK_LOGIN.toString(), params, jsonHttpResponseHandler);
	}

	public static void RegisterExternalLogin(Context context, String username, JsonHttpResponseHandler jsonHttpResponseHandler) {
		if (HttpClient == null)
			InitializeHttpClient(context);
		RequestParams params = new RequestParams();
		params.put("UserName", username);
		HttpClient.post(context, TagboaUrl.REGISTER_EXTERNAL_LOGINS.toString(), params, jsonHttpResponseHandler);
	}

	public static void GetUserInfo(Context context, JsonHttpResponseHandler jsonHttpResponseHandler) {
		if (HttpClient == null)
			InitializeHttpClient(context);
		HttpClient.get(TagboaUrl.USER_INFO.toString(), jsonHttpResponseHandler);
	}
}
