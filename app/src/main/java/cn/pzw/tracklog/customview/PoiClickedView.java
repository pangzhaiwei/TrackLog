package cn.pzw.tracklog.customview;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.tencent.tencentmap.mapsdk.maps.model.LatLng;
import com.tencent.tencentmap.mapsdk.maps.model.MapPoi;

import cn.pzw.tracklog.MainMapFragment;
import cn.pzw.tracklog.R;
import cn.pzw.tracklog.RoutingPlaneActivity;
import cn.pzw.tracklog.bean.LocationBean;

public class PoiClickedView extends CardView implements View.OnClickListener {

    private TextView mTextViewTitle;

    private MainMapFragment mMainMapFragment;
    private MapPoi mMapPoi;

    public PoiClickedView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        LinearLayout view = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.custom_view_marker_poi, null);
        Button mButtonGoto = view.findViewById(R.id.btn_poi_click_goto);
        Button mButtonCancel = view.findViewById(R.id.btn_poi_click_cancel);
        mTextViewTitle = view.findViewById(R.id.tv_poi_click_title);
        mButtonGoto.setOnClickListener(this);
        mButtonCancel.setOnClickListener(this);
        addView(view);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_poi_click_goto:
                LocationBean from = mMainMapFragment.getMyLocation();
                LatLng position =mMapPoi.position;
                LocationBean to = new LocationBean(mMapPoi.getName(), position.getLatitude(), position.getLongitude());
                Intent intent = new Intent(getContext(), RoutingPlaneActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("FROM", from);
                bundle.putParcelable("TO", to);
                intent.putExtra("LOCATION", bundle);
                getContext().startActivity(intent);
                break;
            case R.id.btn_poi_click_cancel:
                mMainMapFragment.unmarkerPoi();
                playCloseAnimation();
                setVisibility(INVISIBLE);
                break;
            default:
                break;
        }
    }

    //让该组件展开动画播放
    public void playOpenAnimation(){
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.anim_poi_clicked_open_view);
        this.startAnimation(animation);
    }

    //让该组件收起动画播放
    public void playCloseAnimation(){
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.anim_poi_clicked_close_view);
        this.startAnimation(animation);
    }

    //从外部更新title
    public void updateTitle(){
        mTextViewTitle.setText(mMapPoi.getName());
    }

    //获取外部BaseMapFragment
    public void setBaseMapFragment(MainMapFragment fragment){
        mMainMapFragment = fragment;
    }

    //获取点击的MapPoi实例
    public void setMapPoi(MapPoi mapPoi){
        mMapPoi = mapPoi;
    }

}
