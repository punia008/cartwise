package com.harshvardhan.cartwise.data.network

import kotlinx.serialization.Serializable

@Serializable
data class ProductsResponse(
    val products: List<NetworkProduct>,
    val total: Int,
    val skip: Int,
    val limit: Int,
)

@Serializable
data class NetworkProduct(
    val id: Long,
    val title: String,
    val description: String = "",
    val category: String = "",
    val brand: String? = null,
    val price: Double,
    val rating: Double = 0.0,
    val stock: Int = 0,
    val thumbnail: String = "",
    val images: List<String> = emptyList(),
)
