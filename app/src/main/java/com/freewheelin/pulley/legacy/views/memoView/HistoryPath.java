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
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;

import com.freewheelin.pulley.revision2023.ui.view.DrawPathType;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Riccardo Moro on 9/27/2016.
 */

public class HistoryPath implements Parcelable, Serializable {

    static final float ERASE_WIDTH = 10; //dp

    static final long serialVersionUID = 41L;

    private static final String TAG = HistoryPath.class.getSimpleName();
    private final transient PorterDuffXfermode clear = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);

    private ArrayList<com.freewheelin.pulley.legacy.views.memoView.Point> points = new ArrayList<>();
    private int paintColor;
    private int paintAlpha;
    private float paintWidth;
    private float originX, originY;
    private boolean isPoint;

    private transient Path path = null;
    private transient Paint paint = null;
    public boolean isErase = false;


    public DrawPathType type = DrawPathType.Curve;
    public LocalDateTime createdAt;

    HistoryPath(@NonNull ArrayList<com.freewheelin.pulley.legacy.views.memoView.Point> points, @NonNull Paint paint) {
        this.points = new ArrayList<>(points);
        this.type = points.get(0).type;
        this.paintColor = paint.getColor();
        this.paintAlpha = paint.getAlpha();
        this.paintWidth = paint.getStrokeWidth();
        this.originX = points.get(0).x;
        this.originY = points.get(0).y;
        this.isPoint = FreeDrawHelper.isAPoint(points);
        this.createdAt = LocalDateTime.now();
        this.isErase = paint.getXfermode() != null;

        generatePath();
        generatePaint();
    }

    public void generatePath() {

        path = new Path();

        if (points != null) {
            boolean first = true;

            if (type == DrawPathType.Curve) {
                for (int i = 0; i < points.size(); i++) {

                    Point point = points.get(i);

                    if (first) {
                        path.moveTo(point.x, point.y);
                        first = false;
                    } else if (type == DrawPathType.Curve) {
                        path.lineTo(point.x, point.y);
                    }
                }
            } else if (type == DrawPathType.Circle) {
                Point startP = points.get(0);
                path.moveTo(startP.x, startP.y);

                Point endP = points.get(points.size() - 1);
                double dx = (double) (endP.x - startP.x);
                double dy = (double) (endP.y - startP.y);
                double radius = Math.sqrt(dx * dx + dy * dy);

                path.addCircle(startP.x, startP.y, (float) radius, Path.Direction.CW);
            } else if (type == DrawPathType.Line) {
                Point startP = points.get(0);
                path.moveTo(startP.x, startP.y);

                Point endP = points.get(points.size() - 1);
                path.lineTo(endP.x, endP.y);
            } else if (type == DrawPathType.Arrow) {
                Point startP = points.get(0);
                path.moveTo(startP.x, startP.y);

                Point endP = points.get(points.size() - 1);
                path.lineTo(endP.x, endP.y);

                // A  = endp,  B = startp
                double mfDegree = Math.atan2(startP.y - endP.y, startP.x - endP.x) * 180 / Math.PI;
                float x1 = 0, y1 = 0, x2 = 0, y2 = 0;
                double angle = mfDegree - 45;
                x1 = endP.x + (float)(30 * Math.cos(angle * (Math.PI / 180)));
                y1 = endP.y + (float)(30 * Math.sin(angle * (Math.PI / 180)));

                path.moveTo(endP.x, endP.y);
                path.lineTo(x1, y1);

                double angle2 = mfDegree + 45;
                x2 = endP.x + (float)(30 * Math.cos(angle2 * (Math.PI / 180)));
                y2 = endP.y + (float)(30 * Math.sin(angle2 * (Math.PI / 180)));
                path.moveTo(endP.x, endP.y);
                path.lineTo(x2, y2);

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

    public ArrayList<com.freewheelin.pulley.legacy.views.memoView.Point> getPoints() {
        return points;
    }

    public void setPoints(ArrayList<com.freewheelin.pulley.legacy.views.memoView.Point> points) {
        this.points = points;
    }

    // Parcelable stuff
    private HistoryPath(Parcel in) {
        in.readTypedList(points, com.freewheelin.pulley.legacy.views.memoView.Point.CREATOR);

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

    public Boolean isIn(com.freewheelin.pulley.legacy.views.memoView.Point point) {
        if (type == DrawPathType.Curve) {
            for (com.freewheelin.pulley.legacy.views.memoView.Point myPoint : points) {

                float xDiff = Math.abs(myPoint.x - point.x);
                float yDiff = Math.abs((myPoint.y - point.y));

                if (xDiff < ERASE_WIDTH && yDiff < ERASE_WIDTH) {
                    return true;
                }
            }
        } else if (type == DrawPathType.Circle) {
            Point startP = points.get(0);
            Point endP = points.get(points.size() - 1);

            double dx = (double) (endP.x - startP.x);
            double dy = (double) (endP.y - startP.y);
            double radius = Math.sqrt(dx * dx + dy * dy);

            double dPx = (double) (point.x - startP.x);
            double dPy = (double) (point.y - startP.y);
            double pRadius = Math.sqrt(dPx * dPx + dPy * dPy);

            return Math.abs(pRadius - radius) < ERASE_WIDTH;
        } else if (type == DrawPathType.Line) {
            Point startP = points.get(0);
            Point endP = points.get(points.size() - 1);

            float maxX = Math.max(startP.x, endP.x);
            float minX = Math.min(startP.x, endP.x);
            float maxY = Math.max(startP.y, endP.y);
            float minY = Math.min(startP.y, endP.y);
            if (point.x > maxX || point.x < minX) {
                return false;
            }
            if (point.y > maxY || point.y < minY) {
                return false;
            }

            double m = (endP.y - startP.y) / (endP.x - startP.x);
            double b = startP.y - (m * startP.x);
            double distance = Math.abs((m*point.x) + (point.y*-1)+b) / Math.sqrt(m*m + 1);

            return distance < ERASE_WIDTH;
        }

        return false;
    }
}
