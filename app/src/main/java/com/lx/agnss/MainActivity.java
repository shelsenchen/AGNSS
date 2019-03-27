package com.lx.agnss;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.PixelCopy;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.ux.ArFragment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    // Sceenform fragment
    private ArFragment arFragment;
    //메뉴버튼
    private Button btnMenu01;
    private Button btnMenu02;
    private Button btnMenu03;
    private Button btnMenu04;
    private Button btnMenu05;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);

        btnMenu01 = (Button)findViewById(R.id.btnMenu01);
        btnMenu02 = (Button)findViewById(R.id.btnMenu02);
        btnMenu03 = (Button)findViewById(R.id.btnMenu03);
        btnMenu04 = (Button)findViewById(R.id.btnMenu04);
        btnMenu05 = (Button)findViewById(R.id.btnMenu05);

        /*
         ***   메뉴버튼 이벤트 처리 시작  ***
         */
        btnMenu01.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // 마커측점
                Toast.makeText(getApplicationContext(), "버튼1(마커측점) 클릭",Toast.LENGTH_SHORT).show();
            }
        });

        btnMenu02.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 좌표측정
                Toast.makeText(getApplicationContext(), "버튼2(좌표측정) 클릭",Toast.LENGTH_SHORT).show();
            }
        });

        btnMenu03.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // 거리측정
                Toast.makeText(getApplicationContext(), "버튼3(거리측정) 클릭",Toast.LENGTH_SHORT).show();
            }
        });

        btnMenu04.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 화면캡쳐
                Toast.makeText(getApplicationContext(), "버튼4(화면캡쳐) 클릭",Toast.LENGTH_SHORT).show();
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
        /*
         ***   메뉴버튼 이벤트 처리 끝   ***
        */

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        //mapFragment.getMapAsync(this);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentPostion = new LatLng(location.getLatitude(), location.getLongitude());
                //mMap.moveCamera(CameraUpdateFactory.newLatLng(currentPostion));
                mMap.animateCamera(CameraUpdateFactory.newLatLng(currentPostion));
                //info03.setText("Latitude:" + location.getLatitude() + ", Longitude:" + location.getLongitude());
                Log.d("onLocationChanged","Latitude:" + location.getLatitude() + ", Longitude:" + location.getLongitude());

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

    }

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
                        saveBitmapToDisk(bitmap, filename);
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
                if(mApps.get(i).activityInfo.packageName.startsWith("kr.or.kcsc.android.application")){
                    isExist = true;
                    break;
                }
            }
        }
        catch (Exception e) {
            isExist = false;
        }
        return isExist;
    }
    /* 외부 어플리케이션 실행 End */
    public void callLx(){
        boolean isExist = false;
        isExist = getPackageList();

        if(isExist){
            Intent intent = getPackageManager().getLaunchIntentForPackage("kr.or.kcsc.android.application");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }else if(!isExist){
            String url = "market://details?id=" + "kr.or.kcsc.android.application";
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(i);
        }
    }
}
