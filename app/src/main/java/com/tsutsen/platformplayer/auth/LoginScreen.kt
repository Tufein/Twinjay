/*
 * Login Screen (Compose)
 *
 * A Compose-based login screen that opens a WebView for plugin authentication.
 * Monitors network requests for auth cookies/headers and returns SourceAuth when complete.
 */

package com.tsutsen.platformplayer.auth

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.tsutsen.platformplayer.api.media.platforms.js.SourceAuth
import com.tsutsen.platformplayer.api.media.platforms.js.SourcePluginConfig
import com.tsutsen.platformplayer.core.designsystem.layout.AppHeader
import com.tsutsen.platformplayer.logging.Logger
import com.tsutsen.platformplayer.others.LoginWebViewClient

private const val TAG = "LoginScreen"

/**
 * Plugin login: the source's own sign-in page in a WebView, with a header
 * that shows which site you are on, a page-load progress bar, a short hint
 * that the screen closes by itself, reload, and back-inside-the-page.
 * The plugin's login warnings are shown above the page.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LoginScreen(
    config: SourcePluginConfig,
    onLogin: (SourceAuth?) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val webViewClient = remember { LoginWebViewClient(config) }
    var progress by remember { mutableIntStateOf(0) }
    var currentHost by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var finished by remember { mutableStateOf(false) }
    var reloadKey by remember { mutableIntStateOf(0) }
    val webViewRef = remember { mutableStateOf<WebView?>(null) }
    val loginUrl = config.authentication?.loginUrl ?: config.sourceUrl

    fun close(auth: SourceAuth?) {
        if (finished) return
        finished = true
        onLogin(auth)
        onBack()
    }

    LaunchedEffect(webViewClient) {
        webViewClient.onLogin.subscribe { auth ->
            Logger.i(TAG, "Login successful for ${config.name}")
            Handler(Looper.getMainLooper()).post {
                webViewRef.value?.stopLoading()
                Toast.makeText(context, "Signed in to ${config.name}", Toast.LENGTH_SHORT).show()
                close(auth)
            }
        }
    }

    // Load the login URL once the WebView is ready (and again on retry).
    LaunchedEffect(webViewRef.value, reloadKey) {
        val webView = webViewRef.value ?: return@LaunchedEffect
        if (loginUrl == null) {
            errorMessage = "${config.name} has no login page configured."
            return@LaunchedEffect
        }
        errorMessage = null
        Logger.i(TAG, "Loading login URL: $loginUrl")
        webView.loadUrl(loginUrl)
    }

    // Back walks back through the sign-in pages first, then cancels.
    BackHandler {
        val webView = webViewRef.value
        if (webView != null && webView.canGoBack()) webView.goBack()
        else close(null)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AppHeader(
            title = {
                Column {
                    Text(
                        "Sign in to ${config.name}",
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    currentHost?.let { host ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                host,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            },
            leading = {
                IconButton(onClick = {
                    Logger.i(TAG, "Login cancelled by user")
                    close(null)
                }) {
                    Icon(Icons.Filled.Close, contentDescription = "Cancel sign-in")
                }
            },
            actions = {
                IconButton(onClick = { webViewRef.value?.reload() ?: reloadKey++ }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Reload")
                }
            },
        )

        // Thin page-load progress bar right under the header.
        if (progress in 1..99) {
            LinearProgressIndicator(
                progress = { progress / 100f },
                modifier = Modifier.fillMaxWidth().height(2.dp),
            )
        } else {
            Spacer(Modifier.height(2.dp))
        }

        // What happens next, so it doesn't feel like a dead end.
        LoginNote(
            icon = Icons.Outlined.Info,
            text = "Sign in as you normally would. This screen closes by itself once you are signed in; " +
                "your login stays on this device.",
            container = MaterialTheme.colorScheme.secondaryContainer,
            content = MaterialTheme.colorScheme.onSecondaryContainer,
        )
        config.authentication?.loginWarning?.let { warning ->
            LoginNote(
                icon = Icons.Outlined.WarningAmber,
                text = warning,
                container = MaterialTheme.colorScheme.errorContainer,
                content = MaterialTheme.colorScheme.onErrorContainer,
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { ctx ->
                    val webView = WebView(ctx)
                    webView.settings.javaScriptEnabled = true
                    webView.settings.domStorageEnabled = true
                    CookieManager.getInstance().setAcceptCookie(true)
                    webView.webViewClient = webViewClient
                    webView.webChromeClient =
                        object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                progress = newProgress
                                currentHost = view?.url?.let { runCatching { android.net.Uri.parse(it).host }.getOrNull() }
                            }

                            override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {}
                        }
                    webView.settings.useWideViewPort = true
                    webView.settings.loadWithOverviewMode = true

                    // Set custom user agent if configured
                    config.authentication?.userAgent?.let { ua ->
                        webView.settings.userAgentString = ua
                    }

                    webViewRef.value = webView
                    webView
                },
                modifier = Modifier.fillMaxSize(),
                onRelease = { webView ->
                    webViewRef.value = null
                    webView.destroy()
                },
            )

            errorMessage?.let { error ->
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                    )
                    Button(onClick = { reloadKey++ }) { Text("Try again") }
                }
            }
        }
    }
}

@Composable
private fun LoginNote(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    container: androidx.compose.ui.graphics.Color,
    content: androidx.compose.ui.graphics.Color,
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = container, contentColor = content),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text(text, style = MaterialTheme.typography.bodySmall)
        }
    }
}
