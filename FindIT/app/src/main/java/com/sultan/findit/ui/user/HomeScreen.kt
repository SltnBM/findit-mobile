package com.sultan.findit.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sultan.findit.data.model.Category
import com.sultan.findit.ui.components.EmptyState
import com.sultan.findit.ui.components.ErrorView
import com.sultan.findit.ui.components.FindItSectionTitle
import com.sultan.findit.ui.components.ItemCard
import com.sultan.findit.ui.components.LoadingView
import com.sultan.findit.viewmodel.AuthViewModel
import com.sultan.findit.viewmodel.ItemViewModel

@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    itemViewModel: ItemViewModel,
    onOpenDetail: (Int) -> Unit,
    onLogout: () -> Unit
) {
    val items by itemViewModel.localItems.collectAsState(initial = emptyList())
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        itemViewModel.loadCategories()
        itemViewModel.refreshUserItems()
    }

    val filteredItems = items.filter { item ->
        val matchSearch = itemViewModel.searchQuery.isBlank() ||
                item.name.contains(itemViewModel.searchQuery, ignoreCase = true) ||
                item.description.contains(itemViewModel.searchQuery, ignoreCase = true) ||
                item.foundLocation.contains(itemViewModel.searchQuery, ignoreCase = true) ||
                item.pickupLocation.contains(itemViewModel.searchQuery, ignoreCase = true) ||
                item.foundDate.contains(itemViewModel.searchQuery, ignoreCase = true) ||
                (item.categoryName?.contains(itemViewModel.searchQuery, ignoreCase = true) == true)

        val matchCategory = selectedCategory == null || item.categoryId == selectedCategory?.id
        matchSearch && matchCategory
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Konfirmasi Logout",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin keluar dari aplikasi?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        authViewModel.logout { onLogout() }
                    }
                ) {
                    Text(
                        text = "Keluar",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(
                        text = "Batal",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(
                            RoundedCornerShape(
                                bottomStart = 32.dp,
                                bottomEnd = 32.dp
                            )
                        )
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        )
                        .padding(start = 24.dp, top = 28.dp, end = 20.dp, bottom = 32.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(0.82f).align(Alignment.CenterStart)
                    ) {
                        Text(
                            text = "FindIT",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Layanan informasi barang temuan di lingkungan kampus.",
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.88f),
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )
                    }

                    IconButton(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier
                            .size(42.dp)
                            .align(Alignment.TopEnd),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f),
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Keluar Aplikasi",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        OutlinedTextField(
                            value = itemViewModel.searchQuery,
                            onValueChange = itemViewModel::onSearchChange,
                            placeholder = { Text("Ketik nama barang, lokasi, atau tanggal...", fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(
                                    onClick = { categoryMenuExpanded = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = if (categoryMenuExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                        containerColor = if (categoryMenuExpanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = 1.5.dp,
                                        color = if (categoryMenuExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                    )
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Menu,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = if (categoryMenuExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        Text(
                                            text = selectedCategory?.name ?: "Semua Jenis",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            modifier = Modifier.weight(1f)
                                        )

                                        Icon(
                                            imageVector = if (categoryMenuExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.ArrowDropDown,
                                            contentDescription = "Pilih Jenis",
                                            modifier = Modifier.size(18.dp),
                                            tint = if (categoryMenuExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = categoryMenuExpanded,
                                    onDismissRequest = { categoryMenuExpanded = false },
                                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                                ) {
                                    DropdownMenuItem(
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        text = { Text("Semua Jenis Barang", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                                        onClick = {
                                            selectedCategory = null
                                            categoryMenuExpanded = false
                                        }
                                    )
                                    itemViewModel.categories.forEach { category ->
                                        DropdownMenuItem(
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.List,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            },
                                            text = { Text(category.name, color = MaterialTheme.colorScheme.onSurface) },
                                            onClick = {
                                                selectedCategory = category
                                                categoryMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = { itemViewModel.refreshUserItems() },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                                modifier = Modifier.size(height = 42.dp, width = 100.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Perbarui", fontSize = 12.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }
            }

            item {
                FindItSectionTitle(
                    title = "Daftar Barang Ditemukan",
                    subtitle = "Menampilkan data laporan terkini yang tersimpan dengan aman di dalam perangkat Anda."
                )
            }

            if (itemViewModel.errorMessage != null) {
                item {
                    ErrorView(
                        message = "Gagal memuat data. Silakan periksa koneksi internet Anda atau tekan tombol Perbarui data.",
                        onRetry = { itemViewModel.refreshUserItems() }
                    )
                }
            }

            if (itemViewModel.isLoading) {
                item {
                    LoadingView()
                }
            }

            if (!itemViewModel.isLoading && filteredItems.isEmpty()) {
                item {
                    EmptyState(
                        title = "Pencarian Tidak Ditemukan",
                        message = "Belum ada laporan barang yang sesuai. Coba ubah kata kunci pencarian Anda atau tekan tombol Perbarui data di atas."
                    )
                }
            }

            items(
                items = filteredItems,
                key = { item -> "secure_user_card_${item.id}" }
            ) { item ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp)
                ) {
                    ItemCard(
                        item = item,
                        onClick = { onOpenDetail(item.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}