package com.playzone.admin.data.model

import com.google.firebase.firestore.DocumentId

// ── PS Unit Model ──────────────────────────────────────────────
data class PsUnit(
    @DocumentId val id: String = "",
    val name: String = "",
    val type: String = "PS5",
    val pricePerHour: Int = 15000,
    val isAvailable: Boolean = true,
    val imageUrl: String = "",
    val facilities: List<String> = emptyList(),
    val description: String = ""
)

// ── Booking Model ──────────────────────────────────────────────
data class Booking(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhone: String = "",
    val psUnitId: String = "",
    val psUnitName: String = "",
    val psUnitType: String = "",
    val bookingDate: String = "",       // "dd/MM/yyyy"
    val startTime: String = "",         // "HH:00"
    val startTimeMillis: Long = 0L,
    val endTimeMillis: Long = 0L,
    val durationHours: Int = 1,
    val totalPrice: Int = 0,
    val status: BookingStatus = BookingStatus.PENDING,
    val paymentMethod: PaymentMethod = PaymentMethod.COD,
    val createdAt: Long = System.currentTimeMillis(),
    val fcmToken: String = "",
    // Admin fields
    val checkinAt: Long = 0L,
    val checkoutAt: Long = 0L,
    val snackTotal: Int = 0,
    val grandTotal: Int = 0,
    val adminNotes: String = ""
)

enum class BookingStatus {
    PENDING, CONFIRMED, ACTIVE, COMPLETED, CANCELLED
}

enum class PaymentMethod {
    COD, TRANSFER
}

// ── Snack Order Model ──────────────────────────────────────────
data class SnackOrder(
    val id: String = "",
    val bookingId: String = "",
    val userId: String = "",
    val userName: String = "",
    val psUnitName: String = "",
    val items: List<SnackOrderItem> = emptyList(),
    val totalPrice: Int = 0,
    val status: SnackOrderStatus = SnackOrderStatus.WAITING,
    val createdAt: Long = System.currentTimeMillis()
)

data class SnackOrderItem(
    val name: String = "",
    val emoji: String = "🍟",
    val qty: Int = 1,
    val price: Int = 0
)

enum class SnackOrderStatus {
    WAITING, PREPARING, SERVED
}

// ── User Profile ───────────────────────────────────────────────
data class UserProfile(
    @DocumentId val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val fcmToken: String = "",
    val role: String = "customer",
    val createdAt: Long = System.currentTimeMillis()
)

// ── Extensions ─────────────────────────────────────────────────
fun Booking.toMap(): Map<String, Any> = mapOf(
    "id" to id,
    "userId" to userId,
    "userName" to userName,
    "userPhone" to userPhone,
    "psUnitId" to psUnitId,
    "psUnitName" to psUnitName,
    "psUnitType" to psUnitType,
    "bookingDate" to bookingDate,
    "startTime" to startTime,
    "startTimeMillis" to startTimeMillis,
    "endTimeMillis" to endTimeMillis,
    "durationHours" to durationHours,
    "totalPrice" to totalPrice,
    "status" to status.name,
    "paymentMethod" to paymentMethod.name,
    "createdAt" to createdAt,
    "fcmToken" to fcmToken,
    "checkinAt" to checkinAt,
    "checkoutAt" to checkoutAt,
    "snackTotal" to snackTotal,
    "grandTotal" to grandTotal,
    "adminNotes" to adminNotes
)

fun Map<String, Any>.toBooking(): Booking = Booking(
    id = this["id"] as? String ?: "",
    userId = this["userId"] as? String ?: "",
    userName = this["userName"] as? String ?: "",
    userPhone = this["userPhone"] as? String ?: "",
    psUnitId = this["psUnitId"] as? String ?: "",
    psUnitName = this["psUnitName"] as? String ?: "",
    psUnitType = this["psUnitType"] as? String ?: "",
    bookingDate = this["bookingDate"] as? String ?: "",
    startTime = this["startTime"] as? String ?: "",
    startTimeMillis = (this["startTimeMillis"] as? Long) ?: 0L,
    endTimeMillis = (this["endTimeMillis"] as? Long) ?: 0L,
    durationHours = (this["durationHours"] as? Long)?.toInt() ?: 1,
    totalPrice = (this["totalPrice"] as? Long)?.toInt() ?: 0,
    status = BookingStatus.valueOf(this["status"] as? String ?: "PENDING"),
    paymentMethod = PaymentMethod.valueOf(this["paymentMethod"] as? String ?: "COD"),
    createdAt = (this["createdAt"] as? Long) ?: 0L,
    fcmToken = this["fcmToken"] as? String ?: "",
    checkinAt = (this["checkinAt"] as? Long) ?: 0L,
    checkoutAt = (this["checkoutAt"] as? Long) ?: 0L,
    snackTotal = (this["snackTotal"] as? Long)?.toInt() ?: 0,
    grandTotal = (this["grandTotal"] as? Long)?.toInt() ?: 0,
    adminNotes = this["adminNotes"] as? String ?: ""
)

// ── Snack Menu Item (katalog snack yang tersedia) ───────────────
data class SnackMenuItem(
    @DocumentId val id: String = "",
    val name: String = "",
    val emoji: String = "🍟",
    val price: Int = 0,
    val isAvailable: Boolean = true,
    val category: String = "food" // "food" or "drink"
)
