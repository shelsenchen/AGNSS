package com.lx.agnss;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;

import android.graphics.Canvas;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.PixelCopy;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.Sun;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.lx.agnss.service.impl.DemoUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import uk.co.appoly.arcorelocation.LocationMarker;
import uk.co.appoly.arcorelocation.LocationScene;
import uk.co.appoly.arcorelocation.rendering.LocationNode;
import uk.co.appoly.arcorelocation.rendering.LocationNodeRender;
import uk.co.appoly.arcorelocation.utils.ARLocationPermissionHelper;

/**
 * This class for the main activity job.
 */
public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private boolean installRequested;
    private boolean hasFinishedLoading = false;
    private Snackbar loadingMessageSnackbar = null;

    // Sceenform fragment
    private ArFragment arFragment;
    private ArSceneView arSceneView;
    private LocationScene locationScene;

    // Renderablesfor this app
    private ModelRenderable andyRenderable;
    private ViewRenderable popupLayoutRenderable;
    private ModelRenderable iconRenderable;
    private ModelRenderable distIconRenderable;

    // 용산 전주 model
    private ModelRenderable mrJeonJuBuildingDEM;
    private ModelRenderable mrJeonJuPointOut;
    private ModelRenderable mrYongSanBuildingDEM;
    private ModelRenderable mrYongSanPointOut;

    private boolean boolJeonJuBuildingDEM = false;
    private boolean boolJeonJuPointOut = false;
    private boolean boolYongSanBuildingDEM = false;
    private boolean boolYongSanPointOut = false;

    //메뉴버튼
    private Button btnMenu01;
    private Button btnMenu02;
    private Button btnMenu03;
    private Button btnMenu04;
    private Button btnMenu05;
    private boolean chkDist = false;

    // 지적 건물 표출용 임시 버튼
    private Button btnJeonJuBuildingDEM;
    private Button btnJeonJuPointOut;
    private Button btnYongSanBuildingDEM;
    private Button btnYongSanPointOut;

    private TextView locationView;
    private TextView distanceView;

    // 거리측정용 변수 선언
    private Pose startPose = null;
    private Pose endPose = null;
    private Anchor distanceAnchor;

    //GoogleMap 변수 선언
    private MapFragment mapFragment;
    private GoogleMap mMap;
    private MapView mapView;
    private static final String MAP_VIEW_BUNDLE_KEY = "AIzaSyDFIvA8d4BKkrjXj_WYd_EDPFdxkOS4ww8";
    private LatLng currentPostion;
    private LocationManager locationManager;
    private LocationListener locationListener;

    //permmition
    final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE_ = 1001;
    final int REQUEST_PERMISSION_ACCESS_FINE_LOCATION_ = 1002;
    final int PERMISSION = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //test update

        /* Reqeust Permission */
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA}, PERMISSION);
        }
        /* Request Permission End */

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        arSceneView = arFragment.getArSceneView();

        btnMenu01 = (Button) findViewById(R.id.btnMenu01);
        btnMenu02 = (Button) findViewById(R.id.btnMenu02);
        btnMenu03 = (Button) findViewById(R.id.btnMenu03);
        btnMenu04 = (Button) findViewById(R.id.btnMenu04);
        btnMenu05 = (Button) findViewById(R.id.btnMenu05);

        // 지적 & 건물정보 표출 임시버튼
        btnJeonJuBuildingDEM = (Button) findViewById(R.id.btnJeonJuBuildingDEM_0);
        btnJeonJuPointOut = (Button) findViewById(R.id.btnJeonJuPointOut_0);
        btnYongSanBuildingDEM = (Button) findViewById(R.id.btnYoungSanBuildingDEM_0);
        btnYongSanPointOut = (Button) findViewById(R.id.btnYoungSanPointOut_0);

        locationView = (TextView) findViewById(R.id.locationView);
        distanceView = (TextView) findViewById(R.id.distanceView);

        /* 기본 마커 아이콘 */
        ModelRenderable.builder()
                //.setSource(this,R.raw.andy)
                .setSource(this, Uri.parse("default_icon.sfb"))
                .build()
                .thenAccept(renderable -> iconRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unalbe to load icon renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        }
                );

        /* 거리측정용 마커 아이콘*/
        ModelRenderable.builder()
                //.setSource(this,R.raw.andy)
                .setSource(this, Uri.parse("reverse_drop.sfb"))
                .build()
                .thenAccept(renderable -> distIconRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unalbe to load icon renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        }
                );

        /* 전주 건물
         * private ModelRenderable mrJeonJuBuildingDEM;
         **/
        ModelRenderable.builder()
                //.setSource(this,R.raw.andy)
                .setSource(this, Uri.parse("reverse_drop.sfb"))
                .build()
                .thenAccept(renderable -> mrJeonJuBuildingDEM = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unalbe to load icon renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        }
                );

        /* 전주 지적
         * private ModelRenderable mrJeonJuPointOut;
         **/
        ModelRenderable.builder()
                //.setSource(this,R.raw.andy)
                .setSource(this, Uri.parse("reverse_drop.sfb"))
                .build()
                .thenAccept(renderable -> mrJeonJuPointOut = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unalbe to load icon renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        }
                );

        /* 용산 건물
         * private ModelRenderable mrYongSanBuildingDEM;
         * private ModelRenderable mrYongSanPointOut;
         **/
        ModelRenderable.builder()
                //.setSource(this,R.raw.andy)
                .setSource(this, Uri.parse("YongSanBuildingDEM_0.sfb"))
                .build()
                .thenAccept(renderable -> mrYongSanBuildingDEM = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unalbe to load icon renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        }
                );

        /* 용산 지적
         * private ModelRenderable mrYongSanPointOut;
         **/
        ModelRenderable.builder()
                //.setSource(this,R.raw.andy)
                .setSource(this, Uri.parse("YongSanPointOut_0.sfb"))
                .build()
                .thenAccept(renderable -> mrYongSanPointOut = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unalbe to load icon renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        }
                );

        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plan, MotionEvent motionEvent) -> {
                    if (iconRenderable == null || distIconRenderable == null) {
                        return;
                    }

                    if (!chkDist) {
                        Anchor anchor = hitResult.createAnchor();
                        AnchorNode anchorNode = new AnchorNode(anchor);
                        anchorNode.setParent(arFragment.getArSceneView().getScene());

                        TransformableNode icon = new TransformableNode(arFragment.getTransformationSystem());
                        icon.setLocalRotation(Quaternion.axisAngle(new Vector3(0, 1f, 0), 90f));
                        icon.setParent(anchorNode);
                        icon.setRenderable(iconRenderable);
                        icon.select();
                    } else if (chkDist) {
                        distanceAnchor = hitResult.createAnchor();
                        AnchorNode anchorNode = new AnchorNode(distanceAnchor);
                        anchorNode.setParent(arFragment.getArSceneView().getScene());

                        TransformableNode distIcon = new TransformableNode(arFragment.getTransformationSystem());
                        distIcon.setLocalRotation(Quaternion.axisAngle(new Vector3(0, 1f, 0), 90f));
                        distIcon.setParent(anchorNode);
                        distIcon.setRenderable(distIconRenderable);
                        distIcon.select();

                        if (startPose == null) {
                            startPose = hitResult.getHitPose();
                            distanceView.setText("두번째 지점을 선택해 주세요");
                        } else if (startPose != null) {
                            endPose = hitResult.getHitPose();
                            addLineBetweenPoints(arFragment.getArSceneView().getScene(), startPose, endPose);

                            double distanceM = Math.sqrt(Math.pow((startPose.tx() - endPose.tx()), 2) +
                                    Math.pow((startPose.ty() - endPose.ty()), 2) +
                                    Math.pow((startPose.tz() - endPose.tz()), 2));

                            startPose = null;

                            distanceView.setText("거리 : " + String.format("%.2f", distanceM) + "m");


                        }
                    }

                    // 전주 건물
                    if (mrJeonJuBuildingDEM != null && boolJeonJuBuildingDEM == true) {
                        Anchor makeAnchor = hitResult.createAnchor();
                        AnchorNode anchorNode = new AnchorNode(makeAnchor);
                        anchorNode.setParent(arFragment.getArSceneView().getScene());

                        TransformableNode distIcon = new TransformableNode(arFragment.getTransformationSystem());
                        distIcon.setParent(anchorNode);
                        distIcon.setRenderable(mrJeonJuBuildingDEM);
                        distIcon.select();
                    }

                    // 전주 지적
                    if (mrJeonJuPointOut != null && boolJeonJuPointOut == true) {
                        Anchor makeAnchor = hitResult.createAnchor();
                        AnchorNode anchorNode = new AnchorNode(makeAnchor);
                        anchorNode.setParent(arFragment.getArSceneView().getScene());

                        TransformableNode distIcon = new TransformableNode(arFragment.getTransformationSystem());
                        distIcon.setParent(anchorNode);
                        distIcon.setRenderable(mrJeonJuPointOut);
                        distIcon.select();
                    }

                    //용산 건물
                    if (mrYongSanBuildingDEM != null && boolYongSanBuildingDEM == true) {
                        Anchor makeAnchor = hitResult.createAnchor();
                        AnchorNode anchorNode = new AnchorNode(makeAnchor);
                        anchorNode.setParent(arFragment.getArSceneView().getScene());

                        TransformableNode distIcon = new TransformableNode(arFragment.getTransformationSystem());
                        distIcon.setParent(anchorNode);
                        distIcon.setRenderable(mrYongSanBuildingDEM);
                        distIcon.select();
                    }

                    // 용산 지적
                    if (mrYongSanPointOut != null && boolYongSanPointOut == true) {
                        Anchor makeAnchor = hitResult.createAnchor();
                        AnchorNode anchorNode = new AnchorNode(makeAnchor);
                        anchorNode.setParent(arFragment.getArSceneView().getScene());

                        TransformableNode distIcon = new TransformableNode(arFragment.getTransformationSystem());
                        distIcon.setParent(anchorNode);
                        distIcon.setRenderable(mrYongSanPointOut);
                        distIcon.select();
                    }


                }
        );

        /*
         ***   메뉴버튼 이벤트 처리 시작  ***
         */
        btnMenu01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 마커측점
                Toast.makeText(getApplicationContext(), "버튼1(마커측점) 클릭", Toast.LENGTH_SHORT).show();
            }
        });

        btnMenu02.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 좌표측정
                Toast.makeText(getApplicationContext(), "버튼2(좌표측정) 클릭", Toast.LENGTH_SHORT).show();
            }
        });

        btnMenu03.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 거리측정
                if (chkDist) {
                    chkDist = false;
                    btnMenu03.setBackgroundColor(getResources().getColor(R.color.btnBackground_off));
                    distanceView.setVisibility(View.INVISIBLE);
                    distanceView.setText("");
                    onClear();
                } else if (!chkDist) {
                    chkDist = true;
                    btnMenu03.setBackgroundColor(getResources().getColor(R.color.btnBackground_on));
                    distanceView.setVisibility(View.VISIBLE);
                    distanceView.setText("첫번째 지점을 선택해 주세요");
                }
                //Toast.makeText(getApplicationContext(), "버튼3(거리측정) 클릭",Toast.LENGTH_SHORT).show();
            }
        });

        btnMenu04.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 화면캡쳐
                //Toast.makeText(getApplicationContext(), "버튼4(화면캡쳐) 클릭",Toast.LENGTH_SHORT).show();
                takePhoto();
            }
        });

        btnMenu05.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // LX토지알림e 연동
                callLx();
                //Toast.makeText(getApplicationContext(), "버튼5(LX토지알림e 연동) 클릭",Toast.LENGTH_SHORT).show();
            }
        });

        btnJeonJuBuildingDEM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onClear();

                Scene scene = arFragment.getArSceneView().getScene();
                Quaternion camQ = scene.getCamera().getWorldRotation();

                float[] f1 = new float[]{camQ.x, camQ.y, camQ.z};
                float[] f2 = new float[]{camQ.x, camQ.y, camQ.z, 90f};
                Pose anchorPose = new Pose(f1, f2);

                Anchor anchor = arFragment.getArSceneView().getSession().createAnchor(anchorPose);

                placeObject(arFragment, anchor, Uri.parse("reverse_drop.sfb"));

            }
        });

        btnJeonJuPointOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onClear();

                Scene scene = arFragment.getArSceneView().getScene();
                Quaternion camQ = scene.getCamera().getWorldRotation();

                float[] f1 = new float[]{camQ.x, camQ.y, camQ.z};
                float[] f2 = new float[]{camQ.x, camQ.y, camQ.z, 90f};
                Pose anchorPose = new Pose(f1, f2);

                Anchor anchor = arFragment.getArSceneView().getSession().createAnchor(anchorPose);

                placeObject(arFragment, anchor, Uri.parse("reverse_drop.sfb"));
            }
        });

        btnYongSanBuildingDEM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onClear();

                Scene scene = arFragment.getArSceneView().getScene();
                Quaternion camQ = scene.getCamera().getWorldRotation();

                float[] f1 = new float[]{camQ.x, camQ.y, camQ.z};
                float[] f2 = new float[]{camQ.x, camQ.y, camQ.z, 90f};
                Pose anchorPose = new Pose(f1, f2);

                Anchor anchor = arFragment.getArSceneView().getSession().createAnchor(anchorPose);

                placeObject(arFragment, anchor, Uri.parse("YoungSanBuildingDEM_0.sfb"));
            }
        });

        btnYongSanPointOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onClear();

                Scene scene = arFragment.getArSceneView().getScene();
                Quaternion camQ = scene.getCamera().getWorldRotation();

                float[] f1 = new float[]{camQ.x, camQ.y, camQ.z};
                float[] f2 = new float[]{camQ.x, camQ.y, camQ.z, 90f};
                Pose anchorPose = new Pose(f1, f2);

                Anchor anchor = arFragment.getArSceneView().getSession().createAnchor(anchorPose);

                placeObject(arFragment, anchor, Uri.parse("YoungSanPointOut_0.sfb"));
            }
        });

        /*
         ***   메뉴버튼 이벤트 처리 끝   ***
         */

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentPostion = new LatLng(location.getLatitude(), location.getLongitude());
                //mMap.moveCamera(CameraUpdateFactory.newLatLng(currentPostion));
                mMap.animateCamera(CameraUpdateFactory.newLatLng(currentPostion));
                //info03.setText("Latitude:" + location.getLatitude() + ", Longitude:" + location.getLongitude());
                Log.d("onLocationChanged", "Latitude:" + location.getLatitude() + ", Longitude:" + location.getLongitude());
                locationView.setText("Lon : " + location.getLongitude() + ", Lat : " + location.getLatitude());

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        /* 퍼미션 설정 */
        if (locationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER))
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        if (locationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER))
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        /* 퍼미션 설정 끝 */

        /*
         * 건물 및 마커정보 표출 시작
         * */
        // Build a renderable from a 2D View.
        CompletableFuture<ViewRenderable> popupLayout =
                ViewRenderable.builder()
                        .setView(this, R.layout.popup_layout)
                        .build();

        // When you build a Renderable, Sceneform loads its resources in the background while returning
        // a CompletableFuture. Call thenAccept(), handle(), or check isDone() before calling get().
        CompletableFuture<ModelRenderable> andy = ModelRenderable.builder()
                .setSource(this, R.raw.andy)
                .build();

        CompletableFuture.allOf(popupLayout, andy)
                .handle(
                        (notUsed, throwable) -> {
                            // When you build a Renderable, Sceneform loads its resources in the background while
                            // returning a CompletableFuture. Call handle(), thenAccept(), or check isDone()
                            // before calling get().

                            if (throwable != null) {
                                DemoUtils.displayError(this, "Unalbe to load renderables", throwable);
                                return null;
                            }

                            try {
                                popupLayoutRenderable = popupLayout.get();
                                Log.e("try success", "complete get popupLayout");
                                //andyRenderable = andy.get();
                                hasFinishedLoading = true;

                            } catch (InterruptedException | ExecutionException ex) {
                                Log.e("error", "Unable to load renderables");
                                DemoUtils.displayError(this, "Unable to load renderables", ex);
                            }

                            return null;
                        }
                );

        // Set an update listener on the Scene that will hide the loading message once a Plane is
        // detected.
        arSceneView
                .getScene()
                .addOnUpdateListener(
                        frameTime -> {

                            if (!hasFinishedLoading) {
                                return;
                            }

                            if (locationScene == null) {

                                // If our locationScene object hasn't been setup yet, this is a good time to do it
                                // We know that here, the AR components have been initiated.
                                locationScene = new LocationScene(this, this, arSceneView);

                                // Now lets create our location markers.
                                // First, a layout
                                List<LocationMarker> LLM = new ArrayList<LocationMarker>();
                                LocationMarker layoutLocationMarker = new LocationMarker(
                                        37.532946, 126.959868,
                                        getExampleView()
                                );

                                // An example "onRender" event, called every frame
                                // Updates the layout with the markers distance
                                layoutLocationMarker.setRenderEvent(new LocationNodeRender() {
                                    @Override
                                    public void render(LocationNode locationNode) {
                                        View eView = popupLayoutRenderable.getView();
                                        TextView distanceTextView = eView.findViewById(R.id.textView2);
                                        TextView nameView = eView.findViewById(R.id.textView1);
                                        TextView addrView = eView.findViewById(R.id.textView3);
                                        nameView.setText("롯데시네마 용산");
                                        distanceTextView.setText("lon : 37.532946\nlat : 126.959868");
                                        addrView.setText("주소 : 서울특별시 용산구 한강로3가 청파로 74");
                                    }
                                });


                                // Adding the marker
                                //locationScene.mLocationMarkers.add(layoutLocationMarker);

                                LLM.add(layoutLocationMarker);

                                locationScene.mLocationMarkers.addAll(LLM);

                                // Adding a simple location marker of a 3D model
//                                locationScene.mLocationMarkers.add(
//                                        new LocationMarker(
//                                                37.399543,
//                                                127.107045,
//                                                getAndy()));

                            }

                            Frame frame = arSceneView.getArFrame();

                            if (frame == null) {
                                return;
                            }

                            if (frame.getCamera().getTrackingState() != TrackingState.TRACKING) {
                                return;
                            }

                            if (locationScene != null) {
                                locationScene.processFrame(frame);
                            }

                            if (loadingMessageSnackbar != null) {
                                for (Plane plane : frame.getUpdatedTrackables(Plane.class)) {
                                    if (plane.getTrackingState() == TrackingState.TRACKING) {
                                        hideLoadingMessage();
                                    }
                                }
                            }


                        });
        // Lastly request CAMERA & fine location permission which is required by ARCore-Location.
        ARLocationPermissionHelper.requestPermission(this);
        /*
         * 건물 및 마커정보 끝
         * */

    }
    /* OnCreate End */

    /* 지도 시작 */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }

        //mapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMinZoomPreference(18);
        mMap.setMyLocationEnabled(true);

        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setCompassEnabled(true);


        //seattle coordinates 37.400075, 127.103346
        LatLng seattle = new LatLng(38.400075, 127.103346);
        //mMap.addMarker(new MarkerOptions().position(seattle).title("Seattle"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(seattle));





      /*
      float brearing = mMap.getMyLocation().getBearing();
      CameraPosition newCamPos = new CameraPosition(currentPostion,8,1,brearing);
      mMap.animateCamera(CameraUpdateFactory.newCameraPosition(newCamPos));
      */

    }
    /* 지도 끝 */


    /* 파일 저장 시작 */
    private void takePhoto() {
        final String filename = generateFilename();
        ArSceneView view = arFragment.getArSceneView();

        // Create a bitmap the size of the scene view.
        final Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
                Bitmap.Config.ARGB_8888);

        // Create a handler thread to offload the processing of the image.
        final HandlerThread handlerThread = new HandlerThread("PixelCopier");
        handlerThread.start();
        // Make the request to copy.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            PixelCopy.request(view, bitmap, (copyResult) -> {
                if (copyResult == PixelCopy.SUCCESS) {
                    try {
                        /*
                        LinearLayout fContainer1 = (LinearLayout)findViewById(R.id.menuLayout);
                        fContainer1.buildDrawingCache();
                        Bitmap fContainerLayoutView01 = fContainer1.getDrawingCache();

                        LinearLayout fContainer2 = (LinearLayout)findViewById(R.id.mapLayout);
                        fContainer2.buildDrawingCache();
                        Bitmap fContainerLayoutView02 = fContainer2.getDrawingCache();

                        Bitmap layout = mergeToPin(fContainerLayoutView01, fContainerLayoutView02);
                        Bitmap result = mergeToPin(bitmap, layout);
                        */


                        ConstraintLayout fContainer = (ConstraintLayout) findViewById(R.id.masterLayout);
                        fContainer.buildDrawingCache();
                        Bitmap fContainerLayoutView = fContainer.getDrawingCache();

                        Bitmap result = mergeToPin(bitmap, fContainerLayoutView);


                        saveBitmapToDisk(result, filename);
                        //saveBitmapToDisk(bitmap, filename);
                    } catch (IOException e) {
                        Toast toast = Toast.makeText(this, e.toString(),
                                Toast.LENGTH_LONG);
                        toast.show();
                        return;
                    }
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                            "저장 완료", Snackbar.LENGTH_LONG);
                    snackbar.setAction("사진 보기", v -> {
                        File photoFile = new File(filename);

                        Uri photoURI = FileProvider.getUriForFile(this,
                                this.getPackageName() + ".save.provider",
                                photoFile);
                        Intent intent = new Intent(Intent.ACTION_VIEW, photoURI);
                        intent.setDataAndType(photoURI, "image/*");
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(intent);

                    });
                    snackbar.show();
                } else {
                    Toast toast = Toast.makeText(this,
                            "Failed to copyPixels: " + copyResult, Toast.LENGTH_LONG);
                    toast.show();
                }
                handlerThread.quitSafely();
            }, new Handler(handlerThread.getLooper()));
        }
    }

    private void saveBitmapToDisk(Bitmap bitmap, String filename) throws IOException {

        File out = new File(filename);
        if (!out.getParentFile().exists()) {
            out.getParentFile().mkdirs();
        }
        try (FileOutputStream outputStream = new FileOutputStream(filename);
             ByteArrayOutputStream outputData = new ByteArrayOutputStream()) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputData);
            outputData.writeTo(outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IOException ex) {
            throw new IOException("Failed to save bitmap to disk", ex);
        }
    }

    private String generateFilename() {
        String date =
                null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            date = new SimpleDateFormat("yyyyMMddHHmmss", java.util.Locale.getDefault()).format(new Date());
        }
        return Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM) + File.separator + "Screenshots/" + date + "_screenshot.jpg";
    }

    public static Bitmap mergeToPin(Bitmap back, Bitmap front) {
        Bitmap result = Bitmap.createBitmap(back.getWidth(), back.getHeight(), back.getConfig());
        Canvas canvas = new Canvas(result);
        int widthBack = 300; //back.getWidth();
        int widthFront = 100; //front.getWidth();
        //float move = (widthBack - widthFront) / 2;
        canvas.drawBitmap(back, 0f, 0f, null);
        //canvas.drawBitmap(front, move, move, null);
        canvas.drawBitmap(front, 0, 0, null);
        return result;
    }
    /* 파일 저장 End */

    /* 외부 어플리케이션 실행 시작 */
    public boolean getPackageList() {
        boolean isExist = false;

        PackageManager pkgMgr = getPackageManager();
        List<ResolveInfo> mApps;
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mApps = pkgMgr.queryIntentActivities(mainIntent, 0);

        try {
            for (int i = 0; i < mApps.size(); i++) {
                if (mApps.get(i).activityInfo.packageName.startsWith("kr.or.kcsc.android.application")) {
                    isExist = true;
                    break;
                }
            }
        } catch (Exception e) {
            isExist = false;
        }
        return isExist;
    }

    public void callLx() {
        boolean isExist = false;
        isExist = getPackageList();

        if (isExist) {
            Intent intent = getPackageManager().getLaunchIntentForPackage("kr.or.kcsc.android.application");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if (!isExist) {
            String url = "market://details?id=" + "kr.or.kcsc.android.application";
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(i);
        }
    }
    /* 외부 어플리케이션 실행 End */

    /**
     * Example node of a layout
     *
     * @return
     */
    private Node getExampleView() {
        Node base = new Node();
        base.setRenderable(popupLayoutRenderable);
        Context c = this;
        // Add  listeners etc here
        View eView = popupLayoutRenderable.getView();
        eView.setOnTouchListener((v, event) -> {
            Toast.makeText(
                    c, "NC 소프트\n(37.399464, 127.108851)", Toast.LENGTH_LONG)
                    .show();
            return false;
        });

        return base;
    }


    /**
     * Make sure we call locationScene.resume();
     */
    @Override
    protected void onResume() {
        super.onResume();

        if (locationScene != null) {
            locationScene.resume();
        }

        if (arSceneView.getSession() == null) {
            // If the session wasn't created yet, don't resume rendering.
            // This can happen if ARCore needs to be updated or permissions are not granted yet.
            try {
                Session session = DemoUtils.createArSession(this, installRequested);
                if (session == null) {
                    installRequested = ARLocationPermissionHelper.hasPermission(this);
                    return;
                } else {
                    arSceneView.setupSession(session);
                }
            } catch (UnavailableException e) {
                DemoUtils.handleSessionException(this, e);
            }
        }

        try {
            arSceneView.resume();
        } catch (CameraNotAvailableException ex) {
            DemoUtils.displayError(this, "Unable to get camera", ex);
            finish();
            return;
        }

        if (arSceneView.getSession() != null) {
            showLoadingMessage();
        }
    }

    /**
     * Make sure we call locationScene.pause();
     */
    @Override
    public void onPause() {
        super.onPause();

        if (locationScene != null) {
            locationScene.pause();
        }

        arSceneView.pause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        arSceneView.destroy();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] results) {
        if (!ARLocationPermissionHelper.hasPermission(this)) {
            if (!ARLocationPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                ARLocationPermissionHelper.launchPermissionSettings(this);
            } else {
                Toast.makeText(
                        this, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                        .show();
            }
            finish();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // Standard Android full-screen functionality.
            getWindow()
                    .getDecorView()
                    .setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private void showLoadingMessage() {
        if (loadingMessageSnackbar != null && loadingMessageSnackbar.isShownOrQueued()) {
            return;
        }

        loadingMessageSnackbar =
                Snackbar.make(
                        MainActivity.this.findViewById(android.R.id.content),
                        "서페이스 탐색중입니다",
                        Snackbar.LENGTH_INDEFINITE);
        loadingMessageSnackbar.getView().setBackgroundColor(0xbf323232);
        loadingMessageSnackbar.show();
    }

    private void hideLoadingMessage() {
        if (loadingMessageSnackbar == null) {
            return;
        }

        loadingMessageSnackbar.dismiss();
        loadingMessageSnackbar = null;
    }

    private void onClear() {
        List<Node> children = new ArrayList<>(arFragment.getArSceneView().getScene().getChildren());
        for (Node node : children) {
            if (node instanceof AnchorNode) {
                if (((AnchorNode) node).getAnchor() != null) {
                    ((AnchorNode) node).getAnchor().detach();
                }
            }
            if (!(node instanceof Camera) && !(node instanceof Sun)) {
                node.setParent(null);
            }
        }
    }


    private void addLineBetweenPoints(Scene scene, Pose fromPose, Pose toPose) {

        // If you call without fucking anchor you never do anything. asshole.
        if (fromPose == null || toPose == null) {
            return;
        }

        //
        Vector3 from, to;                               // vector variable from the Anchor.
        float anchorX = 0.0f;
        float anchorY = 0.0f;
        float anchorZ = 0.0f;

        anchorY = fromPose.ty();
        anchorZ = fromPose.tz();
        anchorX = fromPose.tx();
        from = new Vector3(anchorX, anchorY, anchorZ);

        anchorY = toPose.ty();
        anchorZ = toPose.tz();
        anchorX = toPose.tx();
        to = new Vector3(anchorX, anchorY, anchorZ);

        // prepare an anchor position
        Quaternion camQ = scene.getCamera().getWorldRotation();

        float[] f1 = new float[]{to.x, to.y, to.z};
        // float[] f2 = new float[]{camQ.x, camQ.y, camQ.z, camQ.w};
        float[] f2 = new float[]{from.x, from.y, from.z, camQ.w};
        Pose anchorPose = new Pose(f1, f2);

        // make an ARCore Anchor
        // But you mother fucker don't any explain to mCallback.
        // Than I make an anchor freely.
        // Original source in this below.
        // Anchor anchor = mCallback.getSession().createAnchor(anchorPose);
        Anchor anchor = arFragment.getArSceneView().getSession().createAnchor(anchorPose);

        // Node that is automatically positioned in world space based on the ARCore Anchor.
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(scene);

        // Compute a line's length
        float lineLength = Vector3.subtract(from, to).length();

        // Prepare a color
        Color colorOrange = new Color(android.graphics.Color.parseColor("#ffa71c"));

        // 1. make a material by the color
        MaterialFactory.makeOpaqueWithColor(arFragment.getContext(), colorOrange)
                .thenAccept(material -> {
                    // 2. make a model by the material
                    ModelRenderable model = ShapeFactory.makeCylinder(0.0025f, lineLength,
                            new Vector3(0f, lineLength / 2, 0f), material);
                    model.setShadowReceiver(false);
                    model.setShadowCaster(false);

                    // 3. make node
                    Node node = new Node();
                    node.setRenderable(model);
                    node.setParent(anchorNode);

                    // 4. set rotation
                    final Vector3 difference = Vector3.subtract(to, from);
                    final Vector3 directionFromTopToBottom = difference.normalized();
                    final Quaternion rotationFromAToB =
                            Quaternion.lookRotation(directionFromTopToBottom, Vector3.up());
                    node.setWorldRotation(Quaternion.multiply(rotationFromAToB,
                            Quaternion.axisAngle(new Vector3(1.0f, 0.0f, 0.0f), 90)));
                });
    }


    private void placeObject(ArFragment fragment, Anchor anchor, Uri model) {
        ModelRenderable.builder()
                .setSource(fragment.getContext(), model)
                .build()
                .thenAccept(renderable -> addNodeToScene(fragment, anchor, renderable))
                .exceptionally((throwable -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(throwable.getMessage())
                            .setTitle("Error!");
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    return null;
                }));
    }

    private void addNodeToScene(ArFragment fragment, Anchor anchor, Renderable renderable) {
        AnchorNode anchorNode = new AnchorNode(anchor);
        TransformableNode node = new TransformableNode(fragment.getTransformationSystem());
        node.setRenderable(renderable);
        node.setParent(anchorNode);
        fragment.getArSceneView().getScene().addChild(anchorNode);
        node.select();
    }
}
