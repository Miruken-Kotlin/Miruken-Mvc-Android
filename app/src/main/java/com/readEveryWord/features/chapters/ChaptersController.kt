package com.readEveryWord.features.chapters

import com.android.databinding.library.baseAdapters.BR
import com.miruken.callback.Provides
import com.miruken.context.Scoped
import com.miruken.mvc.android.AndroidController
import com.miruken.mvc.android.component.table
import com.miruken.mvc.partial
import com.readEveryWord.R
import com.readEveryWord.domain.Book

class ChaptersController
    @Provides @Scoped
    constructor() : AndroidController() {
    lateinit var book: Book

    fun showChapters(book: Book) {
        this.book = book

        show(R.layout.chapters, BR.ctrl) {
            table(this, R.id.chapter_table, 6).apply {
                 book.chapters.forEach { chapter ->
                    add().partial<ChapterController> {
                        showChapter(book, chapter) }
                 }
            }
        }
    }
}
