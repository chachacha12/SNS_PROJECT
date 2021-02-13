package com.example.sns_project    

import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList
//:serializable을 추가한 이유는 MainActivity에서 인텐트를 보내려는데 이 객체를 putExtra에 실어서 보내려는데,, 그러려면 이걸 해줘야해서임
data class PostInfo (val title:String, var contents: ArrayList<String>, val publisher:String, val createdAt:Date?, val id: String? = null) : Serializable { //게시글 제목, 내용(글 내용+ 첨부한 이미지들의 설명), 작성자uid(나중에 db에서 게시글 작성자를 찾기위함), 생성일, 게시글id를 인자로
                                                                                                                                                             //근데 게시글 작성한걸 db에 올릴땐(WritePostActivity) 게시글의 id값 필요없으므로 오버로딩해서 인자 안들어갈땐 null이 들어가도록 해줌. (id는 사용자가 게시글 삭제or수정때 게시글 찾는걸로 필요할뿐임)
    //자바였다면 이렇게 생성자 만들고 getter, setter함수들 따로 또 만들어 줬어야 했을거임. 근데 코틀린은 안만들어도 멤버변수 접근 가능
    //또한 코틀린은 위의 저게 생성자임.
}