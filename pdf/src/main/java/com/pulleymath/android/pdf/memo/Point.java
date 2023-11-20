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
package com.pulleymath.android.pdf.memo;


import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by Riccardo Moro on 9/25/2016.
 */

class Point implements Parcelable, Serializable {

    static final long serialVersionUID = 42L;

    float x, y;

    Point() {
        x = y = -1;
    }

    public DrawPathType type = DrawPathType.Curve;
    Point(DrawPathType type) {
        this.type = type;
        x = y = -1;
    }

    @Override
    public String toString() {
        return "" + x + " : " + y + " - ";
    }


    // Parcelable stuff
    private Point(Parcel in) {
        x = in.readFloat();
        y = in.readFloat();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(x);
        dest.writeFloat(y);
    }

    // Parcelable CREATOR class
    public static final Creator<Point> CREATOR = new Creator<Point>() {
        @Override
        public Point createFromParcel(Parcel in) {
            return new Point(in);
        }

        @Override
        public Point[] newArray(int size) {
            return new Point[size];
        }
    };
}
