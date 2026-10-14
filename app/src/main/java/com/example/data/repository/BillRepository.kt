package com.example.data.repository

import com.example.data.local.BillDao
import com.example.data.local.BillEntity
import com.example.data.local.CategoryDao
import com.example.data.local.CategoryEntity
import com.example.data.local.ProfileDao
import com.example.data.local.ProfileEntity
import kotlinx.coroutines.flow.Flow

class BillRepository(
    private val billDao: BillDao,
    private val profileDao: ProfileDao,
    private val categoryDao: CategoryDao
) {
    // Bills
    fun getBillsForMonth(monthYear: String): Flow<List<BillEntity>> {
        return billDao.getBillsForMonth(monthYear)
    }

    fun getAllBills(): Flow<List<BillEntity>> {
        return billDao.getAllBills()
    }

    suspend fun insertBill(bill: BillEntity): Long {
        return billDao.insertBill(bill)
    }

    suspend fun insertBills(bills: List<BillEntity>): List<Long> {
        return billDao.insertBills(bills)
    }

    suspend fun getBillsListForMonth(monthYear: String): List<BillEntity> {
        return billDao.getBillsListForMonth(monthYear)
    }

    suspend fun updateBill(bill: BillEntity) {
        billDao.updateBill(bill)
    }

    suspend fun deleteBill(bill: BillEntity) {
        billDao.deleteBill(bill)
    }

    suspend fun deleteBillById(id: Long) {
        billDao.deleteBillById(id)
    }

    suspend fun togglePaidStatus(id: Long, isPaid: Boolean) {
        billDao.updatePaidStatus(id, isPaid)
    }

    // Profiles
    fun getAllProfiles(): Flow<List<ProfileEntity>> {
        return profileDao.getAllProfiles()
    }

    suspend fun insertProfile(profile: ProfileEntity): Long {
        return profileDao.insertProfile(profile)
    }

    suspend fun deleteProfile(profile: ProfileEntity) {
        if (profile.id != 0L) {
            profileDao.deleteProfileById(profile.id)
        }
        profileDao.deleteProfileByName(profile.name)
    }

    suspend fun getProfileCount(): Int {
        return profileDao.getCount()
    }

    // Categories
    fun getAllCategories(): Flow<List<CategoryEntity>> {
        return categoryDao.getAllCategories()
    }

    suspend fun insertCategory(category: CategoryEntity): Long {
        return categoryDao.insertCategory(category)
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        if (category.id != 0L) {
            categoryDao.deleteCategoryById(category.id)
        }
        categoryDao.deleteCategoryByName(category.name)
    }

    suspend fun getCategoryCount(): Int {
        return categoryDao.getCount()
    }
}
