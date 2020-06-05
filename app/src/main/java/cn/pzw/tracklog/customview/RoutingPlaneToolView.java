package cn.pzw.tracklog.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;

import cn.pzw.tracklog.R;
import cn.pzw.tracklog.RoutingPlaneActivity;

public class RoutingPlaneToolView extends LinearLayout implements View.OnClickListener {

    private Button mButtonBack;
    private TextView mTextViewFrom;
    private TextView mTextViewTo;
    private TabLayout mTabLayout;

    private RoutingPlaneActivity routingPlaneActivity;

    public RoutingPlaneToolView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);
        init();
    }

    public RoutingPlaneToolView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_custom_view_routing_plane_back:
                //关闭该活动
                routingPlaneActivity.finish();
                break;
            default:
                break;
        }
    }

    private void init(){
        LayoutInflater.from(getContext()).inflate(R.layout.custom_view_routing_plane_tools, this);
        mButtonBack = findViewById(R.id.btn_custom_view_routing_plane_back);
        mTextViewFrom = findViewById(R.id.tv_routing_plane_from);
        mTextViewTo = findViewById(R.id.tv_routing_plane_to);
        mTabLayout = findViewById(R.id.tab_layout_routing_plane);
        initTabs();
        mButtonBack.setOnClickListener(this);
    }

    private void initTabs(){
        mTabLayout.addTab(mTabLayout.newTab().setText("驾车"));
        mTabLayout.addTab(mTabLayout.newTab().setText("步行"));
        mTabLayout.addTab(mTabLayout.newTab().setText("骑行"));
        mTabLayout.addTab(mTabLayout.newTab().setText("公交"));

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()){
                    case 0:
                        routingPlaneActivity.driving();
                        break;
                    case 1:
                        routingPlaneActivity.walking();
                        break;
                    case 2:
                        routingPlaneActivity.bicycling();
                        break;
                    case 3:
                        routingPlaneActivity.transiting();
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    //获取活动
    public void setRoutingPlaneActivity(RoutingPlaneActivity activity){
        routingPlaneActivity = activity;
    }

    //设置起点名称
    public void setFromName(String from){
        mTextViewFrom.setText(from);
    }

    //设置终点名称
    public void setToName(String to){
        mTextViewTo.setText(to);
    }

}
