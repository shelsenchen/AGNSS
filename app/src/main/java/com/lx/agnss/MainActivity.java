package com.lx.agnss;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.PixelCopy;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
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

import static com.lx.agnss.R.id.nav_view;

/**
 * This class for the main activity job.
 */
public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

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

    private TextView locationView; // This view shown a coordinate
    private TextView distanceView; // This view shown a distance

    // 거리측정용 변수 선언
    private boolean boolMeasureDistOnOff = false;
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

    //Left slide menu
    ListView listView = null;

    /**
     * Navigation drawer
     */
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle drawerToggle;
    Toolbar toolbar;

    /**
     * on create
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /** Initialization Layout */
        initLayout();

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

        // Render assets
        renderModel();

        // Make an instance for AR and listner
        initAR();

        /*
         ***   메뉴버튼 이벤트 처리 끝   ***
         */
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);


        // initMap();
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


        displayBuildingPannel();

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

    }
    /* OnCreate End */

    /**
     * Get start map(start?)
     * @param outState
     */
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

    /**
     * Map ready(End?)
     * @param googleMap
     */
    @SuppressLint("MissingPermission")
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


    /**
     * Take a screen capture
     */
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
                    Toast.makeText(this,"Failed to copyPixels: " + copyResult, Toast.LENGTH_LONG).show();
                }
                handlerThread.quitSafely();
            }, new Handler(handlerThread.getLooper()));
        }
    }

    /**
     * Save bitmap to disk
     * @param bitmap
     * @param filename
     * @throws IOException
     */
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

    /**
     * Make a file name
     * @return
     */
    private String generateFilename() {
        String date =
                null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            date = new SimpleDateFormat("yyyyMMddHHmmss", java.util.Locale.getDefault()).format(new Date());
        }
        return Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM) + File.separator + "Screenshots/" + date + "_screenshot.jpg";
    }

    /**
     * Merge bitmap between
     * @param back
     * @param front
     * @return
     */
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

    /**
     *  Find a LX app from the package manager
     */
    public boolean getPackageList() {

        PackageManager pkgMgr;
        boolean isExist = false;

        pkgMgr = getPackageManager();

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> mApps;
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

    /**
     * Call the LX App
     */
    public void callLx() {

        if ( getPackageList() ) {
            Intent intent = getPackageManager().getLaunchIntentForPackage("kr.or.kcsc.android.application");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            String url = "market://details?id=" + "kr.or.kcsc.android.application";
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(i);
        }
    }

    /**
     * Example node of a layout
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

    /**
     *
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        arSceneView.destroy();
    }

    /**
     *
     * @param requestCode
     * @param permissions
     * @param results
     */
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

    /**
     *
     * @param hasFocus
     */
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

    /**
     *  Represent the message while it searching a sufface.
     */
    private void showLoadingMessage() {
        if (loadingMessageSnackbar != null && loadingMessageSnackbar.isShownOrQueued()) {
            return;
        }

//        loadingMessageSnackbar =
//                Snackbar.make(
//                        MainActivity.this.findViewById(android.R.id.content),
//                        "서페이스 탐색중입니다",
//                        Snackbar.LENGTH_INDEFINITE);
//        loadingMessageSnackbar.getView().setBackgroundColor(0xbf323232);
//        loadingMessageSnackbar.show();
    }

    /**
     * Hide the message when it figured out a surffce.
     */
    private void hideLoadingMessage() {
        if (loadingMessageSnackbar == null) {
            return;
        }

        loadingMessageSnackbar.dismiss();
        loadingMessageSnackbar = null;
    }

    /**
     * Clear all anchor & marker
     */
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


    /**
     * Draw a line in AR view.
     * @param scene
     * @param fromPose
     * @param toPose
     */
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


    /**
     *
     * @param fragment
     * @param anchor
     * @param model
     */
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

    /**
     *
     * @param fragment
     * @param anchor
     * @param renderable
     */
    private void addNodeToScene(ArFragment fragment, Anchor anchor, Renderable renderable) {
        AnchorNode anchorNode = new AnchorNode(anchor);
        TransformableNode node = new TransformableNode(fragment.getTransformationSystem());
        node.setRenderable(renderable);
        node.setParent(anchorNode);
        fragment.getArSceneView().getScene().addChild(anchorNode);
        node.select();
    }

    /**
     * Navigation menu item click
     * @param item
     * @return
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_jeonju:
                Toast.makeText(this, "Jeon-ju clicked..", Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_yongsan:
                Toast.makeText(this, "Yong-san clicked..", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_itm_point_out_yongsan:
                Toast.makeText(this, "Young-san point out clicked..", Toast.LENGTH_SHORT).show();
                displayYongSanPointOut();
                break;
            case R.id.nav_itm_building_yongsan:
                Toast.makeText(this, "Young-san building clicked..", Toast.LENGTH_SHORT).show();
                displayYongSanBuilding();
                break;
            case R.id.nav_itm_axis_yongsan:
                Toast.makeText(this, "Young-san Axis out clicked..", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_itm_point_out_jeonju:
                Toast.makeText(this, "Jeon-ju point out clicked..", Toast.LENGTH_SHORT).show();
                displayJeonJuPointOut();
                break;
            case R.id.nav_itm_building_jeonju:
                Toast.makeText(this, "Jeon-ju building clicked..", Toast.LENGTH_SHORT).show();
                displayJeonjuBuilding();
                break;
            case R.id.nav_itm_axis_jeonju:
                Toast.makeText(this, "Jeon-ju Axis clicked..", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_itm_point_out:
                Toast.makeText(this, "Common Geo menu point out clicked..", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_itm_building:
                Toast.makeText(this, "Common Geo menu Building clicked..", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_itm_axis:
                Toast.makeText(this, "Common Geo menu Axis clicked..", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_itm_marker_pointing:
                Toast.makeText(this, "Common menu marker clicked..", Toast.LENGTH_SHORT).show();
                onClear();
                boolMeasureDistOnOff = false;
                break;
            case R.id.nav_itm_current_coordinates:
                Toast.makeText(this, "Common menu current coordinate clicked..", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_itm_measure_distance:
                Toast.makeText(this, "Common menu mesure distance clicked..", Toast.LENGTH_SHORT).show();
                measureDistance();
                break;
            case R.id.nav_itm_screen_capture:
                // Toast.makeText(this, "Common menu capture screen clicked..", Toast.LENGTH_SHORT).show();
                takePhoto();
                Toast.makeText(getApplicationContext(), "현재 화면을 겔러리에 저장하였습니다.",Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_itm_lx_app:
                // Toast.makeText(this, "Common menu LX App clicked..", Toast.LENGTH_SHORT).show();
                callLx();
                Toast.makeText(getApplicationContext(), "토지알리e 앱을 엽니다.",Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_itm_gps_frequency:
                Toast.makeText(this, "Common menu Show GPS frequency clicked..", Toast.LENGTH_SHORT).show();
                break;

        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return false;
    }

    /**
     * Initialization ground layout
     */
    private void initLayout() {

        // Head bar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("A-GNSS");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        toolbar.setVisibility(View.GONE);

        drawerLayout = (DrawerLayout) findViewById(R.id.masterLayout);
        navigationView = (NavigationView) findViewById(nav_view);
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        );

        drawerLayout.addDrawerListener(drawerToggle);

        // left side
        navigationView.setNavigationItemSelectedListener(this);

        /** Left floating button for toggling menu view */
        FloatingActionButton flobtnLeftFloating = findViewById(R.id.flobtnLeftFloating);
        flobtnLeftFloating.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Toggle left side menu
                Toast.makeText(getApplicationContext(), "Left Slide", Toast.LENGTH_SHORT).show();
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        /** Right floating button for toggling info view*/
        FloatingActionButton flobtnRightFloating = findViewById(R.id.flobtnRightFloating);
        flobtnRightFloating.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Right floating", Toast.LENGTH_SHORT).show();

                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer);
                if (!drawer.isDrawerOpen(Gravity.RIGHT)) {
                    drawer.openDrawer(Gravity.RIGHT);
                }
            }
        });

        locationView = (TextView) findViewById(R.id.locationView);
        distanceView = (TextView) findViewById(R.id.distanceView);
    }

    /**
     *  Render assets
     */
    private void renderModel() {
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
    }

    /**
     * Initialize AR
     */
    private void initAR() {
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        arSceneView = arFragment.getArSceneView();

        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plan, MotionEvent motionEvent) -> {
                    if (iconRenderable == null || distIconRenderable == null) {
                        return;
                    }

                    if (!boolMeasureDistOnOff) {
                        Anchor anchor = hitResult.createAnchor();
                        AnchorNode anchorNode = new AnchorNode(anchor);
                        anchorNode.setParent(arFragment.getArSceneView().getScene());

                        TransformableNode icon = new TransformableNode(arFragment.getTransformationSystem());
                        icon.setLocalRotation(Quaternion.axisAngle(new Vector3(0, 1f, 0), 90f));
                        icon.setParent(anchorNode);
                        icon.setRenderable(iconRenderable);
                        icon.select();
                    } else if (boolMeasureDistOnOff) {
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
    }

    private void measureDistance() {
        // 거리측정
        if (boolMeasureDistOnOff) {
            boolMeasureDistOnOff = false;
            //btnMenu03.setBackgroundColor(getResources().getColor(R.color.btnBackground_off));
            distanceView.setVisibility(View.INVISIBLE);
            distanceView.setText("");
            onClear();
        } else if (!boolMeasureDistOnOff) {
            boolMeasureDistOnOff = true;
            //btnMenu03.setBackgroundColor(getResources().getColor(R.color.btnBackground_on));
            distanceView.setVisibility(View.VISIBLE);
            distanceView.setText("첫번째 지점을 선택해 주세요");
        }
        //Toast.makeText(getApplicationContext(), "버튼3(거리측정) 클릭",Toast.LENGTH_SHORT).show();
    }

    /**
     * display JeonjuBuilding
     */
    private void displayJeonjuBuilding() {

        onClear();

        Scene scene = arFragment.getArSceneView().getScene();
        Quaternion camQ = scene.getCamera().getWorldRotation();

        float[] f1 = new float[]{camQ.x, camQ.y, camQ.z};
        float[] f2 = new float[]{camQ.x, camQ.y, camQ.z, 90f};
        Pose anchorPose = new Pose(f1, f2);

        Anchor anchor = arFragment.getArSceneView().getSession().createAnchor(anchorPose);

        placeObject(arFragment, anchor, Uri.parse("JeonJuBuildingInterpolate_0.sfb"));
    }

    /**
     * Display Jeon-ju Point Out
     */
    private void displayJeonJuPointOut() {
        onClear();

        Scene scene = arFragment.getArSceneView().getScene();
        Quaternion camQ = scene.getCamera().getWorldRotation();

        float[] f1 = new float[]{camQ.x, camQ.y, camQ.z};
        float[] f2 = new float[]{camQ.x, camQ.y, camQ.z, 90f};
        Pose anchorPose = new Pose(f1, f2);

        Anchor anchor = arFragment.getArSceneView().getSession().createAnchor(anchorPose);

        placeObject(arFragment, anchor, Uri.parse("JeonJuBuildPointOutInterpolate_0.sfb"));
    }

    /**
     * Display Young-san Building
     */
    private void displayYongSanBuilding() {
        onClear();

        Scene scene = arFragment.getArSceneView().getScene();
        Quaternion camQ = scene.getCamera().getWorldRotation();

        float[] f1 = new float[]{camQ.x, camQ.y, camQ.z};
        float[] f2 = new float[]{camQ.x, camQ.y, camQ.z, 90f};
        Pose anchorPose = new Pose(f1, f2);

        Anchor anchor = arFragment.getArSceneView().getSession().createAnchor(anchorPose);

        placeObject(arFragment, anchor, Uri.parse("YoungSanBuildingDEM_0.sfb"));
    }

    /**
     * Display Yong-san Point out
     */
    private void displayYongSanPointOut() {
        onClear();

        Scene scene = arFragment.getArSceneView().getScene();
        Quaternion camQ = scene.getCamera().getWorldRotation();

        float[] f1 = new float[]{camQ.x, camQ.y, camQ.z};
        float[] f2 = new float[]{camQ.x, camQ.y, camQ.z, 90f};
        Pose anchorPose = new Pose(f1, f2);

        Anchor anchor = arFragment.getArSceneView().getSession().createAnchor(anchorPose);

        placeObject(arFragment, anchor, Uri.parse("YoungSanPointOut_0.sfb"));
    }

    /**
     * Building information pannel
     **/
    private void displayBuildingPannel() {
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
    }

}
