/*
 * This file is derived from the MuPDF Android viewer
 * (https://github.com/ArtifexSoftware/mupdf-android-viewer).
 * Copyright (C) 2006-2017 Artifex Software, Inc.
 *
 * Modified by Freewheelin Inc. since 2021 for the Pulley Math app
 * (package rename and app-specific changes).
 * Modifications Copyright (C) 2021-2023 Freewheelin Inc.
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

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.Serializable;
import java.util.ArrayList;

public class OutlineActivity extends ListActivity
{
	public static class Item implements Serializable {
		public String title;
		public int page;
		public Item(String title, int page) {
			this.title = title;
			this.page = page;
		}
		public String toString() {
			return title;
		}
	}

	protected ArrayAdapter<Item> adapter;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		adapter = new ArrayAdapter<Item>(this, android.R.layout.simple_list_item_1);
		setListAdapter(adapter);

		Bundle bundle = getIntent().getExtras();
		int currentPage = bundle.getInt("POSITION");
		ArrayList<Item> outline = (ArrayList<Item>)bundle.getSerializable("OUTLINE");
		int found = -1;
		for (int i = 0; i < outline.size(); ++i) {
			Item item = outline.get(i);
			if (found < 0 && item.page >= currentPage)
				found = i;
			adapter.add(item);
		}
		if (found >= 0)
			setSelection(found);
	}

	protected void onListItemClick(ListView l, View v, int position, long id) {
		Item item = adapter.getItem(position);
		setResult(RESULT_FIRST_USER + item.page);
		finish();
	}
}
