package net.tagboa.app.page;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ScrollView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.loopj.android.http.JsonHttpResponseHandler;
import net.tagboa.app.BaseActivity;
import net.tagboa.app.R;
import net.tagboa.app.model.TagboaItem;
import net.tagboa.app.model.TagboaUrlLink;
import net.tagboa.app.net.TagboaApi;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class NewItemActivity extends BaseActivity {

    private android.widget.EditText editTextTitle;
    private android.widget.EditText editTextDescription;
    private android.widget.EditText editTextLink;
    private android.widget.ScrollView scrollView;
    protected BaseActivity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Tagboa);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_launcher);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_item);
        setTitle("새 자료 입력");

        this.scrollView = (ScrollView) findViewById(R.id.scrollView);
        this.editTextTitle = (EditText) findViewById(R.id.editTextTitle);
        this.editTextDescription = (EditText) findViewById(R.id.editTextDescription);
        this.editTextLink = (EditText) findViewById(R.id.editTextLink);
        mActivity = NewItemActivity.this;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mActivity == null)
            mActivity = NewItemActivity.this;

        menu.add(getString(R.string.buttonInputComplete))
                .setIcon(R.drawable.ic_action_ok)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String title = (String) item.getTitle();
        if (title.equals(getString(R.string.buttonInputComplete))) {
            TagboaItem tagboaItem;
            try {
                tagboaItem = prepareItem();
            }catch (IllegalArgumentException e){
                HomeActivity.ShowToast(NewItemActivity.this, e.getMessage());
                return super.onOptionsItemSelected(item);
            }

            try {
                TagboaApi.PostItem(NewItemActivity.this, tagboaItem, new JsonHttpResponseHandler(){
                    @Override
                    public void onSuccess(JSONObject response) {
                        super.onSuccess(response);
                        setResult(RESULT_OK);
                        NewItemActivity.this.finish();
                    }

                    @Override
                    public void onFailure(int statusCode, Throwable e, JSONObject errorResponse) {
                        super.onFailure(statusCode, e, errorResponse);
                        if(statusCode >= 500){
                            HomeActivity.ShowToast(NewItemActivity.this, getString(R.string.errorInternalServerError));
                            // TODO: YoungjaeKim 자동으로 리포팅되도록 해야 한다.
                        }
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 전송할 아이템 인스턴스 만들기.
     * @return {@link net.tagboa.app.model.TagboaItem} 오브젝트.
     */
    private TagboaItem prepareItem() {
        TagboaItem item = new TagboaItem();
        // 기본 정보.
        item.Title = editTextTitle.getText().toString().trim();
        item.Description = editTextDescription.getText().toString().trim();
        if("".equals(item.Title))
            throw new IllegalArgumentException(getString(R.string.errorValidationTitleEmpty));

        // Link 처리.
        item.Links = new ArrayList<TagboaUrlLink>();
        TagboaUrlLink link = new TagboaUrlLink();
        link.Address = editTextLink.getText().toString().trim();
        item.Links.add(link);

        return item;
    }
}
