package com.farwolf.qr.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.farwolf.qr.R;
import com.farwolf.qr.zxing.android.BeepManager;
import com.farwolf.qr.zxing.android.CaptureActivityHandler;
import com.farwolf.qr.zxing.android.InactivityTimer;
import com.farwolf.qr.zxing.android.IntentSource;
import com.farwolf.qr.zxing.camera.CameraManager;
import com.farwolf.qr.zxing.view.ViewfinderView;
import com.farwolf.qrcode.zxing.android.CaptureActivity;
import com.farwolf.weex.app.WeexApplication;
import com.farwolf.weex.util.Weex;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.taobao.weex.bridge.JSCallback;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class QRView  extends LinearLayout implements
        SurfaceHolder.Callback{


    public JSCallback callback;


    public QRView(Context context) {
        super(context);
        init();
    }

    public QRView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }



    private static final String TAG = CaptureActivity.class.getSimpleName();


    // 相机控制
    private CameraManager cameraManager;
    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private boolean hasSurface;
    private IntentSource source;
    private Collection<BarcodeFormat> decodeFormats;
    private Map<DecodeHintType, ?> decodeHints;
    private String characterSet;
    // 电量控制
    private InactivityTimer inactivityTimer;
    // 声音、震动控制
    private BeepManager beepManager;

    private ImageView imageButton_back;

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }



    void init(){
        Window window = ((Activity)getContext()).getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        LayoutInflater.from(getContext()).inflate(R.layout.capture_view,this);

        hasSurface = false;
//        onResume();



    }


    public void onResume(){
        inactivityTimer = new InactivityTimer((Activity) getContext());
        beepManager = new BeepManager((Activity) getContext());

        cameraManager = new CameraManager(WeexApplication.getInstance());

        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        viewfinderView.setCameraManager(cameraManager);
//        String bgColor= getIntent().getStringExtra("bgColor");
//        if(bgColor!=null)
//            viewfinderView.themeColor=bgColor;
        beepManager.updatePrefs();
        inactivityTimer.onResume();
        source = IntentSource.NONE;
        decodeFormats = null;
        characterSet = null;
        handler = null;

        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            // activity在paused时但不会stopped,因此surface仍旧存在；
            // surfaceCreated()不会调用，因此在这里初始化camera
            initCamera(surfaceHolder);
        } else {
            // 重置callback，等待surfaceCreated()来初始化camera
            surfaceHolder.addCallback(this);
        }
    }

    public void setScanArea(int width,int height,int alph,String color)
    {
        width= (int)Weex.length(width);
        height= (int)Weex.length(height);
        RelativeLayout.LayoutParams l=new RelativeLayout.LayoutParams(width,height);
        l.addRule(RelativeLayout.CENTER_IN_PARENT);

        int c=Color.parseColor(color);
        viewfinderView.maskColor=Color.argb( (int)(((float)alph)/100*255),Color.red(c),Color.green(c),Color.blue(c));
        viewfinderView.setLayoutParams(l);
        viewfinderView.setBackgroundColor(viewfinderView.maskColor);
        viewfinderView.invalidate();
    }






   public void openCamera()
    {

        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (cameraManager.isOpen()) {
            return;
        }
        try {
            // 打开Camera硬件设备
            cameraManager.openDriver(surfaceHolder);
            // 创建一个handler来打开预览，并抛出一个运行时异常
            if (handler == null) {
                handler = new CaptureActivityHandler(this, decodeFormats,
                        decodeHints, characterSet, cameraManager);
            }
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
            displayFrameworkBugMessageAndExit();
        } catch (RuntimeException e) {
            Log.w(TAG, "Unexpected error initializing camera", e);
            displayFrameworkBugMessageAndExit();
        }


    }


    public void start(){
        this.onResume();
    }
    public void stop(){
        this.onPause();
    }
    public void onPause() {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }

        if(cameraManager!=null)
        {
            inactivityTimer.onPause();
            beepManager.close();
            cameraManager.closeDriver();
            if (!hasSurface) {
                SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
                SurfaceHolder surfaceHolder = surfaceView.getHolder();
                surfaceHolder.removeCallback(this);
            }
        }

    }



    public void onDestroy() {
        if(inactivityTimer!=null)
        inactivityTimer.shutdown();

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    /**
     * 扫描成功，处理反馈信息
     *
     * @param rawResult
     * @param barcode
     * @param scaleFactor
     */
    public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
        inactivityTimer.onActivity();

        boolean fromLiveScan = barcode != null;
        //这里处理解码完成后的结果，此处将参数回传到Activity处理
        if (fromLiveScan) {
            beepManager.playBeepSoundAndVibrate();
            HashMap m=new HashMap();
            m.put("res", rawResult.getText());
            if(callback!=null)
            callback.invokeAndKeepAlive(m);
//            Toast.makeText(this, "扫描成功", Toast.LENGTH_SHORT).show();

//            Intent intent = getIntent();
//            intent.putExtra("url", rawResult.getText());
////            intent.putExtra("codedBitmap", barcode);
//            setResult(1, intent);
//            finish();
        }

    }

    /**
     * 初始化Camera
     *
     * @param surfaceHolder
     */
    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }

        openCamera();


    }

    /**
     * 显示底层错误信息并退出应用
     */
    private void displayFrameworkBugMessageAndExit() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle(getString(R.string.app_name));
//        builder.setMessage("Sorry, the Android camera encountered a problem. You may need to restart the device.");
//        builder.setPositiveButton("", new FinishListener(this));
//        builder.setOnCancelListener(new FinishListener(this));
//        builder.show();
    }



}
