package com.pulleymath.android.pdf

import android.Manifest
import android.animation.Animator
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.Activity.RESULT_FIRST_USER
import android.app.AlertDialog
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Context.MODE_PRIVATE
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
import androidx.fragment.app.DialogFragment
import com.pulleymath.android.pdf.ReaderView.ViewMapper
import java.io.*
import java.security.MessageDigest
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread


open class PdfViewerDialog : DialogFragment() {
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

    private val CACHE_DIR by lazy {"${requireContext().filesDir}/pdfs"}
    lateinit var mBackButton: View
    lateinit var mAnswerButton: View

    private var bookTitle: String? = ""
    private var bookId:Int = 0
    private var pdfId:Int = 0
    private var answerPath:String = ""
    private var answerPageLink:MutableMap<Int,Int> = mutableMapOf()

    lateinit var rootView:View

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

    /** Called when the activity is first created.  */
    lateinit var rootLayout:FrameLayout
    lateinit var bgPdfLoading:ImageView
    lateinit var bgPdfProgress:ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val metrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(metrics)
        mDisplayDPI = metrics.densityDpi
        mAlertBuilder = AlertDialog.Builder(requireContext())

        rootView = inflater.inflate(R.layout.document_activity, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rootLayout = view.findViewById(R.id.rootLayout)
        bgPdfLoading = view.findViewById(R.id.pdfLoading)
        bgPdfProgress = view.findViewById(R.id.pdfProgress)

        createCacheDir()

        loadFile(savedInstanceState)
    }

    private fun createCacheDir() {
        File(CACHE_DIR)?.let { outputDir ->
            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }
        }
    }

    private fun loadFile(savedInstanceState: Bundle?) {
        if (core == null) {
            if (savedInstanceState != null && savedInstanceState.containsKey("FileName")) {
                mFileName = savedInstanceState.getString("FileName")
            }
        }
        if (core == null) {
            setIntentExtra()
            if (Intent.ACTION_VIEW == arguments?.getString("action")) {
                val uri = arguments?.getString("data")
                if (uri?.startsWith("file") == true) {
                    openLocalFile(Uri.parse(uri), savedInstanceState)
                } else {
                    openDownloadedFile(uri.toString(), savedInstanceState)
                }
            }
        }
    }

    private fun setIntentExtra() {
        arguments?.let { args ->
            bookTitle = args.getString(KEY_BOOK_TITLE)
            bookId = args.getInt(KEY_BOOK_ID, 0)
            pdfId = args.getInt(KEY_PDF_ID, 0)
            answerPath = args.getString(KEY_ANSWER_PDF_PATH)?:""
            generateLinkAnswerPage(args)
        }
    }

    private fun generateLinkAnswerPage(arguments:Bundle) {
        val linkListString = arguments.getString(KEY_ANSWER_PAGE_LINK)?:""

        Log.d(javaClass.simpleName, "linkString=$linkListString")
        val linkList = linkListString.split("/")
        for(link in linkList) {
            val temp = link.split(":")
            answerPageLink[temp[0].toInt()] = temp[1].toInt()
        }
    }

    private fun openLocalFile(uri: Uri, savedInstanceState: Bundle?) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST)
        }
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
                val decodedBase64 = MuPDFCrypto.decrypt(byteString, getString(R.string.publisher_key))
                val decodedByteArray = android.util.Base64.decode(decodedBase64, android.util.Base64.NO_PADDING)
                val magic = "application/pdf"
                core = openBuffer(decodedByteArray, magic)
                SearchTaskResult.set(null)

                if (core != null && core!!.needsPassword()) {
                    requestPassword(savedInstanceState)
                }
                if (core != null && core!!.countPages() == 0) {
                    core = null
                }
                requireActivity().runOnUiThread {
                    fileLoaded(savedInstanceState)
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
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
            alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.dismiss)) { dialog, which -> dismiss()
            }
            alert.setOnCancelListener { dismiss() }
            alert.show()
        } else {
            createUI(savedInstanceState)
        }
    }

    private fun requestPassword(savedInstanceState: Bundle?) {
        mPasswordView = EditText(requireContext())
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
        alert.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel)
        ) { dialog, which -> dismiss() }
        alert.show()
    }

    fun relayoutDocument() {
        val loc = core!!.layout(mDocView!!.mCurrent, mLayoutW, mLayoutH, mLayoutEM)
        mFlatOutline = null
        mDocView!!.mHistory.clear()
        mDocView!!.refresh()
        mDocView!!.displayedViewIndex = loc
    }

    fun createUI(savedInstanceState: Bundle?) {
        if (core == null) return

        // Now create the UI.
        // First create the document view
        mDocView = object : ReaderView(requireContext()) {
            override fun onMoveToChild(i: Int) {
                if (core == null) return
                mPageNumberView?.text = String.format(Locale.ROOT, "%d / %d", i + 1, core!!.countPages())
                mPageSlider!!.max = (core!!.countPages() - 1) * mPageSliderRes
                mPageSlider!!.progress = i * mPageSliderRes
                super.onMoveToChild(i)
            }

            override fun onTapMainDocArea() {
                if (!mButtonsVisible) {
                    showButtons()
                } else {
                    if (mTopBarMode == TopBarMode.Main) hideButtons()
                }
            }

            override fun onDocMotion() {
                hideButtons()
            }

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
        mDocView?.setAdapter(PageAdapter(requireContext(), core))
        mSearchTask = object : SearchTask(requireContext(), core) {
            override fun onTextFound(result: SearchTaskResult) {
                SearchTaskResult.set(result)
                // Ask the ReaderView to move to the resulting page
                mDocView?.setDisplayedViewIndex(result.pageNumber)
                // Make the ReaderView act on the change to SearchTaskResult
                // via overridden onChildSetup method.
                mDocView?.resetupChildren()
            }
        }

        // Make the buttons overlay, and store all its
        // controls in variables
        makeButtonsView()

        // Set up the page slider
        val smax = Math.max(core!!.countPages() - 1, 1)
        mPageSliderRes = (10 + smax - 1) / smax * 2

        // Set the file-name text
        val docTitle = bookTitle //core!!.title
        if (docTitle != null) mFilenameView!!.text = docTitle else mFilenameView!!.text = mFileName

        // Activate the seekbar
        mPageSlider!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                mDocView?.pushHistory()
                mDocView?.setDisplayedViewIndex((seekBar.progress + mPageSliderRes / 2) / mPageSliderRes)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int,
                                           fromUser: Boolean) {
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

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int,
                                           after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int,
                                       count: Int) {
            }
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
        /** no use - */
//        if (core!!.isReflowable) {
//            mLayoutButton!!.visibility = View.VISIBLE
//            mLayoutPopupMenu = PopupMenu(this, mLayoutButton)
//            mLayoutPopupMenu!!.menuInflater.inflate(R.menu.layout_menu, mLayoutPopupMenu!!.menu)
//            mLayoutPopupMenu!!.setOnMenuItemClickListener { item ->
//                val oldLayoutEM = mLayoutEM.toFloat()
//                val id = item.itemId
//                if (id == R.id.action_layout_6pt) mLayoutEM = 6 else if (id == R.id.action_layout_7pt) mLayoutEM = 7 else if (id == R.id.action_layout_8pt) mLayoutEM = 8 else if (id == R.id.action_layout_9pt) mLayoutEM = 9 else if (id == R.id.action_layout_10pt) mLayoutEM = 10 else if (id == R.id.action_layout_11pt) mLayoutEM = 11 else if (id == R.id.action_layout_12pt) mLayoutEM = 12 else if (id == R.id.action_layout_13pt) mLayoutEM = 13 else if (id == R.id.action_layout_14pt) mLayoutEM = 14 else if (id == R.id.action_layout_15pt) mLayoutEM = 15 else if (id == R.id.action_layout_16pt) mLayoutEM = 16
//                if (oldLayoutEM != mLayoutEM.toFloat()) relayoutDocument()
//                true
//            }
//            mLayoutButton!!.setOnClickListener { mLayoutPopupMenu!!.show() }
//        }

        /** bugs */
//        if (core!!.hasOutline()) {
//            mOutlineButton!!.setOnClickListener {
//                if (mFlatOutline == null) mFlatOutline = core!!.outline
//                if (mFlatOutline != null) {
//                    val intent = Intent(this@PdfViewerActivity, OutlineActivity::class.java)
//                    val bundle = Bundle()
//                    bundle.putInt("POSITION", mDocView?.displayedViewIndex ?: -1)
//                    bundle.putSerializable("OUTLINE", mFlatOutline)
//                    intent.putExtras(bundle)
//                    startActivityForResult(intent, OUTLINE_REQUEST)
//                }
//            }
//        } else {
//            mOutlineButton!!.visibility = View.GONE
//        }

        /** just hide button */
        mOutlineButton!!.visibility = View.GONE
        mLinkButton!!.visibility = View.GONE

        // Reenstate last state if it was recorded
        val prefs = requireActivity().getPreferences(MODE_PRIVATE)
        mDocView?.setDisplayedViewIndex(prefs.getInt("page$mFileKey", 0))
        if (savedInstanceState == null || !savedInstanceState.getBoolean("ButtonsHidden", false)) showButtons()
        if (savedInstanceState != null && savedInstanceState.getBoolean("SearchMode", false)) searchModeOn()
        // Stick the document view and the buttons overlay into a parent view
//        rootLayout.removeAllViews() // remove progressBar

//        rootLayout.setBackgroundColor(Color.DKGRAY)
        /** fade in added view */
        mDocView?.alpha = 0f
//        mButtonsView?.alpha = 0f

        rootLayout.addView(mDocView)
        rootLayout.addView(mButtonsView)

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

        /** should call here */
        setButtons()

        /** loading answer pages */
        loadAnswerPages()

        // prevent screen shot
//        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
    }

    private fun setButtons() {
        mBackButton = rootView.findViewById(R.id.backBtn)
        mBackButton.setOnClickListener { dismiss() }
        mAnswerButton = rootView.findViewById(R.id.answerBtn)
        mAnswerButton.setOnClickListener {

        }
        mAnswerButton.visibility = View.GONE
    }

    private fun loadAnswerPages() {

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (mFileKey != null && mDocView != null) {
            if (mFileName != null) outState.putString("FileName", mFileName)

            // Store current page in the prefs against the file name,
            // so that we can pick it up each time the file is loaded
            // Other info is needed only for screen-orientation change,
            // so it can go in the bundle
            val prefs = requireActivity().getPreferences(MODE_PRIVATE)
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
            val prefs = requireActivity().getPreferences(MODE_PRIVATE)
            val edit = prefs.edit()
            edit.putInt("page$mFileKey", mDocView!!.displayedViewIndex)
            edit.apply()
        }
    }

    public override fun onDestroy() {
        if (mDocView != null) {
            mDocView!!.applyToChildren(object : ViewMapper() {
                public override fun applyToView(view: View) {
                    (view as PageView).releaseBitmaps()
                }
            })
        }
        if (core != null) core!!.onDestroy()
        core = null
        super.onDestroy()
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
                    mTopBarSwitcher!!.visibility = View.INVISIBLE
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
        mPageNumberView?.setText(String.format(Locale.ROOT, "%d / %d", index + 1, core!!.countPages()))
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
        val imm = requireContext().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm?.showSoftInput(mSearchText, 0)
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm?.hideSoftInputFromWindow(mSearchText!!.windowToken, 0)
    }

    private fun search(direction: Int) {
        hideKeyboard()
        val displayPage = mDocView!!.displayedViewIndex
        val r = SearchTaskResult.get()
        val searchPage = r?.pageNumber ?: -1
        mSearchTask!!.go(mSearchText!!.text.toString(), direction, displayPage, searchPage)
    }

    fun onSearchRequested() {
        if (mButtonsVisible && mTopBarMode == TopBarMode.Search) {
            hideButtons()
        } else {
            showButtons()
            searchModeOn()
        }
    }

    companion object {
        val KEY_BOOK_TITLE = "book_title"
        val KEY_PDF_ID = "pdf_id"
        val KEY_BOOK_ID = "book_id"
        val KEY_ANSWER_PDF_PATH = "answer_pdf_path"
        val KEY_ANSWER_PAGE_LINK = "answer_page_link"
    }
}