package com.trendvibes.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.trendvibes.app.data.repository.TrendVibesRepository
import com.trendvibes.app.ui.accessibility.LocalTtsManager
import com.trendvibes.app.ui.accessibility.TtsManager
import com.trendvibes.app.ui.components.NeoHeader
import com.trendvibes.app.ui.screens.*
import com.trendvibes.app.ui.theme.TrendVibesTheme
import com.trendvibes.app.viewmodel.*

sealed class Screen(val route: String) {
    object Store : Screen("store")
    object Login : Screen("login")
    object Vendedor : Screen("vendedor")
    object Estoquista : Screen("estoquista")
    object Gerente : Screen("gerente")
    object Rh : Screen("rh")
}

@Composable
fun TrendVibesApp() {
    val repository = remember { TrendVibesRepository() }

    val storeViewModel = remember { StoreViewModel(repository) }
    val authViewModel = remember { AuthViewModel(repository) }
    val staffViewModel = remember { StaffViewModel(repository) }

    val navController = rememberNavController()

    val accessibilityState by storeViewModel.accessibility.collectAsState()
    val cartItems by storeViewModel.cart.collectAsState()
    val session by authViewModel.currentSession.collectAsState()

    var isCartOpen by remember { mutableStateOf(false) }
    var isAccessibilityOpen by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val ttsManager = remember { TtsManager(context) }
    DisposableEffect(Unit) {
        onDispose {
            ttsManager.shutdown()
        }
    }

    CompositionLocalProvider(LocalTtsManager provides ttsManager) {
        TrendVibesTheme(
            darkTheme = true,
            highContrast = accessibilityState.isHighContrast,
            dyslexiaFont = accessibilityState.isDyslexiaFont,
            fontScale = accessibilityState.fontScale
        ) {
            var currentRoute by remember { mutableStateOf(Screen.Store.route) }

            Scaffold(
                topBar = {
                    if (currentRoute == Screen.Store.route) {
                        NeoHeader(
                            cartCount = cartItems.sumOf { it.quantity },
                            onCartClick = {
                                if (accessibilityState.isTtsEnabled) {
                                    val count = cartItems.sumOf { it.quantity }
                                    ttsManager.speak("Sacola de compras aberta com $count peças.")
                                }
                                isCartOpen = true
                            },
                            onAccessibilityClick = { isAccessibilityOpen = true },
                            onStaffClick = {
                                if (session != null) {
                                    val dest = when (session?.cargo?.lowercase()) {
                                        "vendedor" -> Screen.Vendedor.route
                                        "estoquista" -> Screen.Estoquista.route
                                        "gerente" -> Screen.Gerente.route
                                        "rh" -> Screen.Rh.route
                                        else -> Screen.Login.route
                                    }
                                    navController.navigate(dest)
                                } else {
                                    navController.navigate(Screen.Login.route)
                                }
                            },
                            isStaffLoggedIn = session != null,
                            staffRole = session?.cargo
                        )
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = Screen.Store.route,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // 1. CLIENT STORE SCREEN
                    composable(Screen.Store.route) {
                        currentRoute = Screen.Store.route
                        StoreScreen(
                            viewModel = storeViewModel,
                            onOpenCart = { isCartOpen = true }
                        )
                    }

                    // 2. STAFF LOGIN SCREEN
                    composable(Screen.Login.route) {
                        currentRoute = Screen.Login.route
                        LoginScreen(
                            viewModel = authViewModel,
                            onLoginSuccess = { role ->
                                val dest = when (role.lowercase()) {
                                    "vendedor" -> Screen.Vendedor.route
                                    "estoquista" -> Screen.Estoquista.route
                                    "gerente" -> Screen.Gerente.route
                                    "rh" -> Screen.Rh.route
                                    else -> Screen.Store.route
                                }
                                navController.navigate(dest) {
                                    popUpTo(Screen.Store.route)
                                }
                            },
                            onBackToStore = {
                                navController.popBackStack()
                            }
                        )
                    }

                    // 3. STAFF: VENDEDOR
                    composable(Screen.Vendedor.route) {
                        currentRoute = Screen.Vendedor.route
                        VendedorScreen(
                            viewModel = staffViewModel,
                            onLogout = {
                                authViewModel.logout()
                                navController.navigate(Screen.Store.route) {
                                    popUpTo(Screen.Store.route) { inclusive = true }
                                }
                            }
                        )
                    }

                    // 4. STAFF: ESTOQUISTA
                    composable(Screen.Estoquista.route) {
                        currentRoute = Screen.Estoquista.route
                        EstoquistaScreen(
                            viewModel = staffViewModel,
                            onLogout = {
                                authViewModel.logout()
                                navController.navigate(Screen.Store.route) {
                                    popUpTo(Screen.Store.route) { inclusive = true }
                                }
                            }
                        )
                    }

                    // 5. STAFF: GERENTE
                    composable(Screen.Gerente.route) {
                        currentRoute = Screen.Gerente.route
                        GerenteScreen(
                            viewModel = staffViewModel,
                            onLogout = {
                                authViewModel.logout()
                                navController.navigate(Screen.Store.route) {
                                    popUpTo(Screen.Store.route) { inclusive = true }
                                }
                            }
                        )
                    }

                    // 6. STAFF: RH
                    composable(Screen.Rh.route) {
                        currentRoute = Screen.Rh.route
                        RhScreen(
                            viewModel = staffViewModel,
                            onLogout = {
                                authViewModel.logout()
                                navController.navigate(Screen.Store.route) {
                                    popUpTo(Screen.Store.route) { inclusive = true }
                                }
                            }
                        )
                    }
                }

                // Cart Bottom Sheet
                if (isCartOpen) {
                    CartBottomSheet(
                        viewModel = storeViewModel,
                        onDismiss = { isCartOpen = false }
                    )
                }

                // Accessibility Bottom Sheet
                if (isAccessibilityOpen) {
                    AccessibilitySheet(
                        viewModel = storeViewModel,
                        onDismiss = { isAccessibilityOpen = false }
                    )
                }
            }
        }
    }
}
