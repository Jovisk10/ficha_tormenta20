package com.jovis.t20.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.jovis.t20.R

/*
 * Identidade visual inspirada nas páginas do Tormenta 20 Jogo do Ano:
 *  - papel quase branco e tabelas em faixas de cinza quente
 *  - vermelho da Tormenta nos títulos e vermelho-sangue nas faixas e bordas rasgadas
 *  - títulos em versaletes (Cinzel), texto em serifa (Crimson Pro) e dados em sem serifa (Source Sans 3)
 * As fontes do livro são proprietárias; estas são livres (licença OFL) e próximas em estilo.
 *
 * Papéis das cores:
 *  - primary: vermelho da Tormenta (títulos, botões)
 *  - primaryContainer: vermelho-sangue (faixa de atributos)
 *  - tertiary: vermelho vivo, reservado para Pontos de Vida
 *  - secondary: ouro velho, reservado para Pontos de Mana
 *  - surfaceContainer / surfaceContainerHighest: as faixas das tabelas
 */

object T20Fonts {
    val display = FontFamily(
        Font(R.font.cinzel_semibold, FontWeight.SemiBold),
        Font(R.font.cinzel_bold, FontWeight.Bold),
    )
    val serif = FontFamily(
        Font(R.font.crimson_pro_regular, FontWeight.Normal),
        Font(R.font.crimson_pro_semibold, FontWeight.SemiBold),
        Font(R.font.crimson_pro_bold, FontWeight.Bold),
    )
    val sans = FontFamily(
        Font(R.font.source_sans_regular, FontWeight.Normal),
        Font(R.font.source_sans_semibold, FontWeight.SemiBold),
        Font(R.font.source_sans_bold, FontWeight.Bold),
    )
}

private val PaperLight = lightColorScheme(
    primary = Color(0xFFA82828),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF481212),
    onPrimaryContainer = Color(0xFFF4E6E3),
    secondary = Color(0xFF8A6A1F),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFEFE4C8),
    onSecondaryContainer = Color(0xFF3D2E08),
    tertiary = Color(0xFFC42A24),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF6DCD9),
    onTertiaryContainer = Color(0xFF4A0F0C),
    background = Color(0xFFF7F5F6),
    onBackground = Color(0xFF201A1A),
    surface = Color(0xFFFCFBFB),
    onSurface = Color(0xFF201A1A),
    surfaceVariant = Color(0xFFE8E1DE),
    onSurfaceVariant = Color(0xFF6E605E),
    outline = Color(0xFF9A8C89),
    outlineVariant = Color(0xFFDFD7D4),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFAF8F8),
    surfaceContainer = Color(0xFFEFEAE8),
    surfaceContainerHigh = Color(0xFFE8E1DE),
    surfaceContainerHighest = Color(0xFFDFD7D4),
)

private val ChapterDark = darkColorScheme(
    primary = Color(0xFFE5736B),
    onPrimary = Color(0xFF3D0A08),
    primaryContainer = Color(0xFF5C1717),
    onPrimaryContainer = Color(0xFFF4E1DD),
    secondary = Color(0xFFD9B65E),
    onSecondary = Color(0xFF3A2A05),
    secondaryContainer = Color(0xFF4A3A12),
    onSecondaryContainer = Color(0xFFF2E3BC),
    tertiary = Color(0xFFF08A80),
    onTertiary = Color(0xFF4A0C08),
    tertiaryContainer = Color(0xFF5E1E1A),
    onTertiaryContainer = Color(0xFFFAD9D5),
    background = Color(0xFF1B1212),
    onBackground = Color(0xFFEDE4E1),
    surface = Color(0xFF241818),
    onSurface = Color(0xFFEDE4E1),
    surfaceVariant = Color(0xFF3A2B2A),
    onSurfaceVariant = Color(0xFFC7B7B4),
    outline = Color(0xFF8A7672),
    outlineVariant = Color(0xFF4A3836),
    surfaceContainerLowest = Color(0xFF140D0D),
    surfaceContainerLow = Color(0xFF211616),
    surfaceContainer = Color(0xFF2A1D1C),
    surfaceContainerHigh = Color(0xFF332423),
    surfaceContainerHighest = Color(0xFF3D2C2B),
)

private val base = Typography()

private val T20Typography = Typography(
    displayLarge = base.displayLarge.copy(fontFamily = T20Fonts.display),
    displayMedium = base.displayMedium.copy(fontFamily = T20Fonts.display),
    displaySmall = base.displaySmall.copy(fontFamily = T20Fonts.display),
    headlineLarge = base.headlineLarge.copy(fontFamily = T20Fonts.display, fontWeight = FontWeight.Bold),
    headlineMedium = base.headlineMedium.copy(fontFamily = T20Fonts.display, fontWeight = FontWeight.Bold),
    headlineSmall = base.headlineSmall.copy(fontFamily = T20Fonts.display, fontWeight = FontWeight.Bold),
    titleLarge = base.titleLarge.copy(fontFamily = T20Fonts.display, fontWeight = FontWeight.SemiBold),
    titleMedium = base.titleMedium.copy(fontFamily = T20Fonts.sans, fontWeight = FontWeight.SemiBold),
    titleSmall = base.titleSmall.copy(fontFamily = T20Fonts.sans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    // Linhas de dados, como nas tabelas do livro
    bodyLarge = base.bodyLarge.copy(fontFamily = T20Fonts.sans, fontSize = 17.sp),
    // Texto corrido (descrições, resumos), em serifa como o livro
    bodyMedium = base.bodyMedium.copy(fontFamily = T20Fonts.serif, fontSize = 16.sp, lineHeight = 22.sp),
    bodySmall = base.bodySmall.copy(fontFamily = T20Fonts.sans, fontSize = 13.sp),
    labelLarge = base.labelLarge.copy(fontFamily = T20Fonts.sans, fontWeight = FontWeight.SemiBold),
    labelMedium = base.labelMedium.copy(fontFamily = T20Fonts.sans, fontWeight = FontWeight.SemiBold),
    labelSmall = base.labelSmall.copy(fontFamily = T20Fonts.sans),
)

@Composable
fun T20Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) ChapterDark else PaperLight,
        typography = T20Typography,
        content = content,
    )
}
