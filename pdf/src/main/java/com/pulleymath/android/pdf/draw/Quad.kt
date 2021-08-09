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

class Quad(private val x1: Float, private val y1: Float, private val x2: Float, private val y2: Float) : Action {

    override fun perform(path: Path) {
        path.quadTo(x1, y1, x2, y2)
    }

    override fun perform(writer: Writer) {
        writer.write("Q$x1,$y1 $x2,$y2")
    }
}