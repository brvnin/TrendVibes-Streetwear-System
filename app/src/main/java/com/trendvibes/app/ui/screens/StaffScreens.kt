package com.trendvibes.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trendvibes.app.data.model.Collaborator
import com.trendvibes.app.data.model.StockItem
import com.trendvibes.app.ui.components.*
import com.trendvibes.app.ui.theme.*
import com.trendvibes.app.viewmodel.StaffViewModel

// ==========================================
// 1. PAINEL DO VENDEDOR (vendedor.html)
// ==========================================
@Composable
fun VendedorScreen(
    viewModel: StaffViewModel,
    onLogout: () -> Unit
) {
    val session by viewModel.currentSession.collectAsState()
    val salesLog by viewModel.salesLog.collectAsState()
    val salesMessage by viewModel.salesMessage.collectAsState()

    var skuInput by remember { mutableStateOf("TV-MOL-CR-G") }
    var valueInput by remember { mutableStateOf("349.90") }

    val calculatedCommission = (valueInput.toDoubleOrNull() ?: 0.0) * 0.05

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBlack)
            .verticalScroll(rememberScrollState())
    ) {
        StaffHeader(
            title = "PAINEL DO VENDEDOR",
            role = "VENDEDOR",
            onLogout = onLogout
        )

        Column(modifier = Modifier.padding(14.dp)) {
            // Turno Status Card
            BrutalCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = BrandWhite,
                borderColor = BrandBorder
            ) {
                Text(
                    text = "CONTROLE DE TURNO OPERACIONAL",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = session?.nome ?: "COLABORADOR",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = BrandBlack
                )
                Text(
                    text = "E-mail: ${session?.email ?: "vendedor@trendvibes.com"}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isAtivo = session?.turnoStatus == "ATIVO"
                    BrutalButton(
                        text = if (isAtivo) "TURNO: ATIVO 🟢" else "TURNO: PAUSA 🟡",
                        onClick = { viewModel.toggleShift() },
                        backgroundColor = if (isAtivo) BrandGreen else BrandYellow,
                        textColor = if (isAtivo) BrandWhite else BrandBlack,
                        shadowOffset = 2.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Registration Card: Registrar Venda
            BrutalCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = BrandWhite,
                borderColor = BrandBorder
            ) {
                Text(
                    text = "REGISTRAR NOVA VENDA",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = BrandBlack
                )
                Text(
                    text = "Comissão automática de 5% sobre o valor faturado.",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (salesMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .border(1.5.dp, BrandBorder)
                            .background(BrandYellow)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = salesMessage ?: "",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = BrandBlack
                        )
                    }
                }

                // SKU Field
                BrutalTextField(
                    value = skuInput,
                    onValueChange = { skuInput = it },
                    label = "CÓDIGO SKU DO PRODUTO",
                    placeholder = "Ex: TV-MOL-CR-G",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Value Field
                BrutalTextField(
                    value = valueInput,
                    onValueChange = { valueInput = it },
                    label = "VALOR DA VENDA (R$)",
                    placeholder = "0.00",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Commission Preview
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, BrandBorder)
                        .background(BrandGrayLight)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SUA COMISSÃO (5%):",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = BrandBlack
                    )
                    Text(
                        text = "R$ ${String.format("%.2f", calculatedCommission)}",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = BrandRed
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                BrutalButton(
                    text = "LANÇAR VENDA NO SISTEMA",
                    onClick = {
                        val v = valueInput.toDoubleOrNull() ?: 0.0
                        if (skuInput.isNotEmpty() && v > 0) {
                            viewModel.registerSale(skuInput, v)
                        }
                    },
                    backgroundColor = BrandRed,
                    textColor = BrandWhite,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Recent sales list
            Text(
                text = "VENDAS REGISTRADAS HOJE (${salesLog.size})",
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = BrandWhite
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (salesLog.isEmpty()) {
                Text(
                    text = "Nenhuma venda registrada nesta sessão ainda.",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            } else {
                salesLog.reversed().forEach { sale ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                            .border(1.5.dp, BrandBorder)
                            .background(BrandWhite)
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = sale.sku,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    color = BrandBlack
                                )
                                Text(
                                    text = "Valor: R$ ${String.format("%.2f", sale.valor)}",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = Color.DarkGray
                                )
                            }
                            Text(
                                text = "+ R$ ${String.format("%.2f", sale.valor * 0.05)}",
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                color = BrandGreen
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. PAINEL DO ESTOQUISTA (estoquista.html)
// ==========================================
@Composable
fun EstoquistaScreen(
    viewModel: StaffViewModel,
    onLogout: () -> Unit
) {
    val stockItems by viewModel.stockItems.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBlack)
    ) {
        StaffHeader(
            title = "PAINEL DO ESTOQUISTA",
            role = "ESTOQUISTA",
            onLogout = onLogout
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "GRADE DE ESTOQUE",
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                color = BrandWhite
            )
            BrutalButton(
                text = "+ NOVO SKU",
                onClick = { showAddDialog = true },
                backgroundColor = BrandYellow,
                textColor = BrandBlack,
                shadowOffset = 2.dp
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(stockItems, key = { it.sku }) { item ->
                StockItemCard(
                    item = item,
                    onIncrease = { viewModel.updateStock(item.sku, 1) },
                    onDecrease = { viewModel.updateStock(item.sku, -1) }
                )
            }
        }

        if (showAddDialog) {
            AddStockDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { sku, name, cat, size, qty ->
                    viewModel.addStockItem(sku, name, cat, size, qty)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun StockItemCard(
    item: StockItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    val isLowStock = item.quantity <= item.minThreshold

    BrutalCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = BrandWhite,
        borderColor = BrandBorder,
        borderWidth = 1.5.dp,
        shadowOffset = 2.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.sku,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = BrandBlack
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    if (isLowStock) {
                        BrutalBadge(
                            text = "ESTOQUE BAIXO ⚠️",
                            backgroundColor = BrandRed,
                            textColor = BrandWhite,
                            borderColor = BrandBorder
                        )
                    }
                }
                Text(
                    text = item.name,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = BrandBlack
                )
                Text(
                    text = "Cat: ${item.category} • Tam: ${item.size}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = Color.DarkGray
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .border(1.5.dp, BrandBorder)
                        .background(BrandGrayLight)
                        .clickable(onClick = onDecrease),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Diminuir", tint = BrandBlack, modifier = Modifier.size(16.dp))
                }
                Box(
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .border(1.5.dp, BrandBorder)
                        .background(if (isLowStock) BrandRed else BrandBlack)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${item.quantity}",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = BrandWhite
                    )
                }
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .border(1.5.dp, BrandBorder)
                        .background(BrandGrayLight)
                        .clickable(onClick = onIncrease),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Aumentar", tint = BrandBlack, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun AddStockDialog(
    onDismiss: () -> Unit,
    onConfirm: (sku: String, name: String, category: String, size: String, qty: Int) -> Unit
) {
    var sku by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("MOLETONS") }
    var size by remember { mutableStateOf("G") }
    var qty by remember { mutableStateOf("10") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        containerColor = BrandWhite,
        modifier = Modifier.border(3.dp, BrandBorder),
        text = {
            Column {
                Text(
                    text = "NOVO ITEM DE ESTOQUE",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = BrandBlack
                )
                Divider(color = BrandBorder, thickness = 2.dp, modifier = Modifier.padding(vertical = 8.dp))

                BrutalTextField(value = sku, onValueChange = { sku = it }, label = "SKU", placeholder = "TV-NEW-01")
                Spacer(modifier = Modifier.height(6.dp))
                BrutalTextField(value = name, onValueChange = { name = it }, label = "NOME", placeholder = "Moletom Boxy")
                Spacer(modifier = Modifier.height(6.dp))
                BrutalTextField(value = size, onValueChange = { size = it }, label = "TAMANHO", placeholder = "M / G / GG")
                Spacer(modifier = Modifier.height(6.dp))
                BrutalTextField(value = qty, onValueChange = { qty = it }, label = "QUANTIDADE", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

                Spacer(modifier = Modifier.height(14.dp))

                BrutalButton(
                    text = "CADASTRAR ITEM",
                    onClick = {
                        if (sku.isNotEmpty() && name.isNotEmpty()) {
                            onConfirm(sku, name, category, size, qty.toIntOrNull() ?: 0)
                        }
                    },
                    backgroundColor = BrandRed,
                    textColor = BrandWhite,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

// ==========================================
// 3. PAINEL DO GERENTE (gerente.html)
// ==========================================
@Composable
fun GerenteScreen(
    viewModel: StaffViewModel,
    onLogout: () -> Unit
) {
    val collaborators by viewModel.collaborators.collectAsState()
    val stockItems by viewModel.stockItems.collectAsState()
    val salesLog by viewModel.salesLog.collectAsState()

    val totalSalesValue = collaborators.sumOf { it.salesTotal } + salesLog.sumOf { it.valor }
    val totalCommissions = collaborators.sumOf { it.commissionTotal } + salesLog.sumOf { it.valor * 0.05 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBlack)
            .verticalScroll(rememberScrollState())
    ) {
        StaffHeader(
            title = "PAINEL DA GERÊNCIA",
            role = "GERENTE",
            onLogout = onLogout
        )

        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "INDICADORES GERAIS (KPIs)",
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                color = BrandWhite
            )

            Spacer(modifier = Modifier.height(10.dp))

            // KPI Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KpiCard(
                    title = "FATURAMENTO",
                    value = "R$ ${String.format("%.2f", totalSalesValue)}",
                    badge = "+12% HOJE",
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "COMISSÕES",
                    value = "R$ ${String.format("%.2f", totalCommissions)}",
                    badge = "5% PADRÃO",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KpiCard(
                    title = "EQUIPE ATIVA",
                    value = "${collaborators.count { it.turnStatus == "ATIVO" }} / ${collaborators.size}",
                    badge = "TURNOS",
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "TOTAL EM ESTOQUE",
                    value = "${stockItems.sumOf { it.quantity }} PEÇAS",
                    badge = "LOGÍSTICA",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "DESEMPENHO DOS VENDEDORES",
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                color = BrandWhite
            )

            Spacer(modifier = Modifier.height(10.dp))

            collaborators.filter { it.role == "Vendedor" }.forEach { vendedor ->
                BrutalCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    backgroundColor = BrandWhite,
                    borderColor = BrandBorder,
                    borderWidth = 1.5.dp,
                    shadowOffset = 2.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = vendedor.name,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = BrandBlack
                            )
                            Text(
                                text = "Status: ${vendedor.turnStatus} • E-mail: ${vendedor.email}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = Color.DarkGray
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "R$ ${String.format("%.2f", vendedor.salesTotal)}",
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = BrandBlack
                            )
                            Text(
                                text = "Comissão: R$ ${String.format("%.2f", vendedor.commissionTotal)}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = BrandGreen
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. PAINEL DE RECURSOS HUMANOS (rh.html)
// ==========================================
@Composable
fun RhScreen(
    viewModel: StaffViewModel,
    onLogout: () -> Unit
) {
    val collaborators by viewModel.collaborators.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBlack)
    ) {
        StaffHeader(
            title = "PAINEL DE RECURSOS HUMANOS",
            role = "RH",
            onLogout = onLogout
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "QUADRO DE COLABORADORES",
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                color = BrandWhite
            )
            BrutalButton(
                text = "+ COLABORADOR",
                onClick = { showAddDialog = true },
                backgroundColor = BrandYellow,
                textColor = BrandBlack,
                shadowOffset = 2.dp
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(collaborators, key = { it.id }) { col ->
                BrutalCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = BrandWhite,
                    borderColor = BrandBorder,
                    borderWidth = 1.5.dp,
                    shadowOffset = 2.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = col.name,
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = BrandBlack
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                BrutalBadge(
                                    text = col.role,
                                    backgroundColor = BrandRed,
                                    textColor = BrandWhite,
                                    borderColor = BrandBorder
                                )
                            }
                            Text(
                                text = col.email,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = Color.DarkGray
                            )
                        }

                        BrutalBadge(
                            text = col.turnStatus,
                            backgroundColor = if (col.turnStatus == "ATIVO") BrandGreen else BrandYellow,
                            textColor = if (col.turnStatus == "ATIVO") BrandWhite else BrandBlack,
                            borderColor = BrandBorder
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            AddCollaboratorDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { name, email, role ->
                    viewModel.addCollaborator(name, email, role)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun AddCollaboratorDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, email: String, role: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Vendedor") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        containerColor = BrandWhite,
        modifier = Modifier.border(3.dp, BrandBorder),
        text = {
            Column {
                Text(
                    text = "NOVO COLABORADOR",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = BrandBlack
                )
                Divider(color = BrandBorder, thickness = 2.dp, modifier = Modifier.padding(vertical = 8.dp))

                BrutalTextField(value = name, onValueChange = { name = it }, label = "NOME COMPLETO")
                Spacer(modifier = Modifier.height(6.dp))
                BrutalTextField(value = email, onValueChange = { email = it }, label = "E-MAIL CORPORATIVO")
                Spacer(modifier = Modifier.height(6.dp))
                BrutalTextField(value = role, onValueChange = { role = it }, label = "CARGO (Vendedor, Estoquista, Gerente, RH)")

                Spacer(modifier = Modifier.height(14.dp))

                BrutalButton(
                    text = "CADASTRAR",
                    onClick = {
                        if (name.isNotEmpty() && email.isNotEmpty()) {
                            onConfirm(name, email, role)
                        }
                    },
                    backgroundColor = BrandRed,
                    textColor = BrandWhite,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    badge: String,
    modifier: Modifier = Modifier
) {
    BrutalCard(
        modifier = modifier,
        backgroundColor = BrandWhite,
        borderColor = BrandBorder,
        borderWidth = 1.5.dp,
        shadowOffset = 2.dp
    ) {
        Text(
            text = title,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            color = Color.DarkGray
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Black,
            fontSize = 16.sp,
            color = BrandBlack
        )
        Spacer(modifier = Modifier.height(6.dp))
        BrutalBadge(
            text = badge,
            backgroundColor = BrandYellow,
            textColor = BrandBlack,
            borderColor = BrandBorder
        )
    }
}

@Composable
fun StaffHeader(
    title: String,
    role: String,
    onLogout: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, BrandBorder)
            .background(BrandWhite)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                color = BrandBlack
            )
            Text(
                text = "CARGO ATIVO: $role",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                color = BrandRed
            )
        }
        BrutalButton(
            text = "SAIR / LOJA",
            onClick = onLogout,
            backgroundColor = BrandBlack,
            textColor = BrandWhite,
            shadowOffset = 2.dp
        )
    }
}
