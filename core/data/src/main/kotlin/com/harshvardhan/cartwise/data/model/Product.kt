package com.harshvardhan.cartwise.data.model

import com.harshvardhan.cartwise.pricing.Money

data class Product(
    val id: Long,
    val title: String,
    val description: String,
    val category: String,
    val brand: String?,
    val price: Money,
    val rating: Double,
    val stock: Int,
    val thumbnail: String,
    val images: List<String>,
)
