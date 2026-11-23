package com.trendvibes.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trendvibes.app.data.model.StaffRole
import com.trendvibes.app.ui.components.*
import com.trendvibes.app.ui.theme.*
import com.trendvibes.app.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: (String) -> Unit,
    onBackToStore: () -> Unit
) {
    var email by remember { mutableStateOf("eduardo@trendvibes.com") }
    var password by remember { mutableStateOf("123456") }
    var selectedRole by remember { mutableStateOf(StaffRole.VENDEDOR) }
    var roleDropdownExpanded by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val serverUrl by viewModel.serverUrl.collectAsState()
    var showServerSettings by remember { mutableStateOf(false) }
    var customUrlInput by remember { mutableStateOf(serverUrl) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBlack)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, BrandBorder)
                .background(BrandWhite)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(onClick = onBackToStore)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Voltar",
                    tint = BrandBlack
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "VOLTAR À LOJA",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    color = BrandBlack
                )
            }

            IconButton(onClick = { showServerSettings = !showServerSettings }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Configurar Servidor",
                    tint = BrandBlack
                )
            }
        }

        // Server URL configuration box (optional)
        if (showServerSettings) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(2.dp, BrandBorder)
                    .background(BrandGrayDark)
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "ENDPOINT DO BACKEND C# (.NET)",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = BrandYellow
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = customUrlInput,
                        onValueChange = { customUrlInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = BrandWhite,
                            unfocusedContainerColor = BrandWhite,
                            focusedTextColor = BrandBlack,
                            unfocusedTextColor = BrandBlack
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    BrutalButton(
                        text = "SALVAR URL",
                        onClick = {
                            viewModel.updateServerUrl(customUrlInput)
                            showServerSettings = false
                        },
                        backgroundColor = BrandYellow,
                        textColor = BrandBlack,
                        shadowOffset = 2.dp
                    )
                }
            }
        }

        // Main Login Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BrutalBadge(
                text = "🛡️ NEO-BRUTALIST ACCESS",
                backgroundColor = BrandRed,
                textColor = BrandWhite,
                borderColor = BrandBorder
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "PORTAL OPERACIONAL",
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                letterSpacing = (-0.5).sp,
                color = BrandWhite
            )

            Text(
                text = "Acesso exclusivo para colaboradores do sistema TrendVibes.",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = Color.LightGray
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Error alert
            if (errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .border(2.dp, BrandBorder)
                        .background(BrandRed)
                        .padding(10.dp)
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = BrandWhite,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            // Input: Email
            BrutalTextField(
                value = email,
                onValueChange = { email = it },
                label = "Usuário / E-mail",
                placeholder = "colaborador@trendvibes.com",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Input: Senha
            BrutalTextField(
                value = password,
                onValueChange = { password = it },
                label = "Senha de Acesso",
                placeholder = "••••••••",
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Dropdown: Cargo
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "SELECIONE SEU CARGO",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    color = BrandWhite,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 4.dp, bottom = 4.dp)
                        .brutalShadow(shadowColor = BrandBorder, offsetX = 4.dp, offsetY = 4.dp)
                        .border(2.dp, BrandBorder)
                        .background(BrandWhite)
                        .clickable { roleDropdownExpanded = true }
                        .padding(horizontal = 14.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedRole.displayName.uppercase(),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = BrandBlack
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Expandir cargos",
                            tint = BrandBlack
                        )
                    }

                    DropdownMenu(
                        expanded = roleDropdownExpanded,
                        onDismissRequest = { roleDropdownExpanded = false },
                        modifier = Modifier
                            .background(BrandWhite)
                            .border(2.dp, BrandBorder)
                    ) {
                        StaffRole.entries.forEach { role ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = role.displayName.uppercase(),
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandBlack
                                    )
                                },
                                onClick = {
                                    selectedRole = role
                                    roleDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Login Button
            BrutalButton(
                text = if (isLoading) "AUTENTICANDO..." else "ENTRAR NO SISTEMA",
                onClick = {
                    viewModel.login(
                        email = email,
                        pass = password,
                        role = selectedRole.displayName,
                        onSuccess = { role ->
                            onLoginSuccess(role)
                        }
                    )
                },
                enabled = !isLoading && email.isNotEmpty() && password.isNotEmpty(),
                backgroundColor = BrandRed,
                textColor = BrandWhite,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "© 2026 TrendVibes Streetwear Augusta Inc.\nIntegração com C# .NET API & MySQL",
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                color = Color.Gray,
                lineHeight = 13.sp
            )
        }
    }
}
