package com.freewheelin.pulley.activities.learning.tabFragment.usertest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.learning.tabFragment.mockExam.StudentMockFragment
import kotlinx.android.synthetic.main.activity_student_manager.*

class StudentManagerActivity : AppCompatActivity() {

    var studentID: String? = ""
    var studentName: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_student_manager)

        studentID = intent.getStringExtra("studentID")
        studentName = intent.getStringExtra("studentName")

        if(studentID?.isNotEmpty() == true) {
            initUI()
        } else {
            textEmpty.visibility = View.VISIBLE
        }
    }

    private fun initUI() {

        textName.text = "$studentName"

        btnClose.setOnClickListener {
            finish()
        }

        val fragment = StudentMockFragment()
        fragment.arguments = Bundle().apply {
            putString("studentID", studentID)
            putString("studentName", studentName)
        }

        val trx = supportFragmentManager.beginTransaction()
        trx.add(R.id.container, fragment)
        trx.commit()
    }
}