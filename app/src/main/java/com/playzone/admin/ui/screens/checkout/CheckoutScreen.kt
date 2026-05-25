package com.playzone.admin.ui.screens.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.playzone.admin.data.model.Booking
import com.playzone.admin.data.model.BookingStatus
import com.playzone.admin.ui.theme.*
import com.playzone.admin.ui.viewmodel.AdminBookingViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    bookingId: String,
    onBack: () -> Unit,
    onDone: () -> Unit,
    bookingViewModel: AdminBookingViewModel = hiltViewModel()
) {
    val allBookings by bookingViewModel.allBookings.collectAsStateWithLifecycle()
    val snackOrders by bookingViewModel.checkoutSnackOrders.collectAsStateWithLifecycle()
    val uiState by bookingViewModel.uiState.collectAsStateWithLifecycle()

    val booking = allBookings.find { it.id == bookingId }
    var adminNotes by remember { mutableStateOf("") }
    var showReceiptDialog by remember { mutableStateOf(false) }
    var checkoutDone by remember { mutableStateOf(false) }

    // Load snack orders for this booking
    LaunchedEffect(bookingId) {
        bookingViewModel.loadSnackOrdersForCheckout(bookingId)
    }

    // Navigate after successful checkout
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null && checkoutDone) {
            onDone()
        }
    }

    val snackTotal = snackOrders.sumOf { it.totalPrice }
    val grandTotal = (booking?.totalPrice ?: 0) + snackTotal

    val currency = NumberFormat.getNumberInstance(Locale("in", "ID"))
    val timeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout & Struk", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        if (booking == null) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PurpleLight)
            }
            return@Scaffold
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Booking info card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Gamepad, null, tint = PurpleLight, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Info Booking", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                    }
                    Spacer(Modifier.height(10.dp))
                    ReceiptRow("ID Booking", booking.id)
                    ReceiptRow("Pelanggan", "${booking.userName} • ${booking.userPhone}")
                    ReceiptRow("Unit", "${booking.psUnitName} (${booking.psUnitType})")
                    ReceiptRow("Tanggal", booking.bookingDate)
                    ReceiptRow("Jam Mulai", booking.startTime)
                    ReceiptRow("Durasi", "${booking.durationHours} jam")
                    if (booking.checkinAt > 0) {
                        ReceiptRow("Check-in", timeFormat.format(Date(booking.checkinAt)))
                    }
                    ReceiptRow("Check-out", timeFormat.format(Date()))
                }
            }

            // Biaya PS
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Receipt, null, tint = GoldAccent, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Rincian Tagihan", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                    }
                    Spacer(Modifier.height(10.dp))

                    // PS cost
                    BillRow(
                        label = "Sewa PS (${booking.durationHours} jam)",
                        amount = booking.totalPrice,
                        currency = currency
                    )

                    // Snack items
                    if (snackOrders.isNotEmpty()) {
                        Spacer(Modifier.height(6.dp))
                        Text("Pesanan Snack:", color = TextSecondary, fontSize = 12.sp)
                        snackOrders.forEach { order ->
                            order.items.forEach { item ->
                                BillRow(
                                    label = "  ${item.emoji} ${item.name} x${item.qty}",
                                    amount = item.price * item.qty,
                                    currency = currency,
                                    color = TextSecondary
                                )
                            }
                        }
                        BillRow("Total Snack", snackTotal, currency, color = OrangeWarning)
                    }

                    HorizontalDivider(Modifier.padding(vertical = 8.dp), color = SurfaceDark)

                    // Grand total
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("TOTAL", fontWeight = FontWeight.Black, color = TextPrimary, fontSize = 16.sp)
                        Text(
                            "Rp ${currency.format(grandTotal)}",
                            fontWeight = FontWeight.Black,
                            color = GoldAccent,
                            fontSize = 20.sp
                        )
                    }

                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Metode: ${booking.paymentMethod.name}",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            // Admin notes
            OutlinedTextField(
                value = adminNotes,
                onValueChange = { adminNotes = it },
                label = { Text("Catatan Admin (opsional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PurpleLight,
                    focusedLabelColor = PurpleLight
                )
            )

            // Receipt preview button
            OutlinedButton(
                onClick = { showReceiptDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PurpleLight)
            ) {
                Icon(Icons.Default.Preview, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Preview Struk")
            }

            // Checkout button
            Button(
                onClick = {
                    checkoutDone = true
                    bookingViewModel.checkOutBooking(
                        bookingId = booking.id,
                        snackTotal = snackTotal,
                        grandTotal = grandTotal,
                        adminNotes = adminNotes
                    )
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isLoading && booking.status == BookingStatus.ACTIVE,
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Selesaikan & Checkout", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (booking.status == BookingStatus.COMPLETED) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = GreenSuccess.copy(0.12f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null, tint = GreenSuccess, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Sesi ini sudah selesai / di-checkout", color = GreenSuccess, fontSize = 13.sp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // Receipt preview dialog
    if (showReceiptDialog && booking != null) {
        AlertDialog(
            onDismissRequest = { showReceiptDialog = false },
            title = { Text("🧾 Struk Pembayaran", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    Text("PLAYZONE RENTAL PS", fontWeight = FontWeight.Black, fontSize = 16.sp, color = PurpleLight)
                    Text("─────────────────────────────", color = TextSecondary, fontSize = 11.sp)
                    Text("ID     : ${booking.id}", fontSize = 12.sp)
                    Text("Pelanggan: ${booking.userName}", fontSize = 12.sp)
                    Text("Unit   : ${booking.psUnitName}", fontSize = 12.sp)
                    Text("Tgl    : ${booking.bookingDate}", fontSize = 12.sp)
                    Text("Mulai  : ${booking.startTime}", fontSize = 12.sp)
                    Text("Durasi : ${booking.durationHours} jam", fontSize = 12.sp)
                    Text("─────────────────────────────", color = TextSecondary, fontSize = 11.sp)
                    Text("Sewa PS: Rp ${currency.format(booking.totalPrice)}", fontSize = 12.sp)
                    if (snackTotal > 0) {
                        Text("Snack  : Rp ${currency.format(snackTotal)}", fontSize = 12.sp)
                    }
                    Text("─────────────────────────────", color = TextSecondary, fontSize = 11.sp)
                    Text(
                        "TOTAL  : Rp ${currency.format(grandTotal)}",
                        fontWeight = FontWeight.Black,
                        color = GoldAccent,
                        fontSize = 14.sp
                    )
                    Text("Bayar  : ${booking.paymentMethod.name}", fontSize = 12.sp)
                    Text("─────────────────────────────", color = TextSecondary, fontSize = 11.sp)
                    Text("Waktu checkout: ${sdf.format(Date())}", fontSize = 11.sp, color = TextSecondary)
                    Text("Terima kasih sudah bermain!", fontSize = 12.sp, color = PurpleLight)
                }
            },
            confirmButton = {
                TextButton(onClick = { showReceiptDialog = false }) {
                    Text("Tutup")
                }
            },
            containerColor = SurfaceDark
        )
    }
}

@Composable
private fun ReceiptRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextSecondary, fontSize = 12.sp)
        Text(value, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun BillRow(
    label: String,
    amount: Int,
    currency: NumberFormat,
    color: androidx.compose.ui.graphics.Color = TextPrimary
) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = color, fontSize = 13.sp)
        Text("Rp ${currency.format(amount)}", color = color, fontSize = 13.sp)
    }
}
