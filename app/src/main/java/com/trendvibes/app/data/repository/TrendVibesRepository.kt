package com.trendvibes.app.data.repository

import com.trendvibes.app.data.api.RetrofitClient
import com.trendvibes.app.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TrendVibesRepository {

    // Default Catalog from TrendVibes Augusta
    private val _products = MutableStateFlow<List<Product>>(
        listOf(
            Product(
                id = "p1",
                name = "MOLETOM CRIMSON REBEL",
                price = 349.90,
                category = "MOLETONS",
                img = "https://images.unsplash.com/photo-1556821840-3a63f95609a7?q=80&w=800",
                isNew = true,
                description = "Moletom pesado 450 GSM com silk frontal 'Rebel' emborrachado e capuz forrado duplo."
            ),
            Product(
                id = "p2",
                name = "CORTA-VENTO TECH-RED",
                price = 389.00,
                category = "JAQUETAS",
                img = "https://images.unsplash.com/photo-1548883354-7622d03aca27?q=80&w=800",
                isNew = false,
                description = "Nylon ripstop repelente a água com zíperes selados e forro respirável."
            ),
            Product(
                id = "p3",
                name = "CAMISETA INFERNO OVERSIZE",
                price = 159.90,
                category = "CAMISETAS",
                img = "https://images.unsplash.com/photo-1521572267360-ee0c2909d518?q=80&w=800",
                isNew = true,
                description = "Algodão penteado 260g fio 20.1 com gola canelada de 3cm e caimento boxy."
            ),
            Product(
                id = "p4",
                name = "CALÇA CARGO CRIMSON",
                price = 299.90,
                category = "CALÇAS",
                img = "https://images.unsplash.com/photo-1517423568366-8b83523034fd?q=80&w=800",
                isNew = false,
                description = "Sarja peletizada de alta durabilidade com 6 bolsos utilitários e ajuste no tornozelo."
            ),
            Product(
                id = "p5",
                name = "BONÉ TRUCKER BLOODLINE",
                price = 129.90,
                category = "ACESSÓRIOS",
                img = "https://images.unsplash.com/photo-1588850561407-ed78c282e89b?q=80&w=800",
                isNew = true,
                description = "Aba curva estruturada com fecho snapback e patch emborrachado frontal."
            ),
            Product(
                id = "p6",
                name = "SHOULDER BAG STREET BLACK",
                price = 149.90,
                category = "ACESSÓRIOS",
                img = "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?q=80&w=800",
                isNew = false,
                description = "Cordura resistente com alça regulável removível e compartimento acolchoado."
            )
        )
    )
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    // Shopping Cart State
    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    // Logged In Staff Session
    private val _currentSession = MutableStateFlow<LoginResponse?>(null)
    val currentSession: StateFlow<LoginResponse?> = _currentSession.asStateFlow()

    // Stock Management Items
    private val _stockItems = MutableStateFlow<List<StockItem>>(
        listOf(
            StockItem("s1", "TV-MOL-CR-P", "Moletom Crimson Rebel (P)", "MOLETONS", "P", 12),
            StockItem("s2", "TV-MOL-CR-M", "Moletom Crimson Rebel (M)", "MOLETONS", "M", 8),
            StockItem("s3", "TV-MOL-CR-G", "Moletom Crimson Rebel (G)", "MOLETONS", "G", 3, minThreshold = 5),
            StockItem("s4", "TV-MOL-CR-GG", "Moletom Crimson Rebel (GG)", "MOLETONS", "GG", 1, minThreshold = 5),
            StockItem("s5", "TV-JKT-TR-M", "Corta-Vento Tech-Red (M)", "JAQUETAS", "M", 15),
            StockItem("s6", "TV-JKT-TR-G", "Corta-Vento Tech-Red (G)", "JAQUETAS", "G", 7),
            StockItem("s7", "TV-TSH-INF-M", "Camiseta Inferno Oversize (M)", "CAMISETAS", "M", 25),
            StockItem("s8", "TV-TSH-INF-G", "Camiseta Inferno Oversize (G)", "CAMISETAS", "G", 4, minThreshold = 5),
            StockItem("s9", "TV-CAR-CRM-40", "Calça Cargo Crimson (40)", "CALÇAS", "40", 11),
            StockItem("s10", "TV-CAR-CRM-42", "Calça Cargo Crimson (42)", "CALÇAS", "42", 2, minThreshold = 5)
        )
    )
    val stockItems: StateFlow<List<StockItem>> = _stockItems.asStateFlow()

    // Collaborators list for RH and Gerente
    private val _collaborators = MutableStateFlow<List<Collaborator>>(
        listOf(
            Collaborator(1, "Eduardo Silva", "eduardo@trendvibes.com", "Vendedor", "ATIVO", 3450.0, 172.5),
            Collaborator(2, "Lucas Santos", "lucas@trendvibes.com", "Estoquista", "ATIVO", 0.0, 0.0),
            Collaborator(3, "Mariana Costa", "mariana@trendvibes.com", "Gerente", "ATIVO", 8900.0, 445.0),
            Collaborator(4, "Beatriz Lima", "beatriz@trendvibes.com", "RH", "ATIVO", 0.0, 0.0),
            Collaborator(5, "Felipe Rocha", "felipe@trendvibes.com", "Vendedor", "PAUSA", 2150.0, 107.5)
        )
    )
    val collaborators: StateFlow<List<Collaborator>> = _collaborators.asStateFlow()

    // Sales registered in this session
    private val _salesLog = MutableStateFlow<List<VendaRequest>>(emptyList())
    val salesLog: StateFlow<List<VendaRequest>> = _salesLog.asStateFlow()

    // --- CART ACTIONS ---
    fun addToCart(product: Product, size: String = "G") {
        val currentList = _cart.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.product.id == product.id && it.selectedSize == size }
        if (existingIndex >= 0) {
            val item = currentList[existingIndex]
            currentList[existingIndex] = item.copy(quantity = item.quantity + 1)
        } else {
            currentList.add(CartItem(product, 1, size))
        }
        _cart.value = currentList
    }

    fun removeFromCart(productId: String, size: String) {
        _cart.value = _cart.value.filterNot { it.product.id == productId && it.selectedSize == size }
    }

    fun updateQuantity(productId: String, size: String, newQty: Int) {
        if (newQty <= 0) {
            removeFromCart(productId, size)
            return
        }
        _cart.value = _cart.value.map {
            if (it.product.id == productId && it.selectedSize == size) it.copy(quantity = newQty) else it
        }
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    fun getCartTotal(): Double {
        return _cart.value.sumOf { it.product.price * it.quantity }
    }

    // --- AUTH ACTIONS ---
    suspend fun login(email: String, senha: String, cargo: String): Result<LoginResponse> {
        return try {
            val response = RetrofitClient.apiService.login(LoginRequest(email, senha, cargo))
            if (response.isSuccessful && response.body()?.sucesso == true) {
                val body = response.body()!!
                _currentSession.value = body
                Result.success(body)
            } else {
                val errorMsg = response.body()?.mensagem ?: "Credenciais ou cargo inválidos no servidor."
                // Demo fallback if backend is offline or returned error
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            // Offline demo fallback so app can be tested immediately even if C# server is stopped
            val mockResponse = LoginResponse(
                sucesso = true,
                colaboradorId = 1,
                nome = email.substringBefore("@").replace(".", " ").capitalize(),
                cargo = cargo,
                email = email,
                turnoStatus = "ATIVO",
                mensagem = "Conectado via Modo Local/Offline (Servidor C# não respondeu)",
                url = "${cargo.lowercase()}.html"
            )
            _currentSession.value = mockResponse
            Result.success(mockResponse)
        }
    }

    fun logout() {
        _currentSession.value = null
    }

    // --- STAFF ACTIONS ---
    fun toggleShiftStatus() {
        val current = _currentSession.value ?: return
        val newStatus = if (current.turnoStatus == "ATIVO") "PAUSA" else "ATIVO"
        _currentSession.value = current.copy(turnoStatus = newStatus)
    }

    suspend fun registerSale(sku: String, valor: Double): Result<VendaResponse> {
        val session = _currentSession.value ?: return Result.failure(Exception("Nenhum usuário logado"))
        val comissao = valor * 0.05 // Business rule from C# API: 5% commission

        return try {
            val response = RetrofitClient.apiService.registrarVenda(
                VendaRequest(session.colaboradorId ?: 1, sku, valor)
            )
            if (response.isSuccessful && response.body()?.sucesso == true) {
                _salesLog.value = _salesLog.value + VendaRequest(session.colaboradorId ?: 1, sku, valor)
                Result.success(response.body()!!)
            } else {
                // Local simulation
                _salesLog.value = _salesLog.value + VendaRequest(session.colaboradorId ?: 1, sku, valor)
                Result.success(VendaResponse(true, "Venda registrada (Modo Local)", comissao))
            }
        } catch (e: Exception) {
            _salesLog.value = _salesLog.value + VendaRequest(session.colaboradorId ?: 1, sku, valor)
            Result.success(VendaResponse(true, "Venda registrada com sucesso (Simulado)", comissao))
        }
    }

    fun updateStock(sku: String, delta: Int) {
        _stockItems.value = _stockItems.value.map { item ->
            if (item.sku == item.sku && item.sku == sku) {
                val newQty = (item.quantity + delta).coerceAtLeast(0)
                item.copy(quantity = newQty)
            } else {
                item
            }
        }
    }

    fun addStockItem(newItem: StockItem) {
        _stockItems.value = _stockItems.value + newItem
    }

    fun addCollaborator(collaborator: Collaborator) {
        _collaborators.value = _collaborators.value + collaborator
    }
}
