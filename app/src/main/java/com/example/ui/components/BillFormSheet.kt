package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BillEntity
import com.example.data.local.CategoryEntity
import com.example.data.local.ProfileEntity
import com.example.ui.theme.ErrorRose
import com.example.ui.theme.OnAccentBlue
import com.example.util.CurrencyFormatter
import com.example.util.DateUtils
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillFormSheet(
    sheetState: SheetState,
    editingBill: BillEntity?,
    currentYearMonth: YearMonth,
    categories: List<CategoryEntity>,
    profiles: List<ProfileEntity>,
    onDismiss: () -> Unit,
    onOpenProfileManager: () -> Unit = {},
    onSave: (id: Long, name: String, amount: Double, dueDate: String, category: String, profileName: String, isPaid: Boolean, isRecurring: Boolean, repeatMonths: Int) -> Unit,
    onDeleteRequest: (BillEntity) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val defaultDate = run {
        val now = LocalDate.now()
        if (now.year == currentYearMonth.year && now.month == currentYearMonth.month) {
            now.format(DateUtils.isoFormatter)
        } else {
            currentYearMonth.atDay(10).format(DateUtils.isoFormatter)
        }
    }
    val initialDate = editingBill?.dueDate?.takeIf { it.isNotBlank() && it != "SEM_VENCIMENTO" } ?: defaultDate

    var name by remember { mutableStateOf(editingBill?.name ?: "") }
    var amountText by remember {
        mutableStateOf(
            if (editingBill != null) {
                String.format(java.util.Locale("pt", "BR"), "%.2f", editingBill.amount).replace(".", ",")
            } else ""
        )
    }
    var isNoDueDate by remember {
        mutableStateOf(editingBill != null && (editingBill.dueDate.isBlank() || editingBill.dueDate == "SEM_VENCIMENTO"))
    }
    var dueDate by remember { mutableStateOf(if (isNoDueDate) "" else initialDate) }
    var selectedCategory by remember { mutableStateOf(editingBill?.category ?: (categories.firstOrNull()?.name ?: "Outros")) }
    var isNewCategoryMode by remember { mutableStateOf(false) }
    var newCategoryText by remember { mutableStateOf("") }
    var selectedProfile by remember { mutableStateOf(editingBill?.profileName ?: (profiles.firstOrNull()?.name ?: "Conjunto")) }
    var isPaid by remember { mutableStateOf(editingBill?.isPaid ?: false) }
    var isRecurring by remember { mutableStateOf(editingBill?.isRecurring ?: false) }
    var repeatMonths by remember { mutableIntStateOf(3) }
    var isCustomRepeat by remember { mutableStateOf(false) }
    var customRepeatText by remember { mutableStateOf("") }
    var customRepeatError by remember { mutableStateOf<String?>(null) }

    var showDatePicker by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val isEditing = editingBill != null

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val dismissWithKeyboard: () -> Unit = {
        focusManager.clearFocus()
        keyboardController?.hide()
        onDismiss()
    }

    ModalBottomSheet(
        onDismissRequest = dismissWithKeyboard,
        sheetState = sheetState,
        containerColor = colors.surface,
        dragHandle = null,
        modifier = Modifier.testTag("bill_form_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 20.dp)
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            // Header: Title & Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEditing) "Editar conta" else "Nova conta",
                    color = colors.onSurface,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = dismissWithKeyboard,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fechar",
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Nome da conta
            Text(
                text = "Nome da conta",
                color = colors.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    errorMessage = null
                },
                placeholder = { Text("Ex: Aluguel, Internet, Cartão", color = colors.onSurfaceVariant) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                colors = outlinedTextFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bill_name_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Valor
            Text(
                text = "Valor",
                color = colors.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = amountText,
                onValueChange = { input ->
                    val filtered = input.filter { it.isDigit() || it == ',' || it == '.' }
                    amountText = filtered
                    errorMessage = null
                },
                leadingIcon = {
                    Text(
                        text = "R$",
                        color = colors.primary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                },
                placeholder = { Text("0,00", color = colors.onSurfaceVariant) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = outlinedTextFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bill_amount_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Responsável (Perfil)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Responsável / Perfil",
                    color = colors.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "+ Criar perfil",
                    color = colors.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable { onOpenProfileManager() }
                        .padding(4.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                profiles.forEach { p ->
                    val isSelected = selectedProfile == p.name
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(if (isSelected) colors.primary else colors.surfaceVariant)
                            .border(
                                1.dp,
                                if (isSelected) colors.primary else colors.outline,
                                RoundedCornerShape(100.dp)
                            )
                            .clickable { selectedProfile = p.name }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = p.name,
                            color = if (isSelected) OnAccentBlue else colors.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Vencimento
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Vencimento",
                    color = colors.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            isNoDueDate = !isNoDueDate
                            if (isNoDueDate) {
                                dueDate = ""
                            } else if (dueDate.isBlank()) {
                                dueDate = initialDate
                            }
                        }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .testTag("toggle_no_due_date_button")
                ) {
                    Text(
                        text = "Conta sem vencimento",
                        color = if (isNoDueDate) colors.primary else colors.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = if (isNoDueDate) FontWeight.Bold else FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = isNoDueDate,
                        onCheckedChange = { checked ->
                            isNoDueDate = checked
                            if (checked) {
                                dueDate = ""
                            } else if (dueDate.isBlank()) {
                                dueDate = initialDate
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = colors.primary
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))

            if (!isNoDueDate) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surfaceVariant)
                        .border(1.dp, colors.outline, RoundedCornerShape(12.dp))
                        .clickable { showDatePicker = true }
                        .padding(horizontal = 14.dp, vertical = 14.dp)
                        .testTag("bill_due_date_picker_button"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = try {
                            val parsed = LocalDate.parse(dueDate, DateUtils.isoFormatter)
                            parsed.format(DateUtils.displayFullDateFormatter)
                        } catch (e: Exception) {
                            if (dueDate.isBlank()) "Selecionar data de vencimento" else dueDate
                        },
                        color = colors.onSurface,
                        fontSize = 14.sp
                    )
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Selecionar data",
                        tint = colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surfaceVariant.copy(alpha = 0.5f))
                        .border(1.dp, colors.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✓ Conta sem vencimento definido (não será considerada atrasada nem gerará notificações).",
                        color = colors.onSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Categoria
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Categoria",
                    color = colors.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (isNewCategoryMode) "Escolher existente" else "+ Nova categoria",
                    color = colors.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable { isNewCategoryMode = !isNewCategoryMode }
                        .padding(4.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (isNewCategoryMode) {
                OutlinedTextField(
                    value = newCategoryText,
                    onValueChange = { newCategoryText = it },
                    placeholder = { Text("Nome da nova categoria", color = colors.onSurfaceVariant) },
                    singleLine = true,
                    colors = outlinedTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_category_input")
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory == cat.name
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(if (isSelected) colors.primary else colors.surfaceVariant)
                                .border(
                                    1.dp,
                                    if (isSelected) colors.primary else colors.outline,
                                    RoundedCornerShape(100.dp)
                                )
                                .clickable { selectedCategory = cat.name }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                                .testTag("category_chip_${cat.name}")
                        ) {
                            Text(
                                text = cat.name,
                                color = if (isSelected) OnAccentBlue else colors.onSurfaceVariant,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Situação: Já está paga checkbox
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surfaceVariant)
                    .border(1.dp, colors.outline, RoundedCornerShape(12.dp))
                    .clickable { isPaid = !isPaid }
                    .padding(horizontal = 14.dp, vertical = 12.dp)
                    .testTag("is_paid_checkbox_row"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (isPaid) colors.primary else Color.Transparent)
                        .border(
                            1.5.dp,
                            if (isPaid) colors.primary else colors.onSurfaceVariant,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isPaid) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Já está paga",
                        color = colors.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (isPaid) "Marcada como paga no mês" else "Pendente de pagamento",
                        color = colors.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Repetição / Recorrência de conta
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surfaceVariant)
                    .border(
                        1.dp,
                        if (isRecurring) colors.primary.copy(alpha = 0.5f) else colors.outline,
                        RoundedCornerShape(14.dp)
                    )
                    .padding(14.dp)
                    .testTag("recurring_bill_section")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (isRecurring) colors.primary.copy(alpha = 0.2f) else colors.outline.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = null,
                                tint = if (isRecurring) colors.primary else colors.onSurfaceVariant,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Repetir nos outros meses",
                                color = colors.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (isRecurring) "Conta fixa em múltiplos meses" else "Cadastrar apenas neste mês",
                                color = colors.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Switch(
                        checked = isRecurring,
                        onCheckedChange = { isRecurring = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = colors.primary
                        ),
                        modifier = Modifier.testTag("recurring_switch")
                    )
                }

                if (isRecurring) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Repetir mensalmente por:",
                        color = colors.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        (1..12).forEach { count ->
                            val isSelected = !isCustomRepeat && repeatMonths == count
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) colors.primary else colors.surface)
                                    .border(
                                        1.dp,
                                        if (isSelected) colors.primary else colors.outline,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        isCustomRepeat = false
                                        customRepeatError = null
                                        repeatMonths = count
                                    }
                                    .padding(horizontal = 12.dp, vertical = 9.dp)
                                    .testTag("repeat_chip_$count"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (count == 1) "1 mês" else "$count meses",
                                    color = if (isSelected) OnAccentBlue else colors.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }

                        // Mais de 12 meses option
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isCustomRepeat) colors.primary else colors.surface)
                                .border(
                                    1.dp,
                                    if (isCustomRepeat) colors.primary else colors.outline,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    isCustomRepeat = true
                                    if (customRepeatText.isBlank()) {
                                        customRepeatText = "18"
                                        repeatMonths = 18
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 9.dp)
                                .testTag("repeat_chip_more_than_12"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Mais de 12 meses",
                                color = if (isCustomRepeat) OnAccentBlue else colors.onSurfaceVariant,
                                fontSize = 12.sp,
                                fontWeight = if (isCustomRepeat) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }

                    if (isCustomRepeat) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Digite a quantidade de meses:",
                            color = colors.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = customRepeatText,
                            onValueChange = { input ->
                                val digits = input.filter { it.isDigit() }
                                customRepeatText = digits
                                val num = digits.toIntOrNull()
                                if (num != null && num > 12) {
                                    repeatMonths = num
                                    customRepeatError = null
                                } else if (digits.isNotBlank()) {
                                    customRepeatError = "Digite um número inteiro superior a 12."
                                } else {
                                    customRepeatError = "Campo obrigatório."
                                }
                            },
                            placeholder = { Text("Ex: 18, 24, 36 meses", color = colors.onSurfaceVariant) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = outlinedTextFieldColors(),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_repeat_months_input")
                        )
                        if (customRepeatError != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = customRepeatError!!,
                                color = ErrorRose,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    val previewCount = if (isCustomRepeat) (customRepeatText.toIntOrNull() ?: repeatMonths) else repeatMonths
                    val baseYm = try {
                        if (dueDate.isNotBlank() && dueDate != "SEM_VENCIMENTO") {
                            val parsed = LocalDate.parse(dueDate, DateUtils.isoFormatter)
                            YearMonth.of(parsed.year, parsed.month)
                        } else currentYearMonth
                    } catch (e: Exception) {
                        currentYearMonth
                    }
                    val endYm = baseYm.plusMonths((previewCount - 1).coerceAtLeast(0).toLong())
                    Text(
                        text = "Total de $previewCount ocorrências mensais (de ${DateUtils.formatMonthYearHeader(baseYm)} até ${DateUtils.formatMonthYearHeader(endYm)})${if (isNoDueDate) " sem vencimento." else " no mesmo dia de vencimento."}",
                        color = colors.primary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = errorMessage ?: "",
                    color = ErrorRose,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botão Salvar Conta
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "Informe o nome da conta."
                        return@Button
                    }
                    val parsedAmount = CurrencyFormatter.parseAmount(amountText)
                    if (parsedAmount <= 0.0) {
                        errorMessage = "Informe um valor válido maior que zero."
                        return@Button
                    }
                    if (isRecurring && isCustomRepeat) {
                        val customNum = customRepeatText.toIntOrNull()
                        if (customNum == null || customNum <= 12) {
                            errorMessage = "Para mais de 12 meses, digite um número inteiro superior a 12."
                            return@Button
                        }
                        repeatMonths = customNum
                    }
                    val finalCategory = if (isNewCategoryMode && newCategoryText.isNotBlank()) {
                        newCategoryText.trim()
                    } else {
                        selectedCategory
                    }

                    focusManager.clearFocus()
                    keyboardController?.hide()

                    val totalOccurrences = if (isRecurring) {
                        if (isCustomRepeat) (customRepeatText.toIntOrNull() ?: repeatMonths) else repeatMonths
                    } else 0

                    onSave(
                        editingBill?.id ?: 0L,
                        name.trim(),
                        parsedAmount,
                        if (isNoDueDate) "" else dueDate,
                        finalCategory,
                        selectedProfile,
                        isPaid,
                        isRecurring,
                        totalOccurrences
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = OnAccentBlue
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_bill_button")
            ) {
                Text(
                    text = "Salvar conta",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Se em edição: Botão Excluir
            if (isEditing && editingBill != null) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        onDeleteRequest(editingBill)
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ErrorRose
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(ErrorRose.copy(alpha = 0.4f))
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("delete_bill_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Excluir conta",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Material 3 Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = try {
                val localDate = LocalDate.parse(dueDate, DateUtils.isoFormatter)
                localDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
            } catch (e: Exception) {
                System.currentTimeMillis()
            }
        )
        val confirmEnabled by remember {
            derivedStateOf { datePickerState.selectedDateMillis != null }
        }

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedLocalDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                            dueDate = selectedLocalDate.format(DateUtils.isoFormatter)
                        }
                        showDatePicker = false
                    },
                    enabled = confirmEnabled
                ) {
                    Text("Confirmar", color = colors.primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar", color = colors.onSurfaceVariant)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun outlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    cursorColor = MaterialTheme.colorScheme.primary
)
