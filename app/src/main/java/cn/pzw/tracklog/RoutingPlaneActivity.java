package cn.pzw.tracklog;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.tencent.tencentmap.mapsdk.maps.model.LatLng;

import cn.pzw.tracklog.bean.LocationBean;
import cn.pzw.tracklog.customview.RoutingPlaneToolView;

public class RoutingPlaneActivity extends AppCompatActivity {

    private RoutingPlaneToolView mRoutingPlaneToolView;
    private PolyLineFragment mPolyLineFragment;

    private LatLng fromLatLng;
    private LatLng toLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routing_plane);

        mRoutingPlaneToolView = findViewById(R.id.routing_plane_tool_view);
        mRoutingPlaneToolView.setRoutingPlaneActivity(this);

        Bundle bundle = getIntent().getBundleExtra("LOCATION");

        LocationBean from = bundle.getParcelable("FROM");
        LocationBean to = bundle.getParcelable("TO");
        mRoutingPlaneToolView.setFromName(from.getName());
        mRoutingPlaneToolView.setToName(to.getName());

        fromLatLng = new LatLng(from.getLatitude(), from.getLongitude());
        toLatLng = new LatLng(to.getLatitude(), to.getLongitude());

        mPolyLineFragment = new PolyLineFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.routing_plane_container, mPolyLineFragment)
                .commit();
    }

    //提供起点\终点经纬度
    public LatLng getFromLatLng(){
        return fromLatLng;
    }
    public LatLng getToLatLng(){
        return toLatLng;
    }

    //驾车
    public void driving(){
        mPolyLineFragment.getRoutingPlane(PolyLineFragment.DRIVING_PARAM);
    }

    //步行
    public void walking(){
        mPolyLineFragment.getRoutingPlane(PolyLineFragment.WALKING_PARAM);
    }

    //骑行
    public void bicycling(){
        mPolyLineFragment.getRoutingPlane(PolyLineFragment.BICYCLING_PARAM);
    }

    //公交
    public void transiting(){
        mPolyLineFragment.getRoutingPlane(PolyLineFragment.TRANSIT_PARAM);
    }

}
