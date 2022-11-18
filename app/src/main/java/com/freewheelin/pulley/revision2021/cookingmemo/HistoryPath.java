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

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Riccardo Moro on 9/27/2016.
 */

class HistoryPath implements Parcelable, Serializable {

    static final float ERASE_WIDTH = 10; //dp

    static final long serialVersionUID = 41L;

    private static final String TAG = HistoryPath.class.getSimpleName();

    private static transient PorterDuffXfermode clear = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);

    private ArrayList<Point> points = new ArrayList<>();
    private int paintColor;
    private int paintAlpha;
    private float paintWidth;
    private float originX, originY;
    private boolean isPoint;

    private transient Path path = null;
    private transient Paint paint = null;

    private boolean isErase = false;

    HistoryPath(@NonNull ArrayList<Point> points, @NonNull Paint paint) {
        this.points = new ArrayList<>(points);
        this.paintColor = paint.getColor();
        this.paintAlpha = paint.getAlpha();
        this.paintWidth = paint.getStrokeWidth();
        this.originX = points.get(0).x;
        this.originY = points.get(0).y;
        this.isPoint = FreeDrawHelper.isAPoint(points);

        this.isErase = (paint.getXfermode() != null) ? true : false;

        generatePath();
        generatePaint();
    }

    public void generatePath() {

        path = new Path();

        if (points != null) {
            boolean first = true;

            for (int i = 0; i < points.size(); i++) {

                Point point = points.get(i);

                if (first) {
                    path.moveTo(point.x, point.y);
                    first = false;
                } else {
                    path.lineTo(point.x, point.y);
                }
            }
        }
    }

    private void generatePaint() {

        paint = FreeDrawHelper.createPaintAndInitialize(paintColor, paintAlpha, paintWidth,
                isPoint);

        if (isErase) paint.setXfermode(clear);
    }

    public Path getPath() {

        if (path == null) {

            generatePath();
        }

        return path;
    }

    public boolean isPoint() {
        return isPoint;
    }

    public void setPoint(boolean point) {
        isPoint = point;
    }

    public float getOriginX() {
        return originX;
    }

    public void setOriginX(float originX) {
        this.originX = originX;
    }

    public float getOriginY() {
        return originY;
    }

    public void setOriginY(float originY) {
        this.originY = originY;
    }

    public int getPaintColor() {
        return paintColor;
    }

    public void setPaintColor(int paintColor) {
        this.paintColor = paintColor;
    }

    public int getPaintAlpha() {
        return paintAlpha;
    }

    public void setPaintAlpha(int paintAlpha) {
        this.paintAlpha = paintAlpha;
    }

    public float getPaintWidth() {
        return paintWidth;
    }

    public void setPaintWidth(float paintWidth) {
        this.paintWidth = paintWidth;
    }

    public Paint getPaint() {

        if (paint == null) {
            generatePaint();
        }

        return paint;
    }

    public ArrayList<Point> getPoints() {
        return points;
    }

    public void setPoints(ArrayList<Point> points) {
        this.points = points;
    }

    // Parcelable stuff
    private HistoryPath(Parcel in) {
        in.readTypedList(points, Point.CREATOR);

        paintColor = in.readInt();
        paintAlpha = in.readInt();
        paintWidth = in.readFloat();

        originX = in.readFloat();
        originY = in.readFloat();

        isPoint = in.readByte() != 0;

        generatePath();
        generatePaint();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(points);

        dest.writeInt(paintColor);
        dest.writeInt(paintAlpha);
        dest.writeFloat(paintWidth);

        dest.writeFloat(originX);
        dest.writeFloat(originY);

        dest.writeByte((byte) (isPoint ? 1 : 0));
    }

    // Parcelable CREATOR class
    public static final Creator<HistoryPath> CREATOR = new Creator<HistoryPath>() {
        @Override
        public HistoryPath createFromParcel(Parcel in) {
            return new HistoryPath(in);
        }

        @Override
        public HistoryPath[] newArray(int size) {
            return new HistoryPath[size];
        }
    };

    @Override
    public String toString() {
        return "Point: " + isPoint + "\n" +
                "Points: " + points + "\n" +
                "Color: " + paintColor + "\n" +
                "Alpha: " + paintAlpha + "\n" +
                "Width: " + paintWidth;
    }

    public Boolean isIn(Point point) {
        for (Point myPoint : points) {

             float xDiff = Math.abs(myPoint.x - point.x);
             float yDiff = Math.abs((myPoint.y - point.y));

             if (xDiff < ERASE_WIDTH && yDiff < ERASE_WIDTH) {
                 return true;
             }
        }

        return false;
    }
}
