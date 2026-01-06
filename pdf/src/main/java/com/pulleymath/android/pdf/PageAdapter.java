/*
 * This file is derived from the MuPDF Android viewer
 * (https://github.com/ArtifexSoftware/mupdf-android-viewer).
 * Copyright (C) 2006-2017 Artifex Software, Inc.
 *
 * Modified by Freewheelin Inc. since 2021 for the Pulley Math app
 * (package rename and app-specific changes).
 * Modifications Copyright (C) 2021-2026 Freewheelin Inc.
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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;

import com.pulleymath.android.pdf.memo.MemoView;
import com.pulleymath.android.pdf.memo.PathRedoUndoCountChangeListener;
import com.pulleymath.android.pdf.memo.PencilPanel;
//import com.pulleymath.android.pdf.memo.PencilcaseView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PageAdapter extends BaseAdapter implements PathRedoUndoCountChangeListener {
	private final Context mContext;
	private final com.pulleymath.android.pdf.MuPDFCore mCore;
  public boolean fingerDrawMode = false;
	private final SparseArray<PointF> mPageSizes = new SparseArray<PointF>();
	private       Bitmap mSharedHqBm;

	public final static String TAG_PAGEVIEW = "pageView";
	public final static String TAG_MEMOVIEW = "memoView";

	private PencilPanel penPanel;

	private String drawingId = "";

	public PageAdapter(Context c, com.pulleymath.android.pdf.MuPDFCore core, boolean fingerDrawMode) {
		mContext = c;
		mCore = core;
    this.fingerDrawMode = fingerDrawMode;
	}

	public int getCount() {
		return mCore.countPages();
	}

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return 0;
	}

	public void releaseBitmaps()
	{
		//  recycle and release the shared bitmap.
		if (mSharedHqBm!=null)
			mSharedHqBm.recycle();
		mSharedHqBm = null;
	}

	public void refresh() {
		mPageSizes.clear();
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		FrameLayout container;
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
		final com.pulleymath.android.pdf.PageView pageView;
		if (convertView == null) {
			if (mSharedHqBm == null || mSharedHqBm.getWidth() != parent.getWidth() || mSharedHqBm.getHeight() != parent.getHeight())
				mSharedHqBm = Bitmap.createBitmap(parent.getWidth(), parent.getHeight(), Bitmap.Config.ARGB_8888);
			container = new FrameLayout(mContext);
			pageView = new com.pulleymath.android.pdf.PageView(mContext, mCore, new Point(parent.getWidth(), parent.getHeight()), mSharedHqBm);
			pageView.setTag(TAG_PAGEVIEW);
			container.addView(pageView);

			// pageview 가 생성될 때 memoview add
			final MemoView memoView = new MemoView(mContext);
			memoView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
			memoView.set(penPanel);
			memoView.setLayoutParams(layoutParams);
			memoView.setTag(TAG_MEMOVIEW);
      memoView.setPathRedoUndoCountChangeListener(this);
      memoView.setFingerDrawMode(fingerDrawMode);

			pageView.memoView = memoView;
			container.addView(memoView);

		} else {
			container = (FrameLayout) convertView;
			pageView = container.findViewWithTag(TAG_PAGEVIEW);
		}
		// 저장한 드로잉 불러오기
		loadDrawing(container, position);

		PointF pageSize = mPageSizes.get(position);

		if (pageSize != null) {
			// We already know the page size. Set it up
			// immediately
			pageView.setPage(position, pageSize);
		} else {
			// Page size as yet unknown. Blank it for now, and
			// start a background task to find the size
			pageView.blank(position);
			AsyncTask<Void,Void,PointF> sizingTask = new AsyncTask<Void,Void,PointF>() {
				@Override
				protected PointF doInBackground(Void... arg0) {
					try {
						return mCore.getPageSize(position);
					} catch (Exception e) {
						return null;
					}
				}

				@Override
				protected void onPostExecute(PointF result) {
					super.onPostExecute(result);
					if(result != null) {
						// We now know the page size
						mPageSizes.put(position, result);
						// Check that this view hasn't been reused for another page since we started
						if (pageView.getPage() == position)
							pageView.setPage(position, result);
					}
				}
			};
			sizingTask.execute((Void)null);
		}

//		return pageView;
		return container;
	}
	private void loadDrawing(View container, int position) {
		MemoView memoView = container.findViewWithTag(TAG_MEMOVIEW);
		String memoId = drawingId + position;
		memoView.setMemoId(memoId);
		memoView.clearBitmap();
		memoView.load();
	}

  public void setMemoViewFingerDrawModeInPencilcase(boolean value) {
      this.penPanel.setFingerDrawModeWithPencilcase(value);
  }
	public void setPenPanel(@Nullable PencilPanel penPanel) {
		this.penPanel = penPanel;
	}
	public void setDrawingId(@NotNull String drawingId) {
		this.drawingId = drawingId;
	}

    @Override
    public void onUndoCountChanged(int undoCount) {
        if (this.penPanel.getMemoViews().size() == 0) return;
        List<MemoView> list = this.penPanel.getMemoViews();
        int totalUndoCount = 0;
        for (int i = 0; i < list.size(); i++) {
            totalUndoCount += list.get(i).getUndoCount();
        }
        this.penPanel.setUndoCount(totalUndoCount);
    }

    @Override
    public void onRedoCountChanged(int redoCount) {
        if (this.penPanel.getMemoViews().size() == 0) return;
        List<MemoView> list = this.penPanel.getMemoViews();
        int totalRedoCount = 0;
        for (int i = 0; i < list.size(); i++) {
            totalRedoCount += list.get(i).getRedoCount();
        }
        this.penPanel.setRedoCount(totalRedoCount);
    }
}
