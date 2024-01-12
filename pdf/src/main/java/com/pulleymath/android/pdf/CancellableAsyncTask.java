/*
 * This file is derived from the MuPDF Android viewer
 * (https://github.com/ArtifexSoftware/mupdf-android-viewer).
 * Copyright (C) 2006-2017 Artifex Software, Inc.
 *
 * Modified by Freewheelin Inc. since 2021 for the Pulley Math app
 * (package rename and app-specific changes).
 * Modifications Copyright (C) 2021-2024 Freewheelin Inc.
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

import android.os.AsyncTask;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

// Ideally this would be a subclass of AsyncTask, however the cancel() method is final, and cannot
// be overridden. I felt that having two different, but similar cancel methods was a bad idea.
public class CancellableAsyncTask<Params, Result>
{
	private final AsyncTask<Params, Void, Result> asyncTask;
	private final CancellableTaskDefinition<Params, Result> ourTask;

	public void onPreExecute()
	{

	}

	public void onPostExecute(Result result)
	{

	}

	public CancellableAsyncTask(final CancellableTaskDefinition<Params, Result> task)
	{
		if (task == null)
				throw new IllegalArgumentException();

		this.ourTask = task;
		asyncTask = new AsyncTask<Params, Void, Result>()
				{
					@Override
					protected Result doInBackground(Params... params)
					{
						return task.doInBackground(params);
					}

					@Override
					protected void onPreExecute()
					{
						com.pulleymath.android.pdf.CancellableAsyncTask.this.onPreExecute();
					}

					@Override
					protected void onPostExecute(Result result)
					{
						com.pulleymath.android.pdf.CancellableAsyncTask.this.onPostExecute(result);
						task.doCleanup();
					}

					@Override
					protected void onCancelled(Result result)
					{
						task.doCleanup();
					}
				};
	}

	public void cancel()
	{
		this.asyncTask.cancel(true);
		ourTask.doCancel();

		try
		{
			this.asyncTask.get();
		}
		catch (InterruptedException e)
		{
		}
		catch (ExecutionException e)
		{
		}
		catch (CancellationException e)
		{
		}
	}

	public void execute(Params ... params)
	{
		asyncTask.execute(params);
	}

}
