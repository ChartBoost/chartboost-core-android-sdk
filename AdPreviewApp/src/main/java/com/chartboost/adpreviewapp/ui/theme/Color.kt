/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.ui.theme

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ChipColors
import androidx.compose.ui.graphics.Color

// ---- BASE COLORS ----
val CbRedPrimary = Color(0xFFD32F2F)
val CbRedSecondary = Color(0xFFB71C1C)
val CbRedSecondaryLight = Color(0xFFFF8A80)

val CbWhite = Color.White
val CbBlack = Color.Black
val CbGray = Color(0xFFA1A1A1)

val CbBackgroundLight = Color.White
val CbBackgroundDark = Color(0xFF121212)
val CbSurfaceLight = Color(0xFFF5F5F5)
val CbSurfaceDark = Color(0xFF1E1E1E)

// ---- BUTTON STYLES ----

val PrimaryRedButtonColors: ButtonColors =
    ButtonColors(
        containerColor = CbRedPrimary,
        contentColor = CbWhite,
        disabledContainerColor = CbGray,
        disabledContentColor = CbWhite,
    )

val ActivatedChipColor =
    ChipColors(
        containerColor = CbGray,
        labelColor = CbWhite,
        leadingIconContentColor = CbWhite,
        trailingIconContentColor = CbWhite,
        disabledContainerColor = CbWhite,
        disabledLabelColor = CbRedPrimary,
        disabledLeadingIconContentColor = CbGray,
        disabledTrailingIconContentColor = CbGray,
    )

val ErrorChipColor =
    ActivatedChipColor.copy(
        disabledLabelColor = CbRedPrimary,
    )
