package net.tagboa.app.net;

import android.content.Context;
import android.net.Uri;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * 비디오 재생을 위한 추상화 클래스.
 * Created by Youngjae on 2014-08-07.
 */
public interface VideoUrlResolver {
    abstract String getResolverName();

    /**
     * url을 받아서 필요한 동영상 주소만 추출한다.
     * @param context
     * @param lecId
     * @param handler
     */
    abstract void getUrl(Context context, String lecId, VideoUrlResponseHandler handler);


    /**
     * Created by Youngjae on 2014-08-16.
     */
    public interface VideoUrlResponseHandler {
        public void onSuccess(String uri);
        public void onFailure(String message);
    }
}
