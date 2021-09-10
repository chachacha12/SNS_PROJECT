package com.example.sns_project.activity

//로그인해서 들어왔을때 창임. 여기서 로그아웃 가능
//클라우드firestore 데이터베이스를 통해서 로그인된 계정이 db에 있는지, db에서 데이터 읽어와서 확인함.

//이 SNS_PROJECT 앱은 파이어베이스를 기반으로해서 만듬. (파이어베이스는 서버리스인 db임. 이 db가 서버역할도 하는 것)
// 파이어베이스-문서-가이드-개발(인증(앱에 파이어베이스연결, 신규사용자가입 등 기능), cloud firestore(db에 저장된 회원정보 읽거나 추가 기능), storage() 등을 이용)

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sns_project.PostInfo
import com.example.sns_project.R
import com.example.sns_project.adapter.MainAdapter
import com.example.sns_project.listener.OnPostListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : BasicActivity() {

    //전역으로 해둔 이유는 여러함수 안에서 불러와서 쓰고 싶기에. 등등
    private val TAG = "MainActivity"
    var firebaseUser: FirebaseUser? = null
    private lateinit var firebaseFirestore: FirebaseFirestore
    private lateinit var mainAdapter: MainAdapter
    private lateinit var postList: ArrayList<PostInfo>
    lateinit var storageRef: StorageReference   //게시글 지울때 스토리지에도 접근해서 이미지 지워줘야해서, 그때 필요함
    var successCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var storage = Firebase.storage   //파이어베이스 저장소(스토리지)의 객체를 가져옴
        storageRef = storage.reference
        init()
    }

    fun init() {

        firebaseUser = Firebase.auth.currentUser   //회원 객체

        if (firebaseUser == null)   //만약 현재 유저가 null이면... (즉, 로그인이 아직 안되어있다는 뜻)
        {
            var i = Intent(this, SignUpActivity::class.java)   //회원가입창 화면으로 이동
            startActivity(i)
            //이렇게 하는 이유는 이 앱의 첫 실행화면을 메인액티비티로 해두어서임. 그 이유는 메인액티비티에서 뒤로가기 했을때 로그인창 같은게 나오면 보기 안좋으니까, 바로 앱이 꺼질수 있게 하기위함
        } else {
 //회원가입or로그인 했을시  (원래 여기에 파이어베이스의 인증 프로필 업데이트를 썻다가 그거 안쓰고 데이터베이스(클라우드firestore) 쓰기로 해서 지우고 이거씀
        //회원정보가 파이어베이스의 firestore 데이터베이스에 없을때만 회원정보 입력창으로 이동하도록 하는 코드
        firebaseFirestore = FirebaseFirestore.getInstance()

        //*  이 구간은 클라우드 firestore에서 데이터 읽기-데이터 한번 가져오기-문서가져오기 에 있는 코드임. 직접 더 추가한 코드도 있음
        val docRef = firebaseFirestore.collection("users").document(firebaseUser!!.uid)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    if (document.exists()) {      //회원정보를 이미 이전에 작성한 계정인 경우(저장된 uid정보가 있을때)
                        Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                    } else {  //저장된 uid정보?가 없을때
                        Log.d(TAG, "No such document")
                        var i = Intent(
                            this,
                            MemberInitActivity::class.java
                        )      //회원정보 입력하라는 액티비티 띄움
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(i)
                    } //else
                }  //if
            } //Lisener
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
        }


        postList = ArrayList<PostInfo>()   //postInfo 객체를 저장하는 리스트를 초기화
        var recyclerView = findViewById<RecyclerView>(R.id.recyclerView)  //화면에 보일 리사이클러뷰객체

        //리사이클러뷰를 여기서 제대로 만들어줌.
        mainAdapter = MainAdapter(
            this,
            postList, onPostListener
        )  //어댑터의 멤버변수에 onPostListener를 전달. 즉 이 덕분에 어댑터에서도 인터페이스객체(리스너객체) 사용가능. 즉, 인터페이스안의 함수들 사용가능

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter =
            mainAdapter    //리사이클러뷰의 어댑터에 내가 만든 어댑터 붙힘. 사용자가 게시글 지우거나 수정 등 해서 데이터 바뀌면 어댑터를 다른걸로 또 바꿔줘야함 ->notifyDataSetChanged()이용

        /*
        logoutbutton.setOnClickListener {  //로그아웃버튼 눌렀을때
            Firebase.auth.signOut()  //현재 로그인된 계정이 로그아웃됨
            var i = Intent(this, SignUpActivity::class.java)   //회원가입창 화면으로 이동
            startActivity(i)
        }
         */
        //새 게시글 만들기위해 floatingActionButton눌렀을때
        floatingActionButton.setOnClickListener {
            var i = Intent(this, WritePostActivity::class.java)
            startActivity(i)
        }
    }  //init


    //사용자가 실시간으로 게시글 삭제, 수정할때에 맞춰서 리스트 업데이트 해줄거임
    //인터페이스 객체를 어댑터말고 액티비티에 구현해둬야하는 이유는 onResume함수 등이 있어서 게시글 업데이트를 해줄수 있어서?
    //인터페이스를 구현한 익명객체를 생성해서 사용할거임. 그리고 이걸 어댑터에 인자로 넣어주면 어댑터에서도 사용가능.
    val onPostListener = object : OnPostListener {
        override fun onDelete(position: Int) {        //postList상에서의 게시글 위치값을 받아서 지워줄거임
            var id = postList.get(position).id!!  //postList의 특정위치의 게시글의 id값 가져옴. 밑에서 splite한거 찾을때 쓰임

            //이미지,영상등을 스토리지에 저장된 이름을 알아내서 스토리지에서 삭제하는 로직  // 파이어베이스의 스토리지는 여러 사진들 모아두는 폴더가 따로 없어서 사진 하나하나 지워줘야한다고 함.
            //이제 postList안의 특정 게시물의 contents를 돌면서, 그게 이미지면 삭제할거임.
            //indices는 배열의 인덱스번호를 하나하나 알려줌. 즉 for문은 i가 0부터 contentsList의 크기만큼 반복. // 메인어댑터에서 가져온 코드
            var contentsList = postList?.get(position).contents   //\
            for (i in contentsList.indices) {      //스토리지 안에있는 이미지들 지우기 위한 반복문
                var contents = contentsList.get(i)
                if (Patterns.WEB_URL.matcher(contents).matches() && contents.contains("https://firebasestorage.googleapis.com/v0/b/sns-project-d0fb7.appspot.com/o/post")) {        //올바른 url형식인지 판별, 즉 이미지or영상인지 // Patterns.WEB_URL.matcher().matches() 이 구문은 matcher안의 문자열이 올바른 url형식인지 판단해서 true나 false반환함
                    //editText가 url주소면 밑의 작업을 수행하므로, 혹시나 사용자가 url을 텍스트로 입력해도 이미지뷰가 나오므로 그 경우 차단을 위해, 우리가 인정한 url경로만 이미지뷰로 출력할 수 있도록 하기위해 && 뒤의 조건을 추가함. db에 있는 모든 저장된 사진들의 주소의 앞부분은 저게 포함되있는걸 이용함
                    successCount++ //전역변수임. 이걸 증가시켜주고 밑에서 --해줌으로써
                    var list: List<String> =
                        contents.split("?")  //이미지경로안를 split해서 이미지의 이름을 가져옴. 이미지의 이름을 알기위해
                    var list2: List<String> = list[0].split("%2F")
                    var name = list2[list2.size - 1] //스토리지에 저장된 이미지의 이름(ex. 0.jpg)을 알아냄

                    //파이어베이스 문서-스토리지-안드로이드-파일삭제  (스토리지 안의 내용 삭제)
                    val desertRef =
                        storageRef.child("posts/" + id + "/" + name) //스토리지에서 지울 이미지의 경로를 줌
                    desertRef.delete().addOnSuccessListener {
                        successCount--   //위에서 ++하고 여기서 --해서, 지울 게시글에 이미지 여러개 일때 한개만 거치고 바로 끝내는거 아니냐 할수도 있는데 그렇지않음. 리스너는 서버와 통신하는 거라서, 만약 이미지3개라면 위의 ++이 먼저 3번 다 돌아가고 그 후 리스너 동작해서 3,2,1,0해서 if문 돌고 끝냄.
                        storeUploader(id) //스토리지가 제대로 다 삭제되었으면 db안의 내용을 지워줄거임
                    }.addOnFailureListener {
                        Toast.makeText(this@MainActivity, "ERROR.", Toast.LENGTH_SHORT)
                            .show()
                    }
                } //if
            } //for
            storeUploader(id)  //이건 지우려는 게시글에 이미지가 하나도 없을때도 db는 지워야하기 때문에 둠.
        }

        //게시글 수정작업
        override fun onModify(position: Int) {
            myStartActivity(WritePostActivity::class.java, postList.get(position))  //선택한 게시물을 인텐트를 보내줌
        }
    }

    //데이터를 실어서 특정 액티비티에 보내주는 인텐트를 함수로 만들어둠
    fun myStartActivity(c: Class<*>, postInfo: PostInfo) {
        var i = Intent(this, c)
        i.putExtra("postInfo", postInfo)  //내가 클래스 통해 만든 객체들을 putExtra로 보내려면 보내려는 객체 클래스(PostInfo)에 : Serializable 해줘야함
        startActivity(i)
    }


    //액티비티가 재실행되거나 홈버튼 눌러서 나갔다왔을때 등의 경우에 onCreate말고 이 함수가 실행됨. (이때마다 게시글들 새로고침 해주면될듯)
    //앱 처음 실행시엔 onCreate와 onResume함수가 둘다 실행되므로 중복되는 코드는 쓰지 않기
    override fun onResume() {
        super.onResume()
        postUpdate()  //
    } //onResume


    //사용자가 게시글을 삭제하거나 수정하거나 만들거나 등등 했을때 게시글 다 지웠다가 다시 바뀐 postList통해 넣어주는 방식으로 화면에 업데이트 시켜줄거임
    private fun postUpdate() {
        if (firebaseUser != null) {
            val collectionReference = firebaseFirestore.collection("posts")
            //db(클라우드firestore)에서 게시글 데이터들을 가져오는 코드(파이어베이스문서 - cloudeFirestore-데이터읽기-데이터한번 가져오기- 컬렉션에서 여러 문서가져오기)
            collectionReference
                .orderBy("createdAt",  Query.Direction.DESCENDING)
                .get()     //게시글을 생성일기준으로 순서대로 보여주고자할때 orderBy함수이용(문서-가이드-클라우드fireStore-데이터읽기-데이터정렬 및 제한-내림차순으로 정리하기?)
                .addOnSuccessListener { documents ->
                    postList.clear()  //재실행 될때마다 onResume()이 실행되어서 for문 땜시 데이터가 추가되므로 그걸 막기위해 리스트 비워줌
                    for (document in documents) {           //postList안에 게시글 데이터들을 넣어줌 (document가 posts컬렉션안에 있는 각각 게시글들 인듯)

                        postList.add(
                            PostInfo(
                                document.data.get("title").toString(),
                                document.data.get("contents") as ArrayList<String>,  //형변환
                                document.data.get("publisher").toString(),
                                document.getDate("createdAt")?.time?.let { Date(it) },  //게시글 생성일정보
                                document.id
                            )  //게시글id정보(수정, 삭제하려고 할때 이용하기 위해)
                        )
                    }
                    mainAdapter.notifyDataSetChanged()   //이렇게 해주면 어댑터의 데이터가 업데이트된 상태로 바뀜. 즉 새로고침해줌. recyclerView.adapter = mainAdapter를 새로 한 것과 비슷.
                    // 하지만 다른점은 이건 새로운 데이터(인자)가지고 어댑터 클래스에서 onBindViewHolder()만 거침. onCreateView등은 안 거침.
                    // 그래서 onBindView안에서 기능들 다 넣어줘야함.
                    //즉, 어댑터에 postList 있는데 그게 새로 바뀐(게시글 삭제or수정시) postList가 들어옴
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents: ", exception)
                }
        }
    }

    //db안에 데이터 지워주는 함수
    fun storeUploader(id: String) {
        if (successCount == 0) {  //스토리지가 제대로 다 삭제되었을때만 db안의 내용도 삭제할거임.
            //아래 코드는 파이어베이스-문서-데이터추가및 관리-데이터삭제-문서삭제 에서 가져옴 (클라우드firestore, 즉 db안의 게시글 삭제해줌)
            firebaseFirestore.collection("posts")
                .document(id)  //이러면 posts컬렉션에서 해당 id값을 가진 문서(게시글)를 삭제할거임
                .delete() //"게시글을 삭제하였습니다."
                .addOnSuccessListener {
                    Toast.makeText(this@MainActivity, "게시글을 삭제하였습니다.", Toast.LENGTH_SHORT).show()
                    postUpdate()   //게시글을 업데이트, 새로고침해줌.  onResume함수안에 있는 로직임.
                }
                .addOnFailureListener {
                }
        }

    }

}

