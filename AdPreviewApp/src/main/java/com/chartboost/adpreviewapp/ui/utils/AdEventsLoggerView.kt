/*
 * Copyright 2025 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.adpreviewapp.ui.utils

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.chartboost.adpreviewapp.R
import com.chartboost.adpreviewapp.ui.theme.Dimens

@Composable
fun EventLogViewer(
    logs: List<String>,
    onClearClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
        ) {
            LazyColumn(
                modifier = Modifier.padding(Dimens.paddingS),
            ) {
                items(logs.size) { index ->
                    val numberedLog = "${logs.size - index}. ${logs[logs.size - 1 - index]}"
                    Text(
                        text = numberedLog,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = Dimens.paddingXS),
                    )
                    HorizontalDivider(thickness = Dimens.horizontalDivider)
                }
            }
        }

        Spacer(modifier = Modifier.height(Dimens.spacerM))

        PrimaryButton(
            onClick = onClearClicked,
            enabled = logs.isNotEmpty(),
            text = stringResource(R.string.clear_logs_button),
            modifier = Modifier.align(Alignment.End),
        )
    }
}
