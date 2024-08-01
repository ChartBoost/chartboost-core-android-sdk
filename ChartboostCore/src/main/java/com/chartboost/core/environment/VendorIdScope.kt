/*
 * Copyright 2023-2024 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.environment

/**
 * The scope of the vendor identifier.
 */
enum class VendorIdScope {
    /**
     * It is unknown where the vendor ID came from or there is not one set.
     */
    UNKNOWN,

    /**
     * This vendor identifier is only associated with this app.
     */
    APPLICATION,

    /**
     * This vendor identifier is associated with a developer.
     */
    DEVELOPER,
}
