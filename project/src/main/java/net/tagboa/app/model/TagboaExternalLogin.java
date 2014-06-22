package net.tagboa.app.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Youngjae on 2014-06-12.
 */
public class TagboaExternalLogin {
	public String name;
	public String url;
	public String state;

	public static TagboaExternalLogin fromJson(JSONObject jsonObject) throws JSONException {
		TagboaExternalLogin login = new TagboaExternalLogin();
		login.name = jsonObject.getString("Name");
		login.url = jsonObject.getString("Url");
		login.state = jsonObject.getString("State");
		return login;
	}
}
