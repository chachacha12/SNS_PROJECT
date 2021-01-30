package com.example.sns_project.activity

//로그인하는 창

//신규회원 가입 화면임. 여기서 사용자가 친 이메일, 비밀번호를 받아서 가입 가능한지 판별?

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sns_project.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_sign_up.emaileditText
import kotlinx.android.synthetic.main.activity_sign_up.checkbutton
import kotlinx.android.synthetic.main.activity_sign_up.passwordeditText
import kotlinx.android.synthetic.main.view_loader.*


class LoginActivity : BasicActivity() {

    private lateinit var auth: FirebaseAuth                //

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        init()
    }

    fun init() {
        checkbutton.setOnClickListener {
            login()
        }

        gotoPasswordResetButton.setOnClickListener {
            //누르면 비밀번호재설정 액티비티로 이동
            var i = Intent(this, PasswordResetActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(i)
        }

        // Initialize Firebase Auth
        auth = Firebase.auth                                //
    }


    fun login()   // 회원가입하려는 신규 사용자가 입력한 이메일과 비밀번호를 가져와서 신규 가입되는지 확인하고 가입시키는 메소드?
    {
        var email = emaileditText.text.toString()
        var password = passwordeditText.text.toString()

        if (email.length > 0 && password.length > 0) {   //이메일과 비번 칸에 뭐 하나라도 적은경우
            loaderLayout.visibility = View.VISIBLE    //로딩화면 보여줌.  (view_loader 액티비티를 보여주어서)

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    loaderLayout.visibility = View.GONE         //로딩화면끔
                    if (task.isSuccessful) {         //로그인 성공시
                        val user = auth.currentUser
                        Toast.makeText(this, "로그인에 성공하였습니다.", Toast.LENGTH_SHORT).show()

                        var i = Intent(this, MainActivity::class.java)   //메인액티비티로 이동
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)  //이동하려는 액티비티가 액티비티스택에 존재할 경우 새로 액티비티 객체 안만들고 존재하던 액티비티를 스택 위로 가져옴.
                        startActivity(i)                             //그리고 그 이전의 액티비티들은 지워줌.  여기서 메인 액티비티가 시작액티비티이기에 가능(이 함수 더 찾아보면 이유암)
                        //즉 이 로그인액티비티에서 메인액티비티로 이동했다면 무조건 메인액티비티만 스택에 남아있어서, 메인에서 뒤로가기 누르면 앱 꺼짐

                    } else {                 //로그인 실패시
                        if (task.exception != null) {     //task.exception이 null인 경우가 있을수도 있다해서..
                            Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT)
                                .show()
                        }  //if문
                    } //else문
                }
        } else {   //이메일, 비번 등에 아무것도 안친경우..
            Toast.makeText(this, "이메일 또는 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
        }

    }  //signup 함수
}