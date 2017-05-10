package cn.org.xiwi.xwalk;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;

import org.xwalk.core.XWalkActivity;
import org.xwalk.core.XWalkActivityDelegate;
import org.xwalk.core.XWalkPreferences;
import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkUIClient;
import org.xwalk.core.XWalkView;
import org.xwalk.core.internal.XWalkSettingsInternal;
import org.xwalk.core.internal.XWalkViewBridge;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class XwalkActivity extends XWalkActivity {
    private XWalkView xWalkWebView;
    private boolean isReadyXwalk = false;

    @Override
    protected void onXWalkReady() {
        log("onXWalkReady");
        xWalkWebView.loadUrl("http://cn.bing.com/");//https://crosswalk-project.org
        setCacheMode(xWalkWebView);
        isReadyXwalk = true;
//
//        xWalkWebView.setUIClient(new XWalkUIClient(xWalkWebView));
//        xWalkWebView.setResourceClient(new XWalkResourceClient(xWalkWebView));

    }

    private static void log(String log) {
        Log.d("Tag", "log : " + log);
    }

    private XWalkActivityDelegate mActivityDelegate;

    /**
     * Return true if the Crosswalk runtime is ready, false otherwise.
     */
    public boolean isXWalkReady() {
        return mActivityDelegate.isXWalkReady();
    }

    /**
     * Return true if running in shared mode, false otherwise.
     */
    public boolean isSharedMode() {
        return mActivityDelegate.isSharedMode();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Runnable cancelCommand = new Runnable() {
            @Override
            public void run() {
                finish();
            }
        };
        Runnable completeCommand = new Runnable() {
            @Override
            public void run() {
                onXWalkReady();
            }
        };
        mActivityDelegate = new XWalkActivityDelegate(this, cancelCommand, completeCommand);

        setContentView(R.layout.activity_xwalk);

        xWalkWebView = (XWalkView) findViewById(R.id.xwalkview);

        //添加对javascript支持
        XWalkPreferences.setValue("enable-javascript", true);

        //开启调式,支持谷歌浏览器调式
        XWalkPreferences.setValue(XWalkPreferences.REMOTE_DEBUGGING, true);

        //置是否允许通过file url加载的Javascript可以访问其他的源,包括其他的文件和http,https等其他的源XWalkPreferences.setValue(XWalkPreferences.ALLOW_UNIVERSAL_ACCESS_FROM_FILE, true);

        //JAVASCRIPT_CAN_OPEN_WINDOW
        XWalkPreferences.setValue(XWalkPreferences.JAVASCRIPT_CAN_OPEN_WINDOW, true);

        // enable multiple windows.
        XWalkPreferences.setValue(XWalkPreferences.SUPPORT_MULTIPLE_WINDOWS, true);

    }

    //设置加载缓存的方式，和WebView的设置方式其实差不多
    public void setCacheMode(XWalkView xw) {
        try {
            Method _getBridge = XWalkView.class.getDeclaredMethod("getBridge");
            _getBridge.setAccessible(true);
            XWalkViewBridge xWalkViewBridge = null;
            xWalkViewBridge = (XWalkViewBridge) _getBridge.invoke(xw);
            XWalkSettingsInternal xWalkSettings = xWalkViewBridge.getSettings();
            if (isWifiConnected(this)) {
                xWalkSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
            } else {
                xWalkSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            }
            xWalkSettings.setAllowUniversalAccessFromFileURLs(true);
            xWalkSettings.setAllowFileAccessFromFileURLs(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static boolean isWifiConnected(Context context) {

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                log("" + wifiInfo.getIpAddress());
            }
        }

        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        log("onPause");
        if (xWalkWebView != null && isReadyXwalk) {
            xWalkWebView.pauseTimers();
            xWalkWebView.onHide();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        log("onResume");
        mActivityDelegate.onResume();
        if (xWalkWebView != null && isReadyXwalk) {
            xWalkWebView.resumeTimers();
            xWalkWebView.onShow();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (xWalkWebView != null && isReadyXwalk) {
            xWalkWebView.onDestroy();
            isReadyXwalk = false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (xWalkWebView != null) {
            xWalkWebView.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        if (xWalkWebView != null) {
            xWalkWebView.onNewIntent(intent);
        }
    }
}
