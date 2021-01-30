package com.example.sns_project     //회원 한명의 4가지 정보들을 담고있는 객체를 하나씩 생성하는 클래스

import kotlinx.android.synthetic.main.activity_member_init.*

data class MemberInfo(val name:String, val phoneNumber:String, val birthDay:String, val address:String, val photoUrl:String? = null){

    //자바였다면 이렇게 생성자 만들고 getter, setter함수들 따로 또 만들어 줬어야 했을거임. 근데 코틀린은 안만들어도 멤버변수 접근 가능
}