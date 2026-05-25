package com.playzone.admin.ui.screens.dashboard

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.playzone.admin.service.AdminFCMService
import com.playzone.admin.ui.screens.active.ActiveSessionScreen
import com.playzone.admin.ui.screens.history.BookingHistoryScreen
import com.playzone.admin.ui.screens.management.PsManagementScreen
import com.playzone.admin.ui.screens.queue.BookingQueueScreen
import com.playzone.admin.ui.screens.report.DailyReportScreen
import com.playzone.admin.ui.screens.snack.SnackManagementScreen
import com.playzone.admin.ui.theme.*
import com.playzone.admin.ui.viewmodel.AdminAuthViewModel
import com.playzone.admin.ui.viewmodel.AdminBookingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMainScreen(
    onLogout: () -> Unit,
    onGoCheckout: (String) -> Unit,
    authViewModel: AdminAuthViewModel = hiltViewModel(),
    bookingViewModel: AdminBookingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val userProfile by authViewModel.userProfile.collectAsStateWithLifecycle()
    val uiState by bookingViewModel.uiState.collectAsStateWithLifecycle()
    val pendingBookings by bookingViewModel.pendingBookings.collectAsStateWithLifecycle()
    val activeSnackOrders by bookingViewModel.activeSnackOrders.collectAsStateWithLifecycle()
    val allBookings by bookingViewModel.allBookings.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showPsManagement by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Izin notifikasi Android 13+
    val notifPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Deteksi booking PENDING baru → notifikasi real-time
    var prevPendingCount by remember { mutableIntStateOf(-1) }
    LaunchedEffect(pendingBookings) {
        val count = pendingBookings.size
        if (prevPendingCount >= 0 && count > prevPendingCount) {
            val newest = pendingBookings.first()
            val msg = "Booking baru dari ${newest.userName.ifBlank { "Pelanggan" }} untuk ${newest.psUnitName}"
            snackbarHostState.showSnackbar(
                message = "🔔 $msg",
                actionLabel = "Lihat",
                duration = SnackbarDuration.Long
            )
            AdminFCMService.showBookingNotification(context, "🎮 Booking Baru Masuk!", msg)
        }
        prevPendingCount = count
    }

    // Deteksi snack order baru → notifikasi real-time
    var prevSnackCount by remember { mutableIntStateOf(-1) }
    LaunchedEffect(activeSnackOrders) {
        val count = activeSnackOrders.size
        if (prevSnackCount >= 0 && count > prevSnackCount) {
            val newest = activeSnackOrders.first()
            val msg = "Pesanan snack dari ${newest.userName.ifBlank { "Pelanggan" }} di ${newest.psUnitName}"
            snackbarHostState.showSnackbar(
                message = "🍟 $msg",
                actionLabel = "Lihat",
                duration = SnackbarDuration.Long
            )
            AdminFCMService.showSnackNotification(context, "🍟 Order Snack Baru!", msg)
        }
        prevSnackCount = count
    }

    // Snackbar sukses / error
    LaunchedEffect(uiState.successMessage, uiState.error) {
        val msg = uiState.successMessage ?: uiState.error ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        bookingViewModel.clearMessage()
    }

    if (showPsManagement) {
        PsManagementScreen(bookingViewModel = bookingViewModel, onBack = { showPsManagement = false })
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Playzone Admin", fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Halo, ${userProfile?.name ?: "Admin"} 👋", fontSize = 12.sp, color = TextSecondary)
                    }
                },
                actions = {
                    val totalPending = pendingBookings.size + activeSnackOrders.size
                    if (totalPending > 0) {
                        BadgedBox(badge = { Badge { Text("$totalPending") } }) {
                            Icon(Icons.Default.Notifications, null, tint = GoldAccent)
                        }
                    }
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.Logout, null, tint = RedCancel.copy(0.8f))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = SurfaceDark) {
                // Tab 0: Ringkasan
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Dashboard, null) },
                    label = { Text("Ringkasan", fontSize = 10.sp) }
                )
                // Tab 1: Antrian
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        BadgedBox(badge = {
                            if (pendingBookings.isNotEmpty()) Badge { Text(pendingBookings.size.toString()) }
                        }) { Icon(Icons.Default.Queue, null) }
                    },
                    label = { Text("Antrian", fontSize = 10.sp) }
                )
                // Tab 2: Sesi Aktif
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.PlayCircle, null) },
                    label = { Text("Sesi Aktif", fontSize = 10.sp) }
                )
                // Tab 3: Snack
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        BadgedBox(badge = {
                            if (activeSnackOrders.isNotEmpty()) Badge { Text(activeSnackOrders.size.toString()) }
                        }) { Icon(Icons.Default.Fastfood, null) }
                    },
                    label = { Text("Snack", fontSize = 10.sp) }
                )
                // Tab 4: Riwayat (BARU — ini yang diminta)
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = {
                        BadgedBox(badge = {
                            // Badge jumlah total booking di riwayat untuk awareness
                            if (allBookings.isNotEmpty() && selectedTab != 4) {
                                Badge { Text(allBookings.size.toString()) }
                            }
                        }) { Icon(Icons.Default.History, null) }
                    },
                    label = { Text("Riwayat", fontSize = 10.sp) }
                )
                // Tab 5: Laporan
                NavigationBarItem(
                    selected = selectedTab == 5,
                    onClick = { selectedTab = 5 },
                    icon = { Icon(Icons.Default.Assessment, null) },
                    label = { Text("Laporan", fontSize = 10.sp) }
                )
            }
        },
        containerColor = DarkBackground
    ) { padding ->
        when (selectedTab) {
            0 -> AdminDashboardTab(
                modifier = Modifier.padding(padding),
                bookingViewModel = bookingViewModel,
                onGoToQueue = { selectedTab = 1 },
                onGoToActive = { selectedTab = 2 },
                onGoToPsManagement = { showPsManagement = true }
            )
            1 -> BookingQueueScreen(
                modifier = Modifier.padding(padding),
                bookingViewModel = bookingViewModel,
                onGoCheckout = onGoCheckout
            )
            2 -> ActiveSessionScreen(
                modifier = Modifier.padding(padding),
                bookingViewModel = bookingViewModel,
                onGoCheckout = onGoCheckout
            )
            3 -> SnackManagementScreen(
                modifier = Modifier.padding(padding),
                bookingViewModel = bookingViewModel
            )
            4 -> BookingHistoryScreen(
                modifier = Modifier.padding(padding),
                bookingViewModel = bookingViewModel
            )
            5 -> DailyReportScreen(
                modifier = Modifier.padding(padding),
                bookingViewModel = bookingViewModel
            )
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = { Icon(Icons.Default.Logout, null, tint = RedCancel) },
            title = { Text("Logout") },
            text = { Text("Yakin ingin keluar dari akun admin?") },
            confirmButton = {
                Button(
                    onClick = { authViewModel.logout(); onLogout() },
                    colors = ButtonDefaults.buttonColors(containerColor = RedCancel)
                ) { Text("Ya, Logout") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLogoutDialog = false }) { Text("Batal") }
            },
            containerColor = SurfaceDark
        )
    }
}
