package net.tagboa.app.net;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

/**
 * Ebs 영상의 mp4 원본주소를 받아오기 위함.
 * Created by Youngjae on 2014-08-07.
 */
public class YoutubeUrlResolver implements VideoUrlResolver {
    @Override
    public String getResolverName() {
        return "Youtube";
    }

    /**
     * URL을 받아서 유튜브 재생 주소를 반환.
     *
     * @param context
     * @param url
     * @param handler
     */
    public void getUrl(Context context, String url, final VideoUrlResponseHandler handler) {
        Uri uri;
        try {
            uri = Uri.parse(url);
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid url format");
        }
        String videoUrl = getUrlVideoRTSP(url);
        if("".equals(videoUrl))
            handler.onSuccess(videoUrl);
        else
            handler.onFailure("video url empty");
    }


    /**
     * YouTube로부터 VideoView로 재생하기 위한 URL 가져오기.
     * 원본: http://stackoverflow.com/a/13814855/361100
     *
     * @param urlYoutube 유튜브 share 주소. 예시: "http://www.youtube.com/watch?v=1FJHYqE0RDg"
     * @return
     */
    protected static String getUrlVideoRTSP(String urlYoutube) {
        try {
            String gdy = "http://gdata.youtube.com/feeds/api/videos/";
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            String id = extractYoutubeId(urlYoutube);
            URL url = new URL(gdy + id);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            Document doc = documentBuilder.parse(connection.getInputStream());
            Element el = doc.getDocumentElement();
            NodeList list = el.getElementsByTagName("media:content");///media:content
            String cursor = urlYoutube;
            for (int i = 0; i < list.getLength(); i++) {
                Node node = list.item(i);
                if (node != null) {
                    NamedNodeMap nodeMap = node.getAttributes();
                    HashMap<String, String> maps = new HashMap<String, String>();
                    for (int j = 0; j < nodeMap.getLength(); j++) {
                        Attr att = (Attr) nodeMap.item(j);
                        maps.put(att.getName(), att.getValue());
                    }
                    if (maps.containsKey("yt:format")) {
                        String f = maps.get("yt:format");
                        if (maps.containsKey("url")) {
                            cursor = maps.get("url");
                        }
                        if (f.equals("1"))
                            return cursor;
                    }
                }
            }
            return cursor;
        } catch (Exception ex) {
            Log.e("Get Url Video RTSP Exception======>>", ex.toString());
        }
        return urlYoutube;

    }

    protected static String extractYoutubeId(String url) throws MalformedURLException {
        String id = null;
        try {
            String query = new URL(url).getQuery();
            if (query != null) {
                String[] param = query.split("&");
                for (String row : param) {
                    String[] param1 = row.split("=");
                    if (param1[0].equals("v")) {
                        id = param1[1];
                    }
                }
            } else {
                if (url.contains("embed")) {
                    id = url.substring(url.lastIndexOf("/") + 1);
                }
            }
        } catch (Exception ex) {
            Log.e("Exception", ex.toString());
        }
        return id;
    }
}
