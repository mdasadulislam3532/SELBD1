package com.example.selbd;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar progressBar;

    private final String CURRENT_VERSION = "v1.0.0";
    private final String GITHUB_REPO = "mdasadulislam3532/SELBD1";
    private final String TELEGRAM_BOT_URL = "https://t.me/BUYHOTMAILPRO_bot";
    private final String TELEGRAM_CHANNEL_URL = "https://t.me/sendotp12";
    private final String TWO_FACTOR_URL = "https://2fa.live";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("SELBD");
        }

        progressBar = findViewById(R.id.progressBar);
        webView = findViewById(R.id.webView);

        initWebView();
        loadHome();
    }

    private void initWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
            }
        });
    }

    private void loadHome() {
        webView.getSettings().setUserAgentString(null);
        webView.loadUrl("https://m.facebook.com");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.mode_mobile) {
            webView.getSettings().setUserAgentString(null);
            webView.loadUrl("https://m.facebook.com");
            Toast.makeText(this, "Mobile View লোড হচ্ছে", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.mode_mbasic) {
            webView.getSettings().setUserAgentString(null);
            webView.loadUrl("https://mbasic.facebook.com");
            Toast.makeText(this, "mBasic View লোড হচ্ছে", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.mode_desktop) {
            String desktopUA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36";
            webView.getSettings().setUserAgentString(desktopUA);
            webView.loadUrl("https://www.facebook.com");
            Toast.makeText(this, "Desktop View লোড হচ্ছে", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_proxy) {
            showProxyDialog();
            return true;
        } else if (id == R.id.action_2fa) {
            openExternalUrl(TWO_FACTOR_URL);
            return true;
        } else if (id == R.id.action_telegram_bot) {
            openExternalUrl(TELEGRAM_BOT_URL);
            return true;
        } else if (id == R.id.action_telegram_support) {
            openExternalUrl(TELEGRAM_CHANNEL_URL);
            return true;
        } else if (id == R.id.action_update) {
            checkGitHubUpdate();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showProxyDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_proxy, null);
        EditText etHost = dialogView.findViewById(R.id.etProxyHost);
        EditText etPort = dialogView.findViewById(R.id.etProxyPort);

        new AlertDialog.Builder(this)
                .setTitle("SELBD Proxy সেটিংস")
                .setView(dialogView)
                .setPositiveButton("প্রয়োগ", (dialog, which) -> {
                    String host = etHost.getText().toString().trim();
                    String port = etPort.getText().toString().trim();

                    if (!host.isEmpty() && !port.isEmpty()) {
                        System.setProperty("http.proxyHost", host);
                        System.setProperty("http.proxyPort", port);
                        System.setProperty("https.proxyHost", host);
                        System.setProperty("https.proxyPort", port);
                        Toast.makeText(MainActivity.this, "প্রক্সি সেট হয়েছে: " + host + ":" + port, Toast.LENGTH_SHORT).show();
                        webView.reload();
                    } else {
                        System.clearProperty("http.proxyHost");
                        System.clearProperty("http.proxyPort");
                        System.clearProperty("https.proxyHost");
                        System.clearProperty("https.proxyPort");
                        Toast.makeText(MainActivity.this, "প্রক্সি রিমুভ করা হয়েছে", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("বাতিল", null)
                .show();
    }

    private void openExternalUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "লিংক ওপেন করা সম্ভব হয়নি", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkGitHubUpdate() {
        Toast.makeText(this, "আপডেট চেক করা হচ্ছে...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                URL url = new URL("https://api.github.com/repos/" + GITHUB_REPO + "/releases/latest");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/vnd.github.v3+json");

                if (conn.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    JSONObject json = new JSONObject(response.toString());
                    String latestVersion = json.getString("tag_name");
                    String downloadUrl = json.getString("html_url");

                    runOnUiThread(() -> {
                        if (!latestVersion.equalsIgnoreCase(CURRENT_VERSION)) {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("SELBD নতুন আপডেট পাওয়া গেছে!")
                                    .setMessage("ভার্সন: " + latestVersion + "\nএখনই ডাউনলোড করবেন?")
                                    .setPositiveButton("ডাউনলোড", (d, w) -> openExternalUrl(downloadUrl))
                                    .setNegativeButton("পরে", null)
                                    .show();
                        } else {
                            Toast.makeText(MainActivity.this, "SELBD সম্পূর্ণ আপ-টু-ডেট আছে", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "আপডেট তথ্য পাওয়া যায়নি", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
