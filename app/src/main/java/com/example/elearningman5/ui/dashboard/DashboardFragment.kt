package com.example.elearningman5.ui.dashboard

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.elearningman5.*
import com.example.elearningman5.databinding.FragmentDashboardBinding
import com.squareup.picasso.Picasso
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private lateinit var localStorage: LocalStorage

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //////////////
        localStorage = LocalStorage(this.context)
        val builder = context?.let { AlertDialog.Builder(it) }
        val dialog = builder?.create()

        builder?.setCancelable(false)
        builder?.setView(R.layout.progress_layout)

        dialog?.show()
        dialog?.let { getUser(it) }

        dialog?.show()
        binding.btnLogout.setOnClickListener{
            val url = getString(R.string.api_server) + "/logout"
            Thread {
                val http = Http(context, url)
                http.setMethod("post")
                http.send()

                activity?.runOnUiThread {
                    val code = http.getStatusCode()
                    if (code == 200) {
                        try {
                            localStorage.setEmail("")
                            localStorage.setNis("")
                            localStorage.setSesi("")

                            startActivity(Intent(context, MainActivity::class.java))
                            requireActivity().finish()
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                        dialog?.dismiss()
                    } else {
                        dialog?.dismiss()
                        Toast.makeText(context, "Error $code", Toast.LENGTH_SHORT).show()
                    }
                }

            }.start()
        }
        //////////////

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("SetTextI18n")
    private fun getUser(it: AlertDialog) {
        val params = JSONObject()
        try {
            params.put("email", localStorage.getEmail().toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val data = params.toString()
        val url = getString(R.string.api_server) + "/profil"

        Thread {
            val http = Http(context, url)
            http.setMethod("post")
            http.setData(data)
            http.send()
            activity?.runOnUiThread {
                val code = http.getStatusCode()
                if (code == 200) {
                    try {
                        val response =
                            http.getResponse()?.let { JSONObject(it).getJSONObject("data") }

                        Picasso.with(context)
                            .load(getString(R.string.api_server)
                                .replace("/api",
                                    "/assets/img/${response?.getString("gambar")}"))
                            .error(R.drawable.baseline_person_24)
                            .resize(300, 300)
                            .centerCrop()
                            .into(binding.recProfile)

                        binding.textNama.text =
                            "Nama : ${response?.getString("name")
                                ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }}"
                        binding.textNis.text = "NIS : ${ response?.getString("nis") }"

                        binding.textEmailSiswa.text = " Email : ${ response?.getString("email") }"
                        binding.textNoHp.text = "No HP : ${ response?.getString("no_hp") }"

                        binding.textKelas.text = "Kelas : ${ response?.getString("nama_kelas")?.uppercase() }"
                        binding.textJurusan.text = "Jurusan : ${ response?.getString("nama_jurusan")?.uppercase() }"

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