package com.hadithwatch.hadithreminderwearos.presentation

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import kotlinx.serialization.Serializable

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Cyan
import androidx.compose.ui.platform.LocalContext
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
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HadithScreen(context = this)
        }
    }
}

@Composable
fun HadithScreen(context: Context) {
    val scrollState = rememberLazyListState() // Add LazyListState

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp)
    )
    {
        val hadithList = remember { mutableStateListOf<Hadith>() }
        val coroutineScope = rememberCoroutineScope()

        // Fetch the initial Hadith
        LaunchedEffect(Unit) {
            hadithList.addAll(parseHadithJson(context))
        }

        // Fetch a new Hadith and update the state
        val fetchNewHadith: () -> Unit = {
            coroutineScope.launch {
                val newHadithList = parseHadithJson(context)
                hadithList.clear()
                hadithList.addAll(newHadithList)

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
                val random = hadithList.shuffled().firstOrNull()
                Text(
                    text = random?.english ?: "",
                    textAlign = TextAlign.Center,
                    fontSize = 10.sp,
                    color = Color.White,
                    style = TextStyle(fontWeight = FontWeight.Normal),
                    softWrap = true
                )

                Text(
                    text = "[Sahih Bukhari]",
                    textAlign = TextAlign.Center,
                    fontSize = 10.sp,
                    color = Color.White,
                    style = TextStyle(fontWeight = FontWeight.Normal),
                    softWrap = true
                )

                if (hadithList.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .padding(top = 20.dp)
                            .wrapContentWidth()
                    ) {
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
}

fun parseHadithJson(context: Context): List<Hadith> {
    val hadithList = mutableListOf<Hadith>()

    try {
        val jsonString = context.assets.open("bukhari.json").bufferedReader().use { it.readText() }
        val jsonArray = JSONArray(jsonString)

        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val reference = jsonObject.getString("reference")
            val arabic = jsonObject.getString("arabic")
            val english = jsonObject.getString("english")
                .replace(":", ": ")
                .replace("Narrated ", "Narrated by ")
            val hadith = Hadith(reference, arabic, english)
            hadithList.add(hadith)
        }
    } catch (e: IOException) {
        e.printStackTrace()
    } catch (e: JSONException) {
        e.printStackTrace()
    }

    return hadithList
}
