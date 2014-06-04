package net.tagboa.app.model;

import android.content.Context;

/**
 * Activity common methods
 * Created by Youngjae on 13. 8. 15.
 */
public interface TagboaClient {
	public static final int REQUEST_LOGIN = 101;

	String GetApplicationName();
	String GetBaseUrl();
	String GetBaseRestUrl();
	void RequestLogin(Context context);
}
