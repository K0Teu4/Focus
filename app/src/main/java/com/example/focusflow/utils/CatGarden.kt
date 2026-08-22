package com.example.focusflow.utils

/** Все показатели для достижений */
data class GardenStats(
    val pomodoros: Int = 0,
    val streak: Int = 0,
    val focusMinutes: Int = 0,
    val tasksDone: Int = 0,
    val earlyBirds: Int = 0,      // сессии до 7:00
    val nightOwls: Int = 0,        // сессии после 23:00
    val deepFocusCount: Int = 0    // завершённые сессии без пауз
)

data class CatAchievement(
    val id: String,
    val emoji: String,
    val name: String,
    val desc: String,
    val threshold: Int,
    val kind: Kind
) {
    enum class Kind { POMODOROS, STREAK, FOCUS_MINUTES, TASKS, EARLY_BIRDS, NIGHT_OWLS, DEEP_FOCUS }
    
    fun progress(stats: GardenStats): Int = when (kind) {
        Kind.POMODOROS -> stats.pomodoros
        Kind.STREAK -> stats.streak
        Kind.FOCUS_MINUTES -> stats.focusMinutes
        Kind.TASKS -> stats.tasksDone
        Kind.EARLY_BIRDS -> stats.earlyBirds
        Kind.NIGHT_OWLS -> stats.nightOwls
        Kind.DEEP_FOCUS -> stats.deepFocusCount
    }
    
    fun isUnlocked(stats: GardenStats): Boolean = progress(stats) >= threshold
    
    fun unitLabel(): String = when (kind) {
        Kind.POMODOROS -> "🍅"
        Kind.STREAK -> "дн."
        Kind.FOCUS_MINUTES -> "мин"
        Kind.TASKS -> "задач"
        Kind.EARLY_BIRDS -> "утр"
        Kind.NIGHT_OWLS -> "ноч"
        Kind.DEEP_FOCUS -> "сесс"
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
        CatAchievement("hour2", "🏃", "Марафон", "120 минут — марафонская дистанция!", 120, CatAchievement.Kind.FOCUS_MINUTES),
        CatAchievement("hour5", "🕔", "Пять часов", "300 минут — кот впечатлён.", 300, CatAchievement.Kind.FOCUS_MINUTES),
        CatAchievement("hour25", "🕰️", "25 часов", "1500 минут — почти сутки фокуса!", 1500, CatAchievement.Kind.FOCUS_MINUTES),
        CatAchievement("hour100", "💯", "100 часов", "6000 минут — сотня часов!", 6000, CatAchievement.Kind.FOCUS_MINUTES),
        
        // ✅ Задачи
        CatAchievement("task1", "✅", "Первая задача", "Заверши первую задачу.", 1, CatAchievement.Kind.TASKS),
        CatAchievement("task10", "📚", "Десяток задач", "10 выполненных задач.", 10, CatAchievement.Kind.TASKS),
        CatAchievement("task50", "🎓", "Полсотни задач", "50 задач — выпускной кота!", 50, CatAchievement.Kind.TASKS),
        
        // 🌅 Ранние пташки
        CatAchievement("early1", "🌅", "Ранняя пташка", "Первая сессия до 7:00 — доброе утро!", 1, CatAchievement.Kind.EARLY_BIRDS),
        CatAchievement("early5", "☀️", "Жаворонок", "5 ранних сессий — ты на высоте.", 5, CatAchievement.Kind.EARLY_BIRDS),
        CatAchievement("early10", "🌄", "Рассветный кот", "10 ранних сессий — мастер утра.", 10, CatAchievement.Kind.EARLY_BIRDS),
        
        // 🌙 Ночные совы
        CatAchievement("night1", "🌙", "Полночник", "Первая сессия после 23:00 — ночная смена!", 1, CatAchievement.Kind.NIGHT_OWLS),
        CatAchievement("night5", "🦉", "Сова", "5 ночных сессий — ты мудр.", 5, CatAchievement.Kind.NIGHT_OWLS),
        CatAchievement("night10", "🌃", "Ночной дозор", "10 ночных сессий — страж ночи.", 10, CatAchievement.Kind.NIGHT_OWLS),
        
        // 🧘 Чистый фокус
        CatAchievement("deep1", "🧘", "Первый чистый фокус", "Завершённая сессия без пауз.", 1, CatAchievement.Kind.DEEP_FOCUS),
        CatAchievement("deep5", "💎", "Кристальный фокус", "5 чистых сессий — концентрация.", 5, CatAchievement.Kind.DEEP_FOCUS),
        CatAchievement("deep10", "🔮", "Чистый фокус ×10", "10 завершённых сессий — мастер медитации!", 10, CatAchievement.Kind.DEEP_FOCUS)
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
    
    /** Настроение кота на основе сегодняшнего прогресса */
    fun catMood(pomodorosToday: Int, dailyGoal: Int): String {
        if (pomodorosToday == 0) return "😿"  // голодный
        if (pomodorosToday >= dailyGoal) return "😻"  // счастлив
        return "😺"  // доволен
    }
}