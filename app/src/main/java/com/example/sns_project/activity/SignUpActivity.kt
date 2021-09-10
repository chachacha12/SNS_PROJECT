package com.example.sns_project.activity

//신규회원 가입 화면임. 여기서 사용자가 친 이메일, 비밀번호를 받아서 가입 가능한지 판별?

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.sns_project.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.view_loader.*



class SignUpActivity : BasicActivity() {

    private lateinit var auth: FirebaseAuth                //

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        init()
    }

    fun init()
    {
        checkbutton.setOnClickListener {
            signup()
        }

        gotoLoginButton.setOnClickListener {         //로그인버튼 누르면 로그인액티비티로 이동
           var i = Intent(this, LoginActivity::class.java)
            startActivity(i)
        }

        // Initialize Firebase Auth
        auth = Firebase.auth                                //
    }

   override fun onBackPressed() {     // 이 액티비티에서 뒤로가기버튼을 눌럿을때 처리 - 이거안해주면 메인에서 로그아웃해서 나왓는데 뒤로가기 눌르면 다시 메인창으로 가는 오류 생겨서. 앱 그냥 꺼지게 하기
        super.onBackPressed()
       moveTaskToBack(true) // 태스크를 백그라운드로 이동
       android.os.Process.killProcess(android.os.Process.myPid()) // 앱 프로세스 종료
       System.exit(1)
    }


    fun signup()   // 회원가입하려는 신규 사용자가 입력한 이메일과 비밀번호를 가져와서 신규 가입되는지 확인하고 가입시키는 메소드?
    {
        var email = emaileditText.text.toString()
        var password = passwordeditText.text.toString()
        var passwordCheck = passwordCheckeditText.text.toString()

        if(email.length > 0 && password.length >0 && passwordCheck.length > 0){
            if(password == passwordCheck) {   //비밀번호와 비밀번호확인 editText가 서로 같을 경우에만 회원가입 작동
                loaderLayout.visibility = View.VISIBLE    //로딩화면 보여줌.
                auth.createUserWithEmailAndPassword(          //신규가입하려는 사용자가 입력한 이메일과 비밀번호를 여기 함수에 넣어줌
                    email,
                    password
                )
                    .addOnCompleteListener(this) { task ->
                        loaderLayout.visibility = View.GONE
                        if (task.isSuccessful) {       //로그인 성공햇을때 화면상태
                            val user = auth.currentUser
                            Toast.makeText(this, "회원가입에 성공하였습니다.", Toast.LENGTH_SHORT).show()

                            var i = Intent(this, MainActivity::class.java)    //회원가입 성공하면 바로 메인액티비티로 이동
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(i)
                        }
                        else {                     //로그인 실패했을때  (비밀번호가 6글자 이하인 경우 등등은 구글서버에서 에러를 보내줌..그게 task.exception인듯)
                            if(task.exception != null){     //task.exception이 null인 경우가 있을수도 있다해서..
                                Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT).show()
                            }
                        }   //else문
                    }                   //Lisener
            }else{         //비밀번호와 비밀번호확인이 서로 틀렸을 경우
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
            }
        }else{   //이메일, 비번 등에 아무것도 안친경우..
            Toast.makeText(this, "이메일 또는 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
        }
    }  //signup 함수






}