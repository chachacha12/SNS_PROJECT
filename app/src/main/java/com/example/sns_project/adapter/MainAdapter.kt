package com.example.sns_project.adapter

//GalleryAdapter클래스를 복사해서 좀 바꿔서 써준 어댑터임

import android.app.Activity
import android.graphics.Color
import android.util.Log
import android.util.Patterns
import android.view.*
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sns_project.PostInfo
import com.example.sns_project.R
import com.example.sns_project.listener.OnPostListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_login.view.*
import kotlinx.android.synthetic.main.activity_login.view.titletextView
import kotlinx.android.synthetic.main.item_post.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
                                    //괄호안은 어댑터클래스의 인자들
class MainAdapter(var activity: Activity, private var myDataset: ArrayList<PostInfo>, var onPostListener: OnPostListener)    //인자로 onPostListener라는 인터페이스 객체를 준 이유는 어댑터안에서도 인터페이스의 onDelete, onModify 함수를 쓰기위해.
                                        : RecyclerView.Adapter<MainAdapter.MainViewHolder>() {

    //뷰홀더에 텍스트뷰말고 카드뷰를 넣음
    class MainViewHolder(val cardView: CardView) : RecyclerView.ViewHolder(cardView)


    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(    //레이아웃 item_post에 있는 카드뷰를 가리키는 뷰홀더를 만듬. 이건 처음에 액티비티에서 recyclerView.adapter = mainAdapter 할때만 작동하고 그후엔 안함.
        parent: ViewGroup,
        viewType: Int
    ): MainViewHolder {
        val cardView: CardView = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false) as CardView   //item_post에 있는 뷰들에 접근가능하게 해줌.  inflate에 들어간 레이아웃은 row파일과 같은거임.

        val mainViewHolder = MainViewHolder(cardView)  //밑의 setOnClickListener에서 사용자가 선택한 특정뷰의 위치값 알아야해서 여기서 뷰홀더객체생성

        //특정 게시글을 눌렀을때 효과
        cardView.setOnClickListener {
        }

        //게시글의 toolbar(점3개)버튼을 클릭했을때 효과
        cardView.menu.setOnClickListener {
            showPopup(it, mainViewHolder.adapterPosition)      //post.xml을 띄워줌. 밑에 있음. 구글에 android menu검색하고 developers사이트들어가서 코드 가져옴
        }                                                     //mainViewHolder.adapterPosition을 넣어주는 이유는 사용자가 선택한 특정위치의 게시글을 삭제or수정해야 하기에.

        return mainViewHolder
    }


    // 여기서 리사이클러뷰의 리스트 하나하나 가리키는 뷰홀더와 내가 주는 데이터(게시글)가 연결되어짐. 즉 리사이클러뷰 화면에 띄워짐
     //액티비티에서 게시글 업데이트 해주려고 mainAdapter.notifyDataSetChanged() 하면 이 함수만 작동함.
    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        var cardView = holder.cardView
        var titletextView = cardView.titletextView
        titletextView.text = myDataset?.get(position).title        //게시글의 제목을 가져옴

        var createdAt = cardView.createdAttextView  //게시글의 생성일을 가져옴
        createdAt.text = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault()
        ).format(myDataset?.get(position).createdAt)

        var contentsList = myDataset?.get(position).contents   //게시글 내용인 데이터들
        val layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        var contentsLayout = cardView.contentsLayout  //여기안에 contentsList의 내용들(사진,영상,글) 등을 넣을거임


        //이미지, 동영상, 글 등 contents내용들을 담는 뷰들(이미지뷰, 텍스트뷰)만들고 데이터들 그 안에 넣을거임
        if (contentsLayout.getTag() == null || !contentsLayout.getTag().equals(contentsList)) {     //데이터가 같을수도 있는데 계속 뷰들 다 지웠다 만들고 하는건 낭비라서 이 로직 추가함.(null일땐 처음 앱 실행할때를 위해) 이 로직 없다면 스크롤 내릴때마다 뷰들 삭제되고 생성되고했을거임!!
            contentsLayout.setTag(contentsList)
            contentsLayout.removeAllViews()   //액티비티 onResume()의 notifyDataSetChanged()를 통해 게시글 업데이트 해줄때마다 뷰 다 지우고 새롭게 만들어줄거임
            val MORE_INDEX = 2   //메인화면상에서 한 게시글마다 몇개의 뷰까지 보여주고 더보기 나오게 할지를 정할 숫자. 2개로 함

            //indices는 배열의 인덱스번호를 하나하나 알려줌. 즉 for문은 i가 0부터 contentsList의 크기만큼 반복.
            for (i in contentsList.indices) {       //리스트안의 데이터 개수에 맞춰서 뷰를 생성해줌.
                if (i == MORE_INDEX) {         //메인화면상에서 한 게시글의 모든 내용이 보이면 좀 그러니까 2개정도 보여주고 <더보기> 기능 만들기위함
                    var textView = TextView(activity)
                    textView.layoutParams = layoutParams
                    textView.text = "더보기.."
                    contentsLayout.addView(textView)
                    break   //더이상 뷰 안 만들고 게시글 만드는거 끝.
                }

                var contents = contentsList.get(i)
                if (Patterns.WEB_URL.matcher(contents).matches() && contents.contains("https://firebasestorage.googleapis.com/v0/b/sns-project-d0fb7.appspot.com/o/post")) {        //올바른 url형식인지 판별, 즉 이미지or영상인지 // Patterns.WEB_URL.matcher().matches() 이 구문은 matcher안의 문자열이 올바른 url형식인지 판단해서 true나 false반환함
                    //editText가 url주소면 밑의 작업을 수행하므로, 혹시나 사용자가 url을 텍스트로 입력해도 이미지뷰가 나오므로 그 경우 차단을 위해, 우리가 인정한 url경로만 이미지뷰로 출력할 수 있도록 하기위해 && 뒤의 조건을 추가함. db에 있는 모든 저장된 사진들의 주소의 앞부분은 저게 포함되있는걸 이용함

                    var imageView = ImageView(activity)
                    imageView.layoutParams = layoutParams
                    imageView.adjustViewBounds = true
                    imageView.scaleType = ImageView.ScaleType.FIT_XY  // 이미지가 꽉 차서 나올거임
                    contentsLayout.addView(imageView)
                    Glide.with(activity).load(contents).override(1000).thumbnail(0.1F)
                        .into(imageView)  //이 작업은 밑에 onbindViewholder함수에서 할거임
                } else {
                    var textView = TextView(activity)
                    textView.layoutParams = layoutParams
                    textView.text = contents
                    textView.setTextColor(Color.rgb(0, 0, 0))   //글씨를 진한 검은색으로 해줌줌
                    contentsLayout.addView(textView)
                }
            } //for

        }
    }


    override fun getItemCount() = myDataset!!.size


   //res안에 menu디렉토리 만든거에서, 그 안의 menu파일을 불러와서 toolbar보여주고, 클릭했을때 이벤트처리해줌  //developers사이트에서 가져온 함수.
    private fun showPopup(v: View, position: Int) {
        val popup = PopupMenu(activity, v)
        popup.setOnMenuItemClickListener {

            return@setOnMenuItemClickListener when (it.itemId) {
                R.id.modify -> {                    //수정하기 눌렀을때
                    onPostListener.onModify(position)      //게시글의 postList상에서의 위치를 인자를 통해 액티비티에 전달함. 그 후 액티비티에서 삭제로직을 통해 게시글 db, 스토리지에서 삭제.->어댑터에서 삭제로직 안하는 이유는 여기선 db접근해서 삭제는 할수있어도 실시간으로 업데이트는 못해줘서임. OnResume()함수 등이 액티비티에 존재.
                     true
                }
                R.id.delete -> {                  //삭제하기 눌렀을때
                    onPostListener.onDelete(position)
                    true
                }
                else -> false
            }
        }
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.post, popup.menu)
        popup.show()
    }

}