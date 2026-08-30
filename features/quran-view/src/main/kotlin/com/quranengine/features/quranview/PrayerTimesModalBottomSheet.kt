package com.quranengine.features.quranview

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTimeFilled
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quranengine.ui.theme.QuranTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerTimesModalBottomSheet(
    onDismissRequest: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    val prefs = remember {
        context.getSharedPreferences("FlutterSharedPreferences", Context.MODE_PRIVATE)
    }

    val locationName = remember {
        prefs.getString("flutter.mizan_prayer_times_location", null)?.takeIf { it.isNotEmpty() }
            ?: prefs.getString("mizan_prayer_times_location", null)?.takeIf { it.isNotEmpty() }
            ?: "Your Location"
    }

    val nextPrayerName = remember {
        prefs.getString("flutter.mizan_prayer_times_next_name", null)
            ?: prefs.getString("mizan_prayer_times_next_name", null)
            ?: "Prayer"
    }

    val nextPrayerTimeStr = remember {
        prefs.getString("flutter.mizan_prayer_times_next_time", null)
            ?: prefs.getString("mizan_prayer_times_next_time", null)
    }

    var nowMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(1000)
        }
    }

    fun parseIsoToMillis(iso: String?): Long? {
        if (iso.isNullOrEmpty()) return null
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
                timeZone = TimeZone.getDefault()
            }
            format.parse(iso.substringBefore("."))?.time
        } catch (_: Exception) {
            null
        }
    }

    fun formatTime(iso: String?): String {
        val millis = parseIsoToMillis(iso) ?: return "--:--"
        val outFormat = SimpleDateFormat("h:mm a", Locale.US)
        return outFormat.format(Date(millis))
    }

    val nextPrayerMillis = parseIsoToMillis(nextPrayerTimeStr)
    val countdownText = remember(nowMillis, nextPrayerMillis) {
        if (nextPrayerMillis == null) "Soon"
        else {
            val diffSecs = ((nextPrayerMillis - nowMillis) / 1000).coerceAtLeast(0)
            val hours = diffSecs / 3600
            val mins = (diffSecs % 3600) / 60
            val secs = diffSecs % 60
            if (hours > 0) String.format(Locale.US, "%dh %02dm %02ds", hours, mins, secs)
            else String.format(Locale.US, "%dm %02ds", mins, secs)
        }
    }

    val fajrStr = prefs.getString("flutter.mizan_prayer_times_fajr", null) ?: prefs.getString("mizan_prayer_times_fajr", null)
    val sunriseStr = prefs.getString("flutter.mizan_prayer_times_sunrise", null) ?: prefs.getString("mizan_prayer_times_sunrise", null)
    val dhuhrStr = prefs.getString("flutter.mizan_prayer_times_dhuhr", null) ?: prefs.getString("mizan_prayer_times_dhuhr", null)
    val asrStr = prefs.getString("flutter.mizan_prayer_times_asr", null) ?: prefs.getString("mizan_prayer_times_asr", null)
    val maghribStr = prefs.getString("flutter.mizan_prayer_times_maghrib", null) ?: prefs.getString("mizan_prayer_times_maghrib", null)
    val ishaStr = prefs.getString("flutter.mizan_prayer_times_isha", null) ?: prefs.getString("mizan_prayer_times_isha", null)

    val prayers = listOf(
        "Fajr" to formatTime(fajrStr),
        "Sunrise" to formatTime(sunriseStr),
        "Dhuhr" to formatTime(dhuhrStr),
        "Asr" to formatTime(asrStr),
        "Maghrib" to formatTime(maghribStr),
        "Isha" to formatTime(ishaStr),
    )

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color(0xFF121614),
        contentColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Location and Date Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = QuranTheme.mizanGold,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = locationName,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Today's Schedule",
                        color = Color(0xFFF1D4BC),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    val dateFormatted = remember(nowMillis) {
                        SimpleDateFormat("EEEE, MMMM d", Locale.US).format(Date(nowMillis))
                    }
                    Text(
                        text = dateFormatted,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                    )
                }
            }

            // Hero Countdown Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF1A6B4A).copy(alpha = 0.85f),
                                Color(0xFF0F442E).copy(alpha = 0.95f),
                            ),
                        ),
                        shape = RoundedCornerShape(16.dp),
                    )
                    .border(
                        width = 1.dp,
                        color = QuranTheme.mizanGold.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(16.dp),
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTimeFilled,
                    contentDescription = "Clock",
                    tint = QuranTheme.mizanGold,
                    modifier = Modifier.size(26.dp),
                )
                Column {
                    Text(
                        text = "NEXT PRAYER: " + nextPrayerName.uppercase(Locale.US),
                        color = QuranTheme.mizanGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "in " + countdownText,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            // 6 Prayer Rows
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                prayers.forEach { (name, time) ->
                    val isActive = name.equals(nextPrayerName, ignoreCase = true)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (isActive) QuranTheme.mizanGold.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.04f),
                                shape = RoundedCornerShape(12.dp),
                            )
                            .border(
                                width = if (isActive) 1.5.dp else 1.dp,
                                color = if (isActive) QuranTheme.mizanGold else Color.White.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(12.dp),
                            )
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            if (isActive) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(QuranTheme.mizanGold, CircleShape)
                                )
                            }
                            Text(
                                text = name,
                                color = if (isActive) QuranTheme.mizanGold else Color.White,
                                fontSize = 14.sp,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                            )
                        }
                        Text(
                            text = time,
                            color = if (isActive) QuranTheme.mizanGold else Color.White.copy(alpha = 0.75f),
                            fontSize = 14.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                        )
                    }
                }
            }
        }
    }
}
