package com.freewheelin.pulley.views.memoView

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
//import com.microsoft.appcenter.utils.HandlerUtils.runOnUiThread
import java.io.*

/**
 * Created by Riccardo on 23/05/2017.
 */

public class FileHelper {

    companion object {
        fun saveStateIntoFile(
                context: Context?,
                state: FreeDrawSerializableState?,
                fileName: String,
                listener: StateSaveInterface?) {

            if (context != null && state != null) {
                StateSaveRunnable(context, listener, state, fileName).run()
            } else {
                listener?.onStateSaveError()
            }
        }

        fun getSavedStoreFromFile(
                context: Context?,
                fileName: String,
                listener: StateExtractorInterface?) {

            if (context != null) {
                StateExtractorRunnable(context, listener, fileName).run()
            } else {

                listener?.onStateExtractionError()
            }
        }

        fun deleteSavedStateFile(context: Context?, fileName: String) {

            if (context != null) {

                var fos: FileOutputStream? = null
                try {
                    fos = context.openFileOutput(fileName, Context.MODE_PRIVATE)
                    val os = ObjectOutputStream(fos)
                    os.close()
                    fos!!.close()
                } catch (e: Exception) {
                    e.printStackTrace()

                    if (fos != null) {

                        try {
                            fos.close()
                        } catch (e1: Exception) {
                            e1.printStackTrace()
                        }

                    }
                }

            }
        }
    }


    // Runnable that extracts the FreeDrawSerializableState from a file
    private class StateExtractorRunnable(
            private val mContext: Context,
            private val mListener: StateExtractorInterface?,
            val fileName: String) : Runnable {

        override fun run() {
            try {
                var fis: FileInputStream? = null

                val file = mContext.getFileStreamPath(fileName)
                if (file.exists() == false) {
                    mListener?.onStateExtractionError()
                    return
                }

                fis = mContext.openFileInput(fileName)
                val `is` = ObjectInputStream(fis)

                val state = `is`.readObject() as FreeDrawSerializableState

                fis!!.close()
                `is`.close()

                mListener?.onStateExtracted(state)
            } catch (e:Exception) {
                Log.e(javaClass.simpleName, "state Extractor error:${e.localizedMessage}")
            }
        }
    }


    // Runnable that save a FreeDrawSerializableState inside a file
    private class StateSaveRunnable(
            private val mContext: Context,
            private val mListener: StateSaveInterface?,
            private val mState: FreeDrawSerializableState,
            private val fileName: String) : Runnable {

        override fun run() {
            var fos: FileOutputStream? = null
            fos = mContext.openFileOutput(fileName, Context.MODE_PRIVATE)
            val os = ObjectOutputStream(fos)
            os.writeObject(mState)
            os.flush()
            fos!!.flush()
            os.close()
            fos.close()

            mListener?.onStateSaved()
        }
    }


    // Listener for file creation
    interface StateSaveInterface {
        fun onStateSaved()

        fun onStateSaveError()
    }

    // Listener for file data extraction
    interface StateExtractorInterface {
        fun onStateExtracted(state: FreeDrawSerializableState)

        fun onStateExtractionError()
    }


    // Shortcut method to run on uiThread a runnable
    private fun runOnUiThread(runnable: Runnable) {

        Handler(Looper.getMainLooper()).post(runnable)
    }
}
