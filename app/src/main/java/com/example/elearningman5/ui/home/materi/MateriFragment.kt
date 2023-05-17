package com.example.elearningman5.ui.home.materi

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.elearningman5.Http
import com.example.elearningman5.R
import com.example.elearningman5.databinding.FragmentMateriBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONException
import org.json.JSONObject
import java.util.*

open class MateriFragment : Fragment() {
    private var _binding: FragmentMateriBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    //////////////////
    private var dataList: ArrayList<DataMateriClass>? = null
    private var adapter: MyAdapterMateri? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMateriBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val bundle = this.arguments

        ///////////////
        if (bundle != null) {
            Log.d("TAG Tugas Fragment", bundle.getString("key").toString())
            Log.d("TAG Tugas Fragment", bundle.getString("mapel").toString())
        }
        
        val recyclerView: RecyclerView = binding.recyclerViewTugas
        val gridLayoutManager = GridLayoutManager(context, 1)
        recyclerView.layoutManager = gridLayoutManager

        val builder = context?.let { AlertDialog.Builder(it) }
        builder?.setCancelable(false)
        builder?.setView(R.layout.progress_layout)
        val dialog = builder?.create()
        dialog?.show()

        dataList = ArrayList()
        adapter = context?.let { MyAdapterMateri(it, dataList!!) }
        recyclerView.adapter = adapter
        dialog?.show()

        dialog?.let { bundle?.let { it1 -> getMateri(it, it1) } }

        binding.arrowBack.setOnClickListener {
            val navController = findNavController()
            navController.run {
                onDestroyView()
                popBackStack()
                navigate(R.id.navigation_home)
            }
        }
        ///////////////

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    ///////////////
    @SuppressLint("NotifyDataSetChanged")
    private fun getMateri(it: AlertDialog, it1: Bundle) {
        val params = JSONObject()
        try {
            params.put("jadwal_id", it1.getString("key").toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val data = params.toString()
        val url = getString(R.string.api_server) + "/siswa/viewMateri"

        Thread {
            val http = Http(context, url)
            http.setMethod("post")
            http.setData(data)
            http.send()

            activity?.runOnUiThread {
                dataList?.clear()

                val code = http.getStatusCode()
                if (code == 200) {
                    try {
                        val response =
                            http.getResponse()?.let { JSONObject(it).getJSONArray("data").toString() }

                        if (response!! == "[]") {
                            Toast.makeText(context, "TIDAK ADA MATERI", Toast.LENGTH_SHORT).show()
                            val navController = findNavController()

                            navController.run {
                                onDestroyView()
                                popBackStack()
                                navigate(R.id.navigation_home)
                            }
                        } else {
                            val gson = Gson()
                            val dataClass = object : TypeToken<ArrayList<DataMateriClass>>() {}.type

                            val addData: ArrayList<DataMateriClass> = gson.fromJson(response, dataClass)

                            dataList?.addAll(addData)
                            adapter?.notifyDataSetChanged()
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    it.dismiss()
                } else {
                    it.dismiss()
                    Toast.makeText(context, "Error $code", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }
}