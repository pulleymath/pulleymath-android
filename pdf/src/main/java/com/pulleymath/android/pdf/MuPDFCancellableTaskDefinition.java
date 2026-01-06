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

import com.artifex.mupdf.fitz.Cookie;

public abstract class MuPDFCancellableTaskDefinition<Params, Result> implements com.pulleymath.android.pdf.CancellableTaskDefinition<Params, Result>
{
	private Cookie cookie;

	public MuPDFCancellableTaskDefinition()
	{
		this.cookie = new Cookie();
	}

	@Override
	public void doCancel()
	{
		if (cookie == null)
			return;

		cookie.abort();
	}

	@Override
	public void doCleanup()
	{
		if (cookie == null)
			return;

		cookie.destroy();
		cookie = null;
	}

	@Override
	public final Result doInBackground(Params ... params)
	{
		return doInBackground(cookie, params);
	}

	public abstract Result doInBackground(Cookie cookie, Params ... params);
}
