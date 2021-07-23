package com.freewheelin.pulley.activities.learning.tabFragment.usertest

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.collection.arraySetOf
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.learning.tabFragment.usertest.analysis.UserAnalysisActivity
import com.freewheelin.pulley.activities.learning.tabFragment.usertest.analysis.UserAnalysisActivity.Companion.KEY_STUDENT_ID
import com.freewheelin.pulley.activities.learning.tabFragment.usertest.analysis.UserAnalysisActivity.Companion.KEY_STUDENT_NAME
import com.freewheelin.pulley.databinding.DialogStudentManagerBinding
import kotlinx.android.synthetic.main.dialog_student_manager.*
import kotlinx.android.synthetic.main.item_student_radio.view.*
import kotlinx.android.synthetic.main.item_student_radio.view.studentName
import kotlinx.android.synthetic.main.item_teacher.view.*

class StudentManagerDialog(val activity: Activity, val studentManager: StudentManagerResponse, val successCB:()->Unit, val failCB:()->Unit): Dialog(activity) {

    data class Student(val studentID:String, val name :String)
    data class Teacher(val email:String, val name :String)
    data class StudentGroup(val teachers: List<Teacher>, val students:List<Student>)
    data class StudentManagerResponse(val admins: List<String>, val group: List<StudentGroup>)

    private val studentList = arrayListOf<Student>()
    private val teacherList = arraySetOf<Teacher>()

    val sharedPreference: SharedPreferences by lazy { context.getSharedPreferences(TAG, Context.MODE_PRIVATE) }
    private var recentSelectedTeacher: String? = null
    private val TAG = this.javaClass.name
    private val sfKey = "RecentSelectedTeacher"

    private val binding: DialogStudentManagerBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(activity), R.layout.dialog_student_manager, null, false)
    }

    init {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(binding.root)
        initData()
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

        recentSelectedTeacher = sharedPreference.getString(sfKey, null)
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
            itemContainer.removeAllViews()
            studentManagerTitleTv.text = "학생을 선택해주세요."
            btnBack.visibility = View.VISIBLE
        }

        setStudentList(teacherEmail)

        for(student in studentList) {
            val item  = LayoutInflater.from(activity).inflate(R.layout.item_student_radio, itemContainer, false) as LinearLayout
            item.studentName.text = "${student.name}\n(${student.studentID})"

            item.btnReport.setOnClickListener {
                val intent = Intent(context, StudentManagerActivity::class.java)
                intent.putExtra("studentID", student.studentID)
                intent.putExtra("studentName", student.name)
                context.startActivity(intent)
            }
            item.btnAnalysis.setOnClickListener {
                val intent = Intent(context, UserAnalysisActivity::class.java)
                intent.putExtra(KEY_STUDENT_ID, student.studentID)
                intent.putExtra(KEY_STUDENT_NAME, student.name)
                context.startActivity(intent)
            }
            itemContainer.addView(item)
        }

        setStudentListEmptyView ()
    }

    private fun setTeacherList() {
        with(binding) {
            itemContainer.removeAllViews()
            studentManagerTitleTv.text = "선생님을 선택해주세요."
            btnBack.visibility = View.GONE

            recentSelectedTeacher?.let { teacherStr ->
                val item  = LayoutInflater.from(activity).inflate(R.layout.item_teacher, itemContainer, false) as LinearLayout
                item.btnTeacher.text = teacherStr
                item.btnTeacher.setOnClickListener {
                    val teacherEmail = teacherStr.substringAfter("(").substringBefore(")")
                    setStudentListView(teacherEmail)
                }
                itemContainer.addView(item)

                // Divider 추가
                itemContainer.addView(LayoutInflater.from(activity).inflate(R.layout.item_divider, itemContainer, false) as LinearLayout)
            }

            teacherList.forEach {
                val item  = LayoutInflater.from(activity).inflate(R.layout.item_teacher, itemContainer, false) as LinearLayout
                val teacherStr = "${it.name} 선생님 (${it.email})"
                item.btnTeacher.text = teacherStr

                item.btnTeacher.setOnClickListener {
                    sharedPreference.edit().putString(sfKey, teacherStr).apply()
                    val teacherEmail = teacherStr.substringAfter("(").substringBefore(")")
                    setStudentListView(teacherEmail)
                }
                itemContainer.addView(item)
            }
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

    private fun setStudentList(teacherEmail: String) {
        studentManager.group.forEach { studentGroup ->
            val isContain = containsTeacherEmail(studentGroup, teacherEmail)
            if (isContain) {
                studentList.addAll(studentGroup.students)
            }
        }
    }

    private fun setStudentListEmptyView () {
        if (studentList.size == 0) {
            // TODO 관리하고있는 학생이 없을때 표시할 뷰
        }
    }
}