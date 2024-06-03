package com.itcm.rodriguez.maria.productos.models

data class Product(
    val productId: Int,
    val productName: String,
    val productNumber: String,
    val photos: List<ProductPhoto>,
    val documents: List<Document>
)

data class ProductDto(
    val productName: String,
    val productNumber: String,
    val photos: List<ProductPhoto>,
    val documents: List<Document>
)

data class ProductPhoto(
    val largePhoto: String,
    val productId: Int
)

data class Document(
    val documentId: Int = 0,
    val title: String,
    val fileName: String,
    val fileExtension: String,
    val revision: String,
    val documentFile: String,
    val productId: Int
)
