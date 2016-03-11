package com.example.jakkot93.cameraface;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.io.IOException;

public class MainActivity extends Activity implements SurfaceHolder.Callback{

    Camera camera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    boolean previewing = false;

    Bitmap BitmapShrek, resizedBitmap;

    DrawingView drawingView;
    Camera.Face[] detectedFaces;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        getWindow().setFormat(PixelFormat.UNKNOWN);
        surfaceView = (SurfaceView)findViewById(R.id.camerapreview);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        drawingView = new DrawingView(this);
        ViewGroup.LayoutParams layoutParamsDrawing
                = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.FILL_PARENT);
        this.addContentView(drawingView, layoutParamsDrawing);

        //przypisanie layoutu aby wykrywać dotknięcie ekranu i wywołać funkcje autofocus
        RelativeLayout layoutBackground = (RelativeLayout)findViewById(R.id.background);
        layoutBackground.setOnClickListener(new RelativeLayout.OnClickListener(){

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                camera.autoFocus(myAutoFocusCallback);
            }});

        // wczytanie obrazka
        BitmapShrek = BitmapFactory.decodeResource(getResources(), R.drawable.shrek);
    }

    Camera.FaceDetectionListener faceDetectionListener = new Camera.FaceDetectionListener(){

        @Override
        public void onFaceDetection(Camera.Face[] faces, Camera camera) {

            if (faces.length == 0){
                drawingView.setHaveFace(false);
            }else{
                drawingView.setHaveFace(true);
                detectedFaces = faces;
            }
            drawingView.invalidate();
        }};

    Camera.AutoFocusCallback myAutoFocusCallback = new Camera.AutoFocusCallback(){

        @Override
        public void onAutoFocus(boolean arg0, Camera arg1) {
            // TODO Auto-generated method stub
        }};

    Camera.ShutterCallback myShutterCallback = new Camera.ShutterCallback(){

        @Override
        public void onShutter() {
            // TODO Auto-generated method stub
        }};

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // TODO Auto-generated method stub
        if(previewing){
            camera.stopFaceDetection();
            camera.stopPreview();
            previewing = false;
        }

        if (camera != null){
            try {
                camera.setPreviewDisplay(surfaceHolder);
                camera.startPreview();

                camera.startFaceDetection();
                previewing = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        camera = Camera.open();
        camera.setFaceDetectionListener(faceDetectionListener);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        camera.stopFaceDetection();
        camera.stopPreview();
        camera.release();
        camera = null;
        previewing = false;
    }

    private class DrawingView extends View {

        boolean haveFace;
        Paint drawingPaint;

        public DrawingView(Context context) {
            super(context);
            haveFace = false;
            drawingPaint = new Paint();
            drawingPaint.setColor(Color.GREEN);
            drawingPaint.setStyle(Paint.Style.STROKE);
            drawingPaint.setStrokeWidth(2);
        }

        public void setHaveFace(boolean h) {
            haveFace = h;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            // TODO Auto-generated method stub
            try {

                if (haveFace) {
                    // Camera driver coordinates range from (-1000, -1000) to (1000, 1000).
                    // UI coordinates range from (0, 0) to (width, height).

                    int vWidth = getWidth();
                    int vHeight = getHeight();

                    for (int i = 0; i < detectedFaces.length; i++) {

                        int l = detectedFaces[i].rect.left;
                        int t = detectedFaces[i].rect.top;
                        int r = detectedFaces[i].rect.right;
                        int b = detectedFaces[i].rect.bottom;
                        int left = (l + 1000) * vWidth / 2000;
                        int top = (t + 1000) * vHeight / 2000;
                        int right = (r + 1000) * vWidth / 2000;
                        int bottom = (b + 1000) * vHeight / 2000;

                        //rysowanie prostąkotu wokół twarzy
                        //canvas.drawRect(left, top, right, bottom, drawingPaint);

                        //rysowanie kółka w lewym górnym rogu twarzy
                        //canvas.drawCircle(left, top, 20, drawingPaint);

                        //oblizenie rożnicy miedzy górą a dółem do prawidłowego skalowania
                        double roznica = (bottom - top)*1.5;

                        //skalowanie
                        resizedBitmap = Bitmap.createScaledBitmap(BitmapShrek, (int)roznica, (int)roznica, false);
                        canvas.drawBitmap(resizedBitmap, left, top, drawingPaint);
                    }
                } else {
                    canvas.drawColor(Color.TRANSPARENT);
                }
            } catch (Exception e) {
            }
        }
    }
}