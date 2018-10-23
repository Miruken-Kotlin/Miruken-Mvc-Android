package com.readEveryWord.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.miruken.callback.Provides
import com.miruken.callback.Singleton
import com.miruken.context.Scoped

class DataContext
    @Provides @Singleton
    constructor(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(ReadingRecordTable.CREATE_TABLE)
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }
    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }
    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "ReadEveryWord.db"
    }
}
