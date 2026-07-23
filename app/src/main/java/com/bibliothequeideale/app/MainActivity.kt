package com.bibliothequeideale.app

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.ServiceWorkerClient
import android.webkit.ServiceWorkerController
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : Activity() {

    private lateinit var webView: WebView

    // URL du site hébergé (Netlify).
    private val siteUrl = "https://labibliothequeideale.netlify.app/"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialisation du SDK AdMob sur un thread d'arrière-plan (recommandation Google)
        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(this@MainActivity) {}
        }

        val adView = findViewById<AdView>(R.id.adView)
        adView.loadAd(AdRequest.Builder().build())

        // Nettoyage forcé de tout le stockage WebView (cache, cookies,
        // bases de données) à chaque démarrage de l'app, pour repartir sur
        // une base garantie propre quoi qu'il ait pu être enregistré avant.
        WebView.setWebContentsDebuggingEnabled(true)
        CookieManager.getInstance().removeAllCookies(null)
        WebStorage.getInstance().deleteAllData()

        // Neutralise tout service worker, y compris un déjà enregistré et
        // actif avant ce correctif : contrairement au blocage sur le
        // WebViewClient normal (qui n'empêche qu'un *nouvel* enregistrement),
        // ceci intercepte directement les requêtes gérées par le service
        // worker lui-même, où qu'il ait été enregistré auparavant.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val swController = ServiceWorkerController.getInstance()
            swController.setServiceWorkerClient(object : ServiceWorkerClient() {
                override fun shouldInterceptRequest(request: WebResourceRequest): WebResourceResponse? {
                    return WebResourceResponse("text/plain", "utf-8", null)
                }
            })
        }

        webView = findViewById(R.id.webview)
        webView.settings.javaScriptEnabled = true
        // domStorageEnabled est INDISPENSABLE : c'est ce qui permet aux avis
        // des lecteurs (localStorage) de fonctionner dans l'app.
        webView.settings.domStorageEnabled = true
        webView.settings.cacheMode = android.webkit.WebSettings.LOAD_NO_CACHE
        webView.clearCache(true)
        webView.clearHistory()

        webView.webViewClient = object : WebViewClient() {
            // Filet de sécurité supplémentaire : bloque aussi tout appel
            // direct à sw.js passant par le chargement normal de la page.
            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                val url = request?.url?.toString() ?: ""
                if (url.endsWith("sw.js")) {
                    return WebResourceResponse("application/javascript", "utf-8", null)
                }
                return super.shouldInterceptRequest(view, request)
            }
        }
        webView.webChromeClient = WebChromeClient()
        webView.loadUrl(siteUrl)
    }

    // Le bouton "retour" navigue dans l'historique de la page plutôt que de
    // fermer directement l'application.
    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) {
            webView.goBack()
        } else {
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
    }
}
