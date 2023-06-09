package com.example.elearningman5.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.elearningman5.Http
import com.example.elearningman5.LocalStorage
import com.example.elearningman5.MainActivity
import com.example.elearningman5.R
import com.example.elearningman5.databinding.FragmentHomeBinding
import com.example.elearningman5.pelengkap.kode401
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONException
import org.json.JSONObject

@Suppress("DEPRECATION")
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var localStorage: LocalStorage

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    //////////////////
    private lateinit var dataList: ArrayList <DataClass>
    private var adapter: MyAdapter? = null

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //////////////////
        localStorage = LocalStorage(this.context)

        val recyclerView: RecyclerView = binding.recyclerView
        val gridLayoutManager = GridLayoutManager(context, 1)
        recyclerView.layoutManager = gridLayoutManager

        val builder = context?.let { AlertDialog.Builder(it) }
        builder?.setCancelable(false)
        builder?.setView(R.layout.progress_layout)
        val dialog = builder?.create()
        dialog?.show()

        dataList = ArrayList()
        adapter = context?.let { parentFragment?.let { it1 -> MyAdapter(it, dataList, it1) } }
        recyclerView.adapter = adapter
        dialog?.show()

        dialog?.let { getDataMapel(it) }
        //////////////////

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //////////////////
    @SuppressLint("NotifyDataSetChanged")
    private fun getDataMapel(dialog: AlertDialog) {
        val params = JSONObject()
        try {
            params.put("nis", localStorage.getNis().toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val data = params.toString()
        val url = getString(R.string.api_server) + "/siswa/listPelajaran"

        Thread {
            val http = Http(context, url)
            http.setMethod("post")
            http.setToken(true)
            http.setData(data)
            http.send()

            activity?.runOnUiThread {
                dataList.clear()

                val code = http.getStatusCode()
                val response = http.getResponse()?.let { JSONObject(it) }
                when (code) {
                    200 -> {
                        try {
                            val gson = Gson()

                            val dataClass = object : TypeToken<ArrayList<DataClass>>() {}.type
                            val addDataMapel: ArrayList<DataClass> = gson.fromJson(
                                response?.getJSONArray("data").toString(), dataClass)

                            dataList.addAll(addDataMapel)
                            adapter!!.notifyDataSetChanged()
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                    401 -> {
                        context?.let { it1 -> kode401(response!!.getString("message"), it1) }
                        localStorage.setToken("")

                        startActivity(Intent(context, MainActivity::class.java))
                        requireActivity().finish()
                    }
                    else -> {
                        Toast.makeText(context, "Error $code (${ response?.getString("message")})", Toast.LENGTH_SHORT).show()
                    }
                }
                dialog.dismiss()
            }
        }.start()
    }
}