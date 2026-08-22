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
    val showPaywall = !isPremium || previewPaywall
    val context = LocalContext.current

    var code by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    val expiresText = remember(expiresAt) {
        if (expiresAt == 0L) "Р‘РµСЃСЃСЂРѕС‡РЅРѕ"
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
                            contentDescription = "РќР°Р·Р°Рґ",
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
                            Text("в…", color = appColors.success, fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Premium Р°РєС‚РёРІРµРЅ",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = appColors.success,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "РЎРїР°СЃРёР±Рѕ Р·Р° РїРѕРґРґРµСЂР¶РєСѓ СЂР°Р·СЂР°Р±РѕС‚С‡РёРєР°!",
                                    color = appColors.text,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = appColors.success.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(12.dp))

                        PremiumInfoRow("РЎС‚Р°С‚СѓСЃ", "РђРєС‚РёРІРµРЅ", appColors.success, appColors)
                        Spacer(modifier = Modifier.height(8.dp))
                        PremiumInfoRow("РЎРїРѕСЃРѕР±", "РљРѕРґ Р°РєС‚РёРІР°С†РёРё", appColors.text, appColors)
                        Spacer(modifier = Modifier.height(8.dp))
                        PremiumInfoRow("Р”РµР№СЃС‚РІСѓРµС‚ РґРѕ", expiresText, appColors.text, appColors)
                    }
                }

                OutlinedButton(
                    onClick = openShop,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = appColors.primary)
                ) {
                    Icon(Icons.Outlined.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("РљСѓРїРёС‚СЊ РєРѕРґ РІ РїРѕРґР°СЂРѕРє вЂ” 399 в‚Ѕ")
                }

                TextButton(
                    onClick = { previewPaywall = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("РџСЂРµРґРїСЂРѕСЃРјРѕС‚СЂ СЌРєСЂР°РЅР° РїРѕРєСѓРїРєРё", color = appColors.textSecondary)
                }
            } else {
                if (isPremium) {
                    TextButton(onClick = { previewPaywall = false }) {
                        Text("в†ђ Р’РµСЂРЅСѓС‚СЊСЃСЏ Рє СЃС‚Р°С‚СѓСЃСѓ", color = appColors.textSecondary)
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
                        PremiumFeature("4 СЌРєСЃРєР»СЋР·РёРІРЅС‹Рµ С‚РµРјС‹: РћРєРµР°РЅ, Р—Р°РєР°С‚, Р›РµСЃ, РњРѕРЅРѕС…СЂРѕРј", appColors)
                        Spacer(modifier = Modifier.height(10.dp))
                        PremiumFeature("Р¤РѕРЅРѕРІС‹Рµ Р·РІСѓРєРё РґР»СЏ С„РѕРєСѓСЃР°: Р±РµР»С‹Р№, СЂРѕР·РѕРІС‹Р№, РіР»СѓР±РѕРєРёР№ С€СѓРј", appColors)
                        Spacer(modifier = Modifier.height(10.dp))
                        PremiumFeature("РђРІС‚Рѕ-С‚РµРјР° РїРѕ РІСЂРµРјРµРЅРё СЃСѓС‚РѕРє (РїРѕСЏРІРёС‚СЃСЏ РІ РѕР±РЅРѕРІР»РµРЅРёРё)", appColors)
                        Spacer(modifier = Modifier.height(10.dp))
                        PremiumFeature("РџРѕРґРґРµСЂР¶РєР° СЂР°Р·РІРёС‚РёСЏ РїСЂРёР»РѕР¶РµРЅРёСЏ", appColors)
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
                        "РљСѓРїРёС‚СЊ РєРѕРґ Р·Р° 399 в‚Ѕ",
                        color = if (appColors.mode == "dark") appColors.bg else androidx.compose.ui.graphics.Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    "РћРїР»Р°С‚Р° С‡РµСЂРµР· РЎР‘Рџ РёР»Рё РєР°СЂС‚Сѓ. РљРѕРґ РІС‹РґР°С‘С‚СЃСЏ СЃСЂР°Р·Сѓ РїРѕСЃР»Рµ РѕРїР»Р°С‚С‹.",
                    style = MaterialTheme.typography.bodySmall,
                    color = appColors.textSecondary
                )

                HorizontalDivider(color = appColors.surface2)

                Text(
                    "РЈ РјРµРЅСЏ РµСЃС‚СЊ РєРѕРґ",
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
                        "РќРµРІРµСЂРЅС‹Р№ РєРѕРґ Р°РєС‚РёРІР°С†РёРё",
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
                    Text("РђРєС‚РёРІРёСЂРѕРІР°С‚СЊ", color = appColors.text, fontWeight = FontWeight.Bold)
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = appColors.surface)
                ) {
                    Text(
                        "РљСѓРїРёР»Рё СЂР°РЅРµРµ? РљРѕРґ Р°РєС‚РёРІР°С†РёРё СЃРѕС…СЂР°РЅС‘РЅ РІ РїРёСЃСЊРјРµ РѕР± РѕРїР»Р°С‚Рµ Рё РІ С‡РµРєРµ вЂ” РІРІРµРґРёС‚Рµ РµРіРѕ СЃРЅРѕРІР° РІ РїРѕР»Рµ РІС‹С€Рµ.",
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