package net.tagboa.app.model;

import net.tagboa.app.listener.TagboaApiHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Youngjae on 2014-06-05.
 */
public class TagboaItemsResponseHander implements TagboaApiHandler {
	public List<TagboaItem> items;
	@Override
	public void onSuccess(JSONObject response) {
	}

	@Override
	public void onSuccess(JSONArray response) {
		for (int i=0;i<response.length();i++)
			try {
				items.add(TagboaItem.fromJson(response.getJSONObject(i)));
			} catch (JSONException e) {
				e.printStackTrace();
			}
	}

	@Override
	public void onFailure(JSONObject response) {

	}

	@Override
	public void onFailure(JSONArray response) {

	}

	@Override
	public void onUnAuthorized() {

	}

	@Override
	public void onFinish() {

	}
}
