package com.example.sns_project     //회원 한명의 4가지 정보들을 담고있는 객체를 하나씩 생성하는 클래스

import java.util.*
import kotlin.collections.ArrayList

data class PostInfo(val title:String, var contents: ArrayList<String>, val publisher:String, val createdAt:Date?, val id: String? = null){ //게시글 제목, 내용(글 내용+ 첨부한 이미지들의 설명), 작성자uid(나중에 db에서 게시글 작성자를 찾기위함), 생성일, 게시글id를 인자로
                                                                                                                                            //근데 게시글 작성한걸 db에 올릴땐(WritePostActivity) 게시글의 id값 필요없으므로 오버로딩해서 인자 안들어가면 null로 해줌. (id는 사용자가 게시글 삭제or수정때 게시글 찾는걸로 필요할뿐임)
    //자바였다면 이렇게 생성자 만들고 getter, setter함수들 따로 또 만들어 줬어야 했을거임. 근데 코틀린은 안만들어도 멤버변수 접근 가능
    //또한 코틀린은 위의 저게 생성자임.
}