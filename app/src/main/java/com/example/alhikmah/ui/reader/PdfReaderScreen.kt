package com.example.alhikmah.ui.reader

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.alhikmah.data.Book
import com.example.alhikmah.data.BookRepository
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.github.barteksc.pdfviewer.util.FitPolicy
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfReaderScreen(
    book: Book,
    repository: BookRepository,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var currentPage by remember { mutableIntStateOf(book.currentPage) }
    var totalPages by remember { mutableIntStateOf(book.totalPages) }
    var isLoading by remember { mutableStateOf(true) }
    var showControls by remember { mutableStateOf(true) }
    var pageInput by remember { mutableStateOf("") }
    var showPageDialog by remember { mutableStateOf(false) }

    // Night mode state
    var isNightMode by remember { mutableStateOf(false) }

    // Zoom level
    var zoomLevel by remember { mutableFloatStateOf(1f) }

    // PDF View reference
    var pdfView by remember { mutableStateOf<PDFView?>(null) }

    // Auto-hide controls
    LaunchedEffect(showControls) {
        if (showControls) {
            delay(3000)
            showControls = false
        }
    }

    // Save progress periodically
    LaunchedEffect(currentPage) {
        if (currentPage > 0 && !isLoading) {
            scope.launch {
                repository.updateReadingProgress(book.id, currentPage)

                // Save reading session every 10 pages
                if (currentPage % 10 == 0) {
                    val percentage = if (totalPages > 0) {
                        (currentPage.toFloat() / totalPages * 100)
                    } else 0f

                    repository.saveReadingSession(
                        bookId = book.id,
                        page = currentPage,
                        percentage = percentage,
                        duration = 0 // TODO: Calculate duration
                    )
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // PDF Viewer
        AndroidView(
            factory = { ctx ->
                PDFView(ctx, null).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    val file = File(book.filePath)

                    fromFile(file)
                        .defaultPage(book.currentPage)
                        .enableSwipe(true)
                        .swipeHorizontal(false)
                        .enableDoubletap(true)
                        .enableAnnotationRendering(true)
                        .scrollHandle(DefaultScrollHandle(ctx))
                        .spacing(10)
                        .pageFitPolicy(FitPolicy.WIDTH)
                        .nightMode(isNightMode)
                        .onLoad { nbPages ->
                            isLoading = false
                            totalPages = nbPages

                            // Update total pages di database jika belum ada
                            if (book.totalPages == 0) {
                                scope.launch {
                                    repository.updateBook(
                                        book.copy(totalPages = nbPages)
                                    )
                                }
                            }
                        }
                        .onPageChange { page, pageCount ->
                            currentPage = page
                        }
                        .onError { throwable ->
                            throwable.printStackTrace()
                            isLoading = false
                        }
                        .onTap {
                            showControls = !showControls
                            true
                        }
                        .load()

                    pdfView = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Loading indicator
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text("Memuat PDF...")
                }
            }
        }

        // Top Controls
        AnimatedVisibility(
            visible = showControls && !isLoading,
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            TopAppBar(
                title = {
                    Text(
                        book.title,
                        maxLines = 1,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali")
                    }
                },
                actions = {
                    // Night mode toggle
                    IconButton(onClick = {
                        isNightMode = !isNightMode
                        // Reload PDF dengan night mode baru
                        pdfView?.let { pdf ->
                            val file = File(book.filePath)
                            pdf.fromFile(file)
                                .defaultPage(currentPage)
                                .nightMode(isNightMode)
                                .enableSwipe(true)
                                .swipeHorizontal(false)
                                .enableDoubletap(true)
                                .scrollHandle(DefaultScrollHandle(context))
                                .spacing(10)
                                .pageFitPolicy(FitPolicy.WIDTH)
                                .onPageChange { page, pageCount ->
                                    currentPage = page
                                }
                                .load()
                        }
                    }) {
                        Icon(
                            if (isNightMode) Icons.Filled.LightMode else Icons.Default.DarkMode,
                            if (isNightMode) "Mode Terang" else "Mode Gelap"
                        )
                    }


                    // Bookmark
                    IconButton(onClick = {
                        scope.launch {
                            repository.updateBook(
                                book.copy(isFavorite = !book.isFavorite)
                            )
                        }
                    }) {
                        Icon(
                            if (book.isFavorite) Icons.Filled.Bookmark else Icons.Default.BookmarkBorder,
                            "Bookmark"
                        )
                    }
                }
            )
        }

        // Bottom Controls
        AnimatedVisibility(
            visible = showControls && !isLoading,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                tonalElevation = 3.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Page info and slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showPageDialog = true }) {
                            Text("Hal ${currentPage + 1} / $totalPages")
                        }

                        Text(
                            "${((currentPage.toFloat() / totalPages.coerceAtLeast(1)) * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Slider(
                        value = currentPage.toFloat(),
                        onValueChange = { newPage ->
                            currentPage = newPage.toInt()
                            pdfView?.jumpTo(newPage.toInt())
                        },
                        valueRange = 0f..(totalPages - 1).toFloat().coerceAtLeast(0f),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Navigation buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(
                            onClick = {
                                if (currentPage > 0) {
                                    pdfView?.jumpTo(currentPage - 1)
                                }
                            },
                            enabled = currentPage > 0
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Halaman Sebelumnya")
                        }

                        IconButton(
                            onClick = {
                                if (currentPage < totalPages - 1) {
                                    pdfView?.jumpTo(currentPage + 1)
                                }
                            },
                            enabled = currentPage < totalPages - 1
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, "Halaman Selanjutnya")
                        }
                    }
                }
            }
        }

        // Go to page dialog
        if (showPageDialog) {
            AlertDialog(
                onDismissRequest = { showPageDialog = false },
                title = { Text("Pergi ke Halaman") },
                text = {
                    OutlinedTextField(
                        value = pageInput,
                        onValueChange = { pageInput = it.filter { char -> char.isDigit() } },
                        label = { Text("Nomor Halaman (1-$totalPages)") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val page = pageInput.toIntOrNull()
                            if (page != null && page in 1..totalPages) {
                                pdfView?.jumpTo(page - 1)
                                pageInput = ""
                                showPageDialog = false
                            }
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        pageInput = ""
                        showPageDialog = false
                    }) {
                        Text("Batal")
                    }
                }
            )
        }
    }
}

@Composable
fun AnimatedVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    androidx.compose.animation.AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically(),
        exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.slideOutVertically()
    ) {
        content()
    }
}