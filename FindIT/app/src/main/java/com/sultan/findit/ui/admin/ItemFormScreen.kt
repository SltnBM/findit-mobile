package com.sultan.findit.ui.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.sultan.findit.data.repository.ItemFormData
import com.sultan.findit.viewmodel.ItemViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemFormScreen(
    itemId: Int?,
    itemViewModel: ItemViewModel,
    onBack: () -> Unit
) {
    val isEditMode = itemId != null && itemId > 0
    val localItems by itemViewModel.localItems.collectAsState()
    val editingItem = remember(localItems, itemId) {
        if (isEditMode) localItems.find { it.id == itemId } else null
    }

    var categoryId by rememberSaveable { mutableIntStateOf(0) }
    var name by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var foundLocation by rememberSaveable { mutableStateOf("") }
    var foundDate by rememberSaveable { mutableStateOf("") }
    var pickupLocation by rememberSaveable { mutableStateOf("") }
    var characteristics by rememberSaveable { mutableStateOf("") }
    var adminNote by rememberSaveable { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isFormSubmitted by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            photoUri = uri
            localError = null
        }
    }

    LaunchedEffect(itemId) {
        itemViewModel.loadCategories()
        if (isEditMode) {
            itemViewModel.loadAdminItems()
        }
    }

    LaunchedEffect(editingItem?.id) {
        if (editingItem != null) {
            categoryId = editingItem.categoryId ?: 0
            name = editingItem.name
            description = editingItem.description
            foundLocation = editingItem.foundLocation
            foundDate = editingItem.foundDate
            pickupLocation = editingItem.pickupLocation
            characteristics = editingItem.characteristics.orEmpty()
            adminNote = editingItem.adminNote.orEmpty()
        }
    }

    val selectedCategoryName = itemViewModel.categories
        .find { it.id == categoryId }
        ?.name ?: "Pilih Kategori Barang"

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()

        AlertDialog(
            onDismissRequest = { showDatePicker = false },
            modifier = Modifier.fillMaxWidth(0.95f),
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            title = null,
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    DatePicker(
                        state = datePickerState,
                        showModeToggle = false,
                        title = null,
                        colors = DatePickerDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                            selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                            todayDateBorderColor = MaterialTheme.colorScheme.primary,
                            todayContentColor = MaterialTheme.colorScheme.primary,
                            navigationContentColor = MaterialTheme.colorScheme.primary,
                            headlineContentColor = MaterialTheme.colorScheme.onSurface,
                            titleContentColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
                                timeZone = TimeZone.getDefault()
                            }
                            foundDate = formatter.format(Date(millis))
                            localError = null
                        }
                        showDatePicker = false
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.height(38.dp)
                ) {
                    Text(
                        text = "Pilih",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDatePicker = false },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.height(38.dp)
                ) {
                    Text(
                        text = "Batal",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                        ),
                        shape = RoundedCornerShape(
                            bottomStart = 24.dp,
                            bottomEnd = 24.dp
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = if (isEditMode) "Edit Data Barang" else "Tambah Temuan Baru",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Isi info detail barang untuk mempermudah pencarian",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isEditMode && editingItem == null && !itemViewModel.isLoading) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Data edit belum termuat sempurna. Silakan kembali dan muat ulang.",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Column {
                    Text(
                        text = "Kategori *",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedCategoryName,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            isError = isFormSubmitted && categoryId <= 0,
                            trailingIcon = {
                                Icon(
                                    imageVector = if (categoryMenuExpanded) {
                                        Icons.Default.KeyboardArrowUp
                                    } else {
                                        Icons.Default.KeyboardArrowDown
                                    },
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable {
                                        categoryMenuExpanded = !categoryMenuExpanded
                                    }
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                errorBorderColor = MaterialTheme.colorScheme.error
                            )
                        )

                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { categoryMenuExpanded = true }
                        )

                        DropdownMenu(
                            expanded = categoryMenuExpanded,
                            onDismissRequest = { categoryMenuExpanded = false },
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            itemViewModel.categories.forEach { category ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = category.name,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    onClick = {
                                        categoryId = category.id
                                        categoryMenuExpanded = false
                                        localError = null
                                    }
                                )
                            }
                        }
                    }

                    if (isFormSubmitted && categoryId <= 0) {
                        Text(
                            text = "Pilih kategori terlebih dahulu",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }
                }

                CustomFormTextField(
                    label = "Nama Barang *",
                    value = name,
                    placeholder = "Contoh: Kunci Motor Honda, Dompet Hitam",
                    onValueChange = {
                        name = it
                        localError = null
                    },
                    isError = isFormSubmitted && name.isBlank(),
                    errorText = "Nama barang tidak boleh kosong",
                    imeAction = ImeAction.Next
                )

                CustomFormTextField(
                    label = "Deskripsi Kronologi / Keadaan *",
                    value = description,
                    placeholder = "Gambarkan secara singkat detail kondisi saat ditemukan...",
                    onValueChange = {
                        description = it
                        localError = null
                    },
                    isError = isFormSubmitted && description.isBlank(),
                    errorText = "Deskripsi barang wajib diisi",
                    minLines = 3,
                    singleLine = false,
                    imeAction = ImeAction.Next
                )

                CustomFormTextField(
                    label = "Lokasi Ditemukan *",
                    value = foundLocation,
                    placeholder = "Contoh: Meja Kantin Gedung Kuliah Terpadu",
                    onValueChange = {
                        foundLocation = it
                        localError = null
                    },
                    isError = isFormSubmitted && foundLocation.isBlank(),
                    errorText = "Lokasi temuan wajib diisi",
                    imeAction = ImeAction.Next
                )

                Column {
                    Text(
                        text = "Tanggal Ditemukan *",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    OutlinedTextField(
                        value = foundDate,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = {
                            Text(
                                text = "Pilih tanggal kalender",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true },
                        shape = RoundedCornerShape(14.dp),
                        isError = isFormSubmitted && foundDate.isBlank(),
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Kalender",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            errorBorderColor = MaterialTheme.colorScheme.error
                        )
                    )

                    if (isFormSubmitted && foundDate.isBlank()) {
                        Text(
                            text = "Tanggal wajib dipilih",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }
                }

                CustomFormTextField(
                    label = "Lokasi Tempat Pengambilan Barang *",
                    value = pickupLocation,
                    placeholder = "Contoh: Ruang Pos Satpam Gedung A",
                    onValueChange = {
                        pickupLocation = it
                        localError = null
                    },
                    isError = isFormSubmitted && pickupLocation.isBlank(),
                    errorText = "Lokasi pengambilan wajib diisi resmi",
                    imeAction = ImeAction.Next
                )

                CustomFormTextField(
                    label = "Ciri-Ciri Spesifik (Opsional)",
                    value = characteristics,
                    placeholder = "Contoh: Ada gantungan kunci berlogo anime, baret di sisi kanan",
                    onValueChange = {
                        characteristics = it
                        localError = null
                    },
                    isError = false,
                    minLines = 2,
                    singleLine = false,
                    imeAction = ImeAction.Next
                )

                CustomFormTextField(
                    label = "Catatan Internal Admin (Opsional)",
                    value = adminNote,
                    placeholder = "Contoh: Dititipkan sementara oleh mhs sipil angkatan 24",
                    onValueChange = {
                        adminNote = it
                        localError = null
                    },
                    isError = false,
                    minLines = 2,
                    singleLine = false,
                    imeAction = ImeAction.Done
                )

                Column {
                    Text(
                        text = "Foto Bukti Barang *",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { imagePicker.launch("image/*") }
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (photoUri != null || (isEditMode && !editingItem?.photoUrl.isNullOrBlank())) {
                                        "Ganti/Pilih Foto Baru"
                                    } else {
                                        "Klik Untuk Upload Foto"
                                    },
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = "Format berkas gambar (JPG, PNG)",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }

                    if (isFormSubmitted && !isEditMode && photoUri == null) {
                        Text(
                            text = "Foto barang wajib diupload",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }

                    if (photoUri != null || (isEditMode && !editingItem?.photoUrl.isNullOrBlank())) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.7f)
                                .clip(RoundedCornerShape(16.dp))
                        ) {
                            AsyncImage(
                                model = photoUri ?: editingItem?.photoUrl,
                                contentDescription = "Pratinjau Foto",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            if (photoUri != null) {
                                IconButton(
                                    onClick = { photoUri = null },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .background(
                                            color = Color.Black.copy(alpha = 0.6f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Hapus",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                val displayError = localError ?: itemViewModel.errorMessage

                AnimatedVisibility(visible = displayError != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = displayError.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            isFormSubmitted = true

                            localError = validateForm(
                                categoryId = categoryId,
                                name = name,
                                description = description,
                                foundLocation = foundLocation,
                                foundDate = foundDate,
                                pickupLocation = pickupLocation,
                                photoProvided = photoUri != null || (isEditMode && !editingItem?.photoUrl.isNullOrBlank())
                            )

                            if (localError != null) {
                                return@Button
                            }

                            val form = ItemFormData(
                                categoryId = categoryId,
                                name = name.trim(),
                                description = description.trim(),
                                foundLocation = foundLocation.trim(),
                                foundDate = foundDate.trim(),
                                pickupLocation = pickupLocation.trim(),
                                characteristics = characteristics.trim(),
                                adminNote = adminNote.trim()
                            )

                            if (isEditMode && itemId != null) {
                                itemViewModel.updateItem(
                                    id = itemId,
                                    form = form,
                                    photoUri = photoUri,
                                    onSuccess = onBack
                                )
                            } else {
                                itemViewModel.createItem(
                                    form = form,
                                    photoUri = photoUri,
                                    onSuccess = onBack
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        enabled = !itemViewModel.isLoading
                    ) {
                        Text(
                            text = if (itemViewModel.isLoading) {
                                "Memproses..."
                            } else if (isEditMode) {
                                "Simpan Perubahan Data"
                            } else {
                                "Simpan Barang Temuan"
                            },
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Text(
                            text = "Batalkan & Kembali",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomFormTextField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    errorText: String = "",
    minLines: Int = 1,
    singleLine: Boolean = true,
    imeAction: ImeAction = ImeAction.Next
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            isError = isError,
            minLines = minLines,
            singleLine = singleLine,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = imeAction
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                errorBorderColor = MaterialTheme.colorScheme.error
            )
        )

        if (isError && errorText.isNotBlank()) {
            Text(
                text = errorText,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}

private fun validateForm(
    categoryId: Int,
    name: String,
    description: String,
    foundLocation: String,
    foundDate: String,
    pickupLocation: String,
    photoProvided: Boolean
): String? {
    if (categoryId <= 0) return "Harap lengkapi opsi Kategori terlebih dahulu."
    if (name.isBlank()) return "Judul / Nama barang wajib dimasukkan."
    if (description.isBlank()) return "Deskripsi kondisi barang wajib diisi."
    if (foundLocation.isBlank()) return "Lokasi spesifik penemuan wajib diisi."
    if (foundDate.isBlank()) return "Tanggal penemuan wajib dipilih."
    if (pickupLocation.isBlank()) return "Tempat penyimpanan/pengambilan wajib diisi."
    if (!photoProvided) return "Foto barang wajib diupload."
    return null
}