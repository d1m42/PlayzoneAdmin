package com.playzone.admin.ui.screens.management

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
import com.playzone.admin.data.model.PsUnit
import com.playzone.admin.ui.theme.*
import com.playzone.admin.ui.viewmodel.AdminBookingViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PsManagementScreen(
    modifier: Modifier = Modifier,
    bookingViewModel: AdminBookingViewModel,
    onBack: () -> Unit
) {
    val psUnits by bookingViewModel.psUnits.collectAsStateWithLifecycle()
    val currency = NumberFormat.getNumberInstance(Locale("in", "ID"))

    var showAddDialog by remember { mutableStateOf(false) }
    var editingUnit by remember { mutableStateOf<PsUnit?>(null) }
    var deleteTarget by remember { mutableStateOf<PsUnit?>(null) }

    Scaffold(
        modifier = modifier,
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Kelola PS Unit", fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("${psUnits.size} unit terdaftar", fontSize = 12.sp, color = TextSecondary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, null, tint = PurpleLight)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PurplePrimary
            ) {
                Icon(Icons.Default.Add, null, tint = TextPrimary)
            }
        }
    ) { padding ->
        if (psUnits.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎮", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("Belum ada PS Unit", color = TextSecondary, fontWeight = FontWeight.SemiBold)
                    Text("Tap tombol + untuk tambah PS baru", color = TextSecondary.copy(0.7f), fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Summary cards
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SummarySmallCard(
                            modifier = Modifier.weight(1f),
                            label = "Total Unit",
                            value = "${psUnits.size}",
                            color = PurpleLight
                        )
                        SummarySmallCard(
                            modifier = Modifier.weight(1f),
                            label = "Tersedia",
                            value = "${psUnits.count { it.isAvailable }}",
                            color = GreenSuccess
                        )
                        SummarySmallCard(
                            modifier = Modifier.weight(1f),
                            label = "Tidak Tersedia",
                            value = "${psUnits.count { !it.isAvailable }}",
                            color = RedCancel
                        )
                    }
                }

                items(psUnits, key = { it.id }) { unit ->
                    PsUnitCard(
                        unit = unit,
                        currency = currency,
                        onEdit = { editingUnit = unit },
                        onDelete = { deleteTarget = unit },
                        onToggleAvailability = {
                            bookingViewModel.updatePsUnitAvailability(unit.id, !unit.isAvailable)
                        }
                    )
                }
            }
        }
    }

    // Add dialog
    if (showAddDialog) {
        PsUnitFormDialog(
            title = "Tambah PS Unit Baru",
            initial = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { unit ->
                bookingViewModel.addPsUnit(unit)
                showAddDialog = false
            }
        )
    }

    // Edit dialog
    editingUnit?.let { unit ->
        PsUnitFormDialog(
            title = "Edit PS Unit",
            initial = unit,
            onDismiss = { editingUnit = null },
            onConfirm = { updated ->
                bookingViewModel.updatePsUnit(updated.copy(id = unit.id))
                editingUnit = null
            }
        )
    }

    // Delete confirm dialog
    deleteTarget?.let { unit ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            containerColor = SurfaceDark,
            title = { Text("Hapus PS Unit?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Yakin ingin menghapus \"${unit.name}\"? Data booking yang sudah ada tidak akan terhapus.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    bookingViewModel.deletePsUnit(unit.id)
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
private fun SummarySmallCard(modifier: Modifier, label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Card(modifier = modifier, shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = CardDark)) {
        Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontWeight = FontWeight.Black, color = color, fontSize = 20.sp)
            Text(label, color = TextSecondary, fontSize = 10.sp)
        }
    }
}

@Composable
private fun PsUnitCard(
    unit: PsUnit,
    currency: NumberFormat,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleAvailability: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎮", fontSize = 18.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(unit.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                    }
                    Spacer(Modifier.height(2.dp))
                    Surface(shape = RoundedCornerShape(4.dp), color = PurplePrimary.copy(0.2f)) {
                        Text(
                            unit.type,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            color = PurpleLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                // Available toggle
                Switch(
                    checked = unit.isAvailable,
                    onCheckedChange = { onToggleAvailability() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = GreenSuccess,
                        checkedTrackColor = GreenSuccess.copy(0.3f),
                        uncheckedThumbColor = RedCancel,
                        uncheckedTrackColor = RedCancel.copy(0.3f)
                    )
                )
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = SurfaceDark)
            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Harga/jam", color = TextSecondary, fontSize = 11.sp)
                    Text("Rp ${currency.format(unit.pricePerHour)}", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Status", color = TextSecondary, fontSize = 11.sp)
                    Text(
                        if (unit.isAvailable) "✅ Tersedia" else "❌ Tidak Tersedia",
                        color = if (unit.isAvailable) GreenSuccess else RedCancel,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }

            if (unit.description.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text("📝 ${unit.description}", color = TextSecondary, fontSize = 12.sp)
            }

            if (unit.facilities.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text("🎁 ${unit.facilities.joinToString(", ")}", color = TextSecondary, fontSize = 12.sp)
            }

            Spacer(Modifier.height(10.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PurpleLight)
                ) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Edit", fontSize = 13.sp)
                }
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RedCancel)
                ) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Hapus", fontSize = 13.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PsUnitFormDialog(
    title: String,
    initial: PsUnit?,
    onDismiss: () -> Unit,
    onConfirm: (PsUnit) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var type by remember { mutableStateOf(initial?.type ?: "PS5") }
    var priceStr by remember { mutableStateOf(initial?.pricePerHour?.toString() ?: "") }
    var description by remember { mutableStateOf(initial?.description ?: "") }
    var facilitiesStr by remember { mutableStateOf(initial?.facilities?.joinToString(", ") ?: "") }
    var isAvailable by remember { mutableStateOf(initial?.isAvailable ?: true) }
    var expanded by remember { mutableStateOf(false) }

    val typeOptions = listOf("PS3", "PS4", "PS5")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Text(title, fontWeight = FontWeight.Bold, color = TextPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Unit (contoh: PS5 - Unit 1)", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PurpleLight,
                        unfocusedBorderColor = TextSecondary.copy(0.4f)
                    ),
                    singleLine = true
                )

                // Type dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipe PS", color = TextSecondary) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = PurpleLight,
                            unfocusedBorderColor = TextSecondary.copy(0.4f)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        containerColor = CardDark
                    ) {
                        typeOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, color = TextPrimary) },
                                onClick = { type = option; expanded = false }
                            )
                        }
                    }
                }

                // Price
                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it.filter { c -> c.isDigit() } },
                    label = { Text("Harga per Jam (Rp)", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PurpleLight,
                        unfocusedBorderColor = TextSecondary.copy(0.4f)
                    ),
                    singleLine = true
                )

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Deskripsi (opsional)", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PurpleLight,
                        unfocusedBorderColor = TextSecondary.copy(0.4f)
                    ),
                    maxLines = 2
                )

                // Facilities
                OutlinedTextField(
                    value = facilitiesStr,
                    onValueChange = { facilitiesStr = it },
                    label = { Text("Fasilitas (pisahkan koma: TV 4K, Headset)", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = PurpleLight,
                        unfocusedBorderColor = TextSecondary.copy(0.4f)
                    ),
                    singleLine = true
                )

                // Available toggle
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tersedia untuk booking", color = TextPrimary, fontSize = 14.sp)
                    Switch(
                        checked = isAvailable,
                        onCheckedChange = { isAvailable = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GreenSuccess,
                            checkedTrackColor = GreenSuccess.copy(0.3f)
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && priceStr.isNotBlank()) {
                        val facilities = if (facilitiesStr.isBlank()) emptyList()
                        else facilitiesStr.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        onConfirm(
                            PsUnit(
                                name = name.trim(),
                                type = type,
                                pricePerHour = priceStr.toIntOrNull() ?: 0,
                                description = description.trim(),
                                facilities = facilities,
                                isAvailable = isAvailable
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                enabled = name.isNotBlank() && priceStr.isNotBlank()
            ) {
                Text(if (initial != null) "Simpan Perubahan" else "Tambah Unit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = TextSecondary) }
        }
    )
}
