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
package com.freewheelin.pulley.revision2021.cookingmemo;

import android.content.res.Resources;
import android.graphics.ComposePathEffect;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import androidx.annotation.NonNull;

import java.util.List;


/**
 * Created by Riccardo Moro on 10/23/2016.
 */

public class FreeDrawHelper {

    /**
     * Function used to check whenever a list of points is a line or a path to draw
     */
    static boolean isAPoint(@NonNull List<Point> points) {
        if (points.size() == 0)
            return false;

        if (points.size() == 1)
            return true;

        for (int i = 1; i < points.size(); i++) {
            if (points.get(i - 1).x != points.get(i).x || points.get(i - 1).y != points.get(i).y)
                return false;
        }

        return true;
    }

    /**
     * Create, initialize and setup a paint
     */
    static Paint createPaintAndInitialize(int paintColor, int paintAlpha,
                                          float paintWidth, boolean fill) {

        Paint paint = createPaint();

        initializePaint(paint, paintColor, paintAlpha, paintWidth, fill);

        return paint;
    }

    static Paint createPaint() {
        return new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    static void initializePaint(Paint paint, int paintColor, int paintAlpha, float paintWidth,
                                boolean fill) {

        if (fill) {
            setupFillPaint(paint);
        } else {
            setupStrokePaint(paint);
        }

        paint.setStrokeWidth(paintWidth);
        paint.setColor(paintColor);
        paint.setAlpha(paintAlpha);
    }

    static void setupFillPaint(Paint paint) {
        paint.setStyle(Paint.Style.FILL);
    }

    static void setupStrokePaint(Paint paint) {
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setPathEffect(new ComposePathEffect(
                new CornerPathEffect(100f),
                new CornerPathEffect(100f)));
        paint.setStyle(Paint.Style.STROKE);
    }

    static void copyFromPaint(Paint from, Paint to, boolean copyWidth) {

        to.setColor(from.getColor());
        to.setAlpha(from.getAlpha());

        if (copyWidth) {
            to.setStrokeWidth(from.getStrokeWidth());
        }
    }

    static void copyFromValues(Paint to, int color, int alpha, float strokeWidth,
                               boolean copyWidth) {

        to.setColor(color);
        to.setAlpha(alpha);

        if (copyWidth) {
            to.setStrokeWidth(strokeWidth);
        }
    }

    /**
     * Converts a given dp number to it's pixel corresponding number
     */
    public static float convertDpToPixels(float dp) {
        return (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    /**
     * Converts a given pixel number to it's dp corresponding number
     */
    public static float convertPixelsToDp(float px) {
        return px / Resources.getSystem().getDisplayMetrics().density;
    }
}
