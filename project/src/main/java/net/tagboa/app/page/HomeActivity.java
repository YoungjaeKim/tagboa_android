package net.tagboa.app.page;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Toast;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.loopj.android.http.JsonHttpResponseHandler;
import net.tagboa.app.BaseActivity;
import net.tagboa.app.R;
import net.tagboa.app.model.TagboaItem;
import net.tagboa.app.model.VideoItem;
import net.tagboa.app.net.FileDownloadTask;
import net.tagboa.app.net.TagboaApi;
import net.tagboa.app.view.listview.PullToRefreshListView;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class HomeActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "HomeActivity";
    private static final int REQUEST_REGISTER_FACEBOOK = 103;
    private static final int REQUEST_ITEM_VIEW = 104;
    private static final int REQUEST_NEW_ITEM = 105;
    public static String ApplicationName = "TagBoa";
    public static String BaseUrl = "app.tagboa.net";
    public static String BaseRestUrl = "app.tagboa.net/api";
    public static ArrayList<VideoItem> VideoItems;
    public ArrayList<TagboaItem> _items = new ArrayList<TagboaItem>();
    protected BaseActivity mActivity;
    private Integer _currentLastKey = 0;
    private Integer mOffset = 20;
    private ItemAdapter _itemAdapter;  // 아이템 목록 adapter

    private PullToRefreshListView _listView;
    private String _token;
    private JsonHttpResponseHandler streamResponseHandler = new jsonStreamResponsehandler();
    private String _lastKey;
    private Intent receivedIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Tagboa);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setHomeButtonEnabled(false);
        super.onCreate(savedInstanceState);
        // 하나의 바풀앱 인스턴스만 뜨도록 처리.
        // http://stackoverflow.com/a/10598594/361100  //Yang (2014-03-21 14:12:33) : manifest 에서 처리함.
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
        setContentView(R.layout.activity_home);
        receivedIntent = getIntent();
        setSupportProgressBarIndeterminateVisibility(false);

        mActivity = HomeActivity.this;


        // 로그아웃 후에 로그인창 표시.
        boolean finish = getIntent().getBooleanExtra("finish", false);
        if (finish) {
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivityForResult(intent, REQUEST_LOGIN);
            return;
        }

        // 로그인 기본 조건 체크.
        if (!TagboaApi.HasLoginToken(mActivity)) {
            // OAuth 로그인.
            RequestLogin(HomeActivity.this);
            return;
        }

        //// 로그인 성공 후의 프로세스 ////
        _listView = (PullToRefreshListView) findViewById(R.id.listViewItems);
        _listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i < 0 || _items.size() <= i)
                    return;
                Intent intent = new Intent(mActivity, VideoPlayerActivity.class);
                try {
                    intent.putExtra("item", _items.get(i).toJson().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                HomeActivity.this.startActivityForResult(intent, REQUEST_ITEM_VIEW);
//				mActivity.overridePendingTransition(R.anim.enter_fade_in, R.anim.no_effect);
            }
        });

        _listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i < 0 || _items.size() <= i)
                    return false;
                confirmItemRemoveDialog(_items.get(i));
                return false;
            }
        });

        _listView.setOnScrollListener(new PullToRefreshListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    if (_listView.getLastVisiblePosition() >= _listView.getCount() - 2) {
                        //load more list items:
                        loadItems(String.valueOf(_currentLastKey));
                    }
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // Do nothing.
            }
        });

        // Set a listener to be invoked when the list should be refreshed.
        _listView.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
            public void onRefresh() {
                loadItems();
            }
        });
        //Initialize our array adapter how it references the listitems.xml layout
        _itemAdapter = new ItemAdapter(mActivity, R.layout.adapter_item, _items);
        //Set the above adapter as the adapter of choice for our list
        _listView.setAdapter(_itemAdapter);
        if (_itemAdapter != null)
            _itemAdapter.notifyDataSetChanged();

        // 이미 로그인 되어 있으면 리스트 조회.
        if (TagboaApi.HasLoginToken(mActivity))
            loadItems();
    }

    /**
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        receivedIntent = intent;
        super.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (receivedIntent != null && receivedIntent.getBooleanExtra("finish", false)) {
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivityForResult(intent, REQUEST_LOGIN);
            receivedIntent.removeExtra("finish");
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {

        if (mActivity == null)
            mActivity = HomeActivity.this;

        if (TagboaApi.HasLoginToken(mActivity)) {
            menu.add(getString(R.string.buttonNewItem))
                    .setIcon(R.drawable.ic_action_new)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

            menu.add(getString(R.string.buttonProfile))
                    .setIcon(R.drawable.ic_action_person)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        }
        else
            menu.add(getString(R.string.buttonLogin))
                    .setIcon(R.drawable.ic_action_accounts)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

        // Inflate the menu; this adds items to the action bar if it is present.
        getSupportMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.buttonLogin) {
            // 로그인 테스트.
            TagboaApi.Login(HomeActivity.this, "tester", "qwerty", new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    try {
                        _token = response.getString("access_token");
                        _sharedPrefs.edit().putString("Authentication", response.toString()).commit();
                        TagboaApi.InitializeHttpClient(HomeActivity.this);
                        HomeActivity.ShowToast(HomeActivity.this, String.format("%s 로그인", TagboaApi.Username));
                        loadItems(); // 로그인 성공하면 바로 리스트 가져온다.
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Throwable e, JSONObject errorResponse) {
                    super.onFailure(statusCode, e, errorResponse);
                    if (statusCode == 400) {
                        TestActivity.ShowToast(HomeActivity.this, getString(R.string.errorIncorrectIdOrPassword));
                    }
                }
            });
            return true;
        }
        if(id == R.id.action_logout){
            RequestLogin(HomeActivity.this);
            return true;
        }
        // 가변형 메뉴
        String title = item.getTitle().toString();
        if(title.equals(getString(R.string.buttonNewItem))){
            Intent intent = new Intent(this, NewItemActivity.class);
            startActivityForResult(intent, REQUEST_NEW_ITEM);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;
        switch (requestCode) {
            case REQUEST_LOGIN:{
                HomeActivity.ShowToast(HomeActivity.this, String.format("%s 로그인", TagboaApi.Username));
                loadItems();
            }
            break;
            case REQUEST_NEW_ITEM:{
                loadItems();
            }
            break;
        }
    }

    /**
     * 토스트 메시지.
     *
     * @param context 메시지 표출할 콘텍스트(액티비티)
     * @param message 토스트로 보일 메시지.
     */
    public static void ShowToast(Context context, String message) {
        ShowToast(context, message, false);
    }

    /**
     * 토스트 메시지.
     *
     * @param context
     * @param message
     * @param isCenter
     */
    public static void ShowToast(Context context, String message, boolean isCenter) {
        Toast toast = Toast.makeText(context, String.format("%s: %s", context.getString(R.string.app_name), message), Toast.LENGTH_SHORT);
        if (isCenter)
            toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    /**
     * 업로드 완료 시 이벤트 발생.
     */
    protected FileDownloadTask.OnResponseListener onResponseListener = new FileDownloadTask.OnResponseListener() {
        public static final String TAG = "FileDownloadTask.OnResponseListener";

        public void onSuccess() {
            Log.d(TAG, "download success");
        }

        public void onFailure(String message) {
            HomeActivity.ShowToast(HomeActivity.this, message);
        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        }
    }

    /**
     * 스트림 가져오는 것에 대한 재시도를 위해 OnStreamResponseListener 처리를 위해서 inner class 처리함.
     */
    private class jsonStreamResponsehandler extends JsonHttpResponseHandler {
        @Override
        public void onSuccess(JSONArray response) {
            super.onSuccess(response);
            try {
                if (response.length() > 0) {
                    processItems(response);
                } else {
                    if(_currentLastKey == 0)
                        _items.clear();
                    else
                        HomeActivity.ShowToast(mActivity, mActivity.getString(R.string.toastLoadNothing));
                }
            } catch (Exception e) {
                Log.e(TAG, "HomeActivity.loadItems: " + e.getMessage());
            }

            if (_itemAdapter != null) {
                _itemAdapter.notifyDataSetChanged();
            }

            if (_listView.isRefreshing()) {
                _listView.onRefreshComplete();
            }
        }

        @Override
        public void onFailure(Throwable e, JSONObject errorResponse) {
            super.onFailure(e, errorResponse);
            HomeActivity.ShowToast(HomeActivity.this, getString(R.string.errorFetchData));

            if (_itemAdapter != null) {
                _itemAdapter.notifyDataSetChanged();
            }

            if (_listView.isRefreshing()) {
                _listView.onRefreshComplete();
            }
        }

        @Override
        public void onFinish() {
            super.onFinish();
            setSupportProgressBarIndeterminateVisibility(false);
        }
    }

    /**
     * 불러온 아이템 목록을 배열에 입력.
     *
     * @param jsonArray
     * @throws JSONException
     */
    private void processItems(JSONArray jsonArray) throws JSONException {
        if (_currentLastKey == 0) { // 새로고침이면 리스트 지우고 다시 로딩.
            _items.clear();
        }

        // 질문 리스트 넣기.
        if (jsonArray.length() > 0) {
            // 기존에 가지고 있는 질문 목록 사이즈가 더 크면 추가.
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    // NOTE: 늘상 나던
                    _items.add(TagboaItem.fromJson(jsonArray.getJSONObject(i)));
                } catch (Exception ignore) {
                }
            }
            _currentLastKey += _items.size();
        }
    }


    /**
     * 질문 로딩.
     */
    protected void loadItems() {
        _currentLastKey = 0;
        loadItems(null);
    }

    /**
     * 질문 로딩.
     *
     * @param lastKey
     */
    protected void loadItems(String lastKey) {
        try {
            setSupportProgressBarIndeterminateVisibility(true);

            TagboaApi.GetItems(mActivity, null, lastKey, streamResponseHandler);
        } catch (JSONException e) {

            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 질문 취소 확인 다이얼로그
     */
    private void confirmItemRemoveDialog(final TagboaItem item) {
        new AlertDialog.Builder(mActivity)
                .setMessage(mActivity.getString(R.string.dialogConfirmDeleteItem))
//				.setCancelable(false)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteItem(item);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();

    }

    /**
     * 아이템 삭제 처리 함수.
     * @param item
     */
    private void deleteItem(TagboaItem item){
        try {
            setSupportProgressBarIndeterminateVisibility(true);
            TagboaApi.DeleteItem(mActivity, item, new JsonHttpResponseHandler(){

                @Override
                public void onSuccess(JSONObject response) {
                    super.onSuccess(response);
                    HomeActivity.ShowToast(mActivity, getString(R.string.toastDeleteComplete));
                    loadItems(); // 삭제하면 리프래시
                }

                @Override
                public void onFinish() {
                    super.onFinish();
                    setSupportProgressBarIndeterminateVisibility(false);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 완료 이벤트 delegation용 인터페이스.
     */
    public interface OnStreamResponseListener {
        public void onSuccess();

        public void onFailure(String message);
    }
}
