/*
 * Copyright 2023 Chartboost, Inc.
 * 
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.environment

/**
 * Interface for observing changes to the publisher metadata.
 */
interface PublisherMetadataObserver {
    /**
     * Called when a property changes. By design the new value is not provided.
     *
     * @param property The property that changed.
     */
    fun onChanged(property: PublisherMetadataChangedProperty)
}
