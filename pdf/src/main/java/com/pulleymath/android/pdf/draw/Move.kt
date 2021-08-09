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

import android.graphics.Path
import java.io.Writer
import java.security.InvalidParameterException

class Move(val x: Float, val y: Float) : Action {

    override fun perform(path: Path) {
        path.moveTo(x, y)
    }

    override fun perform(writer: Writer) {
        writer.write("M$x,$y")
    }
}