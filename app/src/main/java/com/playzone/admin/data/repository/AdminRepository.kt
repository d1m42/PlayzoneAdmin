package com.playzone.admin.data.repository

import com.playzone.admin.data.model.*
import com.playzone.admin.data.remote.AdminFirebaseService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepository @Inject constructor(
    private val service: AdminFirebaseService
) {
    val currentUser get() = service.currentUser

    // Auth
    suspend fun signIn(email: String, password: String) = service.signIn(email, password)
    fun signOut() = service.signOut()
    suspend fun getUserProfile(uid: String) = service.getUserProfile(uid)

    // FIX: Simpan FCM token admin ke Firestore saat login
    suspend fun saveAdminFcmToken() = service.saveAdminFcmToken()

    // PS Units
    fun getPsUnitsFlow(): Flow<List<PsUnit>> = service.getPsUnitsFlow()
    suspend fun updatePsUnitAvailability(unitId: String, isAvailable: Boolean) =
        service.updatePsUnitAvailability(unitId, isAvailable)
    suspend fun addPsUnit(unit: PsUnit) = service.addPsUnit(unit)
    suspend fun updatePsUnit(unit: PsUnit) = service.updatePsUnit(unit)
    suspend fun deletePsUnit(unitId: String) = service.deletePsUnit(unitId)

    // Snack Menu
    fun getSnackMenuFlow(): Flow<List<SnackMenuItem>> = service.getSnackMenuFlow()
    suspend fun addSnackMenuItem(item: SnackMenuItem) = service.addSnackMenuItem(item)
    suspend fun updateSnackMenuItem(item: SnackMenuItem) = service.updateSnackMenuItem(item)
    suspend fun deleteSnackMenuItem(itemId: String) = service.deleteSnackMenuItem(itemId)

    // Bookings
    fun getAllBookingsFlow(): Flow<List<Booking>> = service.getAllBookingsFlow()
    // FIX: Flow untuk deteksi booking baru → trigger notifikasi lokal admin
    fun getNewBookingAlertFlow(): Flow<Booking> = service.getNewBookingAlertFlow()
    suspend fun confirmBooking(bookingId: String) = service.confirmBooking(bookingId)
    suspend fun cancelBooking(bookingId: String) = service.cancelBooking(bookingId)
    suspend fun checkInBooking(bookingId: String) = service.checkInBooking(bookingId)
    suspend fun checkOutBooking(
        bookingId: String, snackTotal: Int, grandTotal: Int, adminNotes: String
    ) = service.checkOutBooking(bookingId, snackTotal, grandTotal, adminNotes)
    suspend fun updateBookingStatus(bookingId: String, status: BookingStatus) =
        service.updateBookingStatus(bookingId, status)

    // Snack Orders
    fun getActiveSnackOrdersFlow(): Flow<List<SnackOrder>> = service.getActiveSnackOrdersFlow()
    suspend fun updateSnackOrderStatus(orderId: String, status: SnackOrderStatus) =
        service.updateSnackOrderStatus(orderId, status)
    suspend fun getSnackOrdersByBooking(bookingId: String) =
        service.getSnackOrdersByBooking(bookingId)

    // Daily Report
    suspend fun getBookingsByDate(date: String) = service.getBookingsByDate(date)
}
