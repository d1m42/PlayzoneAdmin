package com.playzone.admin.ui.screens.queue

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.playzone.admin.data.model.BookingStatus
import com.playzone.admin.data.model.PaymentMethod
import com.playzone.admin.ui.theme.*
import com.playzone.admin.ui.viewmodel.AdminBookingViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun BookingQueueScreen(
    modifier: Modifier,
    bookingViewModel: AdminBookingViewModel,
    onGoCheckout: (String) -> Unit
) {
    val pendingBookings by bookingViewModel.pendingBookings.collectAsStateWithLifecycle()
    val confirmedBookings by bookingViewModel.confirmedBookings.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }

    Column(modifier = modifier.fillMaxSize()) {
        // Sub-tab: Pending vs Confirmed
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = SurfaceDark,
            contentColor = PurpleLight
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Pending (${pendingBookings.size})") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Dikonfirmasi (${confirmedBookings.size})") }
            )
        }

        when (selectedTab) {
            0 -> QueueList(
                bookings = pendingBookings,
                emptyMessage = "Tidak ada booking menunggu 🎉",
                bookingViewModel = bookingViewModel,
                onGoCheckout = onGoCheckout,
                showConfirmAction = true
            )
            1 -> QueueList(
                bookings = confirmedBookings,
                emptyMessage = "Tidak ada booking terkonfirmasi",
                bookingViewModel = bookingViewModel,
                onGoCheckout = onGoCheckout,
                showCheckInAction = true
            )
        }
    }
}

@Composable
private fun QueueList(
    bookings: List<Booking>,
    emptyMessage: String,
    bookingViewModel: AdminBookingViewModel,
    onGoCheckout: (String) -> Unit,
    showConfirmAction: Boolean = false,
    showCheckInAction: Boolean = false
) {
    if (bookings.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.CheckCircle,
                    null,
                    tint = GreenSuccess,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(emptyMessage, color = TextSecondary)
            }
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(bookings, key = { it.id }) { booking ->
            BookingQueueCard(
                booking = booking,
                bookingViewModel = bookingViewModel,
                onGoCheckout = onGoCheckout,
                showConfirmAction = showConfirmAction,
                showCheckInAction = showCheckInAction
            )
        }
    }
}

@Composable
fun BookingQueueCard(
    booking: Booking,
    bookingViewModel: AdminBookingViewModel,
    onGoCheckout: (String) -> Unit,
    showConfirmAction: Boolean = false,
    showCheckInAction: Boolean = false
) {
    val currency = NumberFormat.getNumberInstance(Locale("in", "ID"))
    var showCancelDialog by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column(Modifier.padding(16.dp)) {
            // Header
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
                        fontSize = 15.sp
                    )
                    Text(
                        "${booking.psUnitType} • ${booking.durationHours} jam",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = (if (booking.status == BookingStatus.PENDING) GoldAccent else GreenSuccess).copy(0.15f)
                ) {
                    Text(
                        booking.status.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = if (booking.status == BookingStatus.PENDING) GoldAccent else GreenSuccess,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 10.dp), color = SurfaceDark)

            // Customer info
            InfoRow(Icons.Default.Person, "Pelanggan", booking.userName)
            InfoRow(Icons.Default.Phone, "Telepon", booking.userPhone)
            InfoRow(Icons.Default.CalendarMonth, "Jadwal", "${booking.bookingDate} pukul ${booking.startTime}")
            InfoRow(Icons.Default.AccessTime, "Durasi", "${booking.durationHours} jam")

            // Payment method
            val (payIcon, payLabel, payColor) = when (booking.paymentMethod) {
                PaymentMethod.COD -> Triple(Icons.Default.Money, "COD - Bayar di Tempat", OrangeWarning)
                PaymentMethod.TRANSFER -> Triple(Icons.Default.AccountBalance, "Transfer Bank", BlueInfo)
            }
            Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(payIcon, null, tint = payColor, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Pembayaran: ", color = TextSecondary, fontSize = 12.sp)
                Text(payLabel, color = payColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            Row(
                Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("ID: ${booking.id}", color = TextSecondary.copy(0.5f), fontSize = 10.sp)
                Text(
                    "Rp ${currency.format(booking.totalPrice)}",
                    color = GoldAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            // Action buttons
            if (showConfirmAction) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { showCancelDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RedCancel)
                    ) {
                        Icon(Icons.Default.Cancel, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Tolak", fontSize = 13.sp)
                    }
                    Button(
                        onClick = { bookingViewModel.confirmBooking(booking.id) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Konfirmasi", fontSize = 13.sp)
                    }
                }
            }

            if (showCheckInAction) {
                Button(
                    onClick = { bookingViewModel.checkInBooking(booking.id) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    Icon(Icons.Default.Login, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Check-In Pelanggan → Mulai Sesi", fontSize = 13.sp)
                }
            }
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            icon = { Icon(Icons.Default.Warning, null, tint = RedCancel) },
            title = { Text("Batalkan Booking?") },
            text = {
                Text("Booking ${booking.psUnitName} untuk ${booking.userName} akan dibatalkan dan pelanggan akan mendapat notifikasi.")
            },
            confirmButton = {
                TextButton(onClick = {
                    bookingViewModel.cancelBooking(booking.id)
                    showCancelDialog = false
                }) { Text("Ya, Batalkan", color = RedCancel) }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("Tidak") }
            },
            containerColor = SurfaceDark
        )
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        Modifier.padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = TextSecondary, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(6.dp))
        Text("$label: ", color = TextSecondary, fontSize = 12.sp)
        Text(value, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}
