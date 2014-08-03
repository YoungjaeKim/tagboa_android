package net.tagboa.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.actionbarsherlock.app.SherlockActivity;
import net.tagboa.app.model.TagboaClient;
import net.tagboa.app.page.TestActivity;

/**
 * 공통 액티비티.
 * Created by Youngjae on 2014-06-03.
 */
public class BaseActivity extends SherlockActivity implements TagboaClient {
	public SharedPreferences _sharedPrefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (_sharedPrefs == null)
			_sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
	}


	@Override
	public String GetApplicationName() {
		return TestActivity.ApplicationName;
	}

	@Override
	public String GetBaseUrl() {
		return TestActivity.BaseUrl;
	}

	@Override
	public String GetBaseRestUrl() {
		return TestActivity.BaseRestUrl;
	}

	@Override
	public void RequestLogin(Context context) {

	}

}
