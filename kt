package www.studytask.ptbr.ceara

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.*
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    private lateinit var fullScreenContainer: FrameLayout
    private lateinit var webView: WebView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var progressBar: View // ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            window.statusBarColor = Color.TRANSPARENT
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1
            )
        }

        fullScreenContainer = findViewById(R.id.fullscreen_container)
        webView = findViewById(R.id.webview)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        progressBar = findViewById(R.id.progress_bar)

        val menuButton: View = findViewById(R.id.menu_button)
        menuButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.allowFileAccess = true
        webSettings.allowContentAccess = true
        webSettings.cacheMode = WebSettings.LOAD_DEFAULT
        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        webSettings.mediaPlaybackRequiresUserGesture = false

        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                fullScreenContainer.visibility = View.VISIBLE
                fullScreenContainer.addView(view)
                customView = view
                customViewCallback = callback
            }

            override fun onHideCustomView() {
                super.onHideCustomView()
                if (customView != null) {
                    fullScreenContainer.removeView(customView)
                    fullScreenContainer.visibility = View.GONE
                    customView = null
                    customViewCallback?.onCustomViewHidden()
                }
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url != null && (url.contains("ad") || url.contains("ads") || url.contains("track") || url.contains("popup"))) {
                    return true
                }
                return super.shouldOverrideUrlLoading(view, url)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                showLoading()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                hideLoading()
                webView.loadUrl("javascript:window.ads.blockAds()")
            }
        }

        webView.addJavascriptInterface(object : Any() {
            @JavascriptInterface
            fun blockAds() {
                val jsCode = """
                    var ads = document.querySelectorAll('iframe, .ads, .advertisement, .banner, .sponsor, .popup, .ad-container, .ad-banner, .ad-frame, .ad-box, .ad-block');
                    for (var i = 0; i < ads.length; i++) {
                        ads[i].style.display = 'none';
                        ads[i].remove();
                    }
                """
                webView.evaluateJavascript(jsCode, null)
            }
        }, "ads")

        webView.setDownloadListener { url, _, _, _, _ ->
            val request = DownloadManager.Request(Uri.parse(url))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                request.allowScanningByMediaScanner()
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            }
            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
        }

        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)

        if (networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            webView.loadUrl("https://infinitycodegov.github.io/SiteTeste/")
        } else {
            webView.loadData(
                "<html><body><h1>Offline</h1><p>Sem conexão com a internet.</p></body></html>",
                "text/html",
                "UTF-8"
            )
        }

        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_item1 -> {
                    webView.loadUrl("https://visioncine-1.com.br")
                    true
                }
                R.id.nav_item2 -> {
                    webView.loadUrl("https://aluno.seduc.ce.gov.br")
                    true
                }
                else -> false
            }.also {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
        }
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        webView.visibility = View.GONE
    }

    private fun hideLoading() {
        progressBar.visibility = View.GONE
        webView.visibility = View.VISIBLE
    }

    override fun onBackPressed() {
        if (customView != null) {
            onHideCustomView()
        } else if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    private fun onHideCustomView() {
        if (customView != null) {
            fullScreenContainer.removeView(customView)
            customView = null
            fullScreenContainer.visibility = View.GONE
            webView.visibility = View.VISIBLE
            customViewCallback?.onCustomViewHidden()
        }
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        webView.destroy()
    }
}
