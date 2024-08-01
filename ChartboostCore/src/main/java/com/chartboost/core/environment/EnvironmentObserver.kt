/*
 * Copyright 2023-2024 Chartboost, Inc.
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.environment

/**
 * Interface for observing changes that the publisher is able to set on the Environment.
 */
interface EnvironmentObserver {
    /**
     * Called when a property changes. By design the new value is not provided.
     *
     * @param property The property that changed.
     */
    fun onChanged(property: ObservableEnvironmentProperty)
}
