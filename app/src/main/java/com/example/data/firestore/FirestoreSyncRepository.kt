package com.example.data.firestore

import android.content.Context
import com.example.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.Locale
import java.util.UUID

class FirestoreSyncRepository(private val db: FirebaseFirestore) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    suspend fun ensureUserAndSpace(user: FirebaseUser): SpaceDocument {
        val uid = user.uid
        val userRef = db.collection("users").document(uid)
        val userSnap = try {
            userRef.get().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, userRef.path)
            throw e
        }

        var spaceId = if (userSnap.exists()) {
            userSnap.getString("activeSpaceId") ?: ""
        } else {
            ""
        }

        var spaceDoc: SpaceDocument? = null
        if (spaceId.isNotBlank()) {
            val spaceRef = db.collection("spaces").document(spaceId)
            val snap = try {
                spaceRef.get().await()
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.GET, spaceRef.path)
                null
            }
            if (snap != null && snap.exists()) {
                spaceDoc = snap.toObject(SpaceDocument::class.java)
            }
        }

        if (spaceDoc == null) {
            val newSpaceId = "space_${uid.take(8)}_${UUID.randomUUID().toString().take(6)}"
            val shareCode = (100000 + (Math.random() * 900000).toInt()).toString()
            val spaceRef = db.collection("spaces").document(newSpaceId)
            val newSpacePayload = mapOf(
                "id" to newSpaceId,
                "name" to "Minhas Finanças",
                "code" to shareCode,
                "ownerId" to uid,
                "memberUids" to listOf(uid),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            try {
                spaceRef.set(newSpacePayload).await()
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.CREATE, spaceRef.path)
                throw e
            }

            // Seed default profiles
            val defaultProfiles = listOf(
                "Você" to "#0062FF",
                "Parceiro(a)" to "#38BDF8",
                "Conjunto" to "#818CF8"
            )
            defaultProfiles.forEach { (name, color) ->
                val pId = UUID.randomUUID().toString()
                val pRef = spaceRef.collection("profiles").document(pId)
                pRef.set(
                    mapOf(
                        "id" to pId,
                        "spaceId" to newSpaceId,
                        "name" to name,
                        "colorHex" to color,
                        "memberUids" to listOf(uid),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            // Seed default categories
            val defaultCategories = listOf(
                "Moradia" to "#0062FF",
                "Alimentação" to "#F59E0B",
                "Transporte" to "#6366F1",
                "Assinaturas" to "#EC4899",
                "Saúde" to "#10B981",
                "Outros" to "#64748B"
            )
            defaultCategories.forEach { (catName, color) ->
                val cId = UUID.randomUUID().toString()
                val cRef = spaceRef.collection("categories").document(cId)
                cRef.set(
                    mapOf(
                        "id" to cId,
                        "spaceId" to newSpaceId,
                        "name" to catName,
                        "colorHex" to color,
                        "icon" to "category",
                        "memberUids" to listOf(uid),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            spaceId = newSpaceId
            spaceDoc = SpaceDocument(
                id = newSpaceId,
                name = "Minhas Finanças",
                code = shareCode,
                ownerId = uid,
                memberUids = listOf(uid)
            )
        }

        // Save/Update user profile
        val userPayload = mapOf(
            "id" to uid,
            "email" to (user.email ?: ""),
            "displayName" to (user.displayName ?: "Usuário"),
            "activeSpaceId" to spaceId,
            "createdAt" to (userSnap.getTimestamp("createdAt") ?: FieldValue.serverTimestamp()),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            userRef.set(userPayload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.SET, userRef.path)
            throw e
        }

        return spaceDoc
    }

    suspend fun joinSpaceByCode(code: String): SpaceDocument {
        val uid = requireUserId()
        val query = db.collection("spaces")
            .whereEqualTo("code", code.trim().uppercase(Locale.ROOT))
            .limit(1)
        val snap = try {
            query.get().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, "spaces")
            throw e
        }
        if (snap.isEmpty) {
            throw IllegalArgumentException("Nenhum espaço encontrado com o código '$code'. Verifique se o código está correto.")
        }
        val targetDoc = snap.documents.first()
        val spaceId = targetDoc.id
        val existingMembers = targetDoc.get("memberUids") as? List<*> ?: emptyList<Any>()
        val memberUids = existingMembers.mapNotNull { it?.toString() }.toMutableList()
        if (!memberUids.contains(uid)) {
            memberUids.add(uid)
            targetDoc.reference.update(
                mapOf(
                    "memberUids" to memberUids,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }

        db.collection("users").document(uid).update(
            mapOf(
                "activeSpaceId" to spaceId,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()

        return targetDoc.toObject(SpaceDocument::class.java)
            ?: SpaceDocument(id = spaceId, name = targetDoc.getString("name") ?: "Espaço", code = code, memberUids = memberUids)
    }

    fun observeSpace(spaceId: String): Flow<SpaceDocument?> {
        val path = "spaces/$spaceId"
        return db.collection("spaces").document(spaceId)
            .snapshots()
            .map { snap ->
                if (snap.exists()) snap.toObject(SpaceDocument::class.java)?.copy(id = snap.id) else null
            }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.GET, path)
                throw e
            }
    }

    fun observeBills(spaceId: String): Flow<List<BillDocument>> {
        val uid = requireUserId()
        val path = "spaces/$spaceId/bills"
        return db.collection("spaces").document(spaceId).collection("bills")
            .whereArrayContains("memberUids", uid)
            .snapshots()
            .map { snap ->
                snap.documents.mapNotNull { it.toObject(BillDocument::class.java)?.copy(id = it.id) }
            }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, path)
                throw e
            }
    }

    fun observeProfiles(spaceId: String): Flow<List<ProfileDocument>> {
        val uid = requireUserId()
        val path = "spaces/$spaceId/profiles"
        return db.collection("spaces").document(spaceId).collection("profiles")
            .whereArrayContains("memberUids", uid)
            .snapshots()
            .map { snap ->
                snap.documents.mapNotNull { it.toObject(ProfileDocument::class.java)?.copy(id = it.id) }
            }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, path)
                throw e
            }
    }

    fun observeCategories(spaceId: String): Flow<List<CategoryDocument>> {
        val uid = requireUserId()
        val path = "spaces/$spaceId/categories"
        return db.collection("spaces").document(spaceId).collection("categories")
            .whereArrayContains("memberUids", uid)
            .snapshots()
            .map { snap ->
                snap.documents.mapNotNull { it.toObject(CategoryDocument::class.java)?.copy(id = it.id) }
            }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, path)
                throw e
            }
    }

    suspend fun saveBill(
        spaceId: String,
        billId: String?,
        name: String,
        amount: Double,
        dueDate: String,
        monthYear: String,
        category: String,
        profileName: String,
        isPaid: Boolean,
        isRecurring: Boolean = false,
        memberUids: List<String>
    ) {
        val uid = requireUserId()
        val members = if (memberUids.contains(uid)) memberUids else memberUids + uid
        val id = billId?.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()
        val docRef = db.collection("spaces").document(spaceId).collection("bills").document(id)
        val payload = mapOf(
            "id" to id,
            "spaceId" to spaceId,
            "name" to name.trim(),
            "amount" to amount,
            "dueDate" to dueDate,
            "monthYear" to monthYear,
            "category" to category,
            "isPaid" to isPaid,
            "isRecurring" to isRecurring,
            "profileName" to profileName,
            "memberUids" to members,
            "createdBy" to uid,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun toggleBillPaid(spaceId: String, billId: String, currentIsPaid: Boolean) {
        val docRef = db.collection("spaces").document(spaceId).collection("bills").document(billId)
        try {
            docRef.update(
                mapOf(
                    "isPaid" to !currentIsPaid,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteBill(spaceId: String, billId: String) {
        val docRef = db.collection("spaces").document(spaceId).collection("bills").document(billId)
        try {
            docRef.delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    suspend fun addProfile(spaceId: String, name: String, colorHex: String, memberUids: List<String>) {
        val uid = requireUserId()
        val members = if (memberUids.contains(uid)) memberUids else memberUids + uid
        val id = UUID.randomUUID().toString()
        val docRef = db.collection("spaces").document(spaceId).collection("profiles").document(id)
        val payload = mapOf(
            "id" to id,
            "spaceId" to spaceId,
            "name" to name.trim(),
            "colorHex" to colorHex,
            "memberUids" to members,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteProfile(spaceId: String, profileId: String, profileName: String = "") {
        try {
            if (profileId.isNotBlank()) {
                db.collection("spaces").document(spaceId).collection("profiles").document(profileId).delete().await()
            }
            if (profileName.isNotBlank()) {
                val query = db.collection("spaces").document(spaceId).collection("profiles")
                    .whereEqualTo("name", profileName)
                    .get().await()
                for (doc in query.documents) {
                    doc.reference.delete().await()
                }
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "spaces/$spaceId/profiles")
            throw e
        }
    }

    suspend fun addCategory(spaceId: String, name: String, colorHex: String, memberUids: List<String>) {
        val uid = requireUserId()
        val members = if (memberUids.contains(uid)) memberUids else memberUids + uid
        val id = UUID.randomUUID().toString()
        val docRef = db.collection("spaces").document(spaceId).collection("categories").document(id)
        val payload = mapOf(
            "id" to id,
            "spaceId" to spaceId,
            "name" to name.trim(),
            "colorHex" to colorHex,
            "icon" to "category",
            "memberUids" to members,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteCategory(spaceId: String, categoryId: String, categoryName: String = "") {
        try {
            if (categoryId.isNotBlank()) {
                db.collection("spaces").document(spaceId).collection("categories").document(categoryId).delete().await()
            }
            if (categoryName.isNotBlank()) {
                val query = db.collection("spaces").document(spaceId).collection("categories")
                    .whereEqualTo("name", categoryName)
                    .get().await()
                for (doc in query.documents) {
                    doc.reference.delete().await()
                }
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "spaces/$spaceId/categories")
            throw e
        }
    }
}
