package com.nexusdigital.admin

import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
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
                // Fuerza a que la cookie de "recordar sesión" (y las demás) se guarden ya
                // en disco, para que sigan ahí aunque se cierre la app o se reinicie el celular.
                CookieManager.getInstance().flush()
            }
        }

        // Sin esto, los confirm()/alert()/prompt() de JavaScript (como los "¿Eliminar...?"
        // de pedidos, chats, devoluciones, etc.) NO se muestran dentro del WebView y el
        // botón parece no hacer nada. Aquí los mostramos como diálogos nativos de Android.
        webView.webChromeClient = object : WebChromeClient() {
            override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                AlertDialog.Builder(this@MainActivity)
                    .setMessage(message)
                    .setPositiveButton("Aceptar") { _, _ -> result?.confirm() }
                    .setOnCancelListener { result?.cancel() }
                    .setCancelable(false)
                    .show()
                return true
            }

            override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                AlertDialog.Builder(this@MainActivity)
                    .setMessage(message)
                    .setPositiveButton("Aceptar") { _, _ -> result?.confirm() }
                    .setNegativeButton("Cancelar") { _, _ -> result?.cancel() }
                    .setOnCancelListener { result?.cancel() }
                    .setCancelable(true)
                    .show()
                return true
            }

            override fun onJsPrompt(
                view: WebView?, url: String?, message: String?, defaultValue: String?, result: JsPromptResult?
            ): Boolean {
                val campoTexto = EditText(this@MainActivity)
                campoTexto.setText(defaultValue)
                AlertDialog.Builder(this@MainActivity)
                    .setMessage(message)
                    .setView(campoTexto)
                    .setPositiveButton("Aceptar") { _, _ -> result?.confirm(campoTexto.text.toString()) }
                    .setNegativeButton("Cancelar") { _, _ -> result?.cancel() }
                    .setOnCancelListener { result?.cancel() }
                    .setCancelable(true)
                    .show()
                return true
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
