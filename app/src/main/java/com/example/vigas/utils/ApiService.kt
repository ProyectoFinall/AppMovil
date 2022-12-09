package com.example.vigas.utils

import com.example.vigas.models.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @POST("Usuarios/Login")
    fun login(@Body bodyLogin: BodyLogin): Call<LoginResponse>

    @GET("Proceso")
    fun getProcess(): Call<List<ProcessResponse>>

    @GET("Defectos")
    fun getDefects(): Call<List<DefectResponse>>

    @GET("Viga/GetById")
    fun getVigaById(@Query("ClvViga") clvViga: Int): Call<VigaResponse>

    @POST("Viga")
    fun updateViga(@Body body : UpdateViga): Call<VigaResponse>

}