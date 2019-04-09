package com.farwolf.qr.component;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;

import com.farwolf.perssion.Perssion;
import com.farwolf.perssion.PerssionCallback;
import com.farwolf.qr.view.QRView;
import com.farwolf.weex.annotation.WeexComponent;
import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.annotation.JSMethod;
import com.taobao.weex.bridge.JSCallback;
import com.taobao.weex.ui.action.BasicComponentData;
import com.taobao.weex.ui.component.WXComponent;
import com.taobao.weex.ui.component.WXVContainer;

import java.util.HashMap;


@WeexComponent(name="qr")
public class WXQRComponent extends WXComponent<QRView> {


    JSCallback callback;

    public WXQRComponent(WXSDKInstance instance, WXVContainer parent, BasicComponentData basicComponentData) {
        super(instance, parent, basicComponentData);
    }


    @Override
    protected QRView initComponentHostView(@NonNull Context context) {
        QRView qrView=new QRView(context);
        return qrView;
    }


    @JSMethod
    public void scan(JSCallback callback){
        final QRView qrView=getHostView();
        qrView.callback=callback;
        this.callback=callback;


        Perssion.check((Activity) mInstance.getContext(), Manifest.permission.CAMERA,new PerssionCallback(){


            @Override
            public void onGranted() {


                Perssion.check((Activity)mInstance.getContext(),Manifest.permission.WRITE_EXTERNAL_STORAGE , new PerssionCallback() {
                    @Override
                    public void onGranted() {
                        qrView.start();

                    }
                });



            }
        });


//        qrView.setVisibility(View.VISIBLE);

    }

    @JSMethod
    public void stop(){
        QRView qrView=getHostView();

        qrView.stop();
//        qrView.setVisibility(View.GONE);
    }

    @Override
    public void onActivityResume() {
        super.onActivityResume();
//        getHostView().onResume();
    }

    @Override
    public void onActivityPause() {
        super.onActivityPause();
        getHostView().onPause();
    }


    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
//        getHostView().onDestroy();
    }

    @JSMethod
    public void setScanArea(HashMap map){
      int width=map.containsKey("width")?Integer.parseInt(map.get("width")+""):0;
      int height=map.containsKey("height")?Integer.parseInt(map.get("height")+""):0;
      int alph=map.containsKey("alph")?Integer.parseInt(map.get("alph")+""):0;
      String color =map.containsKey("color")? map.get("color")+"":"";
      getHostView().setScanArea(width,height,alph,color);
    }


//    setScanArea
}
