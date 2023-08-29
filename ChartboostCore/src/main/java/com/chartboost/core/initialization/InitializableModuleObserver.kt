/*
 * Copyright 2023 Chartboost, Inc.
 * 
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.initialization

/**
 * Listener for module initialization.
 */
interface InitializableModuleObserver {
    /**
     * Called when a module is initialized.
     *
     * @param result Result of the module initialization.
     */
    fun onModuleInitializationCompleted(result: ModuleInitializationResult)
}
