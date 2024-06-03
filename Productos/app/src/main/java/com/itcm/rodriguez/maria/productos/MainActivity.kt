package com.itcm.rodriguez.maria.productos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.itcm.rodriguez.maria.productos.models.Product
import com.itcm.rodriguez.maria.productos.ui.theme.ProductosTheme
import com.itcm.rodriguez.maria.productos.viewmodel.ProductViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProductosTheme {
                val navController = rememberNavController()
                val productViewModel: ProductViewModel = viewModel()
                val products by productViewModel.products.collectAsState()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavigationComponent(navController, products, productViewModel)
                }
            }
        }
    }

    @Composable
    fun NavigationComponent(
        navController: NavHostController,
        products: List<Product>,
        viewModel: ProductViewModel
    ) {
        NavHost(navController = navController, startDestination = "mainScreen") {
            composable("mainScreen") {
                MainScreen(products, navController, { selectedProduct ->
                    navController.navigate("productDetailScreen/${selectedProduct.productId}")
                }, viewModel, {
                    navController.navigate("addProductScreen")
                })
            }
            composable("productDetailScreen/{productId}") { backStackEntry ->
                val productId =
                    backStackEntry.arguments?.getString("productId")?.toInt() ?: return@composable
                val selectedProduct = products.find { it.productId == productId }
                selectedProduct?.let {
                    ProductDetailScreen(product = it, viewModel = viewModel) {
                        navController.popBackStack()
                    }
                }
            }
            composable("addProductScreen") {
                AddProductScreen(viewModel) {
                    navController.popBackStack()
                }
            }
            composable("editProductScreen/{productId}") { backStackEntry ->
                val productId =
                    backStackEntry.arguments?.getString("productId")?.toInt() ?: return@composable
                val selectedProduct = products.find { it.productId == productId }
                selectedProduct?.let {
                    ProductDetailScreen(product = it, viewModel) {
                        navController.popBackStack()
                    }
                }
            }
        }
    }

    @Composable
    fun MainScreen(
        products: List<Product>,
        navController: NavHostController,
        onProductClick: (Product) -> Unit,
        viewModel: ProductViewModel,
        onAddProductClick: () -> Unit
    ) {
        Column {
            TopButtons(onAddProductClick, viewModel::refreshProducts)
            ListProducts(products, onProductClick, viewModel, navController)
        }
    }

    @Composable
    fun TopButtons(onAddProductClick: () -> Unit, onRefreshClick: () -> Unit) {
        LazyRow(modifier = Modifier.padding(4.dp)) {
            item {
                Button(onClick = onAddProductClick) {
                    Text(text = "Agregar producto")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onRefreshClick) {
                    Text(text = "Refrescar productos")
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ListProducts(
        products: List<Product>,
        onProductClick: (Product) -> Unit,
        viewModel: ProductViewModel,
        navController: NavHostController
    ) {
        val showDialog by viewModel.showDialog
        val productToDelete by viewModel.productToDelete

        LazyColumn(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            items(products, key = { it.productId }) { product ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                        .clickable {
                            onProductClick(product)
                        },
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    product.photos.firstOrNull()?.largePhoto ?: ""
                                ),
                                contentDescription = null,
                                modifier = Modifier
                                    .width(100.dp)
                                    .height(100.dp)
                            )
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = product.productName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontSize = 25.sp
                                )
                                Text(
                                    text = product.productNumber,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 15.sp
                                )
                            }
                        }
                        Button(onClick = { viewModel.confirmDeletion(product) }) {
                            Text("Eliminar")
                        }
                        Spacer(modifier = Modifier.width(8.dp))

                    }
                }
            }
        }

        if (showDialog && productToDelete != null) {
            AlertDialog(
                onDismissRequest = {
                    viewModel.cancelDeletion()
                },
                title = {
                    Text(text = "Confirmar eliminación")
                },
                text = {
                    Text("¿Estás seguro de que quieres eliminar este producto?")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.performDeletion()
                        }
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            viewModel.cancelDeletion()
                        }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
