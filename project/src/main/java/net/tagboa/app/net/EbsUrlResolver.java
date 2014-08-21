package net.tagboa.app.net;

import android.content.Context;
import android.net.Uri;
import android.widget.MediaController;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Ebs 영상의 mp4 원본주소를 받아오기 위함.
 * Created by Youngjae on 2014-08-07.
 */
public class EbsUrlResolver implements VideoUrlResolver {
    @Override
    public String getResolverName() {
        return "Ebs";
    }

    /**
     * URL을 받아서 ebs 재생 mp4 주소를 반환.
     * @param context
     * @param url
     * @param handler
     */
    public void getUrl(Context context, String url, final VideoUrlResponseHandler handler) throws IllegalArgumentException{
        Uri uri = Uri.parse(url);
        String lecId = uri.getQueryParameter("lectId");
        if(lecId == null || "".equals(lecId)) {
            throw new IllegalArgumentException("no lecId in url");
        }
        // 1. URL을 전송.
        // lectId=10062578&authCd=&vodType=M10
        AsyncHttpClient httpClient = new AsyncHttpClient();
        httpClient.addHeader("Accept", "application/json");
        httpClient.addHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        RequestParams params = new RequestParams();

        params.put("lectId", lecId);
        params.put("authCd", "");
        params.put("vodType", "M10");

        httpClient.post(context, "http://www.ebs.co.kr/replay/show/vod", params, new JsonHttpResponseHandler("UTF-8") {
            @Override
            public void onSuccess(JSONObject response) {
                super.onSuccess(response);
                try {
                    String urlString = response.getString("vodStreamUrl");
                    handler.onSuccess(urlString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(String responseBody, Throwable error) {
                super.onFailure(responseBody, error);
                handler.onFailure(responseBody + error.getMessage());
            }
        });
    }
}
