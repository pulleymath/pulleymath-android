package com.pulleymath.android.pdf

import android.Manifest
import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.pulleymath.android.pdf.ReaderView.ViewMapper
import com.pulleymath.android.pdf.log.Network
import com.pulleymath.android.pdf.log.PdfPageLog
import com.pulleymath.android.pdf.log.PdfReadLog
import com.pulleymath.android.pdf.memo.PencilcaseView
import com.pulleymath.android.pdf.memo.storage.DatabaseHelper
import com.pulleymath.android.pdf.memo.storage.PdfMemo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import java.util.*
import kotlin.concurrent.thread

/*
    1. 해설페이지 연결
    2. 필터
    3. 사용시간 업데이트
    TODO: 4. 남은 디스크 용량 체크
          5. 파일 삭제
 */

open class PdfViewerActivity : Activity() {
    /* The core rendering instance */
    internal enum class TopBarMode {
        Main, Search, More
    }

    private val OUTLINE_REQUEST = 0
    private val PERMISSION_REQUEST = 0
    private var core: MuPDFCore? = null
    private var mFileName: String? = null
    private var mFileKey: String? = null
    private var mDocView: ReaderView? = null
    private var mButtonsView: View? = null
    private var mButtonsVisible = false
    private var mPasswordView: EditText? = null
    private var mFilenameView: TextView? = null
    private var mPageSlider: SeekBar? = null
    private var mPageSliderRes = 0
    private var mPageNumberView: TextView? = null
    private var mSearchButton: ImageButton? = null
    private var mOutlineButton: ImageButton? = null
    private var mTopBarSwitcher: ViewAnimator? = null
    private var mLinkButton: ImageButton? = null
    private var mTopBarMode = TopBarMode.Main
    private var mSearchBack: ImageButton? = null
    private var mSearchFwd: ImageButton? = null
    private var mSearchClose: ImageButton? = null
    private var mSearchText: EditText? = null
    private var mSearchTask: SearchTask? = null
    private var mAlertBuilder: AlertDialog.Builder? = null
    private var mLinkHighlight = false
    private val mHandler = Handler(Looper.getMainLooper())
    private val mAlertsActive = false
    private val mAlertDialog: AlertDialog? = null
    private var mFlatOutline: ArrayList<OutlineActivity.Item>? = null
    protected var mDisplayDPI = 0
    private var mLayoutEM = 10
    private var mLayoutW = 312
    private var mLayoutH = 504
    private var mLayoutButton: View? = null
    private var mLayoutPopupMenu: PopupMenu? = null

//    private val CACHE_DIR by lazy {"$filesDir/pdfs"}
    lateinit var mBackButton: View
    lateinit var mAnswerButton: View

    private var pageNo: Int = 0
    private var targetPageNo: Int = 0
    private var bookTitle: String? = ""
    private var bookId: Int = 0
    private var pdfId: Int = 0
    private var answerPdfId: Int = 0
    private var includeAnswer: Boolean = false
    private var answerPath: String = ""
    private var answerPageLink: MutableMap<Int,Int> = mutableMapOf()

    private var studentId: String = ""

    lateinit var db:DatabaseHelper

    private fun toHex(digest: ByteArray): String {
        val builder = StringBuilder(2 * digest.size)
        for (b in digest) builder.append(String.format("%02x", b))
        return builder.toString()
    }

    private fun openFile(path: String?): MuPDFCore? {
        val lastSlashPos: Int = path?.lastIndexOf('/')?:-1
        mFileName = if (lastSlashPos == -1) path else (path?.substring(lastSlashPos + 1)?:"")
        println("Trying to open $path")
        try {
            mFileKey = mFileName
            core = MuPDFCore(path)
        } catch (e: Exception) {
            println(e)
            return null
        } catch (e: OutOfMemoryError) {
            //  out of memory is not an Exception, so we catch it separately.
            println(e)
            return null
        }
        return core
    }

    private fun openBuffer(buffer: ByteArray?, magic: String?): MuPDFCore? {
        println("Trying to open byte buffer")
        try {
            mFileKey = toHex(MessageDigest.getInstance("MD5").digest(buffer))
            core = MuPDFCore(buffer, magic)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
        return core
    }

    /** Called when the activity is first created. */
    lateinit var rootLayout:FrameLayout
    lateinit var bgPdfLoading:ImageView
    lateinit var bgPdfProgress:ProgressBar
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = DatabaseHelper.get(this)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        mDisplayDPI = metrics.densityDpi
        mAlertBuilder = AlertDialog.Builder(this)

        setContentView(R.layout.document_activity)
        rootLayout = findViewById(R.id.rootLayout)
        bgPdfLoading = findViewById(R.id.pdfLoading)
        bgPdfProgress = findViewById(R.id.pdfProgress)

//        createCacheDir()

        loadFile(savedInstanceState)
    }

//    private fun createCacheDir() {
//        File(CACHE_DIR)?.let { outputDir ->
//            if (!outputDir.exists()) {
//                outputDir.mkdirs()
//            }
//        }
//    }

    private fun loadFile(savedInstanceState: Bundle?) {
        if (core == null) {
            if (savedInstanceState != null && savedInstanceState.containsKey("FileName")) {
                mFileName = savedInstanceState.getString("FileName")
            }
        }
        if (core == null) {
            setIntentExtra()
            if (Intent.ACTION_VIEW == intent.action) {
                val uri = intent.data
                if (uri!!.scheme == "file") {
                    openLocalFile(uri, savedInstanceState)
                } else {
                    openDownloadedFile(uri.toString(), savedInstanceState)
                }
            }
        }
    }

    private fun setIntentExtra() {
        bookId = intent.getIntExtra(KEY_BOOK_ID, 0)
        bookTitle = intent.getStringExtra(KEY_BOOK_TITLE)
        pdfId = intent.getIntExtra(KEY_PDF_ID, 0)
        targetPageNo = intent.getIntExtra(KEY_TARGET_PAGE_NO, 0)

        answerPdfId = intent.getIntExtra(KEY_ANSWER_PDF_ID, 0)
        includeAnswer = intent.getBooleanExtra(KEY_INCLUDE_ANSWER, false)
        answerPath = intent.getStringExtra(KEY_ANSWER_PDF_PATH)?:""

        studentId =  intent.getStringExtra(KEY_STUDENT_ID)?:""
        token =  intent.getStringExtra(KEY_TOKEN)?:""

//        onTestApi = intent.getBooleanExtra(KEY_TEST_API_FLAG, false)
        onServerApi = intent.getStringExtra(KEY_API_FLAG) ?: "live"

        Network.token = token

        generateLinkAnswerPage()
    }

    private fun generateLinkAnswerPage() {
        intent.getStringExtra(KEY_ANSWER_PAGE_LINK)?.let { linkListString ->
            val linkList = linkListString.split("/")
            for (link in linkList) {
                val temp = link.split(":")
                answerPageLink[temp[0].toInt()] = temp[1].toInt()
            }
        }
    }

    private fun openLocalFile(uri: Uri, savedInstanceState: Bundle?) {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
//            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST)
//        }
        val path = uri.path
        core = openFile(path)
        SearchTaskResult.set(null)

        if (core != null && core!!.needsPassword()) {
            requestPassword(savedInstanceState)
            return
        }
        if (core != null && core!!.countPages() == 0) {
            core = null
        }

        fileLoaded(savedInstanceState)
    }

    private fun openDownloadedFile(path: String, savedInstanceState: Bundle?) {
        thread(start=true) {
            try {
                val byteArray = readFileByteArray(path)
                val byteString = String(byteArray)
                val decodedBase64 =
                    MuPDFCrypto.decrypt(byteString, getString(R.string.publisher_key))
                val decodedByteArray =
                    android.util.Base64.decode(decodedBase64, android.util.Base64.NO_PADDING)
                val magic = "application/pdf"

                core = openBuffer(decodedByteArray, magic)
                SearchTaskResult.set(null)

                if (core != null && core!!.needsPassword()) {
                    requestPassword(savedInstanceState)
                }

                if (core != null && core!!.countPages() == 0) {
                    core = null
                }

                runOnUiThread {
                    fileLoaded(savedInstanceState)
                }
            } catch (e:OutOfMemoryError) {
                showErrorMessage("파일 용량이 커서 열 수 없습니다. 메모리를 정리해야 합니다.")
            } catch (e: Exception) {
                showErrorMessage("다음과 같은 오류로 파일을 열 수 없습니다 : ${e.localizedMessage}")
            }
        }
    }

    fun showErrorMessage(msg:String?=null) {
        runOnUiThread {
            val alert = mAlertBuilder!!.create()
            alert.setTitle(R.string.cannot_open_document)
            if (msg != null)
                alert.setMessage(msg)
            alert.setButton(
                AlertDialog.BUTTON_POSITIVE,
                getString(R.string.dismiss)
            ) { dialog, which -> finish() }
            alert.setOnCancelListener { finish() }
            alert.show()
        }
    }

    fun readFileByteArray(path: String) : ByteArray {
        val file = File(path)
        val isr = FileInputStream(file)
        var len: Int
        val buffer = ByteArrayOutputStream().use { stream ->
            val data = ByteArray(16384)
            while (isr!!.read(data, 0, data.size).also { len = it } != -1) {
                stream.write(data, 0, len)
            }
            stream.flush()
            stream.toByteArray()
        }
        isr.close()
        return buffer
    }

    private fun fileLoaded(savedInstanceState: Bundle?) {
        if (core == null) {
            val alert = mAlertBuilder!!.create()
            alert.setTitle(R.string.cannot_open_document)
            alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.dismiss)) { dialog, which -> finish()
            }
            alert.setOnCancelListener { finish() }
            alert.show()
        } else {
            loadMemo(savedInstanceState)
        }
    }

    private fun requestPassword(savedInstanceState: Bundle?) {
        mPasswordView = EditText(this)
        mPasswordView!!.inputType = EditorInfo.TYPE_TEXT_VARIATION_PASSWORD
        mPasswordView!!.transformationMethod = PasswordTransformationMethod()
        val alert = mAlertBuilder!!.create()
        alert.setTitle(R.string.enter_password)
        alert.setView(mPasswordView)
        alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.okay)
        ) { dialog, which ->
            if (core!!.authenticatePassword(mPasswordView!!.text.toString())) {
                createUI(savedInstanceState)
            } else {
                requestPassword(savedInstanceState)
            }
        }
        alert.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel)) { dialog, which -> finish() }
        alert.show()
    }

    fun loadMemo(savedInstanceState: Bundle?) {
        CoroutineScope(Dispatchers.IO).launch {
            val latest:Long? = if(db.pdfWritingDao().countPdf(studentId, pdfId) < 1) null else db.pdfWritingDao().getLatestTimestamp(studentId)
            Log.d(javaClass.simpleName, "latest timestamp=$latest")
            Network.downloadMemo(studentId, pdfId, null, latest, { response ->
                CoroutineScope(Dispatchers.IO).launch {
                    db.pdfWritingDao().upsert(response?.data?: listOf())
                    runOnUiThread { createUI(savedInstanceState) }
                }
            },{ error ->
                Log.e(javaClass.simpleName, "download memo -> $error")
                runOnUiThread { createUI(savedInstanceState) }
            })
        }
    }

    fun createUI(savedInstanceState: Bundle?) {
        if (core == null) return
        // Now create the UI.
        // First create the document view
        mDocView = object : ReaderView(this) {
            override fun onMoveToChild(i: Int) {
                if (core == null) return
                mPageNumberView?.text = String.format(Locale.ROOT, "%d / %d", i + 1, core!!.countPages())
                mPageSlider!!.max = (core!!.countPages() - 1) * mPageSliderRes
                mPageSlider!!.progress = i * mPageSliderRes
                super.onMoveToChild(i)
            }
            override fun onTapMainDocArea() {
                if (!mButtonsVisible) { showButtons() } else { if (mTopBarMode == TopBarMode.Main) hideButtons() }
            }
            override fun onDocMotion() { hideButtons() }
            public override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
                if (core!!.isReflowable) {
                    mLayoutW = w * 72 / mDisplayDPI
                    mLayoutH = h * 72 / mDisplayDPI
                    relayoutDocument()
                } else {
                    refresh()
                }
            }
        }

        mSearchTask = object : SearchTask(this, core) {
            override fun onTextFound(result: SearchTaskResult) {
                SearchTaskResult.set(result)
                // Ask the ReaderView to move to the resulting page
                mDocView?.setDisplayedViewIndex(result.pageNumber)
                // Make the ReaderView act on the change to SearchTaskResult
                // via overridden onChildSetup method.
                mDocView?.resetupChildren()
            }
        }

        makeButtonsView()
        setButtons(mButtonsView!!)

        /** set drawingId */
        val adapter = PageAdapter(this, core)
        adapter.setDrawingId("memo_${studentId}_${pdfId}_")
        /** set pencilcase */
        val pencilcase = mButtonsView!!.findViewById(R.id.pencilcase) as PencilcaseView
        adapter.setPencilcase(pencilcase)

        mDocView?.adapter = adapter

        // Set up the page slider
        val smax = Math.max(core!!.countPages() - 1, 1)
        mPageSliderRes = (10 + smax - 1) / smax * 2

        // Set the file-name text
        val docTitle = bookTitle //core!!.title
        if (docTitle != null) mFilenameView!!.text = docTitle else mFilenameView!!.text = mFileName

        // Activate the seekbar
        mPageSlider!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {
//                mDocView?.pushHistory()
                mDocView?.setDisplayedViewIndex((seekBar.progress + mPageSliderRes / 2) / mPageSliderRes)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                updatePageNumView((progress + mPageSliderRes / 2) / mPageSliderRes)
            }
        })

        // Activate the search-preparing button
        mSearchButton!!.setOnClickListener { searchModeOn() }
        mSearchClose!!.setOnClickListener { searchModeOff() }

        // Search invoking buttons are disabled while there is no text specified
        mSearchBack!!.isEnabled = false
        mSearchFwd!!.isEnabled = false
        mSearchBack!!.setColorFilter(Color.argb(255, 128, 128, 128))
        mSearchFwd!!.setColorFilter(Color.argb(255, 128, 128, 128))

        // React to interaction with the text widget
        mSearchText!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                val haveText = s.toString().length > 0
                setButtonEnabled(mSearchBack, haveText)
                setButtonEnabled(mSearchFwd, haveText)

                // Remove any previous search results
                if (SearchTaskResult.get() != null && mSearchText!!.text.toString() != SearchTaskResult.get().txt) {
                    SearchTaskResult.set(null)
                    mDocView?.resetupChildren()
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })

        //React to Done button on keyboard
        mSearchText!!.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) search(1)
            false
        }
        mSearchText!!.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) search(1)
            false
        }

        // Activate search invoking buttons
        mSearchBack!!.setOnClickListener { search(-1) }
        mSearchFwd!!.setOnClickListener { search(1) }
        mLinkButton!!.setOnClickListener { setLinkHighlight(!mLinkHighlight) }

        /** just hide button */
        mOutlineButton!!.visibility = View.GONE
        mLinkButton!!.visibility = View.GONE

        // Reenstate last state if it was recorded
        val prefs = getPreferences(MODE_PRIVATE)
        mDocView?.setDisplayedViewIndex(prefs.getInt("page$mFileKey", 0))
        if (savedInstanceState == null || !savedInstanceState.getBoolean("ButtonsHidden", false)) showButtons()
        if (savedInstanceState != null && savedInstanceState.getBoolean("SearchMode", false)) searchModeOn()

        mDocView?.alpha = 0f

        rootLayout.addView(mDocView)
        rootLayout.addView(mButtonsView)

        setProgressAnimation()
        preventScreenShot()
        loadTargetPage()
    }

    fun relayoutDocument() {
        val loc = core!!.layout(mDocView!!.mCurrent, mLayoutW, mLayoutH, mLayoutEM)
        mFlatOutline = null
        mDocView!!.mHistory.clear()
        mDocView!!.refresh()
        mDocView!!.displayedViewIndex = loc
    }

    private fun setProgressAnimation() {
        val animator = ObjectAnimator.ofFloat(mDocView, View.ALPHA, 0f, 1f)
        animator.duration = 1000
        animator.addListener(object: Animator.AnimatorListener{
            override fun onAnimationStart(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) {
                rootLayout.removeView(bgPdfLoading)
                rootLayout.removeView(bgPdfProgress)
            }
            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationRepeat(animation: Animator?) {}
        })
        animator.start()
    }

    fun preventScreenShot() {
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
    }

    private fun loadTargetPage() {
        if(targetPageNo > 0)
            mDocView?.displayedViewIndex = targetPageNo - 1
    }

    private fun setButtons(view:View) {
        mBackButton = view.findViewById(R.id.backBtn)
        mBackButton.setOnClickListener { finish() }
        mAnswerButton = view.findViewById(R.id.answerBtn)
        mAnswerButton.setOnClickListener {
            loadAnswerPages()
        }
        mAnswerButton.visibility = View.GONE
    }
    /** 해설 페이지 열기 */
    private fun loadAnswerPages() {
        answerPageLink.get(pageNo)?.let { targetPageNo ->
            if(includeAnswer) { // 같은 pdf 면 페이지 이동
                mPageSlider?.progress = targetPageNo - 1
            } else {
                Intent(this, PdfViewerActivity::class.java).apply {

                    action = Intent.ACTION_VIEW
                    data = Uri.parse(answerPath)

                    putExtra(KEY_BOOK_ID, bookId)
                    putExtra(KEY_BOOK_TITLE, "(정답 및 해설) $bookTitle")
                    putExtra(KEY_PDF_ID, answerPdfId)
                    putExtra(KEY_TARGET_PAGE_NO, targetPageNo)
                    putExtra(KEY_INCLUDE_ANSWER, false)

                    putExtra(KEY_STUDENT_ID, studentId)
                    putExtra(KEY_TOKEN, token)

                    startActivity(this)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        when (requestCode) {
            OUTLINE_REQUEST -> if (resultCode >= RESULT_FIRST_USER) {
                mDocView!!.pushHistory()
                mDocView!!.displayedViewIndex = resultCode - RESULT_FIRST_USER
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (mFileKey != null && mDocView != null) {
            if (mFileName != null) outState.putString("FileName", mFileName)

            // Store current page in the prefs against the file name,
            // so that we can pick it up each time the file is loaded
            // Other info is needed only for screen-orientation change,
            // so it can go in the bundle
            val prefs = getPreferences(MODE_PRIVATE)
            val edit = prefs.edit()
            edit.putInt("page$mFileKey", mDocView!!.displayedViewIndex)
            edit.apply()
        }
        if (!mButtonsVisible) outState.putBoolean("ButtonsHidden", true)
        if (mTopBarMode == TopBarMode.Search) outState.putBoolean("SearchMode", true)
    }

    override fun onPause() {
        super.onPause()
        if (mSearchTask != null) mSearchTask!!.stop()
        if (mFileKey != null && mDocView != null) {
            val prefs = getPreferences(MODE_PRIVATE)
            val edit = prefs.edit()
            edit.putInt("page$mFileKey", mDocView!!.displayedViewIndex)
            edit.apply()
        }
    }

    private fun setButtonEnabled(button: ImageButton?, enabled: Boolean) {
        button!!.isEnabled = enabled
        button.setColorFilter(if (enabled) Color.argb(255, 255, 255, 255) else Color.argb(255, 128, 128, 128))
    }

    private fun setLinkHighlight(highlight: Boolean) {
        mLinkHighlight = highlight
        // LINK_COLOR tint
        mLinkButton!!.setColorFilter(if (highlight) Color.argb(0xFF, 0x00, 0x66, 0xCC) else Color.argb(0xFF, 255, 255, 255))
        // Inform pages of the change.
        mDocView!!.setLinksEnabled(highlight)
    }

    private fun showButtons() {
        if (core == null) return
        if (!mButtonsVisible) {
            mButtonsVisible = true
            // Update page number text and slider
            val index = mDocView!!.displayedViewIndex
            updatePageNumView(index)
            mPageSlider!!.max = (core!!.countPages() - 1) * mPageSliderRes
            mPageSlider!!.progress = index * mPageSliderRes
            if (mTopBarMode == TopBarMode.Search) {
                mSearchText!!.requestFocus()
                showKeyboard()
            }
            var anim: Animation = TranslateAnimation(0.toFloat(), 0.toFloat(), (-mTopBarSwitcher!!.height).toFloat(), 0.toFloat())
            anim.duration = 200
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {
                    mTopBarSwitcher!!.visibility = View.VISIBLE
                }

                override fun onAnimationRepeat(animation: Animation) {}
                override fun onAnimationEnd(animation: Animation) {}
            })
            mTopBarSwitcher!!.startAnimation(anim)
            anim = TranslateAnimation(0.toFloat(), 0.toFloat(), mPageSlider!!.height.toFloat(), 0.toFloat())
            anim.setDuration(200)
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {
                    mPageSlider!!.visibility = View.VISIBLE
                }

                override fun onAnimationRepeat(animation: Animation) {}
                override fun onAnimationEnd(animation: Animation) {
                    mPageNumberView!!.visibility = View.VISIBLE
                }
            })
            mPageSlider!!.startAnimation(anim)
        }
    }

    private fun hideButtons() {
        if (mButtonsVisible) {
            mButtonsVisible = false
            hideKeyboard()
            var anim: Animation = TranslateAnimation(0.toFloat(), 0.toFloat(), 0.toFloat(), (-mTopBarSwitcher!!.height).toFloat())
            anim.duration = 200
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}
                override fun onAnimationRepeat(animation: Animation) {}
                override fun onAnimationEnd(animation: Animation) {
                    mTopBarSwitcher!!.visibility = View.GONE
                }
            })
            mTopBarSwitcher!!.startAnimation(anim)
            anim = TranslateAnimation(0.toFloat(), 0.toFloat(), 0.toFloat(), mPageSlider!!.height.toFloat())
            anim.setDuration(200)
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {
                    mPageNumberView!!.visibility = View.INVISIBLE
                }

                override fun onAnimationRepeat(animation: Animation) {}
                override fun onAnimationEnd(animation: Animation) {
                    mPageSlider!!.visibility = View.INVISIBLE
                }
            })
            mPageSlider!!.startAnimation(anim)
        }
    }

    private fun searchModeOn() {
        if (mTopBarMode != TopBarMode.Search) {
            mTopBarMode = TopBarMode.Search
            //Focus on EditTextWidget
            mSearchText!!.requestFocus()
            showKeyboard()
            mTopBarSwitcher!!.displayedChild = mTopBarMode.ordinal
        }
    }

    private fun searchModeOff() {
        if (mTopBarMode == TopBarMode.Search) {
            mTopBarMode = TopBarMode.Main
            hideKeyboard()
            mTopBarSwitcher!!.displayedChild = mTopBarMode.ordinal
            SearchTaskResult.set(null)
            // Make the ReaderView act on the change to mSearchTaskResult
            // via overridden onChildSetup method.
            mDocView!!.resetupChildren()
        }
    }

    private fun updatePageNumView(index: Int) {
        if (core == null) return
        val originPageNo = pageNo
        pageNo = index + 1
        mPageNumberView?.setText(String.format(Locale.ROOT, "%d / %d", pageNo, core!!.countPages()))
        if (index + 1 != originPageNo && index != 0) {
            // updatePageNumView는 액티비티 내에서 화면 탭만 해도 pageNo가 바뀌지 않고 호출된다.
            sendPageLog()
        }
        showAnswerButton()
    }
    /** 페이지 변경 시 해설 버튼 showing */
    private fun showAnswerButton() { //
        mAnswerButton?.visibility = if (answerPageLink.keys.contains(pageNo)) View.VISIBLE else View.GONE
        mAnswerButton?.requestLayout()
    }

    private fun makeButtonsView() {
        mButtonsView = layoutInflater.inflate(R.layout.document_toolbar, null)
        mButtonsView!!.apply {
            mFilenameView = findViewById<View>(R.id.docNameText) as TextView
            mPageSlider = findViewById<View>(R.id.pageSlider) as SeekBar
            mPageNumberView = findViewById<View>(R.id.pageNumber) as TextView
            mSearchButton = findViewById<View>(R.id.searchButton) as ImageButton
            mOutlineButton = findViewById<View>(R.id.outlineButton) as ImageButton
            mTopBarSwitcher = findViewById<View>(R.id.switcher) as ViewAnimator
            mSearchBack = findViewById<View>(R.id.searchBack) as ImageButton
            mSearchFwd = findViewById<View>(R.id.searchForward) as ImageButton
            mSearchClose = findViewById<View>(R.id.searchClose) as ImageButton
            mSearchText = findViewById<View>(R.id.searchText) as EditText
            mLinkButton = findViewById<View>(R.id.linkButton) as ImageButton
            mLayoutButton = findViewById(R.id.layoutButton)
        }
        mTopBarSwitcher!!.visibility = View.INVISIBLE
        mPageNumberView!!.visibility = View.INVISIBLE
        mPageSlider!!.visibility = View.INVISIBLE
    }

    private fun showKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm?.showSoftInput(mSearchText, 0)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm?.hideSoftInputFromWindow(mSearchText!!.windowToken, 0)
    }

    private fun search(direction: Int) {
        hideKeyboard()
        val displayPage = mDocView!!.displayedViewIndex
        val r = SearchTaskResult.get()
        val searchPage = r?.pageNumber ?: -1
        mSearchTask!!.go(mSearchText!!.text.toString(), direction, displayPage, searchPage)
    }

    override fun onSearchRequested(): Boolean {
        if (mButtonsVisible && mTopBarMode == TopBarMode.Search) {
            hideButtons()
        } else {
            showButtons()
            searchModeOn()
        }
        return super.onSearchRequested()
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        if (mButtonsVisible && mTopBarMode != TopBarMode.Search) {
            hideButtons()
        } else {
            showButtons()
            searchModeOff()
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onBackPressed() {
        if (mSearchText?.hasFocus() == true) {
            searchModeOff()
            mSearchText?.clearFocus()
            return
        }
        if (mDocView?.popHistory() != true) super.onBackPressed()
    }

    /** Read Log */
    var readLogId:Int? = null
    var timer:Timer? = null

    private fun startReadLogger() {
        if (timer == null) {
            val handler = Handler(Looper.getMainLooper())

            timer = Timer(false)
            val timerTask: TimerTask = object : TimerTask() {
                override fun run() {
                    handler.post { sendLog() }
                }
            }
            timer?.scheduleAtFixedRate(timerTask, 0, 5000)
        }
    }

    private fun stopReadLogger() {
        timer?.cancel()
        timer = null
        readLogId = null
    }

    private fun sendLog() {
        Log.d(javaClass.simpleName, "readLogId=$readLogId")
        if(readLogId == null || readLogId!! < 1) {
            val request = PdfReadLog(pdfId, bookId, studentId)
            Network.sendLog(request, readLogId) {
                readLogId = it
                sendPageLog()
            }
        } else {
            Network.sendLog(null, readLogId) { /** do nothing */ }
        }
    }

    private fun sendPageLog() {
        Log.d(javaClass.simpleName, "sendPageLog LogId=$readLogId , $pageNo")

        readLogId?.let{ logId ->
            val pageNo = if (this.pageNo == 0) 1 else this.pageNo
            val request = PdfPageLog(pdfId, bookId, studentId, pageNo, logId)
            Network.sendPageLog(request)
        }
    }

    override fun onResume() {
        super.onResume()
        startReadLogger()
    }

    override fun onStop() {
        sendPageLog()
        stopReadLogger()
        uploadMemos()
        super.onStop()
    }

    public override fun onDestroy() {
        if (mDocView != null) {
            mDocView!!.applyToChildren(object : ViewMapper() {
                public override fun applyToView(view: View) {
                    val pageView = view.findViewWithTag<PageView>(PageAdapter.TAG_PAGEVIEW)
                    pageView.releaseBitmaps()
                }
            })
        }
        if (core != null) core!!.onDestroy()
        core = null
        super.onDestroy()
    }

    private fun uploadMemos() {
//        if(memos.isNotEmpty()) {
//            Network.uploadMemo(memos.values.toList()) {
//                memos.clear()
//            }
//        }
    }

    companion object {
        const val KEY_BOOK_ID = "book_id"
        const val KEY_BOOK_TITLE = "book_title"
        const val KEY_PDF_ID = "pdf_id"
        const val KEY_TARGET_PAGE_NO = "target_page_no"
        const val KEY_INCLUDE_ANSWER = "include_answer"
        const val KEY_ANSWER_PDF_ID = "answer_pdf_id"
        const val KEY_ANSWER_PDF_PATH = "answer_pdf_path"
        const val KEY_ANSWER_PAGE_LINK = "answer_page_link"

        const val KEY_STUDENT_ID = "student_id"
        const val KEY_TOKEN = "token"

        const val KEY_TEST_API_FLAG = "test_api_flag"
        const val KEY_API_FLAG = "api_flag"

        var studentId: String = ""
        var token: String = ""

        var onTestApi = false
        var onServerApi = "live"
//        var memos = mutableMapOf<String, PdfMemo>()
    }
}