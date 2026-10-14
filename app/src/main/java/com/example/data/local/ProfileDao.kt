package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles ORDER BY name ASC")
    fun getAllProfiles(): Flow<List<ProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity): Long

    @Delete
    suspend fun deleteProfile(profile: ProfileEntity)

    @Query("DELETE FROM profiles WHERE id = :id")
    suspend fun deleteProfileById(id: Long)

    @Query("DELETE FROM profiles WHERE name = :name")
    suspend fun deleteProfileByName(name: String)

    @Query("SELECT COUNT(*) FROM profiles")
    suspend fun getCount(): Int
}
