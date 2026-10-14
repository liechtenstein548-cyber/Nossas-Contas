package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {
    @Query("SELECT * FROM bills WHERE monthYear = :monthYear ORDER BY isPaid ASC, dueDate ASC, id ASC")
    fun getBillsForMonth(monthYear: String): Flow<List<BillEntity>>

    @Query("SELECT * FROM bills ORDER BY dueDate DESC")
    fun getAllBills(): Flow<List<BillEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: BillEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBills(bills: List<BillEntity>): List<Long>

    @Query("SELECT * FROM bills WHERE monthYear = :monthYear")
    suspend fun getBillsListForMonth(monthYear: String): List<BillEntity>

    @Update
    suspend fun updateBill(bill: BillEntity)

    @Delete
    suspend fun deleteBill(bill: BillEntity)

    @Query("DELETE FROM bills WHERE id = :id")
    suspend fun deleteBillById(id: Long)

    @Query("UPDATE bills SET isPaid = :isPaid WHERE id = :id")
    suspend fun updatePaidStatus(id: Long, isPaid: Boolean)

    @Query("SELECT DISTINCT category FROM bills WHERE category != '' ORDER BY category ASC")
    fun getDistinctCategories(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM bills")
    suspend fun getCount(): Int
}
