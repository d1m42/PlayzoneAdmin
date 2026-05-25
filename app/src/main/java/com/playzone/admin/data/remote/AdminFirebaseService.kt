package com.playzone.admin.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.messaging.FirebaseMessaging
import com.playzone.admin.data.model.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminFirebaseService @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    val currentUser get() = auth.currentUser

    // ── Auth ────────────────────────────────────────────────────
    suspend fun signIn(email: String, password: String): Result<String> = runCatching {
        auth.signInWithEmailAndPassword(email, password).await().user?.uid
            ?: throw Exception("Login gagal")
    }

    fun signOut() = auth.signOut()

    suspend fun getUserProfile(uid: String): UserProfile? = try {
        firestore.collection("users").document(uid).get().await()
            .toObject(UserProfile::class.java)
    } catch (e: Exception) { null }

    // ── Admin FCM Token — simpan ke Firestore agar bisa dipakai oleh Booking app ──
    suspend fun saveAdminFcmToken() {
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            if (token.isNotEmpty()) {
                firestore.collection("admin_config").document("fcm")
                    .set(mapOf("token" to token, "updatedAt" to System.currentTimeMillis()))
                    .await()
            }
        } catch (e: Exception) { /* ignore */ }
    }

    // ── PS Units ────────────────────────────────────────────────
    fun getPsUnitsFlow(): Flow<List<PsUnit>> = callbackFlow {
        val listener = firestore.collection("ps_units")
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(emptyList()); return@addSnapshotListener }
                val units = snap?.documents?.mapNotNull { doc ->
                    try {
                        PsUnit(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            type = doc.getString("type") ?: "PS4",
                            pricePerHour = (doc.getLong("pricePerHour") ?: 0L).toInt(),
                            isAvailable = doc.getBoolean("isAvailable") ?: true,
                            imageUrl = doc.getString("imageUrl") ?: "",
                            facilities = (doc.get("facilities") as? List<*>)
                                ?.map { it.toString() } ?: emptyList(),
                            description = doc.getString("description") ?: ""
                        )
                    } catch (e: Exception) { null }
                } ?: emptyList()
                trySend(units)
            }
        awaitClose { listener.remove() }
    }

    suspend fun updatePsUnitAvailability(unitId: String, isAvailable: Boolean): Result<Unit> =
        runCatching {
            firestore.collection("ps_units").document(unitId)
                .update("isAvailable", isAvailable).await()
        }

    suspend fun addPsUnit(unit: PsUnit): Result<Unit> = runCatching {
        val data = mapOf(
            "name" to unit.name,
            "type" to unit.type,
            "pricePerHour" to unit.pricePerHour,
            "isAvailable" to unit.isAvailable,
            "imageUrl" to unit.imageUrl,
            "facilities" to unit.facilities,
            "description" to unit.description
        )
        firestore.collection("ps_units").add(data).await()
        Unit
    }

    suspend fun updatePsUnit(unit: PsUnit): Result<Unit> = runCatching {
        val data = mapOf(
            "name" to unit.name,
            "type" to unit.type,
            "pricePerHour" to unit.pricePerHour,
            "isAvailable" to unit.isAvailable,
            "imageUrl" to unit.imageUrl,
            "facilities" to unit.facilities,
            "description" to unit.description
        )
        firestore.collection("ps_units").document(unit.id).update(data).await()
    }

    suspend fun deletePsUnit(unitId: String): Result<Unit> = runCatching {
        firestore.collection("ps_units").document(unitId).delete().await()
    }

    // ── Snack Menu (katalog) ──────────────────────────────────────
    fun getSnackMenuFlow(): Flow<List<SnackMenuItem>> = callbackFlow {
        val listener = firestore.collection("snack_menu")
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(emptyList()); return@addSnapshotListener }
                val items = snap?.documents?.mapNotNull { doc ->
                    try {
                        SnackMenuItem(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            emoji = doc.getString("emoji") ?: "🍟",
                            price = (doc.getLong("price") ?: 0L).toInt(),
                            isAvailable = doc.getBoolean("isAvailable") ?: true,
                            category = doc.getString("category") ?: "food"
                        )
                    } catch (e: Exception) { null }
                } ?: emptyList()
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addSnackMenuItem(item: SnackMenuItem): Result<Unit> = runCatching {
        val data = mapOf(
            "name" to item.name,
            "emoji" to item.emoji,
            "price" to item.price,
            "isAvailable" to item.isAvailable,
            "category" to item.category
        )
        firestore.collection("snack_menu").add(data).await()
        Unit
    }

    suspend fun updateSnackMenuItem(item: SnackMenuItem): Result<Unit> = runCatching {
        val data = mapOf(
            "name" to item.name,
            "emoji" to item.emoji,
            "price" to item.price,
            "isAvailable" to item.isAvailable,
            "category" to item.category
        )
        firestore.collection("snack_menu").document(item.id).update(data).await()
    }

    suspend fun deleteSnackMenuItem(itemId: String): Result<Unit> = runCatching {
        firestore.collection("snack_menu").document(itemId).delete().await()
    }

    // ── Bookings ────────────────────────────────────────────────
    fun getAllBookingsFlow(): Flow<List<Booking>> = callbackFlow {
        val listener = firestore.collection("bookings")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(emptyList()); return@addSnapshotListener }
                val bookings = snap?.documents?.mapNotNull { doc ->
                    try {
                        Booking(
                            id = doc.getString("id") ?: doc.id,
                            userId = doc.getString("userId") ?: "",
                            userName = doc.getString("userName") ?: "",
                            userPhone = doc.getString("userPhone") ?: "",
                            psUnitId = doc.getString("psUnitId") ?: "",
                            psUnitName = doc.getString("psUnitName") ?: "",
                            psUnitType = doc.getString("psUnitType") ?: "",
                            bookingDate = doc.getString("bookingDate") ?: "",
                            startTime = doc.getString("startTime") ?: "",
                            startTimeMillis = doc.getLong("startTimeMillis") ?: 0L,
                            endTimeMillis = doc.getLong("endTimeMillis") ?: 0L,
                            durationHours = (doc.getLong("durationHours") ?: 1L).toInt(),
                            totalPrice = (doc.getLong("totalPrice") ?: 0L).toInt(),
                            status = try {
                                BookingStatus.valueOf(doc.getString("status") ?: "PENDING")
                            } catch (e: Exception) { BookingStatus.PENDING },
                            paymentMethod = try {
                                PaymentMethod.valueOf(doc.getString("paymentMethod") ?: "COD")
                            } catch (e: Exception) { PaymentMethod.COD },
                            createdAt = doc.getLong("createdAt") ?: 0L,
                            fcmToken = doc.getString("fcmToken") ?: "",
                            checkinAt = doc.getLong("checkinAt") ?: 0L,
                            checkoutAt = doc.getLong("checkoutAt") ?: 0L,
                            snackTotal = (doc.getLong("snackTotal") ?: 0L).toInt(),
                            grandTotal = (doc.getLong("grandTotal") ?: 0L).toInt(),
                            adminNotes = doc.getString("adminNotes") ?: ""
                        )
                    } catch (e: Exception) { null }
                } ?: emptyList()
                trySend(bookings)
            }
        awaitClose { listener.remove() }
    }

    /**
     * FIX: Flow khusus untuk deteksi booking BARU (added) — dipakai untuk notifikasi lokal admin.
     * Hanya emit booking yang ditambahkan SETELAH listener aktif (bukan data lama).
     */
    fun getNewBookingAlertFlow(): Flow<Booking> = callbackFlow {
        var isFirstSnapshot = true
        val listener = firestore.collection("bookings")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null || snap == null) return@addSnapshotListener
                // Skip snapshot pertama — itu data existing, bukan baru masuk
                if (isFirstSnapshot) {
                    isFirstSnapshot = false
                    return@addSnapshotListener
                }
                // Hanya proses document yang ADDED (bukan modified/removed)
                for (change in snap.documentChanges) {
                    if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                        val doc = change.document
                        try {
                            val booking = Booking(
                                id = doc.getString("id") ?: doc.id,
                                userName = doc.getString("userName") ?: "",
                                psUnitName = doc.getString("psUnitName") ?: "",
                                bookingDate = doc.getString("bookingDate") ?: "",
                                startTime = doc.getString("startTime") ?: "",
                                durationHours = (doc.getLong("durationHours") ?: 1L).toInt(),
                                totalPrice = (doc.getLong("totalPrice") ?: 0L).toInt(),
                                status = try {
                                    BookingStatus.valueOf(doc.getString("status") ?: "CONFIRMED")
                                } catch (e: Exception) { BookingStatus.CONFIRMED },
                                paymentMethod = try {
                                    PaymentMethod.valueOf(doc.getString("paymentMethod") ?: "COD")
                                } catch (e: Exception) { PaymentMethod.COD },
                                createdAt = doc.getLong("createdAt") ?: 0L
                            )
                            trySend(booking)
                        } catch (e: Exception) { /* skip */ }
                    }
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateBookingStatus(bookingId: String, status: BookingStatus): Result<Unit> =
        runCatching {
            firestore.collection("bookings").document(bookingId)
                .update("status", status.name).await()
        }

    suspend fun confirmBooking(bookingId: String): Result<Unit> = runCatching {
        firestore.collection("bookings").document(bookingId)
            .update("status", BookingStatus.CONFIRMED.name).await()
    }

    suspend fun checkInBooking(bookingId: String): Result<Unit> = runCatching {
        val now = System.currentTimeMillis()
        firestore.collection("bookings").document(bookingId).update(
            mapOf(
                "status" to BookingStatus.ACTIVE.name,
                "checkinAt" to now
            )
        ).await()
    }

    suspend fun checkOutBooking(
        bookingId: String,
        snackTotal: Int,
        grandTotal: Int,
        adminNotes: String
    ): Result<Unit> = runCatching {
        val now = System.currentTimeMillis()
        firestore.collection("bookings").document(bookingId).update(
            mapOf(
                "status" to BookingStatus.COMPLETED.name,
                "checkoutAt" to now,
                "snackTotal" to snackTotal,
                "grandTotal" to grandTotal,
                "adminNotes" to adminNotes
            )
        ).await()
    }

    suspend fun cancelBooking(bookingId: String): Result<Unit> = runCatching {
        firestore.collection("bookings").document(bookingId)
            .update("status", BookingStatus.CANCELLED.name).await()
    }

    // ── Snack Orders ─────────────────────────────────────────────
    fun getActiveSnackOrdersFlow(): Flow<List<SnackOrder>> = callbackFlow {
        val listener = firestore.collection("snack_orders")
            .whereIn("status", listOf(
                SnackOrderStatus.WAITING.name,
                SnackOrderStatus.PREPARING.name
            ))
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(emptyList()); return@addSnapshotListener }
                val orders = snap?.documents?.mapNotNull { doc ->
                    try {
                        @Suppress("UNCHECKED_CAST")
                        val rawItems = doc.get("items") as? List<Map<String, Any>> ?: emptyList()
                        val items = rawItems.map { item ->
                            SnackOrderItem(
                                name = item["name"] as? String ?: "",
                                emoji = item["emoji"] as? String ?: "🍟",
                                qty = (item["qty"] as? Long)?.toInt() ?: 1,
                                price = (item["price"] as? Long)?.toInt() ?: 0
                            )
                        }
                        SnackOrder(
                            id = doc.id,
                            bookingId = doc.getString("bookingId") ?: "",
                            userId = doc.getString("userId") ?: "",
                            userName = doc.getString("userName") ?: "",
                            psUnitName = doc.getString("psUnitName") ?: "",
                            items = items,
                            totalPrice = (doc.getLong("totalPrice") ?: 0L).toInt(),
                            status = try {
                                SnackOrderStatus.valueOf(doc.getString("status") ?: "WAITING")
                            } catch (e: Exception) { SnackOrderStatus.WAITING },
                            createdAt = doc.getLong("createdAt") ?: 0L
                        )
                    } catch (e: Exception) { null }
                } ?: emptyList()
                trySend(orders)
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateSnackOrderStatus(orderId: String, status: SnackOrderStatus): Result<Unit> =
        runCatching {
            firestore.collection("snack_orders").document(orderId)
                .update("status", status.name).await()
        }

    suspend fun getSnackOrdersByBooking(bookingId: String): List<SnackOrder> = try {
        val snap = firestore.collection("snack_orders")
            .whereEqualTo("bookingId", bookingId)
            .get().await()
        snap.documents.mapNotNull { doc ->
            try {
                @Suppress("UNCHECKED_CAST")
                val rawItems = doc.get("items") as? List<Map<String, Any>> ?: emptyList()
                val items = rawItems.map { item ->
                    SnackOrderItem(
                        name = item["name"] as? String ?: "",
                        emoji = item["emoji"] as? String ?: "🍟",
                        qty = (item["qty"] as? Long)?.toInt() ?: 1,
                        price = (item["price"] as? Long)?.toInt() ?: 0
                    )
                }
                SnackOrder(
                    id = doc.id,
                    bookingId = bookingId,
                    items = items,
                    totalPrice = (doc.getLong("totalPrice") ?: 0L).toInt(),
                    status = try {
                        SnackOrderStatus.valueOf(doc.getString("status") ?: "WAITING")
                    } catch (e: Exception) { SnackOrderStatus.WAITING },
                    createdAt = doc.getLong("createdAt") ?: 0L
                )
            } catch (e: Exception) { null }
        }
    } catch (e: Exception) { emptyList() }

    // ── Daily Report ─────────────────────────────────────────────
    suspend fun getBookingsByDate(date: String): List<Booking> = try {
        val snap = firestore.collection("bookings")
            .whereEqualTo("bookingDate", date)
            .get().await()
        snap.documents.mapNotNull { doc ->
            try {
                Booking(
                    id = doc.getString("id") ?: doc.id,
                    userName = doc.getString("userName") ?: "",
                    userPhone = doc.getString("userPhone") ?: "",
                    psUnitName = doc.getString("psUnitName") ?: "",
                    psUnitType = doc.getString("psUnitType") ?: "",
                    bookingDate = doc.getString("bookingDate") ?: "",
                    startTime = doc.getString("startTime") ?: "",
                    durationHours = (doc.getLong("durationHours") ?: 1L).toInt(),
                    totalPrice = (doc.getLong("totalPrice") ?: 0L).toInt(),
                    status = try {
                        BookingStatus.valueOf(doc.getString("status") ?: "PENDING")
                    } catch (e: Exception) { BookingStatus.PENDING },
                    paymentMethod = try {
                        PaymentMethod.valueOf(doc.getString("paymentMethod") ?: "COD")
                    } catch (e: Exception) { PaymentMethod.COD },
                    snackTotal = (doc.getLong("snackTotal") ?: 0L).toInt(),
                    grandTotal = (doc.getLong("grandTotal") ?: 0L).toInt()
                )
            } catch (e: Exception) { null }
        }
    } catch (e: Exception) { emptyList() }
}
