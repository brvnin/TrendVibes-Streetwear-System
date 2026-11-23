package com.trendvibes.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trendvibes.app.ui.accessibility.LocalTtsManager
import com.trendvibes.app.ui.components.BrutalButton
import com.trendvibes.app.ui.theme.*
import com.trendvibes.app.viewmodel.StoreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibilitySheet(
    viewModel: StoreViewModel,
    onDismiss: () -> Unit
) {
    val state by viewModel.accessibility.collectAsState()
    val ttsManager = LocalTtsManager.current
    val isHighContrast = state.isHighContrast
    val isDyslexia = state.isDyslexiaFont

    LaunchedEffect(Unit) {
        if (state.isTtsEnabled) {
            ttsManager?.speak("Painel de acessibilidade aberto. Opções: tamanho da fonte, modo alto contraste, fonte para dislexia e narrador de tela.")
        }
    }

    val sheetBg = if (isHighContrast) Color(0xFF000000) else BrandWhite
    val textPrimary = if (isHighContrast) Color(0xFFFFFFFF) else BrandBlack
    val borderColor = if (isHighContrast) BrandYellow else BrandBorder
    val fontTitle = if (isDyslexia) FontFamily.Monospace else FontFamily.SansSerif

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = sheetBg,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(sheetBg)
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚙ ACESSIBILIDADE",
                    fontFamily = fontTitle,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = textPrimary,
                    letterSpacing = if (isDyslexia) 1.sp else 0.sp
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fechar acessibilidade",
                        tint = textPrimary
                    )
                }
            }

            HorizontalDivider(
                color = borderColor,
                thickness = if (isHighContrast) 3.dp else 2.dp,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // Text Size Scaling
            val fontPercentage = (state.fontScale * 100).toInt()
            Text(
                text = "TAMANHO DA FONTE (${fontPercentage}%)",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                color = textPrimary,
                letterSpacing = if (isDyslexia) 1.sp else 0.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BrutalButton(
                    text = "A-",
                    onClick = {
                        val newScale = (state.fontScale - 0.1f).coerceIn(0.85f, 1.4f)
                        viewModel.setFontScale(newScale)
                        if (state.isTtsEnabled) {
                            ttsManager?.speak("Tamanho da fonte reduzido para ${(newScale * 100).toInt()}%")
                        }
                    },
                    modifier = Modifier.weight(1f),
                    backgroundColor = if (isHighContrast) Color(0xFF222222) else BrandGrayLight,
                    textColor = if (isHighContrast) BrandWhite else BrandBlack,
                    borderColor = borderColor
                )
                BrutalButton(
                    text = "100%",
                    onClick = {
                        viewModel.setFontScale(1.0f)
                        if (state.isTtsEnabled) {
                            ttsManager?.speak("Tamanho da fonte padrão 100%")
                        }
                    },
                    modifier = Modifier.weight(1f),
                    backgroundColor = if (kotlin.math.abs(state.fontScale - 1.0f) < 0.05f) BrandYellow else if (isHighContrast) Color(0xFF222222) else BrandGrayLight,
                    textColor = BrandBlack,
                    borderColor = borderColor
                )
                BrutalButton(
                    text = "A+",
                    onClick = {
                        val newScale = (state.fontScale + 0.1f).coerceIn(0.85f, 1.4f)
                        viewModel.setFontScale(newScale)
                        if (state.isTtsEnabled) {
                            ttsManager?.speak("Tamanho da fonte aumentado para ${(newScale * 100).toInt()}%")
                        }
                    },
                    modifier = Modifier.weight(1f),
                    backgroundColor = if (isHighContrast) Color(0xFF222222) else BrandGrayLight,
                    textColor = if (isHighContrast) BrandWhite else BrandBlack,
                    borderColor = borderColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // High Contrast Toggle
            Text(
                text = "MODO DE VISUALIZAÇÃO",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                color = textPrimary,
                letterSpacing = if (isDyslexia) 1.sp else 0.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            BrutalButton(
                text = if (state.isHighContrast) "👁 ALTO CONTRASTE ATIVO" else "👁 ATIVAR ALTO CONTRASTE",
                onClick = {
                    val willEnable = !state.isHighContrast
                    viewModel.toggleHighContrast()
                    if (state.isTtsEnabled) {
                        ttsManager?.speak(if (willEnable) "Modo alto contraste ativado" else "Modo alto contraste desativado")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = if (state.isHighContrast) BrandYellow else BrandBlack,
                textColor = if (state.isHighContrast) BrandBlack else BrandWhite,
                borderColor = borderColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Dyslexia Friendly Font
            Text(
                text = "TIPOGRAFIA PARA DISLEXIA",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                color = textPrimary,
                letterSpacing = if (isDyslexia) 1.sp else 0.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            BrutalButton(
                text = if (state.isDyslexiaFont) "📄 FONTE DISLEXIA ATIVA" else "📄 ATIVAR FONTE PARA DISLEXIA",
                onClick = {
                    val willEnable = !state.isDyslexiaFont
                    viewModel.toggleDyslexiaFont()
                    if (state.isTtsEnabled) {
                        ttsManager?.speak(if (willEnable) "Tipografia para dislexia ativada com espaçamento aprimorado" else "Tipografia padrão restaurada")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = if (state.isDyslexiaFont) BrandYellow else BrandBlack,
                textColor = if (state.isDyslexiaFont) BrandBlack else BrandWhite,
                borderColor = borderColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Screen Reader / TTS
            Text(
                text = "LEITOR DE TELA (NARRADOR TTS)",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                color = textPrimary,
                letterSpacing = if (isDyslexia) 1.sp else 0.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            BrutalButton(
                text = if (state.isTtsEnabled) "🔊 NARRADOR DE TELA ATIVO" else "🔊 ATIVAR NARRADOR DE TELA",
                onClick = {
                    val willEnable = !state.isTtsEnabled
                    viewModel.toggleTts()
                    if (willEnable) {
                        ttsManager?.speak("Leitor de tela TrendVibes ativado. O narrador falará produtos, categorias e ações.")
                    } else {
                        ttsManager?.speak("Leitor de tela desativado.")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = if (state.isTtsEnabled) BrandGreen else BrandBlack,
                textColor = BrandWhite,
                borderColor = borderColor
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
