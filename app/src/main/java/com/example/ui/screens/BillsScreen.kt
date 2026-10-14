package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.BillFormSheet
import com.example.ui.components.BillItemRow
import com.example.ui.components.CategoryDistributionCard
import com.example.ui.components.CategoryManagerDialog
import com.example.ui.components.DeleteConfirmDialog
import com.example.ui.components.DockTab
import com.example.ui.components.DueSoonAlertBanner
import com.example.ui.components.EmptyStateView
import com.example.ui.components.FloatingDockBar
import com.example.ui.components.HeroStackedCard
import com.example.ui.components.HeroSummaryCard
import com.example.ui.components.MonthPickerSheet
import com.example.ui.components.PillFilterRow
import com.example.ui.components.ProfileManagerDialog
import com.example.ui.components.ReferenceTopBar
import com.example.ui.components.RepeatBillDialog
import com.example.ui.components.SpaceSyncDialog
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.SpaceGroteskFamily
import com.example.ui.viewmodel.BillViewModel
import com.example.util.NotificationHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillsScreen(
    viewModel: BillViewModel,
    onSignOutRequested: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme

    var filterTab by remember { mutableStateOf("Todas") } // "Todas", "Pendentes", "Pagas"
    var showMonthPicker by remember { mutableStateOf(false) }
    var currentDockTab by remember { mutableStateOf(DockTab.HOME) }

    val pendingBills = uiState.bills.filter { !it.isPaid }
    val paidBills = uiState.bills.filter { it.isPaid }
    val displayedBills = when (filterTab) {
        "Pendentes" -> pendingBills
        "Pagas" -> paidBills
        else -> uiState.bills
    }

    // Notification permission launcher for Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.checkAndFireNotifications()
        }
    }

    LaunchedEffect(Unit) {
        NotificationHelper.createNotificationChannel(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!NotificationHelper.hasNotificationPermission(context)) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("bills_screen"),
        containerColor = colors.background,
        bottomBar = {
            // Floating Dock Bar matching the reference design
            FloatingDockBar(
                currentTab = currentDockTab,
                onSelectTab = { tab ->
                    currentDockTab = tab
                    when (tab) {
                        DockTab.HOME -> { /* already on home */ }
                        DockTab.CALENDAR -> { showMonthPicker = true }
                        DockTab.CATEGORIES -> { viewModel.openCategoryManager() }
                        DockTab.PROFILES -> { viewModel.openProfileManager() }
                    }
                },
                onAddNewBill = { viewModel.openNewBillForm() }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                // 1. Reference Top Bar with Avatar, Verified badge, Action buttons & Greeting
                ReferenceTopBar(
                    currentYearMonth = uiState.currentYearMonth,
                    isDarkMode = uiState.isDarkMode,
                    onToggleDarkMode = { viewModel.toggleDarkMode() },
                    onOpenMonthPicker = { showMonthPicker = true },
                    onCheckNotifications = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            !NotificationHelper.hasNotificationPermission(context)
                        ) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            viewModel.checkAndFireNotifications()
                        }
                    },
                    onOpenSpaceSync = { viewModel.openSpaceSyncDialog() },
                    hasDueSoon = uiState.summary.dueIn10DaysBills.isNotEmpty(),
                    userName = uiState.userName
                )
            }

            // 2. Pill Filter Row (Matching "All 6", "Today's tasks 3" from Reference)
            item {
                PillFilterRow(
                    selectedTab = filterTab,
                    totalCount = uiState.bills.size,
                    pendingCount = pendingBills.size,
                    paidCount = paidBills.size,
                    onSelectTab = { filterTab = it },
                    selectedProfile = uiState.selectedProfile,
                    profiles = uiState.profiles,
                    onSelectProfile = { viewModel.selectProfileFilter(it) },
                    onManageProfiles = { viewModel.openProfileManager() }
                )
            }

            // 3. Focal Stacked Hero Card (Centerpiece matching reference design!)
            item {
                HeroStackedCard(
                    bills = uiState.bills,
                    onTogglePaid = { viewModel.toggleBillPaid(it) },
                    onClickEdit = { viewModel.openEditBillForm(it) },
                    onAddNew = { viewModel.openNewBillForm() }
                )
            }

            // 4. Due Soon Alert Banner (if any bills are due in 10 days)
            if (uiState.summary.dueIn10DaysBills.isNotEmpty()) {
                item {
                    DueSoonAlertBanner(
                        dueBills = uiState.summary.dueIn10DaysBills,
                        onTriggerNotification = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                !NotificationHelper.hasNotificationPermission(context)
                            ) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                viewModel.checkAndFireNotifications()
                            }
                        }
                    )
                }
            }

            // 5. Monthly Financial Breakdown Hero Card
            item {
                HeroSummaryCard(summary = uiState.summary)
            }

            // 6. Bills List Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CONTAS DO MÊS (${displayedBills.size})",
                        color = colors.onSurface,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SpaceGroteskFamily,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.copyBillsFromPreviousMonth() }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                            .testTag("copy_prev_month_shortcut"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Copiar mês ant.",
                            color = ElectricBlue,
                            fontSize = 11.sp,
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 7. Bills List or Empty State
            if (displayedBills.isEmpty()) {
                item {
                    EmptyStateView(
                        onAddBill = { viewModel.openNewBillForm() },
                        onCopyPreviousMonth = { viewModel.copyBillsFromPreviousMonth() }
                    )
                }
            } else {
                items(
                    items = displayedBills,
                    key = { it.id }
                ) { bill ->
                    BillItemRow(
                        bill = bill,
                        onTogglePaid = { viewModel.toggleBillPaid(it) },
                        onClickEdit = { viewModel.openEditBillForm(it) },
                        onRepeatBill = { viewModel.openRepeatBillDialog(it) }
                    )
                }
            }

            // 8. Expandable Category Distribution Card
            if (uiState.bills.isNotEmpty()) {
                item {
                    CategoryDistributionCard(
                        categoryExpenses = uiState.summary.categoryExpenses,
                        onManageCategories = { viewModel.openCategoryManager() }
                    )
                }
            }

            // Bottom clearance for floating dock
            item {
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }

    // Modal Form Sheet for Add / Edit Bill
    if (uiState.isFormOpen) {
        BillFormSheet(
            sheetState = sheetState,
            editingBill = uiState.editingBill,
            currentYearMonth = uiState.currentYearMonth,
            categories = uiState.categories,
            profiles = uiState.profiles,
            onDismiss = {
                scope.launch {
                    sheetState.hide()
                    viewModel.closeForm()
                }
            },
            onOpenProfileManager = { viewModel.openProfileManager() },
            onSave = { id, name, amount, dueDate, category, profileName, isPaid, isRecurring, repeatMonths ->
                viewModel.saveBill(id, name, amount, dueDate, category, profileName, isPaid, isRecurring, repeatMonths)
            },
            onDeleteRequest = { bill ->
                viewModel.requestDeleteConfirmation(bill)
            }
        )
    }

    // Month Picker Bottom Sheet
    if (showMonthPicker) {
        MonthPickerSheet(
            currentYearMonth = uiState.currentYearMonth,
            onSelectYearMonth = { ym -> viewModel.setYearMonth(ym) },
            onDismiss = { showMonthPicker = false }
        )
    }

    // Repeat Bill Dialog for replicating across months
    uiState.billToRepeat?.let { bill ->
        RepeatBillDialog(
            bill = bill,
            onConfirm = { monthsCount ->
                viewModel.confirmRepeatBill(bill, monthsCount)
            },
            onDismiss = {
                viewModel.closeRepeatBillDialog()
            }
        )
    }

    // Profile Manager Dialog
    if (uiState.isProfileManagerOpen) {
        ProfileManagerDialog(
            profiles = uiState.profiles,
            onDismiss = { viewModel.closeProfileManager() },
            onCreateProfile = { name, color -> viewModel.createProfile(name, color) },
            onDeleteProfile = { p -> viewModel.deleteProfile(p) }
        )
    }

    // Category Manager Dialog
    if (uiState.isCategoryManagerOpen) {
        CategoryManagerDialog(
            categories = uiState.categories,
            onDismiss = { viewModel.closeCategoryManager() },
            onCreateCategory = { name, color -> viewModel.createCategory(name, color) },
            onDeleteCategory = { c -> viewModel.deleteCategory(c) }
        )
    }

    // Space & Cloud Sync Dialog
    if (uiState.isSpaceDialogOpen) {
        SpaceSyncDialog(
            space = uiState.currentSpace,
            onDismiss = { viewModel.closeSpaceSyncDialog() },
            onJoinSpace = { code -> viewModel.joinSpaceByCode(code) },
            onSignOut = {
                viewModel.closeSpaceSyncDialog()
                onSignOutRequested()
            },
            isJoining = uiState.isJoiningSpace,
            joinError = uiState.spaceJoinError
        )
    }

    // Delete bill confirmation dialog
    uiState.billToDelete?.let { bill ->
        DeleteConfirmDialog(
            bill = bill,
            onConfirm = { viewModel.confirmDeleteBill() },
            onDismiss = { viewModel.dismissDeleteConfirmation() }
        )
    }

    // Delete profile confirmation dialog (if requested externally)
    uiState.profileToDelete?.let { profile ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteProfile() },
            containerColor = colors.surface,
            title = {
                Text(
                    text = "Excluir Perfil",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = colors.onSurface
                )
            },
            text = {
                Text(
                    text = "Deseja realmente excluir o perfil \"${profile.name}\"?",
                    color = colors.onSurfaceVariant,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                androidx.compose.material3.Button(
                    onClick = { viewModel.confirmDeleteProfile() },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.ErrorRose)
                ) {
                    Text("Excluir", color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { viewModel.dismissDeleteProfile() }) {
                    Text("Cancelar", color = colors.onSurfaceVariant)
                }
            }
        )
    }

    // Delete category confirmation dialog (if requested externally)
    uiState.categoryToDelete?.let { cat ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteCategory() },
            containerColor = colors.surface,
            title = {
                Text(
                    text = "Excluir Categoria",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = colors.onSurface
                )
            },
            text = {
                Text(
                    text = "Deseja realmente excluir a categoria \"${cat.name}\"?",
                    color = colors.onSurfaceVariant,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                androidx.compose.material3.Button(
                    onClick = { viewModel.confirmDeleteCategory() },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.ErrorRose)
                ) {
                    Text("Excluir", color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { viewModel.dismissDeleteCategory() }) {
                    Text("Cancelar", color = colors.onSurfaceVariant)
                }
            }
        )
    }
}
