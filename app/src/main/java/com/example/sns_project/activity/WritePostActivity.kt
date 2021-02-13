package com.example.sns_project.activity
//게시글에 올린 이미지들 경로를 pathList라는 리스트에 넣어두고, 그 리스트를 통해 사진들을 파이어베이스 스토리지에 올려주고
//사진들 url들을 모아서 editText에 쓰여진 내용들과 같이 db(클라우드fireStore)에 올릴거임
//메타데이터란 어떤 데이터(이미지 등)를 설명해주거나 찾을때 유용하게 쓰는 데이터인듯. 예를 들면 인스타의 해쉬태그 느낌


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.example.sns_project.R
import com.example.sns_project.PostInfo
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata
import kotlinx.android.synthetic.main.activity_write_post.*
import kotlinx.android.synthetic.main.view_loader.*
import java.io.File
import java.io.FileInputStream
import java.util.*
import kotlin.collections.ArrayList


class WritePostActivity : BasicActivity() {
    private val TAG = "WritePostActivity"
    private lateinit var user: FirebaseUser         //현재 로그인된 회원객체를 전역으로 둘거임. 초기화는 안하고 선언만.
    private var pathList = ArrayList<String>()       //게시글에 넣은 사진이미지들의 경로들 여기에 저장해서 리스트로 만들거임
    private lateinit var buttonsBackgroundlayout: RelativeLayout     //게시글에 있는 이미지or 이 레이아웃 자체를 눌렀을때 이미지 수정 및 삭제하는 기능을 위한 레이아웃객체 전역으로둠
    private lateinit var selectedImageView: ImageView //사용자가 게시글에 올린 이미지 삭제or수정하려고 선택했을때 그 이미지를 이 전역변수에 저장해둘거임. 삭제하기 편하게.
    private var selectedEditText: EditText? = null  //우선 null로 지정해둠. 안해두면 포커스 지정안해줬을때 에러남. selectedEditText변수가 쓰이는데 초기화는 안되어있어서 에러나는듯. 그래서 null로 초기화해줌
    private lateinit var postInfo: PostInfo  //특정 게시물 수정하기 버튼 눌렀을때 이 변수에 넣어줄거임. 여러 지역함수?안에서 쓸거라 전역으로빼둠

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_post)
        postinit()
        init()
    }

    //수정하기버튼눌러서 이 액티비티 온 경우 등엔 게시글의 editText가 원래 수정전 내용으로 차있도록 하게할거임.
    private fun postinit(){
         postInfo = intent.getSerializableExtra("postInfo")  as PostInfo  //MainActivity에서 게시글 수정버튼을 눌러서 보낸 인텐트에 실린 값(수정하고자하는 게시물 객체)를 받음. 인텐트를 받을땐 getIntent() 또는 Intent 이용.
        //getSerializable은 보내는 데이터가 내가 만든 클래스의 객체일때 사용함.

        if(postInfo != null){   //null이라면 수정하기버튼 누른게 아니라 +버튼눌러서 새로운 게시글 만드려는거임. 즉, postinit()을 안거쳐도됨
            titleEditText.setText(postInfo.title)
            //이제 contents 내용들 삥삥 돌면서 기존 이미지랑 EditText들을 넣어주면됨
            var contentsList = postInfo.contents
            for (i in contentsList.indices) {
                var contents = contentsList.get(i)
                if (Patterns.WEB_URL.matcher(contents).matches() && contents.contains("https://firebasestorage.googleapis.com/v0/b/sns-project-d0fb7.appspot.com/o/post")) {        //올바른 url형식인지 판별, 즉 이미지or영상인지 // Patterns.WEB_URL.matcher().matches() 이 구문은 matcher안의 문자열이 올바른 url형식인지 판단해서 true나 false반환함
                    pathList.add(contents)
                    val layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ) //가로는 matchparent하고 위아래 길이는 wrapcontent인듯?

                    var linearLayout =
                        LinearLayout(this)   //이미지뷰와 editText뷰를 묶어서 관리하기 쉽게 레이아웃 하나 만듬
                    linearLayout.layoutParams = layoutParams
                    linearLayout.orientation =
                        LinearLayout.VERTICAL   //이렇게 갑작스럽게 만들어지는 레이아웃 또는 뷰들은 속성을 이렇게 코딩해주기

                    contentsLayout.addView(linearLayout)

                    val imageView = ImageView(this)   //새로운 이미지뷰를 하나를 이 액티비티xml에 생성함
                    imageView.layoutParams = layoutParams  //위에서 만든 layoutParams를 이미지뷰에 붙힘

                    //아래의 두줄을 통해 게시글 만드는 화면에서 이미지 추가할때, 그 이미지가 화면에 꽉차보이게 나옴.
                    imageView.adjustViewBounds = true
                    imageView.scaleType = ImageView.ScaleType.FIT_XY

                    imageView.setOnClickListener {
                        buttonsBackgroundlayout.visibility =
                            View.VISIBLE       //이미지를 삭제or수정하려고 눌렀을때
                        selectedImageView = it as ImageView
                    }

                    Glide.with(this).load(contents).override(1000)
                        .into(imageView)  //사진 경로이용해서 이미지뷰에 띄워줌
                    linearLayout.addView(imageView)  //이렇게 해주면 contentsLayout안에, 만든 이미지뷰가 생성될거임

                    val editText = EditText(this)  ////새로운 editText뷰 하나를 이 액티비티xml에 생성함
                    editText.layoutParams = layoutParams
                    editText.inputType =
                        InputType.TYPE_TEXT_FLAG_MULTI_LINE  //editText의 인풋속성(사용자가 editText에 글쓸때의 속성)을 추가해줌
                    editText.inputType = InputType.TYPE_CLASS_TEXT
                    editText.setHint("내용")
                    if (i < contentsList.size - 1) {   //처음에 이 게시글 만들때 이미지 붙여놓고 밑에 같이 생성되었던 editText안에 글을 써두었다면.
                        var nextContents = contentsList.get(i + 1)
                        if (!Patterns.WEB_URL.matcher(nextContents).matches() && nextContents.contains(
                                "https://firebasestorage.googleapis.com/v0/b/sns-project-d0fb7.appspot.com/o/post"
                            )
                        ) { //다음 contents가 이미지나 영상이 아닐경우에만
                            editText.setText(nextContents)  //editTEXT에다가 수정전에 작성했던 내용을 넣어줌
                        }
                    }

                    editText.onFocusChangeListener =
                        onFocusChangedListener   //포커스가 있는지 판별함. 포커스 있으면 이 뷰가 selectedEditText가 됨
                    linearLayout.addView(editText)
                }else if(i==0){  //i가 0인데 url형식이 아니라면 첫번째 editTEXT가 있다는 것임.
                    contentsEditText.setText(contents)   //첫 editText에다가 수정전, 기존에 있던 내용을 넣어줌
                }
            } //for
        }
    }

    private fun init() {
        checkButton.setOnClickListener {
            storageUpload()                     //이걸 누르면 파이어베이스로 게시글 쓴거 저장됨
        }

        image.setOnClickListener {
            //누르면 갤러리 실행하면됨
            var i = Intent(this, Galleryactivity::class.java)
            i.putExtra(
                "media",
                "image"
            )      //갤러리액티비티에 image라는 String값을 보냄. 갤러리액티비티에서 받을땐 키값인 media이용
            startActivityForResult(i, 0)   //requestCode가 필요한 이유는 나중에 갤러리 액티비티에서 일 마치고 결과값이
            // 이 액티비티로 돌아올때 onActivityResult()함수에서 requestCode를 비교해서 각각 다른 동작을 수행하게 할때를 위한 구분이 됨
        }

        video.setOnClickListener {
            var i = Intent(this, Galleryactivity::class.java)
            i.putExtra("media", "video")
            startActivityForResult(i, 0)
        }

        buttonsBackgroundlayout =
            buttonsBackgroundLayout    //게시글 올린 이미지 삭제or수정 창 끄려고할때 .  //전역변수를 초기화해줌.
        buttonsBackgroundlayout.setOnClickListener {
            //게시글 이미지 올린거 수정or 삭제 등등 할때를 위한 기능
            if (buttonsBackgroundlayout.visibility == View.VISIBLE) {
                buttonsBackgroundlayout.visibility = View.GONE
            }
        }

        imageModify.setOnClickListener {
            var i = Intent(this, Galleryactivity::class.java)
            i.putExtra("media", "image")
            startActivityForResult(i, 1)         //위에와 다르게 requestCode를 1로 줌
            buttonsBackgroundlayout.visibility = View.GONE
        }

        videoModify.setOnClickListener {
            var i = Intent(this, Galleryactivity::class.java)
            i.putExtra("media", "video")
            startActivityForResult(i, 1)
            buttonsBackgroundlayout.visibility = View.GONE
        }

        delete.setOnClickListener {
            var selectedView = selectedImageView.parent as View   // .parent 또는 getParent()를 하면 그 뷰의 부모 뷰(linearLayout 등)가 선택되어진다.  //removeView()안에는 뷰가 와야하는데 레이아웃이 와버려서 에러뜸. 그러므로 as를 통해 뷰로 형변환 해줌
            pathList.removeAt(contentsLayout.indexOfChild(selectedView) - 1) //pathList에서 해당 이미지를 삭제함  // indexOfChild를 써서 contentsLayout의 몇번째 뷰인지 알아냄  //첫번째 editText가 무조건 있으니까 마이너스 1 해줌
            contentsLayout.removeView(selectedView)
            buttonsBackgroundlayout.visibility = View.GONE
        }

        contentsEditText.onFocusChangeListener =
            onFocusChangedListener   //포커스리스너 붙이면 포커스가 있는지 판별함. 포커스 있으면 이 뷰가 selectedEditText가 됨
        titleEditText.setOnFocusChangeListener { v, hasFocus ->
            selectedEditText = null
        }   //만약 제목칸에 포커스 있을때, 이미지 넣었을때 처리

    }  //init


    // 이미지가 저장된 파일경로가 string값으로 여기로 인텐트 통해서 전달됨 / 사용자가 갤러리에서 카드뷰사진 하나 선택했을때
    //이미지뷰와 editTextView가 동적으로 하나씩 계속 생성되도록함.
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {  //다른 액티비티로 보낸 인텐트가 다시 결과값 가지고 돌아왔을때 작동하는 함수
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            0 -> if (resultCode == Activity.RESULT_OK) {          //requestCode가 0일땐 게시글에 선택한 이미지를 붙여줌
                var profilePath = data!!.getStringExtra("profilePath")  //데이터(파일)을 받아서 저장
                pathList.add(profilePath)     //ArrayList에 사진경로들을 저장함

                val layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ) //가로는 matchparent하고 위아래 길이는 wrapcontent인듯?

                var linearLayout = LinearLayout(this)   //이미지뷰와 editText뷰를 묶어서 관리하기 쉽게 레이아웃 하나 만듬
                linearLayout.layoutParams = layoutParams
                linearLayout.orientation =
                    LinearLayout.VERTICAL   //이렇게 갑작스럽게 만들어지는 레이아웃 또는 뷰들은 속성을 이렇게 코딩해주기

                if (selectedEditText == null) {
                    contentsLayout.addView(linearLayout)
                } else {            //내가 포커스 준 editText가 있을때
                    var i = 0
                    repeat(contentsLayout.childCount) {
                        if (contentsLayout.getChildAt(i) == selectedEditText?.parent) {   //이미 onFocusChangeListener가 selectedEditText를 내가 포커스 준 녀석으로 바꿔뒀을거임
                            contentsLayout.addView(
                                linearLayout,
                                i + 1
                            )    //내가 선택해서 포커스 가있는 editText 바로 다음에 새로운 이미지를 추가해준다.
                        }
                        i++
                    }
                }

                val imageView = ImageView(this)   //새로운 이미지뷰를 하나를 이 액티비티xml에 생성함
                imageView.layoutParams = layoutParams  //위에서 만든 layoutParams를 이미지뷰에 붙힘

                //아래의 두줄을 통해 게시글 만드는 화면에서 이미지 추가할때, 그 이미지가 화면에 꽉차보이게 나옴.
                imageView.adjustViewBounds=true
                imageView.scaleType = ImageView.ScaleType.FIT_XY

                imageView.setOnClickListener {
                    buttonsBackgroundlayout.visibility = View.VISIBLE       //이미지를 삭제or수정하려고 눌렀을때
                    selectedImageView = it as ImageView
                }

                Glide.with(this).load(profilePath).override(1000).into(imageView)  //사진 경로이용해서 이미지뷰에 띄워줌
                linearLayout.addView(imageView)  //이렇게 해주면 contentsLayout안에 만든 이미지뷰가 생성될거임

                val editText = EditText(this)  ////새로운 editText뷰 하나를 이 액티비티xml에 생성함
                editText.layoutParams = layoutParams
                editText.inputType =
                    InputType.TYPE_TEXT_FLAG_MULTI_LINE  //editText의 인풋속성(사용자가 editText에 글쓸때의 속성)을 추가해줌
                editText.inputType = InputType.TYPE_CLASS_TEXT
                editText.setHint("내용")
                editText.onFocusChangeListener =
                    onFocusChangedListener   //포커스가 있는지 판별함. 포커스 있으면 이 뷰가 selectedEditText가 됨
                linearLayout.addView(editText)
            }
            1 -> if (resultCode == Activity.RESULT_OK) {
                var profilePath = data!!.getStringExtra("profilePath")
                Glide.with(this).load(profilePath).override(1000)
                    .into(selectedImageView)   //이미지를 수정해줌
            }
        }
    }

    //onFocusChangedListener는 뷰가 포커스를 가지고 있는지 판별해주고, 가지고 있다면(hasFocus) 그때의 이벤트를 처리해줌
    // 여기선 뷰가 이 리스너를 달고 있고 포커스를 가지고 있다면 그 뷰가 전역변수인 selectedEditText가 된다. 즉 이 리스너는 selectedEditText를 정해주는 기능
    private var onFocusChangedListener =
        View.OnFocusChangeListener { v, hasFocus -> selectedEditText = v as EditText }

    //전역변수
    var pathCount = 0     //게시글에 첨부된 사진이 몇개인지 알기위해서
    var successCount = 0    //게시글에 첨부한 사진이 여러개일수 있으니, 언제 끝나는지 확인해주기 위한 변수

    //memberinit액티비티에서 가져온 함수 2개 -> profileUpdate와 uploader함수를 변형해준거임
    private fun storageUpload()   //사용자가 확인버튼 눌르면 실행시킬 함수 -게시글 작성한걸 파이어베이스에 등록(업데이트)해줌
    {
        var tilte = titleEditText.text.toString()

        if (tilte.length > 0) {
            loaderLayout.visibility = View.VISIBLE    //로딩화면 보여줌.
            user = Firebase.auth.currentUser!!          // 현재 회원객체 가져옴
            var contentsList = ArrayList<String>()     // 게시글쓸때 이미지첨부하고 그 밑에 생긴 editText에 쓴 내용들을 여기에 모을거임
            var storage = Firebase.storage   //파이어베이스 저장소(스토리지)의 객체를 하나 만듬
            val storageRef = storage.reference
            val firebaseFirestore =
                FirebaseFirestore.getInstance()  //파이어베이스 클라우드firestore(db)객체를 가져옴

            var documentReference =
                if(postInfo ==null){  //게시글 새로 만들려고 +버튼 눌러서 이 액티비티 왔을때
                    firebaseFirestore.collection("posts").document()   //db에 있는 posts컬렉션의 documents주소를 가져옴 (이 주소안에 데이터넣거나 등등에 쓰려고가져옴)
                }else{  //수정버튼을 눌러서 이 액티비티로 왔을때
                     firebaseFirestore.collection("posts").document(postInfo.id!!)  //이러면 id에 맞는 특정 위치의 문서에 수정한 게시글이 생기면서 원래 있던 게시글은 덮여써질거임.
                }
            //게시글 새로만드려고 이 액티비티 온건지 수정버튼 눌러서 온건지에 따라, 만드는 게시글의 생성일을 새로만들거나, 기존꺼 유지하거나함.
            var date =
                if(postInfo  == null){
                     Date()
                }else{
                     postInfo.createdAt!!
                }

            //contentsLayout안에 들어있는 자식뷰의 유형(이미지뷰, 에디트텍스트뷰)에 따라 나눠서 파이어베이스에 저장
            var i = 0
            repeat(contentsLayout.childCount) {
                //반복문임.  contentsLayout안에 있는 자식뷰의 갯수만큼 반복
                var linearLayout =
                    contentsLayout.getChildAt(i) as LinearLayout  //순서대로 자식뷰를 하나씩 가져옴. 자식뷰들은 다 LinearLayout이었음

                var index = 0
                //linearLayout안에는 이미지뷰와 editText뷰 2개의 자식뷰가 있음
                repeat(linearLayout.childCount) {
                    var view = linearLayout.getChildAt(index)
                    if (view is EditText) {               //코틀린에선 자료형이 일치하는지 판별을 is 연산자씀. 자바에선 instanceof 였음.
                        var text = view.text.toString()   //인덱스 0이 첫번째이므로 이미지뷰이고 1은 editText뷰임
                        if (text.length > 0) {
                            contentsList.add(text)
                        }
                    } else {   //자식뷰가 url이 아닐경우에만 스토리지, db에 저장해줄거임 (url일 경우엔 수정 )
                        contentsList.add(pathList[pathCount])  //contentsList에 사진경로를 넣어줌. pathList라는 리스트안엔 아까 게시글 써줄때 넣은 사진들의 경로가 순서대로 들어있음

                        var pathArray = pathList.get(pathCount).split(".")       // .을 기준으로 나눠서 사진경로문자열을 pathArray배열안에 저장


                        //*****************파이어베이스 스토리지에 사진경로로 사진 저장하기 위한 코드***************** memberinit에서 가져옴
                        val mountainImagesRef =
                            storageRef.child("posts/" + documentReference.id + "/" + pathCount + "."+pathArray[pathArray.size-1])  //첨부한 사진을 순서대로 번호붙여서 저장할거임.   "."+pathArray[pathArray.size-1 이걸 씀으로 .jpg나 .png등으로 저장될거임

                        //**여긴 파이어베이스-문서-가이드-개발-스토리지-파일업로드-(스트림에서업로드) 에서 가져온 코드임. 파일(사진)경로를 받아서 스토리지에 데이터 저장할때 사용함
                        val stream = FileInputStream(File(pathList[pathCount]))

                        var metadata = storageMetadata {
                            //(문서-스토리지-파일 메타데이터사용-커스텀메타데이터)   //메타데이터를 통해 각 데이터(사진 등)의 인덱스 위치를 알 수 있음
                            setCustomMetadata(
                                "index",
                                "" + (contentsList.size - 1)
                            )     //게시글 사진 스토리지 저장때 쓸 메타데이터 하나 만듬. index가 키값. contentsList의 마지막 인덱스값 넣어줌
                        }                                                                //키값다음에 오는 거에는 현재위치?를 넣어줘야함

                        var uploadTask = mountainImagesRef.putStream(
                            stream,
                            metadata
                        )  //사진경로와 메타데이터를 인자로 실어서 스토리지주소에 업로드
                        uploadTask.addOnFailureListener {

                        }.addOnSuccessListener { taskSnapshot ->
                            //위에서 만든 메타데이터를 통해 정보(데이터?)의 인덱스값 받음
                            var index =
                                Integer.parseInt(taskSnapshot.metadata?.getCustomMetadata("index")!!)  //인덱스값을 얻음

                            //스토리지에 사진경로 올렸고, 다시 스토리지주소를 통해 사진경로(uri)값을 가져오는 작업.
                            //가져와서 메타데이터 통해 만든 index값에 맞춰서 리스트에 이미지 uri를 저장하면, editText안의 내용과 uri가 순서대로 contentsList에 잘 들어갈거임!!
                            mountainImagesRef.downloadUrl.addOnSuccessListener {
                                contentsList.set(
                                    index,
                                    it.toString()
                                )         //여기서 it이 uri값임.  contentsList의 index에 맞는 인덱스안에 uri넣음
                                successCount++
                                if (pathList.size == successCount) {    //게시글에 내가 첨부했던 모든 사진들(pathList)이 스토리지에 업로드되었고, contentsList에 추가되었을때
                                    //완료 로직
                                    var WriteInfo = PostInfo(
                                        tilte,
                                        contentsList,
                                        user.uid,
                                        date
                                    )  //게시글 객체 하나 생성
                                    storeupload(documentReference, WriteInfo)  //밑에 만들어둔 함수임. 게시글 객체를 인자로 받아서 게시글을 db에 등록시켜줌. documentReference를 인자로 보내는 이유는
                                    // db에 있는 게시글들의 uid값이랑 스토리지에 있는 이미지들 uid값이랑 같게 해주는게 찾을때 편해서 그리 해주려고.
                                }
                            }
                        }
                        pathCount++
                    }  //자식뷰가 이미지뷰일때
                    index++
                }  //작은 repeat문
                i++
            }  //큰 repeat문
            if (pathList.size == 0) {            //사용자가 게시글에 이미지는 하나도 안넣었을때도 게시글 등록은 해줘야 하므로.
                var WriteInfo = PostInfo(tilte, contentsList, user.uid, date)  //게시글 객체 하나 생성
                storeupload(documentReference, WriteInfo)
            }
        } else {
            Toast.makeText(this, "제목을 입력해주세요.", Toast.LENGTH_SHORT).show()
        }
    }


    //회원이 확인버튼 눌렀을때 회원이 쓴 게시글을 db(클라우드firestore)에 올려주는 코드가진 함수
    private fun storeupload(documentReference: DocumentReference, writeinfo: PostInfo) {
        documentReference.set(writeinfo)                //add함수는 자동으로 데이터의 documents에 uid를 암거나 만들어서 넣어줌. 섞이지 않게. 그리고 set은 내가 uid만든거에 넣어주는함수. documentReference변수가 내가 따로 가져온 uid값임
            .addOnSuccessListener {
                loaderLayout.visibility = View.GONE
                Log.d(TAG, "DocumentSnapshot successfully written!")
                Toast.makeText(this, "게시물을 등록하였습니다.",Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e)
                Toast.makeText(this, "게시물을 등록 실패.",Toast.LENGTH_SHORT).show()
                loaderLayout.visibility = View.GONE}
    }
}




