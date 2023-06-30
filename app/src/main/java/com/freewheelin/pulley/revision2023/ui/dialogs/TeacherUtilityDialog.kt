package com.freewheelin.pulley.revision2023.ui.dialogs

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.DialogSnackTestRecommendSettingBinding
import com.freewheelin.pulley.databinding.DialogTeacherUtilityBinding
import com.freewheelin.pulley.revision2023.ui.fragment.*
import com.freewheelin.pulley.revision2023.viewmodel.RecommendSettingViewModel
import com.freewheelin.pulley.revision2023.viewmodel.TeacherUtilityViewModel
import com.freewheelin.pulley.legacy.utils.DisplayUtils
import com.freewheelin.pulley.legacy.utils.toPx

class TeacherUtilityDialog(): DialogFragment() {

    private val viewModel: TeacherUtilityViewModel by viewModels()

    private val binding: DialogTeacherUtilityBinding by lazy {
        DataBindingUtil.inflate(layoutInflater.cloneInContext(requireContext()), R.layout.dialog_teacher_utility, null, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            vm = viewModel
            lifecycleOwner = viewLifecycleOwner
            setScreen()

            viewModel.onExitClickCallback = {
                dismiss()
            }
        }
    }

    private fun setScreen() {
        val topBottomMargin = resources.getDimension(R.dimen.dp32) * 2
        val lp = binding.rootView.layoutParams
        lp.height = DisplayUtils.getScreenHeight(requireContext()) - topBottomMargin.toInt()
        lp.width = 640.toPx()
        binding.rootView.layoutParams = lp
    }

//    lateinit var studentAdapter: RecyclerView.Adapter<StudentListHolder>
//    lateinit var searchAdapter: RecyclerView.Adapter<SearchListHolder>

//    init {

//        initData()
//        initAdapter()
//        initUI()
//    }
//
//    private fun initData() {
//        val studentsJsonStr = sharedPreference.getString(sfKey, null)
//        studentsJsonStr?.let { it ->
//            val listType = object : TypeToken<List<Student>>() {}.type
//            gson.fromJson<List<Student>>(it, listType).let { list ->
//                studentList.addAll(list)
//            }
//        }
//    }
//
//    private fun initAdapter() {
//        studentAdapter = StudentListAdapter()
//        searchAdapter = SearchListAdapter()
//    }
//
//    private fun initUI() {
//        setCancelable(true)
//        binding.btnClose.setOnClickListener { close() }
//        setStudentListView()
//        setSearchListView()
//        setSearchView()
//    }
//
//    private fun close() {
//        dismiss()
////        failCB()
//    }
//
//    private fun setStudentListView() {
//        binding.studentListRv.adapter = studentAdapter
//        binding.studentListRv.layoutManager = LinearLayoutManager(context)
//        studentAdapter.notifyDataSetChanged()
//    }
//
//    private fun setSearchListView() {
//        binding.searchListRv.adapter = searchAdapter
//        binding.searchListRv.layoutManager = LinearLayoutManager(context)
//        searchAdapter.notifyDataSetChanged()
//    }
//
//    private fun setSearchView() {
//        with(binding) {
//            searchView.setOnQueryTextListener(object : DelayedOnQueryTextListener() {
//                override fun onDelayerQueryTextChange(query: String?) {
//                    query?.let {
//                        getUserListByUserName(query, {
//                            searchList.clear()
//                            searchList.addAll(it)
//                            searchAdapter.notifyDataSetChanged()
//                        }, {})
//                    }
//                }
//            })
//        }
//    }
//
//    private fun containsTeacherEmail(studentGroup: StudentGroup, email: String): Boolean {
//        studentGroup.teachers.forEach { teacher ->
//            if (teacher.email == email) {
//                return@containsTeacherEmail true
//            }
//        }
//        return false
//    }
//
//    private fun addManagedStudent(student: Student) {
//        if (!studentList.contains(student)) {
//            studentList.add(student)
//            saveStudentListInSharedPreferences()
//        }
//        studentAdapter.notifyDataSetChanged()
//    }
//
//    private fun removeManagedStudent(student: Student) {
//        if (studentList.contains(student)) {
//            studentList.remove(student)
//            saveStudentListInSharedPreferences()
//        }
//        studentAdapter.notifyDataSetChanged()
//    }
//
//    private fun saveStudentListInSharedPreferences() {
//        val listType = object : TypeToken<List<Student>>() {}.type
//        var jsonStr = gson.toJsonTree(studentList, listType)
//        sharedPreference.edit().putString(sfKey, jsonStr.toString()).apply()
//    }
//
//    private fun getUserListByUserName(name: String,
//                                      successCB:(List<Student>) -> Unit,
//                                      failCB: (Response<Template<List<Student>>>) -> Unit) {
//        API_V2.getUserListByUserName(name).enqueue(object: Callback<Template<List<Student>>> {
//            override fun onFailure(call: Call<Template<List<Student>>>, t: Throwable) {
//                responseFailed(context, t)
//            }
//
//            override fun onResponse(
//                call: Call<Template<List<Student>>>,
//                response: Response<Template<List<Student>>>
//            ) {
//                if(response.isSuccessful) {
//                    val body = response.body()?.data
//                    if (body != null) {
//                        successCB(body)
//                    }
//                } else {
//                    failCB(response)
//                }
//            }
//        })
//    }
//
//    private fun phoneNumberBlurProcessing(phoneNumber: String): String {
//        if (phoneNumber.length == 11) {
//            val firstPhoneNumber = phoneNumber.substring(0, 3)
//            val thirdPhoneNumber = phoneNumber.substring(7, phoneNumber.length)
//            return "${firstPhoneNumber}-****-${thirdPhoneNumber}"
//        }
//        return "WrongNumber"
//    }
//
//    inner class StudentListAdapter: RecyclerView.Adapter<StudentListHolder>() {
//        override fun onCreateViewHolder(
//            parent: ViewGroup,
//            viewType: Int
//        ): StudentListHolder {
//            return StudentListHolder(LayoutInflater.from(context).inflate(R.layout.item_student_manager_search, parent, false))
//        }
//
//        override fun onBindViewHolder(holder: StudentListHolder, position: Int) {
//            holder.set(studentList[position])
//        }
//
//        override fun getItemCount(): Int {
//            return studentList.size
//        }
//    }
//
//    inner class StudentListHolder(val view: View): RecyclerView.ViewHolder(view) {
//        var studentNameTv = view.findViewById<TextView>(R.id.studentName)
//        var studentPhoneNumberTv = view.findViewById<TextView>(R.id.studentPhoneNumber)
//        var btnReport = view.findViewById<SecondaryButton>(R.id.btnReport)
//        var btnAnalysis = view.findViewById<SecondaryButton>(R.id.btnAnalysis)
//        var btnRemove = view.findViewById<SecondaryButton>(R.id.btnRemove)
//
//        fun set(student: Student) {
//            studentNameTv.text = "${student.name} (${student.studentID})"
//            studentPhoneNumberTv.text = phoneNumberBlurProcessing(student.cellphone)
//            btnReport.setOnClickListener {
//                val intent = Intent(context, StudentManagerActivity::class.java)
//                intent.putExtra("studentID", student.studentID)
//                intent.putExtra("studentName", student.name)
//                context.startActivity(intent)
//            }
//            btnAnalysis.setOnClickListener {
//                val intent = Intent(context, UserAnalysisActivity::class.java)
//                intent.putExtra(KEY_STUDENT_ID, student.studentID)
//                intent.putExtra(KEY_STUDENT_NAME, student.name)
//                context.startActivity(intent)
//            }
//            btnRemove.setOnClickListener {
//                removeManagedStudent(student)
//            }
//        }
//    }
//
//    inner class SearchListAdapter: RecyclerView.Adapter<SearchListHolder> () {
//        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchListHolder {
//            return SearchListHolder(LayoutInflater.from(context).inflate(R.layout.item_student_manager, parent, false))
//        }
//
//        override fun onBindViewHolder(holder: SearchListHolder, position: Int) {
//            holder.set(searchList[position])
//        }
//
//        override fun getItemCount(): Int {
//            return searchList.size
//        }
//    }
//
//    inner class SearchListHolder(val view: View): RecyclerView.ViewHolder(view) {
//        var studentNameTv = view.findViewById<TextView>(R.id.studentName)
//        var btnAdd = view.findViewById<SecondaryButton>(R.id.btnAdd)
//
//        fun set(student: Student) {
//            val cellPhoneBlured = phoneNumberBlurProcessing(student.cellphone)
//            studentNameTv.text = "${student.name}\n${cellPhoneBlured}"
//            btnAdd.setOnClickListener {
//                addManagedStudent(student)
//            }
//        }
//    }
}


//abstract class DelayedOnQueryTextListener : SearchView.OnQueryTextListener {
//    private val handler: Handler = Handler()
//    private var runnable: Runnable? = null
//    override fun onQueryTextSubmit(s: String): Boolean {
//        return false
//    }
//
//    override fun onQueryTextChange(s: String): Boolean {
//        runnable?.let {
//            handler.removeCallbacks(it)
//        }
//        runnable = Runnable { onDelayerQueryTextChange(s) }
//        handler.postDelayed(runnable!!, 400)
//        return true
//    }
//
//    abstract fun onDelayerQueryTextChange(query: String?)
//}