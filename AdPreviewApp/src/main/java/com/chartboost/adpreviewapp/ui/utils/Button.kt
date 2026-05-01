/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.ui.utils

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chartboost.adpreviewapp.ui.theme.*
import com.chartboost.adpreviewapp.ui.utils.model.LoadingButtonState

@Composable
fun PrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(Dimens.paddingS),
        colors = PrimaryRedButtonColors,
        enabled = enabled,
        modifier = modifier,
    ) {
        Text(text = text)
    }
}

@Composable
fun LoadingButton(
    text: String,
    modifier: Modifier = Modifier,
    state: LoadingButtonState = LoadingButtonState.ENABLED,
    onClick: () -> Unit = {},
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(Dimens.paddingS),
        colors = PrimaryRedButtonColors,
        enabled = state == LoadingButtonState.ENABLED,
        modifier = modifier,
    ) {
        if (state == LoadingButtonState.LOADING) {
            Text(text = text, modifier = Modifier.padding(end = Dimens.paddingM))
            CircularProgressIndicator(
                modifier = Modifier.size(ButtonDefaults.IconSize),
                strokeWidth = Dimens.spacerS,
            )
        } else {
            Text(text = text)
        }
    }
}

@Preview
@Composable
fun LoadingPreview() {
    AdPreviewAppTheme {
        LoadingButton(text = "Load", state = LoadingButtonState.LOADING, modifier = Modifier.width(240.dp))
    }
}

@Preview
@Composable
fun EnabledPreview() {
    AdPreviewAppTheme {
        LoadingButton(text = "Load", state = LoadingButtonState.ENABLED, modifier = Modifier.width(240.dp))
    }
}
