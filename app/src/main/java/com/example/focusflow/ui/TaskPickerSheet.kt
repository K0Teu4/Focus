package com.example.focusflow.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focusflow.data.model.Categories
import com.example.focusflow.data.model.TaskEntity
import com.example.focusflow.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskPickerSheet(
    tasks: List<TaskEntity>,
    selectedTaskId: Long?,
    onTaskSelected: (TaskEntity?) -> Unit,
    onDismiss: () -> Unit,
    appColors: AppColors,
    taskCounts: Map<Long, Int> = emptyMap()
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    val categoryColors = mapOf(
        Categories.WORK to appColors.catWork,
        Categories.REST to appColors.catRest,
        Categories.HOBBY to appColors.catHobby,
        Categories.STUDY to appColors.catStudy
    )

    val filteredTasks = tasks.filter { task ->
        val matchesSearch = searchQuery.isBlank() ||
            task.title.lowercase().contains(searchQuery.lowercase())
        val matchesCategory = selectedCategory == null || task.category == selectedCategory
        matchesSearch && matchesCategory
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = appColors.surface,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Выбрать задачу",
                    style = MaterialTheme.typography.titleLarge,
                    color = appColors.text,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Закрыть",
                        tint = appColors.text
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Поиск задач...", color = appColors.textSecondary) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = appColors.textSecondary)
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = appColors.text,
                    unfocusedTextColor = appColors.text,
                    focusedBorderColor = appColors.primary,
                    unfocusedBorderColor = appColors.resetBorder
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("Все") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = appColors.primary,
                        selectedLabelColor = appColors.bg
                    )
                )
                Categories.ALL.forEach { category ->
                    val color = categoryColors[category] ?: appColors.primary
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = {
                            selectedCategory = if (selectedCategory == category) null else category
                        },
                        label = { Text(Categories.LABELS[category] ?: category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = color,
                            selectedLabelColor = appColors.bg
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTaskSelected(null) },
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedTaskId == null) appColors.primary.copy(alpha = 0.2f) else appColors.surface2
                ),
                border = if (selectedTaskId == null) {
                    androidx.compose.foundation.BorderStroke(2.dp, appColors.primary)
                } else null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Без задачи",
                        style = MaterialTheme.typography.bodyLarge,
                        color = appColors.text,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Нет задач", color = appColors.textSecondary)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        TaskPickerItem(
                            task = task,
                            isSelected = task.id == selectedTaskId,
                            onClick = { onTaskSelected(task) },
                            appColors = appColors,
                            categoryColors = categoryColors,
                            pomodoroCount = taskCounts[task.id] ?: 0
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskPickerItem(
    task: TaskEntity,
    isSelected: Boolean,
    onClick: () -> Unit,
    appColors: AppColors,
    categoryColors: Map<String, Color>,
    pomodoroCount: Int
) {
    val categoryColor = categoryColors[task.category] ?: appColors.primary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) appColors.primary.copy(alpha = 0.2f) else appColors.surface2
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, appColors.primary)
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp, 40.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(categoryColor)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = appColors.text,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = categoryColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = Categories.LABELS[task.category] ?: task.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = categoryColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (pomodoroCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🍅 $pomodoroCount",
                            style = MaterialTheme.typography.labelSmall,
                            color = appColors.textSecondary
                        )
                    }

                    if (task.isDone) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Выполнена",
                            style = MaterialTheme.typography.labelSmall,
                            color = appColors.success
                        )
                    }
                }
            }

            if (isSelected) {
                Text(
                    text = "✓",
                    fontSize = 24.sp,
                    color = appColors.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}