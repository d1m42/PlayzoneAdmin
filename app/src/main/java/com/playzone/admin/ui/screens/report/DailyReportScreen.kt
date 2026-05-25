package com.playzone.admin.ui.screens.report

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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DailyReportScreen(
    modifier: Modifier,
    bookingViewModel: AdminBookingViewModel
) {
    val reportBookings by bookingViewModel.reportBookings.collectAsStateWithLifecycle()
    val uiState by bookingViewModel.uiState.collectAsStateWithLifecycle()

    var selectedDate by remember {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        mutableStateOf(sdf.format(Date()))
    }

    LaunchedEffect(Unit) { bookingViewModel.loadDailyReport(selectedDate) }

    val currency = NumberFormat.getNumberInstance(Locale("in", "ID"))

    val completedBookings = reportBookings.filter { it.status == BookingStatus.COMPLETED }

    // ── Rincian pendapatan ─────────────────────────────────────────
    val psRevenue = completedBookings.sumOf { it.totalPrice }
    val snackRevenue = completedBookings.sumOf { it.snackTotal }
    val totalRevenue = completedBookings.sumOf {
        it.grandTotal.takeIf { g -> g > 0 } ?: it.totalPrice
    }
    val totalSessions = completedBookings.size
    val codRevenue = completedBookings.filter { it.paymentMethod == PaymentMethod.COD }
        .sumOf { it.grandTotal.takeIf { g -> g > 0 } ?: it.totalPrice }
    val transferRevenue = completedBookings.filter { it.paymentMethod == PaymentMethod.TRANSFER }
        .sumOf { it.grandTotal.takeIf { g -> g > 0 } ?: it.totalPrice }
    val cancelledCount = reportBookings.count { it.status == BookingStatus.CANCELLED }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Pilih tanggal
        item {
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardDark)) {
                Column(Modifier.padding(14.dp)) {
                    Text("Pilih Tanggal Laporan", color = TextSecondary, fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = selectedDate,
                            onValueChange = { selectedDate = it },
                            label = { Text("Tanggal (dd/MM/yyyy)") },
                            leadingIcon = { Icon(Icons.Default.CalendarMonth, null, tint = PurpleLight) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PurpleLight,
                                focusedLabelColor = PurpleLight
                            )
                        )
                        Button(
                            onClick = { bookingViewModel.loadDailyReport(selectedDate) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                            enabled = !uiState.isLoading
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(color = TextPrimary, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Search, null)
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val today = sdf.format(Date())
                        val yesterday = sdf.format(Date(System.currentTimeMillis() - 86400000))
                        listOf("Hari ini" to today, "Kemarin" to yesterday).forEach { (label, date) ->
                            FilterChip(
                                selected = selectedDate == date,
                                onClick = { selectedDate = date; bookingViewModel.loadDailyReport(date) },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PurplePrimary,
                                    selectedLabelColor = TextPrimary
                                )
                            )
                        }
                    }
                }
            }
        }

        // Kartu ringkasan pendapatan
        item {
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = CardDark)) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Assessment, null, tint = GoldAccent, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Ringkasan $selectedDate", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                    }
                    Spacer(Modifier.height(14.dp))

                    // Total
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Total Pendapatan", color = TextSecondary, fontSize = 12.sp)
                            Text(
                                "Rp ${currency.format(totalRevenue)}",
                                color = GoldAccent, fontWeight = FontWeight.Black, fontSize = 22.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Sesi Selesai", color = TextSecondary, fontSize = 12.sp)
                            Text("$totalSessions sesi", color = GreenSuccess, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }

                    HorizontalDivider(Modifier.padding(vertical = 10.dp), color = SurfaceDark)

                    // ── Sumber pendapatan ──────────────────────────────────
                    Text("Sumber Pendapatan", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // PS Card
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = PurplePrimary.copy(0.15f))
                        ) {
                            Column(Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Gamepad, null, tint = PurpleLight, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Sewa PS", color = TextSecondary, fontSize = 11.sp)
                                }
                                Text(
                                    "Rp ${currency.format(psRevenue)}",
                                    color = PurpleLight, fontWeight = FontWeight.Bold, fontSize = 14.sp
                                )
                            }
                        }
                        // Snack Card
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = GoldAccent.copy(0.12f))
                        ) {
                            Column(Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Fastfood, null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Snack", color = TextSecondary, fontSize = 11.sp)
                                }
                                Text(
                                    "Rp ${currency.format(snackRevenue)}",
                                    color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 14.sp
                                )
                            }
                        }
                    }

                    HorizontalDivider(Modifier.padding(vertical = 10.dp), color = SurfaceDark)

                    // Pembayaran
                    Text("Metode Pembayaran", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    ReportRow("💵 COD (Bayar di Tempat)", "Rp ${currency.format(codRevenue)}", OrangeWarning)
                    ReportRow("🏦 Transfer", "Rp ${currency.format(transferRevenue)}", BlueInfo)
                    ReportRow("❌ Dibatalkan", "$cancelledCount booking", RedCancel)
                    ReportRow("📊 Total Semua Booking", "${reportBookings.size} booking", TextSecondary)
                }
            }
        }

        // Detail per unit PS
        if (completedBookings.isNotEmpty()) {
            item {
                Text("Detail per Unit PS", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
            }
            val byUnit = completedBookings.groupBy { it.psUnitName }
            items(byUnit.entries.toList()) { (unitName, bList) ->
                val unitPs = bList.sumOf { it.totalPrice }
                val unitSnack = bList.sumOf { it.snackTotal }
                val unitTotal = bList.sumOf { it.grandTotal.takeIf { g -> g > 0 } ?: it.totalPrice }
                Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = CardDark)) {
                    Column(Modifier.fillMaxWidth().padding(14.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Gamepad, null, tint = PurpleLight, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(unitName, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                    Text("${bList.size} sesi", color = TextSecondary, fontSize = 12.sp)
                                }
                            }
                            Text("Rp ${currency.format(unitTotal)}", color = GoldAccent, fontWeight = FontWeight.Bold)
                        }
                        if (unitSnack > 0) {
                            Spacer(Modifier.height(4.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("  🎮 Sewa PS", color = TextSecondary, fontSize = 11.sp)
                                Text("Rp ${currency.format(unitPs)}", color = PurpleLight, fontSize = 11.sp)
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("  🍟 Snack", color = TextSecondary, fontSize = 11.sp)
                                Text("Rp ${currency.format(unitSnack)}", color = GoldAccent, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Semua booking hari itu
        if (reportBookings.isNotEmpty()) {
            item {
                Spacer(Modifier.height(4.dp))
                Text("Semua Booking (${reportBookings.size})", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
            }
            items(reportBookings, key = { it.id }) { booking ->
                ReportBookingCard(booking = booking, currency = currency)
            }
        }

        if (reportBookings.isEmpty() && !uiState.isLoading) {
            item {
                Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📭", fontSize = 32.sp)
                        Text("Tidak ada data untuk tanggal ini", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun ReportRow(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextSecondary, fontSize = 13.sp)
        Text(value, color = color, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ReportBookingCard(booking: Booking, currency: NumberFormat) {
    val (statusColor, statusLabel) = when (booking.status) {
        BookingStatus.COMPLETED -> GreenSuccess to "Selesai"
        BookingStatus.CANCELLED -> RedCancel to "Batal"
        BookingStatus.ACTIVE    -> PurpleLight to "Aktif"
        BookingStatus.CONFIRMED -> GoldAccent to "Konfirmasi"
        BookingStatus.PENDING   -> OrangeWarning to "Pending"
    }
    val revenue = booking.grandTotal.takeIf { it > 0 } ?: booking.totalPrice

    Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = CardDark)) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(booking.psUnitName, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text("${booking.userName} • ${booking.startTime} (${booking.durationHours}h)", color = TextSecondary, fontSize = 11.sp)
                    Text(booking.paymentMethod.name, color = TextSecondary.copy(0.7f), fontSize = 10.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Surface(shape = RoundedCornerShape(5.dp), color = statusColor.copy(0.15f)) {
                        Text(statusLabel, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Rp ${currency.format(revenue)}",
                        color = if (booking.status == BookingStatus.CANCELLED) TextSecondary else GoldAccent,
                        fontWeight = FontWeight.Bold, fontSize = 12.sp
                    )
                }
            }
            // Breakdown PS & snack jika ada
            if (booking.snackTotal > 0 && booking.status == BookingStatus.COMPLETED) {
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("🎮 PS: Rp ${currency.format(booking.totalPrice)}", color = PurpleLight, fontSize = 11.sp)
                    Text("🍟 Snack: Rp ${currency.format(booking.snackTotal)}", color = GoldAccent, fontSize = 11.sp)
                }
            }
        }
    }
}
