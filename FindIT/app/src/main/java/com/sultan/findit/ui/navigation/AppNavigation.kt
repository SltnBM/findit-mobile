package com.sultan.findit.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sultan.findit.data.remote.RetrofitClient
import com.sultan.findit.data.repository.ActivityLogRepository
import com.sultan.findit.ui.admin.ActivityLogScreen
import com.sultan.findit.ui.admin.AdminDashboardScreen
import com.sultan.findit.ui.admin.AdminItemListScreen
import com.sultan.findit.ui.admin.CategoryScreen
import com.sultan.findit.ui.admin.ItemFormScreen
import com.sultan.findit.ui.auth.LoginScreen
import com.sultan.findit.ui.auth.RegisterScreen
import com.sultan.findit.ui.components.LoadingView
import com.sultan.findit.ui.user.HomeScreen
import com.sultan.findit.ui.user.ItemDetailScreen
import com.sultan.findit.viewmodel.ActivityLogViewModel
import com.sultan.findit.viewmodel.AuthViewModel
import com.sultan.findit.viewmodel.CategoryViewModel
import com.sultan.findit.viewmodel.ItemViewModel

object Routes {
    const val SPLASH_CHECK = "splash_check"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val USER_HOME = "user_home"
    const val ITEM_DETAIL = "item_detail"
    const val ADMIN_DASHBOARD = "admin_dashboard"
    const val ADMIN_ITEMS = "admin_items"
    const val ITEM_FORM_BASE = "item_form"
    const val ITEM_FORM = "$ITEM_FORM_BASE?itemId={itemId}"
    const val CATEGORY = "category"
    const val ADMIN_ACTIVITY_LOGS = "admin_activity_logs"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    val authViewModel: AuthViewModel = viewModel()
    val itemViewModel: ItemViewModel = viewModel()
    val categoryViewModel: CategoryViewModel = viewModel()

    val token = authViewModel.token
    val role = authViewModel.role

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH_CHECK
    ) {
        composable(Routes.SPLASH_CHECK) {
            if (authViewModel.isCheckingAuth) {
                LoadingView("Memeriksa sesi login...")
            } else {
                LaunchedEffect(token, role) {
                    val startDestination = when {
                        !token.isNullOrBlank() && role == "admin" -> Routes.ADMIN_DASHBOARD
                        !token.isNullOrBlank() -> Routes.USER_HOME
                        else -> Routes.LOGIN
                    }
                    navController.navigate(startDestination) {
                        popUpTo(Routes.SPLASH_CHECK) { inclusive = true }
                    }
                }
            }
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = { userRole ->
                    val destination = if (userRole == "admin") Routes.ADMIN_DASHBOARD else Routes.USER_HOME
                    navController.navigate(destination) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                authViewModel = authViewModel,
                onRegisterSuccess = { userRole ->
                    val destination = if (userRole == "admin") Routes.ADMIN_DASHBOARD else Routes.USER_HOME
                    navController.navigate(destination) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.USER_HOME) {
            if (!token.isNullOrBlank()) {
                HomeScreen(
                    authViewModel = authViewModel,
                    itemViewModel = itemViewModel,
                    onOpenDetail = { itemId ->
                        navController.navigate("${Routes.ITEM_DETAIL}/$itemId")
                    },
                    onLogout = {
                        authViewModel.logout(onDone = {
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(0) { inclusive = true }
                            }
                        })
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }

        composable(
            route = "${Routes.ITEM_DETAIL}/{itemId}",
            arguments = listOf(
                navArgument("itemId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            if (!token.isNullOrBlank()) {
                val itemId = backStackEntry.arguments?.getInt("itemId") ?: 0
                ItemDetailScreen(
                    itemId = itemId,
                    itemViewModel = itemViewModel,
                    onBack = { navController.popBackStack() }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }

        composable(Routes.ADMIN_DASHBOARD) {
            if (!token.isNullOrBlank() && role == "admin") {
                AdminDashboardScreen(
                    authViewModel = authViewModel,
                    itemViewModel = itemViewModel,
                    onOpenItems = { navController.navigate(Routes.ADMIN_ITEMS) },
                    onAddItem = { navController.navigate("${Routes.ITEM_FORM_BASE}?itemId=-1") },
                    onOpenCategory = { navController.navigate(Routes.CATEGORY) },
                    onOpenActivityLogs = { navController.navigate(Routes.ADMIN_ACTIVITY_LOGS) },
                    onLogout = {
                        authViewModel.logout(onDone = {
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(0) { inclusive = true }
                            }
                        })
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }

        composable(Routes.ADMIN_ITEMS) {
            if (!token.isNullOrBlank() && role == "admin") {
                AdminItemListScreen(
                    itemViewModel = itemViewModel,
                    onAddItem = { navController.navigate("${Routes.ITEM_FORM_BASE}?itemId=-1") },
                    onEditItem = { itemId ->
                        navController.navigate("${Routes.ITEM_FORM_BASE}?itemId=$itemId")
                    },
                    onBack = { navController.popBackStack() }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }

        composable(
            route = Routes.ITEM_FORM,
            arguments = listOf(
                navArgument("itemId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            if (!token.isNullOrBlank() && role == "admin") {
                val rawItemId = backStackEntry.arguments?.getInt("itemId") ?: -1
                val itemId = if (rawItemId > 0) rawItemId else null

                ItemFormScreen(
                    itemId = itemId,
                    itemViewModel = itemViewModel,
                    onBack = { navController.popBackStack() }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }

        composable(Routes.CATEGORY) {
            if (!token.isNullOrBlank() && role == "admin") {
                CategoryScreen(
                    categoryViewModel = categoryViewModel,
                    onBack = { navController.popBackStack() }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }

        composable(Routes.ADMIN_ACTIVITY_LOGS) {
            if (!token.isNullOrBlank() && role == "admin") {
                val context = LocalContext.current
                val activityLogRepository = remember { ActivityLogRepository(RetrofitClient.create(context)) }
                val activityLogViewModel: ActivityLogViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return ActivityLogViewModel(activityLogRepository) as T
                        }
                    }
                )

                ActivityLogScreen(
                    viewModel = activityLogViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
    }
}