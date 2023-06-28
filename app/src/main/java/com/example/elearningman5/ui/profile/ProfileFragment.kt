package com.example.elearningman5.ui.profile

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.elearningman5.*
import com.example.elearningman5.databinding.FragmentProfileBinding
import com.example.elearningman5.firebase.FirebaseMessagingHelper
import com.example.elearningman5.pelengkap.kode401
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
                it.startActivity(Intent (it, ChangePasswordActivity::class.java))
            }
        }

        binding.btnLogout.setOnClickListener {
            val url = getString(R.string.api_server) + "/logout"
            Thread {
                val http = Http(context, url)
                http.setMethod("post")
                http.setToken(true)
                http.send()

                activity?.runOnUiThread {
                    val code = http.getStatusCode()
                    val response = http.getResponse()?.let { JSONObject(it) }
                    when (code) {
                        200 -> {
                            try {
                                localStorage.setEmail("")
                                localStorage.setNis("")
                                localStorage.setSesi("")
                                localStorage.setToken("")

                                val firebaseMessagingHelper = context?.let { it1 ->
                                    FirebaseMessagingHelper(
                                        it1
                                    )
                                }
                                firebaseMessagingHelper?.unsubscribeTopics()

                                startActivity(Intent(context, MainActivity::class.java))
                                requireActivity().finish()
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
                            Toast.makeText(context, "Error $code (${ response?.getString("message") })", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            }.start()
        }

        return root
    }

    @Suppress("DEPRECATION")
    private fun openActivityForResult() {
        val intent = Intent(activity, EditProfileActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE)
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Tangkap data dari activity dan lakukan pemrosesan sesuai kebutuhan
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
        val url = getString(R.string.api_server) + "/profile"

        Thread {
            val http = Http(context, url)
            http.setMethod("post")
            http.setToken(true)
            http.send()
            activity?.runOnUiThread {
                val code = http.getStatusCode()
                val response = http.getResponse()?.let { JSONObject(it) }
                when (code) {
                    200 -> {
                        try {
                            val dataProfile = response?.getJSONObject("data")

                            Picasso.with(context)
                                .load(getString(R.string.api_server)
                                    .replace("/api",
                                        "/assets/img/${dataProfile?.getString("gambar")}"))
                                .error(R.drawable.profile_user)
                                .resize(300, 300)
                                .centerCrop()
                                .skipMemoryCache()
                                .into(binding.recProfile)

                            binding.textNama.text =
                                "${dataProfile?.getString("name")
                                    ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }}"
                            binding.textNis.text = "${ dataProfile?.getString("nis") }"

                            id = dataProfile?.getString("id")
                            email = dataProfile?.getString("email")

                            binding.textEmailSiswa.text = "Email : $email"
                            binding.textNoHp.text = "No. Hp : ${ dataProfile?.getString("no_hp") }"

                            binding.textKelas.text = "Kelas : ${ dataProfile?.getString("nama_kelas")?.uppercase() }"
                            binding.textJurusan.text = "Jurusan : ${ dataProfile?.getString("nama_jurusan")?.uppercase() }"
                            binding.textAlamat.text = dataProfile?.getString("alamat")
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
            }
        }.start()
    }
}