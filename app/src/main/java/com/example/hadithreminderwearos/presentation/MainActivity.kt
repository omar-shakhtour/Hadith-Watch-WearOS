package com.example.hadithreminderwearos.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Cyan
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.text.HtmlCompat
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import kotlinx.coroutines.launch
import org.json.JSONObject
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HadithScreen()
        }
    }
}

@Composable
fun HadithScreen() {
    val scrollState = rememberLazyListState() // Add LazyListState

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp)
    )
    {
        val hadith = remember { mutableStateOf("") }
        var isFirstHadithGiven by remember { mutableStateOf(false) } // New flag
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            // Fetch the initial Hadith and update the flag
            val initialHadith = fetchHadith()
            hadith.value = initialHadith
            isFirstHadithGiven = true
        }

        // Fetch a new Hadith and update the state
        val fetchNewHadith: () -> Unit = {
            coroutineScope.launch {
                val newHadith = fetchHadith()
                hadith.value = newHadith

                // Scroll to the top
                scrollState.scrollToItem(0)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = scrollState,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = hadith.value,
                    textAlign = TextAlign.Center,
                    fontSize = 10.sp,
                    color = Color.White,
                    style = TextStyle(fontWeight = FontWeight.Normal),
                    softWrap = true
                )
                if (isFirstHadithGiven) {
                    Button(
                        onClick = { fetchNewHadith() },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                        modifier = Modifier.width(100.dp).height(30.dp)
                    ) {
                        Text(
                            text = "Refresh",
                            fontSize = 10.sp,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

suspend fun fetchHadith(): String {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://api.sunnah.com/v1/hadiths/random")
        .header("X-API-Key", "SqD712P3E82xnwOAEOkGd5JZH8s9wRR24TqNFzjk")
        .build()

    val response: Response = withContext(Dispatchers.IO) {
        client.newCall(request).execute()
    }

    val responseBody = response.body?.string() ?: ""
    val hadithJson = JSONObject(responseBody)
    val hadithArray = hadithJson.getJSONArray("hadith")
    val hadithObject = hadithArray.getJSONObject(0)
    var hadithBody = hadithObject.optString("body", "")

    // Remove HTML tags from the body text
    hadithBody = HtmlCompat.fromHtml(hadithBody, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()

    return hadithBody
}
