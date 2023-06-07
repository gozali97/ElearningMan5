package com.example.elearningman5.ui.home.materi

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.elearningman5.pelengkap.DownloadFile
import com.example.elearningman5.R
import com.example.elearningman5.pelengkap.utcToWib
import com.example.elearningman5.ui.home.tugas.TugasActivity
import com.example.elearningman5.ui.home.chat.ForumActivity
import com.google.android.material.internal.ContextUtils.getActivity
import java.text.SimpleDateFormat
import java.util.*


class MyAdapterMateri(
    private val context: Context,
    dataList: ArrayList<DataMateriClass>
) : RecyclerView.Adapter<MyViewHolder>() {
    private var dataList: ArrayList<DataMateriClass>

    init {
        this.dataList = dataList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.recycler_materi, parent, false)
        return MyViewHolder(view)
    }

    @SuppressLint("ResourceType")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
//        Glide.with(context).load(dataList[position].getDataImage()).into(holder.recImage)
        holder.recMateri.text = dataList[position].getMateri()
        holder.recDesc.text = dataList[position].getDesc()
        holder.recCreat.text = utcToWib(dataList[position].getWaktu()!!)?.let {
                SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
                    .format(it).toString()
            }

        if (dataList[position].getKeyTugas() == null) {
            holder.recCardTugas.removeAllViews()
            holder.recCardChat.removeAllViews()
        }

        holder.recDownload.setOnClickListener {
            val files = dataList[holder.adapterPosition].getFile()

            if (! files?.isEmpty()!!) {
                val file = files.split(",")
                for (f in file) {
                    DownloadFile.download(context, context.getString(R.string.api_server)
                        .replace("/api", "/assets/dokumen/$f"), f)
                }
            }
        }

        holder.recCardTugas.setOnClickListener {
            getActivity(context)?.let{
                it.startActivity(Intent (it, TugasActivity::class.java)
                    .putExtra("key_tugas", dataList[holder.adapterPosition].getKeyTugas())
                    .putExtra("nama_materi", dataList[holder.adapterPosition].getMateri())
                )
            }
        }

        holder.recCardChat.setOnClickListener {
            getActivity(context)?.let{
                it.startActivity(Intent (it, ForumActivity::class.java)
                    .putExtra("key_chat", dataList[holder.adapterPosition].getKeyMateri())
                    .putExtra("nama_materi", dataList[holder.adapterPosition].getMateri())
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onViewDetachedFromWindow(holder: MyViewHolder) {
        super.onViewDetachedFromWindow(holder)
        DownloadFile.unregisterDownloadCompleteReceiver(context)
    }
}

class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    //    var recImage: ImageView
    var recMateri: TextView
    var recDesc: TextView
    var recCreat: TextView
    var recDownload: CardView
    var recCardTugas: CardView
    var recCardChat: CardView

    init {
//        recImage = itemView.findViewById(R.id.recImage)
        recDownload = itemView.findViewById(R.id.recCardDownload)
        recCardTugas = itemView.findViewById(R.id.recCardTugas)
        recCardChat = itemView.findViewById(R.id.recCardChat)
        recCreat = itemView.findViewById(R.id.recCreat)
        recMateri = itemView.findViewById(R.id.recMateri)
        recDesc = itemView.findViewById(R.id.recDesMateri)
    }
}