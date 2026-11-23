package com.trendvibes.app.data.model

import com.google.gson.annotations.SerializedName

/**
 * Product Item Model matching the TrendVibes Drop catalog
 */
data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val category: String,
    val img: String,
    val isNew: Boolean = false,
    val description: String = "Estruturado pesado de alta micragem, corte boxy e algodão penteado de vanguarda urbana."
)

/**
 * Cart Item Model
 */
data class CartItem(
    val product: Product,
    var quantity: Int = 1,
    val selectedSize: String = "G"
)

/**
 * Staff Role Enum
 */
enum class StaffRole(val displayName: String) {
    VENDEDOR("Vendedor"),
    ESTOQUISTA("Estoquista"),
    GERENTE("Gerente"),
    RH("RH");

    companion object {
        fun fromString(value: String): StaffRole {
            return entries.find { it.displayName.equals(value, ignoreCase = true) } ?: VENDEDOR
        }
    }
}

/**
 * Login Request Body (matching .NET C# API: /api/login)
 */
data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("senha") val senha: String,
    @SerializedName("cargo") val cargo: String
)

/**
 * Login Response Body (matching .NET C# API: /api/login)
 */
data class LoginResponse(
    @SerializedName("sucesso") val sucesso: Boolean,
    @SerializedName("colaboradorId") val colaboradorId: Int? = null,
    @SerializedName("nome") val nome: String? = null,
    @SerializedName("cargo") val cargo: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("turnoStatus") val turnoStatus: String? = "ATIVO",
    @SerializedName("mensagem") val mensagem: String? = null,
    @SerializedName("url") val url: String? = null
)

/**
 * Sale Request (matching .NET C# API: /api/vendas)
 */
data class VendaRequest(
    @SerializedName("colaboradorId") val colaboradorId: Int,
    @SerializedName("sku") val sku: String,
    @SerializedName("valor") val valor: Double
)

/**
 * Sale Response
 */
data class VendaResponse(
    @SerializedName("sucesso") val sucesso: Boolean,
    @SerializedName("mensagem") val mensagem: String,
    @SerializedName("comissao") val comissao: Double
)

/**
 * Stock Item representation for Estoquista panel
 */
data class StockItem(
    val id: String,
    val sku: String,
    val name: String,
    val category: String,
    val size: String,
    var quantity: Int,
    val minThreshold: Int = 5
)

/**
 * Collaborator model for RH & Manager panels
 */
data class Collaborator(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    var turnStatus: String = "ATIVO",
    val salesTotal: Double = 0.0,
    val commissionTotal: Double = 0.0
)
