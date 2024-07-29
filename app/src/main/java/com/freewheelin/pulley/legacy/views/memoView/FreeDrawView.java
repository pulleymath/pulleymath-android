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

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Xfermode;
import android.os.AsyncTask;
import android.os.Parcelable;
import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.freewheelin.pulley.R;
import com.freewheelin.pulley.revision2023.ui.view.DrawPathType;
import com.freewheelin.pulley.revision2023.ui.view.DrawType;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Riccardo Moro on 9/10/2016.
 */
public class FreeDrawView extends View implements View.OnTouchListener {
    private static final String TAG = FreeDrawView.class.getSimpleName();

    private static final float DEFAULT_STROKE_WIDTH = 4;
    private static final int DEFAULT_COLOR = Color.BLACK;
    private static final int DEFAULT_ALPHA = 255;

    public Paint mCurrentPaint;
    private Path mCurrentPath;
    public Bitmap loadedBitmap;
    public Bitmap loadedBitmapAtWillRedo;
    private ResizeBehaviour mResizeBehaviour;
    public boolean isCookingMemo = false;

    public ArrayList<Point> mPoints = new ArrayList<>();
    public ArrayList<HistoryPath> mPaths = new ArrayList<>();
    public ArrayList<HistoryPath> mCanceledPaths = new ArrayList<>();

    @ColorInt
    private int mPaintColor = DEFAULT_COLOR;
    @IntRange(from = 0, to = 255)
    private int mPaintAlpha = DEFAULT_ALPHA;

    private int mLastDimensionW = -1;
    private int mLastDimensionH = -1;

    private boolean mFinishPath = false;

    private PathDrawnListener mPathDrawnListener;
    private PathRedoUndoCountChangeListener mPathRedoUndoCountChangeListener;
    private PathAndImageUndoCountListener mPathAndImageUndoCountListener;

    public FreeDrawView(Context context) {
        this(context, null);
    }

    public FreeDrawView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FreeDrawView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setOnTouchListener(this);

        TypedArray a = null;
        try {

            a = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.FreeDrawView,
                    defStyleAttr, 0);

            initPaints(a);
        } finally {
            if (a != null) {
                a.recycle();
            }
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {

        // Get the superclass parcelable state
        Parcelable superState = super.onSaveInstanceState();

        if (mPoints.size() > 0) {// Currently doing a line, save it's current path
            createHistoryPathFromPoints();
        }

        return new FreeDrawSavedState(superState, mPaths, mCanceledPaths,
                getPaintWidth(), getPaintColor(), getPaintAlpha(),
                getResizeBehaviour(), mLastDimensionW, mLastDimensionH);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {

        // If not instance of my state, let the superclass handle it
        if (!(state instanceof FreeDrawSavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        FreeDrawSavedState savedState = (FreeDrawSavedState) state;
        // Superclass restore state
        super.onRestoreInstanceState(savedState.getSuperState());

        // My state restore
        mPaths = savedState.getPaths();
        mCanceledPaths = savedState.getCanceledPaths();
        mCurrentPaint = savedState.getCurrentPaint();

        setPaintWidthPx(savedState.getCurrentPaintWidth());
        setPaintColor(savedState.getPaintColor());
        setPaintAlpha(savedState.getPaintAlpha());

        setResizeBehaviour(savedState.getResizeBehaviour());

        // Restore the last dimensions, so that in onSizeChanged i can calculate the
        // height and width change factor and multiply every point x or y to it, so that if the
        // View is resized, it adapt automatically it's points to the new width/height
        mLastDimensionW = savedState.getLastDimensionW();
        mLastDimensionH = savedState.getLastDimensionH();

        notifyRedoUndoCountChanged();
    }

    /**
     * Set the paint color
     *
     * @param color The now color to be applied to the
     */
    public void setPaintColor(@ColorInt int color) {

        invalidate();

        mPaintColor = color;

        mCurrentPaint.setColor(mPaintColor);
        mCurrentPaint.setAlpha(mPaintAlpha);// Restore the previous alpha
    }

    /**
     * Get the current paint color without it's alpha
     */
    @ColorInt
    public int getPaintColor() {
        return mPaintColor;
    }

    /**
     * Get the current color with the current alpha
     */
    @ColorInt
    public int getPaintColorWithAlpha() {
        return mCurrentPaint.getColor();
    }


    /**
     * Set the paint width in px
     *
     * @param widthPx The new weight in px, must be > 0
     */
    public void setPaintWidthPx(@FloatRange(from = 0) float widthPx) {
        if (widthPx > 0) {

            invalidate();

            mCurrentPaint.setStrokeWidth(widthPx);
        }
    }
    class PrevPencil {
        Xfermode mode;
        int color;
        int alpha;
        float stroke;
        PrevPencil(Xfermode mode, int color, int alpha, float stroke) {
            this.mode = mode;
            this.color = color;
            this.alpha = alpha;
            this.stroke = stroke;
        }
    }

    PrevPencil pp = new PrevPencil(null,-8289919, 255, 1.5f);
    Boolean isStylusBtnClicked = false;
    public void setPencil(Xfermode mode, int paintColor, int paintAlpha, float stroke) {
        isStylusBtnClicked = false;
        pp = new PrevPencil(mode, paintColor, paintAlpha, stroke);
        mCurrentPaint.setXfermode(mode);
        mCurrentPaint.setColor(paintColor);
        mCurrentPaint.setAlpha(paintAlpha);
        setPaintWidthDp(stroke);
    }
    public void setEraser(float stroke) {
        pp = new PrevPencil(new PorterDuffXfermode(PorterDuff.Mode.CLEAR), Color.TRANSPARENT, Color.TRANSPARENT, stroke);
        mCurrentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        mCurrentPaint.setColor(Color.TRANSPARENT);
        mCurrentPaint.setAlpha(Color.TRANSPARENT);
        setPaintWidthPx(FreeDrawHelper.convertDpToPixels(stroke));
    }
    public void setCurrPaint(Xfermode mode, int paintColor, int paintAlpha) {
        isStylusBtnClicked = false;
        pp.color = paintColor;
        pp.alpha = paintAlpha;
        mCurrentPaint.setXfermode(mode);
        mCurrentPaint.setColor(paintColor);
        mCurrentPaint.setAlpha(paintAlpha);
    }
    public void setEraserFromOnTouch(float stroke) {
        mCurrentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        mCurrentPaint.setColor(Color.TRANSPARENT);
        mCurrentPaint.setAlpha(Color.TRANSPARENT);
        setPaintWidthPx(FreeDrawHelper.convertDpToPixels(stroke));

    }

    /**
     * Set the paint width in dp
     *
     * @param dp The new weight in dp, must be > 0
     */
    public void setPaintWidthDp(float dp) {
        setPaintWidthPx(FreeDrawHelper.convertDpToPixels(dp));
    }

    /**
     * {@link #getPaintWidth(boolean)}
     */
    @FloatRange(from = 0)
    public float getPaintWidth() {
        return getPaintWidth(false);
    }

    /**
     * Get the current paint with in dp or pixel
     */
    @FloatRange(from = 0)
    public float getPaintWidth(boolean inDp) {
        if (inDp) {
            return FreeDrawHelper.convertPixelsToDp(mCurrentPaint.getStrokeWidth());
        } else {
            return mCurrentPaint.getStrokeWidth();
        }
    }


    /**
     * Set the paint opacity, must be between 0 and 1
     *
     * @param alpha The alpha to apply to the paint
     */
    public void setPaintAlpha(@IntRange(from = 0, to = 255) int alpha) {

        // Finish current path and redraw, so that the new setting is applied only to the next path
        invalidate();

        mPaintAlpha = alpha;
        mCurrentPaint.setAlpha(mPaintAlpha);
    }

    /**
     * Get the current paint alpha
     */
    @IntRange(from = 0, to = 255)
    public int getPaintAlpha() {
        return mPaintAlpha;
    }


    /**
     * Set what to do when the view is resized (on rotation if its dimensions are not fixed)
     * {@link ResizeBehaviour}
     */
    public void setResizeBehaviour(ResizeBehaviour newBehaviour) {
        mResizeBehaviour = newBehaviour;
    }

    /**
     * Get the current behaviour on view resize
     */
    public ResizeBehaviour getResizeBehaviour() {
        return mResizeBehaviour;
    }


    /**
     * Cancel the last drawn segment
     */
    public void undoLast() {

        if (mPaths.size() > 0) {
            // End current path
            mFinishPath = true;
            invalidate();

            // Cancel the last one and redraw
            mCanceledPaths.add(mPaths.get(mPaths.size() - 1));
            mPaths.remove(mPaths.size() - 1);

//        } else if (isCookingMemo && loadedBitmapAtWillRedo == null) {
        } else if (loadedBitmapAtWillRedo == null) {
            loadedBitmapAtWillRedo = loadedBitmap;
            loadedBitmap = null;
        }

        invalidate();
        notifyRedoUndoCountChanged();
    }

    /**
     * Re-add the first removed path and redraw
     */
    public void redoLast() {
//        if (loadedBitmapAtWillRedo != null && isCookingMemo) {
        if (loadedBitmapAtWillRedo != null) {
            loadedBitmap = loadedBitmapAtWillRedo;
            loadedBitmapAtWillRedo = null;
        } else if (mCanceledPaths.size() > 0) {
            mPaths.add(mCanceledPaths.get(mCanceledPaths.size() - 1));
            mCanceledPaths.remove(mCanceledPaths.size() - 1);
        }

        invalidate();
        notifyRedoUndoCountChanged();
    }

    /**
     * Remove all the paths and redraw (can be undone with {@link #redoLast()})
     */
    public void undoAll() {
        Collections.reverse(mPaths);
        mCanceledPaths.addAll(mPaths);
        mPaths = new ArrayList<>();
        invalidate();

        notifyRedoUndoCountChanged();
    }

    /**
     * Re-add all the removed paths and redraw
     */
    public void redoAll() {

        if (mCanceledPaths.size() > 0) {
            mPaths.addAll(mCanceledPaths);
            mCanceledPaths = new ArrayList<>();
            invalidate();

            notifyRedoUndoCountChanged();
        }
    }

    /**
     * Get how many undo operations are available
     */
    public int getUndoCount() {
        if (mPaths.size() > 0) {
            return mPaths.size();
        } else {
//            if (isCookingMemo && loadedBitmapAtWillRedo == null && loadedBitmap == null) {
            if (loadedBitmapAtWillRedo == null && loadedBitmap == null) {
                return 0;
//            } else if (isCookingMemo && loadedBitmapAtWillRedo == null) {
            } else if (loadedBitmapAtWillRedo == null) {
                return 1;
            } else {
                return 0;
            }
        }
    }
    public int getImageAndStrokeUndoCount() {
        if (mPaths.size() > 0) {
            return mPaths.size();
        } else {
            Log.d(TAG, "getImageAndStrokeUndoCount: mPath 0");
//            if (isCookingMemo && loadedBitmapAtWillRedo == null && loadedBitmap == null) {
            if (loadedBitmapAtWillRedo == null && loadedBitmap == null) {
                Log.d(TAG, "getImageAndStrokeUndoCount: mPath 0, null , null");
                return 0;
//            } else if (isCookingMemo && loadedBitmapAtWillRedo == null) {
            } else if (loadedBitmapAtWillRedo == null || loadedBitmap != null) {
                Log.d(TAG, "getImageAndStrokeUndoCount: mPath 0,loadedBitmapAtWillRedo  null or loadedBitmap != null");
                return 1;
            } else {
                return 0;
            }
        }
    }

    /**
     * Get how many redo operations are available
     */
    public int getRedoCount() {
//        if (isCookingMemo && loadedBitmapAtWillRedo != null) {
        if (loadedBitmapAtWillRedo != null) {
            return 1;
        }
        return mCanceledPaths.size();
    }

    /**
     * Get how many paths are drawn on this FreeDrawView
     *
     * @param includeCurrentlyDrawingPath Include the path that is currently been drawn
     * @return The number of paths drawn
     */
    public int getPathCount(boolean includeCurrentlyDrawingPath) {
        int size = mPaths.size();

        if (includeCurrentlyDrawingPath && mPoints.size() > 0) {
            size++;
        }
        return size;
    }

    /**
     * Set a path drawn listener, will be called every time a new path is drawn
     */
    public void setOnPathDrawnListener(PathDrawnListener listener) {
        mPathDrawnListener = listener;
    }

    /**
     * Remove the path drawn listener
     */
    public void removePathDrawnListener() {
        mPathDrawnListener = null;
    }

    /**
     * Clear the current draw and the history
     */
    public void clearDrawAndHistory() {

        clearDraw(false);
        clearHistory(true);
    }

    /**
     * Clear the current draw
     */
    public void clearDraw() {

        clearDraw(true);
    }

    private void clearDraw(boolean invalidate) {
        mPoints = new ArrayList<>();
        mPaths = new ArrayList<>();

        notifyRedoUndoCountChanged();

        if (invalidate) {
            invalidate();
        }
    }

    /**
     * Clear the history (paths that can be redone)
     */
    public void clearHistory() {
        clearHistory(true);
    }

    private void clearHistory(boolean invalidate) {
        mCanceledPaths = new ArrayList<>();

        notifyRedoUndoCountChanged();

        if (invalidate) {
            invalidate();
        }
    }

    /**
     * Set a redo-undo count change listener, this will be called every time undo or redo count
     * changes
     */
    public void setPathRedoUndoCountChangeListener(PathRedoUndoCountChangeListener listener) {
        mPathRedoUndoCountChangeListener = listener;
    }

    /**
     * Remove the redo-undo count listener
     */
    public void removePathRedoUndoCountChangeListener() {
        mPathRedoUndoCountChangeListener = null;
    }
    /**
     * 이미지와 패스의 총 undo Count 리스너 추가
     */
    public void setPathAndImageUndoCountListener(PathAndImageUndoCountListener listener) {
        mPathAndImageUndoCountListener = listener;
    }

    /**
     * 이미지와 패스의 총 undo Count 리스너 지우기
     */
    public void removePathAndImageUndoCountListener() {
        mPathAndImageUndoCountListener = null;
    }

    /**
     * Get a serializable object with all the needed info about the current draw and state
     *
     * @return A {@link FreeDrawSerializableState} containing all the needed data
     */
    public FreeDrawSerializableState getCurrentViewStateAsSerializable() {

        return new FreeDrawSerializableState(mCanceledPaths, mPaths, getPaintColor(),
                getPaintAlpha(), getPaintWidth(), getResizeBehaviour(),
                mLastDimensionW, mLastDimensionH);
    }

    /**
     * Restore the state of the draw from the given serializable state
     *
     * @param state A {@link FreeDrawSerializableState} containing all the draw and paint info,
     *              if null, nothing will be restored. Null sub fields will be ignored
     */
    private final transient PorterDuffXfermode clear = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);

    public void restoreStateFromSerializable(FreeDrawSerializableState state) {

        if (state != null) {

            if (state.getCanceledPaths() != null) {
                mCanceledPaths = state.getCanceledPaths();
                // Xfermode는 serialize가 불가능하여 restore할때 자동복원되지 않는다.
                for (HistoryPath path : mCanceledPaths) {
                    Paint a = path.getPaint();
                    if (path.isErase) {
                        a.setXfermode(clear);
                    }
                }
            }

            if (state.getPaths() != null) {
                mPaths = state.getPaths();
                // Xfermode는 serialize가 불가능하여 restore할때 자동복원되지 않는다.
                for (HistoryPath path : mPaths) {
                    Paint a = path.getPaint();
                    if (path.isErase) {
                        a.setXfermode(clear);
                    }
                }
            }

            mPaintColor = state.getPaintColor();
            mPaintAlpha = state.getPaintAlpha();

            mCurrentPaint.setColor(state.getPaintColor());
            mCurrentPaint.setAlpha(state.getPaintAlpha());
            setPaintWidthPx(state.getPaintWidth());

            mResizeBehaviour = state.getResizeBehaviour();

            if (state.getLastDimensionW() >= 0) {
                mLastDimensionW = state.getLastDimensionW();
            }

            if (state.getLastDimensionH() >= 0) {
                mLastDimensionH = state.getLastDimensionH();
            }

            notifyRedoUndoCountChanged();
            invalidate();
        }
    }

    /**
     * Create a Bitmap with the content drawn inside the view
     */
    public void getDrawScreenshot(@NonNull final DrawCreatorListener listener) {
        new TakeScreenShotAsyncTask(listener).execute();
    }


    // Internal methods
    private void notifyPathStart() {
        if (mPathDrawnListener != null) {
            mPathDrawnListener.onPathStart();
        }
    }

    private void notifyPathDrawn() {
        if (mPathDrawnListener != null) {
            mPathDrawnListener.onNewPathDrawn();
        }
    }

    private void notifyRedoUndoCountChanged() {
        if (mPathRedoUndoCountChangeListener != null) {
            mPathRedoUndoCountChangeListener.onRedoCountChanged(getRedoCount());
            mPathRedoUndoCountChangeListener.onUndoCountChanged(getUndoCount());
        }
        if (mPathAndImageUndoCountListener != null) {
            mPathAndImageUndoCountListener.onImageAndPathUndoCountChanged(getImageAndStrokeUndoCount());
        }
    }
    public void notifyRedoUndoCountSetting() {
        notifyRedoUndoCountChanged();
    }
    private void initPaints(TypedArray a) {
        mCurrentPaint = FreeDrawHelper.createPaint();

        mCurrentPaint.setColor(a != null ? a.getColor(R.styleable.FreeDrawView_paintColor,
                mPaintColor) : mPaintColor);
        mCurrentPaint.setAlpha(a != null ?
                a.getInt(R.styleable.FreeDrawView_paintAlpha, mPaintAlpha)
                : mPaintAlpha);
        mCurrentPaint.setStrokeWidth(a != null ?
                a.getDimensionPixelSize(R.styleable.FreeDrawView_paintWidth,
                        (int) FreeDrawHelper.convertDpToPixels(DEFAULT_STROKE_WIDTH))
                : FreeDrawHelper.convertDpToPixels(DEFAULT_STROKE_WIDTH));

        FreeDrawHelper.setupStrokePaint(mCurrentPaint);

        if (a != null) {
            int resizeBehaviour = a.getInt(R.styleable.FreeDrawView_resizeBehaviour, -1);
            mResizeBehaviour =
                    resizeBehaviour == 0 ? ResizeBehaviour.CLEAR :
                            resizeBehaviour == 1 ? ResizeBehaviour.FIT_XY :
                                    resizeBehaviour == 2 ? ResizeBehaviour.CROP :
                                            ResizeBehaviour.CROP;
        }
    }

    private Paint createAndCopyColorAndAlphaForFillPaint(Paint from, boolean copyWidth) {
        Paint paint = FreeDrawHelper.createPaint();
        FreeDrawHelper.setupFillPaint(paint);
        paint.setColor(from.getColor());
        paint.setAlpha(from.getAlpha());
        if (copyWidth) {
            paint.setStrokeWidth(from.getStrokeWidth());
        }
        return paint;
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        if (loadedBitmap != null) {
            canvas.drawBitmap(loadedBitmap, 0, 0, null);
        }
        if (mPaths.size() == 0 && mPoints.size() == 0) {
            return;
        }

        final boolean finishedPath = mFinishPath;
        mFinishPath = false;

        for (HistoryPath currentPath : mPaths) {
            if (currentPath.type == DrawPathType.Curve) {
                if (currentPath.isPoint()) {
                    canvas.drawCircle(currentPath.getOriginX(), currentPath.getOriginY(),
                        currentPath.getPaint().getStrokeWidth() / 2, currentPath.getPaint());
                } else {
                    canvas.drawPath(currentPath.getPath(), currentPath.getPaint());
                }
            } else if (currentPath.type == DrawPathType.Circle || currentPath.type == DrawPathType.Line) {
                canvas.drawPath(currentPath.getPath(), currentPath.getPaint());
            } else if (currentPath.type == DrawPathType.Arrow) {
                canvas.drawPath(currentPath.getPath(), currentPath.getPaint());
            }
        }

        if (mCurrentPath == null)
            mCurrentPath = new Path();
        else
            mCurrentPath.rewind();

        if (mPoints.size() == 1 || FreeDrawHelper.isAPoint(mPoints)) {

            canvas.drawCircle(mPoints.get(0).x, mPoints.get(0).y,
                    mCurrentPaint.getStrokeWidth() / 2,
                    createAndCopyColorAndAlphaForFillPaint(mCurrentPaint, false));
        } else if (mPoints.size() != 0) {// Else draw the complete series of points

            Log.d("draw Path", "editType : " + drawType.toString());
            if (drawType == DrawType.Pencil || drawType == DrawType.Eraser) {
                Log.d("draw Path", "pathType : " + pathType.toString());

                boolean first = true;

                for (Point point : mPoints) {

                    if (first) {
                        mCurrentPath.moveTo(point.x, point.y);
                        first = false;
                    } else {
                        mCurrentPath.lineTo(point.x, point.y);
                    }
                }

                canvas.drawPath(mCurrentPath, mCurrentPaint);
            } else if(drawType == DrawType.Figure) {
                Log.d("draw Path", "pathType : " + pathType.toString());

                if (pathType == DrawPathType.Circle) {
                    Point startP = mPoints.get(0);
                    Point endP = mPoints.get(mPoints.size() - 1);
                    mCurrentPath.moveTo(startP.x, startP.y);

                    double dx = (double) (endP.x - startP.x);
                    double dy = (double) (endP.y - startP.y);
                    double radius = Math.sqrt(dx * dx + dy * dy);
                    mCurrentPath.addCircle(startP.x, startP.y, (float) radius, Path.Direction.CW);
                    canvas.drawPath(mCurrentPath, mCurrentPaint);

                } else if (pathType == DrawPathType.Line) {
                    Point startP = mPoints.get(0);
                    mCurrentPath.moveTo(startP.x, startP.y);

                    Point endP = mPoints.get(mPoints.size() - 1);
                    mCurrentPath.lineTo(endP.x, endP.y);
                    canvas.drawPath(mCurrentPath, mCurrentPaint);

                } else if (pathType == DrawPathType.Arrow) {
                    Point startP = mPoints.get(0);
                    mCurrentPath.moveTo(startP.x, startP.y);

                    Point endP = mPoints.get(mPoints.size() - 1);
                    mCurrentPath.lineTo(endP.x, endP.y);

                    double mfDegree = Math.atan2(startP.y - endP.y, startP.x - endP.x) * 180 / Math.PI;
                    float x1 = 0, y1 = 0, x2 = 0, y2 = 0;
                    double angle = mfDegree - 45;
                    x1 = endP.x + (float)(30 * Math.cos(angle * (Math.PI / 180)));
                    y1 = endP.y + (float)(30 * Math.sin(angle * (Math.PI / 180)));

                    mCurrentPath.moveTo(endP.x, endP.y);
                    mCurrentPath.lineTo(x1, y1);

                    double angle2 = mfDegree + 45;
                    x2 = endP.x + (float)(30 * Math.cos(angle2 * (Math.PI / 180)));
                    y2 = endP.y + (float)(30 * Math.sin(angle2 * (Math.PI / 180)));
                    mCurrentPath.moveTo(endP.x, endP.y);
                    mCurrentPath.lineTo(x2, y2);
                    canvas.drawPath(mCurrentPath, mCurrentPaint);
                }
            }
        }

        // If the path is finished, add it to the history
        if (finishedPath && mPoints.size() > 0) {
            Log.d("after finishedPath", "editType : " + drawType.toString());
            createHistoryPathFromPoints();
        }
    }

    // Create a path from the current points
    private void createHistoryPathFromPoints() {

        mPaths.add(new HistoryPath(mPoints, new Paint(mCurrentPaint)));

        mPoints = new ArrayList<>();

        notifyPathDrawn();
        notifyRedoUndoCountChanged();
    }

    public void saveHistoryPathFromPoints() {
        if (mPoints.size() > 0) {
            mPaths.add(new HistoryPath(mPoints, new Paint(mCurrentPaint)));
            mPoints = new ArrayList<>();
        }
    }

    public DrawType drawType = null;
    public DrawPathType pathType = DrawPathType.Curve;

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            notifyPathStart();
        }
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }

        // Clear all the history when restarting to draw
        mCanceledPaths = new ArrayList<>();
        int BUTTON_STYLUS = 213; // spen 버튼 클릭시 왜 motionEvent가 213으로 표기될까?

        if ( motionEvent.getAction() == MotionEvent.ACTION_MOVE || motionEvent.getAction() == BUTTON_STYLUS) {

            if (motionEvent.getAction() == BUTTON_STYLUS) {
                isStylusBtnClicked = true;
                float ERASE_THICK = 28f;
                setEraserFromOnTouch(ERASE_THICK);
            } else if (isStylusBtnClicked) {
                isStylusBtnClicked = false;
                setPencil(pp.mode, pp.color, pp.alpha, pp.stroke);
            }
            Point point;
            for (int i = 0; i < motionEvent.getHistorySize(); i++) {
                point = new Point(pathType);
                point.x = motionEvent.getHistoricalX(i);
                point.y = motionEvent.getHistoricalY(i);
                mPoints.add(point);
            }
            point = new Point(pathType);
            point.x = motionEvent.getX();
            point.y = motionEvent.getY();
            mPoints.add(point);
            mFinishPath = false;

        } else {
            mFinishPath = true;
//            if (mFinishPath && mPoints.size() > 0) {
//                if (mPoints.size() < 50) {
//
//                    if (isPencilcaseVisibleBeforeOnTouchDraw) {
//                        mPoints = new ArrayList<>();
//                    }
//
//                } else {
//                    createHistoryPathFromPoints();
//                }
//            }
        }

        invalidate();
        return false;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        float xMultiplyFactor = 1;
        float yMultiplyFactor = 1;


        if (mLastDimensionW == -1) {
            mLastDimensionW = w;
        }

        if (mLastDimensionH == -1) {
            mLastDimensionH = h;
        }

        if (w >= 0 && w != oldw && w != mLastDimensionW) {
            xMultiplyFactor = (float) w / mLastDimensionW;
            mLastDimensionW = w;
        }

        if (h >= 0 && h != oldh && h != mLastDimensionH) {
            yMultiplyFactor = (float) h / mLastDimensionH;
            mLastDimensionH = h;
        }

        multiplyPathsAndPoints(xMultiplyFactor, yMultiplyFactor);
    }

    // Translate all the paths, used every time that this view size is changed
    @SuppressWarnings("SuspiciousNameCombination")
    private void multiplyPathsAndPoints(float xMultiplyFactor, float yMultiplyFactor) {

        // If both factors == 1 or <= 0 or no paths/points to apply things, just return
        if ((xMultiplyFactor == 1 && yMultiplyFactor == 1)
                || (xMultiplyFactor <= 0 || yMultiplyFactor <= 0) ||
                (mPaths.size() == 0 && mCanceledPaths.size() == 0 && mPoints.size() == 0)) {
            return;
        }

        if (mResizeBehaviour == ResizeBehaviour.CLEAR) {// If clear, clear all and return
            mPaths = new ArrayList<>();
            mCanceledPaths = new ArrayList<>();
            mPoints = new ArrayList<>();
            return;
        } else if (mResizeBehaviour == ResizeBehaviour.CROP) {
            xMultiplyFactor = yMultiplyFactor = 1;
        }

        // Adapt drawn paths
        for (com.freewheelin.pulley.legacy.views.memoView.HistoryPath historyPath : mPaths) {

            if (historyPath.isPoint()) {
                historyPath.setOriginX(historyPath.getOriginX() * xMultiplyFactor);
                historyPath.setOriginY(historyPath.getOriginY() * yMultiplyFactor);
            } else {
                for (com.freewheelin.pulley.legacy.views.memoView.Point point : historyPath.getPoints()) {
                    point.x *= xMultiplyFactor;
                    point.y *= yMultiplyFactor;
                }
            }

            historyPath.generatePath();
        }

        // Adapt canceled paths
        for (com.freewheelin.pulley.legacy.views.memoView.HistoryPath historyPath : mCanceledPaths) {

            if (historyPath.isPoint()) {
                historyPath.setOriginX(historyPath.getOriginX() * xMultiplyFactor);
                historyPath.setOriginY(historyPath.getOriginY() * yMultiplyFactor);
            } else {
                for (com.freewheelin.pulley.legacy.views.memoView.Point point : historyPath.getPoints()) {
                    point.x *= xMultiplyFactor;
                    point.y *= yMultiplyFactor;
                }
            }

            historyPath.generatePath();
        }

        // Adapt drawn points
        for (com.freewheelin.pulley.legacy.views.memoView.Point point : mPoints) {
            point.x *= xMultiplyFactor;
            point.y *= yMultiplyFactor;
        }
    }

    public interface DrawCreatorListener {
        void onDrawCreated(Bitmap draw);

        void onDrawCreationError();
    }


    private class TakeScreenShotAsyncTask extends AsyncTask<Void, Void, Void> {
        private int mWidth, mHeight;
        private Canvas mCanvas;
        private Bitmap mBitmap;
        private DrawCreatorListener mListener;

        public TakeScreenShotAsyncTask(@NonNull DrawCreatorListener listener) {
            mListener = listener;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mWidth = getWidth();
            mHeight = getHeight();
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                mBitmap = Bitmap.createBitmap(
                        mWidth, mHeight, Bitmap.Config.ARGB_8888);
                mCanvas = new Canvas(mBitmap);
            } catch (Exception e) {
                e.printStackTrace();
                cancel(true);
            }

            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();

            if (mListener != null) {
                mListener.onDrawCreationError();
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            draw(mCanvas);

            if (mListener != null) {
                mListener.onDrawCreated(mBitmap);
            }
        }
    }
}
