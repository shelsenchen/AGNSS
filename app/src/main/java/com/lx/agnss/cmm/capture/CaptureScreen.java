package com.lx.agnss.cmm.capture;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.DrawerLayout;
import android.view.PixelCopy;
import android.widget.Toast;

import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.ux.ArFragment;
import com.lx.agnss.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class for capture screen into storage in mobile device as an image file.
 */
public class CaptureScreen {

    public void saveCapture(ArFragment arFragment, DrawerLayout masterLayout) {
        final String filename = generateFilename();
        ArSceneView view = arFragment.getArSceneView();

        // Create a bitmap the size of the scene view.
        final Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);

        // Create a handler thread to offload the processing of the image.
        final HandlerThread handlerThread = new HandlerThread("PixelCopier");
        handlerThread.start();
        // Make the request to copy.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            PixelCopy.request(view, bitmap, (copyResult) -> {
                if (copyResult == PixelCopy.SUCCESS) {
                    try {
                        DrawerLayout fContainer = (DrawerLayout)masterLayout; //findViewById(R.id.masterLayout);

                        fContainer.buildDrawingCache();

                        Bitmap fContainerLayoutView = fContainer.getDrawingCache();

                        Bitmap result = mergeToPin(bitmap, fContainerLayoutView);

                        saveBitmapToDisk(result, filename);

                    } catch (IOException e) {
//                        Toast toast = Toast.makeText(this, e.toString(),Toast.LENGTH_LONG);
//                        toast.show();
                        return;
                    }
//                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),"저장 완료", Snackbar.LENGTH_LONG);
//                    snackbar.setAction("사진 보기", v -> {
//                        File photoFile = new File(filename);
//
//                        Uri photoURI = FileProvider.getUriForFile(this,
//                                this.getPackageName() + ".save.provider",
//                                photoFile);
//                        Intent intent = new Intent(Intent.ACTION_VIEW, photoURI);
//                        intent.setDataAndType(photoURI, "image/*");
//                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                        startActivity(intent);
//
//                    });
//                    snackbar.show();
                } else {
//                    Toast.makeText(this, "Failed to copyPixels: " + copyResult, Toast.LENGTH_LONG).show();
                }
                handlerThread.quitSafely();
            }, new Handler(handlerThread.getLooper()));
        }
    }

    /**
     * Make a file name
     *
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
     *
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
}
