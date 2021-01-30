package com.example.sns_project.activity    //부모클래스(액티비티)임
// 모든 액티비티에 공통으로 필요한 코드를 이 클래스에 넣고 다른 액티비티에서 이 클래스를 상속받을거임
//다른 액티비티들 만들고 클래스이름옆 : 에다가 BasicActivity쓰면 상속됨

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


open class BasicActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED //화면의 가로세로 관련 문제 해결을 위해..?

    }

}