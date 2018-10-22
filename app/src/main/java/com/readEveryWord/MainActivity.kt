package com.readEveryWord

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.miruken.callback.Provider
import com.miruken.callback.TypeHandlers
import com.miruken.callback.policy.HandlerDescriptor
import com.miruken.callback.resolve
import com.miruken.context.Context
import com.miruken.mvc.Navigator
import com.miruken.mvc.android.ViewRegion
import com.miruken.mvc.next
import com.readEveryWord.features.books.BookController
import com.readEveryWord.features.books.BooksController
import com.readEveryWord.features.books.NewTestamentProgressController
import com.readEveryWord.features.books.OldTestamentProgressController
import com.readEveryWord.features.chapters.ChapterController
import com.readEveryWord.features.chapters.ChaptersController

class MainActivity : AppCompatActivity() {
    private val appContext = Context()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        registerComponents()

        val mainRegion = ViewRegion(this)

        appContext.addHandlers(
                Provider(this),
                Navigator(mainRegion),
                TypeHandlers)

        appContext.next<BooksController> { showBooks() }
        setContentView(mainRegion)
    }

    override fun onDestroy() {
        appContext.end()
        super.onDestroy()
    }
}

private fun registerComponents() {
    HandlerDescriptor.getDescriptor<BookController>()
    HandlerDescriptor.getDescriptor<BooksController>()
    HandlerDescriptor.getDescriptor<NewTestamentProgressController>()
    HandlerDescriptor.getDescriptor<OldTestamentProgressController>()
    HandlerDescriptor.getDescriptor<ChaptersController>()
    HandlerDescriptor.getDescriptor<ChapterController>()
}
