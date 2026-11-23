package com.trendvibes.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.trendvibes.app.data.model.CartItem
import com.trendvibes.app.ui.accessibility.LocalTtsManager
import com.trendvibes.app.ui.components.BrutalButton
import com.trendvibes.app.ui.components.BrutalCard
import com.trendvibes.app.ui.theme.*
import com.trendvibes.app.viewmodel.StoreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartBottomSheet(
    viewModel: StoreViewModel,
    onDismiss: () -> Unit
) {
    val cartItems by viewModel.cart.collectAsState()
    val a11yState by viewModel.accessibility.collectAsState()
    val total = viewModel.getCartTotal()

    val ttsManager = LocalTtsManager.current
    val isHighContrast = LocalHighContrast.current
    val isDyslexia = LocalDyslexiaFont.current
    val appColors = LocalAppColors.current

    val sheetBg = if (isHighContrast) Color(0xFF000000) else BrandWhite
    val sheetBorder = if (isHighContrast) appColors.border else BrandBorder
    val sheetBorderWidth = if (isHighContrast) appColors.borderWidth else 2.dp
    val textPrimary = if (isHighContrast) Color(0xFFFFFFFF) else BrandBlack
    val fontTitle = if (isDyslexia) FontFamily.Monospace else FontFamily.SansSerif

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = sheetBg,
        dragHandle = null,
        modifier = Modifier.fillMaxHeight(0.85f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(sheetBg)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = sheetBorderWidth, color = sheetBorder)
                    .background(sheetBg)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "SACOLA DE COMPRAS",
                        fontFamily = fontTitle,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = textPrimary,
                        letterSpacing = if (isDyslexia) 1.sp else 0.sp
                    )
                    Text(
                        text = "${cartItems.sumOf { it.quantity }} ITEM(S) SELECIONADOS",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = if (isHighContrast) BrandYellow else Color.DarkGray,
                        letterSpacing = if (isDyslexia) 0.8.sp else 0.sp
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .border(1.5.dp, sheetBorder)
                        .background(if (isHighContrast) BrandYellow else BrandBlack)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fechar sacola",
                        tint = if (isHighContrast) BrandBlack else BrandWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Items List
            if (cartItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "SUA SACOLA ESTÁ VAZIA",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isHighContrast) Color.LightGray else Color.Gray,
                            letterSpacing = if (isDyslexia) 1.sp else 0.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Explore os lançamentos do Drop Sangue-Frio.",
                            fontFamily = fontTitle,
                            fontSize = 11.sp,
                            color = if (isHighContrast) Color.LightGray else Color.Gray,
                            letterSpacing = if (isDyslexia) 0.8.sp else 0.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(cartItems, key = { "${it.product.id}_${it.selectedSize}" }) { item ->
                        CartItemRow(
                            item = item,
                            onIncrement = {
                                viewModel.updateQuantity(item.product.id, item.selectedSize, item.quantity + 1)
                                if (a11yState.isTtsEnabled) {
                                    ttsManager?.speak("Quantidade de ${item.product.name} aumentada para ${item.quantity + 1}")
                                }
                            },
                            onDecrement = {
                                viewModel.updateQuantity(item.product.id, item.selectedSize, item.quantity - 1)
                                if (a11yState.isTtsEnabled) {
                                    ttsManager?.speak("Quantidade de ${item.product.name} reduzida para ${item.quantity - 1}")
                                }
                            },
                            onRemove = {
                                viewModel.removeFromCart(item.product.id, item.selectedSize)
                                if (a11yState.isTtsEnabled) {
                                    ttsManager?.speak("Item ${item.product.name} removido da sacola.")
                                }
                            }
                        )
                    }
                }
            }

            // Checkout Footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = sheetBorderWidth, color = sheetBorder)
                    .background(if (isHighContrast) Color(0xFF151515) else BrandGrayLight)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TOTAL DO PEDIDO",
                        fontFamily = fontTitle,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = textPrimary,
                        letterSpacing = if (isDyslexia) 1.sp else 0.sp
                    )
                    Text(
                        text = "R$ ${String.format("%.2f", total)}",
                        fontFamily = fontTitle,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = if (isHighContrast) BrandYellow else BrandRed,
                        letterSpacing = if (isDyslexia) 1.sp else 0.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                BrutalButton(
                    text = "FECHAR PEDIDO NO PIX",
                    onClick = {
                        onDismiss()
                        if (a11yState.isTtsEnabled) {
                            ttsManager?.speak("Abrindo pagamento Pix no valor de ${String.format("%.2f", total)} reais.")
                        }
                        viewModel.showPixModal(true)
                    },
                    enabled = cartItems.isNotEmpty(),
                    backgroundColor = if (isHighContrast) BrandYellow else BrandRed,
                    textColor = if (isHighContrast) BrandBlack else BrandWhite,
                    borderColor = sheetBorder,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit
) {
    val isHighContrast = LocalHighContrast.current
    val isDyslexia = LocalDyslexiaFont.current
    val appColors = LocalAppColors.current

    val rowBorder = if (isHighContrast) appColors.border else BrandBorder
    val rowBg = if (isHighContrast) Color(0xFF111111) else BrandWhite
    val textPrimary = if (isHighContrast) Color(0xFFFFFFFF) else BrandBlack
    val fontTitle = if (isDyslexia) FontFamily.Monospace else FontFamily.SansSerif

    BrutalCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = rowBg,
        borderColor = rowBorder,
        borderWidth = if (isHighContrast) 2.dp else 1.5.dp,
        shadowOffset = 2.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .border(1.5.dp, rowBorder)
            ) {
                AsyncImage(
                    model = item.product.img,
                    contentDescription = "Foto do item ${item.product.name}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.product.name,
                    fontFamily = fontTitle,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    color = textPrimary,
                    maxLines = 1,
                    letterSpacing = if (isDyslexia) 0.8.sp else 0.sp
                )
                Text(
                    text = "Tam: ${item.selectedSize} • R$ ${String.format("%.2f", item.product.price)}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = if (isHighContrast) BrandYellow else Color.DarkGray,
                    letterSpacing = if (isDyslexia) 0.8.sp else 0.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .border(1.dp, rowBorder)
                            .background(if (isHighContrast) Color(0xFF222222) else BrandGrayLight)
                            .clickable(role = Role.Button, onClick = onDecrement),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = "Diminuir quantidade",
                            tint = if (isHighContrast) Color.White else BrandBlack,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    Text(
                        text = "${item.quantity}",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = textPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .border(1.dp, rowBorder)
                            .background(if (isHighContrast) Color(0xFF222222) else BrandGrayLight)
                            .clickable(role = Role.Button, onClick = onIncrement),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Aumentar quantidade",
                            tint = if (isHighContrast) Color.White else BrandBlack,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remover item da sacola",
                    tint = if (isHighContrast) Color.White else Color.Gray
                )
            }
        }
    }
}
