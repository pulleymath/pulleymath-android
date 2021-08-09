/*
 * This file is derived from Simple Draw
 * (https://github.com/SimpleMobileTools/Simple-Draw).
 * Copyright (C) Simple Mobile Tools.
 * Licensed under the GNU General Public License v3.0; see licenses/GPL-3.0.txt.
 *
 * Modified by Freewheelin Inc. for the Pulley Math app
 * (package rename and app-specific changes).
 * The modifications are distributed as part of this program under the
 * GNU Affero General Public License v3.0; see LICENSE.
 */
package com.pulleymath.android.pdf.draw

import android.graphics.Color

data class PaintOptions(var color: Int = Color.BLACK, var strokeWidth: Float = 8f, var alpha: Int = 255, var isEraserOn: Boolean = false)
