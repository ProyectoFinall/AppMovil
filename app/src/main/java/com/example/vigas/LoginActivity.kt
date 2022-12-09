package com.example.vigas

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.vigas.databinding.ActivityLoginBinding
import com.example.vigas.models.BodyLogin
import com.example.vigas.models.DefectResponse
import com.example.vigas.models.LoginResponse
import com.example.vigas.models.ProcessResponse
import com.example.vigas.utils.ApiAdapter
import com.example.vigas.utils.Constants
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private var binding: ActivityLoginBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        initViews()
    }

    private fun initViews() {
        val sharedPreferences =
            getSharedPreferences(Constants.SHARED_NAME, Context.MODE_PRIVATE)
        val isSessionStarted = sharedPreferences.getBoolean(Constants.SESSION_IS_STARTED, false)
        if (isSessionStarted) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        binding?.apply {
            button.setOnClickListener {
                if (number.text.toString().isNotEmpty() && password.text.toString().isNotEmpty()) {
                    val imm: InputMethodManager =
                        getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(button.windowToken, 0)
                    val call = ApiAdapter().getApiService()
                        ?.login(BodyLogin(number.text.toString().toInt(), password.text.toString()))
                    call?.enqueue(object : Callback<LoginResponse> {
                        override fun onResponse(
                            call: Call<LoginResponse>,
                            response: Response<LoginResponse>
                        ) {
                            if (response.isSuccessful) {
                                getProcess()
                            } else {
                                showError(getString(R.string.error_1))
                            }
                        }

                        override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                            showError(getString(R.string.error_1))
                        }
                    })
                } else {
                    showError(getString(R.string.error_2))
                }
            }
        }
    }

    private fun getProcess() {
        val call = ApiAdapter().getApiService()?.getProcess()
        call?.enqueue(object : Callback<List<ProcessResponse>> {
            override fun onResponse(
                call: Call<List<ProcessResponse>>,
                response: Response<List<ProcessResponse>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    saveProcess(response.body()!!)
                } else {
                    showError(getString(R.string.error_3))
                }
            }

            override fun onFailure(call: Call<List<ProcessResponse>>, t: Throwable) {
                showError(getString(R.string.error_3))
            }

        })
    }

    private fun saveProcess(list: List<ProcessResponse>) {
        val sharedPreferences =
            getSharedPreferences(Constants.SHARED_NAME, Context.MODE_PRIVATE)
        val stringProcess = Gson().toJson(list)
        sharedPreferences.edit().putString(Constants.PROCESS, stringProcess).apply()
        getDefects()
    }

    private fun getDefects() {
        val call = ApiAdapter().getApiService()?.getDefects()
        call?.enqueue(object : Callback<List<DefectResponse>> {
            override fun onResponse(
                call: Call<List<DefectResponse>>,
                response: Response<List<DefectResponse>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    saveDefects(response.body()!!)
                } else {
                    showError(getString(R.string.error_3))
                }
            }

            override fun onFailure(call: Call<List<DefectResponse>>, t: Throwable) {
                showError(getString(R.string.error_3))
            }
        })
    }

    private fun saveDefects(list: List<DefectResponse>) {
        val sharedPreferences =
            getSharedPreferences(Constants.SHARED_NAME, Context.MODE_PRIVATE)
        val stringProcess = Gson().toJson(list)
        sharedPreferences.edit().putString(Constants.DEFECTS, stringProcess).apply()
        saveSession()
    }

    private fun saveSession() {
        val sharedPreferences =
            getSharedPreferences(Constants.SHARED_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt(
            Constants.NUM_EMPLOYEE,
            binding?.number?.text.toString().toInt()
        )
        editor.putBoolean(Constants.SESSION_IS_STARTED, true)
        editor.apply()
        startActivity(Intent(applicationContext, MainActivity::class.java))
        finish()
    }

    private fun showError(message: String) {
        Toast.makeText(
            applicationContext,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

}
