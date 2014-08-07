package net.tagboa.app.net;

import android.content.Context;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * Ebs 영상의 mp4 원본주소를 받아오기 위함.
 * Created by Youngjae on 2014-08-07.
 */
public class EbsUrlResolver implements VideoUrlResolver {
    public void getUrl(Context context, String lecId, JsonHttpResponseHandler handler){
        // 1. URL을 전송.
        AsyncHttpClient httpClient = new AsyncHttpClient();
        // lectId=10062578&authCd=&vodType=M10
        httpClient.addHeader("Accept", "application/json");
        httpClient.addHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        RequestParams params = new RequestParams();

        params.put("lectId", lecId);
        params.put("authCd", "");
        params.put("vodType", "M10");

        httpClient.post(context, "http://www.ebs.co.kr/replay/show/vod", params, handler);
    }
}
