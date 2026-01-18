package com.example.alhikmah.utils

import android.net.Uri

fun getFileName(context: android.content.Context, uri: Uri): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (index >= 0) {
                    result = it.getString(index)
                }
            }
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/') ?: -1
        if (cut != -1) {
            result = result?.substring(cut + 1)
        }
    }
    return result
}

fun saveFileToInternalStorage(
    context: android.content.Context,
    uri: Uri,
    fileName: String
): String {
    val inputStream = context.contentResolver.openInputStream(uri)
    val file = java.io.File(context.filesDir, "books/$fileName")
    file.parentFile?.mkdirs()

    inputStream?.use { input ->
        file.outputStream().use { output ->
            input.copyTo(output)
        }
    }

    return file.absolutePath
}

suspend fun extractMetadata(
    context: android.content.Context,
    filePath: String,
    fileType: String
): Map<String, String> {
    //for now metadata basic
    // TODO: implement actual metadata extraction
    return mapOf(
        "title" to java.io.File(filePath).nameWithoutExtension,
        "author" to "Unknown",
        "pages" to "0"
    )
}