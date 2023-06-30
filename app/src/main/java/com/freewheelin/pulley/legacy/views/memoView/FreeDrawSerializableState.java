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

import com.freewheelin.pulley.legacy.views.memoView.HistoryPath;
import com.freewheelin.pulley.legacy.views.memoView.ResizeBehaviour;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Riccardo on 23/05/2017.
 */

public class FreeDrawSerializableState implements Serializable {

    static final long serialVersionUID = 40L;

    private ArrayList<com.freewheelin.pulley.legacy.views.memoView.HistoryPath> mCanceledPaths;
    private ArrayList<com.freewheelin.pulley.legacy.views.memoView.HistoryPath> mPaths;

    private int mPaintColor;
    private int mPaintAlpha;
    private float mPaintWidth;

    private com.freewheelin.pulley.legacy.views.memoView.ResizeBehaviour mResizeBehaviour;

    private int mLastDimensionW;
    private int mLastDimensionH;

    public FreeDrawSerializableState(ArrayList<com.freewheelin.pulley.legacy.views.memoView.HistoryPath> canceledPaths,
                                     ArrayList<com.freewheelin.pulley.legacy.views.memoView.HistoryPath> paths, int paintColor, int paintAlpha,
                                     float paintWidth, com.freewheelin.pulley.legacy.views.memoView.ResizeBehaviour resizeBehaviour,
                                     int lastW, int lastH) {

        setCanceledPaths(canceledPaths != null ? canceledPaths : new ArrayList<com.freewheelin.pulley.legacy.views.memoView.HistoryPath>());
        setPaths(paths != null ? paths : new ArrayList<com.freewheelin.pulley.legacy.views.memoView.HistoryPath>());
        setPaintWidth(paintWidth >= 0 ? paintWidth : 0);
        setPaintColor(paintColor);
        setPaintAlpha(paintAlpha);
        setResizeBehaviour(resizeBehaviour);
        setLastDimensionW(lastW >= 0 ? lastW : 0);
        setLastDimensionH(lastH >= 0 ? lastH : 0);
    }

    public ArrayList<com.freewheelin.pulley.legacy.views.memoView.HistoryPath> getCanceledPaths() {
        return mCanceledPaths;
    }

    public void setCanceledPaths(ArrayList<com.freewheelin.pulley.legacy.views.memoView.HistoryPath> canceledPaths) {
        this.mCanceledPaths = canceledPaths;
    }

    public ArrayList<com.freewheelin.pulley.legacy.views.memoView.HistoryPath> getPaths() {
        return mPaths;
    }

    public void setPaths(ArrayList<com.freewheelin.pulley.legacy.views.memoView.HistoryPath> paths) {
        this.mPaths = paths;
    }

    public float getPaintWidth() {
        return mPaintWidth;
    }

    public void setPaintWidth(float paintWidth) {
        this.mPaintWidth = paintWidth;
    }

    public int getPaintColor() {
        return mPaintColor;
    }

    public void setPaintColor(int paintColor) {
        this.mPaintColor = paintColor;
    }

    public int getPaintAlpha() {
        return mPaintAlpha;
    }

    public void setPaintAlpha(int paintAlpha) {
        this.mPaintAlpha = paintAlpha;
    }

    public com.freewheelin.pulley.legacy.views.memoView.ResizeBehaviour getResizeBehaviour() {
        return mResizeBehaviour;
    }

    public void setResizeBehaviour(ResizeBehaviour resizeBehaviour) {
        this.mResizeBehaviour = resizeBehaviour;
    }

    public int getLastDimensionW() {
        return mLastDimensionW;
    }

    public void setLastDimensionW(int lastDimensionW) {
        this.mLastDimensionW = lastDimensionW;
    }

    public int getLastDimensionH() {
        return mLastDimensionH;
    }

    public void setLastDimensionH(int lastDimensionH) {
        this.mLastDimensionH = lastDimensionH;
    }

    public boolean isBlankNote() {
        return getPaths().isEmpty();
    }
}
