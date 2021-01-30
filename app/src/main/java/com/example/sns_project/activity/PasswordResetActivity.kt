package com.example.sns_project.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sns_project.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_password_reset.*
import kotlinx.android.synthetic.main.activity_sign_up.emaileditText
import kotlinx.android.synthetic.main.view_loader.*


class PasswordResetActivity : BasicActivity() {

    private lateinit var auth: FirebaseAuth                //

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_reset)
        init()
    }

    fun init() {
        sendbutton.setOnClickListener {
            send()
        }

        // Initialize Firebase Auth
        auth = Firebase.auth                                //
    }


    fun send() {
        var email = emaileditText.text.toString()


        if (email.length > 0) {   //이메일 칸에 뭐 하나라도 적은경우
            loaderLayout.visibility = View.VISIBLE    //로딩화면 보여줌.
            Firebase.auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    loaderLayout.visibility = View.GONE
                    if (task.isSuccessful) {
                        Toast.makeText(this, "이메일을 보냈습니다.", Toast.LENGTH_SHORT).show()
                    }
                }

        } else {   //이메일, 비번 등에 아무것도 안친경우..
            Toast.makeText(this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
        }

    }  //send 함수
}