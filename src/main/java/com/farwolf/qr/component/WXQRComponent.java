package com.farwolf.qr.component;

import android.content.Context;
import android.support.annotation.NonNull;

import com.farwolf.qr.view.QRView;
import com.farwolf.weex.annotation.WeexComponent;
import com.taobao.weex.WXSDKInstance;
import com.taobao.weex.annotation.JSMethod;
import com.taobao.weex.bridge.JSCallback;
import com.taobao.weex.ui.action.BasicComponentData;
import com.taobao.weex.ui.component.WXComponent;
import com.taobao.weex.ui.component.WXVContainer;


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
        QRView qrView=getHostView();
        qrView.callback=callback;
        this.callback=callback;
        qrView.start();
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
        getHostView().onDestroy();
    }
}
