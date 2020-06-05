package cn.pzw.tracklog;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.tencent.lbssearch.TencentSearch;
import com.tencent.lbssearch.httpresponse.HttpResponseListener;
import com.tencent.lbssearch.object.param.BicyclingParam;
import com.tencent.lbssearch.object.param.DrivingParam;
import com.tencent.lbssearch.object.param.RoutePlanningParam;
import com.tencent.lbssearch.object.param.TransitParam;
import com.tencent.lbssearch.object.param.WalkingParam;
import com.tencent.lbssearch.object.result.BicyclingResultObject;
import com.tencent.lbssearch.object.result.DrivingResultObject;
import com.tencent.lbssearch.object.result.RoutePlanningObject;
import com.tencent.lbssearch.object.result.TransitResultObject;
import com.tencent.lbssearch.object.result.WalkingResultObject;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdateFactory;
import com.tencent.tencentmap.mapsdk.maps.MapView;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptorFactory;
import com.tencent.tencentmap.mapsdk.maps.model.CameraPosition;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;
import com.tencent.tencentmap.mapsdk.maps.model.Marker;
import com.tencent.tencentmap.mapsdk.maps.model.MarkerOptions;
import com.tencent.tencentmap.mapsdk.maps.model.Polyline;
import com.tencent.tencentmap.mapsdk.maps.model.PolylineOptions;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import cn.pzw.tracklog.customview.ZoomControl;
import cn.pzw.tracklog.util.LineColorUtil;


/**
 * A simple {@link Fragment} subclass.
 */
public class PolyLineFragment extends Fragment {

    private TencentMap mTencentMap;
    private MapView mMapView;

    private ZoomControl mZoomControl;

    private TextView mTextViewDistance;
    private TextView mTextViewDuration;
    private Button mButtonInfo;

    private LatLng fromLatLng;
    private LatLng toLatLng;

    public static final int DRIVING_PARAM = 0;//驾车
    public static final int WALKING_PARAM = 1;//步行
    public static final int BICYCLING_PARAM = 2;//骑行
    public static final int TRANSIT_PARAM = 3;//公交

    //路线详细步骤
    private ArrayList<String> routingSteps = new ArrayList<>();

    //保存已绘制的路径
    private List<Polyline> polyLines = new ArrayList<>();

    public PolyLineFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_poly_line, container, false);
        mMapView = view.findViewById(R.id.map_view_routing_plane);
        mZoomControl = view.findViewById(R.id.zoom_control_routing_planing_activity);
        mZoomControl.setMap(mMapView);
        mTextViewDistance = view.findViewById(R.id.tv_routing_plane_fragment_distance);
        mTextViewDuration = view.findViewById(R.id.tv_routing_plane_fragment_duration);
        mButtonInfo = view.findViewById(R.id.btn_routing_plane_fragment_info);
        mButtonInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), RouteStepsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putStringArrayList("step", routingSteps);
                intent.putExtra("DATA", bundle);
                getActivity().startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mTencentMap = mMapView.getMap();
        mTencentMap.setMapType(TencentMap.MAP_TYPE_NORMAL);
        mTencentMap.setOnMapLoadedCallback(new TencentMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                processCamera();
            }
        });
        setFormAndToLatLng();
        getRoutingPlane(DRIVING_PARAM);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
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

    //由activity获取经纬度信息
    private void setFormAndToLatLng() {
        RoutingPlaneActivity routingPlaneActivity = (RoutingPlaneActivity) getActivity();
        fromLatLng = routingPlaneActivity.getFromLatLng();
        toLatLng = routingPlaneActivity.getToLatLng();
    }

    //设置初始视野范围恰好包含起点和终点
    private void processCamera() {
        List<LatLng> points = new ArrayList<>();
        points.add(fromLatLng);
        points.add(toLatLng);
        markerStartPoint();
        markerEndPoint();
        CameraPosition cameraPosition = mTencentMap.calculateZoomToSpanLevel(null, points, 80, 80, 80, 80);
        mTencentMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    //交换起点和终点
    private void exchangePoints() {
        LatLng temp = fromLatLng;
        fromLatLng = toLatLng;
        toLatLng = temp;
    }

    //给起点设置标记
    private void markerStartPoint() {
        Marker startMarker = mTencentMap.addMarker(
                new MarkerOptions(fromLatLng).icon(BitmapDescriptorFactory.fromAsset("route_ic_marker_start.png"))
        );
    }

    //给终点设置标记
    private void markerEndPoint() {
        Marker endMarker = mTencentMap.addMarker(
                new MarkerOptions(toLatLng).icon(BitmapDescriptorFactory.fromAsset("route_ic_marker_end.png"))
        );
    }

    //根据传入的类型进行路线规划
    public void getRoutingPlane(int type) {
        switch (type) {
            case DRIVING_PARAM:
                DrivingParam drivingParam = new DrivingParam(fromLatLng, toLatLng);
                drivingParam.roadType(DrivingParam.RoadType.ON_MAIN_ROAD_BELOW_BRIDGE);
                drivingParam.heading(90);
                drivingParam.accuracy(30);
                drivingParam.policy(DrivingParam.Policy.LEAST_TIME, DrivingParam.Preference.REAL_TRAFFIC);
                getRoutingPlaneObject(drivingParam, DRIVING_PARAM);
                break;
            case WALKING_PARAM:
                WalkingParam walkingParam = new WalkingParam(fromLatLng, toLatLng);
                getRoutingPlaneObject(walkingParam, WALKING_PARAM);
                break;
            case BICYCLING_PARAM:
                BicyclingParam bicyclingParam = new BicyclingParam(fromLatLng, toLatLng);
                getRoutingPlaneObject(bicyclingParam, BICYCLING_PARAM);
                break;
            case TRANSIT_PARAM:
                TransitParam transitParam = new TransitParam(fromLatLng, toLatLng);
                transitParam.policy(TransitParam.Policy.LEAST_TIME, TransitParam.Preference.NO_SUBWAY);
                getRoutingPlaneObject(transitParam, TRANSIT_PARAM);
                break;
            default:
                break;
        }
    }

    //获取路线规划
    private void getRoutingPlaneObject(RoutePlanningParam param, final int type) {
        TencentSearch tencentSearch = new TencentSearch(getContext());
        tencentSearch.getRoutePlan(param, new HttpResponseListener() {
            @Override
            public void onSuccess(int i, Object object) {
                if (object == null) {
                    clearPolyLines();
                    Toast.makeText(MyApplication.getContext(), "未找到合适的路线！", Toast.LENGTH_SHORT).show();
                    return;
                }
                switch (type) {
                    case DRIVING_PARAM:
                        DrivingResultObject drivingResultObject = (DrivingResultObject) object;
                        clearPolyLines();

                        Log.e("Driving", "----" + drivingResultObject.result.routes.size());

                        for (DrivingResultObject.Route route : drivingResultObject.result.routes) {
                            List<LatLng> drivingLines = route.polyline;
                            drawPolyLine(drivingLines, LineColorUtil.getRandomColor());

                            setDistance(route.distance);
                            setDuration(route.duration);

                            //获取文字描述信息
                            routingSteps.clear();
                            for (RoutePlanningObject.Step step : route.steps) {
                                routingSteps.add(step.instruction);
                            }

                        }
                        break;
                    case WALKING_PARAM:
                        WalkingResultObject walkingResultObject = (WalkingResultObject) object;
                        clearPolyLines();

                        Log.e("Walking", "-----" + walkingResultObject.result.routes.size());

                        for (WalkingResultObject.Route route : walkingResultObject.result.routes) {
                            List<LatLng> walkingLines = route.polyline;

                            setDistance(route.distance);
                            setDuration(route.duration);

                            drawPolyLine(walkingLines, LineColorUtil.getRandomColor());

                            //获取文字描述信息
                            routingSteps.clear();
                            for (RoutePlanningObject.Step step : route.steps) {
                                routingSteps.add(step.instruction);
                            }

                        }
                        break;
                    case BICYCLING_PARAM:
                        BicyclingResultObject bicyclingResultObject = (BicyclingResultObject) object;
                        clearPolyLines();

                        Log.e("Bicycling", "-----" + bicyclingResultObject.result.routes.size());

                        for (BicyclingResultObject.Route route : bicyclingResultObject.result.routes) {
                            List<LatLng> bicyclingLines = route.polyline;

                            setDistance(route.distance);
                            setDuration(route.duration);

                            drawPolyLine(bicyclingLines, LineColorUtil.getRandomColor());

                            //获取文字描述信息
                            routingSteps.clear();
                            for (RoutePlanningObject.Step step : route.steps) {
                                routingSteps.add(step.instruction);
                            }

                        }
                        break;
                    case TRANSIT_PARAM:
                        TransitResultObject transitResultObject = (TransitResultObject) object;
                        clearPolyLines();

                        Log.e("Transiting", "-----" + transitResultObject.result.routes.size());

//                        for (TransitResultObject.Route route : transitResultObject.result.routes) {
                        TransitResultObject.Route route = transitResultObject.result.routes.get(0); //这里只取第一条规划路线
                            setDistance(route.distance);
                            setDuration(route.duration);

                            List<TransitResultObject.Segment> segments = route.steps;

                            int color = LineColorUtil.getRandomColor();
                            routingSteps.clear();
                            for (TransitResultObject.Segment segment : segments) {
                                if (segment.mode.equals("WALKING")) {
                                    TransitResultObject.Walking walking = (TransitResultObject.Walking) segment;
                                    List<LatLng> walkingPartLines = walking.polyline;
                                    drawPolyLine(walkingPartLines, color);

                                    //获取文字描述信息
                                    if (walking.steps != null)
                                        for (RoutePlanningObject.Step step : walking.steps) {
                                            routingSteps.add(step.instruction);
                                        }
                                }
                                if (segment.mode.equals("TRANSIT")) {
                                    TransitResultObject.Transit transit = (TransitResultObject.Transit) segment;
                                    List<TransitResultObject.Line> transitLines = transit.lines;
                                    for (TransitResultObject.Line line : transitLines) {
                                        List<LatLng> transitLineParts = line.polyline;
                                        drawPolyLine(transitLineParts, color);

                                        //获取文字描述信息
                                        routingSteps.add("乘坐 "+line.title+" ,终点为 "+line.destination.title+" 站,预计用时 "+line.duration+" 分钟");
                                        routingSteps.add("从 " + line.geton.title + " 站出发");
                                        for (TransitResultObject.Station station : line.stations) {
                                            routingSteps.add("途径 " + station.title + " 站");
                                        }
                                        routingSteps.add("到 " + line.getoff.title + " 站下车");

                                    }
                                }
                            }

//                        }
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onFailure(int i, String s, Throwable throwable) {
                clearPolyLines();
                Toast.makeText(MyApplication.getContext(), s, Toast.LENGTH_SHORT).show();
                Log.e("Routing", "failed------- " + s);
            }
        });
    }

    //清除之前的路线
    private void clearPolyLines() {
        if (polyLines.size() != 0) {
            for (Polyline polyline : polyLines) {
                polyline.remove();
            }
            polyLines.clear();
        }
    }

    //根据坐标点集合画图
    private void drawPolyLine(List<LatLng> lines, int color) {
        Polyline line = mTencentMap.addPolyline(
                new PolylineOptions()
                        .addAll(lines)
                        .color(color)
                        .borderWidth(2)
                        .borderColor(Color.RED)
                        .width(25)
                        .arrow(true)
                        .arrowSpacing(90)
                        .arrowTexture(BitmapDescriptorFactory.fromAsset("color_arrow_texture.png"))
                        .lineCap(true)
        );
        polyLines.add(line);
    }

    //总里程信息
    private void setDistance(float distance) {
        mTextViewDistance.setText(formatDis((int) distance));
    }

    //总共需要时长
    private void setDuration(float duration) {
        mTextViewDuration.setText(formatTime((int) duration));
    }

    //格式化距离
    private String formatDis(int dis) {
        DecimalFormat df = new DecimalFormat("0.0");
        String distance;
        if (dis >= 1000) {
            distance = df.format(dis / 1000.0) + "公里";
        } else {
            distance = +dis + "米";
        }
        return distance;
    }

    //设置距离时间输出样式
    private String formatTime(int time) {

        String content = "";
        if (time > 60) {
            int hour = time / 60;
            int minute = time % 60;
            content += hour + "小时" + minute + "分钟";
        } else {
            content += time + "分钟";
        }
        return content;
    }

}
