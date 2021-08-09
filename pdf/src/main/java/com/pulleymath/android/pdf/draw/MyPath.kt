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
import java.io.ObjectInputStream
import java.io.Serializable
import java.util.*

class MyPath : Path(), Serializable {
    val actions = LinkedList<Action>()

    private fun readObject(inputStream: ObjectInputStream) {
        inputStream.defaultReadObject()

        val copiedActions = actions.map { it }
        copiedActions.forEach {
            it.perform(this)
        }
    }

    override fun reset() {
        actions.clear()
        super.reset()
    }

    override fun moveTo(x: Float, y: Float) {
        actions.add(Move(x, y))
        super.moveTo(x, y)
    }

    override fun lineTo(x: Float, y: Float) {
        actions.add(Line(x, y))
        super.lineTo(x, y)
    }

    override fun quadTo(x1: Float, y1: Float, x2: Float, y2: Float) {
        actions.add(Quad(x1, y1, x2, y2))
        super.quadTo(x1, y1, x2, y2)
    }
}