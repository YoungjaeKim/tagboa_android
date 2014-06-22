package net.tagboa.app.model;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 서버로부터 오는 인증 토큰을 처리.
 * Created by Youngjae on 2014-06-14.
 */
public class TagboaAuthToken {
	public String userName;
	public String access_token;
	public DateTime issued;
	public DateTime exprires;
	public Integer expires_in;

	public static TagboaAuthToken fromJson(JSONObject jsonObject) throws JSONException {
		TagboaAuthToken authToken = new TagboaAuthToken();
		authToken.userName = jsonObject.getString("userName");
		authToken.access_token = jsonObject.getString("access_token");
		authToken.issued = DateTime.parse(jsonObject.getString(".issued"));
		authToken.exprires = DateTime.parse(jsonObject.getString(".exprires"));
		authToken.expires_in = jsonObject.getInt("expires_in");
		return authToken;
	}
}
