package com.playzone.admin.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.playzone.admin.data.model.*
import com.playzone.admin.data.repository.AdminRepository
import com.playzone.admin.service.AdminFCMService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class AdminBookingViewModel @Inject constructor(
    private val repository: AdminRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    // All bookings live stream
    val allBookings: StateFlow<List<Booking>> = repository.getAllBookingsFlow()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // PS Units live stream
    val psUnits: StateFlow<List<PsUnit>> = repository.getPsUnitsFlow()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Snack menu (katalog) live stream
    val snackMenu: StateFlow<List<SnackMenuItem>> = repository.getSnackMenuFlow()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active snack orders live stream
    val activeSnackOrders: StateFlow<List<SnackOrder>> = repository.getActiveSnackOrdersFlow()
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Derived booking lists ──────────────────────────────────
    val pendingBookings: StateFlow<List<Booking>> = allBookings
        .map { it.filter { b -> b.status == BookingStatus.PENDING } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val confirmedBookings: StateFlow<List<Booking>> = allBookings
        .map { it.filter { b -> b.status == BookingStatus.CONFIRMED } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeBookings: StateFlow<List<Booking>> = allBookings
        .map { it.filter { b -> b.status == BookingStatus.ACTIVE } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── State for checkout screen ──────────────────────────────
    private val _checkoutSnackOrders = MutableStateFlow<List<SnackOrder>>(emptyList())
    val checkoutSnackOrders: StateFlow<List<SnackOrder>> = _checkoutSnackOrders.asStateFlow()

    // ── Report state ───────────────────────────────────────────
    private val _reportBookings = MutableStateFlow<List<Booking>>(emptyList())
    val reportBookings: StateFlow<List<Booking>> = _reportBookings.asStateFlow()

    // ── FIX: Listener notifikasi booking baru untuk admin ──────
    // Aktif selama ViewModel hidup (selama app admin terbuka)
    init {
        startNewBookingNotificationListener()
    }

    private fun startNewBookingNotificationListener() {
        viewModelScope.launch {
            repository.getNewBookingAlertFlow()
                .catch { /* ignore errors agar tidak crash */ }
                .collect { booking ->
                    val ctx = getApplication<Application>()
                    AdminFCMService.showBookingNotification(
                        context = ctx,
                        title = "📋 Booking Baru Masuk!",
                        message = "${booking.userName} booking ${booking.psUnitName}\n" +
                                "Tanggal: ${booking.bookingDate}, Jam: ${booking.startTime} " +
                                "(${booking.durationHours} jam)"
                    )
                }
        }
    }

    // ── Actions ────────────────────────────────────────────────

    fun confirmBooking(bookingId: String) {
        viewModelScope.launch {
            _uiState.value = AdminUiState(isLoading = true)
            repository.confirmBooking(bookingId)
                .onSuccess { _uiState.value = AdminUiState(successMessage = "Booking dikonfirmasi ✓") }
                .onFailure { _uiState.value = AdminUiState(error = it.message) }
        }
    }

    fun cancelBooking(bookingId: String) {
        viewModelScope.launch {
            _uiState.value = AdminUiState(isLoading = true)
            repository.cancelBooking(bookingId)
                .onSuccess { _uiState.value = AdminUiState(successMessage = "Booking dibatalkan") }
                .onFailure { _uiState.value = AdminUiState(error = it.message) }
        }
    }

    fun checkInBooking(bookingId: String) {
        viewModelScope.launch {
            _uiState.value = AdminUiState(isLoading = true)
            repository.checkInBooking(bookingId)
                .onSuccess { _uiState.value = AdminUiState(successMessage = "Pelanggan check-in, sesi dimulai! ✓") }
                .onFailure { _uiState.value = AdminUiState(error = it.message) }
        }
    }

    fun checkOutBooking(
        bookingId: String,
        snackTotal: Int,
        grandTotal: Int,
        adminNotes: String
    ) {
        viewModelScope.launch {
            _uiState.value = AdminUiState(isLoading = true)
            repository.checkOutBooking(bookingId, snackTotal, grandTotal, adminNotes)
                .onSuccess { _uiState.value = AdminUiState(successMessage = "Checkout berhasil! Sesi selesai ✓") }
                .onFailure { _uiState.value = AdminUiState(error = it.message) }
        }
    }

    fun updateSnackOrderStatus(orderId: String, status: SnackOrderStatus) {
        viewModelScope.launch {
            repository.updateSnackOrderStatus(orderId, status)
                .onSuccess { _uiState.value = AdminUiState(successMessage = "Status pesanan diperbarui") }
                .onFailure { _uiState.value = AdminUiState(error = it.message) }
        }
    }

    fun updatePsUnitAvailability(unitId: String, isAvailable: Boolean) {
        viewModelScope.launch {
            repository.updatePsUnitAvailability(unitId, isAvailable)
        }
    }

    fun addPsUnit(unit: PsUnit) {
        viewModelScope.launch {
            _uiState.value = AdminUiState(isLoading = true)
            repository.addPsUnit(unit)
                .onSuccess { _uiState.value = AdminUiState(successMessage = "PS Unit berhasil ditambahkan ✓") }
                .onFailure { _uiState.value = AdminUiState(error = it.message) }
        }
    }

    fun updatePsUnit(unit: PsUnit) {
        viewModelScope.launch {
            _uiState.value = AdminUiState(isLoading = true)
            repository.updatePsUnit(unit)
                .onSuccess { _uiState.value = AdminUiState(successMessage = "PS Unit berhasil diperbarui ✓") }
                .onFailure { _uiState.value = AdminUiState(error = it.message) }
        }
    }

    fun deletePsUnit(unitId: String) {
        viewModelScope.launch {
            _uiState.value = AdminUiState(isLoading = true)
            repository.deletePsUnit(unitId)
                .onSuccess { _uiState.value = AdminUiState(successMessage = "PS Unit dihapus") }
                .onFailure { _uiState.value = AdminUiState(error = it.message) }
        }
    }

    fun addSnackMenuItem(item: SnackMenuItem) {
        viewModelScope.launch {
            _uiState.value = AdminUiState(isLoading = true)
            repository.addSnackMenuItem(item)
                .onSuccess { _uiState.value = AdminUiState(successMessage = "Menu snack ditambahkan ✓") }
                .onFailure { _uiState.value = AdminUiState(error = it.message) }
        }
    }

    fun updateSnackMenuItem(item: SnackMenuItem) {
        viewModelScope.launch {
            _uiState.value = AdminUiState(isLoading = true)
            repository.updateSnackMenuItem(item)
                .onSuccess { _uiState.value = AdminUiState(successMessage = "Menu snack diperbarui ✓") }
                .onFailure { _uiState.value = AdminUiState(error = it.message) }
        }
    }

    fun deleteSnackMenuItem(itemId: String) {
        viewModelScope.launch {
            _uiState.value = AdminUiState(isLoading = true)
            repository.deleteSnackMenuItem(itemId)
                .onSuccess { _uiState.value = AdminUiState(successMessage = "Menu snack dihapus") }
                .onFailure { _uiState.value = AdminUiState(error = it.message) }
        }
    }

    fun loadSnackOrdersForCheckout(bookingId: String) {
        viewModelScope.launch {
            _checkoutSnackOrders.value = repository.getSnackOrdersByBooking(bookingId)
        }
    }

    fun loadDailyReport(date: String) {
        viewModelScope.launch {
            _uiState.value = AdminUiState(isLoading = true)
            val bookings = repository.getBookingsByDate(date)
            _reportBookings.value = bookings
            _uiState.value = AdminUiState()
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(
            error = null, successMessage = null, isLoading = false
        )
    }
}
