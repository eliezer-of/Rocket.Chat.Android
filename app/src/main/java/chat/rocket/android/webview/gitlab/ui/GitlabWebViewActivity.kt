package chat.rocket.android.webview.gitlab.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.net.toUri
import chat.rocket.android.R
import chat.rocket.android.util.extensions.decodeUrl
import chat.rocket.android.util.extensions.toJsonObject
import kotlinx.android.synthetic.main.activity_web_view.*
import kotlinx.android.synthetic.main.app_bar.*
import org.json.JSONObject

fun Context.gitlabWebViewIntent(webPageUrl: String, state: String): Intent {
    return Intent(this, GitlabWebViewActivity::class.java).apply {
        putExtra(INTENT_WEB_PAGE_URL, webPageUrl)
        putExtra(INTENT_STATE, state)
    }
}

private const val INTENT_WEB_PAGE_URL = "web_page_url"
private const val INTENT_STATE = "state"
private const val JSON_CREDENTIAL_TOKEN = "credentialToken"
private const val JSON_CREDENTIAL_SECRET = "credentialSecret"
const val INTENT_OAUTH_CREDENTIAL_TOKEN = "credential_token"
const val INTENT_OAUTH_CREDENTIAL_SECRET = "credential_secret"

// Shows a WebView to the user authenticate with your Gitlab credentials.
class GitlabWebViewActivity : AppCompatActivity() {
    private lateinit var webPageUrl: String
    private lateinit var state: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        webPageUrl = intent.getStringExtra(INTENT_WEB_PAGE_URL)
        requireNotNull(webPageUrl) { "no web_page_url provided in Intent extras" }

        state = intent.getStringExtra(INTENT_STATE)
        requireNotNull(state) { "no state provided in Intent extras" }

        setupToolbar()
    }

    override fun onResume() {
        super.onResume()
        setupWebView()
    }

    override fun onBackPressed() {
        if (web_view.canGoBack()) {
            web_view.goBack()
        } else {
            closeView()
        }
    }

    private fun setupToolbar() {
        toolbar.title = getString(R.string.title_authentication)
        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp)
        toolbar.setNavigationOnClickListener { closeView() }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        web_view.settings.javaScriptEnabled = true
        web_view.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                if (url.contains(JSON_CREDENTIAL_TOKEN) && url.contains(JSON_CREDENTIAL_SECRET)) {
                    if (isStateValid(url)) {
                        val jsonResult = url.decodeUrl()
                            .substringAfter("#")
                            .toJsonObject()
                        val credentialToken = getCredentialToken(jsonResult)
                        val credentialSecret = getCredentialSecret(jsonResult)
                        if (credentialToken.isNotEmpty() && credentialSecret.isNotEmpty()) {
                            closeView(Activity.RESULT_OK, credentialToken, credentialSecret)
                        }
                    }
                }
                view_loading.hide()
            }
        }
        web_view.loadUrl(webPageUrl)
    }

    // If the states matches, then try to get the code, otherwise the request was created by a third party and the process should be aborted.
    private fun isStateValid(url: String): Boolean =
        url.substringBefore("#").toUri().getQueryParameter(INTENT_STATE) == state

    private fun getCredentialToken(json: JSONObject): String =
        json.optString(JSON_CREDENTIAL_TOKEN)

    private fun getCredentialSecret(json: JSONObject): String =
        json.optString(JSON_CREDENTIAL_SECRET)

    private fun closeView(activityResult: Int = Activity.RESULT_CANCELED, credentialToken: String? = null, credentialSecret: String? = null) {
        setResult(activityResult, Intent().putExtra(INTENT_OAUTH_CREDENTIAL_TOKEN, credentialToken).putExtra(INTENT_OAUTH_CREDENTIAL_SECRET, credentialSecret))
        finish()
        overridePendingTransition(R.anim.hold, R.anim.slide_down)
    }
}