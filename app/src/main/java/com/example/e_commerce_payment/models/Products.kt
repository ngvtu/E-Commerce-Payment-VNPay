package com.example.e_commerce_payment.models

data class Products(
    val categoryId: Int,
    val createdAt: String,
    val discountPrice: Int,
    val id: Int,
    val product3DModelPath: String,
    val productCode: String,
    val productColor: String,
    val productDescription: String,
    val productImg: String,
    val productName: String,
    val productSize: String,
    val productSlug: String,
    val productThumbnail: String,
    var sellingPrice: Int,
    val updatedAt: String
)