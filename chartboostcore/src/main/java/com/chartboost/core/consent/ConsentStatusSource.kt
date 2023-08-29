/*
 * Copyright 2023 Chartboost, Inc.
 * 
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file.
 */

package com.chartboost.core.consent

/**
 * How consent was gathered.
 */
enum class ConsentStatusSource {
    /**
     * The user directly provided this in a consent dialog or some other user action or button.
     */
    USER,

    /**
     * The developer assumes consent without direct user interaction.
     */
    DEVELOPER,
}
