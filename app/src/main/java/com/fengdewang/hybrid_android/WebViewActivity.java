package com.fengdewang.hybrid_android;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class WebViewActivity extends AppCompatActivity {

    private Handler uiHandler;

    private ActionBar actionBar;

    private WebView customWebView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        uiHandler = new WebViewUIHandler(WebViewActivity.this);

        actionBar = getSupportActionBar();
        initTitleBar();

        progressBar = findViewById(R.id.progressBar);
        progressBar.setAlpha(0);

        customWebView = findViewById(R.id.customWebView);

        WebSettings settings = customWebView.getSettings();

        //如果访问的页面中要与Javascript交互，则webview必须设置支持Javascript
        settings.setJavaScriptEnabled(true);

        //设置自适应屏幕，两者合用
        settings.setUseWideViewPort(true); //将图片调整到适合webview的大小
        settings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小

        //缩放操作
        settings.setSupportZoom(false);

        //其他细节操作
        //关闭webview中缓存
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        //设置可以访问文件
        settings.setAllowFileAccess(true);
        //支持通过JS打开新窗口 允许JS弹窗
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        //支持自动加载图片
        settings.setLoadsImagesAutomatically(true);
        //设置编码格式
        settings.setDefaultTextEncodingName("utf-8");

        //离线加载

//        if(NetStatusUtil){
//
//        }

        // 开启 DOM storage API 功能，就是localStorage
        settings.setDomStorageEnabled(true);
        //开启 database storage API 功能
        settings.setDatabaseEnabled(true);
        //开启 Application Caches 功能
        settings.setAppCacheEnabled(true);

        //原生webview load url 会直接跳转至系统浏览器，所有重新覆盖此跳转
        customWebView.setWebViewClient(new WebViewClient(){

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {

                view.loadUrl(request.getUrl().toString());

                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                //super.onPageStarted(view, url, favicon);
                //todo 开始加载网页 显示loading
                System.out.println(url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                //super.onPageFinished(view, url);
                //todo 网页加载结束 隐藏loading
                System.out.println(url);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                //super.onReceivedError(view, request, error);
                //todo 加载失败了 显示404

            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                //处理https请求 默认不支持https

                super.onReceivedSslError(view, handler, error);
                handler.proceed();//表示等待证书响应

            }
        });

        customWebView.setWebChromeClient(new WebChromeClient(){

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                //super.onProgressChanged(view, newProgress);

                if(newProgress >= 100){
                    progressBar.setAlpha(0);
                } else {
                    progressBar.setAlpha(1);
                    progressBar.setProgress(newProgress);
                }

            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                //todo 设置title
                actionBar.setTitle(title);
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {

                //todo alert拦截
                AlertDialog.Builder alert = new AlertDialog.Builder(WebViewActivity.this);
                alert.setTitle("提示");
                alert.setMessage(message);
                alert.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        result.confirm();
                    }
                });
                alert.setCancelable(false);
                alert.create().show();

                return true;
            }
        });


        // 通过addJavascriptInterface()将Java对象映射到JS对象
        //参数1：Java对象名
        //参数2：Javascript对象名
        JSBridge jsbridge = new JSBridge(uiHandler);
        customWebView.addJavascriptInterface(jsbridge, "JSBridgeAndroid");

        customWebView.loadUrl("file:///android_asset/JSBridgeDemo.html");
        //customWebView.loadUrl("https://www.baidu.com");

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        //右下角返回按钮 先判断webview是否为可后退状态 ，是的话优先进行 history.back()
        if(keyCode == KeyEvent.KEYCODE_BACK && customWebView.canGoBack()){

            customWebView.goBack();
            return true;

        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //左上角按钮 直接关闭webview
        if(item.getItemId() == android.R.id.home){

            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {

        if(customWebView != null){
            customWebView.clearHistory();
            customWebView.destroy();
            customWebView = null;
        }

        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();

        customWebView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        customWebView.onResume();
    }

    public void setTitleVisibility(Boolean show){

        if(!show){
            actionBar.hide();
        } else {
            actionBar.show();
        }

    }


    public void initTitleBar(){
        actionBar.setDisplayHomeAsUpEnabled(true); //是否在左侧返回区域显示返回箭头，默认不显示
        actionBar.setDisplayShowTitleEnabled(true); //是否在左侧返回区域显示左侧标题，默认显示APP名称   setTitle : 设置左侧标题的文本

    }


}
