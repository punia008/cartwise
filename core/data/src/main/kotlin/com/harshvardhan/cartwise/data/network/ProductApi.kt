package com.harshvardhan.cartwise.data.network

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** https://dummyjson.com/docs/products */
interface ProductApi {

    @GET("products")
    suspend fun products(
        @Query("limit") limit: Int,
        @Query("skip") skip: Int,
        @Query("select") select: String = FIELDS,
    ): ProductsResponse

    @GET("products/{id}")
    suspend fun product(@Path("id") id: Long): NetworkProduct

    companion object {
        const val BASE_URL = "https://dummyjson.com/"

        // Ask only for what the app shows: drops reviews, dimensions, meta etc. from every page.
        const val FIELDS = "id,title,description,category,brand,price,rating,stock,thumbnail,images"
    }
}
