package com.example.data.firestore

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserDocument(
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val activeSpaceId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class SpaceDocument(
    val id: String = "",
    val name: String = "",
    val code: String = "",
    val ownerId: String = "",
    val memberUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class BillDocument(
    val id: String = "",
    val spaceId: String = "",
    val name: String = "",
    val amount: Double = 0.0,
    val dueDate: String = "",
    val monthYear: String = "",
    val category: String = "",
    val isPaid: Boolean = false,
    val isRecurring: Boolean = false,
    val profileName: String = "",
    val memberUids: List<String> = emptyList(),
    val createdBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ProfileDocument(
    val id: String = "",
    val spaceId: String = "",
    val name: String = "",
    val colorHex: String = "#0062FF",
    val memberUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CategoryDocument(
    val id: String = "",
    val spaceId: String = "",
    val name: String = "",
    val colorHex: String = "#0062FF",
    val icon: String = "category",
    val memberUids: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
