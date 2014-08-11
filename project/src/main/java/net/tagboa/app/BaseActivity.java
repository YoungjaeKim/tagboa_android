package net.tagboa.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.actionbarsherlock.app.SherlockActivity;
import net.tagboa.app.model.TagboaClient;
import net.tagboa.app.page.HomeActivity;
import net.tagboa.app.page.TestActivity;

/**
 * 공통 액티비티.
 * Created by Youngjae on 2014-06-03.
 */
public class BaseActivity extends SherlockActivity implements TagboaClient {
	public SharedPreferences _sharedPrefs;
    public static String ApplicationName = "TagBoa";
    public static String BaseUrl = "app.tagboa.net";
    public static String BaseRestUrl = "app.tagboa.net/api";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (_sharedPrefs == null)
			_sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
	}


	@Override
	public String GetApplicationName() {
		return BaseActivity.ApplicationName;
	}

	@Override
	public String GetBaseUrl() {
		return BaseActivity.BaseUrl;
	}

	@Override
	public String GetBaseRestUrl() {
		return BaseActivity.BaseRestUrl;
	}

	@Override
	public void RequestLogin(Context context) {

        // 메인액티비티를 띄운 후 메인액티비티가 로그아웃을 호출한다.
        // http://stackoverflow.com/a/9580057/361100
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("finish", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // To clean up all activities
        startActivity(intent);
        finish();
	}
    @Override
    protected void onResume() {
        super.onResume();
        if (_sharedPrefs == null)
            _sharedPrefs = PreferenceManager.getDefaultSharedPreferences(BaseActivity.this);
    }
}
