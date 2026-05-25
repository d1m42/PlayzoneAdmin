package com.playzone.admin.ui.screens.active

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.playzone.admin.data.model.Booking
import com.playzone.admin.ui.theme.*
import com.playzone.admin.ui.viewmodel.AdminBookingViewModel
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ActiveSessionScreen(
    modifier: Modifier,
    bookingViewModel: AdminBookingViewModel,
    onGoCheckout: (String) -> Unit
) {
    val activeBookings by bookingViewModel.activeBookings.collectAsStateWithLifecycle()

    if (activeBookings.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.VideogameAssetOff,
                    null,
                    tint = TextSecondary,
                    modifier = Modifier.size(60.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text("Tidak ada sesi aktif saat ini", color = TextSecondary, fontSize = 15.sp)
                Text("Konfirmasi booking terlebih dahulu", color = TextSecondary.copy(0.7f), fontSize = 13.sp)
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = GreenSuccess.copy(0.12f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(10.dp).background(GreenSuccess, CircleShape)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${activeBookings.size} Sesi Sedang Berjalan",
                        color = GreenSuccess,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        items(activeBookings, key = { it.id }) { booking ->
            ActiveSessionCard(
                booking = booking,
                onGoCheckout = onGoCheckout
            )
        }
    }
}

@Composable
fun ActiveSessionCard(
    booking: Booking,
    onGoCheckout: (String) -> Unit
) {
    val currency = NumberFormat.getNumberInstance(Locale("in", "ID"))

    // Timer countdown
    var remainingMs by remember(booking.id) {
        mutableLongStateOf(booking.endTimeMillis - System.currentTimeMillis())
    }
    LaunchedEffect(booking.id) {
        while (remainingMs > 0) {
            delay(1000L)
            remainingMs = booking.endTimeMillis - System.currentTimeMillis()
        }
    }

    val isOvertime = remainingMs <= 0
    val totalSecs = if (isOvertime) {
        (System.currentTimeMillis() - booking.endTimeMillis) / 1000
    } else {
        remainingMs / 1000
    }
    val hours = totalSecs / 3600
    val minutes = (totalSecs % 3600) / 60
    val seconds = totalSecs % 60
    val timerText = "%02d:%02d:%02d".format(hours, minutes, seconds)
    val timerColor = when {
        isOvertime -> RedCancel
        remainingMs < 15 * 60 * 1000 -> OrangeWarning
        else -> GreenSuccess
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        booking.psUnitName,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 16.sp
                    )
                    Text(booking.psUnitType, color = TextSecondary, fontSize = 12.sp)
                }
                // Timer badge
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        if (isOvertime) "OVERTIME" else "SISA WAKTU",
                        color = timerColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        timerText,
                        color = timerColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp
                    )
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 10.dp), color = SurfaceDark)

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(Modifier.weight(1f)) {
                    Text("👤 Pelanggan", color = TextSecondary, fontSize = 11.sp)
                    Text(booking.userName, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text(booking.userPhone, color = TextSecondary, fontSize = 11.sp)
                }
                Column(Modifier.weight(1f)) {
                    Text("📅 Jadwal", color = TextSecondary, fontSize = 11.sp)
                    Text(booking.bookingDate, color = TextPrimary, fontSize = 13.sp)
                    Text("${booking.startTime} • ${booking.durationHours}h", color = TextSecondary, fontSize = 11.sp)
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("ID: ${booking.id}", color = TextSecondary.copy(0.5f), fontSize = 10.sp)
                Text(
                    "Rp ${currency.format(booking.totalPrice)}",
                    color = GoldAccent,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(12.dp))

            // Overtime warning
            if (isOvertime) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = RedCancel.copy(0.15f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Warning, null,
                            tint = RedCancel, modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Waktu habis! Segera lakukan checkout.",
                            color = RedCancel, fontSize = 12.sp
                        )
                    }
                }
            }

            // Checkout button
            Button(
                onClick = { onGoCheckout(booking.id) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isOvertime) RedCancel else PurplePrimary
                )
            ) {
                Icon(Icons.Default.Receipt, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    if (isOvertime) "Checkout Sekarang!" else "Checkout & Cetak Struk",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
