package com.trendvibes.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.trendvibes.app.data.model.Product
import com.trendvibes.app.ui.accessibility.LocalTtsManager
import com.trendvibes.app.ui.components.*
import com.trendvibes.app.ui.theme.*
import com.trendvibes.app.viewmodel.StoreViewModel

@Composable
fun StoreScreen(
    viewModel: StoreViewModel,
    onOpenCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val products by viewModel.filteredProducts.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isPixModalVisible by viewModel.pixModalVisible.collectAsState()
    val a11yState by viewModel.accessibility.collectAsState()

    val ttsManager = LocalTtsManager.current
    val isHighContrast = LocalHighContrast.current
    val isDyslexia = LocalDyslexiaFont.current
    val appColors = LocalAppColors.current

    val categories = listOf("TODOS", "MOLETONS", "JAQUETAS", "CAMISETAS", "CALÇAS", "ACESSÓRIOS")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(appColors.background)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // HERO BANNER ITEM (Spans full width)
            item(span = { GridItemSpan(2) }) {
                HeroBanner()
            }

            // SEARCH & CATEGORY FILTER ROW (Spans full width)
            item(span = { GridItemSpan(2) }) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Search Input
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .border(if (isHighContrast) appColors.borderWidth else 2.dp, if (isHighContrast) appColors.border else BrandBorder)
                            .background(if (isHighContrast) Color(0xFF151515) else BrandWhite)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Buscar produtos",
                                tint = if (isHighContrast) BrandYellow else BrandBlack,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.onSearchQueryChange(it) },
                                placeholder = {
                                    Text(
                                        "BUSCAR PEÇAS NO DROP...",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = if (isHighContrast) Color.LightGray else Color.Gray,
                                        letterSpacing = if (isDyslexia) 1.sp else 0.sp
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedTextColor = if (isHighContrast) Color.White else BrandBlack,
                                    unfocusedTextColor = if (isHighContrast) Color.White else BrandBlack,
                                    cursorColor = if (isHighContrast) BrandYellow else BrandRed
                                )
                            )
                        }
                    }

                    // Horizontal Category Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { category ->
                            val isSelected = category.equals(selectedCategory, ignoreCase = true)
                            val chipBorder = if (isHighContrast) appColors.border else BrandBorder
                            val chipBg = if (isSelected) {
                                if (isHighContrast) BrandYellow else BrandRed
                            } else {
                                if (isHighContrast) Color(0xFF1A1A1A) else BrandWhite
                            }
                            val chipText = if (isSelected) {
                                if (isHighContrast) BrandBlack else BrandWhite
                            } else {
                                if (isHighContrast) Color.White else BrandBlack
                            }

                            Box(
                                modifier = Modifier
                                    .border(if (isHighContrast) 2.dp else 1.5.dp, chipBorder)
                                    .background(chipBg)
                                    .clickable(role = Role.Button) {
                                        viewModel.selectCategory(category)
                                        if (a11yState.isTtsEnabled) {
                                            ttsManager?.speak("Categoria $category selecionada. ${products.size} peças encontradas.")
                                        }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = category,
                                    color = chipText,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp,
                                    letterSpacing = if (isDyslexia) 1.sp else 0.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Section Heading
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DROP VANGUARDA",
                            color = if (isHighContrast) Color.White else BrandWhite,
                            fontFamily = if (isDyslexia) FontFamily.Monospace else FontFamily.SansSerif,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            letterSpacing = if (isDyslexia) 1.sp else (-0.5).sp
                        )
                        Text(
                            text = "${products.size} ITENS",
                            color = BrandYellow,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = if (isDyslexia) 1.sp else 0.sp
                        )
                    }
                }
            }

            // PRODUCT CARDS
            items(products, key = { it.id }) { product ->
                ProductCard(
                    product = product,
                    onAddToCart = {
                        viewModel.addToCart(product)
                        if (a11yState.isTtsEnabled) {
                            ttsManager?.speak("Produto ${product.name} adicionado à sacola de compras por ${String.format("%.2f", product.price)} reais.")
                        }
                        onOpenCart()
                    },
                    onCardClick = {
                        if (a11yState.isTtsEnabled) {
                            ttsManager?.speak("Produto: ${product.name}. Categoria: ${product.category}. Preço: ${String.format("%.2f", product.price)} reais.")
                        }
                    }
                )
            }

            // FOOTER BRANDING (Spans full width)
            item(span = { GridItemSpan(2) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TRENDVIBES AUGUSTA INC. © 2026",
                        color = if (isHighContrast) BrandYellow else BrandWhite,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = if (isDyslexia) 1.sp else 0.sp
                    )
                    Text(
                        text = "FORJADO NO CONCRETO. VANGUARDA URBANA.",
                        color = if (isHighContrast) Color.White else Color.Gray,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        letterSpacing = if (isDyslexia) 1.sp else 0.sp
                    )
                }
            }
        }

        // PIX CHECKOUT MODAL
        if (isPixModalVisible) {
            PixModal(
                total = viewModel.getCartTotal(),
                onDismiss = {
                    viewModel.showPixModal(false)
                    viewModel.clearCart()
                }
            )
        }
    }
}

@Composable
fun HeroBanner() {
    val isHighContrast = LocalHighContrast.current
    val isDyslexia = LocalDyslexiaFont.current
    val appColors = LocalAppColors.current

    val bannerBorder = if (isHighContrast) appColors.border else BrandBorder
    val bannerBorderWidth = if (isHighContrast) appColors.borderWidth else 2.dp
    val bannerBg = if (isHighContrast) Color(0xFF000000) else BrandGrayDark
    val fontTitle = if (isDyslexia) FontFamily.Monospace else FontFamily.SansSerif

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(bannerBorderWidth, bannerBorder)
            .background(bannerBg)
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.Start
        ) {
            BrutalBadge(
                text = "FALL / WINTER DROP - 2026",
                backgroundColor = if (isHighContrast) BrandYellow else BrandRed,
                textColor = if (isHighContrast) BrandBlack else BrandWhite,
                borderColor = bannerBorder
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "TRENDVIBES",
                color = BrandWhite,
                fontFamily = fontTitle,
                fontWeight = FontWeight.Black,
                fontSize = 32.sp,
                lineHeight = 32.sp,
                letterSpacing = if (isDyslexia) 1.sp else (-1).sp
            )
            Box(
                modifier = Modifier
                    .border(bannerBorderWidth, bannerBorder)
                    .background(if (isHighContrast) BrandYellow else BrandWhite)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "SANGUE-FRIO",
                    color = BrandBlack,
                    fontFamily = fontTitle,
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    letterSpacing = if (isDyslexia) 1.sp else (-0.5).sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Estruturados pesados de alta micragem e o caimento autêntico da vanguarda urbana brasileira.",
                color = if (isHighContrast) BrandYellow else Color.LightGray,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                lineHeight = 14.sp,
                letterSpacing = if (isDyslexia) 0.8.sp else 0.sp
            )
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    onAddToCart: () -> Unit,
    onCardClick: () -> Unit = {}
) {
    val isHighContrast = LocalHighContrast.current
    val isDyslexia = LocalDyslexiaFont.current
    val appColors = LocalAppColors.current

    val cardBg = if (isHighContrast) Color(0xFF0F0F0F) else BrandWhite
    val cardBorder = if (isHighContrast) appColors.border else BrandBorder
    val cardBorderWidth = if (isHighContrast) appColors.borderWidth else 2.dp
    val textTitle = if (isHighContrast) Color.White else BrandBlack
    val textPrice = if (isHighContrast) BrandYellow else BrandBlack
    val fontTitle = if (isDyslexia) FontFamily.Monospace else FontFamily.SansSerif

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onCardClick)
    ) {
        BrutalCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = cardBg,
            borderColor = cardBorder,
            borderWidth = cardBorderWidth,
            shadowOffset = 3.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .border(if (isHighContrast) 2.dp else 1.5.dp, cardBorder)
                    .background(if (isHighContrast) Color(0xFF222222) else BrandGrayLight)
            ) {
                AsyncImage(
                    model = product.img,
                    contentDescription = "Foto do produto ${product.name}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                if (product.isNew) {
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .align(Alignment.TopStart)
                    ) {
                        BrutalBadge(
                            text = "NOVO DROP 🔥",
                            backgroundColor = BrandYellow,
                            textColor = BrandBlack,
                            borderColor = cardBorder
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Category Tag
            Text(
                text = product.category,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 9.sp,
                color = if (isHighContrast) BrandYellow else BrandRed,
                letterSpacing = if (isDyslexia) 1.sp else 0.sp
            )

            // Title
            Text(
                text = product.name,
                fontFamily = fontTitle,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                lineHeight = 16.sp,
                color = textTitle,
                maxLines = 2,
                letterSpacing = if (isDyslexia) 0.8.sp else 0.sp,
                modifier = Modifier.height(34.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Price
            Text(
                text = "R$ ${String.format("%.2f", product.price)}",
                fontFamily = fontTitle,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                color = textPrice,
                letterSpacing = if (isDyslexia) 0.8.sp else 0.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Add to Cart Button
            BrutalButton(
                text = "COMPRAR",
                onClick = onAddToCart,
                backgroundColor = if (isHighContrast) BrandYellow else BrandRed,
                textColor = if (isHighContrast) BrandBlack else BrandWhite,
                borderColor = cardBorder,
                modifier = Modifier.fillMaxWidth(),
                shadowOffset = 2.dp
            )
        }
    }
}

@Composable
fun PixModal(
    total: Double,
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val fakePixCode = "00020126580014br.gov.bcb.pix0136trendvibes-augusta-pix-key-2026520400005303986540${String.format("%.2f", total)}5802BR5915TRENDVIBES INC6009SAO PAULO62070503***6304ABCD"
    var copied by remember { mutableStateOf(false) }

    val isHighContrast = LocalHighContrast.current
    val appColors = LocalAppColors.current
    val modalBg = if (isHighContrast) Color(0xFF000000) else BrandWhite
    val modalBorder = if (isHighContrast) appColors.border else BrandBorder
    val textPrimary = if (isHighContrast) Color(0xFFFFFFFF) else BrandBlack

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        containerColor = modalBg,
        modifier = Modifier
            .border(3.dp, modalBorder)
            .brutalShadow(shadowColor = modalBorder, offsetX = 6.dp, offsetY = 6.dp),
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PAGAMENTO PIX",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = textPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar modal pix", tint = textPrimary)
                    }
                }

                HorizontalDivider(color = modalBorder, thickness = 2.dp, modifier = Modifier.padding(vertical = 8.dp))

                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .border(2.dp, modalBorder)
                        .background(if (isHighContrast) Color(0xFF222222) else BrandGrayLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = "QR Code Pix para pagamento",
                        modifier = Modifier.size(120.dp),
                        tint = if (isHighContrast) BrandYellow else BrandBlack
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "VALOR TOTAL:",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = if (isHighContrast) Color.LightGray else Color.DarkGray
                )
                Text(
                    text = "R$ ${String.format("%.2f", total)}",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Black,
                    fontSize = 26.sp,
                    color = if (isHighContrast) BrandYellow else BrandRed
                )

                Spacer(modifier = Modifier.height(12.dp))

                BrutalButton(
                    text = if (copied) "CÓDIGO COPIADO! ✅" else "COPIAR CHAVE PIX",
                    onClick = {
                        clipboardManager.setText(AnnotatedString(fakePixCode))
                        copied = true
                    },
                    backgroundColor = if (copied) BrandGreen else if (isHighContrast) BrandYellow else BrandBlack,
                    textColor = if (copied) BrandWhite else if (isHighContrast) BrandBlack else BrandWhite,
                    borderColor = modalBorder,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                BrutalButton(
                    text = "CONFIRMAR PAGAMENTO",
                    onClick = onDismiss,
                    backgroundColor = if (isHighContrast) Color(0xFF333333) else BrandRed,
                    textColor = BrandWhite,
                    borderColor = modalBorder,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}
