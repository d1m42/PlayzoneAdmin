package com.playzone.admin.ui.screens.history

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
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
fun BookingHistoryScreen(
    modifier: Modifier,
    bookingViewModel: AdminBookingViewModel
) {
    val allBookings by bookingViewModel.allBookings.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("Semua") }
    var showDetailDialog by remember { mutableStateOf<Booking?>(null) }

    val statusFilters = listOf("Semua", "Pending", "Konfirmasi", "Aktif", "Selesai", "Batal")

    // Filter by status + search query
    val filtered = allBookings
        .filter { booking ->
            when (selectedStatus) {
                "Pending"   -> booking.status == BookingStatus.PENDING
                "Konfirmasi"-> booking.status == BookingStatus.CONFIRMED
                "Aktif"     -> booking.status == BookingStatus.ACTIVE
                "Selesai"   -> booking.status == BookingStatus.COMPLETED
                "Batal"     -> booking.status == BookingStatus.CANCELLED
                else        -> true
            }
        }
        .filter { booking ->
            if (searchQuery.isBlank()) true
            else {
                booking.userName.contains(searchQuery, ignoreCase = true) ||
                booking.psUnitName.contains(searchQuery, ignoreCase = true) ||
                booking.bookingDate.contains(searchQuery) ||
                booking.id.contains(searchQuery, ignoreCase = true) ||
                booking.userPhone.contains(searchQuery)
            }
        }

    Column(modifier = modifier.fillMaxSize()) {

        // ── Header stats bar ──────────────────────────────────────
        Row(
            Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(PurplePrimary.copy(0.3f), SurfaceDark)))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HistoryStatChip(
                label = "Total",
                count = allBookings.size,
                color = PurpleLight
            )
            HistoryStatChip(
                label = "Selesai",
                count = allBookings.count { it.status == BookingStatus.COMPLETED },
                color = GreenSuccess
            )
            HistoryStatChip(
                label = "Aktif",
                count = allBookings.count { it.status == BookingStatus.ACTIVE },
                color = GoldAccent
            )
            HistoryStatChip(
                label = "Batal",
                count = allBookings.count { it.status == BookingStatus.CANCELLED },
                color = RedCancel
            )
        }

        // ── Search bar ────────────────────────────────────────────
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Cari nama, unit PS, tanggal, ID...", fontSize = 13.sp) },
            leadingIcon = {
                Icon(Icons.Default.Search, null, tint = TextSecondary)
            },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, null, tint = TextSecondary)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PurpleLight,
                unfocusedBorderColor = CardDark,
                focusedContainerColor = SurfaceDark,
                unfocusedContainerColor = SurfaceDark,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = PurpleLight
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
        )

        // ── Filter chips ──────────────────────────────────────────
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            statusFilters.forEach { status ->
                val count = when (status) {
                    "Pending"   -> allBookings.count { it.status == BookingStatus.PENDING }
                    "Konfirmasi"-> allBookings.count { it.status == BookingStatus.CONFIRMED }
                    "Aktif"     -> allBookings.count { it.status == BookingStatus.ACTIVE }
                    "Selesai"   -> allBookings.count { it.status == BookingStatus.COMPLETED }
                    "Batal"     -> allBookings.count { it.status == BookingStatus.CANCELLED }
                    else        -> allBookings.size
                }
                FilterChip(
                    selected = selectedStatus == status,
                    onClick = { selectedStatus = status },
                    label = {
                        Text(
                            if (count > 0 && status != "Semua") "$status ($count)" else status,
                            fontSize = 10.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PurplePrimary,
                        selectedLabelColor = TextPrimary,
                        containerColor = CardDark,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedStatus == status,
                        selectedBorderColor = PurpleLight,
                        borderColor = SurfaceDark
                    )
                )
            }
        }

        // ── Content ───────────────────────────────────────────────
        when {
            filtered.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            if (searchQuery.isNotBlank()) Icons.Default.SearchOff else Icons.Default.BookmarkBorder,
                            null,
                            tint = TextSecondary,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            if (searchQuery.isNotBlank()) "Tidak ada hasil untuk \"$searchQuery\""
                            else "Belum ada booking $selectedStatus",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            else -> {
                // Result count
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${filtered.size} hasil ditemukan",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    if (searchQuery.isNotBlank() || selectedStatus != "Semua") {
                        TextButton(onClick = { searchQuery = ""; selectedStatus = "Semua" }) {
                            Text("Reset Filter", color = PurpleLight, fontSize = 11.sp)
                        }
                    }
                }

                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filtered, key = { it.id }) { booking ->
                        HistoryBookingCard(
                            booking = booking,
                            bookingViewModel = bookingViewModel,
                            onClick = { showDetailDialog = booking }
                        )
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }

    // ── Detail dialog ─────────────────────────────────────────────
    showDetailDialog?.let { booking ->
        HistoryDetailDialog(
            booking = booking,
            bookingViewModel = bookingViewModel,
            onDismiss = { showDetailDialog = null }
        )
    }
}

@Composable
private fun HistoryStatChip(label: String, count: Int, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            count.toString(),
            color = color,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp
        )
        Text(label, color = TextSecondary, fontSize = 10.sp)
    }
}

@Composable
fun HistoryBookingCard(
    booking: Booking,
    bookingViewModel: AdminBookingViewModel,
    onClick: () -> Unit
) {
    val currency = NumberFormat.getNumberInstance(Locale("in", "ID"))
    val (statusColor, statusLabel, statusIcon) = when (booking.status) {
        BookingStatus.PENDING   -> Triple(GoldAccent, "PENDING", Icons.Default.HourglassEmpty)
        BookingStatus.CONFIRMED -> Triple(GreenSuccess, "KONFIRMASI", Icons.Default.CheckCircle)
        BookingStatus.ACTIVE    -> Triple(PurpleLight, "AKTIF", Icons.Default.PlayCircle)
        BookingStatus.COMPLETED -> Triple(BlueInfo, "SELESAI", Icons.Default.Done)
        BookingStatus.CANCELLED -> Triple(RedCancel, "BATAL", Icons.Default.Cancel)
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        onClick = onClick
    ) {
        Column(Modifier.padding(14.dp)) {
            // Header
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        Modifier
                            .size(40.dp)
                            .background(statusColor.copy(0.12f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(statusIcon, null, tint = statusColor, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            booking.psUnitName,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            booking.userName.ifBlank { "Tamu" },
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
                Surface(shape = RoundedCornerShape(6.dp), color = statusColor.copy(0.15f)) {
                    Text(
                        statusLabel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 8.dp), color = SurfaceDark)

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    HistoryInfoRow(Icons.Default.CalendarToday, "${booking.bookingDate} ${booking.startTime}")
                    HistoryInfoRow(Icons.Default.AccessTime, "${booking.durationHours} jam")
                    HistoryInfoRow(
                        Icons.Default.Phone,
                        booking.userPhone.ifBlank { "-" }
                    )
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    val total = if (booking.grandTotal > 0) booking.grandTotal else booking.totalPrice
                    Text(
                        "Rp ${currency.format(total)}",
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    if (booking.snackTotal > 0) {
                        Text(
                            "🍟 +Rp ${currency.format(booking.snackTotal)}",
                            color = GoldAccent.copy(0.7f),
                            fontSize = 10.sp
                        )
                    }
                    Text(
                        if (booking.paymentMethod == PaymentMethod.COD) "💵 COD" else "🏦 Transfer",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }

            // Quick action untuk PENDING
            if (booking.status == BookingStatus.PENDING) {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { bookingViewModel.cancelBooking(booking.id) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RedCancel),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        Text("Tolak", fontSize = 12.sp)
                    }
                    Button(
                        onClick = { bookingViewModel.confirmBooking(booking.id) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        Text("Konfirmasi", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = TextSecondary, modifier = Modifier.size(13.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, color = TextSecondary, fontSize = 11.sp)
    }
}

@Composable
fun HistoryDetailDialog(
    booking: Booking,
    bookingViewModel: AdminBookingViewModel,
    onDismiss: () -> Unit
) {
    val currency = NumberFormat.getNumberInstance(Locale("in", "ID"))
    val (statusColor, statusLabel) = when (booking.status) {
        BookingStatus.PENDING   -> GoldAccent to "MENUNGGU KONFIRMASI"
        BookingStatus.CONFIRMED -> GreenSuccess to "DIKONFIRMASI"
        BookingStatus.ACTIVE    -> PurpleLight to "SESI AKTIF"
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
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(0.15f)
                ) {
                    Text(
                        statusLabel,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                HorizontalDivider(color = CardDark)

                HistoryDetailRow("🎮 Unit PS", booking.psUnitName)
                HistoryDetailRow("🏷️ Tipe", booking.psUnitType)
                HistoryDetailRow("👤 Pelanggan", booking.userName.ifBlank { "-" })
                HistoryDetailRow("📞 Telepon", booking.userPhone.ifBlank { "-" })
                HistoryDetailRow("📅 Tanggal", booking.bookingDate)
                HistoryDetailRow("⏰ Jam Mulai", booking.startTime)
                HistoryDetailRow("⏱️ Durasi", "${booking.durationHours} jam")
                HistoryDetailRow(
                    "💳 Pembayaran",
                    if (booking.paymentMethod == PaymentMethod.COD) "Bayar di Tempat (COD)" else "Transfer Bank"
                )
                HistoryDetailRow("🆔 Booking ID", booking.id)

                HorizontalDivider(color = CardDark)

                HistoryDetailRow("🎮 Harga Sewa PS", "Rp ${currency.format(booking.totalPrice)}")
                if (booking.snackTotal > 0) {
                    HistoryDetailRow("🍟 Total Snack", "Rp ${currency.format(booking.snackTotal)}")
                }
                val grandTotal = if (booking.grandTotal > 0) booking.grandTotal else booking.totalPrice
                HistoryDetailRow("💰 Grand Total", "Rp ${currency.format(grandTotal)}", highlight = true)

                if (booking.adminNotes.isNotBlank()) {
                    HistoryDetailRow("📝 Catatan Admin", booking.adminNotes)
                }

                // Quick actions
                if (booking.status == BookingStatus.PENDING) {
                    HorizontalDivider(color = CardDark)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { bookingViewModel.cancelBooking(booking.id); onDismiss() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RedCancel)
                        ) { Text("Tolak", fontSize = 12.sp) }
                        Button(
                            onClick = { bookingViewModel.confirmBooking(booking.id); onDismiss() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess)
                        ) { Text("Konfirmasi", fontSize = 12.sp) }
                    }
                }
                if (booking.status == BookingStatus.CONFIRMED) {
                    HorizontalDivider(color = CardDark)
                    Button(
                        onClick = { bookingViewModel.checkInBooking(booking.id); onDismiss() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                    ) {
                        Icon(Icons.Default.Login, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Check-In Pelanggan")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Tutup", color = PurpleLight) }
        }
    )
}

@Composable
private fun HistoryDetailRow(label: String, value: String, highlight: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(
            value,
            color = if (highlight) GoldAccent else TextPrimary,
            fontSize = 12.sp,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1.2f),
            maxLines = 2
        )
    }
}
