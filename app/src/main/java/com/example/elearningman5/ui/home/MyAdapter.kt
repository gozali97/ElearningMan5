package com.example.elearningman5.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.elearningman5.R
import com.example.elearningman5.firebase.FirebaseMessagingHelper
import com.example.elearningman5.ui.home.materi.MateriFragment


class MyAdapter(dataList: ArrayList<DataClass>,
                private var fa: Fragment
) : RecyclerView.Adapter<MyViewHolder>() {
    private var dataList: ArrayList<DataClass>
    private var firebaseMessagingHelper: FirebaseMessagingHelper = FirebaseMessagingHelper()

    init {
        this.dataList = dataList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.recycler_mapel, parent, false)
        return MyViewHolder(view)
    }

    @SuppressLint("ResourceType", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
//        Glide.with(context).load(dataList[position].getDataImage()).into(holder.recImage)
        holder.recKelas.text = dataList[position].getKelas()
        holder.recMapel.text = dataList[position].getMapel()
        holder.recGuru.text = dataList[position].getGuru()

        firebaseMessagingHelper.subscribeTopics(dataList[position].getKey()!!)

        holder.recCard.setOnClickListener {
            val arguments = Bundle()
            arguments.putString("key", dataList[holder.adapterPosition].getKey())
            arguments.putString("mapel", dataList[holder.adapterPosition].getMapel())

            val fragment: Fragment = MateriFragment()
            fragment.arguments = arguments

            val transaction = fa.requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frag_home, fragment)

            dataList.clear()
            notifyDataSetChanged()
            transaction.disallowAddToBackStack()
            transaction.commit()
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}

class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//    var recImage: ImageView
    var recKelas: TextView
    var recMapel: TextView
    var recGuru: TextView
    var recCard: CardView

    init {
//        recImage = itemView.findViewById(R.id.recImage)
        recCard = itemView.findViewById(R.id.recCard)
        recKelas = itemView.findViewById(R.id.recKelas)
        recMapel = itemView.findViewById(R.id.recMapel)
        recGuru = itemView.findViewById(R.id.recGuru)
    }
}