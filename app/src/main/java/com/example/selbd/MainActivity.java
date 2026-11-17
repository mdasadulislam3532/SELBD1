package com.example.selbd;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.webkit.ProxyConfig;
import androidx.webkit.ProxyController;
import androidx.webkit.WebViewFeature;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar progressBar;

    private final String CURRENT_VERSION = "v1.0.1";
    private final String GITHUB_REPO = "mdasadulislam3532/SELBD1";
    private final String TELEGRAM_BOT_URL = "https://t.me/BUYHOTMAILPRO_bot";
    private final String TELEGRAM_CHANNEL_URL = "https://t.me/sendotp12";
    private final String TWO_FACTOR_URL = "https://2fa.live";

    private String currentProxyHost = "";
    private int currentProxyPort = 0;

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
            showProProxyDialog();
            return true;
        } else if (id == R.id.action_clear_data) {
            confirmClearData();
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

    // ১ ক্লিকে সব ডাটা, ক্যাশ ও কুকি ক্লিয়ার করার অপশন
    private void confirmClearData() {
        new AlertDialog.Builder(this)
                .setTitle("অ্যাপ ডাটা ক্লিয়ার")
                .setMessage("আপনি কি সব ব্রাউজিং হিস্টোরি, ক্যাশ ও সেশন কুকি মুছে ফ্রেশভাবে রিস্টার্ট করতে চান?")
                .setPositiveButton("ক্লিয়ার করুন", (dialog, which) -> {
                    if (webView != null) {
                        webView.clearCache(true);
                        webView.clearHistory();
                        webView.clearFormData();
                    }
                    WebStorage.getInstance().deleteAllData();
                    CookieManager.getInstance().removeAllCookies(null);
                    CookieManager.getInstance().flush();
                    Toast.makeText(MainActivity.this, "অ্যাপ ডাটা সম্পূর্ণ ক্লিয়ার করা হয়েছে!", Toast.LENGTH_LONG).show();
                    loadHome();
                })
                .setNegativeButton("বাতিল", null)
                .show();
    }

    // প্রো-লেভেল প্রক্সি ডায়ালগ (ফুল ফরম্যাট ও লাইভ লোকেশন চেকার)
    private void showProProxyDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_proxy, null);
        EditText etProxyFull = dialogView.findViewById(R.id.etProxyFull);
        Button btnCheckLocation = dialogView.findViewById(R.id.btnCheckLocation);
        TextView tvLocationResult = dialogView.findViewById(R.id.tvLocationResult);

        if (!currentProxyHost.isEmpty()) {
            etProxyFull.setText(currentProxyHost + ":" + currentProxyPort);
        }

        btnCheckLocation.setOnClickListener(v -> {
            String input = etProxyFull.getText().toString().trim();
            if (input.isEmpty()) {
                Toast.makeText(this, "অনুগ্রহ করে আগে প্রক্সি দিন!", Toast.LENGTH_SHORT).show();
                return;
            }
            tvLocationResult.setText("লাইভ লোকেশন চেক করা হচ্ছে... অপেক্ষা করুন");
            checkProxyLocation(input, tvLocationResult);
        });

        new AlertDialog.Builder(this)
                .setTitle("প্রো প্রক্সি কনফিগারেশন")
                .setView(dialogView)
                .setPositiveButton("প্রয়োগ করুন", (dialog, which) -> {
                    String input = etProxyFull.getText().toString().trim();
                    applyProxy(input);
                })
                .setNeutralButton("প্রক্সি রিমুভ", (dialog, which) -> {
                    removeProxy();
                })
                .setNegativeButton("বাতিল", null)
                .show();
    }

    // ফুল ফরম্যাট প্রক্সি পার্সিং এবং অ্যাপ্লাই করা
    private void applyProxy(String input) {
        if (input.isEmpty()) {
            removeProxy();
            return;
        }

        try {
            String clean = input.replace("http://", "").replace("https://", "").trim();
            String[] parts = clean.split(":");
            if (parts.length >= 2) {
                String host = parts[0].trim();
                int port = Integer.parseInt(parts[1].trim());

                currentProxyHost = host;
                currentProxyPort = port;

                // AndroidX WebKit ProxyController (আধুনিক নিরাপদ পদ্ধতি)
                if (WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
                    ProxyConfig proxyConfig = new ProxyConfig.Builder()
                            .addProxyRule(host + ":" + port)
                            .build();
                    ProxyController.getInstance().setProxyOverride(proxyConfig, Runnable::run, () -> {
                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, "প্রক্সি সফলভাবে সেট করা হয়েছে: " + host + ":" + port, Toast.LENGTH_SHORT).show();
                            webView.reload();
                        });
                    });
                } else {
                    // ফলব্যাক সিস্টেম প্রোপার্টিজ
                    System.setProperty("http.proxyHost", host);
                    System.setProperty("http.proxyPort", String.valueOf(port));
                    System.setProperty("https.proxyHost", host);
                    System.setProperty("https.proxyPort", String.valueOf(port));
                    Toast.makeText(this, "প্রক্সি প্রয়োগ করা হয়েছে: " + host + ":" + port, Toast.LENGTH_SHORT).show();
                    webView.reload();
                }
            } else {
                Toast.makeText(this, "ভুল ফরম্যাট! IP:Port দিন (যেমন: 198.51.100.1:8080)", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "প্রক্সি সেট করতে ত্রুটি: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void removeProxy() {
        currentProxyHost = "";
        currentProxyPort = 0;
        if (WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
            ProxyController.getInstance().clearProxyOverride(Runnable::run, () -> {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "প্রক্সি রিমুভ করা হয়েছে", Toast.LENGTH_SHORT).show();
                    webView.reload();
                });
            });
        }
        System.clearProperty("http.proxyHost");
        System.clearProperty("http.proxyPort");
        System.clearProperty("https.proxyHost");
        System.clearProperty("https.proxyPort");
        Toast.makeText(this, "প্রক্সি সম্পূর্ণ রিমুভ করা হয়েছে", Toast.LENGTH_SHORT).show();
        webView.reload();
    }

    // লাইভ আইপি ও লোকেশন টেস্ট ফাংশন
    private void checkProxyLocation(String proxyInput, TextView tvResult) {
        new Thread(() -> {
            try {
                String clean = proxyInput.replace("http://", "").replace("https://", "").trim();
                String[] parts = clean.split(":");
                String host = parts[0].trim();
                int port = Integer.parseInt(parts[1].trim());

                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
                URL url = new URL("https://ipwho.is/");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection(proxy);
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();

                JSONObject json = new JSONObject(sb.toString());
                boolean success = json.optBoolean("success", true);
                if (success) {
                    String ip = json.optString("ip", host);
                    String country = json.optString("country", "Unknown");
                    String city = json.optString("city", "Unknown");
                    String flag = json.optJSONObject("flag") != null ? json.optJSONObject("flag").optString("emoji", "🌐") : "🌐";

                    String resultText = flag + " আইপি: " + ip + "\n📍 লোকেশন: " + city + ", " + country + "\n⚡ স্ট্যাটাস: প্রক্সি সক্রিয় ও কার্যকর!";
                    runOnUiThread(() -> tvResult.setText(resultText));
                } else {
                    runOnUiThread(() -> tvResult.setText("❌ প্রক্সি থেকে সঠিক রেসপন্স পাওয়া যায়নি।"));
                }
            } catch (Exception e) {
                runOnUiThread(() -> tvResult.setText("❌ প্রক্সি কানেকশন ব্যর্থ হয়েছে!\nকারণ: আইপি অফলাইন বা পোর্ট ভুল।"));
            }
        }).start();
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
