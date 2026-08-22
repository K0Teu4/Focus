package com.example.focusflow.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focusflow.ui.theme.AppColors
import com.example.focusflow.viewmodel.PremiumViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val SHOP_URL = "https://k0teu4.github.io/focusflow-privacy/shop.html"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
    onBack: () -> Unit,
    appColors: AppColors,
    viewModel: PremiumViewModel = viewModel()
) {
    val isPremium by viewModel.isPremium.collectAsState()
    val expiresAt by viewModel.premiumExpiresAt.collectAsState()
    var previewPaywall by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var code by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    val expiresText = remember(expiresAt) {
        if (expiresAt == 0L) "Бессрочно"
        else SimpleDateFormat("dd.MM.yyyy", Locale("ru")).format(Date(expiresAt))
    }

    val openShop: () -> Unit = remember {
        {
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(SHOP_URL)))
            } catch (_: Exception) { }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Premium", color = appColors.text) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = appColors.text
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = appColors.surface)
            )
        },
        containerColor = androidx.compose.ui.graphics.Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isPremium && !previewPaywall) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = appColors.success.copy(alpha = 0.15f)
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("★", color = appColors.success, fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Premium активен",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = appColors.success,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Спасибо за поддержку разработчика!",
                                    color = appColors.text,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = appColors.success.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(12.dp))

                        PremiumInfoRow("Статус", "Активен", appColors.success, appColors)
                        Spacer(modifier = Modifier.height(8.dp))
                        PremiumInfoRow("Способ", "Код активации", appColors.text, appColors)
                        Spacer(modifier = Modifier.height(8.dp))
                        PremiumInfoRow("Действует до", expiresText, appColors.text, appColors)
                    }
                }

                OutlinedButton(
                    onClick = openShop,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = appColors.primary)
                ) {
                    Icon(Icons.Outlined.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Купить код в подарок — 399 ₽")
                }

                TextButton(
                    onClick = { previewPaywall = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Предпросмотр экрана покупки", color = appColors.textSecondary)
                }
            } else {
                if (isPremium) {
                    TextButton(onClick = { previewPaywall = false }) {
                        Text("← Вернуться к статусу", color = appColors.textSecondary)
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.Palette,
                        contentDescription = null,
                        tint = appColors.primary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "FocusFlow Premium",
                        style = MaterialTheme.typography.headlineMedium,
                        color = appColors.text,
                        fontWeight = FontWeight.Bold
                    )
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = appColors.surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        PremiumFeature("4 эксклюзивные темы: Океан, Закат, Лес, Монохром", appColors)
                        Spacer(modifier = Modifier.height(10.dp))
                        PremiumFeature("Фоновые звуки для фокуса: белый, розовый, глубокий шум", appColors)
                        Spacer(modifier = Modifier.height(10.dp))
                        PremiumFeature("Авто-тема по времени суток (появится в обновлении)", appColors)
                        Spacer(modifier = Modifier.height(10.dp))
                        PremiumFeature("Поддержка развития приложения", appColors)
                    }
                }

                Button(
                    onClick = openShop,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = appColors.primary),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Icon(
                        Icons.Outlined.ShoppingCart,
                        contentDescription = null,
                        tint = if (appColors.mode == "dark") appColors.bg else androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Купить код за 399 ₽",
                        color = if (appColors.mode == "dark") appColors.bg else androidx.compose.ui.graphics.Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    "Оплата через СБП или карту. Код выдаётся сразу после оплаты.",
                    style = MaterialTheme.typography.bodySmall,
                    color = appColors.textSecondary
                )

                HorizontalDivider(color = appColors.surface2)

                Text(
                    "У меня есть код",
                    style = MaterialTheme.typography.titleMedium,
                    color = appColors.text,
                    fontWeight = FontWeight.Medium
                )

                OutlinedTextField(
                    value = code,
                    onValueChange = {
                        code = it.uppercase()
                        error = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("XXXX-XXXX-XXXX-XXXX", color = appColors.textSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = appColors.text,
                        unfocusedTextColor = appColors.text,
                        focusedBorderColor = appColors.primary,
                        unfocusedBorderColor = appColors.resetBorder
                    )
                )

                if (error) {
                    Text(
                        "Неверный код активации",
                        color = appColors.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = {
                        viewModel.activateCode(code) { ok ->
                            if (ok) {
                                code = ""
                                error = false
                                previewPaywall = false
                            } else {
                                error = true
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = appColors.surface2)
                ) {
                    Text("Активировать", color = appColors.text, fontWeight = FontWeight.Bold)
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = appColors.surface)
                ) {
                    Text(
                        "Купили ранее? Код активации сохранён в письме об оплате и в чеке — введите его снова в поле выше.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = appColors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumInfoRow(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color, appColors: AppColors) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = appColors.textSecondary, style = MaterialTheme.typography.bodyMedium)
        Text(value, color = valueColor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun PremiumFeature(text: String, appColors: AppColors) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Outlined.CheckCircle,
            contentDescription = null,
            tint = appColors.success,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(text, color = appColors.text, style = MaterialTheme.typography.bodyMedium)
    }
}