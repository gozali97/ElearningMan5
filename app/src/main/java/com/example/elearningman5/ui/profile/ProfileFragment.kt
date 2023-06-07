package com.example.elearningman5.ui.profile

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.elearningman5.*
import com.example.elearningman5.databinding.FragmentProfileBinding
import com.example.elearningman5.ui.profile.changepass.ChangePasswordActivity
import com.example.elearningman5.ui.profile.editprofile.EditProfileActivity
import com.squareup.picasso.Picasso
import org.json.JSONException
import org.json.JSONObject
import java.util.*


//Desain UI: https://github.com/theindianappguy/SampleProfileUi
class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private lateinit var localStorage: LocalStorage

    private var id: String? = null
    private var email: String? = null

    companion object {
        private const val REQUEST_CODE = 1
    }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        localStorage = LocalStorage(this.context)
        getUser()

        binding.btnEditProfile.setOnClickListener {
            openActivityForResult()
        }

        binding.btnChangePassword.setOnClickListener {
            activity?.let{
                it.startActivity(Intent (it, ChangePasswordActivity::class.java)
                    .putExtra("id", id)
                    .putExtra("email", email)
                )
            }
        }

        binding.btnLogout.setOnClickListener {
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
                    } else {
                        Toast.makeText(context, "Error $code", Toast.LENGTH_SHORT).show()
                    }
                }

            }.start()
        }

        return root
    }

    @Suppress("DEPRECATION")
    private fun openActivityForResult() {
        val intent = Intent(activity, EditProfileActivity::class.java).putExtra("email", email)
        startActivityForResult(intent, REQUEST_CODE)
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Tangkap data dari activity dan lakukan pemrosesan sesuai kebutuhan
            Log.d("TAG, onActivityResult: ", data?.getStringExtra("result_key").toString())
            if (data?.getStringExtra("result_key").toString() == "refresh")
                getUser()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("SetTextI18n")
    private fun getUser() {
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
                            .error(R.drawable.profile_user)
                            .resize(300, 300)
                            .centerCrop()
                            .skipMemoryCache()
                            .into(binding.recProfile)

                        binding.textNama.text =
                            "${response?.getString("name")
                                ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }}"
                        binding.textNis.text = "${ response?.getString("nis") }"

                        id = response?.getString("id")
                        email = response?.getString("email")

                        binding.textEmailSiswa.text = "Email : $email"
                        binding.textNoHp.text = "No. Hp : ${ response?.getString("no_hp") }"

                        binding.textKelas.text = "Kelas : ${ response?.getString("nama_kelas")?.uppercase() }"
                        binding.textJurusan.text = "Jurusan : ${ response?.getString("nama_jurusan")?.uppercase() }"
                        binding.textAlamat.text = response?.getString("alamat")
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                } else {
                    Toast.makeText(context, "Error $code", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }
}