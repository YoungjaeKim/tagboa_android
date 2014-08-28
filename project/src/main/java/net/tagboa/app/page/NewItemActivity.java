package net.tagboa.app.page;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ScrollView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.tokenautocomplete.FilteredArrayAdapter;
import com.tokenautocomplete.TokenCompleteTextView;
import net.tagboa.app.BaseActivity;
import net.tagboa.app.R;
import net.tagboa.app.model.TagboaItem;
import net.tagboa.app.model.TagboaTag;
import net.tagboa.app.model.TagboaUrlLink;
import net.tagboa.app.net.TagboaApi;
import net.tagboa.app.view.TagCompletionView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Locale;

public class NewItemActivity extends BaseActivity implements TagCompletionView.TokenListener {

    private static final String TAG = "NewItemActivity";
    private android.widget.EditText editTextTitle;
    private android.widget.EditText editTextDescription;
    private android.widget.EditText editTextLink;
    private android.widget.ScrollView scrollView;
    TagCompletionView completionView;
    ArrayList<TagboaTag> tagboaTags = new ArrayList<TagboaTag>();
    ArrayList<TagboaTag> selectedTags = new ArrayList<TagboaTag>();
    ArrayAdapter<TagboaTag> adapter;

    protected BaseActivity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Tagboa);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_launcher);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_item);
        setTitle("새 자료 입력");
        mActivity = NewItemActivity.this;

        this.scrollView = (ScrollView) findViewById(R.id.scrollView);
        this.editTextTitle = (EditText) findViewById(R.id.editTextTitle);
        this.editTextDescription = (EditText) findViewById(R.id.editTextDescription);
        this.editTextLink = (EditText) findViewById(R.id.editTextLink);
        this.completionView = (TagCompletionView) findViewById(R.id.searchView);


        adapter = new FilteredArrayAdapter<TagboaTag>(this, android.R.layout.simple_list_item_1, tagboaTags) {
            @Override
            protected boolean keepObject(TagboaTag obj, String mask) {
                mask = mask.toLowerCase();
                return obj.title.toLowerCase().contains(mask);
            }
        };

        completionView.allowDuplicates(false);
        completionView.setTokenClickStyle(TokenCompleteTextView.TokenClickStyle.Delete);
        completionView.setAdapter(adapter);
        completionView.setTokenListener(this);

        completionView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                String newText = charSequence.toString();
                SearchTopics(newText);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

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
            } catch (IllegalArgumentException e) {
                HomeActivity.ShowToast(NewItemActivity.this, e.getMessage());
                return super.onOptionsItemSelected(item);
            }

            if(tagboaItem == null)
                return true;

            try {
                TagboaApi.PostItem(NewItemActivity.this, tagboaItem, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        super.onSuccess(response);
                        setResult(RESULT_OK);
                        NewItemActivity.this.finish();
                    }

                    @Override
                    public void onFailure(int statusCode, Throwable e, JSONObject errorResponse) {
                        super.onFailure(statusCode, e, errorResponse);
                        if (statusCode >= 500) {
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
     *
     * @return {@link net.tagboa.app.model.TagboaItem} 오브젝트.
     */
    private TagboaItem prepareItem() {
        TagboaItem item = new TagboaItem();
        // 기본 정보.
        item.Title = editTextTitle.getText().toString().trim();
        item.Description = editTextDescription.getText().toString().trim();
        if ("".equals(item.Title))
            throw new IllegalArgumentException(getString(R.string.errorValidationTitleEmpty));

        // Link 처리.
        item.Links = new ArrayList<TagboaUrlLink>();
        TagboaUrlLink link = new TagboaUrlLink();
        link.Address = editTextLink.getText().toString().trim();
        item.Links.add(link);

        return item;
    }

    private void updateTokenConfirmation() {
        StringBuilder sb = new StringBuilder("Current tokens:\n");
        selectedTags.clear();
        for (Object token : completionView.getObjects()) {
            selectedTags.add((TagboaTag) token);
            sb.append(token.toString());
            sb.append("\n");
        }
        Log.d(TAG, sb.toString());
//        ((TextView) findViewById(R.id.tokens)).setText(sb);
    }


    @Override
    public void onTokenAdded(Object token) {
        updateTokenConfirmation();
        System.out.println();
    }


    @Override
    public void onTokenRemoved(Object token) {
        updateTokenConfirmation();
    }


    /**
     * 토픽 검색
     *
     * @param q 토픽 검색어.
     */
    public void SearchTopics(String q) {
        q = q.replaceAll("[ㄱ-ㅣ]", ""); // 단모음,단자음 입력부분 제거.
        if (q.length() < 1)
            return;

        String locale = _sharedPrefs.getString("locale", Locale.getDefault().toString());

        TagboaApi.GetTags(NewItemActivity.this, q, locale, new SearchTopicJsonHttpResponseHandler());
    }

    /**
     * 토픽 검색 이벤트 핸들러.
     */
    private class SearchTopicJsonHttpResponseHandler extends JsonHttpResponseHandler {
        @Override
        public void onSuccess(JSONArray message) {
            try {
                if (tagboaTags == null)
                    tagboaTags = new ArrayList<TagboaTag>();
                tagboaTags.clear();
                for (int i = 0; i < message.length(); i++)
                    tagboaTags.add(TagboaTag.fromJson(message.getJSONObject(i)));

                if (tagboaTags != null && tagboaTags.size() > 0) {
                    NewItemActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            } catch (JSONException e) {
                Log.e("Bapul", "searchTopics" + e.getMessage());
            }
        }
    }

}
