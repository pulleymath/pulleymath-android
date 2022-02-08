/*
 * This file is derived from FreeDrawView
 * (https://github.com/RiccardoMoro/FreeDrawView).
 * Copyright 2017 Riccardo Moro.
 * Licensed under the Apache License, Version 2.0; see licenses/Apache-2.0.txt
 * or http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Modified by Freewheelin Inc. for the Pulley Math app
 * (package rename and app-specific changes).
 * The modifications are distributed as part of this program under the
 * GNU Affero General Public License v3.0; see LICENSE.
 */
package com.freewheelin.pulley.views.memoView;

/**
 * Created by Riccardo Moro on 11/6/2016.
 */

/**
 * When RMFreeDrawView dimensions are changed, you can apply one of the following behaviours
 * {@link #CLEAR} - It just clear the View from every previous paint
 * {@link #FIT_XY} - It stretch the content to fit the new dimensions
 * {@link #CROP} - Keep the exact position of the previous point, if the dimensions changes, there
 * may be points outside the view and not visible
 */
public enum ResizeBehaviour {
    CLEAR,
    FIT_XY,
    CROP
}
