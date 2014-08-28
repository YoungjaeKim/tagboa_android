package net.tagboa.app.page;

import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;
import com.actionbarsherlock.view.Window;
import net.tagboa.app.BaseActivity;
import net.tagboa.app.R;
import net.tagboa.app.model.TagboaItem;
import net.tagboa.app.model.TagboaUrlLink;
import net.tagboa.app.net.EbsUrlResolver;
import net.tagboa.app.net.VideoUrlResolver;
import net.tagboa.app.net.YoutubeUrlResolver;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 비디오 재생기
 * Created by Youngjae on 2014-06-22.
 */
public class VideoPlayerActivity extends BaseActivity implements View.OnClickListener {

    TagboaItem mItem;
    VideoView mVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 가로고정: http://stackoverflow.com/a/10270283/361100
        VideoPlayerActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        // 풀스크린 처리: http://stackoverflow.com/a/10909579/361100
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_video_player);
        mVideoView = (VideoView) VideoPlayerActivity.this.findViewById(R.id.videoView);

        if (getIntent() == null || !getIntent().hasExtra("item")) {
            finishActivityWithFail();
            return;
        }

        String json = getIntent().getStringExtra("item");
        try {
            mItem = TagboaItem.fromJson(new JSONObject(json));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (mItem.Links == null || mItem.Links.size() == 0) {
            finishActivityWithFail();
            return;
        }

        // URL에 따라 적절한 재생기를 찾는다.
        VideoUrlResolver urlResolver = null;
        String videoUrl = null;
        for (TagboaUrlLink link : mItem.Links) {
            urlResolver = detectResolver(link.Address);
            if (urlResolver != null) {
                videoUrl = link.Address;
                break;
            }
        }

        if (urlResolver != null) {
            try {
                urlResolver.getUrl(VideoPlayerActivity.this, videoUrl, new VideoUrlResolver.VideoUrlResponseHandler() {
                    @Override
                    public void onSuccess(String uri) {
                        Uri videoUrl = Uri.parse(uri); //Uri.parse("http://ebsvod.ebs.co.kr/ebsvod/cul/2013/bp0papb0000000009/1m/20130522_140000_m10.mp4");

                        // 비디오 재생: http://stackoverflow.com/a/17407362/361100
                        mVideoView.setMediaController(new MediaController(VideoPlayerActivity.this));
                        mVideoView.setVideoURI(videoUrl);
                        mVideoView.requestFocus();
                        mVideoView.start();
                    }

                    @Override
                    public void onFailure(String message) {
                        HomeActivity.ShowToast(VideoPlayerActivity.this, getString(R.string.errorDestinationVideoServerUnreachable), true);
                        finishActivityWithFail();
                    }
                });
            } catch (IllegalArgumentException e) {
                HomeActivity.ShowToast(VideoPlayerActivity.this, getString(R.string.errorUrlResolverFail), true);
            }
        } else
            finishActivityWithFail();

    }

    @Override
    public void onClick(View view) {

    }

    /**
     * URL에 따라 주소번역기를 선택.
     *
     * @param url
     * @return
     */
    private VideoUrlResolver detectResolver(String url) {
        if (url == null || "".equals(url))
            return null;
        if (url.contains("ebs.co.kr/"))
            return new EbsUrlResolver();
        if (url.contains("youtu.be/") || url.contains("youtube.com/"))
            return new YoutubeUrlResolver();
        return null;
    }

    private void finishActivityWithFail() {
        HomeActivity.ShowToast(VideoPlayerActivity.this, getString(R.string.errorPlayingVideo));
        finish();

    }
}
