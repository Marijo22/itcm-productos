package com.itcm.rodriguez.maria.productos

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.widget.ProgressBar
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.storage.FirebaseStorage
import com.itcm.rodriguez.maria.productos.models.Document
import com.itcm.rodriguez.maria.productos.models.ProductPhoto
import com.itcm.rodriguez.maria.productos.viewmodel.ProductViewModel
import kotlinx.coroutines.launch

@Composable
fun AddProductScreen(viewModel: ProductViewModel, onProductAdded: () -> Unit) {
    val context = LocalContext.current
    var productName by remember { mutableStateOf("") }
    var productNumber by remember { mutableStateOf("") }
    val photoUris = remember { mutableStateListOf<Uri>() }
    val documentUris = remember { mutableStateListOf<Uri>() }
    val photos = remember { mutableStateListOf<ProductPhoto>() }
    val documents = remember { mutableStateListOf<Document>() }
    var selectedPhoto by remember { mutableStateOf<ProductPhoto?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            if (photoUris.size < 5) {
                photoUris.add(uri)
            } else {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("No se pueden agregar más de 5 imágenes.")
                }
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success) {
            cameraImageUri?.let {
                if (photoUris.size < 5) {
                    photoUris.add(it)
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("No se pueden agregar más de 5 imágenes.")
                    }
                }
            }
        }
    }

    val documentPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            if (documentUris.size < 5) {
                documentUris.add(uri)
            } else {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("No se pueden agregar más de 5 documentos.")
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = productName,
            onValueChange = { productName = it },
            label = { Text("Nombre del Producto") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = productNumber,
            onValueChange = { productNumber = it },
            label = { Text("Número del Producto") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            Button(onClick = {
                if (photoUris.size < 5) {
                    imagePickerLauncher.launch("image/*")
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("No se pueden agregar más de 5 imágenes.")
                    }
                }
            }) {
                Text("Agregar Foto")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                if (photoUris.size < 5) {
                    val values = ContentValues().apply {
                        put(MediaStore.Images.Media.TITLE, "Nueva Imagen")
                        put(MediaStore.Images.Media.DESCRIPTION, "Desde la cámara")
                    }
                    cameraImageUri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    cameraLauncher.launch(cameraImageUri)
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("No se pueden agregar más de 5 imágenes.")
                    }
                }
            }) {
                Text("Tomar Foto")
            }
        }
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            items(photoUris) { uri ->
                val index = photoUris.indexOf(uri)
                Box(modifier = Modifier.size(100.dp)) {
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(4.dp)
                            .size(100.dp)
                            .clickable {
                                selectedPhoto = photos.getOrNull(index)
                            }
                    )
                    IconButton(
                        onClick = { photoUris.remove(uri) },
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
                    Text(
                        text = "${index + 1}/5",
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(4.dp),
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            if (documentUris.size < 5) {
                documentPickerLauncher.launch("*/*")
            } else {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("No se pueden agregar más de 5 documentos.")
                }
            }
        }) {
            Text("Agregar Documento")
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            items(documentUris) { uri ->
                val index = documentUris.indexOf(uri)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = getFileName(context, uri),
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)
                            .clickable {
                                openDocument(context, uri.toString())
                            }
                    )
                    IconButton(
                        onClick = { documentUris.remove(uri) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Eliminar",
                            tint = Color.Red
                        )
                    }
                    Text(
                        text = "${index + 1}/5",
                        modifier = Modifier
                            .align(Alignment.Bottom)
                            .padding(4.dp),
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (isLoading) {
            ProgressBar(context)
            Text("Cargando...", fontSize = 16.sp, color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
        }
        Button(
            onClick = {
                if (photoUris.isEmpty() && documentUris.isEmpty()) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Debe agregar al menos una imagen o un documento.")
                    }
                } else {
                    isLoading = true
                    uploadFilesAndSaveProduct(
                        photoUris,
                        documentUris,
                        context,
                        onSuccess = { photoUrls, documentUrls ->
                            photos.clear()
                            documents.clear()
                            photoUrls.forEach { url ->
                                photos.add(ProductPhoto(url, 0))
                            }
                            documentUrls.forEachIndexed { index, url ->
                                val uri = documentUris[index]
                                documents.add(Document(
                                    title = getFileName(context, uri),
                                    fileName = uri.toString(),
                                    fileExtension = getFileExtension(context, uri),
                                    revision = "",
                                    documentFile = url,
                                    productId = 0
                                ))
                            }
                            viewModel.addProduct(
                                productName,
                                productNumber,
                                photos.toList(),
                                documents.toList()
                            )
                            isLoading = false
                            onProductAdded()
                        },
                        onError = { error ->
                            errorMessage = error
                            isLoading = false
                        }
                    )
                }
            },
            enabled = !isLoading
        ) {
            Text("Guardar Producto")
        }
    }

    selectedPhoto?.let { photo ->
        Dialog(onDismissRequest = { selectedPhoto = null }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(photo.largePhoto),
                    contentDescription = null,
                    contentScale = ContentScale.Fit
                )
            }
        }
    }

    errorMessage?.let { message ->
        coroutineScope.launch {
            snackbarHostState.showSnackbar(message)
            errorMessage = null
        }
    }
}

fun getFileExtension(context: Context, uri: Uri): String {
    return context.contentResolver.getType(uri)?.substringAfterLast('/') ?: "unknown"
}

fun openDocument(context: Context, fileName: String) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(Uri.parse(fileName), "*/*")
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    }
    context.startActivity(intent)
}

fun uploadToFirebaseStorage(
    uri: Uri,
    folder: String,
    context: Context,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    val storage = FirebaseStorage.getInstance()
    val storageRef = storage.reference.child("$folder/${System.currentTimeMillis()}_${getFileName(context, uri)}")

    val uploadTask = storageRef.putFile(uri)
    uploadTask.addOnSuccessListener {
        storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
            onSuccess(downloadUri.toString())
        }
    }.addOnFailureListener { exception ->
        onError(exception.message ?: "Error al subir el archivo")
    }
}

fun uploadFilesAndSaveProduct(
    photoUris: List<Uri>,
    documentUris: List<Uri>,
    context: Context,
    onSuccess: (List<String>, List<String>) -> Unit,
    onError: (String) -> Unit
) {
    val photoUrls = mutableListOf<String>()
    val documentUrls = mutableListOf<String>()
    val totalFiles = photoUris.size + documentUris.size
    var filesUploaded = 0

    val onFileUploaded: (Boolean, String?) -> Unit = { success, urlOrError ->
        if (success) {
            if (filesUploaded < photoUris.size) {
                photoUrls.add(urlOrError ?: "")
            } else {
                documentUrls.add(urlOrError ?: "")
            }
            filesUploaded++
            if (filesUploaded == totalFiles) {
                onSuccess(photoUrls, documentUrls)
            }
        } else {
            onError(urlOrError ?: "Error al subir el archivo")
        }
    }

    photoUris.forEach { uri ->
        uploadToFirebaseStorage(uri, "images", context, onSuccess = { url ->
            onFileUploaded(true, url)
        }, onError = { error ->
            onFileUploaded(false, error)
        })
    }

    documentUris.forEach { uri ->
        uploadToFirebaseStorage(uri, "documents", context, onSuccess = { url ->
            onFileUploaded(true, url)
        }, onError = { error ->
            onFileUploaded(false, error)
        })
    }
}
