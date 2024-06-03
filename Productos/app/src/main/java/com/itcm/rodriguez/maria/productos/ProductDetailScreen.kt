package com.itcm.rodriguez.maria.productos

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.itcm.rodriguez.maria.productos.models.Document
import com.itcm.rodriguez.maria.productos.models.Product
import com.itcm.rodriguez.maria.productos.models.ProductPhoto
import com.itcm.rodriguez.maria.productos.viewmodel.ProductViewModel
import android.content.ContentValues
import android.provider.MediaStore

@Composable
fun ProductDetailScreen(
    product: Product,
    viewModel: ProductViewModel,
    onProductUpdated: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var productName by remember { mutableStateOf(product.productName) }
    var productNumber by remember { mutableStateOf(product.productNumber) }
    var documents by remember { mutableStateOf(product.documents) }
    var photos by remember { mutableStateOf(product.photos) }

    var tempPhotos by remember { mutableStateOf(photos) }
    var tempDocuments by remember { mutableStateOf(documents) }

    val context = LocalContext.current
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                if (tempPhotos.size < 5) {
                    tempPhotos = tempPhotos + ProductPhoto(
                        largePhoto = it.toString(),
                        productId = product.productId
                    )
                }
            }
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success: Boolean ->
            if (success) {
                cameraImageUri?.let {
                    if (tempPhotos.size < 5) {
                        tempPhotos = tempPhotos + ProductPhoto(
                            largePhoto = it.toString(),
                            productId = product.productId
                        )
                    }
                }
            }
        }
    )

    val documentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                if (tempDocuments.size < 5) {
                    val documentName = getFileName(context, it)
                    tempDocuments = tempDocuments + Document(
                        title = documentName,
                        fileName = documentName,
                        fileExtension = documentName.substringAfterLast('.', ""),
                        revision = "1",
                        documentFile = it.toString(),
                        productId = product.productId
                    )
                }
            }
        }
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        if (isEditing) {
            OutlinedTextField(
                value = productName,
                onValueChange = { productName = it },
                label = { Text("Product Name") },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(text = productName, style = MaterialTheme.typography.headlineLarge)
        }
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            items(tempPhotos) { photo ->
                Box(modifier = Modifier.size(300.dp)) {
                    Image(
                        painter = rememberAsyncImagePainter(photo.largePhoto),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                    )
                    if (isEditing) {
                        IconButton(
                            onClick = { tempPhotos = tempPhotos - photo },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Eliminar",
                                tint = Color.Red
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (isEditing) {
            Row {
                OutlinedButton(
                    onClick = {
                        if (tempPhotos.size < 5) {
                            galleryLauncher.launch("image/*")
                        }
                    },
                    modifier = Modifier.padding(1.dp)
                ) {
                    Text(text = "Agregar imagen", modifier = Modifier.padding(0.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(
                    onClick = {
                        if (tempPhotos.size < 5) {
                            val values = ContentValues().apply {
                                put(MediaStore.Images.Media.TITLE, "Nueva Imagen")
                                put(MediaStore.Images.Media.DESCRIPTION, "Desde la cámara")
                            }
                            cameraImageUri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                            cameraLauncher.launch(cameraImageUri)
                        }
                    },
                    modifier = Modifier.padding(1.dp)
                ) {
                    Text(text = "Tomar Foto")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = productNumber,
                onValueChange = { productNumber = it },
                label = { Text("Product Number") },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text = "Numero de producto: $productNumber",
                fontSize = 18.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Documentos del producto",
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (isEditing) {
            OutlinedButton(onClick = {
                if (tempDocuments.size < 5) {
                    documentLauncher.launch(arrayOf("*/*"))
                }
            }) {
                Text(text = "Agregar documento")
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        LazyColumn(modifier = Modifier.height(100.dp)) {
            items(tempDocuments) { document ->
                Card(
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxWidth()
                        .clickable { openDocument(context, document.documentFile) }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            modifier = Modifier
                                .weight(1f)
                                .padding(8.dp),
                            text = document.title,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray,
                            fontStyle = FontStyle.Italic
                        )
                        if (isEditing) {
                            IconButton(
                                onClick = { tempDocuments = tempDocuments - document },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Eliminar",
                                    tint = Color.Red
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isEditing) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(
                    onClick = {
                        viewModel.editProduct(
                            id = product.productId,
                            name = productName,
                            number = productNumber,
                            photos = tempPhotos,
                            documents = tempDocuments
                        )
                        onProductUpdated()
                        isEditing = false
                    }
                ) {
                    Text(text = "Guardar", fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = {
                    tempPhotos = photos
                    tempDocuments = documents
                    isEditing = false
                }) {
                    Text(text = "Cancelar", fontSize = 20.sp)
                }
            }
        } else {
            TextButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { isEditing = true }
            ) {
                Text(text = "Editar", fontSize = 20.sp)
            }
        }
    }
}

fun getFileName(context: Context, uri: Uri): String {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        try {
            cursor?.let {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (columnIndex >= 0) {
                        result = it.getString(columnIndex)
                    }
                }
            }
        } finally {
            cursor?.close()
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/')
        if (cut != -1) {
            result = result?.substring(cut!! + 1)
        }
    }
    return result ?: "Documento"
}

@Preview(showSystemUi = true)
@Composable
fun ProductPreview() {
    val product = Product(
        productId = 0, productName = "PS4", productNumber = "1212113421", photos = listOf(
            ProductPhoto(
                largePhoto = "https://gmedia.playstation.com/is/image/SIEPDC/ps4-product-thumbnail-01-en-14sep21?\$facebook$",
                productId = 0
            ), ProductPhoto(
                largePhoto = "https://m.media-amazon.com/images/I/51+AvgQs50L.jpg",
                productId = 0
            )
        ), documents = listOf(
            Document(
                title = "string",
                fileName = "string",
                fileExtension = "string",
                revision = "string",
                documentFile = "string",
                productId = 0
            ), Document(
                title = "string",
                fileName = "string",
                fileExtension = "string",
                revision = "string",
                documentFile = "string",
                productId = 0
            )
        )
    )
    ProductDetailScreen(
        product = product,
        viewModel = ProductViewModel(),
        onProductUpdated = {})
}
