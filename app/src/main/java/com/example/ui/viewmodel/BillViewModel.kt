package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firestore.BillDocument
import com.example.data.firestore.CategoryDocument
import com.example.data.firestore.FirestoreSyncRepository
import com.example.data.firestore.ProfileDocument
import com.example.data.firestore.SpaceDocument
import com.example.data.local.AppDatabase
import com.example.data.local.BillEntity
import com.example.data.local.CategoryEntity
import com.example.data.local.ProfileEntity
import com.example.data.repository.BillRepository
import com.example.util.DateUtils
import com.example.util.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class CategoryExpense(
    val name: String,
    val totalAmount: Double,
    val percentageOfTotal: Float,
    val count: Int
)

data class MonthSummary(
    val totalMonth: Double = 0.0,
    val totalPaid: Double = 0.0,
    val remainingToPay: Double = 0.0,
    val progressFraction: Float = 0f,
    val progressPercent: Int = 0,
    val categoryExpenses: List<CategoryExpense> = emptyList(),
    val totalBillsCount: Int = 0,
    val paidBillsCount: Int = 0,
    val pendingBillsCount: Int = 0,
    val dueIn10DaysBills: List<BillEntity> = emptyList(),
    val dueIn10DaysAmount: Double = 0.0
)

data class BillsUiState(
    val currentYearMonth: YearMonth = YearMonth.now(),
    val selectedProfile: String = "Todos",
    val bills: List<BillEntity> = emptyList(),
    val allMonthBills: List<BillEntity> = emptyList(),
    val summary: MonthSummary = MonthSummary(),
    val profiles: List<ProfileEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val currentSpace: SpaceDocument? = null,
    val isGuestMode: Boolean = true,
    val isDarkMode: Boolean = false,
    val isFormOpen: Boolean = false,
    val isProfileManagerOpen: Boolean = false,
    val isCategoryManagerOpen: Boolean = false,
    val isSpaceDialogOpen: Boolean = false,
    val isJoiningSpace: Boolean = false,
    val spaceJoinError: String? = null,
    val editingBill: BillEntity? = null,
    val billToDelete: BillEntity? = null,
    val billToRepeat: BillEntity? = null,
    val profileToDelete: ProfileEntity? = null,
    val categoryToDelete: CategoryEntity? = null,
    val userName: String? = null
)

private data class MonthData(
    val ym: YearMonth,
    val profile: String,
    val monthBills: List<BillEntity>,
    val profiles: List<ProfileEntity>,
    val categories: List<CategoryEntity>,
    val space: SpaceDocument?
)

private data class DialogsData(
    val formOpen: Boolean,
    val profileOpen: Boolean,
    val categoryOpen: Boolean,
    val spaceOpen: Boolean,
    val editingBill: BillEntity?,
    val billToDelete: BillEntity?,
    val billToRepeat: BillEntity?,
    val userName: String?
)

@OptIn(ExperimentalCoroutinesApi::class)
class BillViewModel(application: Application) : AndroidViewModel(application) {
    private val localRepository: BillRepository
    private val firestoreRepository: FirestoreSyncRepository

    private val _currentYearMonth = MutableStateFlow(YearMonth.now())
    val currentYearMonth: StateFlow<YearMonth> = _currentYearMonth.asStateFlow()

    private val _selectedProfile = MutableStateFlow("Todos")
    val selectedProfile: StateFlow<String> = _selectedProfile.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _currentSpace = MutableStateFlow<SpaceDocument?>(null)
    val currentSpace: StateFlow<SpaceDocument?> = _currentSpace.asStateFlow()

    private val _isFormOpen = MutableStateFlow(false)
    val isFormOpen: StateFlow<Boolean> = _isFormOpen.asStateFlow()

    private val _isProfileManagerOpen = MutableStateFlow(false)
    val isProfileManagerOpen: StateFlow<Boolean> = _isProfileManagerOpen.asStateFlow()

    private val _isCategoryManagerOpen = MutableStateFlow(false)
    val isCategoryManagerOpen: StateFlow<Boolean> = _isCategoryManagerOpen.asStateFlow()

    private val _isSpaceDialogOpen = MutableStateFlow(false)
    val isSpaceDialogOpen: StateFlow<Boolean> = _isSpaceDialogOpen.asStateFlow()

    private val _isJoiningSpace = MutableStateFlow(false)
    val isJoiningSpace: StateFlow<Boolean> = _isJoiningSpace.asStateFlow()

    private val _spaceJoinError = MutableStateFlow<String?>(null)
    val spaceJoinError: StateFlow<String?> = _spaceJoinError.asStateFlow()

    private val _editingBill = MutableStateFlow<BillEntity?>(null)
    val editingBill: StateFlow<BillEntity?> = _editingBill.asStateFlow()

    private val _billToDelete = MutableStateFlow<BillEntity?>(null)
    val billToDelete: StateFlow<BillEntity?> = _billToDelete.asStateFlow()

    private val _billToRepeat = MutableStateFlow<BillEntity?>(null)
    val billToRepeat: StateFlow<BillEntity?> = _billToRepeat.asStateFlow()

    private val _profileToDelete = MutableStateFlow<ProfileEntity?>(null)
    val profileToDelete: StateFlow<ProfileEntity?> = _profileToDelete.asStateFlow()

    private val _categoryToDelete = MutableStateFlow<CategoryEntity?>(null)
    val categoryToDelete: StateFlow<CategoryEntity?> = _categoryToDelete.asStateFlow()

    private val _userName = MutableStateFlow<String?>(FirebaseAuth.getInstance().currentUser?.displayName)
    val userName: StateFlow<String?> = _userName.asStateFlow()

    private val billDocIdMap = java.util.concurrent.ConcurrentHashMap<Long, String>()
    private val profileDocIdMap = java.util.concurrent.ConcurrentHashMap<Long, String>()
    private val categoryDocIdMap = java.util.concurrent.ConcurrentHashMap<Long, String>()

    init {
        val database = AppDatabase.getInstance(application)
        localRepository = BillRepository(
            database.billDao(),
            database.profileDao(),
            database.categoryDao()
        )
        firestoreRepository = FirestoreSyncRepository(application)

        // Seed default local profiles & categories if empty
        viewModelScope.launch {
            try {
                if (localRepository.getProfileCount() == 0) {
                    localRepository.insertProfile(ProfileEntity(name = "Você", colorHex = "#0062FF"))
                    localRepository.insertProfile(ProfileEntity(name = "Parceiro(a)", colorHex = "#38BDF8"))
                    localRepository.insertProfile(ProfileEntity(name = "Conjunto", colorHex = "#818CF8"))
                }
                if (localRepository.getCategoryCount() == 0) {
                    localRepository.insertCategory(CategoryEntity(name = "Moradia", colorHex = "#0062FF"))
                    localRepository.insertCategory(CategoryEntity(name = "Alimentação", colorHex = "#F59E0B"))
                    localRepository.insertCategory(CategoryEntity(name = "Transporte", colorHex = "#6366F1"))
                    localRepository.insertCategory(CategoryEntity(name = "Assinaturas", colorHex = "#EC4899"))
                    localRepository.insertCategory(CategoryEntity(name = "Saúde", colorHex = "#10B981"))
                    localRepository.insertCategory(CategoryEntity(name = "Outros", colorHex = "#64748B"))
                }
            } catch (e: Exception) {
                Log.e("BillViewModel", "Erro ao inicializar dados locais: ${e.message}", e)
            }
        }

        // Observe Auth state and load space when user logs in with Google
        val auth = FirebaseAuth.getInstance()
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _userName.value = user?.displayName
            if (user != null) {
                viewModelScope.launch {
                    try {
                        val space = firestoreRepository.ensureUserAndSpace(user)
                        _currentSpace.value = space
                    } catch (e: Exception) {
                        Log.e("BillViewModel", "Erro ao carregar espaço do Firestore: ${e.message}", e)
                    }
                }
            } else {
                _currentSpace.value = null
            }
        }
    }

    // Bills stream: If connected to space, observe Firestore; else observe local Room
    private val rawBillsFlow = _currentSpace.flatMapLatest { space ->
        if (space != null) {
            firestoreRepository.observeBills(space.id)
        } else {
            localRepository.getAllBills().map { list ->
                list.map { entity ->
                    BillDocument(
                        id = entity.id.toString(),
                        name = entity.name,
                        amount = entity.amount,
                        dueDate = entity.dueDate,
                        monthYear = entity.monthYear,
                        category = entity.category,
                        isPaid = entity.isPaid,
                        profileName = entity.profileName
                    )
                }
            }
        }
    }

    // Profiles stream: If connected to space, observe Firestore; else observe local Room
    private val rawProfilesFlow = _currentSpace.flatMapLatest { space ->
        if (space != null) {
            firestoreRepository.observeProfiles(space.id)
        } else {
            localRepository.getAllProfiles().map { list ->
                list.map { entity ->
                    ProfileDocument(
                        id = entity.id.toString(),
                        name = entity.name,
                        colorHex = entity.colorHex
                    )
                }
            }
        }
    }

    // Categories stream: If connected to space, observe Firestore; else observe local Room
    private val rawCategoriesFlow = _currentSpace.flatMapLatest { space ->
        if (space != null) {
            firestoreRepository.observeCategories(space.id)
        } else {
            localRepository.getAllCategories().map { list ->
                list.map { entity ->
                    CategoryDocument(
                        id = entity.id.toString(),
                        name = entity.name,
                        colorHex = entity.colorHex
                    )
                }
            }
        }
    }

    private data class SpaceData(
        val bills: List<BillDocument>,
        val profiles: List<ProfileDocument>,
        val categories: List<CategoryDocument>
    )

    private val spaceDataFlow = combine(
        rawBillsFlow,
        rawProfilesFlow,
        rawCategoriesFlow
    ) { bills, profiles, categories ->
        SpaceData(bills, profiles, categories)
    }

    private val monthDataFlow = combine(
        _currentYearMonth,
        _selectedProfile,
        spaceDataFlow,
        _currentSpace
    ) { ym, profile, sData, space ->
        val ymStr = DateUtils.toYearMonthString(ym)
        sData.bills.forEach { doc ->
            val lid = doc.id.toLongOrNull() ?: doc.id.hashCode().toLong()
            billDocIdMap[lid] = doc.id
        }
        sData.profiles.forEach { doc ->
            val lid = doc.id.toLongOrNull() ?: doc.id.hashCode().toLong()
            profileDocIdMap[lid] = doc.id
        }
        sData.categories.forEach { doc ->
            val lid = doc.id.toLongOrNull() ?: doc.id.hashCode().toLong()
            categoryDocIdMap[lid] = doc.id
        }

        val monthBills = sData.bills
            .filter { it.monthYear == ymStr }
            .map { doc ->
                BillEntity(
                    id = doc.id.toLongOrNull() ?: doc.id.hashCode().toLong(),
                    name = doc.name,
                    amount = doc.amount,
                    dueDate = doc.dueDate,
                    category = doc.category,
                    isPaid = doc.isPaid,
                    monthYear = doc.monthYear,
                    profileName = doc.profileName.ifEmpty { "Conjunto" },
                    isRecurring = doc.isRecurring,
                    docId = doc.id
                )
            }

        val defaultProfiles = listOf(
            ProfileEntity(name = "Você", colorHex = "#0062FF"),
            ProfileEntity(name = "Parceiro(a)", colorHex = "#38BDF8"),
            ProfileEntity(name = "Conjunto", colorHex = "#818CF8")
        )
        val defaultCategories = listOf(
            CategoryEntity(name = "Moradia", colorHex = "#0062FF"),
            CategoryEntity(name = "Alimentação", colorHex = "#F59E0B"),
            CategoryEntity(name = "Transporte", colorHex = "#6366F1"),
            CategoryEntity(name = "Assinaturas", colorHex = "#EC4899"),
            CategoryEntity(name = "Saúde", colorHex = "#10B981"),
            CategoryEntity(name = "Outros", colorHex = "#64748B")
        )

        val profiles = sData.profiles.map { doc ->
            ProfileEntity(
                id = doc.id.toLongOrNull() ?: doc.id.hashCode().toLong(),
                name = doc.name,
                colorHex = doc.colorHex,
                docId = doc.id
            )
        }.ifEmpty { defaultProfiles }

        val categories = sData.categories.map { doc ->
            CategoryEntity(
                id = doc.id.toLongOrNull() ?: doc.id.hashCode().toLong(),
                name = doc.name,
                colorHex = doc.colorHex,
                docId = doc.id
            )
        }.ifEmpty { defaultCategories }

        MonthData(
            ym = ym,
            profile = profile,
            monthBills = monthBills,
            profiles = profiles,
            categories = categories,
            space = space
        )
    }

    private val dialogsDataFlow = combine(
        _isFormOpen,
        _isProfileManagerOpen,
        _isCategoryManagerOpen,
        _isSpaceDialogOpen,
        combine(_editingBill, _userName) { editing, name -> Pair(editing, name) }
    ) { formOpen, profileOpen, categoryOpen, spaceOpen, (editing, name) ->
        DialogsData(
            formOpen = formOpen,
            profileOpen = profileOpen,
            categoryOpen = categoryOpen,
            spaceOpen = spaceOpen,
            editingBill = editing,
            billToDelete = _billToDelete.value,
            billToRepeat = _billToRepeat.value,
            userName = name
        )
    }

    val uiState: StateFlow<BillsUiState> = combine(
        monthDataFlow,
        _isDarkMode,
        dialogsDataFlow,
        _billToDelete,
        _billToRepeat
    ) { data, isDark, dialogs, toDelete, toRepeat ->
        val filteredBills = if (data.profile == "Todos") {
            data.monthBills
        } else {
            data.monthBills.filter { it.profileName == data.profile }
        }

        val totalMonth = filteredBills.sumOf { it.amount }
        val totalPaid = filteredBills.filter { it.isPaid }.sumOf { it.amount }
        val remainingToPay = (totalMonth - totalPaid).coerceAtLeast(0.0)
        val progressFraction = if (totalMonth > 0.0) {
            (totalPaid / totalMonth).toFloat().coerceIn(0f, 1f)
        } else {
            0f
        }
        val progressPercent = (progressFraction * 100).toInt()

        val categoryExpenses = filteredBills
            .groupBy { it.category }
            .map { (catName, billsInCat) ->
                val sum = billsInCat.sumOf { it.amount }
                val percentage = if (totalMonth > 0.0) (sum / totalMonth).toFloat() else 0f
                CategoryExpense(
                    name = catName,
                    totalAmount = sum,
                    percentageOfTotal = percentage,
                    count = billsInCat.size
                )
            }
            .sortedByDescending { it.totalAmount }

        val dueIn10Days = filteredBills.filter { DateUtils.isDueWithin10Days(it.dueDate, it.isPaid) }
            .sortedBy { it.dueDate }
        val dueIn10DaysTotal = dueIn10Days.sumOf { it.amount }

        val summary = MonthSummary(
            totalMonth = totalMonth,
            totalPaid = totalPaid,
            remainingToPay = remainingToPay,
            progressFraction = progressFraction,
            progressPercent = progressPercent,
            categoryExpenses = categoryExpenses,
            totalBillsCount = filteredBills.size,
            paidBillsCount = filteredBills.count { it.isPaid },
            pendingBillsCount = filteredBills.count { !it.isPaid },
            dueIn10DaysBills = dueIn10Days,
            dueIn10DaysAmount = dueIn10DaysTotal
        )

        BillsUiState(
            currentYearMonth = data.ym,
            selectedProfile = data.profile,
            bills = filteredBills,
            allMonthBills = data.monthBills,
            summary = summary,
            profiles = data.profiles,
            categories = data.categories,
            currentSpace = data.space,
            isGuestMode = data.space == null,
            isDarkMode = isDark,
            isFormOpen = dialogs.formOpen,
            isProfileManagerOpen = dialogs.profileOpen,
            isCategoryManagerOpen = dialogs.categoryOpen,
            isSpaceDialogOpen = dialogs.spaceOpen,
            isJoiningSpace = _isJoiningSpace.value,
            spaceJoinError = _spaceJoinError.value,
            editingBill = dialogs.editingBill,
            billToDelete = toDelete,
            billToRepeat = toRepeat,
            profileToDelete = _profileToDelete.value,
            categoryToDelete = _categoryToDelete.value,
            userName = dialogs.userName
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BillsUiState()
    )

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun selectProfileFilter(profile: String) {
        _selectedProfile.value = profile
    }

    fun navigatePreviousMonth() {
        _currentYearMonth.value = _currentYearMonth.value.minusMonths(1)
    }

    fun navigateNextMonth() {
        _currentYearMonth.value = _currentYearMonth.value.plusMonths(1)
    }

    fun jumpToCurrentMonth() {
        _currentYearMonth.value = YearMonth.now()
    }

    fun setYearMonth(ym: YearMonth) {
        _currentYearMonth.value = ym
    }

    fun toggleBillPaid(bill: BillEntity) {
        val space = _currentSpace.value
        val docId = bill.docId.ifBlank { billDocIdMap[bill.id] ?: bill.id.toString() }
        viewModelScope.launch {
            if (space != null) {
                try {
                    firestoreRepository.toggleBillPaid(space.id, docId, bill.isPaid)
                } catch (e: Exception) {
                    Log.e("BillViewModel", "Erro ao alternar pago no Firestore: ${e.message}", e)
                }
            } else {
                try {
                    localRepository.togglePaidStatus(bill.id, !bill.isPaid)
                } catch (e: Exception) {
                    Log.e("BillViewModel", "Erro ao alternar pago localmente: ${e.message}", e)
                }
            }
        }
    }

    fun checkAndFireNotifications() {
        val dueBills = uiState.value.summary.dueIn10DaysBills
        if (dueBills.isNotEmpty()) {
            NotificationHelper.notifyDueBills(getApplication(), dueBills)
        }
    }

    fun openNewBillForm() {
        _editingBill.value = null
        _isFormOpen.value = true
    }

    fun openEditBillForm(bill: BillEntity) {
        _editingBill.value = bill
        _isFormOpen.value = true
    }

    fun closeForm() {
        _isFormOpen.value = false
        _editingBill.value = null
    }

    fun requestDeleteConfirmation(bill: BillEntity) {
        _billToDelete.value = bill
    }

    fun dismissDeleteConfirmation() {
        _billToDelete.value = null
    }

    fun confirmDeleteBill() {
        val bill = _billToDelete.value ?: return
        val space = _currentSpace.value
        val docId = bill.docId.ifBlank { billDocIdMap[bill.id] ?: bill.id.toString() }
        viewModelScope.launch {
            if (space != null) {
                try {
                    firestoreRepository.deleteBill(space.id, docId)
                } catch (e: Exception) {
                    Log.e("BillViewModel", "Erro ao excluir conta no Firestore: ${e.message}", e)
                }
            } else {
                try {
                    localRepository.deleteBillById(bill.id)
                } catch (e: Exception) {
                    Log.e("BillViewModel", "Erro ao excluir conta localmente: ${e.message}", e)
                }
            }
            _billToDelete.value = null
            if (_editingBill.value?.id == bill.id) {
                closeForm()
            }
        }
    }

    fun openRepeatBillDialog(bill: BillEntity) {
        _billToRepeat.value = bill
    }

    fun closeRepeatBillDialog() {
        _billToRepeat.value = null
    }

    fun confirmRepeatBill(bill: BillEntity, monthsCount: Int) {
        val space = _currentSpace.value
        val originalDay: Int? = try {
            if (bill.dueDate.isNotBlank() && bill.dueDate != "SEM_VENCIMENTO") {
                val parsed = LocalDate.parse(bill.dueDate, DateUtils.isoFormatter)
                parsed.dayOfMonth
            } else null
        } catch (e: Exception) {
            null
        }
        val baseYearMonth: YearMonth = try {
            if (bill.dueDate.isNotBlank() && bill.dueDate != "SEM_VENCIMENTO") {
                val parsed = LocalDate.parse(bill.dueDate, DateUtils.isoFormatter)
                YearMonth.of(parsed.year, parsed.month)
            } else {
                DateUtils.parseYearMonth(bill.monthYear)
            }
        } catch (e: Exception) {
            _currentYearMonth.value
        }

        viewModelScope.launch {
            if (space != null) {
                try {
                    val allCurrentBills = spaceDataFlow.first().bills
                    for (m in 1..monthsCount) {
                        val targetMonth = baseYearMonth.plusMonths(m.toLong())
                        val targetMonthYear = DateUtils.toYearMonthString(targetMonth)
                        val targetDueDate = if (originalDay != null) {
                            val targetDay = originalDay.coerceAtMost(targetMonth.lengthOfMonth())
                            targetMonth.atDay(targetDay).format(DateUtils.isoFormatter)
                        } else ""

                        val alreadyPresent = allCurrentBills.any {
                            it.monthYear == targetMonthYear && it.name.trim().equals(bill.name.trim(), ignoreCase = true)
                        }
                        if (!alreadyPresent) {
                            firestoreRepository.saveBill(
                                spaceId = space.id,
                                billId = null,
                                name = bill.name.trim(),
                                amount = bill.amount,
                                dueDate = targetDueDate,
                                monthYear = targetMonthYear,
                                category = bill.category,
                                profileName = bill.profileName,
                                isPaid = false,
                                isRecurring = true,
                                memberUids = space.memberUids
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e("BillViewModel", "Erro ao replicar no Firestore: ${e.message}", e)
                }
            } else {
                if (!bill.isRecurring) {
                    try {
                        localRepository.updateBill(bill.copy(isRecurring = true))
                    } catch (e: Exception) {
                        Log.e("BillViewModel", "Erro ao atualizar recorrente: ${e.message}", e)
                    }
                }
                for (m in 1..monthsCount) {
                    val targetMonth = baseYearMonth.plusMonths(m.toLong())
                    val targetMonthYear = DateUtils.toYearMonthString(targetMonth)
                    val targetDueDate = if (originalDay != null) {
                        val targetDay = originalDay.coerceAtMost(targetMonth.lengthOfMonth())
                        targetMonth.atDay(targetDay).format(DateUtils.isoFormatter)
                    } else ""

                    try {
                        val existingInMonth = localRepository.getBillsListForMonth(targetMonthYear)
                        val alreadyPresent = existingInMonth.any {
                            it.name.trim().equals(bill.name.trim(), ignoreCase = true)
                        }
                        if (!alreadyPresent) {
                            val newEntity = BillEntity(
                                id = 0L,
                                name = bill.name.trim(),
                                amount = bill.amount,
                                dueDate = targetDueDate,
                                category = bill.category,
                                profileName = bill.profileName,
                                isPaid = false,
                                isRecurring = true,
                                monthYear = targetMonthYear
                            )
                            localRepository.insertBill(newEntity)
                        }
                    } catch (e: Exception) {
                        Log.e("BillViewModel", "Erro ao replicar conta localmente: ${e.message}", e)
                    }
                }
            }
            closeRepeatBillDialog()
        }
    }

    fun copyBillsFromPreviousMonth() {
        val previousMonth = _currentYearMonth.value.minusMonths(1)
        val prevMonthStr = DateUtils.toYearMonthString(previousMonth)
        val currentMonthStr = DateUtils.toYearMonthString(_currentYearMonth.value)

        viewModelScope.launch {
            val existingNames = uiState.value.allMonthBills.map { it.name.trim().lowercase() }.toSet()
            try {
                val prevBills = localRepository.getBillsListForMonth(prevMonthStr)
                val billsToInsert = prevBills.filter { it.name.trim().lowercase() !in existingNames }
                    .map { prev ->
                        val newDueDate = DateUtils.calculateNextMonthDueDate(prev.dueDate, 1L)
                        prev.copy(
                            id = 0L,
                            dueDate = newDueDate,
                            monthYear = currentMonthStr,
                            isPaid = false,
                            isRecurring = true,
                            createdAt = System.currentTimeMillis()
                        )
                    }
                if (billsToInsert.isNotEmpty()) {
                    localRepository.insertBills(billsToInsert)
                }
            } catch (e: Exception) {
                Log.e("BillViewModel", "Erro ao copiar contas do mês anterior: ${e.message}", e)
            }
        }
    }

    fun saveBill(
        id: Long = 0,
        name: String,
        amount: Double,
        dueDate: String,
        category: String,
        profileName: String,
        isPaid: Boolean,
        isRecurring: Boolean = false,
        repeatMonths: Int = 0
    ) {
        val space = _currentSpace.value
        val baseYearMonth: YearMonth = try {
            if (dueDate.isNotBlank() && dueDate != "SEM_VENCIMENTO") {
                val parsed = LocalDate.parse(dueDate, DateUtils.isoFormatter)
                YearMonth.of(parsed.year, parsed.month)
            } else {
                _currentYearMonth.value
            }
        } catch (e: Exception) {
            _currentYearMonth.value
        }
        val monthYear = DateUtils.toYearMonthString(baseYearMonth)

        val originalDay: Int? = try {
            if (dueDate.isNotBlank() && dueDate != "SEM_VENCIMENTO") {
                val parsed = LocalDate.parse(dueDate, DateUtils.isoFormatter)
                parsed.dayOfMonth
            } else null
        } catch (e: Exception) {
            null
        }

        viewModelScope.launch {
            if (space != null) {
                try {
                    val billId = if (_editingBill.value != null) {
                        _editingBill.value!!.docId.ifBlank {
                            billDocIdMap[_editingBill.value!!.id] ?: _editingBill.value!!.id.toString()
                        }
                    } else null
                    firestoreRepository.saveBill(
                        spaceId = space.id,
                        billId = billId,
                        name = name.trim(),
                        amount = amount,
                        dueDate = dueDate,
                        monthYear = monthYear,
                        category = category,
                        profileName = profileName,
                        isPaid = isPaid,
                        isRecurring = isRecurring,
                        memberUids = space.memberUids
                    )
                    if (isRecurring && repeatMonths > 1) {
                        val allCurrentBills = spaceDataFlow.first().bills
                        for (offset in 1 until repeatMonths) {
                            val targetMonth = baseYearMonth.plusMonths(offset.toLong())
                            val targetMonthYear = DateUtils.toYearMonthString(targetMonth)
                            val targetDueDate = if (originalDay != null) {
                                val targetDay = originalDay.coerceAtMost(targetMonth.lengthOfMonth())
                                targetMonth.atDay(targetDay).format(DateUtils.isoFormatter)
                            } else {
                                ""
                            }
                            val alreadyPresent = allCurrentBills.any {
                                it.monthYear == targetMonthYear && it.name.trim().equals(name.trim(), ignoreCase = true)
                            }
                            if (!alreadyPresent) {
                                firestoreRepository.saveBill(
                                    spaceId = space.id,
                                    billId = null,
                                    name = name.trim(),
                                    amount = amount,
                                    dueDate = targetDueDate,
                                    monthYear = targetMonthYear,
                                    category = category,
                                    profileName = profileName,
                                    isPaid = false,
                                    isRecurring = true,
                                    memberUids = space.memberUids
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("BillViewModel", "Erro ao salvar conta no Firestore: ${e.message}", e)
                }
            } else {
                try {
                    val entity = BillEntity(
                        id = _editingBill.value?.id ?: 0L,
                        name = name.trim(),
                        amount = amount,
                        dueDate = dueDate,
                        category = category,
                        profileName = profileName,
                        isPaid = isPaid,
                        isRecurring = isRecurring,
                        monthYear = monthYear
                    )
                    if (_editingBill.value != null && _editingBill.value!!.id != 0L) {
                        localRepository.updateBill(entity)
                    } else {
                        localRepository.insertBill(entity)
                    }

                    if (isRecurring && repeatMonths > 1) {
                        for (offset in 1 until repeatMonths) {
                            val targetMonth = baseYearMonth.plusMonths(offset.toLong())
                            val targetMonthYear = DateUtils.toYearMonthString(targetMonth)
                            val targetDueDate = if (originalDay != null) {
                                val targetDay = originalDay.coerceAtMost(targetMonth.lengthOfMonth())
                                targetMonth.atDay(targetDay).format(DateUtils.isoFormatter)
                            } else {
                                ""
                            }
                            val existingInMonth = localRepository.getBillsListForMonth(targetMonthYear)
                            val alreadyPresent = existingInMonth.any {
                                it.name.trim().equals(name.trim(), ignoreCase = true)
                            }
                            if (!alreadyPresent) {
                                val copyEntity = BillEntity(
                                    id = 0L,
                                    name = name.trim(),
                                    amount = amount,
                                    dueDate = targetDueDate,
                                    category = category,
                                    profileName = profileName,
                                    isPaid = false,
                                    isRecurring = true,
                                    monthYear = targetMonthYear
                                )
                                localRepository.insertBill(copyEntity)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("BillViewModel", "Erro ao salvar conta: ${e.message}", e)
                }
            }
            closeForm()
        }
    }

    // Profile Management
    fun openProfileManager() {
        _isProfileManagerOpen.value = true
    }

    fun closeProfileManager() {
        _isProfileManagerOpen.value = false
    }

    fun createProfile(name: String, colorHex: String = "#0062FF") {
        if (name.isBlank()) return
        val space = _currentSpace.value
        viewModelScope.launch {
            if (space != null) {
                try {
                    firestoreRepository.addProfile(space.id, name, colorHex, space.memberUids)
                } catch (e: Exception) {
                    Log.e("BillViewModel", "Erro ao criar perfil no Firestore: ${e.message}", e)
                }
            } else {
                try {
                    localRepository.insertProfile(ProfileEntity(name = name.trim(), colorHex = colorHex))
                } catch (e: Exception) {
                    Log.e("BillViewModel", "Erro ao criar perfil: ${e.message}", e)
                }
            }
        }
    }

    fun deleteProfile(profile: ProfileEntity) {
        val space = _currentSpace.value
        val docId = profile.docId.ifBlank { profileDocIdMap[profile.id] ?: "" }
        viewModelScope.launch {
            if (space != null) {
                try {
                    firestoreRepository.deleteProfile(space.id, docId, profile.name)
                } catch (e: Exception) {
                    Log.e("BillViewModel", "Erro ao excluir perfil no Firestore: ${e.message}", e)
                }
            } else {
                try {
                    localRepository.deleteProfile(profile)
                } catch (e: Exception) {
                    Log.e("BillViewModel", "Erro ao excluir perfil: ${e.message}", e)
                }
            }
            _profileToDelete.value = null
            if (_selectedProfile.value == profile.name) {
                _selectedProfile.value = "Todos"
            }
        }
    }

    fun requestDeleteProfile(profile: ProfileEntity) {
        _profileToDelete.value = profile
    }

    fun dismissDeleteProfile() {
        _profileToDelete.value = null
    }

    fun confirmDeleteProfile() {
        val profile = _profileToDelete.value ?: return
        deleteProfile(profile)
    }

    // Category Management
    fun openCategoryManager() {
        _isCategoryManagerOpen.value = true
    }

    fun closeCategoryManager() {
        _isCategoryManagerOpen.value = false
    }

    fun createCategory(name: String, colorHex: String = "#0062FF") {
        if (name.isBlank()) return
        val space = _currentSpace.value
        viewModelScope.launch {
            if (space != null) {
                try {
                    firestoreRepository.addCategory(space.id, name, colorHex, space.memberUids)
                } catch (e: Exception) {
                    Log.e("BillViewModel", "Erro ao criar categoria no Firestore: ${e.message}", e)
                }
            } else {
                try {
                    localRepository.insertCategory(CategoryEntity(name = name.trim(), colorHex = colorHex))
                } catch (e: Exception) {
                    Log.e("BillViewModel", "Erro ao criar categoria: ${e.message}", e)
                }
            }
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        val space = _currentSpace.value
        val docId = category.docId.ifBlank { categoryDocIdMap[category.id] ?: "" }
        viewModelScope.launch {
            if (space != null) {
                try {
                    firestoreRepository.deleteCategory(space.id, docId, category.name)
                } catch (e: Exception) {
                    Log.e("BillViewModel", "Erro ao excluir categoria no Firestore: ${e.message}", e)
                }
            } else {
                try {
                    localRepository.deleteCategory(category)
                } catch (e: Exception) {
                    Log.e("BillViewModel", "Erro ao excluir categoria: ${e.message}", e)
                }
            }
            _categoryToDelete.value = null
        }
    }

    fun requestDeleteCategory(category: CategoryEntity) {
        _categoryToDelete.value = category
    }

    fun dismissDeleteCategory() {
        _categoryToDelete.value = null
    }

    fun confirmDeleteCategory() {
        val cat = _categoryToDelete.value ?: return
        deleteCategory(cat)
    }

    // Space Sync Dialog
    fun openSpaceSyncDialog() {
        _spaceJoinError.value = null
        _isSpaceDialogOpen.value = true
    }

    fun closeSpaceSyncDialog() {
        _isSpaceDialogOpen.value = false
        _spaceJoinError.value = null
    }

    fun joinSpaceByCode(code: String) {
        if (code.isBlank()) return
        _isJoiningSpace.value = true
        _spaceJoinError.value = null
        viewModelScope.launch {
            try {
                val newSpace = firestoreRepository.joinSpaceByCode(code)
                _currentSpace.value = newSpace
                _isSpaceDialogOpen.value = false
            } catch (e: Exception) {
                _spaceJoinError.value = e.localizedMessage ?: "Código inválido."
            } finally {
                _isJoiningSpace.value = false
            }
        }
    }

    fun signOut(onSignedOut: () -> Unit = {}) {
        _userName.value = null
        _currentSpace.value = null
        FirebaseAuth.getInstance().signOut()
        onSignedOut()
    }
}
