package com.playzone.admin.ui.screens.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
fun AdminDashboardTab(
    modifier: Modifier,
    bookingViewModel: AdminBookingViewModel,
    onGoToQueue: () -> Unit,
    onGoToActive: () -> Unit,
    onGoToPsManagement: () -> Unit
) {
    val allBookings by bookingViewModel.allBookings.collectAsStateWithLifecycle()
    val psUnits by bookingViewModel.psUnits.collectAsStateWithLifecycle()

    // Hanya dari booking yang CONFIRMED atau COMPLETED
    val completedOrConfirmedBookings = allBookings.filter {
        it.status == BookingStatus.CONFIRMED || it.status == BookingStatus.COMPLETED
    }

    // Total pendapatan PS (dari totalPrice)
    val totalPsRevenue = completedOrConfirmedBookings.sumOf { it.totalPrice }

    // Total pendapatan snack (dari snackTotal)
    val totalSnackRevenue = completedOrConfirmedBookings.sumOf { it.snackTotal }

    // Grand total
    val totalRevenue = completedOrConfirmedBookings.sumOf {
        it.grandTotal.takeIf { g -> g > 0 } ?: it.totalPrice
    }

    val totalBookings = allBookings.size
    val pendingCount = allBookings.count { it.status == BookingStatus.PENDING }
    val activeCount = allBookings.count { it.status == BookingStatus.ACTIVE }
    val completedToday = allBookings.count { it.status == BookingStatus.COMPLETED }

    val currency = NumberFormat.getNumberInstance(Locale("in", "ID"))

    var selectedBooking by remember { mutableStateOf<Booking?>(null) }
    var showCompletedDialog by remember { mutableStateOf(false) }
    var showRevenueDialog by remember { mutableStateOf(false) }  // ← dialog pendapatan

    val completedBookings = allBookings.filter { it.status == BookingStatus.COMPLETED }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(listOf(PurplePrimary, PurpleLight)),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Text("📊 Ringkasan Hari Ini", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                    Text("Kelola semua aktivitas Playzone", color = Color.White.copy(0.8f), fontSize = 13.sp)
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // ← Total Pendapatan sekarang bisa diklik — tampilkan breakdown
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.AttachMoney,
                    label = "Total Pendapatan",
                    value = "Rp ${currency.format(totalRevenue)}",
                    color = GoldAccent,
                    onClick = { showRevenueDialog = true }
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.BookOnline,
                    label = "Total Booking",
                    value = "$totalBookings",
                    color = PurpleLight
                )
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.HourglassTop,
                    label = "Menunggu Konfirmasi",
                    value = "$pendingCount",
                    color = GoldAccent,
                    onClick = onGoToQueue
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.PlayCircle,
                    label = "Sesi Berjalan",
                    value = "$activeCount",
                    color = GreenSuccess,
                    onClick = onGoToActive
                )
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.CheckCircle,
                    label = "Sesi Selesai",
                    value = "$completedToday",
                    color = BlueInfo,
                    onClick = { showCompletedDialog = true }
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Gamepad,
                    label = "Unit Tersedia",
                    value = "${psUnits.count { it.isAvailable }} / ${psUnits.size}",
                    color = PurplePrimary,
                    onClick = onGoToPsManagement
                )
            }
        }

        item { Text("Aksi Cepat", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp) }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onGoToQueue,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldAccent)
                ) {
                    Icon(Icons.Default.Queue, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Antrian", fontSize = 13.sp)
                }
                OutlinedButton(
                    onClick = onGoToActive,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenSuccess)
                ) {
                    Icon(Icons.Default.PlayCircle, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Sesi Aktif", fontSize = 13.sp)
                }
            }
        }

        item { Text("Booking Terbaru", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp) }
        items(allBookings.take(10)) { booking ->
            MiniBookingCard(booking = booking, onClick = { selectedBooking = booking })
        }
    }

    // ── Dialog Detail Booking ──────────────────────────────────────
    selectedBooking?.let { booking ->
        BookingDetailDialog(booking = booking, onDismiss = { selectedBooking = null })
    }

    // ── Dialog Sesi Selesai ────────────────────────────────────────
    if (showCompletedDialog) {
        CompletedSessionsDialog(
            bookings = completedBookings,
            onDismiss = { showCompletedDialog = false },
            onBookingClick = { selectedBooking = it; showCompletedDialog = false }
        )
    }

    // ── Dialog Breakdown Pendapatan (PS vs Snack) ──────────────────
    if (showRevenueDialog) {
        RevenueBreakdownDialog(
            totalRevenue = totalRevenue,
            psRevenue = totalPsRevenue,
            snackRevenue = totalSnackRevenue,
            completedOrConfirmedBookings = completedOrConfirmedBookings,
            onDismiss = { showRevenueDialog = false }
        )
    }
}

// ── Revenue Breakdown Dialog ───────────────────────────────────────
@Composable
fun RevenueBreakdownDialog(
    totalRevenue: Int,
    psRevenue: Int,
    snackRevenue: Int,
    completedOrConfirmedBookings: List<Booking>,
    onDismiss: () -> Unit
) {
    val currency = NumberFormat.getNumberInstance(Locale("in", "ID"))
    val codRevenue = completedOrConfirmedBookings
        .filter { it.paymentMethod == PaymentMethod.COD }
        .sumOf { it.grandTotal.takeIf { g -> g > 0 } ?: it.totalPrice }
    val transferRevenue = completedOrConfirmedBookings
        .filter { it.paymentMethod == PaymentMethod.TRANSFER }
        .sumOf { it.grandTotal.takeIf { g -> g > 0 } ?: it.totalPrice }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AttachMoney, null, tint = GoldAccent, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Text("Rincian Pendapatan", fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Total besar
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = GoldAccent.copy(0.12f))
                ) {
                    Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Semua Pendapatan", color = TextSecondary, fontSize = 12.sp)
                        Text(
                            "Rp ${currency.format(totalRevenue)}",
                            fontWeight = FontWeight.Black,
                            color = GoldAccent,
                            fontSize = 22.sp
                        )
                        Text(
                            "${completedOrConfirmedBookings.size} sesi",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                HorizontalDivider(color = CardDark)
                Text("Sumber Pendapatan", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)

                // PS Revenue
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Gamepad, null, tint = PurpleLight, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Column {
                            Text("Sewa PS", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("Harga sewa unit", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                    Text("Rp ${currency.format(psRevenue)}", color = PurpleLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                // Snack Revenue
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Fastfood, null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Column {
                            Text("Penjualan Snack", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("Makanan & minuman", color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                    Text("Rp ${currency.format(snackRevenue)}", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                HorizontalDivider(color = CardDark)
                Text("Metode Pembayaran", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)

                DetailRow("💵 Bayar di Tempat (COD)", "Rp ${currency.format(codRevenue)}")
                DetailRow("🏦 Transfer", "Rp ${currency.format(transferRevenue)}")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Tutup", color = PurpleLight) }
        }
    )
}

// ── Stat Card ──────────────────────────────────────────────────────
@Composable
fun StatCard(
    modifier: Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        onClick = onClick ?: {}
    ) {
        Column(Modifier.padding(14.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(6.dp))
            Text(value, fontWeight = FontWeight.Black, color = color, fontSize = 18.sp)
            Text(label, color = TextSecondary, fontSize = 11.sp)
            if (onClick != null) {
                Text("Tap untuk lihat →", color = color.copy(0.6f), fontSize = 10.sp)
            }
        }
    }
}

// ── Mini Booking Card ──────────────────────────────────────────────
@Composable
fun MiniBookingCard(booking: Booking, onClick: () -> Unit = {}) {
    val (statusColor, statusLabel) = when (booking.status) {
        BookingStatus.PENDING   -> GoldAccent to "PENDING"
        BookingStatus.CONFIRMED -> GreenSuccess to "CONFIRMED"
        BookingStatus.ACTIVE    -> PurpleLight to "ACTIVE"
        BookingStatus.COMPLETED -> TextSecondary to "SELESAI"
        BookingStatus.CANCELLED -> RedCancel to "BATAL"
    }
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        onClick = onClick
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(booking.psUnitName, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 13.sp)
                Text(
                    "${booking.userName.ifBlank { "Tamu" }} • ${booking.bookingDate} ${booking.startTime}",
                    color = TextSecondary, fontSize = 11.sp
                )
                Text(
                    "Durasi: ${booking.durationHours} jam • ${if (booking.paymentMethod == PaymentMethod.COD) "Bayar di tempat" else "Transfer"}",
                    color = TextSecondary.copy(0.7f), fontSize = 10.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Surface(shape = RoundedCornerShape(6.dp), color = statusColor.copy(0.15f)) {
                    Text(
                        statusLabel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(4.dp))
                Icon(Icons.Default.ChevronRight, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
            }
        }
    }
}

// ── Booking Detail Dialog ──────────────────────────────────────────
@Composable
fun BookingDetailDialog(booking: Booking, onDismiss: () -> Unit) {
    val currency = NumberFormat.getNumberInstance(Locale("in", "ID"))
    val (statusColor, statusLabel) = when (booking.status) {
        BookingStatus.PENDING   -> GoldAccent to "PENDING"
        BookingStatus.CONFIRMED -> GreenSuccess to "DIKONFIRMASI"
        BookingStatus.ACTIVE    -> PurpleLight to "AKTIF"
        BookingStatus.COMPLETED -> BlueInfo to "SELESAI"
        BookingStatus.CANCELLED -> RedCancel to "DIBATALKAN"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Receipt, null, tint = PurpleLight, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Detail Booking", fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(shape = RoundedCornerShape(8.dp), color = statusColor.copy(0.15f)) {
                    Text(
                        statusLabel,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = statusColor, fontWeight = FontWeight.Bold, fontSize = 12.sp
                    )
                }
                HorizontalDivider(color = CardDark)
                DetailRow("🎮 Unit", booking.psUnitName)
                DetailRow("👤 Pelanggan", booking.userName.ifBlank { "-" })
                DetailRow("📞 Telepon", booking.userPhone.ifBlank { "-" })
                DetailRow("📅 Tanggal", booking.bookingDate)
                DetailRow("⏰ Jam Mulai", booking.startTime)
                DetailRow("⏱️ Durasi", "${booking.durationHours} jam")
                DetailRow("💳 Pembayaran", if (booking.paymentMethod == PaymentMethod.COD) "Bayar di tempat" else "Transfer")
                HorizontalDivider(color = CardDark)
                DetailRow("🎮 Harga Sewa PS", "Rp ${currency.format(booking.totalPrice)}")
                if (booking.snackTotal > 0) {
                    DetailRow("🍟 Pendapatan Snack", "Rp ${currency.format(booking.snackTotal)}", highlight = false, color = GoldAccent)
                }
                val total = if (booking.grandTotal > 0) booking.grandTotal else booking.totalPrice
                DetailRow("🧾 Grand Total", "Rp ${currency.format(total)}", highlight = true)
                if (booking.adminNotes.isNotBlank()) {
                    DetailRow("📝 Catatan", booking.adminNotes)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Tutup", color = PurpleLight) }
        }
    )
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    highlight: Boolean = false,
    color: Color? = null
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Text(
            value,
            color = color ?: (if (highlight) GoldAccent else TextPrimary),
            fontSize = 13.sp,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1.2f)
        )
    }
}

// ── Completed Sessions Dialog ──────────────────────────────────────
@Composable
fun CompletedSessionsDialog(
    bookings: List<Booking>,
    onDismiss: () -> Unit,
    onBookingClick: (Booking) -> Unit
) {
    val currency = NumberFormat.getNumberInstance(Locale("in", "ID"))
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, null, tint = BlueInfo, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Sesi Selesai (${bookings.size})", fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        },
        text = {
            if (bookings.isEmpty()) {
                Text("Belum ada sesi selesai", color = TextSecondary)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    bookings.forEach { booking ->
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = CardDark),
                            onClick = { onBookingClick(booking) }
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(booking.psUnitName, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 13.sp)
                                    Text("${booking.userName.ifBlank { "Tamu" }} • ${booking.bookingDate}", color = TextSecondary, fontSize = 11.sp)
                                    if (booking.snackTotal > 0) {
                                        Text("🍟 Snack: Rp ${currency.format(booking.snackTotal)}", color = GoldAccent, fontSize = 10.sp)
                                    }
                                }
                                val total = if (booking.grandTotal > 0) booking.grandTotal else booking.totalPrice
                                Text("Rp ${currency.format(total)}", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Tutup", color = PurpleLight) }
        }
    )
}
