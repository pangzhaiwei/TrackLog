package cn.pzw.tracklog.util;

import android.content.Context;
import android.os.Looper;

import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdate;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdateFactory;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;
import com.tencent.tencentmap.mapsdk.maps.model.CameraPosition;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;

public class SingleMyLocation implements TencentLocationListener {

    private TencentMap mTencentMap;
    private Context mContext;

    private TencentLocation mTencentLocation;

    public SingleMyLocation(Context context, TencentMap map){
        mContext = context;
        mTencentMap = map;
    }

    public void startSingleLocation(){
        TencentLocationManager mLocationManager = TencentLocationManager.getInstance(mContext);
        mLocationManager.requestSingleFreshLocation(null, this, Looper.getMainLooper());
    }

    @Override
    public void onLocationChanged(TencentLocation tencentLocation, int i, String s) {
        mTencentLocation = tencentLocation;
        CameraUpdate cameraSigma =
                CameraUpdateFactory.newCameraPosition(new CameraPosition(
                        new LatLng(tencentLocation.getLatitude(), tencentLocation.getLongitude()), //中心点坐标，地图目标经纬度
                        18,  //目标缩放级别
                        0f, //目标倾斜角[0.0 ~ 45.0] (垂直地图时为0)
                        0f)); //目标旋转角 0~360° (正北方为0)
        mTencentMap.moveCamera(cameraSigma);
    }

    @Override
    public void onStatusUpdate(String s, int i, String s1) {

    }

    //返回当前位置
    public TencentLocation getMyTencentLocation(){
        return mTencentLocation;
    }
}
