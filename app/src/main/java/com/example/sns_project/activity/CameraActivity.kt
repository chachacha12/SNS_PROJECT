/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.sns_project.activity

//이 액티비티엔 camera2basic 프래그먼트가 달려있음. 그래서 이 액티비티 실행하면 카메라로 찍기 가능


import android.app.Activity
import android.content.Intent
import android.media.ImageReader
import android.os.Bundle
import android.util.Log
//import android.support.v7.app.AppCompatActivity
import androidx.appcompat.app.AppCompatActivity
import com.example.sns_project.R
import com.example.sns_project.fragment.Camera2BasicFragment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class CameraActivity :BasicActivity(){

    private lateinit var camera2BasicFragment: Camera2BasicFragment  //카메라2프래그먼트객체를 전역으로 둠


    /**  //이건 카메라2프래그먼트에 있던 코드를 여기로 가져온거임.
     * This a callback object for the [ImageReader]. "onImageAvailable" will be called when a
     * still image is ready to be saved.
     */
    private val mOnImageAvailableListener =       //사진 찍을때마다 작동하는 리스너임. 사진찍으면 내 폰의 특정경로에 저장시키고, memberinit액티비티로 이미지있는 파일경로 보내줌
        ImageReader.OnImageAvailableListener { reader ->

            //카메라2프래그먼트에서 가져온 파일임(ImageSaver클래스안에 run함수안의 내용임).  //이미지를 회원 폰에 저장하기위한 코드
            var mImage = reader.acquireNextImage()  //사진찍은 이미지 객체
            var mFile = File(getExternalFilesDir(null), "profileImage.jpg")  //이미지를 저장할 파일. 이 경로에 저장된 이미지들은 앱이 삭제될때 같이 삭제됨

            var buffer = mImage.planes[0].buffer
            var bytes = ByteArray(buffer.remaining())
            buffer[bytes]
            var output: FileOutputStream? = null
            try {
                output = FileOutputStream(mFile)
                output.write(bytes)
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                mImage.close()
                if (null != output) {
                    try {
                        output.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            //일처리 끝냈으니 memberinit 액티비티에 데이터(이미지)결과값을 전달해줘야함. 따로 memberinit액티비티로 보내라는 코드 작성없이 profilePath라는 name값으로 알아서 찾아감
            var resultIntent = Intent()
            resultIntent.putExtra("profilePath",mFile.toString())  //돌려보낼 인텐트에 값 넣어줌. 여기선 이미지가 저장된 파일을 보냄
            setResult(Activity.RESULT_OK, resultIntent)   //onActivityResult함수로 인텐트 보냄.
            camera2BasicFragment.closeCamera()  //프래그먼트의 카메라 꺼줌
            finish()   // 액티비티 꺼버리면 됨
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        if (null == savedInstanceState) {
            camera2BasicFragment = Camera2BasicFragment.newInstance()   //카메라2 프래그먼트객체 하나 만듬
            camera2BasicFragment.setOnImageAvailableListener(mOnImageAvailableListener) //프래그먼트에 리스너를 달아줌

            supportFragmentManager.beginTransaction()
                .replace(R.id.container, camera2BasicFragment)
                .commit()
        }
    }

}