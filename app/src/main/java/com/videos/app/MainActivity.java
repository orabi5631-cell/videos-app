package com.videos.app;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.webkit.*;
import android.widget.Toast;

public class MainActivity extends Activity {
    // >>> حط هنا لينك السيرفر بتاعك (السيرفر اللي شغال عليه server.py) <<<
    static final String SERVER_URL = "Enter";

    private WebView wv;
    private ValueCallback<Uri[]> fileCb;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        wv = new WebView(this);
        wv.setBackgroundColor(Color.parseColor("#0b0b1a"));
        setContentView(wv);

        WebSettings s = wv.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setAllowFileAccess(true);

        wv.setWebViewClient(new WebViewClient());
        wv.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView w, ValueCallback<Uri[]> cb, FileChooserParams p) {
                if (fileCb != null) fileCb.onReceiveValue(null);
                fileCb = cb;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                i.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"video/*", "image/*"});
                startActivityForResult(Intent.createChooser(i, "اختار ملف"), 1);
                return true;
            }
        });

        wv.setDownloadListener((url, ua, cd, mime, len) -> {
            try {
                String name = URLUtil.guessFileName(url, cd, mime);
                DownloadManager.Request r = new DownloadManager.Request(Uri.parse(url));
                r.setMimeType(mime);
                r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name);
                ((DownloadManager) getSystemService(DOWNLOAD_SERVICE)).enqueue(r);
                Toast.makeText(this, "بدأ التنزيل...", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "فشل التنزيل", Toast.LENGTH_SHORT).show();
            }
        });

        wv.loadUrl(SERVER_URL);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        if (req == 1 && fileCb != null) {
            Uri[] r = null;
            if (res == RESULT_OK && data != null)
                r = WebChromeClient.FileChooserParams.parseResult(res, data);
            fileCb.onReceiveValue(r);
            fileCb = null;
        }
        super.onActivityResult(req, res, data);
    }

    @Override
    public void onBackPressed() {
        if (wv.canGoBack()) wv.goBack(); else super.onBackPressed();
    }

    @Override protected void onPause() { super.onPause(); wv.onPause(); }
    @Override protected void onResume() { super.onResume(); wv.onResume(); }
}
