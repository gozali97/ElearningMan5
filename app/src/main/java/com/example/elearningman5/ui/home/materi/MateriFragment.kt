package com.example.elearningman5.ui.home.materi

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.elearningman5.Http
import com.example.elearningman5.LocalStorage
import com.example.elearningman5.MainActivity
import com.example.elearningman5.R
import com.example.elearningman5.databinding.FragmentMateriBinding
import com.example.elearningman5.pelengkap.kode401
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

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMateriBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val bundle = this.arguments

        ///////////////
        // Access the toolbarTitle view
        val toolbarTitle: AppCompatTextView? = activity?.findViewById(R.id.toolbarTitle)

        // Set the text of the toolbarTitle TextView
        if (bundle != null) {
            toolbarTitle?.text = "Materi " + bundle.getString("mapel").toString()
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
            http.setToken(true)
            http.setData(data)
            http.send()

            activity?.runOnUiThread {
                dataList?.clear()
                val res = http.getResponse()?.let { JSONObject(it) }

                when (val code = http.getStatusCode()) {
                    200 -> {
                        try {
                            val response = res?.getJSONArray("data").toString()

                            if (response == "[]") {
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
                    }
                    401 -> {
                        context?.let { it -> kode401(res!!.getString("message"), it) }
                        LocalStorage(context).setToken("")

                        startActivity(Intent(context, MainActivity::class.java))
                        requireActivity().finish()
                    }
                    else -> {
                        Toast.makeText(context, "Error $code (${ res!!.getString("message") })", Toast.LENGTH_SHORT).show()
                    }
                }
                it.dismiss()
            }
        }.start()
    }
}