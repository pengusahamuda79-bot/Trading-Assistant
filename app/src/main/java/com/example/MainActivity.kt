package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.delay
import kotlin.math.abs
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// TradingView Live Quote Data Class
data class TradingViewQuote(
  val price: Double,
  val changePercent: Double,
  val open: Double,
  val high: Double,
  val low: Double,
  val volume: Double,
  val timeString: String
)

// Helper to fetch live quote from TradingView Global Scan API
fun fetchTradingViewXauUsd(callback: (Result<TradingViewQuote>) -> Unit) {
  val client = OkHttpClient()
  val mediaType = "application/json; charset=utf-8".toMediaType()
  val jsonPayload = """
    {
      "symbols": {
        "tickers": ["OANDA:XAUUSD"]
      },
      "columns": ["close", "change", "open", "high", "low", "volume"]
    }
  """.trimIndent()
  
  val requestBody = jsonPayload.toRequestBody(mediaType)
  val request = Request.Builder()
    .url("https://scanner.tradingview.com/global/scan")
    .post(requestBody)
    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
    .header("Accept", "application/json")
    .build()

  client.newCall(request).enqueue(object : okhttp3.Callback {
    override fun onFailure(call: okhttp3.Call, e: IOException) {
      callback(Result.failure(e))
    }

    override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
      try {
        if (!response.isSuccessful) {
          callback(Result.failure(Exception("HTTP Error: ${response.code}")))
          return
        }
        val bodyString = response.body?.string() ?: ""
        val json = JSONObject(bodyString)
        val dataArray = json.getJSONArray("data")
        if (dataArray.length() > 0) {
          val dataObj = dataArray.getJSONObject(0)
          val dArray = dataObj.getJSONArray("d")
          val close = dArray.getDouble(0)
          val change = dArray.getDouble(1)
          val open = dArray.getDouble(2)
          val high = dArray.getDouble(3)
          val low = dArray.getDouble(4)
          val volume = dArray.optDouble(5, 0.0)
          
          val sdf = SimpleDateFormat("MMM dd, HH:mm:ss 'GMT'", Locale.getDefault())
          val formattedTime = sdf.format(Date())
          
          callback(Result.success(TradingViewQuote(close, change, open, high, low, volume, formattedTime)))
        } else {
          callback(Result.failure(Exception("No data returned for OANDA:XAUUSD")))
        }
      } catch (e: Exception) {
        callback(Result.failure(e))
      }
    }
  })
}

// Deep institutional Dark colors
private val DarkBg = Color(0xFF0B0E14)
private val CardBg = Color(0xFF141A24)
private val CardBorder = Color(0xFF222C3D)
private val GreenActive = Color(0xFF00E676)
private val BearishRed = Color(0xFFFF5252)
private val BullishGreen = Color(0xFF00FF88)
private val GoldAccent = Color(0xFFFFD700)
private val TextWhite = Color(0xFFF5F6F9)
private val TextMuted = Color(0xFF8F9CAE)

@Composable
fun GoldTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = darkColorScheme(
      primary = GoldAccent,
      background = DarkBg,
      surface = CardBg,
      onBackground = TextWhite,
      onSurface = TextWhite
    ),
    content = content
  )
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      GoldTheme {
        var showSplash by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
          delay(2500)
          showSplash = false
        }

        if (showSplash) {
          SplashScreen()
        } else {
          Scaffold(
            modifier = Modifier.fillMaxSize()
          ) { innerPadding ->
            GoldTradingDashboard(
              modifier = Modifier
                .fillMaxSize()
                .background(DarkBg)
                .padding(innerPadding)
            )
          }
        }
      }
    }
  }
}

@Composable
fun SplashScreen() {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(DarkBg),
    contentAlignment = Alignment.Center
  ) {
    Image(
      painter = painterResource(id = R.drawable.img_splash_screen_1782580980603),
      contentDescription = "Splash Screen Background",
      contentScale = ContentScale.Crop,
      modifier = Modifier.fillMaxSize()
    )
    
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            colors = listOf(
              Color(0x990B0E14),
              Color(0xF00B0E14)
            )
          )
        )
    )

    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier.padding(24.dp)
    ) {
      Card(
        modifier = Modifier
          .size(130.dp)
          .padding(8.dp),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.5.dp, GoldAccent),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1520)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
      ) {
        Image(
          painter = painterResource(id = R.drawable.img_app_icon_1782580958409),
          contentDescription = "HK Monogram",
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      Text(
        text = "GOLD TRADING ASSISTANT",
        color = GoldAccent,
        fontSize = 22.sp,
        fontWeight = FontWeight.ExtraBold,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 1.5.sp
      )

      Text(
        text = "INSTITUTIONAL SMC CONFLUENCE",
        color = TextMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 2.sp,
        modifier = Modifier.padding(top = 4.dp)
      )

      Spacer(modifier = Modifier.height(48.dp))

      CircularProgressIndicator(
        color = GoldAccent,
        strokeWidth = 3.dp,
        modifier = Modifier.size(32.dp)
      )
    }
  }
}

// Data models for Symmetrical Pivot Engine Simulation
data class SimCandle(
  val label: String,
  val high: Float,
  val low: Float,
  val open: Float,
  val close: Float
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GoldTradingDashboard(modifier: Modifier = Modifier) {
  val focusManager = LocalFocusManager.current

  // State: TradingView Live Data Ticker
  var tradingViewQuote by remember { mutableStateOf<TradingViewQuote?>(null) }
  var isFetchingTV by remember { mutableStateOf(false) }
  var tvFetchError by remember { mutableStateOf<String?>(null) }

  fun loadLiveTradingViewData() {
    isFetchingTV = true
    tvFetchError = null
    fetchTradingViewXauUsd { result ->
      isFetchingTV = false
      result.fold(
        onSuccess = { quote ->
          tradingViewQuote = quote
        },
        onFailure = { error ->
          tvFetchError = error.localizedMessage ?: "Failed to load live price"
        }
      )
    }
  }

  // State: Position Sizing Calculator
  var balanceInput by remember { mutableStateOf("10000") }
  var stopLossInput by remember { mutableStateOf("50") }
  var riskPercent by remember { mutableStateOf(1.0f) } // default 1%, capped at 2.0%

  // Parse inputs safely
  val balance = balanceInput.toDoubleOrNull() ?: 0.0
  val stopLossPips = stopLossInput.toDoubleOrNull() ?: 0.0
  val clampedRiskPercent = riskPercent.coerceIn(0.1f, 2.0f)

  // Sizing Math: Lot Size = (Account Balance * Risk %) / (Stop Loss Pips * 10.0)
  // Lot Size = (Balance * (Risk% / 100)) / (StopLossPips * 10)
  val riskAmount = balance * (clampedRiskPercent / 100.0)
  val lotSize = if (stopLossPips > 0) {
    riskAmount / (stopLossPips * 10.0)
  } else {
    0.0
  }

  // State: Confluence Matrix Verification Panel Checkpoints
  var checkpointHtfBias by remember { mutableStateOf(true) } // +3
  var checkpointLiquiditySweep by remember { mutableStateOf(true) } // +2
  var checkpointSmcConvergence by remember { mutableStateOf(false) } // +3
  var checkpointKillzone by remember { mutableStateOf(true) } // +1
  var displacementDeltaMode by remember { mutableStateOf(1) } // 0 = None (0), 1 = Standard (+1), 2 = High Displacement (+2)

  // Calculate Confluence Score
  val scoreHtf = if (checkpointHtfBias) 3 else 0
  val scoreLiq = if (checkpointLiquiditySweep) 2 else 0
  val scoreSmc = if (checkpointSmcConvergence) 3 else 0
  val scoreKillzone = if (checkpointKillzone) 1 else 0
  val scoreDisplacement = when (displacementDeltaMode) {
    1 -> 1
    2 -> 2
    else -> 0
  }
  val totalConfluenceScore = scoreHtf + scoreLiq + scoreSmc + scoreKillzone + scoreDisplacement

  // State: H1 Volatility Filter Simulator
  var candleOpenPrice by remember { mutableStateOf(2325.0f) }
  var candleClosePrice by remember { mutableStateOf(2362.0f) }
  val candleBodySize = abs(candleOpenPrice - candleClosePrice)
  val isVolatilityValid = candleBodySize >= 30.0f

  // State: Symmetrical Swing Pivot Engine Simulator (5 candles)
  var isTestingPivotHigh by remember { mutableStateOf(true) } // true for Pivot High, false for Pivot Low
  
  // Custom candle heights (Highs/Lows) for simulation
  var pivotCandles by remember {
    mutableStateOf(
      listOf(
        SimCandle("C1", 2345.0f, 2335.0f, 2338.0f, 2342.0f),
        SimCandle("C2", 2348.0f, 2338.0f, 2340.0f, 2346.0f),
        SimCandle("C3 (Center)", 2353.0f, 2341.0f, 2345.0f, 2351.0f),
        SimCandle("C4", 2347.0f, 2337.0f, 2346.0f, 2341.0f),
        SimCandle("C5", 2343.0f, 2333.0f, 2341.0f, 2336.0f)
      )
    )
  }

  // Calculate Symmetrical Swing Pivot Engine output
  // Swing High Pivot: C3 High > all others
  // Swing Low Pivot: C3 Low < all others
  val isPivotHighDetected = (
    pivotCandles[2].high > pivotCandles[0].high &&
    pivotCandles[2].high > pivotCandles[1].high &&
    pivotCandles[2].high > pivotCandles[3].high &&
    pivotCandles[2].high > pivotCandles[4].high
  )

  val isPivotLowDetected = (
    pivotCandles[2].low < pivotCandles[0].low &&
    pivotCandles[2].low < pivotCandles[1].low &&
    pivotCandles[2].low < pivotCandles[3].low &&
    pivotCandles[2].low < pivotCandles[4].low
  )

  // State: Trading Bias & Execution Settings
  var isMarketBullish by remember { mutableStateOf(true) } // true = Bullish (Buy), false = Bearish (Sell)
  var executionEntryPriceInput by remember { mutableStateOf("2350.0") }
  val executionEntryPrice = executionEntryPriceInput.toDoubleOrNull() ?: 2350.0

  LaunchedEffect(Unit) {
    loadLiveTradingViewData()
  }

  LaunchedEffect(tradingViewQuote) {
    tradingViewQuote?.let { quote ->
      executionEntryPriceInput = String.format(Locale.US, "%.2f", quote.price)
      candleClosePrice = quote.price.toFloat()
      candleOpenPrice = quote.open.toFloat()
      isMarketBullish = quote.changePercent >= 0.0
    }
  }

  // Stop Loss calculation (USD price offset based on SL Pips, where 1 pip = $0.10 in Gold)
  val usdOffset = stopLossPips * 0.10
  val calculatedSL = if (isMarketBullish) {
    executionEntryPrice - usdOffset
  } else {
    executionEntryPrice + usdOffset
  }

  // Take Profit calculation: standard 1:3 Risk-to-Reward ratio
  val calculatedTP = if (isMarketBullish) {
    executionEntryPrice + (usdOffset * 3.0)
  } else {
    executionEntryPrice - (usdOffset * 3.0)
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .background(DarkBg)
      .padding(16.dp)
  ) {
    // App Launcher Banner / Hero Banner
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .height(115.dp)
        .padding(bottom = 16.dp),
      shape = RoundedCornerShape(12.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
      border = BorderStroke(1.dp, Color(0xFF1E283A))
    ) {
      Box(modifier = Modifier.fillMaxSize()) {
        Image(
          painter = painterResource(id = R.drawable.img_hero_banner_1782580996033),
          contentDescription = "App Launcher Banner",
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
        // Elegant gradient overlay to blend into the app's dark interface
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.horizontalGradient(
                colors = listOf(
                  Color(0xCC0B0E14),
                  Color(0x220B0E14)
                )
              )
            )
        )
        // Elegant text overlay
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
          verticalArrangement = Arrangement.Center
        ) {
          Text(
            text = "HK QUANTUM ENGINE",
            color = GoldAccent,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
          )
          Text(
            text = "Institutional SMC Liquidity & Confluence Monitor",
            color = TextWhite,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    }

    // Top Header Section
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "XAUUSD SMC PRO",
            color = GoldAccent,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.testTag("app_title")
          )
          Spacer(modifier = Modifier.width(8.dp))
          // Glowing status indicator dot
          Box(
            modifier = Modifier
              .size(10.dp)
              .clip(CircleShape)
              .background(GreenActive)
          )
        }
        Text(
          text = "INSTITUTIONAL LIQUIDITY ENGINE",
          color = TextMuted,
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          fontFamily = FontFamily.Monospace,
          letterSpacing = 1.sp
        )
      }
      
      Surface(
        color = CardBg,
        border = BorderStroke(1.dp, CardBorder),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(start = 8.dp)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "H1 ENGINE ACTIVE",
            color = GreenActive,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
        }
      }
    }

    // TRADINGVIEW LIVE TICKER WIDGET
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 16.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1520)),
      border = BorderStroke(1.dp, Color(0xFF1E283A))
    ) {
      Column(
        modifier = Modifier.padding(16.dp)
      ) {
        // Ticker Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = "TradingView Feed",
              tint = GoldAccent,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "TRADINGVIEW REALTIME FEED",
              color = TextWhite,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
              letterSpacing = 1.sp
            )
          }

          // Refresh Button
          IconButton(
            onClick = { loadLiveTradingViewData() },
            modifier = Modifier.size(24.dp)
          ) {
            if (isFetchingTV) {
              CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                color = GoldAccent,
                strokeWidth = 2.dp
              )
            } else {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh",
                tint = TextMuted,
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Price Section
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Bottom
        ) {
          Column {
            Text(
              text = "XAUUSD (GOLD SPOT)",
              color = TextMuted,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(vertical = 4.dp)
            ) {
              Text(
                text = tradingViewQuote?.let { String.format(Locale.US, "$%,.3f", it.price) } ?: "Loading...",
                color = if (tradingViewQuote != null) GoldAccent else TextMuted,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
              )
              Text(
                text = " USD",
                color = TextMuted,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
              )
            }
          }

          // Change Indicator Chip
          tradingViewQuote?.let { quote ->
            val isPositive = quote.changePercent >= 0.0
            val chipBgColor = if (isPositive) Color(0xFF1B3F2A) else Color(0xFF452424)
            val chipTextColor = if (isPositive) BullishGreen else BearishRed
            
            Surface(
              color = chipBgColor,
              shape = RoundedCornerShape(6.dp),
              modifier = Modifier.padding(bottom = 6.dp)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = (if (isPositive) "▲ " else "▼ ") + String.format(Locale.US, "%+.2f%%", quote.changePercent),
                  color = chipTextColor,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
          }
        }

        // Daily High/Low Info Grid
        tradingViewQuote?.let { quote ->
          HorizontalDivider(
            color = Color(0xFF1E283A),
            modifier = Modifier.padding(vertical = 12.dp)
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            // Open Column
            Column(horizontalAlignment = Alignment.Start) {
              Text(text = "DAILY OPEN", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
              Text(
                text = String.format(Locale.US, "$%,.2f", quote.open),
                color = TextWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
              )
            }
            // High Column
            Column(horizontalAlignment = Alignment.Start) {
              Text(text = "DAILY HIGH", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
              Text(
                text = String.format(Locale.US, "$%,.2f", quote.high),
                color = BullishGreen,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
              )
            }
            // Low Column
            Column(horizontalAlignment = Alignment.Start) {
              Text(text = "DAILY LOW", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
              Text(
                text = String.format(Locale.US, "$%,.2f", quote.low),
                color = BearishRed,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }

        // Timestamp Footer
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = tvFetchError ?: "Source: TradingView REST API",
            color = if (tvFetchError != null) BearishRed else TextMuted,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
          )
          
          tradingViewQuote?.let { quote ->
            Text(
              text = "Refreshed: ${quote.timeString}",
              color = TextMuted,
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }
    }

    // SECTION 1: QUICK INPUTS & POSITION SIZE CALCULATOR
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 16.dp),
      colors = CardDefaults.cardColors(containerColor = CardBg),
      border = BorderStroke(1.dp, CardBorder)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text(
          text = "POSITION SIZING CALCULATOR",
          color = TextWhite,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          OutlinedTextField(
            value = balanceInput,
            onValueChange = { balanceInput = it },
            label = { Text("Account Balance ($)", color = TextMuted) },
            textStyle = TextStyle(color = TextWhite, fontFamily = FontFamily.Monospace),
            modifier = Modifier
              .weight(1f)
              .testTag("balance_input"),
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Number,
              imeAction = ImeAction.Next
            ),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = GoldAccent,
              unfocusedBorderColor = CardBorder,
              cursorColor = GoldAccent
            ),
            singleLine = true
          )

          OutlinedTextField(
            value = stopLossInput,
            onValueChange = { stopLossInput = it },
            label = { Text("Stop Loss (Pips)", color = TextMuted) },
            textStyle = TextStyle(color = TextWhite, fontFamily = FontFamily.Monospace),
            modifier = Modifier
              .weight(1f)
              .testTag("stop_loss_input"),
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Number,
              imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = GoldAccent,
              unfocusedBorderColor = CardBorder,
              cursorColor = GoldAccent
            ),
            singleLine = true
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Risk Percent Slider Row (Hard-capped at 2.0%)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Risk Percentage: ${String.format("%.2f", clampedRiskPercent)}%",
            color = TextWhite,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace
          )
          Text(
            text = "Max Risk (2.0% Cap)",
            color = BearishRed,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
        }

        Slider(
          value = riskPercent,
          onValueChange = { riskPercent = it },
          valueRange = 0.1f..2.0f,
          colors = SliderDefaults.colors(
            thumbColor = GoldAccent,
            activeTrackColor = GoldAccent,
            inactiveTrackColor = CardBorder
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("risk_slider")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Real-Time Position Sizing Output HUD
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E2633))
            .border(1.dp, Color(0xFF2C394F), RoundedCornerShape(8.dp))
            .padding(14.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "CALCULATED LOT SIZE",
                color = TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
              Text(
                text = String.format("%.2f Lots", lotSize),
                color = BullishGreen,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.testTag("calculated_lot_size")
              )
            }

            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = "TOTAL RISK EXPOSURE",
                color = TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
              Text(
                text = String.format("$%.2f USD", riskAmount),
                color = BearishRed,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }
      }
    }

    // SECTION 2: ADVANCED SMC FILTER LABORATORY (Volatility & Pivots)
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 16.dp),
      colors = CardDefaults.cardColors(containerColor = CardBg),
      border = BorderStroke(1.dp, CardBorder)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text(
          text = "SMC ENGINE FILTERS (H1 LABORATORY)",
          color = GoldAccent,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          modifier = Modifier.padding(bottom = 12.dp)
        )

        // 1. H1 Volatility Filter Container
        Text(
          text = "1. H1 VOLATILITY FILTER (Min Body: $30.00)",
          color = TextWhite,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          modifier = Modifier.padding(bottom = 6.dp)
        )

        Surface(
          color = Color(0xFF0F141C),
          shape = RoundedCornerShape(6.dp),
          border = BorderStroke(1.dp, CardBorder),
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "Candle Body Size (Open vs Close)",
                  color = TextMuted,
                  fontSize = 11.sp,
                  fontFamily = FontFamily.Monospace
                )
                Text(
                  text = String.format("$%.2f USD", candleBodySize),
                  color = TextWhite,
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace
                )
              }

              // Status indicator
              if (isVolatilityValid) {
                Surface(
                  color = Color(0xFF1B4D25),
                  shape = RoundedCornerShape(4.dp)
                ) {
                  Text(
                    text = "VALID BREAKER",
                    color = BullishGreen,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }
              } else {
                Surface(
                  color = Color(0xFF4C2727),
                  shape = RoundedCornerShape(4.dp)
                ) {
                  Text(
                    text = "MUTED / SKIP SETUP",
                    color = BearishRed,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dual sliders for Open and Close price to simulate
            Text(
              text = "Simulate Candle Close: $${String.format("%.1f", candleClosePrice)}",
              color = TextMuted,
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace
            )
            Slider(
              value = candleClosePrice,
              onValueChange = { candleClosePrice = it },
              valueRange = 2300.0f..2390.0f,
              colors = SliderDefaults.colors(
                thumbColor = GoldAccent,
                activeTrackColor = GoldAccent,
                inactiveTrackColor = CardBorder
              ),
              modifier = Modifier.fillMaxWidth()
            )
            
            Text(
              text = "Simulate Candle Open: $${String.format("%.1f", candleOpenPrice)}",
              color = TextMuted,
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace
            )
            Slider(
              value = candleOpenPrice,
              onValueChange = { candleOpenPrice = it },
              valueRange = 2300.0f..2390.0f,
              colors = SliderDefaults.colors(
                thumbColor = TextWhite,
                activeTrackColor = TextWhite,
                inactiveTrackColor = CardBorder
              ),
              modifier = Modifier.fillMaxWidth()
            )

            Text(
              text = "Requires body size >= $30.0 USD to count as an institutional Break of Structure (BOS) or Change of Character (CHoCH).",
              color = TextMuted,
              fontSize = 10.sp,
              lineHeight = 14.sp,
              style = TextStyle(textAlign = TextAlign.Start)
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Symmetrical Swing Pivot Engine Simulator
        Text(
          text = "2. SYMMETRICAL SWING PIVOT ENGINE",
          color = TextWhite,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          modifier = Modifier.padding(bottom = 6.dp)
        )

        Surface(
          color = Color(0xFF0F141C),
          shape = RoundedCornerShape(6.dp),
          border = BorderStroke(1.dp, CardBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            // Mode Select: Pivot High vs Pivot Low
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Button(
                onClick = {
                  isTestingPivotHigh = true
                  // Set clean preset for Pivot High
                  pivotCandles = listOf(
                    SimCandle("C1", 2345.0f, 2335.0f, 2338.0f, 2342.0f),
                    SimCandle("C2", 2348.0f, 2338.0f, 2340.0f, 2346.0f),
                    SimCandle("C3 (Center)", 2353.0f, 2341.0f, 2345.0f, 2351.0f),
                    SimCandle("C4", 2347.0f, 2337.0f, 2346.0f, 2341.0f),
                    SimCandle("C5", 2343.0f, 2333.0f, 2341.0f, 2336.0f)
                  )
                },
                colors = ButtonDefaults.buttonColors(
                  containerColor = if (isTestingPivotHigh) GoldAccent else Color(0xFF1E2633),
                  contentColor = if (isTestingPivotHigh) Color.Black else TextWhite
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 4.dp)
              ) {
                Text("Pivot High Testing", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }

              Button(
                onClick = {
                  isTestingPivotHigh = false
                  // Set clean preset for Pivot Low
                  pivotCandles = listOf(
                    SimCandle("C1", 2345.0f, 2335.0f, 2342.0f, 2338.0f),
                    SimCandle("C2", 2342.0f, 2332.0f, 2339.0f, 2334.0f),
                    SimCandle("C3 (Center)", 2338.0f, 2326.0f, 2334.0f, 2329.0f),
                    SimCandle("C4", 2341.0f, 2331.0f, 2332.0f, 2337.0f),
                    SimCandle("C5", 2344.0f, 2334.0f, 2336.0f, 2341.0f)
                  )
                },
                colors = ButtonDefaults.buttonColors(
                  containerColor = if (!isTestingPivotHigh) GoldAccent else Color(0xFF1E2633),
                  contentColor = if (!isTestingPivotHigh) Color.Black else TextWhite
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 4.dp)
              ) {
                Text("Pivot Low Testing", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }

            // Quick Presets
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("Presets:", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
              
              // Valid Preset
              Surface(
                color = Color(0xFF1E2633),
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.clickable {
                  if (isTestingPivotHigh) {
                    pivotCandles = listOf(
                      SimCandle("C1", 2345.0f, 2335.0f, 2338.0f, 2342.0f),
                      SimCandle("C2", 2348.0f, 2338.0f, 2340.0f, 2346.0f),
                      SimCandle("C3 (Center)", 2353.0f, 2341.0f, 2345.0f, 2351.0f), // Center highest
                      SimCandle("C4", 2347.0f, 2337.0f, 2346.0f, 2341.0f),
                      SimCandle("C5", 2343.0f, 2333.0f, 2341.0f, 2336.0f)
                    )
                  } else {
                    pivotCandles = listOf(
                      SimCandle("C1", 2345.0f, 2335.0f, 2342.0f, 2338.0f),
                      SimCandle("C2", 2342.0f, 2332.0f, 2339.0f, 2334.0f),
                      SimCandle("C3 (Center)", 2338.0f, 2326.0f, 2334.0f, 2329.0f), // Center lowest
                      SimCandle("C4", 2341.0f, 2331.0f, 2332.0f, 2337.0f),
                      SimCandle("C5", 2344.0f, 2334.0f, 2336.0f, 2341.0f)
                    )
                  }
                }
              ) {
                Text(
                  "Valid Pivot",
                  color = BullishGreen,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
              }

              // Invalid Preset (Trend)
              Surface(
                color = Color(0xFF1E2633),
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.clickable {
                  if (isTestingPivotHigh) {
                    pivotCandles = listOf(
                      SimCandle("C1", 2335.0f, 2325.0f, 2327.0f, 2332.0f),
                      SimCandle("C2", 2340.0f, 2330.0f, 2332.0f, 2338.0f),
                      SimCandle("C3 (Center)", 2345.0f, 2335.0f, 2336.0f, 2342.0f), // Ascending
                      SimCandle("C4", 2350.0f, 2340.0f, 2341.0f, 2348.0f),
                      SimCandle("C5", 2355.0f, 2345.0f, 2347.0f, 2352.0f)
                    )
                  } else {
                    pivotCandles = listOf(
                      SimCandle("C1", 2355.0f, 2345.0f, 2353.0f, 2348.0f),
                      SimCandle("C2", 2350.0f, 2340.0f, 2348.0f, 2342.0f),
                      SimCandle("C3 (Center)", 2345.0f, 2335.0f, 2342.0f, 2337.0f), // Descending
                      SimCandle("C4", 2340.0f, 2330.0f, 2338.0f, 2332.0f),
                      SimCandle("C5", 2335.0f, 2325.0f, 2331.0f, 2327.0f)
                    )
                  }
                }
              ) {
                Text(
                  "Invalid (Trend)",
                  color = BearishRed,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
              }
            }

            // Engine Output Status Header
            val enginePassed = if (isTestingPivotHigh) isPivotHighDetected else isPivotLowDetected
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "ENGINE DIAGNOSTIC:",
                color = TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
              
              if (enginePassed) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Pass",
                    tint = BullishGreen,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "VALID SWING ${if (isTestingPivotHigh) "HIGH" else "LOW"}",
                    color = BullishGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                  )
                }
              } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Fail",
                    tint = BearishRed,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "INVALID STRUCTURAL SYMMETRY",
                    color = BearishRed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                  )
                }
              }
            }

            // Interactive candle rendering row
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .background(Color(0xFF070B10))
                .padding(vertical = 8.dp),
              horizontalArrangement = Arrangement.SpaceEvenly
            ) {
              pivotCandles.forEachIndexed { index, candle ->
                val isCenter = index == 2
                Column(
                  modifier = Modifier
                    .fillMaxHeight()
                    .width(50.dp)
                    .background(
                      if (isCenter) Color(0xFF161E2E) else Color.Transparent,
                      shape = RoundedCornerShape(4.dp)
                    ),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(
                    text = candle.label,
                    color = if (isCenter) GoldAccent else TextMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                  )

                  // Graphic representation of candle
                  Box(
                    modifier = Modifier
                      .weight(1f)
                      .fillMaxWidth()
                      .padding(vertical = 2.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    // Draw wick & body using basic Composables
                    val isBullish = candle.close >= candle.open
                    val candleColor = if (isBullish) BullishGreen else BearishRed

                    // Compute relative coordinates for drawing
                    // Just basic stacked boxes for wicks and body to be extremely reliable
                    Column(
                      horizontalAlignment = Alignment.CenterHorizontally,
                      verticalArrangement = Arrangement.Center
                    ) {
                      // Top Wick
                      Box(
                        modifier = Modifier
                          .width(2.dp)
                          .height(14.dp)
                          .background(candleColor)
                      )
                      // Body
                      Box(
                        modifier = Modifier
                          .width(16.dp)
                          .height(30.dp)
                          .background(candleColor)
                          .border(
                            1.dp,
                            if (isCenter) GoldAccent else Color.Transparent,
                            RoundedCornerShape(2.dp)
                          )
                      )
                      // Bottom Wick
                      Box(
                        modifier = Modifier
                          .width(2.dp)
                          .height(14.dp)
                          .background(candleColor)
                      )
                    }
                  }

                  // Manual price tweaking control for simulation
                  val targetPrice = if (isTestingPivotHigh) candle.high else candle.low
                  Text(
                    text = String.format("%.1f", targetPrice),
                    color = TextWhite,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                  )

                  // Increment / Decrement buttons
                  Row(
                    modifier = Modifier.padding(bottom = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                  ) {
                    Box(
                      modifier = Modifier
                        .size(16.dp)
                        .background(Color(0xFF222C3D), RoundedCornerShape(2.dp))
                        .clickable {
                          // Decrement
                          pivotCandles = pivotCandles.mapIndexed { idx, item ->
                            if (idx == index) {
                              if (isTestingPivotHigh) {
                                item.copy(high = item.high - 1.0f)
                              } else {
                                item.copy(low = item.low - 1.0f)
                              }
                            } else {
                              item
                            }
                          }
                        },
                      contentAlignment = Alignment.Center
                    ) {
                      Text("-", color = TextWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Box(
                      modifier = Modifier
                        .size(16.dp)
                        .background(Color(0xFF222C3D), RoundedCornerShape(2.dp))
                        .clickable {
                          // Increment
                          pivotCandles = pivotCandles.mapIndexed { idx, item ->
                            if (idx == index) {
                              if (isTestingPivotHigh) {
                                item.copy(high = item.high + 1.0f)
                              } else {
                                item.copy(low = item.low + 1.0f)
                              }
                            } else {
                              item
                            }
                          }
                        },
                      contentAlignment = Alignment.Center
                    ) {
                      Text("+", color = TextWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "A valid symmetrical pivot requires the center candle (C3) to be a relative peak (High or Low) compared to both its left and right neighbors.",
              color = TextMuted,
              fontSize = 10.sp,
              lineHeight = 14.sp
            )
          }
        }
      }
    }

    // SECTION 3: MATRIX VERIFICATION PANEL
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 16.dp),
      colors = CardDefaults.cardColors(containerColor = CardBg),
      border = BorderStroke(1.dp, CardBorder)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "CONFLUENCE CHECKPOINT MATRIX",
            color = TextWhite,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )

          // Circular Score Badge
          Surface(
            color = if (totalConfluenceScore >= 7) Color(0xFF1B4D25) else Color(0xFF1E2633),
            border = BorderStroke(1.dp, if (totalConfluenceScore >= 7) BullishGreen else CardBorder),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text(
              text = "SCORE: $totalConfluenceScore / 10",
              color = if (totalConfluenceScore >= 7) BullishGreen else GoldAccent,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
              modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 4.dp)
                .testTag("confluence_score")
            )
          }
        }

        Text(
          text = "Tap checkpoints to configure active market confluences. Minimum 7/10 score required for signal activation.",
          color = TextMuted,
          fontSize = 11.sp,
          modifier = Modifier.padding(vertical = 8.dp)
        )

        HorizontalDivider(color = CardBorder, modifier = Modifier.padding(vertical = 8.dp))

        // Matrix checkpoints list
        CheckpointRow(
          title = "HTF Bias Alignment",
          points = 3,
          description = "Daily & H4 market structure are aligned",
          checked = checkpointHtfBias,
          onCheckedChange = { checkpointHtfBias = it }
        )

        CheckpointRow(
          title = "Liquidity Sweep Match",
          points = 2,
          description = "Previous day high/low or old highs swept",
          checked = checkpointLiquiditySweep,
          onCheckedChange = { checkpointLiquiditySweep = it }
        )

        CheckpointRow(
          title = "SMC Convergence Area",
          points = 3,
          description = "Order Block, Fair Value Gap (FVG), or Golden Pocket OB",
          checked = checkpointSmcConvergence,
          onCheckedChange = { checkpointSmcConvergence = it }
        )

        CheckpointRow(
          title = "Killzone Window Active",
          points = 1,
          description = "London or NY session volatility window open",
          checked = checkpointKillzone,
          onCheckedChange = { checkpointKillzone = it }
        )

        // Displacement Delta: Special dynamic checkpoint (+1 or +2)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "Displacement Delta",
                color = TextWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.width(6.dp))
              Surface(
                color = Color(0xFF1E2633),
                shape = RoundedCornerShape(4.dp)
              ) {
                Text(
                  text = "+$scoreDisplacement pts",
                  color = GoldAccent,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
            Text(
              text = "Impulsive expansion and candle body closure",
              color = TextMuted,
              fontSize = 11.sp
            )
          }

          // Options: None, Standard, High
          Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            listOf(0, 1, 2).forEach { mode ->
              val label = when (mode) {
                0 -> "None"
                1 -> "Std (+1)"
                else -> "High (+2)"
              }
              Surface(
                color = if (displacementDeltaMode == mode) GoldAccent else Color(0xFF1E2633),
                border = BorderStroke(1.dp, if (displacementDeltaMode == mode) GoldAccent else CardBorder),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                  .clickable { displacementDeltaMode = mode }
              ) {
                Text(
                  text = label,
                  color = if (displacementDeltaMode == mode) Color.Black else TextWhite,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                )
              }
            }
          }
        }
      }
    }

    // SECTION 4: EXECUTION SIGNAL CENTER
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 24.dp),
      colors = CardDefaults.cardColors(
        containerColor = if (totalConfluenceScore >= 7) {
          if (isMarketBullish) Color(0xFF0F2618) else Color(0xFF2E1313)
        } else {
          Color(0xFF161B22)
        }
      ),
      border = BorderStroke(
        1.dp,
        if (totalConfluenceScore >= 7) {
          if (isMarketBullish) BullishGreen else BearishRed
        } else {
          CardBorder
        }
      )
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "EXECUTION SIGNAL CENTER",
            color = TextWhite,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )

          // Bias Selector Bullish/Bearish
          Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Surface(
              color = if (isMarketBullish) BullishGreen else Color(0xFF1E2633),
              shape = RoundedCornerShape(4.dp),
              modifier = Modifier.clickable { isMarketBullish = true }
            ) {
              Text(
                text = "BULLISH",
                color = if (isMarketBullish) Color.Black else TextWhite,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }

            Surface(
              color = if (!isMarketBullish) BearishRed else Color(0xFF1E2633),
              shape = RoundedCornerShape(4.dp),
              modifier = Modifier.clickable { isMarketBullish = false }
            ) {
              Text(
                text = "BEARISH",
                color = if (!isMarketBullish) Color.Black else TextWhite,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // State-driven Display
        if (totalConfluenceScore >= 7) {
          // Confluence scorer threshold passed! Generate signal card.
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Active Signal",
                tint = if (isMarketBullish) BullishGreen else BearishRed,
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (isMarketBullish) "BULLISH HIGH-CONFLUENCE BUY LIMIT" else "BEARISH HIGH-CONFLUENCE SELL LIMIT",
                color = if (isMarketBullish) BullishGreen else BearishRed,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.testTag("signal_status")
              )
            }

            Text(
              text = "SMC Confluence verified at $totalConfluenceScore/10. Matrix execution parameters loaded successfully.",
              color = TextMuted,
              fontSize = 11.sp,
              modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            // Live editable Entry Price input
            OutlinedTextField(
              value = executionEntryPriceInput,
              onValueChange = { executionEntryPriceInput = it },
              label = { Text("Reference Entry Price ($)", color = TextMuted) },
              textStyle = TextStyle(color = TextWhite, fontFamily = FontFamily.Monospace),
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
              keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
              ),
              keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isMarketBullish) BullishGreen else BearishRed,
                unfocusedBorderColor = CardBorder,
                cursorColor = GoldAccent
              ),
              singleLine = true
            )

            // Dynamic math verification output values box
            Surface(
              color = Color(0xFF0F141C),
              shape = RoundedCornerShape(8.dp),
              border = BorderStroke(1.dp, CardBorder),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                // Parameter: Entry
                TradeParamRow(
                  label = "ENTRY TARGET",
                  value = String.format("$%.2f", executionEntryPrice),
                  color = TextWhite
                )
                // Parameter: Stop Loss
                TradeParamRow(
                  label = "STOP LOSS (SL)",
                  value = String.format("$%.2f", calculatedSL),
                  color = BearishRed
                )
                // Parameter: Take Profit
                TradeParamRow(
                  label = "TAKE PROFIT (TP) [1:3 RR]",
                  value = String.format("$%.2f", calculatedTP),
                  color = BullishGreen
                )
                // Lot size allocation
                TradeParamRow(
                  label = "ALLOCATION SIZE",
                  value = String.format("%.2f Lots", lotSize),
                  color = GoldAccent
                )
              }
            }
          }
        } else {
          // Unmet conditions: Display "SYSTEM ACTIVE: Monitoring/Skipped (No High-Confluence Setup)"
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = "Monitoring",
              tint = TextMuted,
              modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "SYSTEM ACTIVE: Monitoring/Skipped",
              color = TextWhite,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
              modifier = Modifier.testTag("signal_status")
            )
            Text(
              text = "No High-Confluence Setup. Current Score: $totalConfluenceScore/10.",
              color = TextMuted,
              fontSize = 12.sp,
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(top = 4.dp)
            )
            Text(
              text = "Check additional SMC matrix boxes to reach the minimum 7/10 scoring threshold.",
              color = TextMuted,
              fontSize = 11.sp,
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
fun CheckpointRow(
  title: String,
  points: Int,
  description: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 6.dp)
      .clickable { onCheckedChange(!checked) },
    verticalAlignment = Alignment.CenterVertically
  ) {
    Checkbox(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors = CheckboxDefaults.colors(
        checkedColor = GoldAccent,
        uncheckedColor = CardBorder,
        checkmarkColor = Color.Black
      )
    )

    Spacer(modifier = Modifier.width(8.dp))

    Column(modifier = Modifier.weight(1f)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = title,
          color = TextWhite,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(6.dp))
        Surface(
          color = Color(0xFF1E2633),
          shape = RoundedCornerShape(4.dp)
        ) {
          Text(
            text = "+$points pts",
            color = GoldAccent,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }
      }
      Text(
        text = description,
        color = TextMuted,
        fontSize = 11.sp
      )
    }
  }
}

@Composable
fun TradeParamRow(
  label: String,
  value: String,
  color: Color
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      color = TextMuted,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      fontFamily = FontFamily.Monospace
    )
    Text(
      text = value,
      color = color,
      fontSize = 14.sp,
      fontWeight = FontWeight.ExtraBold,
      fontFamily = FontFamily.Monospace
    )
  }
}

// Keep a small compatible Greeting component for screenshot/unit tests
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Box(modifier = modifier) {
    Text(text = "Hello $name!", color = TextWhite)
  }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  GoldTheme {
    Greeting("Android")
  }
}
