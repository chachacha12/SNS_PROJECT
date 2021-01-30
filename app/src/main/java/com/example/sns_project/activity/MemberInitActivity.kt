package com.example.sns_project.activity

//회원 기본정보 기입해서 업데이트 해주는 액티비티
                                    //클라우드firestore 데이터베이스를 이용함. db안에 회원정보를 추가해줌
                                    //내폰에 있던 이미지파일의 경로를 가져와서 파이어베이스 storage(저장소)에 이미지를 올리고 이미지 url을 가져와서 회원정보와 함께 db(클라우드firestore)에 올리는 작업있음

//왜 파이어베이스의 <인증 프로필 업데이트>를 사용하지 않고 데이터베이스를 사용하느냐?
// - 이유는 업데이트할수 있는 정보가 제한적이고(이름 등) 인증받은 사용자만 그 정보를 볼 수 있어서임.
// - 우리는 더 많은 사용자의 정보를 저장하고 다른 사용자가 게시자의 정보를 볼 수 있도록 하는걸 원하기 때문임.
//데이터베이스는 파이어베이스의 문서-가이드-클라우드 Firestore-시작하기, 클라우드firestore 에서 코드 찾아서 만들수 있음


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.sns_project.MemberInfo
import com.example.sns_project.R
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_member_init.*
import kotlinx.android.synthetic.main.activity_sign_up.checkbutton
import kotlinx.android.synthetic.main.view_loader.*
import java.io.File
import java.io.FileInputStream


class MemberInitActivity : BasicActivity() {

    lateinit var profilePath: String  //이미지가 저장된 파일의 경로를 전역으로 둠
    private lateinit var user: FirebaseUser   //회원 객체를 전역으로 선언만 해둠


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_member_init)
        init()
    }

    override fun onBackPressed() {      //이 액티비티에서 뒤로가기를 누르면, 회원정보를 입력 안했는데도 바로 메인액티비티로 다시 가는 문제가 있어서..
        super.onBackPressed()             //이 함수를 오버라이딩해서 뒤로가기를 눌렀을때 이벤트를 따로 지정해줌
        finish()
    }

    //카메라액티비티, 갤러리액티비티에서 각각 사진 찍거나 선택하면 그 이미지가 저장된 파일경로가 string값으로 여기로 인텐트 통해서 전달됨
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {  //다른 액티비티로 보낸 인텐트가 다시 결과값 가지고 돌아왔을때 작동하는 함수
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            0 -> if (resultCode == Activity.RESULT_OK) {
                profilePath = data!!.getStringExtra("profilePath")
                Glide.with(this).load(profilePath).centerCrop().override(500).into(profileimageView)
                //with()안에는 이미지를 띄울 프래그먼트나 액티비티정보를 넣어줘야해서 어댑터클래스의 인자에 띄울 데이터셋 에다가 액티비티도 추가해
                // 매니패스트안에 android:requestLegacyExternalStorage="true"  이것도 추가해줘야 사진 온전히 나옴
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun init() {
        checkbutton.setOnClickListener {
            storageUploader()
        }

        profileimageView.setOnClickListener {
            //이미지뷰 클릭시
            if (buttonsCardView.visibility == View.VISIBLE) {
                buttonsCardView.visibility = View.GONE
            } else {
                buttonsCardView.visibility = View.VISIBLE
            }
        }

        videoModify.setOnClickListener {
            //사진촬영버튼 클릭시
            var i = Intent(this, CameraActivity::class.java)
            startActivityForResult(i,
                0
            ) //다른 액티비티로 이 인텐트 보내고 그 다른 액티비티에서 일처리 끝나면 결과값or데이터 가지고 다시 이 액티비티로 돌아올거임
        }

        gallery.setOnClickListener {
            //갤러리버튼 클릭시
            //갤러리액티비티로 이동
            var i = Intent(this, Galleryactivity::class.java)
            startActivityForResult(i, 0) //다른 액티비티로 이 인텐트 보내고 그 다른 액티비티에서 일처리 끝나면 결과값or데이터 가지고 다시 이 액티비티로 돌아올거임
        } //gallery버튼
    }  //init



    fun storageUploader()   //사용자가 확인버튼 눌르면 실행시킬 함수 - 사용자의 기본 정보들을 파이어베이스에 등록(업데이트)해줌
    {
        var name = nameEditText.text.toString()
        var phoneNumber = phoneNumberEditText.text.toString()
        var birthDay = birthDayEditText.text.toString()
        var address = addressEditText.text.toString()

        if (name.length > 0 && phoneNumber.length > 9 && birthDay.length > 5 && address.length > 0) {
            loaderLayout.visibility = View.VISIBLE       //로딩화면보여줌
            //*****************사용자가 찍은 이미지를 파이어베이스 스토리지에 저장하기 위한 코드*****************
            //여기 아래에다가 파이어베이스-문서-가이드-개발-storge(시작하기, 파일업로드 )코드들 붙힘
            var storage = Firebase.storage   //파이어베이스 저장소의 객체를 하나 만듬         (시작하기의 코드)

            // Create a storage reference from our app      //저장소에 파일(내가찍은 이미지 등)을 업로드하기 위해 파일이름을 포함하여 파일의 주소경로를 파이어베이스 저장소주소에 정해줌    (파일업로드의 코드들)
            val storageRef = storage.reference
            user = Firebase.auth.currentUser!!   //회원 객체
            // Create a reference to 'images/mountains.jpg'
            val mountainImagesRef =
                storageRef.child("users/" + user?.uid + "/profileImage.jpg")     //근데 이렇게 경로를 하나만 지정해두면 모든 사용자들의 이미지가 여기로 저장되어서 덮어쓰여짐. 그래서
            //각 사용자들의 uid정보로 각각 다른 경로를 줄거임. 그래서 위의 코드로 회원 객체 가져옴.

            if (profilePath == null) {        //만약 사용자가 프로필이미지를 등록안하고 회원정보를 등록했을때를 대비한것
                //프로필이미지 없을때는 4가지의 기입한 데이터만 db(클라우드store)에 올리기
                var memberinfo = MemberInfo(
                    name,
                    phoneNumber,
                    birthDay,
                    address
                )  //이미지정보만 뺀 회원객체 하나 생성
                storeUploader(memberinfo)  //밑에 만들어둔 함수임. 회원객체를 인자로 받아서 회원정보 4개를 db에 등록시켜줌

            } else {
                //**여긴 파이어베이스-문서-가이드-개발-스토리지-파일업로드-(스트림에서업로드) 에서 가져온 코드임. 파일경로를 받아서 스토리지에 데이터 저장할때 사용함
                val stream = FileInputStream(File(profilePath))
                var uploadTask = mountainImagesRef.putStream(stream)
                //내 폰 저장소에 있는 데이터(이미지)의 URL주소를 가져올거임 (SNS프로필 등에 띄우기 위해)
                uploadTask.continueWithTask { task ->
                    if (!task.isSuccessful) {     //내폰에 저장된 이미지파일 경로를 파이어베이스의 storage에 무사히 저장했을때
                        task.exception?.let {
                            throw it
                        }
                    }
                    mountainImagesRef.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {           //이미지파일 경로 받은걸로 파이어베이스 db에 회원정보 무사히 보냈을때
                        val downloadUri =
                            task.result   //데이터(이미지)의 URL을 가져옴. 즉 내폰에 저장된 이미지파일의 경로를 가져옴

                        var memberinfo = MemberInfo(
                            name,
                            phoneNumber,
                            birthDay,
                            address,
                            downloadUri.toString()
                        )  //회원객체 하나 생성
                        storeUploader(memberinfo)  //밑에 만들어둔 함수임. 회원객체를 인자로 받아서 회원정보 4개를 db에 등록시켜줌
                    } else {
                        Toast.makeText(this, "회원정보를 보내는데 실패하였습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Toast.makeText(this, "회원정보를 입력해주세요.", Toast.LENGTH_SHORT).show()
        }
    }  //profileUpdate()함수


    //회원이 회원정보액티비티에서 4가지정보(이름. 전번, 생일, 주소)를 입력하고 확인버튼 눌렀을때 그 정보들을 db에 올려주는 코드가진 함수
    private fun storeUploader(memberinfo: MemberInfo) {

        //***************** 파이어베이스 클라우드 FIREStore에 저장하기 위한 코드*****************
        //UID를 매겨줌. 그래서 그 UID를 불러서 데이터베이스에 사용자정보 입력할때 같이 입력해주면 나중에 그 키로 사용자 찾고 할때 편함
        val db =
            FirebaseFirestore.getInstance()        //파이어베이스사이트-문서-가이드-클라우드firestore-초기화 에서 가져온 코드임
        db.collection("users").document(user!!.uid)
            .set(memberinfo)   //document에다가 현재 유저의 uid를 넣어줌.. 이럼 나중에 회원들 따로 찾기 쉬워서?
            .addOnSuccessListener {
                loaderLayout.visibility = View.GONE
                Toast.makeText(this, "회원정보 등록을 성공하였습니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                loaderLayout.visibility = View.GONE
                Toast.makeText(this, "회원정보 등록에 실패하였습니다.", Toast.LENGTH_SHORT).show()
            }
        //사이트에 있던 리스너 2개를 달아줘서 db에 데이터 등록 성공했는지 실패했는지 말해줌

    }
}