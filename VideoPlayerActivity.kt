package www.studytask.ptbr.ceara

import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class VideoPlayerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.video_player)

        val webView: WebView = findViewById(R.id.webView)
        
        // Habilitar JavaScript para exibir vídeos e conteúdos interativos
        webView.settings.javaScriptEnabled = true
        
        // Definir WebViewClient para impedir que o navegador externo seja aberto
        webView.webViewClient = WebViewClient()

        // Definir WebChromeClient para exibir controles de vídeo, se necessários
        webView.webChromeClient = WebChromeClient()

        // Carregar o site de streaming, por exemplo, o YouTube
        val url = "https://www.example.com"  // Substitua pela URL do site de filmes ou streaming
        webView.loadUrl(url)
    }

    // Permitir navegação para trás dentro do WebView
    override fun onBackPressed() {
        val webView: WebView = findViewById(R.id.webView)
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
