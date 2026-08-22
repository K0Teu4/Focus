package com.example.focusflow.utils

/** Все показатели для достижений */
data class GardenStats(
    val pomodoros: Int = 0,
    val streak: Int = 0,
    val focusMinutes: Int = 0,
    val tasksDone: Int = 0
)

data class CatAchievement(
    val id: String,
    val emoji: String,
    val name: String,
    val desc: String,
    val threshold: Int,
    val kind: Kind
) {
    enum class Kind { POMODOROS, STREAK, FOCUS_MINUTES, TASKS }

    fun progress(stats: GardenStats): Int = when (kind) {
        Kind.POMODOROS -> stats.pomodoros
        Kind.STREAK -> stats.streak
        Kind.FOCUS_MINUTES -> stats.focusMinutes
        Kind.TASKS -> stats.tasksDone
    }

    fun isUnlocked(stats: GardenStats): Boolean = progress(stats) >= threshold

    fun unitLabel(): String = when (kind) {
        Kind.POMODOROS -> "🍅"
        Kind.STREAK -> "дн."
        Kind.FOCUS_MINUTES -> "мин"
        Kind.TASKS -> "задач"
    }
}

object CatGarden {

    val achievements: List<CatAchievement> = listOf(
        // 🍅 Помидоры
        CatAchievement("kitten", "🐱", "Котёнок", "Первый помидор — начало пути!", 1, CatAchievement.Kind.POMODOROS),
        CatAchievement("cheerful", "😺", "Весельчак", "5 помидоров — кот доволен.", 5, CatAchievement.Kind.POMODOROS),
        CatAchievement("worker", "😸", "Трудяга", "10 помидоров — серьёзный настрой.", 10, CatAchievement.Kind.POMODOROS),
        CatAchievement("walker", "🐈", "Гуляка", "25 помидоров — кот гуляет по саду.", 25, CatAchievement.Kind.POMODOROS),
        CatAchievement("love", "😻", "Влюблён в фокус", "50 помидоров — это любовь.", 50, CatAchievement.Kind.POMODOROS),
        CatAchievement("shadow", "🐈‍⬛", "Тень", "75 помидоров — кот всегда рядом.", 75, CatAchievement.Kind.POMODOROS),
        CatAchievement("strict_cat", "😼", "Строгий кот", "100 помидоров — дисциплина!", 100, CatAchievement.Kind.POMODOROS),
        CatAchievement("lion", "🦁", "Лев продуктивности", "200 помидоров — мощь!", 200, CatAchievement.Kind.POMODOROS),
        CatAchievement("tiger", "🐅", "Тигр", "350 помидоров — полосатая победа.", 350, CatAchievement.Kind.POMODOROS),
        CatAchievement("master", "🏆", "Хозяин сада", "500 помидоров — легенда!", 500, CatAchievement.Kind.POMODOROS),
        // 🔥 Серии
        CatAchievement("fire3", "🔥", "3 дня подряд", "Три дня с сессиями подряд.", 3, CatAchievement.Kind.STREAK),
        CatAchievement("bolt7", "⚡", "Неделя подряд", "Семь дней — ударная неделя!", 7, CatAchievement.Kind.STREAK),
        CatAchievement("star30", "🌟", "Месяц подряд", "30 дней — ты машина!", 30, CatAchievement.Kind.STREAK),
        // ⏰ Время фокуса
        CatAchievement("hour1", "⏰", "Час фокуса", "60 минут суммарного фокуса.", 60, CatAchievement.Kind.FOCUS_MINUTES),
        CatAchievement("hour5", "🕔", "Пять часов", "300 минут — кот впечатлён.", 300, CatAchievement.Kind.FOCUS_MINUTES),
        CatAchievement("hour25", "🕰️", "25 часов", "1500 минут — почти сутки фокуса!", 1500, CatAchievement.Kind.FOCUS_MINUTES),
        CatAchievement("hour100", "💯", "100 часов", "6000 минут — сотня часов!", 6000, CatAchievement.Kind.FOCUS_MINUTES),
        // ✅ Задачи
        CatAchievement("task1", "✅", "Первая задача", "Заверши первую задачу.", 1, CatAchievement.Kind.TASKS),
        CatAchievement("task10", "📚", "Десяток задач", "10 выполненных задач.", 10, CatAchievement.Kind.TASKS),
        CatAchievement("task50", "🎓", "Полсотни задач", "50 задач — выпускной кота!", 50, CatAchievement.Kind.TASKS)
    )

    fun unlockedCount(stats: GardenStats): Int = achievements.count { it.isUnlocked(stats) }

    /** Ближайшее незакрытое достижение за помидоры */
    fun nextPomodoro(pomodoros: Int): CatAchievement? =
        achievements
            .filter { it.kind == CatAchievement.Kind.POMODOROS && pomodoros < it.threshold }
            .minByOrNull { it.threshold }

    /** Ближайшее незакрытое достижение любого типа */
    fun nearest(stats: GardenStats): CatAchievement? =
        achievements
            .filter { !it.isUnlocked(stats) }
            .minByOrNull { it.threshold - it.progress(stats) }
}