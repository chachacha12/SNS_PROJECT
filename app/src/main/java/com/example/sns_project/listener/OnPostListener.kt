package com.example.sns_project.listener

interface OnPostListener {
    fun onDelete(id:String)    //선택된 게시물의 id값을 전달할거임
    fun onModify(id:String)

}