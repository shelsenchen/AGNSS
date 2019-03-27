package com.lx.agnss;

import android.content.Intent;
import android.graphics.Bitmap;
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
import android.view.PixelCopy;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.ux.ArFragment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    // Sceenform fragment
    private ArFragment arFragment;
    //메뉴버튼
    private Button btnMenu01;
    private Button btnMenu02;
    private Button btnMenu03;
    private Button btnMenu04;
    private Button btnMenu05;


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
         ***   메뉴버튼 이벤트 처리  ***
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
                Toast.makeText(getApplicationContext(), "버튼5(LX토지알림e 연동) 클릭",Toast.LENGTH_SHORT).show();
            }
        });
    }

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
}
