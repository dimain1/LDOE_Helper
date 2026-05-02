package com.example.kotlinclient.di

import android.content.Context
import androidx.room.Room
import com.example.kotlinclient.local_cache.AppDatabase
import org.koin.dsl.module

val databaseModule = module {
    single {
        val db = Room.databaseBuilder(
            get<Context>(),
            AppDatabase::class.java,
            "LDOE_Survival_Companion"
        )
            .fallbackToDestructiveMigration(true)
            .allowMainThreadQueries()
            .build()

        // ПРИНУДИТЕЛЬНЫЙ ВЫЗОВ:
        // Это заставит Room создать файл базы, если его нет
        db.openHelper.writableDatabase

        db
    }
}