package com.example.sns_project.adapter        //아래는 developer사이트의 문서에서 가져온 코드 등등

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sns_project.R
import com.example.sns_project.activity.MemberInitActivity
import kotlinx.android.synthetic.main.activity_member_init.*
import kotlinx.android.synthetic.main.item_gallery.view.*

class GalleryAdapter(var activity: Activity,  private val myDataset: ArrayList<String?>?) :     //어댑터클래스의 인자 2개
    RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder>() {


    class GalleryViewHolder(val cardView: CardView) : RecyclerView.ViewHolder(cardView)   //뷰홀더에 텍스트뷰말고 카드뷰를 넣음

    //온크리에이트뷰홀더함수안에서 사용자가 특정 사진 선택했을때 프로필사진으로 등록되는 기능 여기서 해줄거임
    override fun onCreateViewHolder(    //레이아웃 item_gallery에 있는 카드뷰를 가리키는 뷰홀더를 만듬
        parent: ViewGroup,
        viewType: Int
    ): GalleryViewHolder {
        val cardView: CardView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gallery, parent, false) as CardView   //inflate에 들어간 레이아웃은 row파일과 같은거임.

        val galleryViewHolder = GalleryViewHolder(cardView)  //밑의 setOnClickListener에서 사용자가 선택한 특정뷰의 위치값 알아야해서 여기서 뷰홀더객체생성

        cardView.setOnClickListener {                //사용자가 갤러리에서 특정 사진을 클릭해서 선택했을때
            //memberinit 액티비티에 데이터(이미지)결과값을 전달해줘야함. profilePath라는 보내는 곳의 name값을 통해 알아서 찾아감
            val resultIntent = Intent()
            resultIntent.putExtra("profilePath", myDataset!![galleryViewHolder.adapterPosition])  //돌려보낼 인텐트에 값 넣어줌. 여기선 이미지가 저장된 경로를 보냄
            activity.setResult(Activity.RESULT_OK, resultIntent)   //onActivityResult함수로 인텐트 보냄.
            activity.finish()  //갤러리액티비티 닫아줌
        }
        return galleryViewHolder
    }


    // 여기서 리사이클러뷰의 리스트 하나하나 가리키는 뷰홀더와 내가 주는 데이터(이미지경로)가 연결되어짐
    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {

        var imageView = holder.cardView.imageView
        //myDataset!![position]라는 갤러리의 사진들 경로는 알았으니, 밑의 코드통해 이미지뷰에 내 갤러리 사진들 띄울거임.
        //가져온 이미지들이 리사이클러뷰에서 너무 사이즈 이상하게 나오는 문제 해결해줌 --> Glide라는걸 이용할거임! -> Gradle의 dependency에서 라이브러리 추가해주고, 아래 코드들 넣어주면 됨.+ 이미지경로값 필요
        Glide.with(activity).load(myDataset!![position]).centerCrop().override(500).into(imageView)  //with()안에는 이미지를 띄울 프래그먼트나 액티비티정보를 넣어줘야해서 어댑터클래스의 인자에 띄울 데이터셋 에다가 액티비티도 추가해
                                                                            // 매니패스트안에 android:requestLegacyExternalStorage="true"  이것도 추가해줘야 사진 온전히 나옴
    }



    override fun getItemCount() = myDataset!!.size

}