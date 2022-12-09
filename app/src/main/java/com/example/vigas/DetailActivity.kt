package com.example.vigas

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.vigas.databinding.ActivityDetailBinding
import com.example.vigas.models.DefectResponse
import com.example.vigas.models.ProcessResponse
import com.example.vigas.models.UpdateViga
import com.example.vigas.models.VigaResponse
import com.example.vigas.utils.ApiAdapter
import com.example.vigas.utils.Constants
import com.google.android.material.navigation.NavigationBarView.OnItemSelectedListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.reflect.Type

class DetailActivity : AppCompatActivity() {

    private var binding: ActivityDetailBinding? = null

    private var infoViga: VigaResponse? = null
    private var process: MutableList<ProcessResponse> = mutableListOf()
    private var defects: MutableList<DefectResponse> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        val id = this.intent.extras?.getInt(Constants.ID_VALUE)

        val sharedPreferences =
            getSharedPreferences(Constants.SHARED_NAME, Context.MODE_PRIVATE)
        val stringProcess = sharedPreferences.getString(Constants.PROCESS, "")
        val stringDefects = sharedPreferences.getString(Constants.DEFECTS, "")
        val typeTokenProcess: Type = object : TypeToken<List<ProcessResponse>>() {}.type
        val listProcess = Gson().fromJson<List<ProcessResponse>>(stringProcess, typeTokenProcess)
        process = listProcess.toMutableList()
        val typeTokenDefects: Type = object : TypeToken<List<DefectResponse>>() {}.type
        val listDefects = Gson().fromJson<List<DefectResponse>>(stringDefects, typeTokenDefects)
        defects = listDefects.toMutableList()
        val mutableList = mutableListOf<String>()
        listDefects.forEach {
            mutableList.add(it.descripcion)
        }
        initSpinner(mutableList)
        val call = id?.let { ApiAdapter().getApiService()?.getVigaById(it) }
        call?.enqueue(object : Callback<VigaResponse> {
            override fun onResponse(
                call: Call<VigaResponse>,
                response: Response<VigaResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val info = response.body()!!
                    infoViga = info
                    binding?.apply {
                        largoText.text =
                            "${info.largoViga} ${getString(R.string.metros)}"
                        pesoText.text =
                            "${info.pesoViga} ${getString(R.string.kilogramos)}"
                        materialText.text = "${info.material}"
                        val procesoActual =
                            listProcess.find { it.clvProceso == info.clvProceso }
                        val position = listProcess.indexOf(procesoActual)
                        setDefecto(info)
                        if (info.clvProceso == null) {
                            hideRegresar()
                            actualizar.setOnClickListener {
                                setProcess(info.clvViga, null, listProcess[0].clvProceso)
                            }
                        } else if (info.clvDefecto != null) {
                            hideRegresar()
                            actualizar.setOnClickListener {
                                setProcess(info.clvViga, null, listProcess[position].clvProceso)
                            }
                        } else if (position == listProcess.size - 1) {
                            layoutActualizar.visibility = View.GONE
                            hideRegresar()
                            successMessage.visibility = View.VISIBLE
                            successImage.visibility = View.VISIBLE
                        } else {
                            setBotonRegresar()
                            actualizar.setOnClickListener {
                                if (listProcess.size >= position + 1) {
                                    setProcess(
                                        info.clvViga,
                                        null,
                                        listProcess[position + 1].clvProceso
                                    )
                                }
                            }
                        }
                    }
                } else {
                    showError(getString(R.string.error_4))
                }
            }

            override fun onFailure(call: Call<VigaResponse>, t: Throwable) {
                showError(getString(R.string.error_4))
            }
        })
    }

    private fun setDefecto(info: VigaResponse) {
        binding?.apply {
            if (info.clvDefecto != null) {
                val defectoActual =
                    defects.find { it.clvDefecto == info.clvDefecto }
                defectoText.text = defectoActual?.descripcion
            } else {
                defectoText.text = getString(R.string.sin_defecto)
            }
        }
    }

    private fun hideRegresar() {
        binding?.apply {
            botonesRegresar.visibility = View.GONE
            textDefecto.visibility = View.GONE
            spinner.visibility = View.GONE
        }
    }

    private fun setBotonRegresar() {
        binding?.apply {
            botonesRegresar.visibility = View.VISIBLE
            textDefecto.visibility = View.GONE
            spinner.visibility = View.GONE
            confirmar.visibility = View.GONE
            regresar.setOnClickListener {
                spinner.visibility = View.VISIBLE
                textDefecto.visibility = View.VISIBLE
            }
        }
    }

    private fun initSpinner(options: MutableList<String>) {
        options.add(0, getString(R.string.selecciona_el_defecto_que_tiene_la_viga))
        val adapterSpinner = ArrayAdapter(this, R.layout.spinner, options)
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding?.spinner?.apply {
            this.adapter = adapterSpinner
            onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    positionSpinner: Int,
                    p3: Long
                ) {
                    if (positionSpinner > 0) {
                        binding?.apply {
                            confirmar.visibility = View.VISIBLE
                            confirmar.setOnClickListener {
                                val procesoActual =
                                    process.find { it.clvProceso == infoViga?.clvProceso }
                                val position = process.indexOf(procesoActual)
                                val defecto = defects[positionSpinner - 1]
                                infoViga?.let {
                                    setProcess(
                                        it.clvViga,
                                        defecto.clvDefecto,
                                        process[position].clvProceso
                                    )
                                }
                            }
                        }
                    } else {
                        binding?.confirmar?.visibility = View.GONE
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }
        }
    }

    private fun setProcess(clvViga: Int, clvDefecto: Int?, clvProceso: Int) {
        val sharedPreferences =
            getSharedPreferences(Constants.SHARED_NAME, Context.MODE_PRIVATE)
        val numEmpleado = sharedPreferences.getInt(Constants.NUM_EMPLOYEE, 0)
        val call = ApiAdapter().getApiService()?.updateViga(
            UpdateViga(clvViga, numEmpleado, clvDefecto, clvProceso)
        )
        call?.enqueue(object : Callback<VigaResponse> {
            override fun onResponse(call: Call<VigaResponse>, response: Response<VigaResponse>) {
                val proceso = process.find { it.clvProceso == clvProceso }
                val message = "${getString(R.string.show_success)} ${proceso?.procesoNombre}"
                if (response.isSuccessful && response.body() != null) {
                    Toast.makeText(
                        applicationContext,
                        message,
                        Toast.LENGTH_LONG
                    ).show()
                    Handler().postDelayed({
                        finish()
                    }, 1000)
                } else {
                    showError(getString(R.string.error_5))
                }
            }

            override fun onFailure(call: Call<VigaResponse>, t: Throwable) {
                showError(getString(R.string.error_5))
            }

        })
    }

    private fun showError(message: String) {
        Toast.makeText(
            applicationContext,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

}