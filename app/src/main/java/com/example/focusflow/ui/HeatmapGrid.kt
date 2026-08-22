package com.example.focusflow.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focusflow.ui.theme.AppColors
import com.example.focusflow.viewmodel.DailyActivity

private const val CELL_HEIGHT = 18

@Composable
fun HeatmapGrid(
    activities: List<DailyActivity>,
    appColors: AppColors,
    modifier: Modifier = Modifier
) {
    val maxMinutes = activities.maxOfOrNull { it.workMinutes } ?: 0f
    val weeks = activities.chunked(7).takeLast(13)
    val dayLabels = listOf("Пн", "", "Ср", "", "Пт", "", "Вс")

    // Строки = дни недели: подпись и ячейки в ОДНОМ Row — рассинхрон невозможен
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        for (dayIndex in 0 until 7) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(22.dp)
                        .height(CELL_HEIGHT.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = dayLabels[dayIndex],
                        fontSize = 8.sp,
                        color = appColors.textSecondary
                    )
                }

                weeks.forEach { week ->
                    val day = week.getOrNull(dayIndex)
                    if (day != null) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(CELL_HEIGHT.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(heatColor(day.workMinutes, maxMinutes, appColors))
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f).height(CELL_HEIGHT.dp))
                    }
                }
            }
        }
    }
}

private fun heatColor(minutes: Float, max: Float, appColors: AppColors): Color {
    if (minutes <= 0f) return appColors.textSecondary.copy(alpha = 0.12f)
    val intensity = (minutes / max).coerceIn(0.25f, 1f)
    return appColors.work.copy(alpha = intensity)
}