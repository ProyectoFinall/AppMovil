package com.example.vigas

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import com.example.vigas.databinding.ActivityMainBinding
import com.example.vigas.models.DefectResponse
import com.example.vigas.models.ProcessResponse
import com.example.vigas.utils.Constants
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null

    private val getResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                val value = it.data?.getIntExtra(Constants.ID_VALUE, 0)
                val intent = Intent(applicationContext, DetailActivity::class.java)
                intent.putExtra(Constants.ID_VALUE, value)
                startActivity(intent)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        binding?.scanner?.setOnClickListener {
            getResult.launch(Intent(applicationContext, ScannerQRActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.close_session -> {
                val sharedPreferences =
                    getSharedPreferences(Constants.SHARED_NAME, Context.MODE_PRIVATE)
                sharedPreferences.edit().clear().apply()
                startActivity(Intent(applicationContext, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}