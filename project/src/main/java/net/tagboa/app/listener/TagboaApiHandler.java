package net.tagboa.app.listener;

/**
 * Created by Youngjae on 2014-06-04.
 */
public interface TagboaApiHandler {
	public void onSuccess(org.json.JSONObject response);
	public void onSuccess(org.json.JSONArray response);
	public void onFailure(org.json.JSONObject response);
	public void onFailure(org.json.JSONArray response);
	public void onUnAuthorized();
	public void onFinish();
}
