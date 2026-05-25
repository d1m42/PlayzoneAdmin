package com.playzone.admin.ui.screens.snack

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.playzone.admin.data.model.SnackMenuItem
import com.playzone.admin.data.model.SnackOrder
import com.playzone.admin.data.model.SnackOrderStatus
import com.playzone.admin.ui.theme.*
import com.playzone.admin.ui.viewmodel.AdminBookingViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SnackManagementScreen(
    modifier: Modifier,
    bookingViewModel: AdminBookingViewModel
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(modifier = modifier.fillMaxSize()) {
        // Sub-tab bar
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = SurfaceDark,
            contentColor = PurpleLight
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Pesanan Aktif", fontSize = 13.sp) },
                icon = { Icon(Icons.Default.Receipt, null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Kelola Menu", fontSize = 13.sp) },
                icon = { Icon(Icons.Default.MenuBook, null, modifier = Modifier.size(18.dp)) }
            )
        }

        when (selectedTab) {
            0 -> SnackOrdersTab(bookingViewModel = bookingViewModel)
            1 -> SnackMenuTab(bookingViewModel = bookingViewModel)
        }
    }
}

// ── Tab 0: Pesanan Aktif ──────────────────────────────────────────────────

@Composable
private fun SnackOrdersTab(bookingViewModel: AdminBookingViewModel) {
    val snackOrders by bookingViewModel.activeSnackOrders.collectAsStateWithLifecycle()
    val waitingOrders = snackOrders.filter { it.status == SnackOrderStatus.WAITING }
    val preparingOrders = snackOrders.filter { it.status == SnackOrderStatus.PREPARING }

    if (snackOrders.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🍟", fontSize = 48.sp)
                Spacer(Modifier.height(12.dp))
                Text("Tidak ada pesanan snack aktif", color = TextSecondary)
                Text("Pesanan baru akan muncul di sini", color = TextSecondary.copy(0.7f), fontSize = 13.sp)
            }
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (waitingOrders.isNotEmpty()) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.HourglassTop, null, tint = OrangeWarning, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Menunggu Diproses (${waitingOrders.size})", color = OrangeWarning, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
            items(waitingOrders, key = { it.id }) { order ->
                SnackOrderCard(order = order, bookingViewModel = bookingViewModel)
            }
        }

        if (preparingOrders.isNotEmpty()) {
            item {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalFireDepartment, null, tint = BlueInfo, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Sedang Disiapkan (${preparingOrders.size})", color = BlueInfo, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
            items(preparingOrders, key = { it.id }) { order ->
                SnackOrderCard(order = order, bookingViewModel = bookingViewModel)
            }
        }
    }
}

@Composable
fun SnackOrderCard(order: SnackOrder, bookingViewModel: AdminBookingViewModel) {
    val currency = NumberFormat.getNumberInstance(Locale("in", "ID"))
    val (statusColor, statusLabel) = when (order.status) {
        SnackOrderStatus.WAITING   -> OrangeWarning to "Menunggu"
        SnackOrderStatus.PREPARING -> BlueInfo to "Disiapkan"
        SnackOrderStatus.SERVED    -> GreenSuccess to "Diantar"
    }

    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = CardDark)) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("🍽️ ${order.userName}", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                    Text(order.psUnitName.ifBlank { "Unit PS" }, color = TextSecondary, fontSize = 12.sp)
                }
                Surface(shape = RoundedCornerShape(6.dp), color = statusColor.copy(0.15f)) {
                    Text(statusLabel, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 8.dp), color = SurfaceDark)
            order.items.forEach { item ->
                Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${item.emoji} ${item.name} x${item.qty}", color = TextPrimary, fontSize = 13.sp)
                    Text("Rp ${currency.format(item.price * item.qty)}", color = GoldAccent, fontSize = 13.sp)
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 8.dp), color = SurfaceDark)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total", color = TextSecondary, fontWeight = FontWeight.SemiBold)
                Text("Rp ${currency.format(order.totalPrice)}", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Spacer(Modifier.height(12.dp))
            when (order.status) {
                SnackOrderStatus.WAITING -> {
                    Button(
                        onClick = { bookingViewModel.updateSnackOrderStatus(order.id, SnackOrderStatus.PREPARING) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BlueInfo)
                    ) {
                        Icon(Icons.Default.LocalFireDepartment, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Mulai Siapkan Pesanan")
                    }
                }
                SnackOrderStatus.PREPARING -> {
                    Button(
                        onClick = { bookingViewModel.updateSnackOrderStatus(order.id, SnackOrderStatus.SERVED) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess)
                    ) {
                        Icon(Icons.Default.DeliveryDining, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Antar ke Pelanggan ✓")
                    }
                }
                SnackOrderStatus.SERVED -> {}
            }
        }
    }
}

// ── Tab 1: Kelola Menu Snack ──────────────────────────────────────────────

@Composable
private fun SnackMenuTab(bookingViewModel: AdminBookingViewModel) {
    val snackMenu by bookingViewModel.snackMenu.collectAsStateWithLifecycle()
    val currency = NumberFormat.getNumberInstance(Locale("in", "ID"))

    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<SnackMenuItem?>(null) }
    var deleteTarget by remember { mutableStateOf<SnackMenuItem?>(null) }

    val foodItems = snackMenu.filter { it.category == "food" }
    val drinkItems = snackMenu.filter { it.category == "drink" }

    Box(Modifier.fillMaxSize()) {
        if (snackMenu.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🍽️", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("Menu snack belum ada", color = TextSecondary, fontWeight = FontWeight.SemiBold)
                    Text("Tap + untuk tambah menu baru", color = TextSecondary.copy(0.7f), fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (foodItems.isNotEmpty()) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🍟", fontSize = 16.sp)
                            Spacer(Modifier.width(6.dp))
                            Text("Makanan (${foodItems.size})", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                    items(foodItems, key = { it.id }) { item ->
                        SnackMenuItemCard(
                            item = item,
                            currency = currency,
                            onEdit = { editingItem = item },
                            onDelete = { deleteTarget = item },
                            onToggle = { bookingViewModel.updateSnackMenuItem(item.copy(isAvailable = !item.isAvailable)) }
                        )
                    }
                }

                if (drinkItems.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🥤", fontSize = 16.sp)
                            Spacer(Modifier.width(6.dp))
                            Text("Minuman (${drinkItems.size})", color = BlueInfo, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                    items(drinkItems, key = { it.id }) { item ->
                        SnackMenuItemCard(
                            item = item,
                            currency = currency,
                            onEdit = { editingItem = item },
                            onDelete = { deleteTarget = item },
                            onToggle = { bookingViewModel.updateSnackMenuItem(item.copy(isAvailable = !item.isAvailable)) }
                        )
                    }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = PurplePrimary
        ) {
            Icon(Icons.Default.Add, null, tint = TextPrimary)
        }
    }

    if (showAddDialog) {
        SnackMenuFormDialog(
            title = "Tambah Menu Snack",
            initial = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { item ->
                bookingViewModel.addSnackMenuItem(item)
                showAddDialog = false
            }
        )
    }

    editingItem?.let { item ->
        SnackMenuFormDialog(
            title = "Edit Menu Snack",
            initial = item,
            onDismiss = { editingItem = null },
            onConfirm = { updated ->
                bookingViewModel.updateSnackMenuItem(updated.copy(id = item.id))
                editingItem = null
            }
        )
    }

    deleteTarget?.let { item ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            containerColor = SurfaceDark,
            title = { Text("Hapus Menu?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Yakin ingin menghapus \"${item.emoji} ${item.name}\" dari menu?", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    bookingViewModel.deleteSnackMenuItem(item.id)
                    deleteTarget = null
                }) { Text("Hapus", color = RedCancel) }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Batal", color = PurpleLight) }
            }
        )
    }
}

@Composable
private fun SnackMenuItemCard(
    item: SnackMenuItem,
    currency: NumberFormat,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggle: () -> Unit
) {
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = CardDark)) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Text(item.emoji, fontSize = 24.sp)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(item.name, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 14.sp)
                    Text("Rp ${currency.format(item.price)}", color = GoldAccent, fontSize = 13.sp)
                    Text(
                        if (item.isAvailable) "✅ Tersedia" else "❌ Tidak Tersedia",
                        color = if (item.isAvailable) GreenSuccess else RedCancel,
                        fontSize = 11.sp
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Switch(
                    checked = item.isAvailable,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = GreenSuccess,
                        checkedTrackColor = GreenSuccess.copy(0.3f),
                        uncheckedThumbColor = RedCancel,
                        uncheckedTrackColor = RedCancel.copy(0.3f)
                    )
                )
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, null, tint = PurpleLight, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, null, tint = RedCancel, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SnackMenuFormDialog(
    title: String,
    initial: SnackMenuItem?,
    onDismiss: () -> Unit,
    onConfirm: (SnackMenuItem) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var emoji by remember { mutableStateOf(initial?.emoji ?: "🍟") }
    var priceStr by remember { mutableStateOf(initial?.price?.toString() ?: "") }
    var category by remember { mutableStateOf(initial?.category ?: "food") }
    var isAvailable by remember { mutableStateOf(initial?.isAvailable ?: true) }
    var categoryExpanded by remember { mutableStateOf(false) }

    val categoryOptions = listOf("food" to "🍟 Makanan", "drink" to "🥤 Minuman")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = { Text(title, fontWeight = FontWeight.Bold, color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Menu", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PurpleLight, unfocusedBorderColor = TextSecondary.copy(0.4f)
                    ),
                    singleLine = true
                )

                // Emoji
                OutlinedTextField(
                    value = emoji,
                    onValueChange = { emoji = it },
                    label = { Text("Emoji", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PurpleLight, unfocusedBorderColor = TextSecondary.copy(0.4f)
                    ),
                    singleLine = true,
                    placeholder = { Text("contoh: 🍟 🥤 🍕", color = TextSecondary.copy(0.5f)) }
                )

                // Price
                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it.filter { c -> c.isDigit() } },
                    label = { Text("Harga (Rp)", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PurpleLight, unfocusedBorderColor = TextSecondary.copy(0.4f)
                    ),
                    singleLine = true
                )

                // Category
                ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = it }) {
                    OutlinedTextField(
                        value = categoryOptions.find { it.first == category }?.second ?: "Makanan",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kategori", color = TextSecondary) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(categoryExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                            focusedBorderColor = PurpleLight, unfocusedBorderColor = TextSecondary.copy(0.4f)
                        )
                    )
                    ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }, containerColor = CardDark) {
                        categoryOptions.forEach { (key, label) ->
                            DropdownMenuItem(
                                text = { Text(label, color = TextPrimary) },
                                onClick = { category = key; categoryExpanded = false }
                            )
                        }
                    }
                }

                // Available
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Tersedia", color = TextPrimary, fontSize = 14.sp)
                    Switch(
                        checked = isAvailable,
                        onCheckedChange = { isAvailable = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = GreenSuccess, checkedTrackColor = GreenSuccess.copy(0.3f))
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && priceStr.isNotBlank()) {
                        onConfirm(SnackMenuItem(
                            name = name.trim(),
                            emoji = emoji.trim().ifBlank { "🍟" },
                            price = priceStr.toIntOrNull() ?: 0,
                            category = category,
                            isAvailable = isAvailable
                        ))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                enabled = name.isNotBlank() && priceStr.isNotBlank()
            ) {
                Text(if (initial != null) "Simpan" else "Tambah")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = TextSecondary) }
        }
    )
}
