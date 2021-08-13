package com.freewheelin.pulley.activities.learning.tabFragment.usertest

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SearchView
import android.widget.TextView
import androidx.collection.arraySetOf
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.learning.tabFragment.usertest.analysis.UserAnalysisActivity
import com.freewheelin.pulley.activities.learning.tabFragment.usertest.analysis.UserAnalysisActivity.Companion.KEY_STUDENT_ID
import com.freewheelin.pulley.activities.learning.tabFragment.usertest.analysis.UserAnalysisActivity.Companion.KEY_STUDENT_NAME
import com.freewheelin.pulley.databinding.DialogStudentManagerBinding
import com.freewheelin.pulley.views.Buttons.SecondaryButton
import kotlinx.android.synthetic.main.dialog_marketing.*
import kotlinx.android.synthetic.main.dialog_student_manager.*
import kotlinx.android.synthetic.main.item_student_radio.view.*
import kotlinx.android.synthetic.main.item_teacher.view.*

class StudentManagerDialog(val activity: Activity, val studentManager: StudentManagerResponse, val successCB:()->Unit, val failCB:()->Unit): Dialog(activity) {

    data class Student(val studentID:String, val name :String)
    data class Teacher(val email:String, val name :String)
    data class StudentGroup(val teachers: List<Teacher>, val students:List<Student>)
    data class StudentManagerResponse(val admins: List<String>, val group: List<StudentGroup>)

    private val studentList = arrayListOf<Student>()
    private val teacherList = arraySetOf<Teacher>()

    lateinit var teacherAdapter: RecyclerView.Adapter<TeacherListHolder>
    lateinit var studentAdapter: RecyclerView.Adapter<StudentListHolder>

    private val binding: DialogStudentManagerBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(activity), R.layout.dialog_student_manager, null, false)
    }

    init {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(binding.root)
        initData()
        initAdapter()
        initUI()
    }

    private fun resetData() {
        studentList.clear()
        teacherList.clear()
    }

    private fun initData() {
        studentManager.group.forEach { studentGroup ->
            teacherList.addAll(studentGroup.teachers)
        }
    }

    private fun initAdapter() {
        teacherAdapter = TeacherListAdapter()
        studentAdapter = StudentListAdapter()
    }
    private fun initUI() {
        setCancelable(true)
        btnClose.setOnClickListener { close() }
        btnBack.setOnClickListener {
            resetData()
            initData()
            setTeacherList()
        }
        setTeacherList()
    }

    private fun close() {
        dismiss()
        failCB()
    }

    private fun setStudentListView(teacherEmail: String) {
        with(binding) {
            studentManagerTitleTv.text = "학생을 선택해주세요."
            btnBack.visibility = View.VISIBLE
            teacherListRv.visibility = View.GONE
            studentListRv.visibility = View.VISIBLE
        }

        setStudentListByTeacherEmail(teacherEmail)

        setSearchView(teacherEmail)

        studentListRv.adapter = studentAdapter
        studentListRv.layoutManager = LinearLayoutManager(context)
        studentAdapter.notifyDataSetChanged()
    }

    // searchView가 Teacher를 검색해주는것은 고려하지 않았음
    private fun setSearchView(teacherEmail: String) {
        with(binding) {
            searchView.visibility = View.VISIBLE
            searchView.setOnQueryTextListener(null)
            searchView.setOnQueryTextListener(object : DelayedOnQueryTextListener() {
                override fun onDelayerQueryTextChange(newText: String?) {
                    Log.d("dnjs", "onDelayer1")
                    newText?.let {
                        Log.d("dnjs", "onDelayer2 newText is Alive")
                        setStudentListByTeacherEmail(teacherEmail, newText)
                        studentAdapter.notifyDataSetChanged()
                    }
                }
            })
        }
    }

    private fun setTeacherList() {

        with(binding) {
            studentManagerTitleTv.text = "선생님을 선택해주세요."
            btnBack.visibility = View.GONE
            searchView.visibility = View.GONE

            teacherListRv.visibility = View.VISIBLE
            studentListRv.visibility = View.GONE
            teacherListRv.adapter = teacherAdapter
            teacherListRv.layoutManager = LinearLayoutManager(context)
            teacherAdapter.notifyDataSetChanged()
        }
    }

    private fun containsTeacherEmail(studentGroup: StudentGroup, email: String): Boolean {
        studentGroup.teachers.forEach { teacher ->
            if (teacher.email == email) {
                return@containsTeacherEmail true
            }
        }
        return false
    }

    private fun setStudentListByTeacherEmail(teacherEmail: String, filterString: String = "") {
        studentManager.group.forEach { studentGroup ->
            val isContain = containsTeacherEmail(studentGroup, teacherEmail)
            if (isContain) {
                studentList.clear()
                val students = studentGroup.students.filter { it.name.contains(filterString) }
                studentList.addAll(students)
                studentList.sortBy { it.name }
            }
        }
    }

    inner class TeacherListAdapter: RecyclerView.Adapter<TeacherListHolder> () {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeacherListHolder {
            return TeacherListHolder(LayoutInflater.from(context).inflate(R.layout.item_teacher, parent, false))
        }

        override fun onBindViewHolder(holder: TeacherListHolder, position: Int) {
            val teacherList = teacherList.toArray()
            holder.set(teacherList[position] as Teacher)
        }

        override fun getItemCount(): Int {
            Log.d("dnjs", "getItemCount size ${teacherList.size}")
            return teacherList.size
        }
    }

    inner class TeacherListHolder(val view: View): RecyclerView.ViewHolder(view) {
        var teacherNameTv = view.findViewById<SecondaryButton>(R.id.btnTeacher)

        fun set(teacher: Teacher) {
            val teacherStr = "${teacher.name} 선생님 (${teacher.email})"
            teacherNameTv.text = teacherStr
            teacherNameTv.setOnClickListener {
                val teacherEmail = teacherStr.substringAfter("(").substringBefore(")")
                setStudentListView(teacherEmail)
            }
        }
    }
    inner class StudentListAdapter: RecyclerView.Adapter<StudentListHolder>() {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): StudentListHolder {
            return StudentListHolder(LayoutInflater.from(context).inflate(R.layout.item_student_radio, parent, false))
        }

        override fun onBindViewHolder(holder: StudentListHolder, position: Int) {
            holder.set(studentList[position])
        }

        override fun getItemCount(): Int {
            return studentList.size
        }
    }

    inner class StudentListHolder(val view: View): RecyclerView.ViewHolder(view) {
        var studentNameTv = view.findViewById<TextView>(R.id.studentName)
        var btnReport = view.findViewById<SecondaryButton>(R.id.btnReport)
        var btnAnalysis = view.findViewById<SecondaryButton>(R.id.btnAnalysis)

        fun set(student: Student) {
            studentNameTv.text = "${student.name}\n(${student.studentID})"
            btnReport.setOnClickListener {
                val intent = Intent(context, StudentManagerActivity::class.java)
                intent.putExtra("studentID", student.studentID)
                intent.putExtra("studentName", student.name)
                context.startActivity(intent)
            }
            btnAnalysis.setOnClickListener {
                val intent = Intent(context, UserAnalysisActivity::class.java)
                intent.putExtra(KEY_STUDENT_ID, student.studentID)
                intent.putExtra(KEY_STUDENT_NAME, student.name)
                context.startActivity(intent)
            }
        }
    }
}


abstract class DelayedOnQueryTextListener : SearchView.OnQueryTextListener {
    private val handler: Handler = Handler()
    private var runnable: Runnable? = null
    override fun onQueryTextSubmit(s: String): Boolean {
        return false
    }

    override fun onQueryTextChange(s: String): Boolean {
        runnable?.let {
            handler.removeCallbacks(it)
        }
        runnable = Runnable { onDelayerQueryTextChange(s) }
        handler.postDelayed(runnable!!, 400)
        return true
    }

    abstract fun onDelayerQueryTextChange(query: String?)
}