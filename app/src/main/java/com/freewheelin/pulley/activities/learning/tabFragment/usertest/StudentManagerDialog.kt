package com.freewheelin.pulley.activities.learning.tabFragment.usertest

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.widget.RadioButton
import com.freewheelin.pulley.R
import kotlinx.android.synthetic.main.dialog_student_manager.*

class StudentManagerDialog(val activity: Activity, val successCB:()->Unit, val failCB:()->Unit): Dialog(activity) {

    data class Student(val studentID:String, val email:String, val name :String)

    var studentList = mutableListOf<Student>()

    init {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.dialog_student_manager)
        initUI()
    }

    private fun initUI() {
        studentList.clear()
        setCancelable(true)
        btnClose.setOnClickListener { close() }
        loadStudent()
        setList()
    }

    private fun close() {
        dismiss()
        failCB()
    }

    private fun loadStudent() {
        studentList.add( Student("I13969","dhko@mathflat.com","테스터"))
        studentList.add( Student("I15168","pyojungbin12@naver.com","표정빈"))
        studentList.add( Student("I16203","o398705@naver.com","오유정"))
        studentList.add( Student("I16685","yuja__05@naver.com","서유진"))
        studentList.add( Student("I250233", "ramelusin0430@gmail.com", "정유신"))
        studentList.add( Student("I250350", "elliehi@naver.com", "권하은"))
        studentList.add( Student("I250353", "yujun0116@naver.com", "조유준"))
    }

    private fun setList() {
        studentContainer.removeAllViews()

        for(student in studentList) {
            val radio  = LayoutInflater.from(activity).inflate(R.layout.item_student_radio, studentContainer, false) as RadioButton
            radio.text = student.name
            radio.tag = student.studentID
            radio.setOnClickListener {
                val intent = Intent(context, StudentManagerActivity::class.java)
                intent.putExtra("studentID", student.studentID)
                intent.putExtra("studentName", student.name)
                context.startActivity(intent)
            }
            studentContainer.addView(radio)
        }
    }
}