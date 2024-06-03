package com.itcm.rodriguez.maria.productos.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itcm.rodriguez.maria.productos.models.Document
import com.itcm.rodriguez.maria.productos.models.Product
import com.itcm.rodriguez.maria.productos.models.ProductDto
import com.itcm.rodriguez.maria.productos.models.ProductPhoto
import com.itcm.rodriguez.maria.productos.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    var showDialog = mutableStateOf(false)
    var productToDelete = mutableStateOf<Product?>(null)

    init {
        fetchProducts()
    }

    fun fetchProducts() {
        viewModelScope.launch {
            try {
                val products = RetrofitClient.apiService.getProducts()
                _products.value = products
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            }
        }
    }

    private fun deleteProduct(productId: Int) {
        viewModelScope.launch {
            try {
                RetrofitClient.apiService.deleteProduct(productId)
                fetchProducts() // Refresh the product list after deletion
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            }
        }
    }

    fun addProduct(
        name: String,
        number: String,
        photos: List<ProductPhoto>,
        documents: List<Document>
    ) {
        viewModelScope.launch {
            try {
                val newProduct = ProductDto(
                    productName = name,
                    productNumber = number,
                    photos = photos,
                    documents = documents
                )
                RetrofitClient.apiService.createProduct(newProduct)
                fetchProducts() // Refresh the product list after adding
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            }
        }
    }

    fun editProduct(
        id: Int,
        name: String,
        number: String,
        photos: List<ProductPhoto>,
        documents: List<Document>
    ) {
        viewModelScope.launch {
            try {
                val updatedProduct = ProductDto(
                    productName = name,
                    productNumber = number,
                    photos = photos,
                    documents = documents
                )
                RetrofitClient.apiService.updateProduct(id, updatedProduct)
                fetchProducts() // Refresh the product list after editing
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            }
        }
    }

    fun confirmDeletion(product: Product) {
        productToDelete.value = product
        showDialog.value = true
    }

    fun cancelDeletion() {
        productToDelete.value = null
        showDialog.value = false
    }

    fun performDeletion() {
        productToDelete.value?.let {
            deleteProduct(it.productId)
        }
        productToDelete.value = null
        showDialog.value = false
    }

    fun refreshProducts() {
        fetchProducts()
    }
}
