package com.trendvibes.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trendvibes.app.data.api.RetrofitClient
import com.trendvibes.app.data.model.*
import com.trendvibes.app.data.repository.TrendVibesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AccessibilityState(
    val fontScale: Float = 1.0f,
    val isHighContrast: Boolean = false,
    val isDyslexiaFont: Boolean = false,
    val isTtsEnabled: Boolean = false
)

class StoreViewModel(
    private val repository: TrendVibesRepository
) : ViewModel() {

    val products: StateFlow<List<Product>> = repository.products
    val cart: StateFlow<List<CartItem>> = repository.cart

    private val _selectedCategory = MutableStateFlow("TODOS")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _accessibility = MutableStateFlow(AccessibilityState())
    val accessibility: StateFlow<AccessibilityState> = _accessibility.asStateFlow()

    private val _pixModalVisible = MutableStateFlow(false)
    val pixModalVisible: StateFlow<Boolean> = _pixModalVisible.asStateFlow()

    val filteredProducts: StateFlow<List<Product>> = combine(
        products,
        _selectedCategory,
        _searchQuery
    ) { prods, category, query ->
        prods.filter { p ->
            val matchesCategory = (category == "TODOS" || p.category.equals(category, ignoreCase = true))
            val matchesQuery = query.isEmpty() || p.name.contains(query, ignoreCase = true) || p.description.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun addToCart(product: Product, size: String = "G") {
        repository.addToCart(product, size)
    }

    fun removeFromCart(productId: String, size: String) {
        repository.removeFromCart(productId, size)
    }

    fun updateQuantity(productId: String, size: String, qty: Int) {
        repository.updateQuantity(productId, size, qty)
    }

    fun clearCart() {
        repository.clearCart()
    }

    fun getCartTotal(): Double = repository.getCartTotal()

    fun showPixModal(show: Boolean) {
        _pixModalVisible.value = show
    }

    // Accessibility Controls
    fun setFontScale(scale: Float) {
        _accessibility.value = _accessibility.value.copy(fontScale = scale.coerceIn(0.85f, 1.4f))
    }

    fun toggleHighContrast() {
        _accessibility.value = _accessibility.value.copy(isHighContrast = !_accessibility.value.isHighContrast)
    }

    fun toggleDyslexiaFont() {
        _accessibility.value = _accessibility.value.copy(isDyslexiaFont = !_accessibility.value.isDyslexiaFont)
    }

    fun toggleTts() {
        _accessibility.value = _accessibility.value.copy(isTtsEnabled = !_accessibility.value.isTtsEnabled)
    }
}

class AuthViewModel(
    private val repository: TrendVibesRepository
) : ViewModel() {

    val currentSession: StateFlow<LoginResponse?> = repository.currentSession

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _serverUrl = MutableStateFlow(RetrofitClient.getBaseUrl())
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

    fun updateServerUrl(url: String) {
        _serverUrl.value = url
        RetrofitClient.updateBaseUrl(url)
    }

    fun login(email: String, pass: String, role: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = repository.login(email, pass, role)
            _isLoading.value = false

            result.onSuccess { response ->
                onSuccess(response.cargo ?: role)
            }.onFailure { err ->
                _errorMessage.value = err.message ?: "Falha ao realizar login"
            }
        }
    }

    fun logout() {
        repository.logout()
    }
}

class StaffViewModel(
    private val repository: TrendVibesRepository
) : ViewModel() {

    val currentSession = repository.currentSession
    val stockItems = repository.stockItems
    val collaborators = repository.collaborators
    val salesLog = repository.salesLog

    private val _salesMessage = MutableStateFlow<String?>(null)
    val salesMessage: StateFlow<String?> = _salesMessage.asStateFlow()

    fun toggleShift() {
        repository.toggleShiftStatus()
    }

    fun registerSale(sku: String, valor: Double) {
        viewModelScope.launch {
            val result = repository.registerSale(sku, valor)
            result.onSuccess {
                _salesMessage.value = "Venda de R$ ${String.format("%.2f", valor)} registrada! Comissão: R$ ${String.format("%.2f", it.comissao)}"
            }.onFailure {
                _salesMessage.value = "Erro: ${it.message}"
            }
        }
    }

    fun dismissSalesMessage() {
        _salesMessage.value = null
    }

    fun updateStock(sku: String, delta: Int) {
        repository.updateStock(sku, delta)
    }

    fun addStockItem(sku: String, name: String, category: String, size: String, qty: Int) {
        val item = StockItem(
            id = "s_${System.currentTimeMillis()}",
            sku = sku,
            name = name,
            category = category,
            size = size,
            quantity = qty
        )
        repository.addStockItem(item)
    }

    fun addCollaborator(name: String, email: String, role: String) {
        val col = Collaborator(
            id = (collaborators.value.maxOfOrNull { it.id } ?: 0) + 1,
            name = name,
            email = email,
            role = role,
            turnStatus = "ATIVO"
        )
        repository.addCollaborator(col)
    }
}
