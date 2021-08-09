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
package com.pulleymath.android.pdf.memo;

public interface PathDrawnListener {

    void onPathStart();

    void onNewPathDrawn();
}