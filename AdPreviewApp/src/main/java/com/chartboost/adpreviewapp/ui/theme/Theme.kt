/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.chartboost.adpreviewapp.BuildConfig

private val LightColorScheme =
    lightColorScheme(
        primary = CbRedPrimary,
        onPrimary = CbWhite,
        secondary = CbRedSecondary,
        onSecondary = CbWhite,
        background = CbBackgroundLight,
        surface = CbSurfaceLight,
        onBackground = CbBlack,
        onSurface = CbBlack,
    )

private val DarkColorScheme =
    darkColorScheme(
        primary = CbRedPrimary,
        onPrimary = CbBlack,
        secondary = CbRedSecondaryLight,
        onSecondary = CbBlack,
        background = CbBackgroundDark,
        surface = CbSurfaceDark,
        onBackground = CbWhite,
        onSurface = CbWhite,
    )

@Composable
fun AdPreviewAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            // Allow dynamic colors only in debug builds if ever needed
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && BuildConfig.DEBUG -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
