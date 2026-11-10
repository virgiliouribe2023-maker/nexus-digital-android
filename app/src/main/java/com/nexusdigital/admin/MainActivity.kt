package com.nexusdigital.admin

import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var tokenFcmPendiente: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview)
        val swipeRefresh = findViewById<SwipeRefreshLayout>(R.id.swipe_refresh)

        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.databaseEnabled = true
        // Le agregamos nuestra "firma" al user agent para que el sitio sepa que la página
        // se está mostrando dentro de esta app y active el registro del token de notificaciones.
        webView.settings.userAgentString = webView.settings.userAgentString + " NexusDigitalAndroidApp/1.0"

        swipeRefresh.setOnRefreshListener {
            webView.reload()
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                swipeRefresh.isRefreshing = false
                enviarTokenSiHayUno()
            }
        }

        val urlDestino = intent.getStringExtra(EXTRA_URL_ABRIR) ?: getString(R.string.admin_url)
        webView.loadUrl(urlDestino)

        obtenerTokenFcm()
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    private fun obtenerTokenFcm() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { tarea ->
            if (tarea.isSuccessful) {
                tokenFcmPendiente = tarea.result
                enviarTokenSiHayUno()
            }
        }
    }

    private fun enviarTokenSiHayUno() {
        val token = tokenFcmPendiente ?: return
        val js = "if (window.registrarTokenFcmNexus) { window.registrarTokenFcmNexus('" + token.replace("'", "") + "'); }"
        webView.evaluateJavascript(js, null)
    }

    companion object {
        const val EXTRA_URL_ABRIR = "url_abrir"
    }
}
