package cn.pzw.tracklog.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.tencent.tencentmap.mapsdk.maps.CameraUpdate;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdateFactory;
import com.tencent.tencentmap.mapsdk.maps.MapView;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;

import cn.pzw.tracklog.R;

public class ZoomControl extends LinearLayout implements View.OnClickListener {

    private TencentMap mTencentMap;

    public void setMap(MapView view){
        mTencentMap = view.getMap();
    }

    private void init(){
        CardView view = (CardView) LayoutInflater.from(getContext()).inflate(R.layout.custom_view_zoom_control, null);
        Button mButtonIn = (Button) view.findViewById(R.id.btn_zoom_in);
        Button mButtonOut = (Button) view.findViewById(R.id.btn_zoom_out);
        mButtonIn.setOnClickListener(this);
        mButtonOut.setOnClickListener(this);
        addView(view);
    }

    public ZoomControl(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);
        init();
    }

    public ZoomControl(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_zoom_in:
                CameraUpdate zoomIn = CameraUpdateFactory.zoomIn();
                mTencentMap.moveCamera(zoomIn);
                break;
            case R.id.btn_zoom_out:
                CameraUpdate zoomOut = CameraUpdateFactory.zoomOut();
                mTencentMap.moveCamera(zoomOut);
                break;
            default:
                break;
        }
    }
}
