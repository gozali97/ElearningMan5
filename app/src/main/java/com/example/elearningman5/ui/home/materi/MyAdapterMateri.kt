package com.example.elearningman5.ui.home.materi

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.elearningman5.R
import com.example.elearningman5.ui.home.tugas.TugasActivity
import com.example.elearningman5.String2Date
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
        holder.recCreat.text = dataList[position].getWaktu()
            .String2Date("yyyy-MM-dd'T'HH:mm:ss")
            ?.let {
                SimpleDateFormat("EEEE hh:mm a, dd MMMM yyyy", Locale("id", "ID"))
                    .format(it).toString()
            }
        Log.d("TAG waktu", dataList[holder.adapterPosition].getWaktu().toString())

        if (dataList[position].getKeyTugas() == null) {
            holder.recCardTugas.removeAllViews()
        }

        holder.recDownload.setOnClickListener {
            val file = dataList[holder.adapterPosition].getFile()
            if (! file?.isEmpty()!!) {
                download(context.getString(R.string.api_server).replace("/api", "/assets/dokumen/$file"), file)
//                download("http://192.168.1.9:8000/assets/dokumen/$file", file)
            }
        }

        holder.recCardTugas.setOnClickListener {
            getActivity(context)?.let{
                it.startActivity(Intent (it, TugasActivity::class.java)
                    .putExtra("keytugas", dataList[holder.adapterPosition].getKeyTugas())
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    private fun download(url: String, filename: String) {
            val request = DownloadManager.Request(Uri.parse(url))
                .setDescription("Downloading...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedOverMetered(true)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)

            val dm = context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)
    }
}

class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    //    var recImage: ImageView
    var recMateri: TextView
    var recDesc: TextView
    var recCreat: TextView
    var recDownload: CardView
    var recCardTugas: CardView //http://192.168.1.9:8000/assets/tugas//Vektor.pdf

    init {
//        recImage = itemView.findViewById(R.id.recImage)
        recDownload = itemView.findViewById(R.id.recCardDownload)
        recCardTugas = itemView.findViewById(R.id.recCardTugas)
        recCreat = itemView.findViewById(R.id.recCreat)
        recMateri = itemView.findViewById(R.id.recMateri)
        recDesc = itemView.findViewById(R.id.recDesMateri)
    }
}