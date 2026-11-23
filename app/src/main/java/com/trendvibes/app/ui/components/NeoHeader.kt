package com.trendvibes.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trendvibes.app.ui.theme.*

@Composable
fun NeoHeader(
    cartCount: Int,
    onCartClick: () -> Unit,
    onAccessibilityClick: () -> Unit,
    onStaffClick: () -> Unit,
    isStaffLoggedIn: Boolean = false,
    staffRole: String? = null
) {
    val isHighContrast = LocalHighContrast.current
    val isDyslexia = LocalDyslexiaFont.current
    val appColors = LocalAppColors.current

    val headerBorder = if (isHighContrast) appColors.border else BrandBorder
    val headerBorderWidth = if (isHighContrast) appColors.borderWidth else 2.dp
    val headerBg = if (isHighContrast) Color(0xFF000000) else BrandWhite
    val headerText = if (isHighContrast) Color(0xFFFFFFFF) else BrandBlack
    val fontTitle = if (isDyslexia) FontFamily.Monospace else FontFamily.SansSerif

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(appColors.background)
    ) {
        // Top Banner Drop Marquee / Strip
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = headerBorderWidth, color = headerBorder)
                .background(if (isHighContrast) BrandYellow else BrandRed)
                .padding(vertical = 4.dp, horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🔥 COLEÇÃO 'BLOODLINE' • CUPOM: TRENDVIBES10 🔥",
                color = if (isHighContrast) BrandBlack else BrandWhite,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 9.sp,
                letterSpacing = if (isDyslexia) 1.sp else 0.5.sp
            )
        }

        // Main Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = headerBorderWidth, color = headerBorder)
                .background(headerBg)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Brand Logo & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { /* Home */ }
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .border(headerBorderWidth, headerBorder)
                        .background(if (isHighContrast) BrandYellow else BrandRed),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚡",
                        fontSize = 18.sp,
                        color = if (isHighContrast) BrandBlack else BrandWhite
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Row {
                        Text(
                            text = "TRENDVIBES",
                            color = headerText,
                            fontFamily = fontTitle,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            letterSpacing = if (isDyslexia) 1.sp else (-0.5).sp
                        )
                        Text(
                            text = ".",
                            color = if (isHighContrast) BrandYellow else BrandRed,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp
                        )
                    }
                    Text(
                        text = "STREETWEAR SYSTEM",
                        color = if (isHighContrast) BrandYellow else Color.DarkGray,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 7.sp,
                        letterSpacing = if (isDyslexia) 1.5.sp else 1.sp
                    )
                }
            }

            // Action Icons: Accessibility, Staff, Cart
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Accessibility Button
                IconButton(
                    onClick = onAccessibilityClick,
                    modifier = Modifier
                        .size(34.dp)
                        .border(if (isHighContrast) 2.dp else 1.5.dp, headerBorder)
                        .background(if (isHighContrast) BrandYellow else BrandWhite)
                ) {
                    Icon(
                        imageVector = Icons.Default.Accessibility,
                        contentDescription = "Abrir painel de acessibilidade",
                        tint = BrandBlack,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Staff Login / Portal Button
                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .border(if (isHighContrast) 2.dp else 1.5.dp, headerBorder)
                        .background(if (isStaffLoggedIn) BrandRed else if (isHighContrast) Color(0xFF222222) else BrandGrayLight)
                        .clickable(role = Role.Button, onClick = onStaffClick)
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Acesso Staff",
                            tint = if (isStaffLoggedIn || isHighContrast) BrandWhite else BrandBlack,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = staffRole ?: "STAFF",
                            color = if (isStaffLoggedIn || isHighContrast) BrandWhite else BrandBlack,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 9.sp,
                            letterSpacing = if (isDyslexia) 1.sp else 0.sp
                        )
                    }
                }

                // Cart Icon with Badge
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .border(if (isHighContrast) 2.dp else 1.5.dp, headerBorder)
                        .background(if (isHighContrast) BrandYellow else BrandRed)
                        .clickable(role = Role.Button, onClick = onCartClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Sacola com $cartCount itens",
                        tint = if (isHighContrast) BrandBlack else BrandWhite,
                        modifier = Modifier.size(18.dp)
                    )
                    if (cartCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 4.dp, y = (-4).dp)
                                .size(16.dp)
                                .border(1.dp, headerBorder)
                                .background(if (isHighContrast) Color.Black else BrandYellow),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cartCount.toString(),
                                color = if (isHighContrast) BrandYellow else BrandBlack,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
