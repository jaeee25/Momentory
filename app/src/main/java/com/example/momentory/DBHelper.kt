package com.example.momentory

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, "testdb", null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("create table FRIENDS_DB (" +
                "_id integer primary key autoincrement," +
                "name not null," +
                "message not null)"
        )
        db?.execSQL("INSERT INTO FRIENDS_DB (name, message) VALUES ('John Doe', 'Hello there!')")
        db?.execSQL("INSERT INTO FRIENDS_DB (name, message) VALUES ('Jane Smith', 'Good morning!')")
        db?.execSQL("INSERT INTO FRIENDS_DB (name, message) VALUES ('Mark Lee', 'How are you?')")
        db?.execSQL("INSERT INTO FRIENDS_DB (name, message) VALUES ('Anna Kim', 'See you soon!')")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS FRIENDS_DB")
        onCreate(db)
    }
}