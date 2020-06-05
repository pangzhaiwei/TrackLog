package cn.pzw.tracklog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdate;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdateFactory;
import com.tencent.tencentmap.mapsdk.maps.LocationSource;
import com.tencent.tencentmap.mapsdk.maps.MapView;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptorFactory;
import com.tencent.tencentmap.mapsdk.maps.model.CameraPosition;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;
import com.tencent.tencentmap.mapsdk.maps.model.MapPoi;
import com.tencent.tencentmap.mapsdk.maps.model.Marker;
import com.tencent.tencentmap.mapsdk.maps.model.MarkerOptions;
import com.tencent.tencentmap.mapsdk.maps.model.MyLocationStyle;

import java.util.ArrayList;
import java.util.List;

import cn.pzw.tracklog.bean.LocationBean;
import cn.pzw.tracklog.customview.PoiClickedView;
import cn.pzw.tracklog.customview.ZoomControl;
import cn.pzw.tracklog.util.SingleMyLocation;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainMapFragment extends Fragment implements LocationSource, TencentLocationListener {

    private MapView mMapView;
    private TencentMap mTencentMap;

    //方向
    private DirectionView mDirectionView;

    //连续定位部分
    private TencentLocationManager locationManager;
    private TencentLocationRequest locationRequest;
    private OnLocationChangedListener mChangedListener;

    //我当前的位置信息
    private TencentLocation myLocation;
    private SingleMyLocation singleMyLocation;

    //自定义的底部poi信息栏
    private PoiClickedView mPoiClickedView;

    //地图当前中心点经纬度
    private LocationBean mLocationBeanCenter;

    //保存点击MapPoi产生的点标记
    private static List<Marker> markers = new ArrayList<>();

    public MainMapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_base_map, container, false);
        mMapView = view.findViewById(R.id.map_view);

        mDirectionView = view.findViewById(R.id.direction_view_pointer);

        ZoomControl zoomControl = view.findViewById(R.id.zoom_control);//自定义的缩小放大栏
        zoomControl.setMap(mMapView);

        mPoiClickedView = view.findViewById(R.id.poi_clicked_view);
        mPoiClickedView.setBaseMapFragment(this);

        initFabMyLocation(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        userSetting();

        gotoMyLocation();

    }

    private void gotoMyLocation() {
        singleMyLocation = new SingleMyLocation(getActivity(), mTencentMap);
        singleMyLocation.startSingleLocation();
    }

    private void userSetting() {
        mTencentMap = mMapView.getMap();

        locationManager = TencentLocationManager.getInstance(getContext());
        locationRequest = TencentLocationRequest.create()
                .setRequestLevel(TencentLocationRequest.REQUEST_LEVEL_GEO)
                .setInterval(500)
                .setAllowDirection(true);
        mTencentMap.setLocationSource(this);

        mTencentMap.setMyLocationEnabled(true);
        MyLocationStyle myStyle = new MyLocationStyle()
                .myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW_NO_CENTER)
                .icon(BitmapDescriptorFactory.fromBitmap(getBitMap(R.drawable.mark_location)));
        mTencentMap.setMyLocationStyle(myStyle);
        mTencentMap.getUiSettings().setCompassEnabled(true);
        mTencentMap.getUiSettings().setZoomGesturesEnabled(true);

        mTencentMap.setOnMapPoiClickListener(new MyOnOpiClickListener());
        mTencentMap.setOnCameraChangeListener(new MyOnCameraChangeListener());
    }

    private Bitmap getBitMap(int resourceId){
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resourceId);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newWidth = 55;
        int newHeight = 55;
        float widthScale = ((float)newWidth)/width;
        float heightScale = ((float)newHeight)/height;
        Matrix matrix = new Matrix();
        matrix.postScale(widthScale, heightScale);
        bitmap = Bitmap.createBitmap(bitmap,0,0,width,height,matrix,true);
        return bitmap;
    }

    //将镜头移到指定的点
    public void moveTo(LatLng latLng) {
        CameraUpdate cameraSigma =
                CameraUpdateFactory.newCameraPosition(new CameraPosition(
                        latLng, //中心点坐标，地图目标经纬度
                        18,  //目标缩放级别
                        0f, //目标倾斜角[0.0 ~ 45.0] (垂直地图时为0)
                        0f)); //目标旋转角 0~360° (正北方为0)
        mTencentMap.animateCamera(cameraSigma);
    }

    private void initFabMyLocation(View view) {
        FloatingActionButton mFabMyLocation = view.findViewById(R.id.fab_my_location);
        mFabMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoMyLocation();
            }
        });
    }

    void setModeNormal() {
        mTencentMap.setMapType(TencentMap.MAP_TYPE_NORMAL);
    }

    void setModeSatellite() {
        mTencentMap.setMapType(TencentMap.MAP_TYPE_SATELLITE);
    }

    boolean getTrafficState() {
        return mTencentMap.isTrafficEnabled();
    }

    void setTraffic(boolean flag) {
        mTencentMap.setTrafficEnabled(flag);
    }

    void setBuilding(boolean flag) {
        mTencentMap.setBuildingEnable(flag);
    }

    @Override
    public void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
        mMapView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(locationRequest, this);
        mMapView.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    //给MapPoi添加点标记
    public void markerPoi(MapPoi mapPoi){
        unmarkerPoi();
        markers.add(mTencentMap.addMarker(new MarkerOptions(new LatLng(mapPoi.position))));
        if (mPoiClickedView.getVisibility() != View.VISIBLE){
            mPoiClickedView.setVisibility(View.VISIBLE);
            mPoiClickedView.playOpenAnimation();
        }
        mPoiClickedView.setMapPoi(mapPoi);
        mPoiClickedView.updateTitle();
        moveTo(markers.get(0).getPosition());
    }

    //取消所有点击产生的MapPoi点标记
    public void unmarkerPoi(){
        if (markers.size() != 0){
            for (Marker marker : markers){
                marker.remove();
            }
            markers.clear();
        }
    }

    //获取当前位置
    public LocationBean getMyLocation(){
        myLocation = singleMyLocation.getMyTencentLocation();
        return new LocationBean("我的位置", myLocation.getLatitude(), myLocation.getLongitude());
    }

    //获取地图中心点
    public LocationBean getCenterLocation(){
        return mLocationBeanCenter;
    }

    @Override
    public void onLocationChanged(TencentLocation tencentLocation, int i, String s) {
        if (i == TencentLocation.ERROR_OK && mChangedListener != null){
            Location location = new Location(tencentLocation.getProvider());
            location.setLatitude(tencentLocation.getLatitude());
            location.setLongitude(tencentLocation.getLongitude());
            location.setAccuracy(tencentLocation.getAccuracy());
            location.setBearing(tencentLocation.getBearing());
            mChangedListener.onLocationChanged(location);
            //通过gps定位获得方向
            if ("gps".equals(tencentLocation.getProvider())){
                double direction = tencentLocation.getExtra().getDouble(TencentLocation.EXTRA_DIRECTION);
                mDirectionView.updateDirection(direction);
            }else {
                mDirectionView.updateDirection(0);
            }
        }else {
            mDirectionView.updateDirection(0);
        }
    }

    @Override
    public void onStatusUpdate(String s, int i, String s1) {
        //ignore
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mChangedListener = onLocationChangedListener;

        //开启定位
        int err = locationManager.requestLocationUpdates(locationRequest, this);
        switch (err) {
            case 1:
                Toast.makeText(MyApplication.getContext(),
                        "设备缺少使用腾讯定位服务需要的基本条件",
                        Toast.LENGTH_SHORT).show();
                break;
            case 2:
                Toast.makeText(MyApplication.getContext(),
                        "manifest 中配置的 key 不正确", Toast.LENGTH_SHORT).show();
                break;
            case 3:
                Toast.makeText(MyApplication.getContext(),
                        "自动加载libtencentloc.so失败", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    @Override
    public void deactivate() {
        locationManager.removeUpdates(this);
        locationManager = null;
        locationRequest = null;
        mChangedListener = null;
    }

    //Poi点击事件
    class MyOnOpiClickListener implements TencentMap.OnMapPoiClickListener{

        @Override
        public void onClicked(MapPoi mapPoi) {
            markerPoi(mapPoi);
        }

    }

    //视野改变状态下监听当前地图中心点的经纬度
    class MyOnCameraChangeListener implements TencentMap.OnCameraChangeListener{
        @Override
        public void onCameraChange(CameraPosition cameraPosition) {

        }

        @Override
        public void onCameraChangeFinished(CameraPosition cameraPosition) {
            //在这里获取当前地图中心的经纬度
            mLocationBeanCenter = new LocationBean("center", cameraPosition.target.getLatitude(), cameraPosition.target.getLongitude());
        }
    }

    public static class DirectionView extends View {
        private static final float OFFSET = 0f;

        private Handler mHandler;

        private Paint mPaint;
        private double mDir;
        private Bitmap mBmp;

        private int mBmpW;
        private int mBmpH;

        public void updateDirection(double direction) {
            if (!Double.isNaN(direction)) {
                mDir = direction;
                invalidate();
            }
        }

        public DirectionView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init(context);
        }



        public DirectionView(final Context context) {
            super(context);
            init(context);
        }

        @SuppressLint("HandlerLeak")
        private void init(final Context context) {
            // onLocationChanged() 仅在"位置"发生变化时回调
            // 要实现更流畅的指南针效果, 可通过定时消息, 加快获取"方向"
            mHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    TencentLocation location = TencentLocationManager.getInstance(context).getLastKnownLocation();
                    if (location != null) {
                        double direction = location.getExtra().getDouble(TencentLocation.EXTRA_DIRECTION);
                        updateDirection(direction);
                    }
                }
            };

            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(2f);
            mPaint.setColor(Color.RED);

            Bitmap origin = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_pointer);
            mBmp = scaleBitmap(origin);
            mBmpW = mBmp.getWidth() / 2;
            mBmpH = mBmp.getHeight() / 2;
        }

        //对原始图片进行缩小操作
        private Bitmap scaleBitmap(Bitmap bitmap) {
            Matrix matrix = new Matrix();
            matrix.postScale(0.1f, 0.1f); //根据控件的大小将原图缩小到原来的0.1
            Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return newBitmap;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int w = getMeasuredWidth();
            int h = getMeasuredHeight();

            int r = w < h ? w / 2 : h / 2;
            r -= 30;
            canvas.drawCircle((float)w / 2, (float)h / 2, r, mPaint);
            canvas.save();

            // 调整图标箭头原本方向调整至指向正北，第一个参数是需要调整的角度
            canvas.rotate(-45, (float)w / 2, (float)h / 2);

            // 根据定位SDK获得的方向旋转箭头
            canvas.rotate((float) mDir, (float)w / 2, (float)h / 2);

            canvas.translate((float)w / 2 - mBmpW, (float)h / 2 - mBmpH);
            canvas.drawBitmap(mBmp, 0, 0, null);
            canvas.restore();

            mHandler.sendEmptyMessageDelayed(0, 50);
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            mHandler.removeCallbacksAndMessages(null);
        }

        private float getRawSize(int unit, float size) {
            Context c = getContext();
            Resources r;

            if (c == null)
                r = Resources.getSystem();
            else
                r = c.getResources();

            return TypedValue.applyDimension(unit, size, r.getDisplayMetrics());
        }

    }

}
