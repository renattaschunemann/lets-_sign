package br.com.letssign.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

private fun LetsSignColors.toMaterialColorScheme(darkTheme: Boolean) = if (darkTheme) {
    darkColorScheme(
        primary = corPrimaria,
        onPrimary = corSobrePrimaria,
        primaryContainer = corPrimariaContainer,
        onPrimaryContainer = corSobrePrimariaContainer,
        secondary = corSecundaria,
        onSecondary = corSobreSecundaria,
        background = corFundo,
        onBackground = corTextoPrincipal,
        surface = corSuperficie,
        onSurface = corTextoPrincipal,
        surfaceVariant = corSuperficieVariante,
        onSurfaceVariant = corTextoSecundario,
        outline = corBorda,
        outlineVariant = corDivisor,
        error = corErro,
        onError = corSobrePrimaria,
        errorContainer = corErroContainer,
        onErrorContainer = corErro
    )
} else {
    lightColorScheme(
        primary = corPrimaria,
        onPrimary = corSobrePrimaria,
        primaryContainer = corPrimariaContainer,
        onPrimaryContainer = corSobrePrimariaContainer,
        secondary = corSecundaria,
        onSecondary = corSobreSecundaria,
        background = corFundo,
        onBackground = corTextoPrincipal,
        surface = corSuperficie,
        onSurface = corTextoPrincipal,
        surfaceVariant = corSuperficieVariante,
        onSurfaceVariant = corTextoSecundario,
        outline = corBorda,
        outlineVariant = corDivisor,
        error = corErro,
        onError = corSobrePrimaria,
        errorContainer = corErroContainer,
        onErrorContainer = corErro
    )
}

@Composable
fun LetsSignTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colors: LetsSignColors = if (darkTheme) LetsSignDarkColors else LetsSignLightColors,
    spacing: LetsSignSpacing = LetsSignSpacing(),
    elevation: LetsSignElevation = LetsSignElevation(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalLetsSignColors provides colors,
        LocalLetsSignSpacing provides spacing,
        LocalLetsSignElevation provides elevation
    ) {
        MaterialTheme(
            colorScheme = colors.toMaterialColorScheme(darkTheme),
            typography = LetsSignTypography,
            shapes = LetsSignShapes,
            content = content
        )
    }
}

object LetsSignTheme {
    val colors: LetsSignColors
        @Composable
        @ReadOnlyComposable
        get() = LocalLetsSignColors.current

    val spacing: LetsSignSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalLetsSignSpacing.current

    val elevation: LetsSignElevation
        @Composable
        @ReadOnlyComposable
        get() = LocalLetsSignElevation.current

    val typography: androidx.compose.material3.Typography
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography
}
