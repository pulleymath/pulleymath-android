package com.freewheelin.pulley.activities.learning.tabFragment.usertest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ActivityStudentManagerBinding

class StudentManagerActivity : AppCompatActivity() {

    var studentID: String? = ""
    var studentName: String? = ""
    private val binding: ActivityStudentManagerBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_student_manager, null, false)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        studentID = intent.getStringExtra("studentID")
        studentName = intent.getStringExtra("studentName")

        if(studentID?.isNotEmpty() == true) {
            initUI()
        } else {
            binding.textEmpty.visibility = View.VISIBLE
        }
    }

    private fun initUI() {

        binding.textName.text = "$studentName"

        binding.btnClose.setOnClickListener {
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