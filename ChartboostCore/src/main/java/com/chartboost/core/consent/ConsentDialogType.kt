/*
 * Copyright 2023-2024 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.consent

/**
 * When showing a dialog, what type of dialog it is.
 */
enum class ConsentDialogType {
    /**
     * Typically one that is shown on first launch to all users. It typically has fairly simple
     * functions of a consent or deny button and maybe something to show more.
     */
    CONCISE,

    /**
     * The full consent dialog which usually has a variety of actions a user can take and much
     * more detailed information about the privacy status of a user.
     */
    DETAILED,
}
