package com.bibliothequeideale.app

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
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

        webView = findViewById(R.id.webview)
        webView.settings.javaScriptEnabled = true
        // domStorageEnabled est INDISPENSABLE : c'est ce qui permet aux avis
        // des lecteurs (localStorage) de fonctionner dans l'app.
        webView.settings.domStora
