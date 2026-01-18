package com.example.alhikmah.ui.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.alhikmah.data.AppDatabase
import com.example.alhikmah.data.Book
import com.example.alhikmah.data.BookRepository
import com.example.alhikmah.utils.extractMetadata
import com.example.alhikmah.utils.getFileName
import com.example.alhikmah.utils.saveFileToInternalStorage
import kotlinx.coroutines.launch

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(){
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    //DATABASE SETUP AND REPOSITORY
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { BookRepository(database.bookDao()) }

    // STATE FOR BOOKS LIST
    val books by repository.allBooks.collectAsState(emptyList())

    // FILE PICKER LAUNCHER
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) {
        uri: Uri? ->
        uri?.let {
            scope.launch {
                //File process
                val fileName = getFileName(context, uri) ?: "Unknown"
                val fileType = when {
                    fileName.endsWith(".pdf", ignoreCase = true) -> "PDF"
                    fileName.endsWith(".epub", ignoreCase = true) -> "EPUB"
                    else -> "Unknown"
                }

                // Save file to internal storage
                val savedPath = saveFileToInternalStorage(context, uri, fileName)

                //EXTRACT METADATA
                val metadata = extractMetadata(context, savedPath, fileType)

                val newBook = Book(
                    title = metadata["title"] ?: fileName,
                    author = metadata["author"] ?: "Unknown",
                    filePath = savedPath,
                    fileType = fileType,
                    totalPages = metadata["pages"]?.toIntOrNull() ?: 0
                )

                repository.addBook(newBook)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Al-Hikmah") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Launch file picker untuk PDF dan EPUB
                    filePickerLauncher.launch("*/*")
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Ebook")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (books.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Books Empty",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Tap + For Add PDF Or EPUB",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // List of books
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(books) { book ->
                        BookCard (
                            book = book,
                            onClick = { /* Navigate ke reader */ },
                            onLongClick = { /* Show options */ }
                        )
                    }
                }
            }
        }
    }
}





