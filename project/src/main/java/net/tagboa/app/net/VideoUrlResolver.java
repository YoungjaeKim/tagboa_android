package net.tagboa.app.net;

import android.content.Context;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * Created by Youngjae on 2014-08-07.
 */
public interface VideoUrlResolver {
    public void getUrl(Context context, String lecId, JsonHttpResponseHandler handler);
}
