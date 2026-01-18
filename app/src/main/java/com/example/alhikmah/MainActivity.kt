package com.example.alhikmah

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.alhikmah.data.AppDatabase
import com.example.alhikmah.data.Book
import com.example.alhikmah.data.BookRepository
import com.example.alhikmah.ui.home.BookCard
import com.example.alhikmah.ui.home.HomeScreen
import com.example.alhikmah.ui.reader.PdfReaderScreen
import com.example.alhikmah.ui.theme.AlHikmahTheme
import kotlin.getValue

class MainActivity : ComponentActivity() {
    private val database by lazy {
        AppDatabase.getDatabase(this)
    }

    private val repository by lazy {
        BookRepository(database.bookDao())
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AlHikmahTheme {
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

                when (val screen = currentScreen) {
                    is Screen.Home -> {
                        HomeScreen(
                            onOpenReader = { book ->
                                currentScreen = Screen.Reader(book)
                            }
                        )
                    }
                    is Screen.Reader -> {
                        PdfReaderScreen(
                            book = screen.book,
                            repository = repository,
                            onBackClick = { currentScreen = Screen.Home }
                        )
                    }
                }
            }
        }
    }
}

sealed class Screen {
    object Home : Screen()
    data class Reader(val book: Book) : Screen()
}