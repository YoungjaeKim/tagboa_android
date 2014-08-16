package net.tagboa.app.page;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;
import com.loopj.android.http.JsonHttpResponseHandler;
import net.tagboa.app.BaseActivity;
import net.tagboa.app.R;
import net.tagboa.app.model.TagboaItem;
import net.tagboa.app.net.EbsUrlResolver;
import net.tagboa.app.net.VideoUrlResolver;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Youngjae on 2014-06-22.
 */
public class VideoPlayerActivity extends BaseActivity implements View.OnClickListener {

    TagboaItem mItem;
    VideoView mVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

//        if(mItem.Links == null || mItem.Links.size() == 0){
//            finishActivityWithFail();
//            return;
//        }

        VideoUrlResolver urlResolver;
        urlResolver = new EbsUrlResolver();

        urlResolver.getUrl(VideoPlayerActivity.this, "http://www.ebs.co.kr/replay/show?courseId=BP0PAPB0000000009&stepId=01BP0PAPB0000000009&lectId=10118427", new VideoUrlResolver.VideoUrlResponseHandler() {
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

            }
        });
    }
    @Override
    public void onClick(View view) {

    }

    private void finishActivityWithFail(){
        HomeActivity.ShowToast(VideoPlayerActivity.this, getString(R.string.errorPlayingVideo));
        finish();

    }
}
