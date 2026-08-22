package com.example.focusflow.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

data class AppColors(
    val bg: Color,
    val bgGradientStart: Color,
    val bgGradientEnd: Color,
    val surface: Color,
    val surface2: Color,
    val primary: Color,
    val text: Color,
    val textSecondary: Color,
    val pauseWork: Color,
    val pauseRest: Color,
    val resetBorder: Color,
    val skip: Color,
    val work: Color,
    val rest: Color,
    val longBreak: Color,
    val success: Color,
    val error: Color,
    val catWork: Color,
    val catRest: Color,
    val catHobby: Color,
    val catStudy: Color,
    val mode: String
)

object AppTheme {
    val THEME_DISPLAY = mapOf(
        "dark" to "Тёмная",
        "light" to "Светлая",
        "ocean" to "Океан",
        "sunset" to "Закат",
        "forest" to "Лес",
        "mono" to "Монохром"
    )

    val PREMIUM_THEMES = setOf("ocean", "sunset", "forest", "mono")

    val THEMES = mapOf(
        // ТЁМНАЯ — Tokyo Night (ночное небо)
        "dark" to AppColors(
            bg = Color(0xFF1A1B26),
            bgGradientStart = Color(0xFF20263F),
            bgGradientEnd = Color(0xFF14151D),
            surface = Color(0xFF24283B),
            surface2 = Color(0xFF2F3349),
            primary = Color(0xFF7AA2F7),
            text = Color(0xFFC0CAF5),
            textSecondary = Color(0xFF565F89),
            pauseWork = Color(0xFFFF9E64),
            pauseRest = Color(0xFF7DCFFF),
            resetBorder = Color(0xFF3B4261),
            skip = Color(0xFF565F89),
            work = Color(0xFFF7768E),
            rest = Color(0xFF7DCFFF),
            longBreak = Color(0xFFBB9AF7),
            success = Color(0xFF9ECE6A),
            error = Color(0xFFF7768E),
            catWork = Color(0xFFF7768E),
            catRest = Color(0xFF7DCFFF),
            catHobby = Color(0xFF9ECE6A),
            catStudy = Color(0xFFE0AF68),
            mode = "dark"
        ),

        // СВЕТЛАЯ — тёплая «бумага»
        "light" to AppColors(
            bg = Color(0xFFE8E4DC),
            bgGradientStart = Color(0xFFF5F2EA),
            bgGradientEnd = Color(0xFFDFD9CD),
            surface = Color(0xFFF2EFE8),
            surface2 = Color(0xFFDDD8CE),
            primary = Color(0xFF5B8DEF),
            text = Color(0xFF4A463E),
            textSecondary = Color(0xFF8A8478),
            pauseWork = Color(0xFFE08A4C),
            pauseRest = Color(0xFF3FA796),
            resetBorder = Color(0xFFC7C1B5),
            skip = Color(0xFF8A8478),
            work = Color(0xFFE0556B),
            rest = Color(0xFF3FA796),
            longBreak = Color(0xFF9B7EDE),
            success = Color(0xFF5BA85B),
            error = Color(0xFFE0556B),
            catWork = Color(0xFFE0556B),
            catRest = Color(0xFF3FA796),
            catHobby = Color(0xFF5BA85B),
            catStudy = Color(0xFFD99A3C),
            mode = "light"
        ),

        // ОКЕАН — глубокая вода
        "ocean" to AppColors(
            bg = Color(0xFF081820),
            bgGradientStart = Color(0xFF0B2836),
            bgGradientEnd = Color(0xFF041018),
            surface = Color(0xFF0E2733),
            surface2 = Color(0xFF143443),
            primary = Color(0xFF2DD4BF),
            text = Color(0xFFD6F5F0),
            textSecondary = Color(0xFF5E8A8F),
            pauseWork = Color(0xFFFB923C),
            pauseRest = Color(0xFF38BDF8),
            resetBorder = Color(0xFF1E4452),
            skip = Color(0xFF5E8A8F),
            work = Color(0xFFFB7185),
            rest = Color(0xFF38BDF8),
            longBreak = Color(0xFF818CF8),
            success = Color(0xFF34D399),
            error = Color(0xFFFB7185),
            catWork = Color(0xFFFB7185),
            catRest = Color(0xFF38BDF8),
            catHobby = Color(0xFF34D399),
            catStudy = Color(0xFFFCD34D),
            mode = "dark"
        ),

        // ЗАКАТ — Rosé Pine
        "sunset" to AppColors(
            bg = Color(0xFF191724),
            bgGradientStart = Color(0xFF2C2542),
            bgGradientEnd = Color(0xFF15121C),
            surface = Color(0xFF1F1D2E),
            surface2 = Color(0xFF26233A),
            primary = Color(0xFFF6C177),
            text = Color(0xFFE0DEF4),
            textSecondary = Color(0xFF908CAA),
            pauseWork = Color(0xFFF6C177),
            pauseRest = Color(0xFF9CCFD8),
            resetBorder = Color(0xFF403D52),
            skip = Color(0xFF908CAA),
            work = Color(0xFFEB6F92),
            rest = Color(0xFF9CCFD8),
            longBreak = Color(0xFFC4A7E7),
            success = Color(0xFF31748F),
            error = Color(0xFFEB6F92),
            catWork = Color(0xFFEB6F92),
            catRest = Color(0xFF9CCFD8),
            catHobby = Color(0xFF31748F),
            catStudy = Color(0xFFF6C177),
            mode = "dark"
        ),

        // ЛЕС — Everforest
        "forest" to AppColors(
            bg = Color(0xFF2D353B),
            bgGradientStart = Color(0xFF37454B),
            bgGradientEnd = Color(0xFF20272A),
            surface = Color(0xFF343F44),
            surface2 = Color(0xFF3D484D),
            primary = Color(0xFFA7C080),
            text = Color(0xFFD3C6AA),
            textSecondary = Color(0xFF859289),
            pauseWork = Color(0xFFE69875),
            pauseRest = Color(0xFF83C092),
            resetBorder = Color(0xFF4F585E),
            skip = Color(0xFF859289),
            work = Color(0xFFE67E80),
            rest = Color(0xFF83C092),
            longBreak = Color(0xFF7FBBB3),
            success = Color(0xFFA7C080),
            error = Color(0xFFE67E80),
            catWork = Color(0xFFE67E80),
            catRest = Color(0xFF83C092),
            catHobby = Color(0xFFA7C080),
            catStudy = Color(0xFFDBBC7F),
            mode = "dark"
        ),

        // МОНОХРОМ
        "mono" to AppColors(
            bg = Color(0xFF161616),
            bgGradientStart = Color(0xFF262626),
            bgGradientEnd = Color(0xFF0D0D0D),
            surface = Color(0xFF222222),
            surface2 = Color(0xFF2C2C2C),
            primary = Color(0xFFD4D4D4),
            text = Color(0xFFF0F0F0),
            textSecondary = Color(0xFF808080),
            pauseWork = Color(0xFFB0B0B0),
            pauseRest = Color(0xFF808080),
            resetBorder = Color(0xFF404040),
            skip = Color(0xFF606060),
            work = Color(0xFFFFFFFF),
            rest = Color(0xFFA0A0A0),
            longBreak = Color(0xFF707070),
            success = Color(0xFF4CAF50),
            error = Color(0xFFF44336),
            catWork = Color(0xFFFFFFFF),
            catRest = Color(0xFFA0A0A0),
            catHobby = Color(0xFF707070),
            catStudy = Color(0xFFD0D0D0),
            mode = "dark"
        )
    )

    fun getThemeColors(themeName: String): AppColors {
        return THEMES[themeName] ?: THEMES["dark"]!!
    }

    fun getColorScheme(themeName: String): ColorScheme {
        val colors = getThemeColors(themeName)
        return if (colors.mode == "dark") {
            darkColorScheme(
                primary = colors.primary,
                secondary = colors.primary,
                tertiary = colors.work,
                background = colors.bg,
                surface = colors.surface,
                onPrimary = colors.bg,
                onSecondary = colors.bg,
                onTertiary = colors.bg,
                onBackground = colors.text,
                onSurface = colors.text,
                surfaceVariant = colors.surface2,
                onSurfaceVariant = colors.textSecondary
            )
        } else {
            lightColorScheme(
                primary = colors.primary,
                secondary = colors.primary,
                tertiary = colors.work,
                background = colors.bg,
                surface = colors.surface,
                onPrimary = Color.White,
                onSecondary = Color.White,
                onTertiary = Color.White,
                onBackground = colors.text,
                onSurface = colors.text,
                surfaceVariant = colors.surface2,
                onSurfaceVariant = colors.textSecondary
            )
        }
    }
}