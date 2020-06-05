package cn.pzw.tracklog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.tencent.lbssearch.TencentSearch;
import com.tencent.lbssearch.httpresponse.BaseObject;
import com.tencent.lbssearch.object.param.Geo2AddressParam;
import com.tencent.lbssearch.object.result.Geo2AddressResultObject;
import com.tencent.map.tools.net.http.HttpResponseListener;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;

import cn.pzw.tracklog.bean.LocationBean;

public class SearchActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private SearchView mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initActionBar();

        LocationBean locationBean = getIntent().getParcelableExtra("CenterPoint");

        final Geo2AddressParam geo2AddressParam = new Geo2AddressParam(new LatLng(locationBean.getLatitude(), locationBean.getLongitude()));
        TencentSearch tencentSearch = new TencentSearch(this);
        tencentSearch.geo2address(geo2AddressParam, new HttpResponseListener<BaseObject>() {

            @Override
            public void onSuccess(int i, BaseObject o) {
                Geo2AddressResultObject geo2AddressResultObject = (Geo2AddressResultObject)o;
                Toast.makeText(MyApplication.getContext(), geo2AddressResultObject.result.address, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int i, String s, Throwable throwable) {

            }
        });

    }

    private void initActionBar(){
        mToolbar = (Toolbar)findViewById(R.id.toolbar_search_activity);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initSearchView(Menu menu){
        mSearchView = (SearchView)menu.findItem(R.id.item_search_really).getActionView();
        mSearchView.onActionViewExpanded();
        mSearchView.setQueryHint("请输入地址");
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_activity_menu, menu);
        initSearchView(menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
