package com.itcm.rodriguez.maria.productos.network

import com.itcm.rodriguez.maria.productos.models.Product
import com.itcm.rodriguez.maria.productos.models.ProductDto
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    //Se define el metodo para realizar la solicitud HTTP Get
    @GET("Product/getProducts")
    suspend fun getProducts(): List<Product>

    //Se define el metodo para realizar la solicitud HTTP Delete
    @DELETE("Product/deleteProduct/{id}")
    suspend fun deleteProduct(@Path("id") productId: Int): List<Product>

    //Se define el metodo para realizar la solicitud HTTP Post
    @POST("Product/createProduct")
    suspend fun createProduct(@Body product: ProductDto): Product

    //Se define el metodo para realizar la solicitud HTTP Put
    @PUT("Product/updateProduct/{id}")
    suspend fun updateProduct(@Path("id") id: Int, @Body product: ProductDto): Product
}

object RetrofitClient {
    //URL base de la API
    private const val BASE_URL = "https://composed-unicorn-marginally.ngrok-free.app/api/"

    //Inicializa el servicio API usando Retrofit
    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            //Convertidor Gson a JSON
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            //Crea una implementación de la interfaz ApiService
            .create(ApiService::class.java)
    }
}
