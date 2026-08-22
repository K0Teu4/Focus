package com.example.focusflow.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focusflow.ui.theme.AppColors
import kotlinx.coroutines.launch
import kotlin.math.abs

private data class OnboardingPageData(
    val icon: ImageVector,
    val title: String,
    val text: String
)

private val PAGES = listOf(
    OnboardingPageData(
        Icons.Outlined.Timer,
        "Фокус без лишнего",
        "Pomodoro-таймер с гибкой настройкой длительности работы и перерывов. Запускайте сессии в один тап."
    ),
    OnboardingPageData(
        Icons.Outlined.TaskAlt,
        "Задачи и категории",
        "Планируйте задачи и разделяйте их по категориям: работа, учёба, отдых, хобби. Категория сама подскажет тип сессии."
    ),
    OnboardingPageData(
        Icons.Outlined.BarChart,
        "Статистика и цели",
        "Серия дней, график активности, цель на день и сравнение периодов — вся ваша продуктивность на одном экране."
    ),
    OnboardingPageData(
        Icons.Outlined.Palette,
        "Сделайте своим",
        "6 тем оформления, звуки, вибрация и автозапуск. Настройте приложение под себя и начинайте."
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    appColors: AppColors
) {
    val pagerState = rememberPagerState(pageCount = { PAGES.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == PAGES.size - 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Пропустить
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onFinish) {
                Text("Пропустить", color = appColors.textSecondary)
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
            val alpha = (1f - abs(pageOffset)).coerceIn(0f, 1f)

            OnboardingPageContent(
                data = PAGES[page],
                appColors = appColors,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { this.alpha = alpha }
            )
        }

        // Точки
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(PAGES.size) { i ->
                Box(
                    modifier = Modifier
                        .size(if (i == pagerState.currentPage) 10.dp else 7.dp)
                        .clip(CircleShape)
                        .background(
                            if (i == pagerState.currentPage) appColors.primary
                            else appColors.textSecondary.copy(alpha = 0.3f)
                        )
                )
                if (i < PAGES.size - 1) Spacer(modifier = Modifier.width(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (pagerState.currentPage > 0) {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        contentColor = appColors.text
                    )
                ) {
                    Text("Назад")
                }
            }

            Button(
                onClick = {
                    if (isLastPage) onFinish()
                    else scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                },
                modifier = Modifier.weight(2f),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = appColors.primary
                )
            ) {
                Text(
                    if (isLastPage) "Начать" else "Далее",
                    color = if (appColors.mode == "dark") appColors.bg else androidx.compose.ui.graphics.Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(
    data: OnboardingPageData,
    appColors: AppColors,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(appColors.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Icon(
                imageVector = data.icon,
                contentDescription = null,
                tint = appColors.primary,
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = data.title,
            style = MaterialTheme.typography.headlineMedium,
            color = appColors.text,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = data.text,
            style = MaterialTheme.typography.bodyLarge,
            color = appColors.textSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}