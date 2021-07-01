/*
 * This file is derived from the MuPDF Android viewer
 * (https://github.com/ArtifexSoftware/mupdf-android-viewer).
 * Copyright (C) 2006-2017 Artifex Software, Inc.
 *
 * Modified by Freewheelin Inc. since 2021 for the Pulley Math app
 * (package rename and app-specific changes).
 * Modifications Copyright (C) 2021-2021 Freewheelin Inc.
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package com.pulleymath.android.pdf;

import android.annotation.SuppressLint;
import android.os.Build;
import android.view.View;

public class Stepper {
	protected final View mPoster;
	protected final Runnable mTask;
	protected boolean mPending;

	public Stepper(View v, Runnable r) {
		mPoster = v;
		mTask = r;
		mPending = false;
	}

	@SuppressLint("NewApi")
	public void prod() {
		if (!mPending) {
			mPending = true;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				mPoster.postOnAnimation(new Runnable() {
					@Override
					public void run() {
						mPending = false;
						mTask.run();
					}
				});
			} else {
				mPoster.post(new Runnable() {
					@Override
					public void run() {
						mPending = false;
						mTask.run();
					}
				});

			}
		}
	}
}
