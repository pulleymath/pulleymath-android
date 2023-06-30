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
package com.freewheelin.pulley.legacy.views.memoView;


import android.graphics.Paint;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.ColorInt;
import androidx.annotation.IntRange;
import android.view.View;

import com.freewheelin.pulley.legacy.views.memoView.FreeDrawHelper;
import com.freewheelin.pulley.legacy.views.memoView.HistoryPath;
import com.freewheelin.pulley.legacy.views.memoView.ResizeBehaviour;

import java.util.ArrayList;

/**
 * Created by Riccardo Moro on 11/4/2016.
 */

class FreeDrawSavedState extends View.BaseSavedState {

    private ArrayList<com.freewheelin.pulley.legacy.views.memoView.HistoryPath> mPaths = new ArrayList<>();
    private ArrayList<com.freewheelin.pulley.legacy.views.memoView.HistoryPath> mCanceledPaths = new ArrayList<>();

    private int mPaintColor;
    private int mPaintAlpha;
    private float mPaintWidth;

    private com.freewheelin.pulley.legacy.views.memoView.ResizeBehaviour mResizeBehaviour;

    private int mLastDimensionW;
    private int mLastDimensionH;

    FreeDrawSavedState(Parcelable superState, ArrayList<com.freewheelin.pulley.legacy.views.memoView.HistoryPath> paths,
                       ArrayList<com.freewheelin.pulley.legacy.views.memoView.HistoryPath> canceledPaths, float paintWidth,
                       int paintColor, int paintAlpha, com.freewheelin.pulley.legacy.views.memoView.ResizeBehaviour resizeBehaviour,
                       int lastDimensionW, int lastDimensionH) {
        super(superState);

        mPaths = paths;
        mCanceledPaths = canceledPaths;
        mPaintWidth = paintWidth;

        mPaintColor = paintColor;
        mPaintAlpha = paintAlpha;

        mResizeBehaviour = resizeBehaviour;

        mLastDimensionW = lastDimensionW;
        mLastDimensionH = lastDimensionH;
    }

    ArrayList<com.freewheelin.pulley.legacy.views.memoView.HistoryPath> getPaths() {
        return mPaths;
    }

    ArrayList<com.freewheelin.pulley.legacy.views.memoView.HistoryPath> getCanceledPaths() {
        return mCanceledPaths;
    }

    @ColorInt
    int getPaintColor() {
        return mPaintColor;
    }

    @IntRange(from = 0, to = 255)
    int getPaintAlpha() {
        return mPaintAlpha;
    }

    float getCurrentPaintWidth() {
        return mPaintWidth;
    }

    Paint getCurrentPaint() {

        Paint paint = com.freewheelin.pulley.legacy.views.memoView.FreeDrawHelper.createPaint();
        com.freewheelin.pulley.legacy.views.memoView.FreeDrawHelper.setupStrokePaint(paint);
        FreeDrawHelper.copyFromValues(paint, mPaintColor, mPaintAlpha, mPaintWidth, true);
        return paint;
    }

    com.freewheelin.pulley.legacy.views.memoView.ResizeBehaviour getResizeBehaviour() {
        return mResizeBehaviour;
    }

    int getLastDimensionW() {
        return mLastDimensionW;
    }

    int getLastDimensionH() {
        return mLastDimensionH;
    }

    // Parcelable stuff
    private FreeDrawSavedState(Parcel in) {
        super(in);

        in.readTypedList(mPaths, com.freewheelin.pulley.legacy.views.memoView.HistoryPath.CREATOR);
        in.readTypedList(mCanceledPaths, com.freewheelin.pulley.legacy.views.memoView.HistoryPath.CREATOR);

        mPaintColor = in.readInt();
        mPaintAlpha = in.readInt();
        mPaintWidth = in.readFloat();

        mResizeBehaviour = (ResizeBehaviour) in.readSerializable();

        mLastDimensionW = in.readInt();
        mLastDimensionH = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);

        out.writeTypedList(mPaths);
        out.writeTypedList(mCanceledPaths);

        out.writeInt(mPaintColor);
        out.writeInt(mPaintAlpha);
        out.writeFloat(mPaintWidth);

        out.writeSerializable(mResizeBehaviour);

        out.writeInt(mLastDimensionW);
        out.writeInt(mLastDimensionH);
    }

    // Parcelable CREATOR class, needed for parcelable to work
    public static final Creator<FreeDrawSavedState> CREATOR =
            new Creator<FreeDrawSavedState>() {
                public FreeDrawSavedState createFromParcel(Parcel in) {
                    return new FreeDrawSavedState(in);
                }

                public FreeDrawSavedState[] newArray(int size) {
                    return new FreeDrawSavedState[size];
                }
            };
}