package ai.nova.browser

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.webkit.*
import android.widget.*
import android.graphics.Color
import android.graphics.Typeface
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri

class MainActivity : Activity() {
    private lateinit var web: WebView
    private lateinit var address: EditText
    private lateinit var tabsLabel: TextView
    private lateinit var prefs: SharedPreferences
    private var guardOn = true
    private val tabs = mutableListOf("https://music.youtube.com/")
    private var active = 0

    // Basic domain-level request guard. This is NOT a YouTube-ad remover and cannot
    // reliably filter ads served from the same domains as video/audio content.
    private val blockedHosts = setOf(
        "doubleclick.net", "googlesyndication.com", "googleadservices.com",
        "adservice.google.com", "pagead2.googlesyndication.com",
        "analytics.google.com", "googletagmanager.com"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("nova", Context.MODE_PRIVATE)
        guardOn = prefs.getBoolean("guard", true)
        buildUi()
        openUrl(tabs[active])
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(10, 12, 18))
            setPadding(dp(10), dp(8), dp(10), dp(6))
        }
        val brand = TextView(this).apply {
            text = "✦ NOVA  /  CHROMIUM"
            textSize = 20f; setTextColor(Color.WHITE); typeface = Typeface.DEFAULT_BOLD
            setPadding(dp(4), dp(4), dp(4), dp(8))
        }
        root.addView(brand)
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        address = EditText(this).apply {
            hint = "Search or enter URL"; singleLine = true; textSize = 14f
            setTextColor(Color.WHITE); setHintTextColor(Color.LTGRAY)
            setBackgroundColor(Color.rgb(30, 34, 45)); setPadding(dp(12), 0, dp(8), 0)
            imeOptions = EditorInfo.IME_ACTION_GO
            setOnEditorActionListener { _, action, _ ->
                if (action == EditorInfo.IME_ACTION_GO) { navigateInput(); true } else false
            }
        }
        row.addView(address, LinearLayout.LayoutParams(0, dp(46), 1f))
        row.addView(btn("GO") { navigateInput() }, LinearLayout.LayoutParams(dp(54), dp(46)))
        root.addView(row)
        val quick = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        quick.addView(btn("YouTube") { openUrl("https://www.youtube.com/") }, weightParams())
        quick.addView(btn("YT Music") { openUrl("https://music.youtube.com/") }, weightParams())
        quick.addView(btn("+ Tab") { newTab() }, weightParams())
        quick.addView(btn("Tabs") { showTabs() }, weightParams())
        root.addView(quick)
        val controls = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        controls.addView(btn("←") { if (web.canGoBack()) web.goBack() }, weightParams())
        controls.addView(btn("→") { if (web.canGoForward()) web.goForward() }, weightParams())
        controls.addView(btn("↻") { web.reload() }, weightParams())
        controls.addView(btn(if (guardOn) "Guard: ON" else "Guard: OFF") { guardOn = !guardOn; prefs.edit().putBoolean("guard", guardOn).apply(); rebuildGuardButton() }, weightParams())
        root.addView(controls)
        tabsLabel = TextView(this).apply { text = "Tab 1 / 1"; textSize = 11f; setTextColor(Color.LTGRAY); setPadding(dp(4), dp(4), 0, dp(4)) }
        root.addView(tabsLabel)
        web = WebView(this).apply {
            setBackgroundColor(Color.BLACK)
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.loadsImagesAutomatically = true
            settings.javaScriptCanOpenWindowsAutomatically = true
            settings.setSupportMultipleWindows(false)
            settings.userAgentString = settings.userAgentString // retain official Android WebView UA
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean = false
                override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                    if (guardOn && request != null && isBlocked(request.url.host ?: "")) {
                        return WebResourceResponse("text/plain", "UTF-8", 204, "Blocked by NOVA Guard", emptyMap(), null)
                    }
                    return super.shouldInterceptRequest(view, request)
                }
                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                    address.setText(url ?: ""); updateTabs()
                }
                override fun onPageFinished(view: WebView?, url: String?) { address.setText(url ?: ""); updateTabs() }
            }
            webChromeClient = WebChromeClient()
            setDownloadListener { url, userAgent, contentDisposition, mimeType, length ->
                Toast.makeText(this@MainActivity, "Download link detected. Open in your device browser to save.", Toast.LENGTH_LONG).show()
            }
        }
        root.addView(web, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        setContentView(root)
    }

    private fun rebuildGuardButton() { /* Button label refreshes on next screen rebuild; state is persisted immediately. */ }
    private fun isBlocked(host: String): Boolean = blockedHosts.any { host == it || host.endsWith(".$it") }
    private fun navigateInput() {
        var value = address.text.toString().trim()
        if (value.isEmpty()) return
        if (!value.contains("://")) value = if (value.contains(".") && !value.contains(" ")) "https://$value" else "https://www.google.com/search?q=${Uri.encode(value)}"
        openUrl(value)
    }
    private fun openUrl(url: String) {
        if (!url.startsWith("https://") && !url.startsWith("http://")) return
        if (tabs.isEmpty()) tabs.add(url) else tabs[active] = url
        web.loadUrl(url); updateTabs()
    }
    private fun newTab() {
        tabs.add("https://www.google.com/")
        active = tabs.lastIndex
        web.loadUrl(tabs[active]); updateTabs()
    }
    private fun showTabs() {
        val labels = tabs.mapIndexed { i, url -> "${if (i == active) "● " else ""}${i + 1}. ${url.take(55)}" }.toTypedArray()
        AlertDialog.Builder(this).setTitle("NOVA Tabs")
            .setItems(labels) { _, which -> active = which; web.loadUrl(tabs[active]); updateTabs() }
            .setPositiveButton("New") { _, _ -> newTab() }
            .setNegativeButton("Close active") { _, _ ->
                if (tabs.size > 1) { tabs.removeAt(active); active = (active - 1).coerceAtLeast(0); web.loadUrl(tabs[active]); updateTabs() }
                else Toast.makeText(this, "Keep at least one tab open", Toast.LENGTH_SHORT).show()
            }.show()
    }
    private fun updateTabs() { if (::tabsLabel.isInitialized) tabsLabel.text = "Tab ${active + 1} / ${tabs.size}" }
    private fun btn(text: String, action: () -> Unit) = Button(this).apply {
        this.text = text; textSize = 11f; setTextColor(Color.WHITE)
        setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(38, 45, 60)))
        setPadding(dp(2), 0, dp(2), 0); setOnClickListener { action() }
    }
    private fun weightParams() = LinearLayout.LayoutParams(0, dp(42), 1f).apply { setMargins(dp(2), dp(4), dp(2), dp(4)) }
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    override fun onBackPressed() { if (web.canGoBack()) web.goBack() else super.onBackPressed() }
    override fun onPause() { super.onPause(); web.onPause() }
    override fun onResume() { super.onResume(); web.onResume() }
    override fun onDestroy() { web.destroy(); super.onDestroy() }
}
